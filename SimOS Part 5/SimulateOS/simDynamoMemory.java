package SimulateOS;

import Model.simLog;

import java.text.Normalizer;
import java.util.*;

public class simDynamoMemory 
{

    private simLog log;
    private LinkedList<Integer> dyno_frames;

    public simDynamoMemory(simLog log)
    {
        this.log = log;
        dyno_frames = new LinkedList<Integer>();
    }


    //add our dynamically allocated frame to our 
    public void addFreeFrame(Integer frameNumber)
	{
		dyno_frames.add(frameNumber);
	}


    public Integer return_dyno_frame()
    {
        Integer frame = -1;
        if (dyno_frames.size() != 0)
        {
          frame =  dyno_frames.removeLast();
        }
       
        return frame;
    }

    public void free_dynamo_frames(simMemoryManagerFree memFree)
    {
        Integer counter = 0;
        log.println("simDynamoMemory: Free all dynamically allocated frames. Freeing " + dyno_frames.size() + " frames.");
        while (dyno_frames.size() != 0)
        {
            memFree.addFreeFrame(dyno_frames.removeLast());
            counter++;
        }
        log.println("simDynamoMemory: All dynamically allocated frames freed. We freed " + counter.toString() + " frames.");
    }

    public void execute_MEMA (Integer frames_needed, simPCB executingPCB, simMemoryManagerFree memFree, simVirtualMemory virtualMemory)
	{
		int virtual_mem = 0;
		int no_virtual = 0;
		while (frames_needed != 0)
		{
			//check if there are any frames that are free before we start stealing frames
			if(memFree.getNumberOfFreeFrames() != 0) // there are free frames still --> no need for virtual memory
			{
				if (no_virtual == 0)
				{
					log.println("simDynamoMemory: Using Free Memory");
					no_virtual = 1;
				}
				Integer frame_to_allocate = memFree.getOneFreeFrameNumber(); // retrieve a free frame
				this.addFreeFrame(frame_to_allocate); // allocate that free frame dynamically
				frames_needed--; // iterate down by one
			}

			else
			{
				if(virtual_mem == 0)
				{
					log.println("simDynamoMemory: Using Virtual Memory");
					virtual_mem = 1;
				}
				Integer frame_to_allocate = virtualMemory.steal_frame(executingPCB); // take frame from a page table
				this.addFreeFrame(frame_to_allocate); //give that frame to our process's dynamic memory
				frames_needed--; //iterate down by one
			}
		}
	}

    public Boolean execute_MEMF(Integer frames_needed,simMemoryManagerFree memFree)
    {
		Integer start_frames = frames_needed;
		Boolean complete = true;
		log.println("simDynamoMemory: We need to dynamically free " + frames_needed.toString() + " frames.");
		while (frames_needed != 0)
		{
			Integer returned_frame =  this.return_dyno_frame();
			if (returned_frame == -1)
			{
				log.println("simDynamoMemory: We have no dynamic frames left to free! Were any allocated initially?");
				log.println("simDynamoMemory: We managed to free " + (start_frames - frames_needed) + " frames before stopping.");
				break;
			}
			else
			{
				memFree.addFreeFrame(returned_frame);
				frames_needed--;
			}
		}
		if (frames_needed != start_frames) // some frames were allocated
		{
			log.println("simDynamoMemory: We dynamically freed " + (start_frames - frames_needed) + " frames. Frames have been returned to free frame list.");
		}	
		else
		{
			complete = false;
		}
		
		return complete;
    }
}
