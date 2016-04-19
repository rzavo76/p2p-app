package com.cnt5106.p2p;

import java.io.IOException;
import java.util.*;

/**
 * Created by Dylan Richardson on 4/16/16.
 *
 * Repeated task to determine the next preferred neighbor selection
 */
public class PreferredNeighborsTracker extends TimerTask {

    private HashSet<PeerStream> currentPrefNeighbors;
    private int numPreferredNeighbors;

    public PreferredNeighborsTracker(int numPreferredNeighbors)
    {
        super();
        this.numPreferredNeighbors = numPreferredNeighbors;
    }

    @Override
    public void run()
    {
        BTLogger log = BTLogger.getInstance();
        ThreadManager tm = ThreadManager.getInstance();
        PriorityQueue<PeerStream> orderedStreams = tm.getDownloadQueue();
        HashSet<PeerStream> newPrefs = new HashSet<>(numPreferredNeighbors);

        int newPrefIDs[] = new int[numPreferredNeighbors];
        for (int i = 0; i != numPreferredNeighbors; ++i)
        {
            PeerStream prefNeighbor = orderedStreams.poll();
            newPrefIDs[i] = prefNeighbor.getTargetPeerID();
            newPrefs.add(prefNeighbor);
        }
        try {
            log.writeToLog(log.changeOfPrefNeighbors(newPrefIDs));
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        for (PeerStream ps : newPrefs)
        {
            // Remove every recurring preferred neighbor; nothing is to be sent to these
            if (currentPrefNeighbors.contains(ps))
            {
                currentPrefNeighbors.remove(ps);
            }
            // Every new preferred neighbor must be sent an 'unchoke' message
            else
            {
                ps.unchokeRemote();
            }
        }
        // Remaining PeerStreams in old hashset are now communicating with choked neighbors
        for (PeerStream ps : currentPrefNeighbors)
        {
            ps.chokeRemote();
        }
        currentPrefNeighbors = newPrefs;
    }
}
