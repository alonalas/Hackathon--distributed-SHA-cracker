package sha.dto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import sha.utils.HelperFunctions;

public class CustomMessage {
	
	
	public enum MessageType {
	    DUMMY, //in order to start the real types from 1
		DISCOVER,
	    OFFER,
	    REQUEST,
	    ACK,
	    NACK;
	}
	
	private byte[] teamName;
	private byte type;
	private byte[] hash;
	private byte originalLength;
	private byte[] rangeStart;
	private byte[] rangeEnd;
	
	public CustomMessage(String teamName, MessageType type, String hash, int originalLength, 
			String originalStartRange, String originalEndRange) {
		
		this.teamName = new byte[32];
		teamName = pad(teamName, 32, ' ');
		this.teamName = teamName.getBytes();
		
		//get type int from enum (types are defined in range 1-5) and cast to byte.
		this.type = (byte)(type.ordinal());

		this.hash = new byte[40];
		hash = pad(hash, 40, ' ');
		this.hash = hash.getBytes();
		
		this.originalLength = (byte)originalLength;
		
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
				
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			
			outputStream.write(msg.getTeamName());
			outputStream.write(msg.getType());
			outputStream.write(msg.getHash());
			outputStream.write(msg.getOriginalLength());
			outputStream.write(msg.getRangeStart());
			outputStream.write(msg.getRangeEnd());
			
		} catch (IOException e) {
			e.printStackTrace();
		}

		byte[] bytes = outputStream.toByteArray( );
		
		return bytes;
	}
	
	public static CustomMessage getCustomMessageFromBytes(byte[] bytes) throws Exception{
		
		CustomMessage messageFromBytes = null;
		try {
			String teamName = new String(Arrays.copyOfRange(bytes, 0, 32)).trim();
			
	        byte a = bytes[32]; 
	        Byte b = new Byte(a);  
	        int ordinal = b.intValue();
	        //returns the name of the type (1 = discover)
			MessageType type = MessageType.values()[ordinal];
			
			String hash = new String(Arrays.copyOfRange(bytes, 33, 73)).trim();
			
	        byte c = bytes[73];
	        Byte d = new Byte(c);  
	        int originalLength = d.intValue();
	        
			String rangeStart = new String(Arrays.copyOfRange(bytes, 74, 330)).trim();
			String rangeEnd = new String(Arrays.copyOfRange(bytes, 331, 586)).trim();
		
			messageFromBytes = new CustomMessage(teamName, type, hash, originalLength, rangeStart, rangeEnd); 
		}
		catch (Exception e) { //parsing error
			throw new Exception("ERROR: can't convert byte [] to CustomMessage.");
		}
		return messageFromBytes;
	}
	
	
	public byte[] getTeamName() {
		return teamName;
	}

	public byte getType() {
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
													MessageType.DISCOVER,
													hashToCrack,
													115, //originalLength
													originalStartRange, originalEndRange);
		System.out.println(testMsg);
		
		byte[] arr = getCustomMessageAsBytes(testMsg);
		
		try {
			CustomMessage msg = getCustomMessageFromBytes(arr);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
