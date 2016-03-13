package com.cnt5106.p2p;

import org.junit.Test;
import org.junit.Assert;
import com.cnt5106.p2p.models.MessageType;
import java.util.*;
import java.nio.ByteBuffer;

public class MessageHandlerTest {
	@Test
	public void makeHandshake() {
		MessageHandler mh = MessageHandler.getInstance();
		byte[] handshake = mh.makeHandshake(1002);
    	String str = Arrays.toString(handshake);
    	String[] split = str.substring(1, str.length() - 1).split(", ");
    	StringBuilder sb = new StringBuilder();
    	for(int i = 0; i < 18; ++i) {
    		sb.append((char)Integer.parseInt(split[i]));
    	}
    	System.out.print(sb.toString());
    	Assert.assertEquals(sb.toString(), "P2PFILESHARINGPROJ");

    	StringBuilder sb2 = new StringBuilder();
    	for(int i = 18; i < 28; ++i) {
    		sb2.append(Integer.toString(Integer.parseInt(split[i])));
    	}
    	System.out.print(" " + sb2.toString());
		Assert.assertEquals(sb2.toString(), "0000000000");

		ByteBuffer bb = ByteBuffer.allocate(32);
		bb.put(handshake);
		int pid = bb.getInt(28);
		System.out.println(" " + pid);
		Assert.assertEquals(pid, 1002); 
	}
	@Test
	public void makeMessage() {
		MessageHandler mh = MessageHandler.getInstance();
		try {
			byte[] message = mh.makeMessage(MessageType.INTERESTED);
		ByteBuffer bb = ByteBuffer.allocate(5);
		bb.put(message);
		int length = bb.getInt(0);
		System.out.print("length: " + length);
		Assert.assertEquals(length, 0);

		byte type = bb.get(4);
		System.out.println(" type: " + type);
		Assert.assertEquals(type, (byte) 2);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Test
	public void makePayloadMessage() {
		MessageHandler mh = MessageHandler.getInstance();
		try {
			byte[] data = {0x00, 0x01};
			byte[] message = mh.makeMessage(MessageType.HAVE, data);
		ByteBuffer bb = ByteBuffer.allocate(7);
		bb.put(message);
		int length = bb.getInt(0);
		System.out.print("length: " + length);
		Assert.assertEquals(length, 2);

		byte type = bb.get(4);
		System.out.print(" type: " + type);
		Assert.assertEquals(type, (byte) 4);

		byte b = bb.get(5);
		byte b2 = bb.get(6);
		System.out.println(" bytes: " + b + " " + b2);
		Assert.assertEquals(b, data[0]);
		Assert.assertEquals(b2, data[1]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}