import java.net.*;
import java.io.*;
import java.util.Random;
import java.lang.*;
import java.util.concurrent.TimeUnit;

/*

  Class Name:  server

  Description: This class is the server class for Programming Assingment #1.
               It starts up by listening on a pre-negotiated port number on
				   the server it is started on. It listens for a negotiation
					message from a client. When it receives a message from a
					client with the correct negotiation pin, it responds to the
					client with a random port number to facilitate the rest of the
				  	communication. The server then listens on that port for the
					client to send one to many packets containing a the contents
					of an ASCII text file and outputs the final received file to
					a file locally named "received.txt". During the file content
					exchange, the server respond to each packet received by the
					client with the an upper-case verison of the content that the
					client sent. 

	Author:     Justin Weaver

   Date:       4/26/2015	

*/
public class server
{

  public static final int PAYLOAD_SIZE_PER_PACKET = 16;
  public static final int MIN_PORT_NUM = 1024;
  public static final int MAX_PORT_NUM = 65535;
  public static final int NEGOTIATION_KEY = 117;
  public static final String TRANSFERRED_FILE_NAME = "received.txt";
  public final static String FILE_LEN_SEPARATOR = "@";
  public static boolean DEBUG_FLAG = false;

  /* 
	
	  Function Name: main

     Description: This is the main function of the class which starts
	               execution. The application accepts one required parameter
						and one optional parameter.

						The required parameter is n_port, the negotiated port
						number for which the server initially connect on the local
						machine and start listening. 

						The optional parameters is "-debug" flag which outputs a
						significant amount of information to the console for
						debugging purposes.

						Running the application without the correct number of
						parameters will results in a usage statement printed in
						the console window.
	
	*/
  public static void main(String[] args)
  {
     String n_port;

	  //if 1 or 2 arguments are passed in, set variable
     if(args.length == 1 || args.length == 2)
	  {
		  n_port = args[0];

		  if( (args.length == 2) && (args[1].equals("-debug")) )
		  {
          DEBUG_FLAG = true;
		  }

        DatagramSocket commSocket = ListenForClient(Integer.parseInt(n_port));
          
        if(commSocket != null)
        {
          //Continue processing
          ReceiveFile(commSocket);
        }
	  }
	  //else, print usage statement for user to try again
	  else
	  {
       System.out.println("\nUsage: server <n_port> [-debug]\nNOTE: n_port should be an integer between 1024 and 65535");
	  }
  }//END MAIN

  /*
	
     Function Name: ListenForClient

     Description: This function is responsible for initially connecting to
	               the negotiated port number on the local machine in which
						this process is started, receive a message from a client,
						check the negotiation key send by the client. If the
						negotiation key is correct, then the server generates a
						random port number to facilitate the rest of the session's
						communication and sends that port number to the client.
	  
	
	*/
  public static DatagramSocket ListenForClient(int portNum)
  {

     BufferedReader fromClient = null;
     DatagramSocket serverCommSocket = null;
     ServerSocket socket = null;
     Socket listenSocket = null;

     try
	  {
        socket = new ServerSocket(portNum);
		  
		  if(DEBUG_FLAG)
		  { 
		    System.out.printf("\nSERVER: Listening on port %d...\n", portNum);
        }
 
		  //Wait and listen for initial message from client
        listenSocket = socket.accept();

		  fromClient = new BufferedReader(new InputStreamReader(listenSocket.getInputStream()));
        
		  DataOutputStream outToClient = new DataOutputStream(listenSocket.getOutputStream());

        //Receive initial message from client
        String clientMsg = fromClient.readLine();
        
		  if(DEBUG_FLAG)
		  {  
          System.out.println("SERVER: From Client: " + clientMsg);
		  }

        String actualPortNum = "-1";
              
        int clientMsgConverted = Integer.parseInt(clientMsg);
        
		  if(DEBUG_FLAG)
		  {  
          System.out.println("SERVER: converted client messge: " + clientMsgConverted);
		  }

        //If correct key is received, generate the random port number for
        //additional communication
        if(Integer.parseInt(clientMsg) == NEGOTIATION_KEY)
        {
           Random randGen = new Random();
              
           actualPortNum = Integer.toString(randGen.nextInt(MAX_PORT_NUM - MIN_PORT_NUM) + MIN_PORT_NUM);
        }
        
		  //Required output of random port used for further client communication  
        System.out.println("SERVER: Negotiation detected. Selected random port " + actualPortNum);
          
        //If there was no error, create the UDP socket
        //that further communication will be held on prior
        //to sending response to client
        if(Integer.parseInt(actualPortNum) >= 0)
        {
          serverCommSocket = new DatagramSocket(Integer.parseInt(actualPortNum));
        }
          
        outToClient.writeBytes(actualPortNum);
        
		  if(DEBUG_FLAG)
		  {  
          System.out.println("SERVER: sent message to client");
          System.out.flush();
        }
        
	  }
	  catch(Exception e)
	  {
         System.out.println("SERVER: Exception has occured: " + e.getMessage());
          
         CharArrayWriter cw = new CharArrayWriter();
         PrintWriter w = new PrintWriter(cw);
         e.printStackTrace(w);
         w.close();
         String trace = cw.toString();
          
         System.out.println("\n" + trace);
	  }
     finally
     {
        //Close TCP sockets
        if(listenSocket != null && listenSocket.isConnected())
        {
          try
          {
            listenSocket.close();
          }
          catch(Exception e)
          {
            System.out.println("SERVER: Error closing listener socket: " + e.getMessage());
          }
        }
          
        if(socket != null && !socket.isClosed())
        {
          try
          {
            socket.close();
          }
          catch(Exception e)
          {
            System.out.println("SERVER: Error closing server socket: " + e.getMessage());
          }      
        }         
      }

   return serverCommSocket;

  }//END FUNCTION
    

  /*
      Function Name: ReceiveFile

      Description: This function facilitates the communication between the
		             server and client for the client sending the ASCII text
						 file to the server. The server listens for a client
						 message, processes the payload and sends a response. The
						 response to the client containts the sent payload
						 translated to upper case. After the last packet is
						 received by the server the server outputs the constrcuted
						 ASCII text file locally in a file named received.txt. 

	*/
  public static void ReceiveFile(DatagramSocket commSocket)
  {
    try
    {
      byte[] receiveData, sendData = null;
      boolean loop = true;
      int fileSize = 0;

      String constructedMsg = new String();

		//Keep looping and listening for the next client message
		//until the last message is received
	   while(loop)
		{
		  //recieveData = packet deignator + size (packet 1 only) + payload
		  receiveData = new byte[1 + 12 + PAYLOAD_SIZE_PER_PACKET];

		  DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            
        if(DEBUG_FLAG)
		  {
		    System.out.println("SERVER: waiting for next client message");
          System.out.flush();        
        }
        
        //Listen for message from client		  
		  commSocket.receive(receivePacket);
            
        String receivedMessage = new String(receivePacket.getData());
        
	     if(DEBUG_FLAG)
		  {	  
          System.out.println("SERVER: received message: " + receivedMessage);
	
			 System.out.flush();
        }

        int startTextIdx = 1;
        int endTextIdx = startTextIdx + PAYLOAD_SIZE_PER_PACKET;
        
        if(DEBUG_FLAG)
        {
          System.out.println("SERVER: receivedMessage.length(): " + receivedMessage.length());
        }
            
		  //Check if this is the final message in the exchange
        if(receivedMessage.charAt(0) == '2')
		  {
          loop = false;
            
           //Modify the last message end text index in case
           //it is not exactly the length of the packet
           endTextIdx = fileSize - constructedMsg.length() + 1; //plus 1 for character offset
              
           if(DEBUG_FLAG)
           {
             System.out.println("SERVER: Final message vars: endTextIdx = " + endTextIdx + "; fileSize = " + fileSize + "; constructedMsg length = " + constructedMsg.length());
           }
	     }

        //Check if this is the first message of the exchange
		  if(receivedMessage.charAt(0) == '0')
		  {
          int delimeterIdx = receivedMessage.indexOf(FILE_LEN_SEPARATOR);
          
		    //If there is no delimeter in the message that defines the message
			 //size then something went wrong. Throw an exception.	 
          if(delimeterIdx == -1)
          {
            throw new Exception("The first message received did not contain the expected delimiter to designate the file size");    
          }
              
			 startTextIdx = delimeterIdx + 1;
			 fileSize = Integer.parseInt(receivedMessage.substring(1, delimeterIdx));
              
          endTextIdx = startTextIdx + PAYLOAD_SIZE_PER_PACKET;
		     
			  if(DEBUG_FLAG)
			  {
			    System.out.println("SERVER: File size received is: " + fileSize);
			  }
		  }

		  //Extract the actual message content from
		  //the client message received
        String messageText = new String(receivedMessage.substring(startTextIdx, endTextIdx));
 
		  //Concat the received message text for each packet from the client
		  //to reconstruct the sent file
        constructedMsg += messageText;

		  if(DEBUG_FLAG)
		  {
		    System.out.println("SERVER: constructed message: " + constructedMsg);
              System.out.println("SERVER: constructed message size: " + constructedMsg.length());
        }

		  //Construct the response message to send back to the client
		  //(upper case version of client message text) and format for sending
		  String responseMessage = new String(messageText.toUpperCase());
				 
        if(DEBUG_FLAG)
        {
          System.out.println("SERVER: Sending response msg: " + responseMessage);
        }
            
		  sendData = responseMessage.getBytes();
		
        //Get information needed to send response		  
        InetAddress IPAddress = receivePacket.getAddress();
	     int port = receivePacket.getPort();

		  //Create response packet
		  DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length,IPAddress, port);
	     
        //Send response packet to client		  
		  commSocket.send(sendPacket);
		
		}//END WHILE LOOP
        
		//Write the final constructed text send from client to a file
      PrintWriter outputToFile = new PrintWriter(TRANSFERRED_FILE_NAME, "US-ASCII");
      
		outputToFile.print(constructedMsg);
		outputToFile.close();
        
      if(DEBUG_FLAG)
      {
        System.out.println("SERVER: Final content bytes count: " + constructedMsg.length());
      }

    }//END TRY BLOCK
	 catch(Exception e)
    {
      System.out.println("SERVER: Exception has occured: " + e.getMessage());
        
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
      if(commSocket != null && commSocket.isConnected())
      {
        //Close the UDP socket
        commSocket.close();
      }
    }

  }//END FUNCTION

}//END CLASS

