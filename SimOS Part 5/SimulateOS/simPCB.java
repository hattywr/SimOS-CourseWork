package SimulateOS;

import Model.simLog;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;

public class simPCB
{
	private String id;
	private int arrivalTime;
	private ArrayList<simCPUInstruction> instructions;
	private simLog log;
	private BigInteger imageSize;	//size of the executable image (which must be loaded into physical memory)
	private int instructionPointer;

	//Statistics on a process
	private int startExecuteTime;
	private int startWaitTime;
	private int totalExecuteTime;
	private int totalWaitTime;
	private int turnaroundTime;

	private int number;	//unique number within scenario
	private static int nextNumber = 1;	//class variable: used to gaurentee each PCB has unique number

	//added as part of solution to memory manager Part 01.
   private simPageTable pageTable;
   private simDynamoMemory dynamoMemory;

	public simPCB(String id, int arrivalTime,
			ArrayList<simCPUInstruction> instructions, simLog log,
			BigInteger imageSize)
	{
		this.id = id;
		this.arrivalTime = arrivalTime;
		this.instructions = instructions;
		this.log = log;
		this.imageSize = imageSize;
		this.instructionPointer = 0;
		this.startWaitTime = arrivalTime;
		this.totalExecuteTime = 0;
		this.totalWaitTime = 0;
		this.turnaroundTime = 0;
		this.number = nextNumber;
		this.nextNumber++;
		this.dynamoMemory = new simDynamoMemory(log);
	}


	//purpose: Add to the total execute time.
	//assumptions:  None.
	//inputs: currentTime - the current CPU clock time.
	//post-condition: time added to totalExecuteTime.
	public void addExecuteTime(int currentTime)
	{
		int time = currentTime - startExecuteTime;
		totalExecuteTime = totalExecuteTime + time;
		log.println("simPCB.addExecuteTime time=" + time + " totalExecuteTime=" + totalExecuteTime);
	}

	//purpose: Add to the total wait time.
	//assumptions:  None.
	//inputs: currentTime - the current CPU clock time.
	//post-condition: time added to totalWaitTime.
	public void addWaitTime(int currentTime)
	{
		int time = currentTime - startWaitTime;
		totalWaitTime = totalWaitTime + time;
		log.println("simPCB.addWaitTime time=" + time + " totalWaitTime=" + totalWaitTime);
	}

	public String getId()
	{
		return id;
	}

	public int getArrivalTime()
	{
		return arrivalTime;
	}

	public int getExecuteTime()
	{
		return totalExecuteTime;
	}

	public BigInteger getImageSize()
	{
		return imageSize;
	}

	public simCPUInstruction getInstruction()
	{
		simCPUInstruction instruction = null;
		if (instructions.size() > instructionPointer)
			instruction = instructions.get(instructionPointer);
		return instruction;
	}

	public int getInstructionPointer()
	{
		return instructionPointer;
	}

	public int getInstructionsSize()
	{
		return instructions.size();
	}

	public int getNumber()
	{
		return number;
	}

	public int getTurnaroundTime()
	{
		return turnaroundTime;
	}

	public int getWaitTime()
	{
		return totalWaitTime;
	}

	public void removeInstruction()
	{
		instructionPointer++;
	}

	//purpose: Modify arrivalTime so that totalWaitTime is computed correctly.
	//assumptions: None.
	//inputs: time - the current simulated CPU clock time.
	//post-conditions: arrivalTime == time;
	public void setArrivalTime(int time)
	{
		arrivalTime = time;
	}

	//purpose: Modify startExecuteTime so that totalExecuteTime is computed correctly.
	//assumptions: None.
	//inputs: time - the current simulated CPU clock time.
	//post-conditions: startExecuteTime == time;
	public void setStartExecuteTime(int time)
	{
		startExecuteTime = time;
	}

	//purpose: Modify startWaitTime so that totalWaitTime is computed correctly.
	//assumptions: None.
	//inputs: time - the current simulated CPU clock time.
	//post-conditions: startWaitTime == time;
	public void setStartWaitTime(int time)
	{
		startWaitTime = time;
	}

	//purpose: Set the turnarount time.
	//assumptions:  None.
	//inputs: time - 
	//post-condition: time added to turnaroundTime.
	public void setTurnaroundTime(int turnaroundTime)
	{
		this.turnaroundTime = turnaroundTime;
		log.println("simPCB.setTurnaroundTime turnaroundTime=" + this.turnaroundTime);
	}
	
	//purpose: Return string containing this PCB's statistics.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Returns string showing this PCB's statistics.
	public String statisticsToString()
	{
		return "Id= " + id + " arrivalTime=" + arrivalTime +
			" totalExecuteTime=" + totalExecuteTime +
			" totalWaitTime=" + totalWaitTime +
			" turnaroundTime=" + turnaroundTime;
	}

	//purpose: Return string containing this PCB's basic information.
	//assumptions: None.
	//inputs: None.
	//post-conditions: Returns string containing this PCB's basic information.
	public String toString()
	{
		String result = "Id= " + id + " arrivalTime=" + arrivalTime +
			" imageSize=" + imageSize + " [";
		Iterator<simCPUInstruction> iter = instructions.iterator();
		while (iter.hasNext())
		{
			result = result + iter.next();
			if (iter.hasNext())
				result = result + ",";
		}
		result = result + "] instructionPointer=" + instructionPointer;
		return result;
	}

/* ADDED as part of solution to memory manager part 01 assignment */
	
	//purpose: Create a HashMap as the page table for this process.
	//assumptions: called by simMemoryManager.
	//inputs: memMgr - the simMemoryManager; used to obtain free frames for this process.
	//post-conditions:
	public void createPageTable(simMemoryManager memMgr)
	{
		pageTable = new simPageTable(this, memMgr, log);
	}

	//purpose: Process is being terminated, return all allocated frames back to free list.
	//assumptions: Called by simMemoryManager.
	//inputs: None.
	//post-conditions: Any frames allocated to this process now on free list.
	public void freeAllProcessFrames(simMemoryManagerFree memFree)
	{
		//Free all frames in pageTable.
		pageTable.freeAllProcessFrames(memFree);
		pageTable = null;
	}

	//purpose: Get frame number for the logical page number.
	//assumptions: None.
	//inputs: pageNumber - a logical page number.
	//post-conditions: Returns frame number that pageNumber is mapped to.
	public Integer getFrameNumber(int pageNumber)
	{
		return pageTable.getFrameNumber(pageNumber);
	}

	//get the frame number and page number of the lowest available page number
	public ArrayList<Integer> get_frame_and_page()
	{
		ArrayList<Integer> result =  pageTable.get_first_not_null_frame();
		/* Integer frame = result.get(0);
		Integer page = result.get(1); */
		return result;
	}

	//free all dynamically allocated frames
	public void free_all_dyno_frames(simMemoryManagerFree memFree)
	{
		dynamoMemory.free_dynamo_frames(memFree);
	}

	// method to execute MEMA instruction in Dynamo Memory
	public void execute_dynamo_MEMA (Integer frames_needed, simPCB executingPCB, simMemoryManagerFree memoryManagerFree, simVirtualMemory virtualMemory)
	{
		dynamoMemory.execute_MEMA(frames_needed, executingPCB, memoryManagerFree, virtualMemory);
	}

	// method to execute MEMF instruction in Dynamo Memory
	public Boolean execute_dynamo_MEMF (Integer frames_needed, simMemoryManagerFree memFree)
	{
 		return dynamoMemory.execute_MEMF(frames_needed, memFree);
	}



}
