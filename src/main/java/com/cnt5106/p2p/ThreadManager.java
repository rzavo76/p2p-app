package com.cnt5106.p2p;

import com.cnt5106.p2p.models.RemotePeerInfo;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by Dylan Richardson on 3/13/16.
 *
 * Maintains communication with every sender and
 * receiver thread.
 */
public class ThreadManager {
    private PeerStream[] streams;
    private ArrayList<RemotePeerInfo> peers;
    private RemotePeerInfo myPeerInfo;
    private peerProcess myPeer;
    private HashSet<Integer> requestBuffer;
    private Timer prefNeighborsTimer;
    private Timer optUnchokeTimer;
    private Comparator<PeerStream> comparator;
    private PriorityQueue<PeerStream> downloadQueue;

    private int numPrefNeighbors;
    private long unchokeInterval;
    private long optUnchokeInterval;

    private static ThreadManager mThreadMgr;

    /**
     * Lazy constructor for the ThreadManager instance
     */
    public static ThreadManager getInstance()
    {
        if (mThreadMgr == null)
        {
            mThreadMgr = new ThreadManager();
        }
        return mThreadMgr;
    }

    /**
     * Private constructor for the singleton
     */
    private ThreadManager()
    {
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
    }

    /**
     * Description: Spins up all sending and receiving communication threads
     * in this PeerProcess by parsing the config files and peer info files.
     *
     * @param myPid This threads Peer ID
     *
     * @throws Exception
     */
    public void createThreads(int myPid) throws Exception
    {
        Path peerInfoFile = FileSystems.getDefault().getPath("PeerInfo.cfg");
        Path configFile = FileSystems.getDefault().getPath("Common.cfg");
        FileParser fp = FileParser.getInstance();
        try {
            peers = fp.getPeersFromFile(peerInfoFile);
            fp.parseConfigFile(configFile);
            numPrefNeighbors = fp.getNumPreferredNeighbors();
            optUnchokeInterval = fp.getOptUnchokeInterval();
            unchokeInterval = fp.getUnchokeInterval();
            int thisIndex;
            for (thisIndex = 0; thisIndex != peers.size(); ++thisIndex)
            {
                if (peers.get(thisIndex).peerId == myPid)
                {
                    myPeerInfo = peers.get(thisIndex);
                    break;
                }
            }

            final int numPeers = peers.size() - 1;
            // Initialize basic arrays of sender and receiver threads of size N;
            // very brute force, but this can be adjusted accordingly later
            streams = new PeerStream[numPeers];
            for (int i = 0; i != thisIndex; ++i)
            {
                RemotePeerInfo rpi = peers.get(i);
                streams[i] = new PeerStream(
                        myPeerInfo.peerPort,
                        myPeerInfo.peerAddress,
                        rpi.peerPort,
                        rpi.peerAddress,
                        myPeerInfo.peerId,
                        rpi.peerId);
                streams[i].start();
            }
            for (int i = thisIndex + 1; i <= numPeers; ++i)
            {
                streams[i] = new PeerStream(myPeerInfo.peerPort, myPeerInfo.peerId);
                streams[i].start();
            }
            prefNeighborsTimer = new Timer();
            prefNeighborsTimer.schedule(
                    new PreferredNeighborsTracker(numPrefNeighbors),
                    0,
                    unchokeInterval);
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    public synchronized PriorityQueue<PeerStream> getDownloadQueue()
    {
        // Add streams to download queue in order of download speed
        downloadQueue.clear();
        for (int i = 0; i != streams.length; ++i)
        {
            downloadQueue.add(streams[i]);
        }
        return downloadQueue;
    }

    // Method to be called by a receiver
    public int findRandomPiece(PeerStream ps)
    {

//        RemotePeerInfo desired = null;
//        for (RemotePeerInfo rpi : peers)
//        {
//            if (ps.peerID == rpi.peerId)
//            {
//                desired = rpi;
//            }
//        }
//
//        if (desired != null)
//        {
//            synchronized (this)
//            {
//                int nextIndex = desired.getAvailablePiece(myPeer.pieces, requestBuffer);
//                requestBuffer.add(nextIndex);
//            }
//            // Get handle of corresponding sending thread
//            Sender sender = senders[receiver.index];
//            synchronized (sender)
//            {
//
//            }
//
//        }
        return 0;
    }

    public ArrayList<RemotePeerInfo> getPeerInfo()
    {
        return peers;
    }

    public RemotePeerInfo getMyPeerInfo()
    {
        return myPeerInfo;
    }
}
