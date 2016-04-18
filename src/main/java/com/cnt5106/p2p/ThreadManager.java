package com.cnt5106.p2p;

import com.cnt5106.p2p.models.RemotePeerInfo;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
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
    private HashSet<Integer> requestBuffer;
    private Timer prefNeighborsTimer;
    private Timer optUnchokeTimer;
    private Comparator<PeerStream> comparator;


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
            // read peers and common config
            peers = fp.getPeersFromFile(peerInfoFile);
            fp.parseConfigFile(configFile);
            numPrefNeighbors = fp.getNumPreferredNeighbors();
            optUnchokeInterval = fp.getOptUnchokeInterval();
            unchokeInterval = fp.getUnchokeInterval();
            // find the index of the peer
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
            // initialize the array of connections
            streams = new PeerStream[numPeers];
            // connect to every peer that connected before
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
            //set up the other peer connections as "listeners"
            for (int i = thisIndex; i < numPeers; ++i)
            {
                streams[i] = new PeerStream(myPeerInfo.peerPort, myPeerInfo.peerId);
                streams[i].start();
            }
            // Spin up timer for choosing preferred neighbors
            prefNeighborsTimer = new Timer();
            prefNeighborsTimer.schedule(
                    new PreferredNeighborsTracker(numPrefNeighbors),
                    0,
                    unchokeInterval * 1000);
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    public synchronized Socket waitForSocket() throws Exception
    {
        //create socket by temporarily accepting from a listener
        ServerSocket listener = new ServerSocket();
        listener.bind(new InetSocketAddress(myPeerInfo.peerAddress, myPeerInfo.peerPort));
        Socket socket = listener.accept();
        listener.close();
        return socket;
    }

    public synchronized PriorityQueue<PeerStream> getDownloadQueue()
    {
        // Add streams to download queue in order of download speed
        // TODO: Only send active peers into download queue
        PriorityQueue<PeerStream> downloadQueue = new PriorityQueue<>(streams.length, comparator);
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
