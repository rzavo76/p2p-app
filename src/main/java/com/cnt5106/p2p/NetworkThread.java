package com.cnt5106.p2p;

// NetworkThread class created by Ryan Zavoral Feb. 16, 2016.

import java.text.*;
import java.util.*;
import java.io.*;
import java.lang.*;

public abstract class NetworkThread extends Thread {
	private int port;
	private String hostname;
	NetworkThread(int port, String hostname) {
		this.port = port;
		this.hostname = hostname;
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
	public abstract void run();
}