package com.cnt5106.p2p;

// Receiver class created by Ryan Zavoral Feb. 16, 2016.

import java.text.*;
import java.util.*;
import java.io.*;
import java.lang.*;

public class Receiver extends NetworkThread {
	Receiver(int port, String hostname) {
		super(port, hostname);
	}
	public String toString() {
		return "The port is " + port + " and the hostname is " + hostname;
	}
	public void run() {}
}