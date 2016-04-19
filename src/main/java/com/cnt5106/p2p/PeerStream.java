package com.cnt5106.p2p;

import com.cnt5106.p2p.models.MessageType;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

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
    private String hostname, targetHostName;    // TODO: Local hostname unnecessary?
    private int peerID, targetPeerID;
    private Socket socket;
    private Sender sender;
    private MessageHandler msgHandler;
    private boolean connector;
    private BTLogger btLogger;
    private ThreadManager threadManager;
    private boolean chokeRemote = false;
    private int bytesDownloaded;
    private boolean interestingPeer;

    private ServerSocket listener;

    PeerStream(int port, String hostname, int targetPort, String targetHostName,
               int peerID, int targetPeerID)
    {
        this.connector = true;
        this.port = port;
        this.hostname = hostname;
        this.peerID = peerID;
        this.targetPeerID = targetPeerID;
        this.targetPort = targetPort;
        this.targetHostName = targetHostName;
        this.msgHandler = MessageHandler.getInstance();
        this.btLogger = BTLogger.getInstance();
        this.threadManager = ThreadManager.getInstance();
    }

    PeerStream(int port, int peerID)
    {
        this.connector = false;
        this.peerID = peerID;
        this.port = port;
        this.msgHandler = MessageHandler.getInstance();
        this.btLogger = BTLogger.getInstance();
        this.threadManager = ThreadManager.getInstance();
    }

    public void run()
    {
        try
        {
            if (connector)
            {
                socket = new Socket(InetAddress.getByName(targetHostName), targetPort, InetAddress.getByName(hostname), port);
                btLogger.writeToLog(btLogger.socketStarted(!connector));
                sender = new Sender(socket, peerID);
                sender.start();
            }
            else
            {
                socket = threadManager.waitForSocket();
                btLogger.writeToLog(btLogger.socketStarted(!connector));
                sender = new Sender(socket, peerID);
                sender.start();
            }
            InputStream inStream = socket.getInputStream();
            byte[] bytes = new byte[32];
            inStream.read(bytes);
            int peerID = msgHandler.readHandshake(bytes);
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
            // start reading messages
            byte[] lengthBytes = new byte[4];
            inStream.read(lengthBytes);
            int bytesToRead = java.nio.ByteBuffer.wrap(lengthBytes).getInt();
            MessageType type = MessageType.getMessageTypeFromByte((byte)inStream.read());
            if(bytesToRead > 1)
            {
                byte[] payload = new byte[bytesToRead - 1];
                switch(type)
                {
                    case CHOKE:
                        // TODO: Handle by telling the sender to not send anything except INTERESTED or NOTINTERESTED
                        // TODO: messages until it receives an UNCHOKE message
                        break;
                    case UNCHOKE:
                        // TODO: There are two scenarios: We receive an UNCHOKE when we don't actually want anything from
                        // TODO: this peer, or we still want something.
                        // TODO: Former case:   I can't see this happening unless the NOTINTERESTED message is still in
                        // TODO:                transit. It's probably safest to send another NOTINTERESTED message.
                        // TODO: Latter case:   Send out a REQUEST message for a random piece by alerting ThreadManager,
                        // TODO:                who will synchronously choose a random required piece that matches with
                        // TODO:                this peer's bit field
                        break;
                    case INTERESTED:
                        // TODO: Sort out the peers who are interested and not interested; random selection should be only for those
                        // TODO: who are interested. ThreadManager has to maintain the mutable list. Has to check for interested
                        // TODO: as well in case the peer was not previously interested. Update: see below
                        break;
                    case NOTINTERESTED:
                        // TODO: Same as above; locally track previous declaration of 'ifInterested' though. If it is a duplicate
                        // TODO: message, there is no need to alert the ThreadManager.
                        // TODO: Additionally, if this peer has a full file, it can shut off its connection with the neighbor
                        // TODO: permanently. This is how the entire process will close out.
                        break;
                    case HAVE:
                        // TODO: Alert Thread Manager with message: Thread Manager will make it respond interested or not interested,
                        // TODO: and 'interestingPeer' will be updated accordingly
                        break;
                    case BITFIELD:
                        // TODO: Alert ThreadManager to update local copy of bit field and prompt it to reply with an outgoing
                        // TODO: INTERESTED or NOTINTERESTED message and update 'interestingPeer'
                        break;
                    case REQUEST:
                        // TODO: Retrieve appropriate piece from the PieceManager and send it through the Sender as a PIECE
                        // TODO: message
                        break;
                    case PIECE:
                        // TODO: 1. Use Piece Manager to write the payload
                        // TODO: 2. Alert Thread Manager with the new piece so it can update own bit field and send out HAVE
                        // TODO:    through all PeerStreams
                        // TODO: 3. Send out another REQUEST message or a NOTINTERESTED if everything is complete
                        break;
                    default:
                        // error
                        break;
                }
            }
            else
            {
                switch(type)
                {
                    case CHOKE:
                    case UNCHOKE:
                    case INTERESTED:
                    case NOTINTERESTED:
                        // notify something
                    default:
                        // error
                }
            }
            sender.setRunning(false);
            sender.notify();
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

    public synchronized void chokeRemote()
    {
        chokeRemote = true;
        try {
            sender.clearMessages();
            sender.queueMessage(msgHandler.makeMessage(MessageType.CHOKE));
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



    public void outputByteArray(byte[] message)
    {
        sender.queueMessage(message);
    }

    public synchronized void unchokeRemote()
    {
        chokeRemote = false;
        try {
            sender.queueMessage(msgHandler.makeMessage(MessageType.UNCHOKE));
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

    public synchronized int getTargetPeerID()
    {
        return targetPeerID;
    }

    public int getDownloadRate()
    {
        return bytesDownloaded;
    }
}
