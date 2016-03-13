package com.cnt5106.p2p;

// NetworkThread class created by Ryan Zavoral Feb. 16, 2016.

import java.text.*;
import java.util.*;
import java.io.*;
import java.lang.*;

public abstract class NetworkThread extends Thread {
	protected int port;
	protected String hostname;
	protected int peerID;
	protected int targetPeerID;
	NetworkThread(int port, String hostname, int peerID, int targetPeerID) {
		this.port = port;
		this.hostname = hostname;
		this.peerID = peerID;
		this.targetPeerID = targetPeerID;
	}
	public int getPort() {
		return port;
	}
	public String getHostname() {
		return hostname;
	}
	public void setPort(int port) {
		this.port = port; 
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public abstract String toString();
	public abstract void run();
}