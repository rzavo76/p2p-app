package com.cnt5106.p2p;

import org.junit.Test;
import org.junit.Assert;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.*;
import java.lang.*;
import java.util.*;

public class BTLoggerTest {
	private String readLogAndDelete(int pid) throws Exception {
		Path filePath = FileSystems.getDefault().getPath("log_peer_" + pid + ".log");
    	if (!Files.exists(filePath))
		{
			filePath = Files.createFile(filePath);
		}
    	Scanner in = new Scanner(filePath);
		String logString = in.nextLine();
		Files.delete(filePath);
		return logString;
	}
    @Test
    public void writeToLogs() {
        try {
	        BTLogger log = BTLogger.getInstance();
			int p1id = 1001;
			int p2id = 1002;
			int p3id = 1004;
			int p4id = 1005;
			String logString;
			String logString2;
			log.setPid(p1id);
			logString = log.TCPConnectTo(p2id);
			log.writeToLog(logString);
			log.setPid(p3id);
			logString2 = log.TCPConnectFrom(p4id);
			log.writeToLog(logString2);
			Assert.assertEquals(logString, readLogAndDelete(p1id) + "\n");
			Assert.assertEquals(logString2, readLogAndDelete(p3id) + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void TCPConnectToORFrom() {
    	try {
	        BTLogger log = BTLogger.getInstance();
			int p1id = 1001;
			int p3id = 1004;
			int p4id = 1005;
			String logString;
			String time;
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			log.setPid(p1id);
			logString = log.TCPConnectTo(p3id);
			time = sdf.format(Calendar.getInstance().getTime());
			Assert.assertEquals(time + ": Peer 1001 makes a connection to Peer 1004.\n", logString);
			System.out.print(logString);
			log.setPid(p1id);
			logString = log.TCPConnectFrom(p4id);
			time = sdf.format(Calendar.getInstance().getTime());
			Assert.assertEquals(time + ": Peer 1001 is connected from Peer 1005.\n", logString);
			System.out.print(logString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void changePreferredOROptimisticallyUnchokedNeighbors() {
    	try {
			BTLogger log = BTLogger.getInstance();
			int p1id = 1001;
			int p2id = 1002;
			int p3id = 1004;
			int p4id = 1005;
			int[] neighborList = {p2id, p3id, p4id};
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			String logString;
			String time;
			log.setPid(p1id);
   			logString = log.changeOfPrefNeighbors(neighborList);
   			time = sdf.format(Calendar.getInstance().getTime());
			Assert.assertEquals(time + ": Peer 1001 has the preferred neighbors 1002, 1004, 1005" 
				+  ".\n", logString);
   			System.out.print(logString);
			logString = log.changeOfOUNeighbor(p2id);
			time = sdf.format(Calendar.getInstance().getTime());
			Assert.assertEquals(time + ": Peer 1001 has the optimistically unchoked neighbor 1002" 
				+ ".\n", logString);
			System.out.print(logString);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    @Test
    public void chokeORUnchoke() {
    	try {
    		BTLogger log = BTLogger.getInstance();
			int p1id = 1001;
			int p2id = 1002;
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			String logString;
			String time;
			log.setPid(p1id);
			logString = log.unchoked(p2id);
			time = sdf.format(Calendar.getInstance().getTime());
			Assert.assertEquals(time + ": Peer 1001 is unchoked by 1002.\n", logString);
			System.out.print(logString);
			logString = log.choked(p2id);
			time = sdf.format(Calendar.getInstance().getTime());
			Assert.assertEquals(time + ": Peer 1001 is choked by 1002.\n", logString);
			System.out.print(logString);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    @Test
    public void receivedHaveORInterestedORUninterested() {
    	try {
    		BTLogger log = BTLogger.getInstance();
			int p1id = 1001;
			int p2id = 1002;
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			String logString;
			String time;
			log.setPid(p1id);
			logString = log.receivedHave(p2id, 4);
			time = sdf.format(Calendar.getInstance().getTime());
			Assert.assertEquals(time + ": Peer 1001 received the 'have' message from 1002 " 
				+ "for the piece 4.\n", logString);
			System.out.print(logString);
			logString = log.receivedInterested(p2id);
			time = sdf.format(Calendar.getInstance().getTime());
			Assert.assertEquals(time + ": Peer 1001 received the 'interested' message from 1002.\n"
				, logString);
			System.out.print(logString);
			logString = log.receivedNotInterested(p2id);
			time = sdf.format(Calendar.getInstance().getTime());
			Assert.assertEquals(time + ": Peer 1001 received the 'not interested' message from 1002.\n"
				, logString);
			System.out.print(logString);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
	@Test
    public void downloadedANDTimeCheck() {
    	try {
    		BTLogger log = BTLogger.getInstance();
			int p1id = 1001;
			int p2id = 1002;
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			String logString;
			String time;
			log.setPid(p1id);
			logString = log.downloadedPiece(9, p2id, 72);
			time = sdf.format(Calendar.getInstance().getTime());
			Assert.assertEquals(time + ": Peer 1001 has downloaded the piece 9 from 1002. Now " 
				+ "the number of pieces it has is 72.\n", logString);
			System.out.print(logString);
			Thread.sleep(1000);
			logString = log.downloadedFile();
			time = sdf.format(Calendar.getInstance().getTime());
			Assert.assertEquals(time + ": Peer 1001 has downloaded the complete file.\n", logString);
			System.out.print(logString);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
			
}