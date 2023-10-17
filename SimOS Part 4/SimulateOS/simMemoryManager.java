package SimulateOS;

import Model.scenario;
import Model.simLog;
import java.util.ArrayList;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.math.BigDecimal; 

public class simMemoryManager
{
	private BigInteger RAM;
	public BigInteger pageSize;
	private BigInteger osSize;
	private BigInteger physical_frames;
	private BigInteger os_frames_needed;
	private simLog log;
    private simProcessManager procMgr;
	private ArrayList<Integer>phys_frames_array;
	private int current_free_frame = 0;
	private ArrayList<String> pcb_values;
	
	public simMemoryManager(scenario scen, simInterrupt interrupts, simLog log)
	{
		this.RAM = scen.getMemoryRAM();
		this.pageSize = scen.getMemoryPageSize();
		this.osSize = scen.getMemoryOSsize();
		interrupts.registerInterruptServiceRoutine(simInterrupt.INTERRUPT.MEM_MGR_INSTR, this);
		this.log = log;
		this.pcb_values = new ArrayList<>();

		log.println("simMemoryManager.constructor: free physical frames; allocate OS space.");
		physical_frames = (this.RAM.divide(pageSize)); // find our count of physical frames
		this.phys_frames_array = new ArrayList<Integer>(); // create an array to keep track of free frames
		create_physical_frame_list(physical_frames.intValue()); // fill our Arraylist of free frames
		allocate_space_main(); // method to allocate space for our OS

	}

	// Find the number of frames needed for the OS and allocate them
	private void allocate_space_main()
	{
		compute_os_frames_needed(); // find our number of OS frames we need to reserve
		System.out.println("The OS needs " + this.os_frames_needed + " frames to be reserved!");
		this.pcb_values.add("-1");
		allocate_OS_frames(); // "cordon" off the frames in our physical frame list for the OS
		
	}

	//Fill the array to house our physical frames
	private void create_physical_frame_list(Integer physical_frame_count)
	{
		for (int i = 0; i<physical_frame_count; i++) // for as many physical frames as we have
		{
			this.phys_frames_array.add(i); //fill our arraylist with those values at their index
		}
		System.out.println("Our arraylist has " + phys_frames_array.size() + " frames in it at the end");
	}

	// compute the number of frames needed for the OS
	private void compute_os_frames_needed()
	{
		if ((this.osSize.divide(this.pageSize)).compareTo(BigInteger.ONE) == -1) //OS Size is very small --> only needs one frame
		{
			this.os_frames_needed = BigInteger.ONE;
		}
		else // OS needs more than one frame
		{
			BigDecimal os_size_decimal =  new BigDecimal(this.osSize); // convert Big Int to big decimal
			BigDecimal page_size_decminal = new BigDecimal(this.pageSize); // convert Big Int to big decimal
			BigDecimal result = (os_size_decimal.divide(page_size_decminal)); // get our result of frames we need
			this.os_frames_needed = result.setScale(0, RoundingMode.CEILING).toBigInteger(); // always round up (will fragment but meh) 
		}
	}

	// Reserve Frames needed by the Operating System
	private void allocate_OS_frames()
	{
		for (int i = 0; i<this.os_frames_needed.intValue(); i++)
		{
				phys_frames_array.set(i,-1); // set each frame taken by the OS to -1 so that no other process uses it.
				current_free_frame = current_free_frame + 1;
		}
		System.out.println("We are currently on frame " + current_free_frame); //used to be "current_frame"
		System.out.println("The OS was Allocated " + (this.os_frames_needed.intValue()) + " frames. They are unavailable for use by any process.");
		System.out.println("We have " + get_free_frame_count() + " frames free for use");
	}

	//find the count of our free frames
	public Integer get_free_frame_count()
	{
		int counter = 0; // initialize counter
		for (int i = 0; i<phys_frames_array.size(); i++)
		{
			if (this.pcb_values.contains(phys_frames_array.get(i).toString())) // if our index value is not the same as the value at that index --> it is in use
			{
				continue;
			}
			else
			{
				counter++; // this is a free frame --> add it to our counter
			}
		}
		return counter; //return our free frame count
	}

	//purpose: Map pages to frames as part of process creation.
	//assumptions: None.
	//inputs: pcb - the simPCB for the process requesting creation of its process space.
	//post-conditions: Logical pages for pcb has been mapped to physical frames.
	public void createProcessMemorySpace(simPCB pcb)
	{
		log.println("simMemoryManager.createProcessMemorySpace: create page table; map pages to frames."); //Start our mapping process
		Integer pcbNumber = Integer.parseInt(pcb.getId().replaceAll("[^0-9]", "")); // get our pcb number as an INT
		this.pcb_values.add(pcbNumber.toString()); //add our pcb number to our list of values in use
	
		BigDecimal free_frames = new BigDecimal(get_free_frame_count().toString()); //find our free frames
		BigDecimal page_size = new BigDecimal(this.pageSize); //get our page size
		BigDecimal image_size = new BigDecimal(pcb.getImageSize()); // get our image size


		BigDecimal free_frames_to_map = image_size.divide(page_size).setScale(0,RoundingMode.CEILING); // get the number of free frames we need
		System.out.println("Our image size is " + pcb.getImageSize());
		System.out.println("We have " + free_frames_to_map + " pages to map");

		if (free_frames.compareTo(free_frames_to_map) == -1) // we have less free frames than the amount of frames requested
		{
			log.println("Houston, we have a problem, we dont have enough space for this process");
			System.out.println("We can only map " + free_frames + " of the "+ free_frames_to_map + " requested");
			free_frames_to_map = free_frames;
			System.out.println("We are going to map " + free_frames_to_map + " frames for PCB " + pcb.getId());
			fill_page_table(pcb, free_frames_to_map.toBigIntegerExact()); // fill our page table
		}
		else //we have enough free frames to map all frames requested
		{
			log.println("YAYAYAYAY we have more than enough frames to handle all these pages");
			System.out.println("We are going to map " + free_frames_to_map + " frames for our process.");
			fill_page_table(pcb, free_frames_to_map.toBigIntegerExact()); // fill our page table
		}
		
	}

	// fill / map values in our page table
	public void fill_page_table(simPCB pcb, BigInteger frame_to_map)
	{
		current_free_frame = find_first_free_frame();
		for (BigInteger i = BigInteger.ZERO; i.compareTo(frame_to_map)< 0; i = i.add(BigInteger.ONE))
		{
			int entry = current_free_frame; // find our current free frame
			pcb.PageTable.add(entry);
			int pcbNumber = Integer.parseInt(pcb.getId().replaceAll("[^0-9]", ""));
			this.phys_frames_array.set(entry, pcbNumber); // set all frames associated with the PCB number to that number
			current_free_frame = current_free_frame + 1;
		}
		System.out.println("We now have " + pcb.PageTable.size() + " entries mapped");
		System.out.println("We now have " + get_free_frame_count() + " frames free");
	}

	public int find_first_free_frame()
	{
		int result = 0;
		for (int i = 0; i< this.phys_frames_array.size(); i++)
		{
			if (this.pcb_values.contains(phys_frames_array.get(i).toString())) // if frames are the same value as any value in our list of used values... they are in use
			{
				continue;
			}
			else // frame is free
			{
				return i; //return the frame
			}
		}
		return result;
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
            if (instruction.getOpcode() == simCPUInstruction.OPCODE.MEMA)
            {
      			log.println("simMemoryManager.interruptServiceRoutine: dynamically allocate memory to the executing process.");
            }
            else if (instruction.getOpcode() == simCPUInstruction.OPCODE.MEMF)
            {
      			log.println("simMemoryManager.interruptServiceRoutine: dynamically free memory from the executing process.");
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

	//purpose: Process is being terminated, return all frames used by this process back to free list.
	//assumptions: Called by process manager when process is being terminated.
	//inputs: None.
	//post-conditions: Any frames allocated to this process now on free list.
	public void terminateProcess(simPCB pcb)
	{
		Integer value_to_check = Integer.parseInt(pcb.getId().replaceAll("[^0-9]", ""));
		for (int i = 0; i<this.phys_frames_array.size(); i++)
		{
			//Integer value = phys_frames_array.get(i);
			
			if (this.pcb_values.contains(value_to_check.toString()) && phys_frames_array.get(i) == value_to_check )
			{
				this.phys_frames_array.set(i,i); //re-adding our frames to the memory monster
			}			
		}
		Integer pcbNumber = Integer.parseInt(pcb.getId().replaceAll("[^0-9]", ""));
		this.pcb_values.remove(pcbNumber.toString());
		this.current_free_frame = find_first_free_frame();
		log.println("simMemoryManager.terminateProcess: release all memory used by this process");
	}

	
}
