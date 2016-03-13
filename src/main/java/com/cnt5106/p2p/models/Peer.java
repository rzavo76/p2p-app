package com.cnt5106.p2p.models;

/**
 * Created by Dylan Richardson on 3/10/2016.
 *
 * A model class for containing information about peers
 */

public class Peer {

    private int pID;
    private String hostName;
    private int lPort;
    private Boolean hasFile;

    public Peer(int pID, String hostName, int lPort, Boolean hasFile)
    {
        this.pID = pID;
        this.hostName = hostName;
        this.lPort = lPort;
        this.hasFile = hasFile;
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
