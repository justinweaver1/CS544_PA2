import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


public class server {

	public static final int MIN_PORT_NUM = 1024;
	public static final int MAX_PORT_NUM = 65535;
	public static boolean DEBUG_FLAG = false;
	public final static int PAYLOAD_DATA_SIZE = 30; //30 bytes for the String 
	public final static int PAYLOAD_METADATA_SIZE = 3; //3 bytes for type, seqnum and length

	/* 

		  Function Name: main

	     Description: This is the main function of the class which starts
		               execution.

	 */
	public void main(String[] args)
	{
		String serverAddress, emulatorSendPort, emulatorReceivePort, fileToWrite;

		//if 1 or 2 arguments are passed in, set variable
		if(args.length == 4 || args.length == 5)
		{
			serverAddress = args[0];
			emulatorSendPort = args[1];
			emulatorReceivePort = args[2];
			fileToWrite = args[3];

			if( (args.length == 5) && (args[4].equals("-debug")) )
			{
				DEBUG_FLAG = true;
			}

			int emulatorSendPortInt = Integer.parseInt(emulatorSendPort);
			int emulatorReceivePortInt = Integer.parseInt(emulatorReceivePort);

			ReceivePackets(serverAddress, emulatorSendPortInt, emulatorReceivePortInt, fileToWrite);

		}
		//else, print usage statement for user to try again
		else
		{
			System.out.println("\nUsage: server <serverAddress> <emulatorSendPort> <emulatorReceivePort> <fileToWrite> [-debug]\nNOTE: ports should be an integer between 1024 and 65535");
		}
	}//END MAIN

	/*
	 * Name: ReceivePackets
	 * 
	 * Description: 
	 * 
	 */
	private void ReceivePackets(String inServerAddress, int inEmulatorSendPort, int inEmulatorReceivePort, String inFileToWrite)
	{
		DatagramSocket sendSocket = null;
		DatagramPacket receiveSocket = null;

		try
		{
			byte[] sendData = null;
			byte[] receiveData = new byte[PAYLOAD_DATA_SIZE + PAYLOAD_METADATA_SIZE];	

			boolean loop = true;

			//Initialized the client socket that will be used to send packets
			sendSocket = new DatagramSocket();
			InetAddress IPAddress = InetAddress.getByName(inServerAddress);

			//Initialize the client socket that will listen for packets
			receiveSocket = new DatagramPacket(receiveData, receiveData.length);
		}
		catch(Exception e)
		{

		}

	}//END RECEIVEPACKETS

}
