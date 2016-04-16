package com.cnt5106.p2p;

import java.util.*;

/**
 * Created by Dylan Richardson on 4/16/16.
 *
 * Repeated task to determine the next preferred neighbor selection
 */
public class PreferredNeighborsTracker extends TimerTask {

    private HashSet<PeerStream> currentNeighbors;
    private int numPreferredNeighbors;

    public PreferredNeighborsTracker(int numPreferredNeighbors)
    {
        super();
        this.numPreferredNeighbors = numPreferredNeighbors;
    }

    @Override
    public void run()
    {
        ThreadManager tm = ThreadManager.getInstance();
        PriorityQueue<PeerStream> orderedStreams = tm.getDownloadQueue();
        HashSet<PeerStream> newPrefs = new HashSet<>(numPreferredNeighbors);
        for (int i = 0; i != numPreferredNeighbors; ++i)
        {
            newPrefs.add(orderedStreams.poll());
        }

        for (PeerStream ps : newPrefs)
        {
            // Remove every recurring preferred neighbor; nothing is to be sent to these
            if (currentNeighbors.contains(ps))
            {
                currentNeighbors.remove(ps);
            }
            // Every new preferred neighbor must be sent an 'unchoke' message
            else
            {
                ps.unchokeRemote();
            }
        }
        // Remaining PeerStreams in old hashset are now communicating with choked neighbors
        for (PeerStream ps : currentNeighbors)
        {
            ps.chokeRemote();
        }
        currentNeighbors = newPrefs;
    }
}
