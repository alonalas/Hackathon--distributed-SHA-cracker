package DTO;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import Utils.HelperFunctions;

public class CustomMessage {
	
	
	public enum MessageType {
	    DISCOVER,
	    OFFER,
	    REQUEST,
	    ACK,
	    NACK
	}
	
	private byte[] teamName;
	private byte[] type;
	private byte[] hash;
	private byte originalLength;
	private byte[] rangeStart;
	private byte[] rangeEnd;
	
	public CustomMessage(String teamName, MessageType type, String hash, String originalLength, 
			String originalStartRange, String originalEndRange) {
		
		this.teamName = new byte[32];
		teamName = pad(teamName, 32, ' ');
		this.teamName = teamName.getBytes();
		
		//get type int from enum (types are defined in range 1-5)
		this.type = new byte[1];
		this.type = String.valueOf(type.ordinal()).getBytes();

		this.hash = new byte[40];
		hash = pad(hash, 40, ' ');
		this.hash = hash.getBytes();
		
		this.originalLength = originalLength.getBytes()[0];
		
		this.rangeStart = new byte[256];
		originalStartRange = pad(originalStartRange, 256, ' ');
		this.rangeStart = originalStartRange.getBytes();
		
		this.rangeEnd = new byte[256];
		originalEndRange = pad(originalEndRange, 256, ' ');
		this.rangeEnd = originalEndRange.getBytes();
		
	}
	
	public String pad(String str, int size, char padChar)
	{
	  StringBuffer padded = new StringBuffer(str);
	  while (padded.length() < size)
	  {
	    padded.append(padChar);
	  }
	  return padded.toString();
	}
	
	public static byte[] getCustomMessageAsBytes(CustomMessage msg) {
				
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		try {
			outputStream.write(msg.getTeamName());
			outputStream.write(msg.getType());
			outputStream.write(msg.getHash());
			outputStream.write(msg.getOriginalLength());
			outputStream.write(msg.getRangeStart());
			outputStream.write(msg.getRangeEnd());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("wrong input");
		}

		byte[] bytes = outputStream.toByteArray( );
		
		return bytes;
	}
	
	public static CustomMessage getCustomMessageFromBytes(byte[] bytes) {
			
		String teamName = new String(Arrays.copyOfRange(bytes, 0, 32)).trim();
		MessageType type = MessageType.values()[Integer.parseInt(new String(Arrays.copyOfRange(bytes, 32, 33)))];
		String hash = new String(Arrays.copyOfRange(bytes, 33, 73)).trim();
		String originalLength = new String(Arrays.copyOfRange(bytes, 73, 74)).trim();
		String rangeStart = new String(Arrays.copyOfRange(bytes, 74, 330)).trim();
		String rangeEnd = new String(Arrays.copyOfRange(bytes, 330, 556)).trim();
		
		CustomMessage messageFromBytes = new CustomMessage(teamName, type, hash, originalLength, rangeStart, rangeEnd); 
		return messageFromBytes;
	}
	
	public byte[] getTeamName() {
		return teamName;
	}

	public byte[] getType() {
		return type;
	}

	public byte[] getHash() {
		return hash;
	}

	public byte getOriginalLength() {
		return originalLength;
	}

	public byte[] getRangeStart() {
		return rangeStart;
	}

	public byte[] getRangeEnd() {
		return rangeEnd;
	}

	
	public static void main(String[] args) {
		
		String originalText = "Test";
		String hashToCrack = HelperFunctions.hash(originalText);
	    //int originalLength = originalText.length();
	    
	    String originalStartRange = "aaaa";
	    String originalEndRange = "zzzz";
	    
		CustomMessage testMsg = new CustomMessage("AlonaTeam",
													MessageType.OFFER,
													hashToCrack,
													"115", //originalLength
													originalStartRange, originalEndRange);
		System.out.println(testMsg);
		
		byte[] arr = getCustomMessageAsBytes(testMsg);
		
		CustomMessage msg = getCustomMessageFromBytes(arr);
		
	}
}
