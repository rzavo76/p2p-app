/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

package com.cnt5106.p2p.models;

import com.cnt5106.p2p.FileParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

public class RemotePeerInfo {
	public int peerId;
	public String peerAddress;
	public int peerPort;
	public boolean hasFile;
	private ArrayList<Integer> availPiecesArray;
	private Random randomizer;
	
	public RemotePeerInfo(int pid, String pAddress, int pPort, boolean hasFile)
	{
		peerId = pid;
		peerAddress = pAddress;
		peerPort = pPort;
		this.hasFile = hasFile;
		if (hasFile)
		{
			generateArray();
		}
		else
		{
			availPiecesArray = new ArrayList<>();
		}
		randomizer = new Random();
	}

	/**
	 * Private helper method to generate available piece list with all indices if this process has the file
	 * initially
	 */
	private void generateArray()
	{
		int pieces = FileParser.getInstance().getNumPieces();
		availPiecesArray = new ArrayList<>(pieces);
		for (int i = 0; i != pieces; ++i)
		{
			availPiecesArray.add(i);
		}
	}
}
