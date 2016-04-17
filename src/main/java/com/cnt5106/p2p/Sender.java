package com.cnt5106.p2p;

// Receiver class created by Ryan Zavoral Feb. 16, 2016.
// N Receiving threads are actively run by the thread manager to wait on a socket

import java.io.IOException;
import java.lang.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Sender extends Thread {

	private Socket socket;
	private MessageHandler msgHandler;
	private int peerID;
	private BlockingQueue<byte[]> outgoing;

	Sender(Socket socket, int peerID)
	{
		this.socket = socket;
		this.peerID = peerID;
		msgHandler = MessageHandler.getInstance();
		outgoing = new LinkedBlockingQueue<>();
	}

	public void run()
	{
		try {
			byte[] handshake = msgHandler.makeHandshake(peerID);
			socket.getOutputStream().write(handshake);
			byte[] next = outgoing.remove();
			socket.getOutputStream().write(next);
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


	// Blocking Queue is thread safe
	public void queueMessage(byte[] msg)
	{
		outgoing.add(msg);
	}

	public void clearMessages()
	{
		outgoing.clear();
	}
}