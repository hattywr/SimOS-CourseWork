package SimulateOS;

import Model.scenario;
import Model.simLog;

import java.math.BigInteger;

public class simMemoryManager
{
	private BigInteger RAM;
	private BigInteger pageSize;
	private BigInteger osSize;
	private simLog log;
    private simProcessManager procMgr;

	//added as part of solution to memory manager Part 01.
	private simMemoryManagerFree memFree;
	
	//added as part of solution to Memory Manager Part 02
	private simVirtualMemory virtualMemory;

	public simMemoryManager(scenario scen, simInterrupt interrupts, simLog log)
	{
		this.RAM = scen.getMemoryRAM();
		this.pageSize = scen.getMemoryPageSize();
		this.osSize = scen.getMemoryOSsize();
		interrupts.registerInterruptServiceRoutine(simInterrupt.INTERRUPT.MEM_MGR_INSTR, this);
		this.log = log;
		log.println("simMemoryManager.constructor: free physical frames; allocate OS space.");
		//added as part of solution to memory manager Part 01.
		// Will Part 2
		virtualMemory = new simVirtualMemory(log);
		// add virtual memory to memfree
		memFree = new simMemoryManagerFree(this.RAM, this.pageSize, this.osSize, this.log, this.virtualMemory);
	}

	//purpose: Map pages to frames as part of process creation.
	//assumptions: None.
	//inputs: pcb - the simPCB for the process requesting creation of its process space.
	//post-conditions: Logical pages for pcb have been mapped to physical frames.
   public void createProcessMemorySpace(simPCB pcb)
   {
      //added as part of solution to memory manager Part 01.
      pcb.createPageTable(this);		
   }

	//purpose: Allocate or free memory.
	//assumptions: None.
	//inputs:
	//	MEM_MGR_INSTR: data is a cpuInstruction object.
	//post-conditions:
   // When cpuInstruction is MEMA: frames have been dynamically allocated to the executingPCB.
	// When cpuInstruction is MEMF: frames have been dynamically freed for the executingPCB.
	public void interruptServiceRoutine(Object data)
	{
		if (data instanceof simCPUInstruction)
		{
			//Interrupt is from cpu.
			//A memory instruction needs to be executed by the memory manager.
			simCPUInstruction instruction = (simCPUInstruction)data;
			//Get the executing PCB
			simPCB executingPCB = this.procMgr.getExecutingPCB();
			if (executingPCB == null)
				log.println("simMemoryManager.interruptServiceRoutine (CPU instruction): " +
							"logic error - executingPCB should not be null!");
			else
         {
   			log.println("simMemoryManager.interruptServiceRoutine: " + instruction +
   				" pcbNumber=" + executingPCB.getNumber());

			// add a line to get the number of frames that need to be allocated or freed
			Integer frames_needed = instruction.getOperand();

            if (instruction.getOpcode() == simCPUInstruction.OPCODE.MEMA)
            {
      			log.println("simMemoryManager.interruptServiceRoutine: dynamically allocate memory to the executing process.");
				log.println("simMemoryManager: Allocating " + frames_needed.toString() + " frames to PCB " + executingPCB.getId().toString()); 
				
				executingPCB.execute_dynamo_MEMA(frames_needed, executingPCB, memFree, virtualMemory);
				log.println("SimMemoryManager: " + frames_needed.toString() + " frames allocated to PCB " + executingPCB.getId().toString());
			
            }
            else if (instruction.getOpcode() == simCPUInstruction.OPCODE.MEMF)
            {
      			log.println("simMemoryManager.interruptServiceRoutine: dynamically free memory from the executing process.");
				log.println("simMemoryManager: Freeing " + frames_needed.toString() + " frames from PCB " + executingPCB.getId().toString());

				Boolean complete = executingPCB.execute_dynamo_MEMF(frames_needed, memFree);
				if (complete == true)
				{
					log.println("simMemoryManager: Freed frames for " + executingPCB.getId().toString());
				}
				else
				{
					log.println("simMemoryManager: No frames could be freed, since none were allocated." );
				}
				

            }
            else
            {
      			log.println("simMemoryManager.interruptServiceRoutine: invalid instruction received; ignoring.");
            }
         }
		}
		else
			log.println("simMemoryManager.interruptServiceRoutine unknown data");
	}

	//purpose: Allow the memory manager to call methods in the process manager.
	//assumptions: Called by simOS after the memory and process managers have been created.
	//inputs: None.
	//post-conditions: this memory manager can know call public process manager methods.
   public void setProcessManager(simProcessManager procMgr)
   {
      this.procMgr = procMgr;
   }

	//purpose: Process is being terminated, return all allocated frames back to free list.
	//assumptions: Called by process manager when process is being terminated.
	//inputs: None.
	//post-conditions: Any frames allocated to this process now on free list.
   public void terminateProcess(simPCB pcb)
   {
   	//added as part of solution to memory manager Part 01.
		log.println("simMemoryManager.terminateProcess: releasing all memory used by this process.\n\tNumber of free frames (before)=" + getNumberOfFreeFrames());
		pcb.freeAllProcessFrames(memFree);
		log.println("simMemoryManager.terminateProcess: number of free frames (after)=" + getNumberOfFreeFrames());

		log.println("simMemoryManager: Need to free any dynamic frames!");
		pcb.free_all_dyno_frames(memFree);
		log.println("simMemoryManager: Clear swap file for process " + pcb.getId().toString() + " and remove from disk");
		virtualMemory.remove_pages(pcb);
   }

/*  ADDED as part of solution to memory manager assignment */

	//purpose: Get one free frame number.
	//assumptions: memFree object exists; called by simPCB.
	//inputs: None.
	//post-conditions: Returns a free frame number.
	public Integer getOneFreeFrameNumber()
	{
		return memFree.getOneFreeFrameNumber();
	}

	//purpose: Return number of free frames.
	//assumptions: called by simPCB (multiple times).
	//inputs: None.
	//post-conditions: Return number of free frames.
	public int getNumberOfFreeFrames()
	{
		return memFree.getNumberOfFreeFrames();
	}

	//purpose: Return simulation page size.
	//assumptions: called by simPCB.
	//inputs: None.
	//post-conditions: Return simulation page size.
	public BigInteger getPageSize()
	{
		return pageSize;
	}
}
		

