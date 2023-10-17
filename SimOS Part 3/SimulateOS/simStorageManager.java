package SimulateOS;

import Model.simLog;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class simStorageManager // ACTS AS Server OBJECT 
{
	private simProcessManager procMgr;
	private simLog log;
	private simStorageManager_FakeProcess fakeStorageMgr;
	Socket socket; // add socket to our variables
	

	public simStorageManager(int portNumber, simProcessManager procMgr, simInterrupt interrupts, simLog log)
	{
		portNumber = 6018; //specify our portnumber
		this.procMgr = procMgr;
		interrupts.registerInterruptServiceRoutine(simInterrupt.INTERRUPT.STRG_MGR_INSTR, this);
		interrupts.registerInterruptServiceRoutine(simInterrupt.INTERRUPT.STRG_MGR_DONE, this);
		this.log = log;
		this.fakeStorageMgr = new simStorageManager_FakeProcess(portNumber, interrupts, this.log);
		fakeStorageMgr.start();
		this.socket = create_Socket(socket, portNumber); //create our Socket using portnumber

	
	}

	//purpose: Process an interrupt assigned to the storage manager.
	//	Interrupt STRG_MGR_DONE tells storage manager that a DEVR or DEVW instruction has completed.
	//	Interrupt STRG_MGR_INSTR tells storage manager to exeucte a DEVR or DEVW instruction.
	//assumptions: None.
	//inputs:
	//	STRG_MGR_DONE: data is an Integer object representing a PCB number.
	//	STRG_MGR_INSTR: data is a cpuInstruction object.
	//post-conditions: 
	//	STRG_MGR_DONE: simPCB is removed from wait queue and put on ready queue.
	//	STRG_MGR_INSTR: simCPUInstruction data sent to the storage manager fake process; executingPCB put on wait queue.
	public void interruptServiceRoutine(Object data)
	{
		if (data instanceof Integer)
		{
			//Interrupt is from the storage manager fake process.
			//A device instruction has completed for a simPCB.
			Integer pcbNumber = (Integer)data;
			//Move PCB with pcbNumber from wait queue to ready queue
			procMgr.movePCBfromWaitToReady(pcbNumber);
		}
		else if (data instanceof simCPUInstruction)
		{
			//Interrupt is from cpu.
			//A device instruction needs to be executed by the storage manager fake process.
			simCPUInstruction instruction = (simCPUInstruction)data;
			//Get the executing PCB
			simPCB executingPCB = this.procMgr.getExecutingPCB();
			if (executingPCB == null)
				log.println("simStorageManager.interruptServiceRoutine (CPU instruction): " +
					"logic error - executingPCB should not be null!");
			else
			{
				log.println("simStorageManager.interruptServiceRoutine: send instruction " +
						instruction + " to storage manager for PCB number " +
						executingPCB.getNumber());
				//Put executingPCB on wait queue
				procMgr.addWaitQueue(executingPCB);
				sendToStorageManager_FakeProcess("" + instruction.getOpcode() + " " +
						instruction.getOperand() + " " + executingPCB.getNumber());
				//Start a context switch
				procMgr.startContextSwitch();
			}
		}
		else
			log.println("simStorageManager.interruptServiceRoutine: unknown data");
	}

	//purpose: Send message to storage manager using an IPC mechanism.
	//assumptions: None.
	//inputs: message - has the format "DEVR xx #" or "DEVW xx #" where
	//	one space separates each value in the message.
	//	xx is the integer value that immediately follows the DEVR/DEVW instruction
	//	# is the integer value representing the simPCB number of the process wanting to execute the DEVR/DEVW instruction
	//post-conditions: message has been sent to the storage manager (fake) process.
	public void sendToStorageManager_FakeProcess(String message)
	{
		
		//add code here to send message to the storage manager using an IPC mechanism.
		this.log.println("Sending message " + message + " to storage manager fake process");
		System.out.println("Client running.");

		try
		{
			PrintWriter pout = new PrintWriter(socket.getOutputStream(), true); // create our PrintWriter Object
			pout.println(message); //send our message to the socket
		}
		catch(IOException ioe)
		{
			System.err.println(ioe);
			System.out.println("We crashed in SimStorage Manager");
		}
	}

	//create our socket
	private Socket create_Socket(Socket socket, int portNumber)
	{
		try
		{
			socket = new Socket(InetAddress.getByName(null), portNumber); // create our Client socket --> uses rollback IP address (cuz localhost() kept timing out?)

		}
		catch (IOException ioe)
		{
			System.err.println(ioe);
			System.out.println("We failed to create our Normal Socket in SimStorageManager");
			socket = null;
		}
		return socket; //return our client socket
	}
}
