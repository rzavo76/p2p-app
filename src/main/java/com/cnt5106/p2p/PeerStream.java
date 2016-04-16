package com.cnt5106.p2p;

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

    private int port;
    private String hostname;    // TODO: Unnecessary?
    private int peerID;
    private int targetPeerID;
    private Socket socket;
    private Sender sender;
    private MessageHandler msgHandler;
    private boolean connector;
    private BTLogger btLogger;

    PeerStream(int port, String hostname, int targetPort, String targetHostName,
               int peerID, int targetPeerID)
    {
        this.connector = true;
        this.port = port;
        this.hostname = hostname;
        this.peerID = peerID;
        this.targetPeerID = targetPeerID;
        msgHandler = MessageHandler.getInstance();
        btLogger = BTLogger.getInstance();
        try {
            socket = new Socket(targetHostName, targetPort, null, port);
            sender = new Sender(socket, peerID);
            sender.start();
            start();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    PeerStream(int port, int peerID)
    {
        this.peerID = peerID;
        this.port = port;
        this.connector = false;
        this.msgHandler = MessageHandler.getInstance();
        this.btLogger = BTLogger.getInstance();
        try
        {
            ServerSocket listener = new ServerSocket(port);
            socket = listener.accept();
            sender = new Sender(socket, peerID);
            sender.start();
            start();
        }
        catch(IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    public void run()
    {
        try
        {
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
            socket.close();
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
}
