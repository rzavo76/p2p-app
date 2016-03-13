package com.cnt5106.p2p;

import com.cnt5106.p2p.models.RemotePeerInfo;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.rmi.Remote;
import java.util.ArrayList;

/**
 * Created by Dylan Richardson on 3/13/16.
 *
 * Maintains communication with every sender and
 * receiver thread.
 */
public class ThreadManager {
    private Sender[] senders;
    private Receiver[] receivers;
    private Object[] locks;
    private ArrayList<RemotePeerInfo> peers;
    private RemotePeerInfo myPeer;

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

            RemotePeerInfo me = null;
            for (RemotePeerInfo rpi : peers)
            {
                if (rpi.peerId == myPid)
                {
                    me = rpi;
                }
            }

            final int numPeers = peers.size() - 1;
            // Initialize basic arrays of sender and receiver threads of size N;
            // very brute force, but this can be adjusted accordingly later
            senders = new Sender[numPeers];
            receivers = new Receiver[numPeers];
            locks = new Object[numPeers];
            for (int i = 0; i != numPeers + 1; ++i)
            {
                RemotePeerInfo rpi = peers.get(i);
                if (me != null && rpi != me)
                {
                    senders[i] = new Sender(me.peerPort, me.peerAddress, rpi.peerPort, rpi.peerAddress, me.peerId, rpi.peerId);
                    receivers[i] = new Receiver(me.peerPort, me.peerAddress, me.peerId, rpi.peerId);
                    locks[i] = new Object();
                    senders[i].start();
                    receivers[i].start();
                }
                else
                {
                    myPeer = rpi;
                    System.out.println("THIS IS ME! My ID: " + rpi.peerId);
                }
            }
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    public ArrayList<RemotePeerInfo> getPeerInfo()
    {
        return peers;
    }

    public RemotePeerInfo getMyPeerInfo()
    {
        return myPeer;
    }
}
