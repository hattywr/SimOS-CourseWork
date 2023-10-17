package SimulateOS;

import Model.simLog;

import java.math.BigInteger;
import java.util.LinkedList;

public class simMemoryManagerFree
{
	private simLog log;
	private int firstFreeFrame;
	private int totalRAM_Frames;
	private LinkedList<Integer> freeFrameQueue;
	private simVirtualMemory vSimVirtualMemory;

	//purpose: 
	//assumptions:
   //    RAM % pageSize == 0;
	//		RAM > osSize; 
	//		8GB / 512 (largest RAM / smallest pageSize) == 16,777,216 (fits within 32-bit int)
	//		8GB == 8,589,934,592 == 8 * 1024 * 1024 * 1024
	//inputs: RAM - total RAM used by current scenario.
   //       pageSize - page size used by current scenario.
   //       osSize - OS size used by current scenario.
   //       log - the simLog to report progress and errors.
	//post-conditions:
   //    firstFreeFrame and lastFreeFrame are set.
	//		freeFrameQueue created but EMPTY.
	public simMemoryManagerFree(BigInteger RAM, BigInteger pageSize, BigInteger osSize, simLog log, simVirtualMemory vSimVirtualMemory)
	{
		this.log = log;
		freeFrameQueue = new LinkedList<Integer>();
		BigInteger nbrRAM_Frames, nbrOSFrames, nbrFreeFrames;
		BigInteger[] divideRemainder;
		

		try
		{
			//Compute total number of RAM frames
			divideRemainder = RAM.divideAndRemainder(pageSize);
			if (divideRemainder[1].compareTo(BigInteger.ZERO) != 0)
				log.println("simMemoryManagerFree.constructor Logic error: RAM not factor of pageSize.");
			nbrRAM_Frames = divideRemainder[0];

			//Compute number of frames to reserve for OS
			divideRemainder = osSize.divideAndRemainder(pageSize);
			if (divideRemainder[1].compareTo(BigInteger.ZERO) == 1)
				nbrOSFrames = divideRemainder[0].add(BigInteger.ONE);
			else
				nbrOSFrames = divideRemainder[0];

			//Save first free frame number and total number of RAM frames.
			this.firstFreeFrame = nbrOSFrames.intValueExact();
			this.totalRAM_Frames = nbrRAM_Frames.intValueExact();
			
			log.println("simMemoryManagerFree.constructor: nbrRAM_Frames=" + nbrRAM_Frames.toString() +
					" nbrOSFrames=" + nbrOSFrames.toString());
			log.println("simMemoryManagerFree.constructor: totalRAM_Frames=" + this.totalRAM_Frames +
					" firstFreeFrame=" + this.firstFreeFrame +
					" number of free frames=" + (this.totalRAM_Frames - this.firstFreeFrame));
		}
		catch (Exception ex)
		{
			log.println("simMemoryManagerFree.constructor Exception: " + ex);
		}
	}

	//purpose: Add free frame to list of free frames.
	//assumptions: freeFrameQueue object exists; called by simMemoryManager and simPCB.
	//inputs: frameNumber - frame number that is now free.
	//post-conditions: frameNumber added to list of free frames.
	public void addFreeFrame(Integer frameNumber)
	{
		freeFrameQueue.add(frameNumber);
	}

	//purpose: Get one free frame number.
	//assumptions: memFree object exists; called by simMemoryManager.
	//inputs: None.
	//post-conditions: Returns a free frame number OR null when no free frame is available.
	public Integer getOneFreeFrameNumber()
	{
		Integer frameNumber = null;
		if (firstFreeFrame < totalRAM_Frames)
		{
			//Use next free frame created from original allocation of OS memory space.
			frameNumber = Integer.valueOf(firstFreeFrame);
			firstFreeFrame++;
		}
		else if (this.freeFrameQueue.size() > 0)
			//Original list of free frames is now empty.
			//Get free frame from linked list.
			frameNumber = this.freeFrameQueue.removeFirst();
		else
		{
			//No free frames exist.
			//Need to use virtual memory manager to obtain a free frame.
			log.println("simMemoryManagerFree.getFrameNumber: no more free frames; need to use virtual memory");
			//Following statement is temporary. Should be replaced with use of virtual memory manager logic.
			frameNumber = null;
		}
		return frameNumber;
	}

	//purpose: Return number of free frames.
	//assumptions: called by simMemoryManager.
	//inputs: None.
	//post-conditions: Return number of free frames.
	public int getNumberOfFreeFrames()
	{
		return (this.totalRAM_Frames - this.firstFreeFrame) + this.freeFrameQueue.size();
	}
}
