package com.cnt5106.p2p.models;

// MessageType enum created by Ryan Zavoral Mar. 12, 2016.
// Model for associate types with values

public enum MessageType {
	//link values with types
	CHOKE ((byte)0),
	UNCHOKE ((byte)1),
	INTERESTED ((byte)2),
	NOTINTERESTED ((byte)3),
	HAVE ((byte)4),
	BITFIELD ((byte)5),
	REQUEST ((byte)6),
	PIECE ((byte)7);
	private final byte value;
	private MessageType(byte value) {
		this.value = value;
	}
	//only retrieve the type value without altering it
	public byte getValue() {
		return value;
	}
}