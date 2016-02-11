package com.cnt5106.p2p;

// BTLogger class created by Ryan Zavoral Feb. 5, 2016.

import java.text.*;
import java.util.*;
import java.io.*;
import java.lang.*;

public class BTLogger { 
// simpleton logging class that a peer shares among its threads
// writes to a file with the desired message
    private static SimpleDateFormat sdf;
    private static BTLogger instance = null;
    private BTLogger() {}
    public static synchronized BTLogger getInstance() {
    	if(instance == null) {
    		instance = new BTLogger();
    		sdf = new SimpleDateFormat("HH:mm:ss"); // format hour, minute, second
    	}
    	return instance;
    }
    public synchronized void writeToLog(int pid, String logString) {
    	try {
    	String filename = "log_peer_" + Integer.toString(pid) + ".log";
    	File log = new File(filename);
    	if(!log.exists()) { // make log file if it doesn't exist
    		log.createNewFile();
    	}
	    	FileOutputStream toLog = new FileOutputStream(log, true); // append to end of file
	    	toLog.write(logString.getBytes());
	    	toLog.flush();
	    	toLog.close();
    	} catch(IOException ie) {
    		System.err.println("Failed to write to log file.");
    	}
    }
	public String TCPConnectTo(int p1id, int p2id) {
		String logString = String.format("%s: Peer %d makes a connection to Peer %d.\n", 
			sdf.format(Calendar.getInstance().getTime()), p1id, p2id);
		return logString;
	}
	public String TCPConnectFrom(int p1id, int p2id) {
		String logString = String.format("%s: Peer %d is connected from Peer %d.\n", 
			sdf.format(Calendar.getInstance().getTime()), p1id, p2id);
		return logString;
	}
	public String changeOfPrefNeighbors(int pid, int[] nid) {
		StringBuilder neighborList = new StringBuilder();
		String delim = "";
		for(int s : nid) {
			neighborList = neighborList.append(delim).append(Integer.toString(s));
			delim = ", ";
		}
		String logString = String.format("%s: Peer %d has the preferred neighbors %s.\n", 
			sdf.format(Calendar.getInstance().getTime()), pid, neighborList);
		return logString;
	}
	public String changeOfOUNeighbor(int pid, int nid) {
		String logString = String.format("%s: Peer %d has the optimistically unchoked neighbor " 
			+ "%d.\n", sdf.format(Calendar.getInstance().getTime()), pid, nid);
		return logString;
	}
	public String unchoked(int p1id, int p2id) {
		String logString = String.format("%s: Peer %d is unchoked by %d.\n", 
			sdf.format(Calendar.getInstance().getTime()), p1id, p2id);
		return logString;
	}
	public String choked(int p1id, int p2id) {
		String logString = String.format("%s: Peer %d is choked by %d.\n", 
			sdf.format(Calendar.getInstance().getTime()), p1id, p2id);
		return logString;
	}
	public String receivedHave(int p1id, int p2id, int piece) {
		String logString = String.format("%s: Peer %d received the 'have' message from %d for the " 
			+ "piece %d.\n", sdf.format(Calendar.getInstance().getTime()), p1id, p2id, piece);
		return logString;
	}
	public String receivedInterested(int p1id, int p2id) {
		String logString = String.format("%s: Peer %d received the 'interested' message from " 
			+ "%d.\n", sdf.format(Calendar.getInstance().getTime()), p1id, p2id);
		return logString;
	}
	public String receivedNotInterested(int p1id, int p2id) {
		String logString = String.format("%s: Peer %d received the 'not interested' message from "
			+ "%d.\n", sdf.format(Calendar.getInstance().getTime()), p1id, p2id);
		return logString;
	}
	public String downloadedPiece(int p1id, int pieceIndex, 
		int p2id, int numberPieces) {
		String logString = String.format("%s: Peer %d has downloaded the piece %d from %d. Now "  
			+ "the number of pieces it has is %d.\n", sdf.format(Calendar.getInstance().getTime()), 
			p1id, pieceIndex, p2id, numberPieces);
		return logString;
	}
	public String downloadedFile(int pid) {
		String logString = String.format("%s: Peer %d has downloaded the complete file.\n", 
			sdf.format(Calendar.getInstance().getTime()), pid);
		return logString;
	}
}