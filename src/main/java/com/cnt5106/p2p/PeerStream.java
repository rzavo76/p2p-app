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
                btLogger.writeToLog(this.peerID, btLogger.socketStarted(this.peerID, !connector));
                sender = new Sender(socket, peerID);
                sender.start();
            }
            else
            {
                socket = threadManager.waitForSocket();
                btLogger.writeToLog(this.peerID, btLogger.socketStarted(this.peerID, !connector));
                sender = new Sender(socket, peerID);
                sender.start();
            }
            InputStream inStream = socket.getInputStream();
            byte[] bytes = new byte[32];
            inStream.read(bytes);
            int peerID = msgHandler.readHandshake(bytes);
            if (connector)
            {
                btLogger.writeToLog(this.peerID, btLogger.TCPConnectTo(this.peerID, peerID));
            }
            else
            {
                synchronized (this)
                {
                    targetPeerID = peerID;
                }
                btLogger.writeToLog(this.peerID, btLogger.TCPConnectFrom(this.peerID, peerID));
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
                    case HAVE:
                    case BITFIELD:
                    case REQUEST:
                    case PIECE:
                        // move payload etc
                        //notify something
                    default:
                        // error
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
                BTLogger.getInstance().writeToLog(peerID, Arrays.toString(e.getStackTrace()));
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
                BTLogger.getInstance().writeToLog(peerID, Arrays.toString(e.getStackTrace()));
            }
            catch (IOException ioe) {
                e.printStackTrace();
            }
        }
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
                BTLogger.getInstance().writeToLog(peerID, Arrays.toString(e.getStackTrace()));
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
