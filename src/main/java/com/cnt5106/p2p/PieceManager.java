package com.cnt5106.p2p;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by Ryan Zavoral on 3/22/16.
 * PieceManager class for extracting pieces from a file,
 * writing the pieces into the peer directory, and merging
 * pieces once they are all present.
 */

public class PieceManager {
    private static PieceManager instance = null;
    private static boolean set = false;
    private int numberOfPieces;
    private int fileSize;
    private int pieceSize;
    private int pID;
    private String filename;


    private PieceManager(int numberOfPieces, int fileSize, int pieceSize, int pID, String filename) {
        this.numberOfPieces = numberOfPieces;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
        this.pID = pID;
        this.filename = filename;
    }

    public static synchronized PieceManager getInstance() throws Exception
    {
        if(!set) // instance can only be retrieved after it is set
        {
            throw new Exception("Instance is not set before retrieving it.");
        }
        return instance;
    }

    public static synchronized void setInstance(int numberOfPieces, int fileSize, int pieceSize, int pID, String filename)
    {
        // instance can only be set once
        if(!set)
        {
            instance = new PieceManager(numberOfPieces, fileSize, pieceSize, pID, filename);
            set = true;
        }
    }

    public int getNumberOfPieces() { return numberOfPieces; }

    public int getFileSize() { return fileSize; }

    public int getPieceSize() { return pieceSize; }

    public int getpID() { return pID; }

    public String getFilename() { return filename; }

    public static boolean isSet() { return set; }

    public void splitFile() throws Exception
    {
        // read bytes of the file into an array
        Path fPath = FileSystems.getDefault().getPath(filename);
        byte[] allPieces = Files.readAllBytes(fPath);
        // change path to the directory
        fPath = FileSystems.getDefault().getPath("peer_" + pID);
        // split filename into pieces inside the directory
        for(int pieceIndex = 0; pieceIndex < numberOfPieces; ++pieceIndex) //iterate through files
        {
            int start = pieceIndex*pieceSize;
            start = start == 0 ? start : start - 1;
            // assign correct last byte index if at the last piece
            int end;
            if (pieceIndex == numberOfPieces - 1)
            {
                end = fileSize % pieceSize + start + 1;
            }else
            {
                end = ((pieceIndex + 1)*pieceSize - 1);
            }
            byte[] segment = Arrays.copyOfRange(allPieces, start, end);
            String s = new String(segment, Charset.defaultCharset());
            // write piece
            Files.write(fPath.resolve("/" + pieceIndex + "_" + filename),
                    segment);
        }
    }

    public void makeFolder() throws Exception
    {
        // make directory peer_pID
        Path fPath = FileSystems.getDefault().getPath("peer_" + pID);
        Files.createDirectories(fPath);
    }

    public synchronized void writePiece(byte[] piece, int pieceIndex) throws Exception
    {
        // write byte array piece into peer_pID/filename_pieceIndex.dat
        Path fPath = FileSystems.getDefault().getPath("peer_" + pID + "/" + pieceIndex + "_" + filename);
        Files.write(fPath, piece);
    }
    public byte[] readPiece(int pieceIndex) throws Exception
    {
        // read piece peer_pID/filename_pieceIndex.dat into a byte array
        Path fPath = FileSystems.getDefault().getPath("peer_" + pID + "/" + pieceIndex + "_" + filename);
        return Files.readAllBytes(fPath);
    }
    public synchronized void mergePieces() throws Exception
    {
        Path fPathSource = FileSystems.getDefault().getPath("peer_" + pID);
        //create buffered file write location
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename, true));
        for(int pieceIndex = 0; pieceIndex < numberOfPieces; ++pieceIndex) //iterate through files
        {
            Path currSource = fPathSource.resolve(pieceIndex + "_" + filename);
            // append each piece to the end of the file
            out.write(Files.readAllBytes(currSource));
            // delete piece
            Files.delete(currSource);
        }
        out.close();
    }
}
