package com.cnt5106.p2p.models;

// MessageType enum created by Ryan Zavoral Mar. 12, 2016.
// Model for associate types with values

public enum MessageType {
	//link values with types
	CHOKE,
	UNCHOKE,
	INTERESTED,
	NOTINTERESTED,
	HAVE,
	BITFIELD,
	REQUEST,
	PIECE;
	
	private final byte value;
	private MessageType() { this.value = (byte)ordinal(); }
	//only retrieve the type value without altering it
	public byte getValue() { return value; }

	public static MessageType getMessageTypeFromByte(byte type)
	{
		try
		{
			return MessageType.values()[type];
		} catch( ArrayIndexOutOfBoundsException e )
		{
			throw new IllegalArgumentException("Unknown enum value :"+ type);
		}
	}
}