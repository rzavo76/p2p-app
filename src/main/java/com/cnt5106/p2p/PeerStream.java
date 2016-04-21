package com.cnt5106.p2p;

import com.cnt5106.p2p.models.MessageType;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;

import static com.cnt5106.p2p.models.MessageType.*;

/**
 * Created by Dylan Richardson on 4/10/16.
 *
 * PeerStream acts as the sole communicator with a Peer.
 *
 * If it is making a connection, the PeerID is defined. Otherwise, all PeerThreads will create ServerSockets
 * on the same port and will dynamically assign themselves to PeerIDs as other PeerProcesses are created.
 *
 * PeerStream owns a Sender thread. The PeerStream itself will always be running, waiting for incoming connections.
 */
public class PeerStream extends Thread {

    private int port, targetPort;
    private String hostname, targetHostName;
    private int peerID, targetPeerID;
    private ArrayList<Integer> availPieces;
    private BitSet pieces;
    private int totalPieces;
    private boolean isFull = false;
    private MessageHandler msgHandler;
    private boolean connector;
    private BTLogger btLogger;
    private PieceManager pcManager;
    private ThreadManager threadManager;
    private long bytesDownloaded;
    private final Object downloadLock;
    private final Object queueLock;
    private boolean hasUpdatedInterested = false;
    private boolean receivedInterested = false;
    private boolean readyForHave = false;
    private boolean readyToSend = false;
    private boolean done = false;
    private boolean isOptimUnchokedNeighbor = false;
    private boolean isPreferredNeighbor = false;
    private int outgoingIndexRequest = -1;
    public Sender sender = null;
    public Socket socket = null;

    PeerStream(int port, String hostName, int targetPort, String targetHostName,
               int peerID, int targetPeerID, int numberOfPieces) throws Exception
    {
        this.connector = true;
        this.port = port;
        this.targetPort = targetPort;
        this.targetHostName = targetHostName;
        this.peerID = peerID;
        this.targetPeerID = targetPeerID;
        this.hostname = hostName;
        this.availPieces = new ArrayList<>(numberOfPieces);
        this.pieces = new BitSet(numberOfPieces);
        this.totalPieces = numberOfPieces;
        this.msgHandler = MessageHandler.getInstance();
        this.btLogger = BTLogger.getInstance();
        this.pcManager = PieceManager.getInstance();
        this.threadManager = ThreadManager.getInstance();
        this.bytesDownloaded = 0l;
        this.downloadLock = new Object();
        this.queueLock = new Object();
    }

    PeerStream(int port, String hostName, int peerID, int numberOfPieces) throws Exception
    {
        this.connector = false;
        this.peerID = peerID;
        this.port = port;
        this.hostname = hostName;
        this.availPieces = new ArrayList<>(numberOfPieces);
        this.pieces = new BitSet(numberOfPieces);
        this.totalPieces = numberOfPieces;
        this.msgHandler = MessageHandler.getInstance();
        this.btLogger = BTLogger.getInstance();
        this.pcManager = PieceManager.getInstance();
        this.threadManager = ThreadManager.getInstance();
        this.bytesDownloaded = 0l;
        this.downloadLock = new Object();
        this.queueLock = new Object();
    }

    public void run()
    {
        try
        {
            if (connector)
            {
                boolean scanning = true;
                while(scanning)
                {
                    try
                    {
                        //attempt to connect to socket
                        socket = new Socket(targetHostName, port);
                        scanning = false;
                    }
                    catch(Exception e)
                    {
                        try
                        {
                            Thread.sleep(50); //stop for 50 ms and try again
                        }
                        catch(InterruptedException ie){
                            ie.printStackTrace();
                        }
                    }
                }
                // we connected to the ServerSocket on a different peer!
                sender = new Sender(socket, peerID);
                sender.start();
            }
            else
            {
                // ServerSocket on this peer received a connection!
                sender = new Sender(socket, peerID);
                sender.start();
            }
            InputStream inStream = socket.getInputStream();
            byte[] handshake = new byte[32];
            synchronized (downloadLock)
            {
                bytesDownloaded += inStream.read(handshake);
            }
            int peerID = msgHandler.readHandshake(handshake);
            if (connector)
            {
                btLogger.writeToLog(btLogger.TCPConnectTo(peerID));
            }
            else
            {
                synchronized (this)
                {
                    targetPeerID = peerID;
                }
                btLogger.writeToLog(btLogger.TCPConnectFrom(peerID));
            }
            // send out bit field
            outputByteArray(msgHandler.makeMessage(BITFIELD, Arrays.copyOf(threadManager.getBitField(), totalPieces/8 + 1)));
            readyForHave = true;
            // start reading messages
            while(!done)
            {
                // read length of message
                byte[] lengthBytes = new byte[4];
                synchronized (downloadLock) {
                    bytesDownloaded += inStream.read(lengthBytes);
                }
                // use message length to get type and payload
                int bytesToRead = ByteBuffer.wrap(lengthBytes).getInt();
                byte[] contents = new byte[bytesToRead];
                synchronized (downloadLock) {
                    bytesDownloaded += inStream.read(contents);
                }
                MessageType type = MessageType.getMessageTypeFromByte(contents[0]);
                // Choose what to do based on the message payload and type
                if(bytesToRead > 1)
                {
                    byte[] payload = Arrays.copyOfRange(contents, 1, contents.length);
                    actOnReceive(type, payload);
                }
                else
                {
                    actOnReceive(type);
                }
                checkFullFile();
                threadManager.hasFullFile();
                Thread.sleep(5);
                //btLogger.writeToLog(threadManager.getBitSet().toString());
            }
            closeSender();
            socket.close();
            threadManager.streamFinished();
            threadManager.isDone();
        }
        catch (Exception e)
        {
            try {
                BTLogger.getInstance().writeToLog(Arrays.toString(e.getStackTrace()));
            }
            catch (IOException ioe) {
                e.printStackTrace();
            }
        }
        finally {
            try {
                socket.close();
            }
            catch (Exception e)
            {}
        }
    }

    private void actOnReceive(MessageType type, byte[] payload) throws Exception
    {
        switch(type) {
            case HAVE:
                HAVEReceived(payload);
                break;
            case BITFIELD:
                BITFIELDReceived(payload);
                break;
            case REQUEST:
                REQUESTReceived(payload);
                break;
            case PIECE:
                PIECEReceived(payload);
                break;
            default:
                throw new Exception("Incorrect message payload type.");
        }
    }

    private void actOnReceive(MessageType type) throws Exception
    {
        switch(type)
        {
            case CHOKE:
                CHOKEReceived();
                break;
            case UNCHOKE:
                UNCHOKEReceived();
                break;
            case INTERESTED:
                INTERESTEDReceived();
                break;
            case NOTINTERESTED:
                NOTINTERESTEDReceived();
                break;
            default:
                throw new Exception("Incorrect message type");
        }
    }

    // TODO: Alert Thread Manager with message: Thread Manager will make it respond interested or not interested,
    // TODO: and 'interestingPeer' will be updated accordingly
    private void HAVEReceived(byte[] payload) throws Exception
    {
        //get index of piece they have
        int pieceIndex = java.nio.ByteBuffer.wrap(payload).getInt();
        // log the have message
        btLogger.writeToLog(btLogger.receivedHave(targetPeerID, pieceIndex));
        // update bitfield with index
        availPieces.add(pieceIndex);
        pieces.set(pieceIndex);
        // does the peer need anything? - outputs interested or not interested to connected peer
        sendINTERESTEDorNOT();
    }

    // TODO: Alert ThreadManager to update local copy of bit field and prompt it to reply with an outgoing
    // TODO: INTERESTED or NOTINTERESTED message and update 'interestingPeer'
    private void BITFIELDReceived(byte[] payload) throws Exception
    {
        // assign pieces also checks if the remote peer has the full file
        setPiecesWithBitfield(BitSet.valueOf(payload));
        // does the peer need anything? - outputs interested or not interested to connected peer
        sendINTERESTEDorNOT();
    }

    // TODO: Retrieve appropriate piece from the PieceManager and send it through the Sender as a PIECE
    // TODO: message
    private void REQUESTReceived(byte[] payload) throws Exception
    {
        // get piece index from the payload
        int pieceIndex = java.nio.ByteBuffer.wrap(payload).getInt();
        // get piece using index
        byte[] piece = pcManager.readPiece(pieceIndex);
        // package piece index and piece in byte array
        ByteBuffer message = ByteBuffer.allocate(4 + piece.length);
        message.putInt(pieceIndex);
        message.put(piece);
        // send piece message
        outputByteArray(msgHandler.makeMessage(PIECE, message.array())); // request piece
    }

    // TODO: 1. Use Piece Manager to write the payload
    // TODO: 2. Alert Thread Manager with the new piece so it can update own bit field and send out HAVE
    // TODO:    through all PeerStreams
    // TODO: 3. Send out another REQUEST message or a NOTINTERESTED if everything is complete
    private void PIECEReceived(byte[] payload) throws Exception
    {
        // Update outgoing request check so CHOKE doesn't remove piece index from request buffer
        outgoingIndexRequest = -1;
        // parse payload for piece index and piece
        byte[] byteArrayPieceIndex = Arrays.copyOfRange(payload, 0, 4);
        int pieceIndex = java.nio.ByteBuffer.wrap(byteArrayPieceIndex).getInt();
        byte[] piece = Arrays.copyOfRange(payload, 4, payload.length);
        // write piece into file
        pcManager.writePiece(piece, pieceIndex);
        // update bitfield
        threadManager.addPieceIndex(pieceIndex);
        // send out global have message
        threadManager.broadcastHaveMessage(msgHandler.makeMessage(HAVE, byteArrayPieceIndex));
        // log the piece message
        btLogger.writeToLog(btLogger.downloadedPiece(pieceIndex, targetPeerID, threadManager.currentPieces()));
        // see whether peer needs a piece from the bitfield
        makeNextREQUESTOrSendNOTINTERESTED();
        if(threadManager.hasFullFile())
        {
            btLogger.writeToLog(btLogger.downloadedFile());
        }
    }

    // TODO: Handle by telling the sender to not send anything except INTERESTED or NOTINTERESTED
    // TODO: messages until it receives an UNCHOKE message
    private void CHOKEReceived() throws Exception
    {
        // log the choke message
        btLogger.writeToLog(btLogger.choked(targetPeerID));
		// CHOKE received before PIECE; inform ThreadManager of failed outgoing REQUEST
        if (outgoingIndexRequest != -1)
        {
            threadManager.handleIncompleteRequest(outgoingIndexRequest);
            outgoingIndexRequest = -1;
        }
    }

    private void UNCHOKEReceived() throws Exception
    {
        btLogger.writeToLog(btLogger.unchoked(targetPeerID));
        makeNextREQUESTOrSendNOTINTERESTED();
    }

    private void INTERESTEDReceived() throws Exception
    {
        readyToSend = true;
        btLogger.writeToLog(btLogger.receivedInterested(targetPeerID));
        if(!receivedInterested || !hasUpdatedInterested)
        {
            threadManager.updateInterested(this, true);
            hasUpdatedInterested = true;
        }
        receivedInterested = true;
    }

    // TODO: Same as above; locally track previous declaration of 'ifInterested' though. If it is a duplicate
    // TODO: message, there is no need to alert the ThreadManager.
    // TODO: Additionally, if this peer has a full file, it can shut off its connection with the neighbor
    // TODO: permanently. This is how the entire process will close out.
    private void NOTINTERESTEDReceived() throws Exception
    {
        btLogger.writeToLog(btLogger.receivedNotInterested(targetPeerID));
        if(receivedInterested || !hasUpdatedInterested)
        {
            threadManager.updateInterested(this, false);
            hasUpdatedInterested = true;
        }
        receivedInterested = false;
    }

    private void makeNextREQUESTOrSendNOTINTERESTED() throws Exception
    {
        int pieceIndex = threadManager.getRandomAvailablePieceIndex(this);
        if (pieceIndex != -1)
        {
            // send request message
            // get random pieceIndex
            // package piece index in byte array
            ByteBuffer message = ByteBuffer.allocate(4);
            message.putInt(pieceIndex);
            // send request message
            outputByteArray(msgHandler.makeMessage(REQUEST, message.array()));
        }
        else
        {
            // send not interested message
            outputByteArray(msgHandler.makeMessage(NOTINTERESTED));
        }
    }

    public void sendINTERESTEDorNOT() throws Exception
    {
        // update peer with the status of interested
        if (threadManager.areInterested(this))
        {
            outputByteArray(msgHandler.makeMessage(INTERESTED));
        }
        else
        {
            outputByteArray(msgHandler.makeMessage(NOTINTERESTED));
        }
    }

    public void sendTNTERESTED() throws Exception{
        outputByteArray(msgHandler.makeMessage(INTERESTED));
    }

    public void closeSender()
    {
        synchronized(sender.mutex) {
            sender.close();
            sender.mutex.notify();
        }
    }

    private synchronized void chokeRemote()
    {
        try {
            sender.clearMessages();
            outputByteArray(msgHandler.makeMessage(MessageType.CHOKE));
        }
        catch (Exception e)
        {
            try {
                BTLogger.getInstance().writeToLog(Arrays.toString(e.getStackTrace()));
            }
            catch (IOException ioe) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void outputByteArray(byte[] message)
    {
        synchronized(sender.mutex) {
            sender.queueMessage(message);
            sender.mutex.notify();
        }
    }

    public synchronized boolean checkFullFile()
    {
        if (pieces.cardinality() == totalPieces) {
            isFull = true;
        }
        return isFull;
    }

    private synchronized void unchokeRemote()
    {
        try {
            outputByteArray(msgHandler.makeMessage(MessageType.UNCHOKE));
        }
        catch (Exception e)
        {
            try {
                BTLogger.getInstance().writeToLog(Arrays.toString(e.getStackTrace()));
            }
            catch (IOException ioe) {
                e.printStackTrace();
            }
        }
    }

    public void setDone() { this.done = true; }

    public boolean isReadyToSend() { return readyToSend; }

    public boolean isReadyForHave() { return readyForHave; }

    public synchronized int getTargetPeerID() { return targetPeerID; }

    public long getDownloadRate() { return bytesDownloaded; }

    public BitSet getPieces()
    {
        return pieces;
    }

    public void resetDownloadRate() {
        synchronized (downloadLock) {
            bytesDownloaded = 0;
        }
    }

    public ArrayList<Integer> getAvailPieces()
    {
        return availPieces;
    }

    public void setAvailPieces(ArrayList<Integer> array)
    {
        availPieces = array;
    }

    // Helper method for initial bitfield construction
    private void setPiecesWithBitfield(BitSet bitSet)
    {
        pieces = bitSet;
        for (int i = 0; i != bitSet.size(); ++i)
        {
            if (bitSet.get(i))
                availPieces.add(i);
        }
        checkFullFile();
    }

    public boolean isOptimUnchokedNeighbor()
    {
        return isOptimUnchokedNeighbor;
    }

    public synchronized void setOptimUnchokedNeighbor(boolean unchoke)
    {
        if (unchoke)
        {
            if (!isOptimUnchokedNeighbor && !isPreferredNeighbor)
                unchokeRemote();
        }
        else
        {
            if (isOptimUnchokedNeighbor && !isPreferredNeighbor)
                chokeRemote();
        }

        isOptimUnchokedNeighbor = unchoke;
    }

    public boolean isPreferredNeighbor()
    {
        return isPreferredNeighbor;
    }

    public synchronized void setPreferredNeighbor(boolean preferred)
    {
        if (preferred)
        {
            if (!isOptimUnchokedNeighbor && !isPreferredNeighbor)
            {
                unchokeRemote();
            }
        }
        else
        {
            if (!isOptimUnchokedNeighbor && isPreferredNeighbor)
                chokeRemote();
        }
        isPreferredNeighbor = preferred;
    }

}
