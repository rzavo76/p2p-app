package com.cnt5106.p2p;

// Receiver class created by Ryan Zavoral Feb. 16, 2016.
// N Receiving threads are actively run by the thread manager to wait on a socket

import java.lang.*;

public class Receiver extends NetworkThread {
	Receiver(int port, String hostname, int peerID, int targetPeerID) 
	{
		super(port, hostname, peerID, targetPeerID);
	}
	public String toString() 
	{
		return "The receiver thread " + peerID + 
		" for peer " + targetPeerID + 
		"has port is " + port + 
		" and the hostname is " + hostname;
	}
	public void run() 
	{
		System.out.println(toString());
	}
}