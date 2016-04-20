package com.cnt5106.p2p;

import com.cnt5106.p2p.models.RemotePeerInfo;

import java.io.IOException;
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
    private ArrayList<PeerStream> interestedPeers;
    private HashMap<PeerStream, Integer> interestedPeersIndexMap;
    private Random randomizer;
    private NeighborTaskManager neighborTaskManager;
    private int totalPieces;
    private PieceManager pcManager;
    private SocketListener listener = null;
    private int streamsDone = 0;

    private BitSet bitfield;
    private boolean hasFullFile = false;
    // Technically, the above BitSet can be used as its own lock since it's
    // only assigned once. For good programming practice, we will add a dedicated final lock
    private final Object fieldLock;

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
        randomizer = new Random();
        fieldLock = new Object();
        requestBuffer = new HashSet<>();
        interestedPeers = new ArrayList<>();
        interestedPeersIndexMap = new HashMap<>();
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
            int numPrefNeighbors = fp.getNumPreferredNeighbors();
            totalPieces = fp.getNumPieces();
            bitfield = new BitSet(totalPieces);

            neighborTaskManager = new NeighborTaskManager(fp.getOptUnchokeInterval(), fp.getUnchokeInterval(), numPrefNeighbors);

            // Initialize the piece manager
            PieceManager.setInstance(fp.getNumPieces(), fp.getFileSize(), fp.getPieceSize(), myPid, fp.getFileName());
            pcManager = PieceManager.getInstance();

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

            // make folder to hold pieces
            pcManager.makeFolder();
            // split file if you have it
            if(myPeerInfo.hasFile) {
                pcManager.splitFile();
            }

            // initialize the array of connections
            final int numPeers = peers.size() - 1;
            streams = new PeerStream[numPeers];
            //set up the peer connections as "listeners" but don't start
            for (int i = thisIndex; i < numPeers; ++i)
            {
                streams[i] = new PeerStream(
                        myPeerInfo.peerPort,
                        myPeerInfo.peerAddress,
                        myPeerInfo.peerId,
                        totalPieces);
            }
            // start SocketListener to listen for connections and assign to streams
            if(thisIndex < numPeers)
            {
                listener = new SocketListener(myPeerInfo.peerPort, thisIndex, streams);
                listener.start();
            }
            // connect to every peer before this one in the file
            for (int i = 0; i != thisIndex; ++i)
            {
                RemotePeerInfo rpi = peers.get(i);
                streams[i] = new PeerStream(
                        myPeerInfo.peerPort,
                        myPeerInfo.peerAddress,
                        rpi.peerPort,
                        rpi.peerAddress,
                        myPeerInfo.peerId,
                        rpi.peerId,
                        totalPieces);
                streams[i].start();
            }
            // Spin up tasks via the task manager
            neighborTaskManager.runTasks();
        }
        catch (Exception e)
        {
            throw e;
        }
    }

    /**
     * This method uses the interestedPeers ArrayList to actually store the peers in
     * conjunction with the interestedPeersIndexMap to traverse the ArrayList in O(1) time if a particular
     * peer must be located and removed from the list. Time complexity is more important than space complexity.
     *
     * @param stream The PeerStream calling the method
     * @param isInterested If the PeerStream has switched to interested or not interested
     */
    public synchronized void updateInterested(PeerStream stream, boolean isInterested)
    {
        if (isInterested)
        {
            // If the PeerStream is already in the ArrayList, we don't care about it.
            if (!interestedPeersIndexMap.containsKey(stream))
            {
                interestedPeers.add(stream);
                interestedPeersIndexMap.put(stream, interestedPeers.size() - 1);
            }
        }
        else
        {
            // If the PeerStream is not in the ArrayList, there is nothing to remove.
            if (interestedPeersIndexMap.containsKey(stream))
            {
                // In here, we get the index of the NOTINTERESTED stream, then we get the index
                // of the last element in the list of interested peers. We swap values then remove
                // the end of the array for constant time complexity!!!!
                int index = interestedPeersIndexMap.remove(stream);
                int lastIndex = interestedPeers.size() - 1;
                interestedPeers.set(index, interestedPeers.get(lastIndex));
                interestedPeers.remove(lastIndex);
            }
        }
    }

    public boolean hasFullFile()
    {
        if (!hasFullFile) {
            synchronized (fieldLock) {
                hasFullFile = bitfield.cardinality() == totalPieces;
            }
            if (hasFullFile)
            {
                for (PeerStream peer : streams)
                {
                    if(peer.hasFullFile())
                    {
                        peer.done();
                    }
                }
            }
        }
        return hasFullFile;
    }

    public synchronized void isDone() {
        if(streamsDone == streams.length) {
            try {
                PieceManager.getInstance().mergePieces();
            }
            catch(Exception e) {
                try {
                    BTLogger.getInstance().writeToLog(Arrays.toString(e.getStackTrace()));
                }
                catch (IOException ioe) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void streamFinished()
    {
        ++streamsDone;
    }

    public int currentPieces()
    {
        return bitfield.cardinality();
    }

    public synchronized void broadcastHaveMessage(byte[] message) {
        for (PeerStream ps : streams)
        {
            ps.outputByteArray(message);
        }
    }

    public synchronized ArrayList<PeerStream> getInterestedNeighbors()
    {
        return interestedPeers;
    }

    public void addPieceIndex(int index)
    {
        synchronized (fieldLock) {
            bitfield.set(index);
            requestBuffer.remove(index);
        }
    }

    public byte[] getBitField()
    {
        synchronized (fieldLock) {
            return bitfield.toByteArray();
        }
    }

    public void handleIncompleteRequest(int index)
    {
        synchronized (fieldLock) {
            requestBuffer.remove(index);
        }
    }

    public int getRandomAvailablePieceIndex(PeerStream remote)
    {
        synchronized (fieldLock) {
            int index = findPieceIndex(remote);
            if (index != -1)
                requestBuffer.add(index);
            return index;
        }
    }

    public int findPieceIndex(PeerStream remote)
    {
        int index = -1;
        ArrayList<Integer> availPieces = remote.getAvailPieces();
        synchronized (fieldLock) {
            while (!availPieces.isEmpty()) {
                int arrayIndex = randomizer.nextInt(availPieces.size());
                int pieceIndex = availPieces.get(arrayIndex);
                // Don't return pieces for outgoing requests, but also don't delete them from
                // available in case they don't go through
                if (requestBuffer.contains(pieceIndex))
                    continue;
                // If pieces are already owned by our process, we never will want them from a
                // peer again. Even though they are still available, we are removing them from
                // the peer info class corresponding to that peer.
                if (bitfield.get(pieceIndex)) {
                    // The swap is to ensure an O(1) random lookup. There will never
                    // be any need to get specific index, because it's always randomized. Therefore,
                    // switching order of available piece indices presents no problem
                    Integer lastPiece = availPieces.get(availPieces.size() - 1);
                    availPieces.set(arrayIndex, lastPiece);
                    availPieces.remove(availPieces.size() - 1);
                } else {
                    index = pieceIndex;
                    requestBuffer.add(index);
                    break;
                }
            }
        }
        remote.setAvailPieces(availPieces);
        return index;
    }
}
