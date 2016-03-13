package com.cnt5106.p2p.models;

/**
 * Created by Dylan Richardson on 3/10/2016.
 *
 * A model class for containing information about peers
 */


import java.util.*;
import java.lang.*;

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

    // Thread manager

    public static void main(String[] args) {
        // Read PeerInfo.cfg to match arg[0] to pID and store hostname, lport, hasfile
        // Create two thread arrays to connect with other peers
        // Connect to previously made connections
    }

    public peerProcess(int pID, String hostName, int lPort, 
        Boolean hasFile, int preferredNeighbors, 
        int unchokingInterval, int optUnchokingInterval, 
        String fileName, int fileSize, int pieceSize)
    {
        this.pID = pID;
        this.hostName = hostName;
        this.lPort = lPort;
        this.hasFile = hasFile;

        this.preferredNeighbors = preferredNeighbors;
        this.unchokingInterval = unchokingInterval;
        this.optUnchokingInterval = optUnchokingInterval;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;
        
        pieces = new boolean[(int)Math.ceil((double)fileSize/pieceSize)];
        if(hasFile) {
            Arrays.fill(pieces, hasFile);
        }
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
