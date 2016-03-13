package com.cnt5106.p2p;
/*
 * ConfigParser class created by Dylan Richardson on Feb. 9, 2016
 */
import com.cnt5106.p2p.models.RemotePeerInfo;
import com.cnt5106.p2p.models.peerProcess;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.*;
import java.io.*;
import java.lang.*;

public class FileParser
{
	private int numPreferredNeighbors;
	private int unchokeInterval;
	private int optUnchokeInterval;
	private String fileName;
	private int fileSize;
	private int pieceSize;

	private static FileParser mParser;

	/*
	 * Private constructor b/c of singleton; do nothing for now
	 */
	private FileParser()
	{}

	/*
	 * Method for retrieving instance of the singleton, hence the
	 * private constructor.
	 */
	public static FileParser getInstance()
	{
		if (mParser == null)
		{
			mParser = new FileParser();
		}
		return mParser;
	}

	/*
	 * fPath: The path name of the file (will usually be Common.cfg)
	 *
	 * Description: Populates static global program variables to be
	 * shared by all peers, but only set by the common configuration file.
	 */
	public void parseConfigFile(Path fPath) throws Exception
	{
		try
		{
			Scanner in = new Scanner(fPath);
			while (in.hasNextLine())
			{
				String nextToken = in.next(); // Read in key first

				/* Switch statements on strings are supported in Java 7+.
				 * If this is unacceptable, let me know and I will change
				 * it to use enums or if/else (both of which seem nasty)
				 */
				switch (nextToken)
				{
					case "NumberOfPreferredNeighbors":
						numPreferredNeighbors = in.nextInt();
						break;
					case "UnchokingInterval":
						unchokeInterval = in.nextInt();
						break;
					case "OptimisticUnchokingInterval":
						optUnchokeInterval = in.nextInt();
						break;
					case "FileName":
						fileName = in.next();
						break;
					case "FileSize":
						fileSize = in.nextInt();
						break;
					case "PieceSize":
						pieceSize = in.nextInt();
						break;
					default:
						throw new IOException("Unrecognized token in configuration file");
				}
			}
		}
		catch (Exception e)
		{
			throw e;
		}
	}

	public ArrayList<RemotePeerInfo> getPeersFromFile(Path fPath) throws Exception
	{
		ArrayList<RemotePeerInfo> peers = new ArrayList<>();
		try
		{
			Scanner in = new Scanner(fPath);
			while (in.hasNextLine())
			{
				/*
				 * We are assuming the file is configured properly with all
				 * of the appropriate whitespace and peers. If it is not, the
				 * exception will be re-thrown
				 */
				int peerID = in.nextInt();
				String hostName = in.next();
				int listenPort = in.nextInt();
				Boolean hasFile = in.nextInt() != 0;

				RemotePeerInfo rpi = new RemotePeerInfo(peerID, hostName, listenPort, hasFile);
				peers.add(rpi);
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		return peers;
	}

	public int getNumPreferredNeighbors()
	{
		return numPreferredNeighbors;
	}

	public int getUnchokeInterval()
	{
		return unchokeInterval;
	}

	public int getOptUnchokeInterval()
	{
		return optUnchokeInterval;
	}

	public String getFileName()
	{
		return fileName;
	}

	public int getFileSize()
	{
		return fileSize;
	}

	public int getPieceSize()
	{
		return pieceSize;
	}
}