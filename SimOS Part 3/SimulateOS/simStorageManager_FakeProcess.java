package SimulateOS;

import Model.simLog;

import java.lang.IllegalArgumentException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.ServerSocket;
import java.net.Socket;


public class simStorageManager_FakeProcess extends Thread //ACTS AS SERVER OBJECT --> RECEIVES DATA
{
	private int portNumber;
	private simInterrupt interrupts;
	private simLog log;
	ServerSocket server_socket; //to accept connections

	public simStorageManager_FakeProcess(int portNumber, simInterrupt interrupts, simLog log)
	{	
		this.portNumber = portNumber;
		this.interrupts = interrupts;
		this.log = log;
		this.server_socket = create_server_socket(server_socket); // initialize and create our server socket
	}
	//purpose: Run the fake storage manager.
	//assumptions: None.
	//inputs: None.
	//post-conditions: simulated OS environment has been stopped.
	public void run()
	{
		
		this.log.println("simStorageManager_FakeProcess has started.");
		 try
		 {
			Socket client = server_socket.accept(); // accept our client connection
			InputStream in = client.getInputStream(); //get our client inputstream to read
			BufferedReader bin = new BufferedReader(new InputStreamReader(in)); // read our input stream

			String message;
			while( (message = bin.readLine()) != null) //some message was received and read
			{
				System.out.println("Client received msg: " + message); // display the message
				String [] message_array = message.split(" "); //get our xx value and our PCB number
				int wait_time = Integer.valueOf(message_array[1]); //convert xx value to an integer for use
				int pcbNumber = Integer.valueOf(message_array[2]); //convert PCB number to an integer
				wait_for_some_time(wait_time); //Sleep the thread for our designated amt of time.
				this.interrupts.addInterrupt(simInterrupt.INTERRUPT.STRG_MGR_DONE,pcbNumber); //add an interrupt to show that we're done with our process
			}
					
			
				
			client.close(); //Close our client connection
			
		 }
		 catch (IOException ioe)
		 {
			System.err.println(ioe);
			System.out.println("We crashed in SimStorage Manager Fake Process");
		 }



		this.log.println("simStorageManager_FakeProcess is ending ...");
	}

	//purpose: Return name of this class.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Return name of this class.
	public String toString()
	{
		return "simStorageManager_FakeProcess";
	}

	//creates our serversocket 
	private ServerSocket create_server_socket(ServerSocket serverSocket)
	{
		try
		{
			serverSocket = new ServerSocket(this.portNumber); //create the server socket port
			System.out.println("Server constructed."); // display message to inform user we created our server socket
		}
		catch (IOException ioe)
		{
			System.err.println(ioe);
			System.out.println("We failed to create our Server Socket in SimStorageManager_FakeProcess");
			serverSocket = null;
		}
		return serverSocket; //returns our server socket
	}

	//simulate work being done for a process -->sleeps the thread for a set amount of time 
	private void wait_for_some_time(int wait_time)
	{
		
		int sleep_time = wait_time * simOS.DEVICE_PAUSE_TIME; //calculate our total sleep time
		try 
		{
			log.println("We are doing starting our work");; //inform user we are doing some work (actually just sleeping)
			Thread.sleep(sleep_time); //sleep our thread to simulate work
			log.println("We finished our work"); // Inform user we finished work

		} 
		catch (InterruptedException e) 
		{
			System.err.println(e);
		}
	}
}
