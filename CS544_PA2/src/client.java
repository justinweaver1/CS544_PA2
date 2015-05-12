import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.nio.charset.*;

/*
   Class Name: client

   Description: This class is responsible for the client side of Programming
	             Assignment #2.

   Author:  Justin Weaver

   Date:  

 */
public class client
{
	public final static int PAYLOAD_DATA_SIZE = 30; //30 bytes for the String 
	public final static int PAYLOAD_METADATA_SIZE = 3; //3 bytes for type, seqnum and length
	public static boolean DEBUG_FLAG = false;

	/*

	  Function Name: main

     Description: This function is the intiation point of the client


	 */
	public void main(String[] args)
	{
		String serverAddress, emulatorSendPort, emulatorReceivePort, fileToTransfer;

		//if three parameters are passed in
		//set the variables to their values
		if(args.length == 4 || args.length == 5)
		{  
			serverAddress = args[0];
			emulatorSendPort = args[1];
			emulatorReceivePort = args[2];
			fileToTransfer = args[3];

			if( (args.length == 5) && (args[4].equals("-debug")) )
			{
				DEBUG_FLAG = true;
			}

			if(DEBUG_FLAG)
			{	 
				System.out.printf("\nCLIENT: Starting transfer of file %s on host %s:%s. Receiving ACKS on %s:%s...\n", fileToTransfer, serverAddress, emulatorSendPort, serverAddress, emulatorReceivePort);
			} 

			int emulatorSendPortInt = Integer.parseInt(emulatorSendPort);
			int emulatorReceivePortInt = Integer.parseInt(emulatorReceivePort);
			
			SendFile(serverAddress, emulatorSendPortInt, emulatorReceivePortInt, fileToTransfer);

		}
		//else, issue a usage statement for user to try again
		else
		{
			System.out.println("\nUsage: client <Host Address> <Emulator Send Port> <Emulator Receive Port> <File To Send> [-debug]\nNOTE: ports must be between 1024 and 65535");
		}

	}//END MAIN
	
	/*
	 * Name: Send File
	 * 
	 * Description: 
	 */
	private void SendFile(String inServerAddress, int inEmulatorSendPort, int inEmulatorReceivePort, String inFileToTransfer)
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
		  
          String fileContent = new String(Files.readAllBytes(Paths.get(inFileToTransfer)), StandardCharsets.US_ASCII);
          
          int fileContentSize = fileContent.length();
            
          //Arrive at # of packets by taking total size and dividing by the packet size
          int numOfPackets = (int)Math.ceil((double)fileContentSize / (PAYLOAD_DATA_SIZE + PAYLOAD_METADATA_SIZE));
            
          if(DEBUG_FLAG)
          {
            System.out.println("\nCLIENT: Number of Packets: " + numOfPackets);
          }
            
          while(loop)
          {
            //IF timer not expired
        	//  IF send window not full
        	//     send new packet
        	//  ELSE
        	//    check receive port and process  
        	//ELSE 
        	//  perform timer expired behavior
        	 
          }
          
        }
        catch(Exception e)
        {
        	
        }
		
	}//END SENDFILE

}//END CLIENT CLASS

