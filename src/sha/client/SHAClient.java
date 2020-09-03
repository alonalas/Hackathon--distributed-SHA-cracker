package sha.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.LinkedList;

import sha.dto.CustomMessage;
import sha.dto.CustomMessage.MessageType;
import sha.utils.HelperFunctions;

public class SHAClient {

	private static final String TEAM_NAME = "AlonaTeam";
	private static final int SHA_CRACKER_CLIENT_PORT = 3116;
	private static final int SHA_CRACKER_SERVER_PORT = 3117;
	private static final int OFFER_TIMEOUT_MILLISECONDS = 10000;
	private static final int ACK_NACK_TIMEOUT_MILLISECONDS = 15000;
	private static final int MESSAGE_SIZE_BYTES = 586;

	public static void main(String args[]) throws Exception
	{
		System.out.println("Welcome to " + TEAM_NAME + ". Please enter the hash:\n");
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		String hash = inFromUser.readLine(); // the hash to crack
		System.out.println("Please enter the input string length:\n");
		String lengthOfOriginalAsString = inFromUser.readLine();
		int lengthOfOriginal = Integer.parseInt(lengthOfOriginalAsString); 

		DatagramSocket clientSocket = new DatagramSocket(SHA_CRACKER_CLIENT_PORT, InetAddress.getLocalHost());
		clientSocket.setBroadcast(true); // allow Broadcast

		byte[] receiveData = new byte[MESSAGE_SIZE_BYTES];

		// create DISCOVER message
		DatagramPacket sendPacket = createDiscover();		

		// list for IPs of servers that sent us OFFER
		LinkedList<InetAddress> IPs = sendDiscoverAndCollectOffers(clientSocket, receiveData, sendPacket);

		if (IPs.isEmpty()) { 
			System.out.println("CLIENT: Didn't get OFFERS -> Finish -> Bye.");
		}
		//if IPs not empty
		else
		{
			System.out.println("CLIENT: Start building REQUESTS");
			sendRequests(hash, lengthOfOriginal, clientSocket, IPs);

			// receive ACKs\NACs
			clientSocket.setSoTimeout(ACK_NACK_TIMEOUT_MILLISECONDS);
			boolean recievedAck = false;
			while (!recievedAck){

				System.out.println("CLIENT: Receiving ACKs/NACKs messages...");
				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
				try {
					clientSocket.receive(receivePacket);
					CustomMessage recievedMessage = CustomMessage.getCustomMessageFromBytes(receivePacket.getData());
					InetAddress serverIPAddress = receivePacket.getAddress(); // IP of the server that answered

			        byte a = recievedMessage.getType(); 
			        Byte b = new Byte(a);  
			        int ordinal = b.intValue(); 
			        
					MessageType msgType = MessageType.values()[ordinal];

					switch(msgType) {
					case ACK:
						String crackedMessage = new String(recievedMessage.getHash()).trim();
						System.out.println("CLIENT: ACK recieved from: " + serverIPAddress + " cracked message: " + crackedMessage + " -> Finish client process.");
						clientSocket.close();
						recievedAck = true;
						break;
					case NACK:
						System.out.println("CLIENT: NACK recieved from: " + serverIPAddress);
						break;
					default:
						break;
					}
				}
				catch (SocketTimeoutException e) {
					System.out.println("CLIENT: ACK_NACK_TIMEOUT_MILLISECONDS reached. Didn't recived any ACK -> Finish client process");
					break;
				}
			}
			System.exit(0);
		}
	}

	private static DatagramPacket createDiscover() throws UnknownHostException {
		CustomMessage discoverMsg = new CustomMessage(TEAM_NAME,
				MessageType.DISCOVER,
				"",	// irrelevant for discover message
				0,		// irrelevant for discover message
				"", 	// irrelevant for discover message
				""); 	// irrelevant for discover message

		// convert custom message to bytes
		byte[] discoverbuffer = CustomMessage.getCustomMessageAsBytes(discoverMsg);
		
		// discover sent as Broadcast packet
		DatagramPacket sendPacket = new DatagramPacket(discoverbuffer, discoverbuffer.length, InetAddress.getByName("255.255.255.255"), SHA_CRACKER_SERVER_PORT);
		return sendPacket;
	}

	private static LinkedList<InetAddress> sendDiscoverAndCollectOffers(DatagramSocket clientSocket, byte[] receiveData, DatagramPacket sendPacket) throws IOException, SocketException, Exception {
		
		LinkedList<InetAddress> IPs = new LinkedList<>();
		System.out.println("CLIENT: Sending Broadcast DISCOVER messages...");
		clientSocket.send(sendPacket); // send discover
		
		// Receive data until timeout
		clientSocket.setSoTimeout(OFFER_TIMEOUT_MILLISECONDS);
		while (true){

			System.out.println("CLIENT: Receiving OFFER messages...");
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				clientSocket.receive(receivePacket);
				CustomMessage recievedMessage = CustomMessage.getCustomMessageFromBytes(receivePacket.getData());
				InetAddress serverIPAddress = receivePacket.getAddress(); // IP of the server that answered

		        byte a = recievedMessage.getType(); 
		        Byte b = new Byte(a);  
		        int ordinal = b.intValue(); 
		        
				MessageType msgType = MessageType.values()[ordinal];

				if (msgType.equals(MessageType.OFFER)) {
					System.out.println("CLIENT: Recieved OFFER from:" + serverIPAddress);
					IPs.add(serverIPAddress);
				}
			}
			catch (SocketTimeoutException e) {
				System.out.println("CLIENT: OFFER_TIMEOUT_MILLISECONDS reached. Finished waiting for OFFERs");
				break;
			}
		}
		return IPs;
	}

	/**
	 * create and send REQUESTs for each server IP that answered
	 * create domains according to num of server that answered
	 * @param hash
	 * @param lengthOfOriginal
	 * @param clientSocket
	 * @param IPs
	 * @throws IOException
	 */
	private static void sendRequests(String hash, int lengthOfOriginal, DatagramSocket clientSocket,
			LinkedList<InetAddress> IPs) throws IOException {
		String[] domains = HelperFunctions.divideToDomains(lengthOfOriginal, IPs.size());

		int i = 0;
		for (InetAddress serverIP : IPs) {
				String rangeStart = domains[i];
				String rangeEnd = domains[i + 1];
				DatagramPacket requestPacket = createRequest(hash, lengthOfOriginal, serverIP, rangeStart, rangeEnd);
				System.out.println("CLIENT: Sending REQUEST to: " + serverIP + " Scan from: " + rangeStart + " To: " + rangeEnd);
				clientSocket.send(requestPacket);
				i=i+2;
		}
	}

	private static DatagramPacket createRequest(String hash, int lengthOfOriginal, InetAddress serverIP,
			String rangeStart, String rangeEnd) {
		CustomMessage requestMsg = new CustomMessage(TEAM_NAME,
				MessageType.REQUEST,
				hash,
				lengthOfOriginal,
				rangeStart,
				rangeEnd);

		// send REQUEST message
		// convert custom message to bytes
		byte[] requestBuffer = CustomMessage.getCustomMessageAsBytes(requestMsg);
		System.out.println("CLIENT: Created REQUEST msg with length: " + requestBuffer.length); // this should always be 586 because of the padding
		DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length, serverIP, SHA_CRACKER_SERVER_PORT); // Unicast packet
		return requestPacket;
	}
}
