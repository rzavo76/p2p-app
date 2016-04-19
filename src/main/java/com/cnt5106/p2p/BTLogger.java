package com.cnt5106.p2p;

// BTLogger class created by Ryan Zavoral Feb. 5, 2016.
// singleton logging class that a peer shares among its threads
// writes to a log file with the desired message

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.*;
import java.util.*;
import java.io.*;
import java.lang.*;

public class BTLogger 
{ 
    private static SimpleDateFormat sdf;
    private static BTLogger instance = null;

	private int pid;

    private BTLogger() {}
    public static synchronized BTLogger getInstance() 
    {
    	if(instance == null)
		{
    		instance = new BTLogger();
    		sdf = new SimpleDateFormat("HH:mm:ss"); 
    		// format time
    	}
    	return instance;
    }

	public void setPid(int pid)
	{
		this.pid = pid;
	}

    public synchronized void  writeToLog(String logString) throws IOException
    {
		System.out.println("HEY: " + logString);
    	//synchronized file write function
		Path log = FileSystems.getDefault().getPath("log_peer_" + pid + ".log");
		if (!Files.exists(log))
		{
			Files.createFile(log);
		}
    	FileOutputStream toLog = new FileOutputStream(log.toString(), true); 
    	// append to end of file
    	toLog.write(logString.getBytes());
    	toLog.flush();
    	toLog.close();
    }
	public String socketStarted(boolean isServer)
	{
		return String.format("%s: Peer %d makes a server? %b socket",
				sdf.format(Calendar.getInstance().getTime()), pid, isServer);
	}
	public String TCPConnectTo(int remote_pid)
	{
		return String.format("%s: Peer %d makes a connection to Peer %d.\n",
			sdf.format(Calendar.getInstance().getTime()), pid, remote_pid);
	}
	public String TCPConnectFrom(int remote_pid)
	{
		return String.format("%s: Peer %d is connected from Peer %d.\n",
			sdf.format(Calendar.getInstance().getTime()), pid, remote_pid);
	}
	public String changeOfPrefNeighbors(int[] nid)
	{
		StringBuilder neighborList = new StringBuilder();
		String delim = "";
		for(int s : nid) {
			neighborList = neighborList.append(delim).append(Integer.toString(s));
			delim = ", ";
		}
		// parse neighborlist into a comma separated string format
		return String.format("%s: Peer %d has the preferred neighbors %s.\n",
			sdf.format(Calendar.getInstance().getTime()), pid, neighborList);
	}
	public String changeOfOUNeighbor(int nid)
	{
		return String.format("%s: Peer %d has the optimistically unchoked neighbor "
			+ "%d.\n", sdf.format(Calendar.getInstance().getTime()), pid, nid);
	}
	public String unchoked(int remote_pid)
	{
		return String.format("%s: Peer %d is unchoked by %d.\n",
			sdf.format(Calendar.getInstance().getTime()), pid, remote_pid);
	}
	public String choked(int remote_pid)
	{
		return String.format("%s: Peer %d is choked by %d.\n",
			sdf.format(Calendar.getInstance().getTime()), pid, remote_pid);
	}
	public String receivedHave(int remote_pid, int piece)
	{
		return String.format("%s: Peer %d received the 'have' message from %d for the "
			+ "piece %d.\n", sdf.format(Calendar.getInstance().getTime()), pid, remote_pid, piece);
	}
	public String receivedInterested(int remote_pid)
	{
		return String.format("%s: Peer %d received the 'interested' message from "
			+ "%d.\n", sdf.format(Calendar.getInstance().getTime()), pid, remote_pid);
	}
	public String receivedNotInterested(int remote_pid)
	{
		return String.format("%s: Peer %d received the 'not interested' message from "
			+ "%d.\n", sdf.format(Calendar.getInstance().getTime()), pid, remote_pid);
	}
	public String downloadedPiece(int pieceIndex,
		int remote_pid, int numberPieces)
	{
		return String.format("%s: Peer %d has downloaded the piece %d from %d. Now "
			+ "the number of pieces it has is %d.\n", sdf.format(Calendar.getInstance().getTime()), 
			pid, pieceIndex, remote_pid, numberPieces);
	}
	public String downloadedFile()
	{
		return String.format("%s: Peer %d has downloaded the complete file.\n",
			sdf.format(Calendar.getInstance().getTime()), pid);
	}
}