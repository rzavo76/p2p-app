package com.cnt5106.p2p;

/**
 * Created by Ryan Zavoral on 3/22/16.
 * PieceManager class for extracting pieces from a file,
 * writing the pieces into the peer directory, and merging
 * pieces once they are all present.
 */

public class PieceManager {
    private static PieceManager instance = null;
    private static int numberOfPieces = 0;
    private static int pID = 0;

    private PieceManager() {}
    public static synchronized PieceManager getInstance() throws Exception
    {
        if(numberOfPieces == 0 || pID == 0) // set pieces before you can getInstance
        {
            throw new Exception("Pieces and peer ID are not set before getInstance.");
        }
        if(instance == null)
        {
            instance = new PieceManager();
        }
        return instance;
    }
    public static void setInstance(int pieces, int peerID)
    {
        // instance can only be set while instance is null
        if(instance == null)
        {
            pID = peerID;
            numberOfPieces = pieces;
        }
    }
    public static int getNumberOfPieces()
    {
        return numberOfPieces;
    }

    public static int getpID() {
        return pID;
    }

    public void splitFile() {}
    public synchronized void writePiece(byte[] piece) {}
    public void mergePieces() {}
}
