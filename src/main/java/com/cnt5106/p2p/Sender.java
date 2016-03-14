package com.cnt5106.p2p;

// Sender class created by Ryan Zavoral Feb. 16, 2016.
// K + 1 sending threads are run by the thread manager for a socket

import java.text.*;
import java.util.*;
import java.io.*;
import java.lang.*;

public class Sender extends NetworkThread {
	private int targetPort;
	private String targetHostname;
	Sender(int port, String hostname, int targetPort, String targetHostname, 
		int peerID, int targetPeerID) 
	{
		super(port, hostname, peerID, targetPeerID);
		this.targetPort = targetPort;
		this.targetHostname = targetHostname;
	}
	public int getTargetPort() 
	{
		return targetPort;
	}
	public String getTargetHostname() 
	{
		return targetHostname;
	}
	public void setTargetPort(int targetPort) 
	{
		this.targetPort = targetPort;
	}
	public void setTargetHostname(String targetHostname) 
	{
		this.targetHostname = targetHostname;
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