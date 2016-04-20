package com.cnt5106.p2p;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;

/**
 * Created by ryan on 4/19/16.
 * Runs the receiving PeerStreams in a thread to allow the ThreadManager to continue execution
 */
public class SocketListener extends Thread {
    private ServerSocket listener;
    int thisIndex;
    PeerStream[] streams;
    SocketListener(int port, int thisIndex, PeerStream[] streams) throws Exception
    {
        listener = new ServerSocket(port);
        this.thisIndex = thisIndex;
        this.streams = streams;
    }
    public void run()
    {
        try {
            int numPeers = streams.length;
            for (int i = thisIndex; i < numPeers; ++i) {
                streams[i].socket = listener.accept();
                streams[i].start();
            }
            listener.close();
        } catch(Exception e) {
            try {
                BTLogger.getInstance().writeToLog(Arrays.toString(e.getStackTrace()));
            }
            catch (IOException ioe) {
                e.printStackTrace();
            }
        }
        finally {
            try {
                listener.close();
            } catch(Exception e)
            {}
        }
    }
}
