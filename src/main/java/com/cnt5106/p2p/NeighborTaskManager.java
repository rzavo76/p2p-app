package com.cnt5106.p2p;

import com.cnt5106.p2p.models.MessageType;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Dylan Richardson on 4/19/2016.
 *
 * The NeighborTaskManager encapsulates two task runners. The first deals with the optimistically unchoked
 * neighbor every m seconds, as specified in the protocol, and the second deals with choosing the preferred
 * neighbors of this peer every p seconds, also described in the protocol. Additionally, the manager itself
 * acts as a shared interface for the two task runners to communicate since their tasks are related.
 */
public final class NeighborTaskManager {

    private Timer prefNeighborTimer;
    private Timer optUnchokeTimer;

    private long optUnchokeInterval;
    private long prefNeighborInterval;
    private int numPreferredNeighbors;

    private HashSet<PeerStream> prefNeighbors;

    public NeighborTaskManager(long m, long p, int numPreferredNeighbors)
    {
        this.optUnchokeInterval = m;
        this.prefNeighborInterval = p;
        this.prefNeighborTimer = new Timer();
        this.optUnchokeTimer = new Timer();
        this.numPreferredNeighbors = numPreferredNeighbors;
        this.prefNeighbors = new HashSet<>(numPreferredNeighbors);
    }

    public void close()
    {
        prefNeighborTimer.cancel();
        optUnchokeTimer.cancel();
        prefNeighborTimer = null;
        optUnchokeTimer = null;
    }

    public void runTasks()
    {
        prefNeighborTimer.schedule(
                new PreferredNeighborsTracker(this, numPreferredNeighbors),
                0,
                prefNeighborInterval * 1000);
        optUnchokeTimer.schedule(
                new OptimisticallyUnchokeTracker(this),
                0, optUnchokeInterval * 1000);
    }

    public synchronized HashSet<PeerStream> getPrefNeighbors()
    {
        return prefNeighbors;
    }

    public synchronized void setPrefNeighbors(HashSet<PeerStream> prefNeighbors)
    {
        this.prefNeighbors = prefNeighbors;
    }

}

/**
 * Repeating task to randomly determine the next optimistically unchoked neighbor selection
 */
class OptimisticallyUnchokeTracker extends TimerTask {

    private final NeighborTaskManager manager;
    private Random randomizer;
    private PeerStream current;

    public OptimisticallyUnchokeTracker(NeighborTaskManager manager)
    {
        this.manager = manager;
        this.randomizer = new Random();
        this.current = null;
    }

    @Override
    public void run()
    {
        BTLogger log = BTLogger.getInstance();
        HashSet<PeerStream> prefNeighbors;
        synchronized (manager) {
            prefNeighbors = manager.getPrefNeighbors();

            ArrayList<PeerStream> interested = ThreadManager.getInstance().getInterestedNeighbors();
            ArrayList<PeerStream> copyInterest = new ArrayList<>(interested.size());
            for (PeerStream ps : interested)
                copyInterest.add(ps);
            for (int i = 0; i < copyInterest.size(); ++i) {
                // Prune the list of interested PeerStreams so no Preferred Neighbors remain
                if (prefNeighbors.contains(copyInterest.get(i))) {
                    int lastIndex = copyInterest.size() - 1;
                    copyInterest.set(i, copyInterest.get(lastIndex));
                    copyInterest.remove(lastIndex);
                }
            }
            // No optimistic neighbors
            if (copyInterest.isEmpty()) {
                // If the previous optimistically unchoked neighbor is not in preferred neighbors, choke 'em!
                // Not sure how this works with an empty interested list, I don't THINK this can ever happen but not sure
                if (current != null)
                {
                    current.setOptimUnchokedNeighbor(false);
                    current = null;
                }
            } else {
                int index = randomizer.nextInt(copyInterest.size());
                PeerStream next = copyInterest.get(index);
                if (current != next)
                {
                    if (current != null)
                    {
                        current.setOptimUnchokedNeighbor(false);
                    }
                    next.setOptimUnchokedNeighbor(true);
                    current = next;
                    try {
                        next.outputByteArray(MessageHandler.getInstance().makeMessage(MessageType.UNCHOKE));
                        log.writeToLog(log.changeOfOUNeighbor(current.getTargetPeerID()));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

/**
 * Repeating task to determine the next preferred neighbor selection, to which this peer will upload pieces of the file
 * in addition to the current optimistically unchoked neighbor
 */
class PreferredNeighborsTracker extends TimerTask {

    private final NeighborTaskManager manager;
    private int numPreferredNeighbors;
    private Comparator<PeerStream> comparator;
    private Random randomizer;

    public PreferredNeighborsTracker(NeighborTaskManager manager, int numPreferredNeighbors)
    {
        this.manager = manager;
        this.numPreferredNeighbors = numPreferredNeighbors;
        comparator = new Comparator<PeerStream>() {
            @Override
            public int compare(PeerStream p1, PeerStream p2) {
                if (p1.getDownloadRate() > p2.getDownloadRate()){
                    return -1;
                }
                else if (p1.getDownloadRate() < p2.getDownloadRate())
                {
                    return 1;
                }
                else
                {
                    return 0;
                }
            }
        };
        randomizer = new Random();
    }

    private PriorityQueue<PeerStream> constructDownloadQueue(ArrayList<PeerStream> interestedPeers)
    {
        PriorityQueue<PeerStream> downloadQueue = new PriorityQueue<>(interestedPeers.size(), comparator);
        downloadQueue.addAll(interestedPeers);
        return downloadQueue;
    }

    @Override
    public void run()
    {
        BTLogger log = BTLogger.getInstance();
        ThreadManager tm = ThreadManager.getInstance();
        HashSet<PeerStream> newPrefs = new HashSet<>(numPreferredNeighbors);
        ArrayList<PeerStream> interestedNeighbors = tm.getInterestedNeighbors();
        ArrayList<PeerStream> copyNeighbors = new ArrayList<>(interestedNeighbors.size());
        for (PeerStream ps : interestedNeighbors)
            copyNeighbors.add(ps);
        ArrayList<Integer> newPrefIDs = new ArrayList<>();
        if (tm.hasFullFile())
        {
             for (int i = 0; i != numPreferredNeighbors && !copyNeighbors.isEmpty(); ++i)
             {
                 int nextIndex = randomizer.nextInt(copyNeighbors.size());
                 int lastIndex = copyNeighbors.size() - 1;
                 PeerStream prefNeighbor = copyNeighbors.get(nextIndex);
                 newPrefIDs.add(prefNeighbor.getTargetPeerID());
                 copyNeighbors.set(nextIndex, copyNeighbors.get(lastIndex));
                 copyNeighbors.add(prefNeighbor);
                 copyNeighbors.remove(lastIndex);
                 newPrefs.add(prefNeighbor);
             }
        }
        else
        {
            PriorityQueue<PeerStream> orderedStreams = constructDownloadQueue(interestedNeighbors);
            for (int i = 0; i != numPreferredNeighbors && !orderedStreams.isEmpty(); ++i)
            {
                PeerStream prefNeighbor = orderedStreams.poll();
                newPrefIDs.add(prefNeighbor.getTargetPeerID());
                newPrefs.add(prefNeighbor);
                prefNeighbor.resetDownloadRate();
            }
        }
        try {
            if (!newPrefIDs.isEmpty()) {
                int newPrefArray[] = new int[newPrefIDs.size()];
                for (int i = 0; i != newPrefIDs.size(); ++i)
                    newPrefArray[i] = newPrefIDs.get(i);
                log.writeToLog(log.changeOfPrefNeighbors(newPrefArray));
            }
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        synchronized (manager) {
            HashSet<PeerStream> currentPrefNeighbors = manager.getPrefNeighbors();
            for (PeerStream ps : newPrefs) {
                // Remove every recurring preferred neighbor; nothing is to be sent to these
                if (currentPrefNeighbors.contains(ps)) {
                    currentPrefNeighbors.remove(ps);
                }
                try {
                    ps.setPreferredNeighbor(true);
                    ps.outputByteArray(MessageHandler.getInstance().makeMessage(MessageType.UNCHOKE));
                }
                catch (Exception e)
                {
                    try {
                        log.writeToLog(Arrays.toString(e.getStackTrace()));
                    }
                    catch (Exception ee)
                    {
                        ee.printStackTrace();
                    }
                }
                // Every new preferred neighbor must be set
            }
            for (PeerStream ps : currentPrefNeighbors) {
                ps.setPreferredNeighbor(false);
            }
            manager.setPrefNeighbors(newPrefs);
        }
    }
}
