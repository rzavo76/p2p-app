package com.cnt5106.p2p;

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

    public void runTasks()
    {
        prefNeighborTimer.schedule(
                new PreferredNeighborsTracker(this, numPreferredNeighbors),
                0,
                prefNeighborInterval * 1000);
        optUnchokeTimer.schedule(
                new OptimisicallyUnchokeTracker(this),
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
class OptimisicallyUnchokeTracker extends TimerTask {

    private final NeighborTaskManager manager;
    private Random randomizer;
    private PeerStream current;

    public OptimisicallyUnchokeTracker(NeighborTaskManager manager)
    {
        this.manager = manager;
        this.randomizer = new Random();
        this.current = null;
    }

    @Override
    public void run()
    {
        HashSet<PeerStream> prefNeighbors;
        synchronized (manager) {
            prefNeighbors = manager.getPrefNeighbors();
            ArrayList<PeerStream> interested = ThreadManager.getInstance().getInterestedNeighbors();
            for (int i = 0; i < interested.size(); ++i) {
                // Prune the list of interested PeerStreams so no Preferred Neighbors remain
                if (prefNeighbors.contains(interested.get(i))) {
                    int lastIndex = prefNeighbors.size() - 1;
                    interested.set(i, interested.get(lastIndex));
                    interested.remove(lastIndex);
                }
            }
            // No optimistic neighbors
            if (interested.isEmpty()) {
                // If the previous optimistically unchoked neighbor is not in preferred neighbors, choke 'em!
                // Not sure how this works with an empty interested list, I don't THINK this can ever happen but not sure
                if (current != null)
                {
                    current.setOptimUnchokedNeighbor(false);
                    current = null;
                }
            } else {
                int index = randomizer.nextInt(interested.size());
                PeerStream next = interested.get(index);
                if (current != next)
                {
                    if (current != null)
                    {
                        current.setOptimUnchokedNeighbor(false);
                    }
                    next.setOptimUnchokedNeighbor(true);
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
        ArrayList<PeerStream> removedInterestedNeighbors = new ArrayList<PeerStream>();
        int newPrefIDs[] = new int[numPreferredNeighbors];
        if (tm.hasFullFile())
        {
             for (int i = 0; i != numPreferredNeighbors && !interestedNeighbors.isEmpty(); ++i)
             {
                 int nextIndex = randomizer.nextInt(interestedNeighbors.size());
                 int lastIndex = interestedNeighbors.size() - 1;
                 PeerStream prefNeighbor = interestedNeighbors.get(nextIndex);
                 newPrefIDs[i] = prefNeighbor.getTargetPeerID();
                 interestedNeighbors.set(nextIndex, interestedNeighbors.get(lastIndex));
                 removedInterestedNeighbors.add(interestedNeighbors.get(lastIndex));
                 interestedNeighbors.remove(lastIndex);
                 newPrefs.add(prefNeighbor);
             }
            for (PeerStream ps : removedInterestedNeighbors)
            {
                interestedNeighbors.add(ps);
            }
        }
        else
        {
            PriorityQueue<PeerStream> orderedStreams = constructDownloadQueue(interestedNeighbors);
            for (int i = 0; i != numPreferredNeighbors && !orderedStreams.isEmpty(); ++i)
            {
                PeerStream prefNeighbor = orderedStreams.poll();
                newPrefIDs[i] = prefNeighbor.getTargetPeerID();
                newPrefs.add(prefNeighbor);
                prefNeighbor.resetDownloadRate();
            }
        }
        try {
            log.writeToLog(log.changeOfPrefNeighbors(newPrefIDs));
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
                // Every new preferred neighbor must be set
                else {
                    ps.setPreferredNeighbor(true);
                }
            }
            for (PeerStream ps : currentPrefNeighbors) {
                ps.setPreferredNeighbor(false);
            }
            manager.setPrefNeighbors(newPrefs);
        }
    }
}
