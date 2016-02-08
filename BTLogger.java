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
    private synchronized void writeToFile(String filename, String logString) {
    	try {
    	File log = new File(filename);
    	if(!log.exists()) { //make log file if it doesn't exist
    		log.createNewFile();
    	}
	    	FileOutputStream toLog = new FileOutputStream(log, true); //append to end of file
	    	toLog.write(logString.getBytes());
	    	toLog.flush();
	    	toLog.close();
    	} catch(IOException ie) {
    		System.err.println("Failed to write to log file.");
    	}
    }
    public static synchronized BTLogger getInstance() {
    	if(instance == null) {
    		instance = new BTLogger();
    		sdf = new SimpleDateFormat("HH:mm:ss"); //format hour, minute, second
    	}
    	return instance;
    }
	public void TCPConnectTo(String p1id, String p2id) {
		String logString = String.format("%s: Peer %s makes a connection to Peer %s.\n", 
			sdf.format(Calendar.getInstance().getTime()), p1id, p2id);
		writeToFile("log_peer_" + p1id + ".log", logString);
	}
	public void TCPConnectFrom(String p1id, String p2id) {
		String logString = String.format("%s: Peer %s is connected from Peer %s.\n", 
			sdf.format(Calendar.getInstance().getTime()), p1id, p2id);
		writeToFile("log_peer_" + p1id + ".log", logString);
	}
	public void changeOfPrefNeighbors(String pid, String[] nid) {
		StringBuilder neighborList = new StringBuilder();
		String delim = "";
		for(String s : nid) {
			neighborList = neighborList.append(delim + s);
			delim = ", ";
		}
		String logString = String.format("%s: Peer %s has the preferred neighbots %s.\n", 
			sdf.format(Calendar.getInstance().getTime()), pid, neighborList);
		writeToFile("log_peer_" + pid + ".log", logString);
	}
	public void changeOfOUNeighbor(String pid, String nid) {
		String logString = String.format("%s: Peer %s has the optimistically unchoked neighbor " 
			+ "%s.\n", 
			sdf.format(Calendar.getInstance().getTime()), pid, nid);
		writeToFile("log_peer_" + pid + ".log", logString);
	}
	public void unchoked(String p1id, String p2id) {
		String logString = String.format("%s: Peer %s is unchoked by %s.\n", 
			sdf.format(Calendar.getInstance().getTime()), p1id, p2id);
		writeToFile("log_peer_" + p1id + ".log", logString);
	}
	public void choked(String p1id, String p2id) {
		String logString = String.format("%s: Peer %s is choked by %s.\n", 
			sdf.format(Calendar.getInstance().getTime()), p1id, p2id);
		writeToFile("log_peer_" + p1id + ".log", logString);
	}
	public void receivedHave(String p1id, String p2id, String piece) {
		String logString = String.format("%s: Peer %s received the 'have' message from %s for the " 
			+ "piece %s.\n", sdf.format(Calendar.getInstance().getTime()), p1id, p2id, piece);
		writeToFile("log_peer_" + p1id + ".log", logString);
	}
	public void receivedInterested(String p1id, String p2id) {
		String logString = String.format("%s: Peer %s received the 'interested' message from " 
			+ "%s.\n", sdf.format(Calendar.getInstance().getTime()), p1id, p2id);
		writeToFile("log_peer_" + p1id + ".log", logString);
	}
	public void receivedNotInterested(String p1id, String p2id) {
		String logString = String.format("%s: Peer %s received the 'not interested' message from "
			+ "%s.\n", sdf.format(Calendar.getInstance().getTime()), p1id, p2id);
		writeToFile("log_peer_" + p1id + ".log", logString);
	}
	public void downloadedPiece(String p1id, String pieceIndex, 
		String p2id, String numberPieces) {
		String logString = String.format("%s: Peer %s has downloaded the piece %s from %s. Now "  
			+ "the number of pieces it has is %s\n", sdf.format(Calendar.getInstance().getTime()), 
			p1id, pieceIndex, p2id, numberPieces);
		writeToFile("log_peer_" + p1id + ".log", logString);
	}
	public void downloadedFile(String pid) {
		String logString = String.format("%s: Peer %s has has downloaded the complete file.\n", 
			sdf.format(Calendar.getInstance().getTime()), pid);
		writeToFile("log_peer_" + pid + ".log", logString);
	}
}