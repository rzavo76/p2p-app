package com.cnt5106.p2p;

import com.cnt5106.p2p.models.MessageType;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;

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
    private boolean chokeRemote = false;
    private int bytesDownloaded;

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
    }

    PeerStream(int port, int peerID)
    {
        this.peerID = peerID;
        this.port = port;
        this.connector = false;
        this.msgHandler = MessageHandler.getInstance();
        this.btLogger = BTLogger.getInstance();
    }

    public void run()
    {
        try
        {
            if (connector)
            {
                socket = new Socket(targetHostName, targetPort, null, port);
                sender = new Sender(socket, peerID);
                sender.start();
            }
            else
            {
                ServerSocket listener = new ServerSocket(port);
                socket = listener.accept();
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

            while (!Thread.interrupted())
            {
                synchronized (this)
                {

                }
            }
            socket.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
