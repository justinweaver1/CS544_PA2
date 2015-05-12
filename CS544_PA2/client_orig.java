import java.io.*;
import java.net.*;
import java.lang.*;
import java.nio.file.*;
import java.nio.charset.*;

/*
   Class Name: client

   Description: This class is responsible for the client side of Programming
	             Assignment #1. It starts up sending a negotiation message to
					 the server which should already be listening on a
					 pre-negotiated host and port number. The first message
					 contains a negotiation key that the server should know. The
					 server then responds with a random port number used for the
					 rest of the communication. The client then switches to that
					 port and sends the ASCII text file identified upon startup.
					 The file is sent via one to many packets and each packet is
					 responded to from the server with an upper-case version of
					 the client-sent packet payload. This response is output on
					 the client side console. The client sends the final packet
					 containing the final text file content and then terminates.

   Author:  Justin Weaver

   Date: 4/26/2015	

*/
public class client
{
  public final static int NEGOTIATION_KEY = 117;
  public final static int PAYLOAD_SIZE_PER_PACKET = 16;
  public static boolean DEBUG_FLAG = false;
  public final static String FILE_LEN_SEPARATOR = "@";

  /*

	  Function Name: main

     Description: This function is the intiation point of the client
	               executable. It is responsible for reading any parameters
						and executing the applciation appropriately. There are
						three required parameters and one optional parameter.

						The first required parameters is the host name. This is
						either a server name or localhost if running locally.

						The second required parameter is the initial negotiated
						port number the server should already be listening on.

						The third required parameter is the file name of the ASCII
						text file to be sent to the server. This can be a file
						path or if no path is provided the file is assumed to be
						local to the start of this executable.

						The fourth and optional parameter is the -debug flag. This
						turns on output, which goes to the console, that is used
						for debugging purposes.

						If the incorrect number of parameters are used for this
						executable, a usage statement is printed to the console.

	*/
  public static void main(String[] args)
  {
    String server_address, n_port, filename;

	 //if three parameters are passed in
	 //set the variables to their values
    if(args.length == 3 || args.length == 4)
	 {  
      server_address = args[0];
		n_port = args[1];
		filename = args[2];

		if( (args.length == 4) && (args[3].equals("-debug")) )
	   {
        DEBUG_FLAG = true;
	   }

		if(DEBUG_FLAG)
		{	 
		  System.out.printf("\nCLIENT: Starting negotiaition for file %s on host %s:%s...\n", filename, server_address, n_port);
      } 

      int commPortNum = SendInitServerMessage(server_address, Integer.parseInt(n_port));
          
      if(commPortNum >= 0)
      {
        
        System.out.printf("\nCLIENT: Sending file %s to %s on port %s...\n", filename, server_address, commPortNum);
          
        //continue processing
        SendFile(commPortNum, filename, server_address);
      }
         
	  }
	  //else, issue a usage statement for user to try again
	  else
	  {
       System.out.println("\nUsage: client <host/server address> <n_port> <filename> [-debug]\nNOTE: n_port must be between 1024 and 65535");
	  }

  }//END MAIN
    
  /*
     Function Name: SendInitServerMessage

     Description: This function contains the functionality that sends the
	               initial negotiation communication with the server. It
						sends a negotiation messgae to the server on the
						negotiated port number. The server responds with a random
						port number in which the file transfer communicaiton will
						be held. 

	*/
  public static int SendInitServerMessage(String serverAddress, int portNum)
  {
 
    int actualPortNumInt = -1;
      Socket clientSocket = null;

		 try
		 {
         clientSocket = new Socket(serverAddress, portNum);
         DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
         BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         outToServer.writeBytes(new Integer(NEGOTIATION_KEY).toString() + '\n');
            
			if(DEBUG_FLAG)
			{
           System.out.println("CLIENT: Sent message to server");  
           System.out.println("CLIENT: Listening for response from server...");
             
           System.out.flush();
			}
             
         String actualPortNum = inFromServer.readLine();
             
         actualPortNumInt = Integer.parseInt(actualPortNum);
         
			if(DEBUG_FLAG)
			{
			  System.out.println("CLIENT: Actual Port Number is " + actualPortNumInt);
             
           System.out.flush();
			}

			//Set the random port number received from the server
			//to be returned
		   actualPortNumInt = Integer.parseInt(actualPortNum);
       }
		 catch(Exception e)
		 {
         System.out.println("\nCLIENT: Exception has occured: " + e.getMessage());
             
         CharArrayWriter cw = new CharArrayWriter();
         PrintWriter w = new PrintWriter(cw);
         
	  		e.printStackTrace(w);
         w.close();
         
			String trace = cw.toString();
		 }
      
       finally
       {
         if(clientSocket != null && clientSocket.isConnected())
         {
           try
           {
             //Close the TCP socket
             clientSocket.close();
           }
           catch(Exception e)
           {
             System.out.println("CLIENT: Error occurred when closing client socket: " + e.getMessage());
           }
         }
       }

		 return actualPortNumInt;

    }//END FUNCTION
    

    /*
       Function Name: SendFile

       Description: This function is contains the capability to send the
		              ASCII text file to the server in predefined packet
						  sizes. It reads the ASCII text file, chunks into
						  designated portions and sends one to many packets to the
						  server. Upon sending a packet, the server's response
						  (the upper case version of the send packet's payload) is
						  output to the console. Upon sending the last packet
						  containing the final text file content, the applciation
						  terminates.

	  */
    public static void SendFile(int commPort, String fileName, String hostName)
    {
        DatagramSocket clientSocket = null;
        
        try
        {
          clientSocket = new DatagramSocket();
          InetAddress IPAddress = InetAddress.getByName(hostName);
          byte[] sendData = null;
			 byte[] receiveData = null;
          
          String fileContent = new
			 String(Files.readAllBytes(Paths.get(fileName)), StandardCharsets.US_ASCII);
          
          int fileContentSize = fileContent.length();
          int fileContentSizeStrLen = Integer.toString(fileContentSize).length();
            
          //Arrive at # of packets by taking total size and dividing by the packet size
          int numOfPackets = (int)Math.ceil((double)fileContentSize / PAYLOAD_SIZE_PER_PACKET);
            
          int first_idx = 0;
          int last_idx = 0;
            
          if(DEBUG_FLAG)
          {
            System.out.println("\nCLIENT: Number of Packets: " + numOfPackets);
          }
            
          //FOR loop that constructs and sends the packets as well as
          //receives the response for each packet from the server
          for(int i = 0; i < numOfPackets; i++)
			 {
             String sendStringPrefix = "1";
              
             //If this is the first packet of the session
             if(i == 0)
				 {
               sendStringPrefix = "0" + Integer.toString(fileContentSize) + FILE_LEN_SEPARATOR;
                     
               //string.substring function is non-inclusive for last index
               last_idx = first_idx + (PAYLOAD_SIZE_PER_PACKET);
				 }	 

				 //If this is the last packet of the session
             else if(i ==  (numOfPackets - 1) )
				 {
               sendStringPrefix = "2";

               first_idx = last_idx;
                     
               //We must only retrieve a substring for the remaining
               //characters of the file string for this packet
					last_idx = first_idx + (fileContent.length() - first_idx);
				 }
             //Else, normal packet processing
             else
             {
               first_idx = last_idx;
                     
               //string.substring function is non-inclusive for last index
					//so the last_idx value must be one after the last character
					//needed in the substring
               last_idx = first_idx + PAYLOAD_SIZE_PER_PACKET;
             }
                 
             if(DEBUG_FLAG)
             {
               System.out.println("CLIENT: first_idx = " + first_idx + "; last_idx = " + last_idx);
             }

             //Concatinate the prefix and string to construct that packet content
				 String sendString = sendStringPrefix + fileContent.substring(first_idx, last_idx);
            
             sendData = sendString.getBytes();

             DatagramPacket sendDataPacket = new DatagramPacket(sendData, sendData.length, IPAddress, commPort);
             
				 if(DEBUG_FLAG)
				 {	 
               System.out.println("CLIENT: Sent to Server message: " + sendString);
              
               System.out.flush();
             }

             //Send the packet to the server
             clientSocket.send(sendDataPacket);

             receiveData = new byte[PAYLOAD_SIZE_PER_PACKET];

             DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

             //Listen for the response from the server
				 clientSocket.receive(receivePacket);

				 String responseFromServer = new String(receivePacket.getData());
                 
             if(DEBUG_FLAG)
             {
               System.out.println("CLIENT: Full message received: " + responseFromServer);
             }
             
				 //Required output of response received from server
             System.out.println("CLIENT: Received message: " + responseFromServer.substring(0, responseFromServer.length()));
				 System.out.flush();

			 }//END FOR LOOP

        }//END TRY BLOCK
        catch(Exception e)
        {
          System.out.println("\nCLIENT: Exception has occured: " + e.getMessage());
            
          CharArrayWriter cw = new CharArrayWriter();
          PrintWriter w = new PrintWriter(cw);
            
          e.printStackTrace(w);
          w.close();
            
          String trace = cw.toString();
            
          System.out.println("\n" + trace);
            
          System.out.flush();
        }
        finally
        {
          if(clientSocket != null && clientSocket.isConnected())
          {
            try
            {
              //Close UDP socket connection when we're all done.
              clientSocket.close();
            }
            catch(Exception e)
            {
              System.out.println("CLIENT: Error encountered when closing client socket: " + e.getMessage());
            }
          }
        }

    }//END FUNCTION

}//END CLASS
