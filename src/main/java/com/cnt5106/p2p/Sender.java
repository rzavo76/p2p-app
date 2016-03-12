package com.cnt5106.p2p;

// Sender class created by Ryan Zavoral Feb. 16, 2016.

import java.text.*;
import java.util.*;
import java.io.*;
import java.lang.*;

public class Sender extends NetworkThread {
	private int targetPort;
	private String targetHostname;
	Sender(int port, String hostname, int targetPort, String targetHostname) {
		super(port, hostname);
		this.targetPort = targetPort;
		this.targetHostname = targetHostname;
	}
	public int getTargetPort() {
		return targetPort;
	}
	public String getTargetHostname() {
		return targetHostname;
	}
	public void setTargetPort(int targetPort) {
		this.targetPort = targetPort;
	}
	public void setTargetHostname(String targetHostname) {
		this.targetHostname = targetHostname;
	}
	public String toString() {
		return "The port is " + port + " and the hostname is " + hostname + "." + 
		"The target port is " + targetPort + " and the target hostname is " + targetHostname + "."; 
	}
	public void run() {}
}