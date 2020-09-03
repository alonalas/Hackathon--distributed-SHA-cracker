package sha.server;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SHAServer 
{ 

	private static final int SHA_CRACKER_SERVER_PORT = 3117;
	private static final int MESSAGE_SIZE_BYTES = 586;
	private static final int MAX_THREADS = 5;

	public static void main(String[] args) throws IOException
	{

	    ExecutorService serverThreadPool = Executors.newFixedThreadPool(MAX_THREADS);

		System.out.println("SERVER: Server started..");
		DatagramSocket serverSocket = new DatagramSocket(SHA_CRACKER_SERVER_PORT, InetAddress.getByName("0.0.0.0")); //listen for both broadcast and unicast
		serverSocket.setBroadcast(true);

		while(true){ //server always up.

			byte[] receiveData = new byte[MESSAGE_SIZE_BYTES];
			DatagramPacket receivePacket = new DatagramPacket(receiveData, MESSAGE_SIZE_BYTES);
			
            serverSocket.receive(receivePacket); // handle each received message in a thread
            serverThreadPool.execute(new SHAMessageHandler(serverSocket, receivePacket));
            
		}     
	}

}