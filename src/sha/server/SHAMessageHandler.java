package sha.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import sha.dto.CustomMessage;
import sha.dto.CustomMessage.MessageType;
import sha.utils.HelperFunctions;

public class SHAMessageHandler implements Runnable {

	private static final String TEAM_NAME = "AlonaTeam";
	
	DatagramSocket serverSocket;
	DatagramPacket receivePacket;
	
	public SHAMessageHandler(DatagramSocket serverSocket, DatagramPacket receivePacket) {
		
		this.serverSocket = serverSocket;
		this.receivePacket = receivePacket;
	}
	
	@Override
	public void run() {
		// get the IP and Port to send back the message 
		InetAddress clientIPAddress = receivePacket.getAddress();
		int port = receivePacket.getPort();

		CustomMessage recievedMessage;
		try {
			recievedMessage = CustomMessage.getCustomMessageFromBytes(receivePacket.getData());
			
	        byte a = recievedMessage.getType(); 
	        Byte b = new Byte(a);  
	        int ordinal = b.intValue(); 
	        
			MessageType msgType = MessageType.values()[ordinal];

			switch(msgType) {
			case DISCOVER:
				System.out.println("SERVER: Recieved DISCOVER");
				DatagramPacket offerPacket = createOffer(clientIPAddress, port);
				serverSocket.send(offerPacket);
				System.out.println("SERVER: Sent OFFER");
				break;

			case REQUEST:
				System.out.println("SERVER: Recieved REQUEST");

				String startRange = new String(recievedMessage.getRangeStart()).trim();
				String endRange = new String(recievedMessage.getRangeEnd()).trim();
				String hashToCrack = new String(recievedMessage.getHash()).trim();

				// validate hash
				if (!HelperFunctions.isValidHash(hashToCrack)) {
					System.out.println("ERROR: the provided hash is not valid.");
					break;
				}
				
				System.out.println("SERVER: StartRange: " + startRange + " EndRange: " + endRange + " Hash: " + hashToCrack);

				// de-hash and send Ack\Nack
				String crackedHash = HelperFunctions.tryDeHash(startRange, endRange, hashToCrack);
				if (crackedHash != null) {
					// send Ack
					System.out.println("SERVER: CRACKED: " + hashToCrack + " -> " + crackedHash);
					DatagramPacket ackPacket = createAck(clientIPAddress, port, crackedHash);
					serverSocket.send(ackPacket);
					System.out.println("SERVER: Sent ACK to client: " + clientIPAddress);
				}
				else {
					// send Nack
					System.out.println("SERVER: NOT CRACKED" + hashToCrack);
					DatagramPacket nackPacket = createNack(clientIPAddress, port);
					serverSocket.send(nackPacket);
					System.out.println("SERVER: Sent NACK to client: " + clientIPAddress);
				}
				break;	
			default:
				break;
			}
		} catch (Exception e) {
			System.out.println("SERVER: exception while handeling client messange.");
			System.out.println(e.getMessage());
		}
	}
	
	private static DatagramPacket createNack(InetAddress clientIPAddress, int port) {
		CustomMessage nackMsg = new CustomMessage(TEAM_NAME,
				MessageType.NACK,
				"",		// irrelevant for nack message
				0,	// irrelevant for nack message
				"", 	// irrelevant for nack message
				""); 	// irrelevant for nack message

		byte[] nackBuffer = CustomMessage.getCustomMessageAsBytes(nackMsg);
		System.out.println("SERVER: Created NACK msg with length: " + nackBuffer.length); // this should always be 586 because of the padding
		DatagramPacket nackPacket = new DatagramPacket(nackBuffer, nackBuffer.length, clientIPAddress, port);
		return nackPacket;
	}

	private static DatagramPacket createAck(InetAddress clientIPAddress, int port, String crackedHash){
		CustomMessage ackMsg = new CustomMessage(TEAM_NAME,
				MessageType.ACK,
				crackedHash,
				crackedHash.length(),
				"", 	// irrelevant for ack message
				""); 	// irrelevant for ack message

		byte[] ackBuffer = CustomMessage.getCustomMessageAsBytes(ackMsg);
		System.out.println("SERVER: Created ACK msg with length: " + ackBuffer.length); // this should always be 586 because of the padding
		DatagramPacket ackPacket = new DatagramPacket(ackBuffer, ackBuffer.length, clientIPAddress, port);
		return ackPacket;
	}

	private static DatagramPacket createOffer(InetAddress clientIPAddress, int port) {
		CustomMessage offerMsg = new CustomMessage(TEAM_NAME,
				MessageType.OFFER,
				"",// irrelevant for offer message
				0,	// irrelevant for offer message
				"", 	// irrelevant for offer message
				""); 	// irrelevant for offer message

		// send OFFER message
		// convert custom message to bytes
		byte[] offerBuffer = CustomMessage.getCustomMessageAsBytes(offerMsg);
		System.out.println("SERVER: Created OFFER msg with length: " + offerBuffer.length); // this should always be 586 because of the padding
		DatagramPacket offerPacket = new DatagramPacket(offerBuffer, offerBuffer.length, clientIPAddress, port);
		return offerPacket;
	} 

}
