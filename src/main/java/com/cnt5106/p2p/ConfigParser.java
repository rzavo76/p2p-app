package com.cnt5106.p2p;
/*
 * ConfigParser class created by Dylan Richardson on Feb. 9, 2016
 */
import java.util.*;
import java.io.*;
import java.lang.*;

public class ConfigParser
{
	private static int numPreferredNeighbors;
	private static int unchokeInterval;
	private static int optUnchokeInterval;
	private static String fileName;
	private static int fileSize;
	private static int pieceSize;

	/*
	 * fPath: The path name of the file (will usually be Common.cfg)
	 *
	 * Description: Populates static global program variables to be
	 * shared by all peers, but only set by the common configuration file.
	 */
	public static void parseConfigFile(String fPath) throws Exception
	{
		try
		{
			Scanner in = new Scanner(new File(fPath));
			while (in.hasNextLine())
			{
				String nextToken = in.next(); // Read in key first
				System.out.println(nextToken);

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

	public static int getNumPreferredNeighbors()
	{
		return numPreferredNeighbors;
	}

	public static int getUnchokeInterval()
	{
		return unchokeInterval;
	}

	public static int getOptUnchokeInterval()
	{
		return optUnchokeInterval;
	}

	public static String getFileName()
	{
		return fileName;
	}

	public static int getFileSize()
	{
		return fileSize;
	}

	public static int getPieceSize()
	{
		return pieceSize;
	}
}