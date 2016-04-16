package com.cnt5106.p2p;

import com.cnt5106.p2p.models.RemotePeerInfo;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;

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
    {}

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

            int thisIndex;
            for (thisIndex = 0; thisIndex != peers.size(); ++thisIndex)
            {
                if (peers.get(thisIndex).peerId == myPid)
                {
                    myPeerInfo = peers.get(thisIndex);
                }
            }

            final int numPeers = peers.size() - 1;
            // Initialize basic arrays of sender and receiver threads of size N;
            // very brute force, but this can be adjusted accordingly later
            streams = new PeerStream[numPeers];
            for (int i = 0; i != thisIndex; ++i)
            {
                System.out.println("We should not get here");
                System.out.println(i);
                RemotePeerInfo rpi = peers.get(i);
                streams[i] = new PeerStream(
                        myPeerInfo.peerPort,
                        myPeerInfo.peerAddress,
                        rpi.peerPort,
                        rpi.peerAddress,
                        myPeerInfo.peerId,
                        rpi.peerId);
            }
            for (int i = thisIndex + 1; i <= numPeers; ++i)
            {
                System.out.println("We got here!");
                streams[i] = new PeerStream(myPeerInfo.peerPort, myPeerInfo.peerId);
            }
        }
        catch (Exception e)
        {
            throw e;
        }
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
