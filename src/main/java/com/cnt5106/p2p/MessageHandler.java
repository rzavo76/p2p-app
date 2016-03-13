package com.cnt5106.p2p;

// MessageHandler class created by Ryan Zavoral Mar. 12, 2016.

import java.nio.ByteBuffer;
import com.cnt5106.p2p.models.MessageType;

public class MessageHandler {
	private static MessageHandler instance = null;
	private MessageHandler() {}
	public static synchronized MessageHandler getInstance() {
		if(instance == null) {
			instance = new MessageHandler();
		}
		return instance;
	}
	public byte[] makeHandshake(int pID) {
		ByteBuffer data = ByteBuffer.allocate(18);
		data.put("P2PFILESHARINGPROJ".getBytes());
		data.allocate(10).putInt(0);
		data.allocate(4).putInt(pID);
		return data.array();
	}
	private byte[] makeBytes(int type) {
		ByteBuffer data = ByteBuffer.allocate(4);
		data.putInt(0);
		data.allocate(1).putInt(type);
		return data.array();
	}
	private byte[] makeBytes(int type, byte[] payload) {
		ByteBuffer data = ByteBuffer.allocate(4);
		data.putInt(payload.length);
		data.allocate(1).putInt(type);
		data.allocate(payload.length).put(payload);
		return data.array();
	}
	public byte[] makeMessage(MessageType type) throws Exception {
		switch(type) {
			case CHOKE:
			case UNCHOKE:
			case INTERESTED:
			case NOTINTERESTED:
			return makeBytes(type.getValue());
			default:
			throw new Exception("Invalid message type");
		}
	}
	public byte[] makeMessage(MessageType type, byte[] payload) throws Exception{
		switch(type) {
			case HAVE:
			case BITFIELD:
			case REQUEST:
			case PIECE:
			return makeBytes(type.getValue(), payload);
			default:
			throw new Exception("Invalid payload message type");
		}
	}
}