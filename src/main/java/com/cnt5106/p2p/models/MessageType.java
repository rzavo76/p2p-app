package com.cnt5106.p2p.models;

// MessageType enum created by Ryan Zavoral Mar. 12, 2016.

public enum MessageType {
	//associate types with values
	CHOKE (0),
	UNCHOKE (1),
	INTERESTED (2),
	NOTINTERESTED (3),
	HAVE (4),
	BITFIELD (5),
	REQUEST (6),
	PIECE (7);
	private final int value;
	private MessageType(int value) {
		this.value = value;
	}
	//able to retrieve the type value without altering it
	public int getValue() {
		return value;
	}
}