/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

package com.cnt5106.p2p.models;

public class RemotePeerInfo {
	public int peerId;
	public String peerAddress;
	public int peerPort;
	public boolean hasFile;
	
	public RemotePeerInfo(int pId, String pAddress, int pPort) 
	{
		peerId = pId;
		peerAddress = pAddress;
		peerPort = pPort;
		hasFile = false;
	}
	
	public RemotePeerInfo(int pid, String pAddress, int pPort, boolean hasFile)
	{
		peerId = pid;
		peerAddress = pAddress;
		peerPort = pPort;
		this.hasFile = hasFile;
	}
}
