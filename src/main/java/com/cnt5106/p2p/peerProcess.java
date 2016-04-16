package com.cnt5106.p2p;

/**
 * Created by Dylan Richardson on 3/10/2016.
 *
 * A model class for containing information about peers
 */



import com.cnt5106.p2p.models.RemotePeerInfo;

import java.io.IOException;
import java.util.*;
import java.lang.*;
import java.nio.file.FileSystems;

public class peerProcess {

    // PeerInfo data
    private int pID;
    private String hostName;
    private int lPort;
    private Boolean hasFile;
    
    // Common data
    private int preferredNeighbors;
    private int unchokingInterval;
    private int optUnchokingInterval;
    private String fileName;
    private int fileSize;
    private int pieceSize;

    // File piece bit field
    boolean[] pieces;

    public static void main(String[] args) {
        FileParser fp = FileParser.getInstance();
        // Read PeerInfo.cfg to match arg[0] to pID and hostname, lport, hasfile
        // Create two thread arrays to connect with other peers
        // Connect to previously made connections
        int pID = Integer.valueOf(args[0]);
        try 
        {
            ThreadManager.getInstance().createThreads(pID);
        }
        catch(Exception e) {
            try {
                BTLogger.getInstance().writeToLog(pID, Arrays.toString(e.getStackTrace()));
            }
            catch (IOException ioe) {
                e.printStackTrace();
            }
        }
    }

    public peerProcess(int pID, String hostName, int lPort, 
        Boolean hasFile) throws Exception
    {
        // FileParser instance will always have these values by this point in the program
        // Assign peer info config variables
        this.pID = pID;
        this.hostName = hostName;
        this.lPort = lPort;
        this.hasFile = hasFile;

        //Assign common config variables
        FileParser fp = FileParser.getInstance();
        fp.parseConfigFile(FileSystems.getDefault().getPath("Common.cfg"));
        this.preferredNeighbors = fp.getNumPreferredNeighbors();
        this.unchokingInterval = fp.getUnchokeInterval();
        this.optUnchokingInterval = fp.getOptUnchokeInterval();
        this.fileName = fp.getFileName();
        this.fileSize = fp.getFileSize();       // TODO: Do we need these?
        this.pieceSize = fp.getPieceSize();     // TODO: Do we need these?
        
        //calculate number of pieces and initialize piece array
        int numberOfPieces = fp.getNumPieces();
        pieces = new boolean[numberOfPieces];
        if(this.hasFile)
        {
            Arrays.fill(pieces, true);
        }
        PieceManager.setInstance(numberOfPieces, this.fileSize, this.pieceSize, this.pID, this.fileName);
    }

    public void setPID(int pID)
    {
        this.pID = pID;
    }

    public int getPID()
    {
        return pID;
    }

    public void setHostName(String hostName)
    {
        this.hostName = hostName;
    }

    public String getHostName()
    {
        return hostName;
    }

    public void setLPort(int lPort)
    {
        this.lPort = lPort;
    }

    public int getLPort()
    {
        return lPort;
    }

    public void setHasFile(Boolean hasFile)
    {
        this.hasFile = hasFile;
    }

    public Boolean getHasFile()
    {
        return hasFile;
    }

    public String toString()
    {
         return "Peer: " + pID + "\n"
                 + "Host Name: " + hostName + "\n"
                 + "Listening Port: " + lPort + "\n"
                 + "Has File: " + hasFile;
    }
}
