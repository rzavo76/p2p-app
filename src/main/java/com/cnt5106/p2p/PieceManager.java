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

    private PieceManager() {}
    public static synchronized PieceManager getInstance() throws Exception
    {
        if(numberOfPieces == 0) // set pieces before you can getInstance
        {
            throw new Exception("Cannot get PieceManager until pieces are set.");
        }
        if(instance == null)
        {
            instance = new PieceManager();
        }
        return instance;
    }
    public static void setNumberOfPieces(int p)
    {
        // pieces can only be set once
        numberOfPieces = numberOfPieces == 0 ? p : numberOfPieces;
    }
    public static int getNumberOfPieces()
    {
        return numberOfPieces;
    }
}
