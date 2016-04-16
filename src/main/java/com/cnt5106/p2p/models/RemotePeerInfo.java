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

	public void addAvailablePiece(int pIndex)
	{
		availPiecesArray.add(pIndex);
	}

	public int getAvailablePiece(boolean[] ownedPieces, HashSet<Integer> requestBuffer)
	{
		while (!availPiecesArray.isEmpty())
		{
			int arrayIndex = randomizer.nextInt(availPiecesArray.size());
			int pieceIndex = availPiecesArray.get(arrayIndex);
			// Don't return pieces for outgoing requests, but also don't delete them from
			// available in case they don't go through
			if (requestBuffer.contains(pieceIndex))
				continue;
			// If pieces are already owned by our process, we never will want them from a
			// peer again. Even though they are still available, we are removing them from
			// the peer info class corresponding to that peer.
			if (ownedPieces[pieceIndex])
			{
				// The swap is to ensure an O(1) random lookup. There will never
				// be any need to get specific index, because it's always randomized. Therefore,
				// switching order of available piece indices presents no problem
				Integer lastPiece = availPiecesArray.get(availPiecesArray.size() - 1);
				availPiecesArray.set(arrayIndex, lastPiece);
				availPiecesArray.remove(availPiecesArray.size() - 1);
			}
			else
			{
				return pieceIndex;
			}
		}
		// There are no more pieces that we want from this file right now.
		return -1;
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
