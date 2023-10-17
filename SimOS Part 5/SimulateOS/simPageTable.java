package SimulateOS;

import Model.simLog;

import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;

public class simPageTable
{
	private simPCB pcb;
	private simMemoryManager memMgr;
	private simLog log;

	private ArrayList<Integer> pageTable;

	public simPageTable(simPCB pcb, simMemoryManager memMgr, simLog log)
	{
		this.memMgr = memMgr;
		this.log = log;
		this.pcb = pcb;

		BigInteger pageSize = memMgr.getPageSize();
		BigInteger nbrProcessPages;
		BigInteger[] divideRemainder;
		int numberProcessPages;

		divideRemainder = this.pcb.getImageSize().divideAndRemainder(pageSize);
		if (divideRemainder[1].compareTo(BigInteger.ZERO) == 1)
			nbrProcessPages = divideRemainder[0].add(BigInteger.ONE);
		else
			nbrProcessPages = divideRemainder[0];

		try
		{
			numberProcessPages = nbrProcessPages.intValueExact();
		}
		catch (Exception ex)
		{
			numberProcessPages = Integer.MAX_VALUE;
		}
		pageTable = new ArrayList<Integer>(numberProcessPages);

		log.println("simPageTable.constructor: created page table with capacity of " + numberProcessPages);

		mapPagesToFrames(memMgr, nbrProcessPages);
		log.println("simPageTable.constructor: number of free frames (after mapping process pages to frames)=" + memMgr.getNumberOfFreeFrames());
	}

	//purpose: Process is being terminated, return all dynamically allocated frames back to free list.
	//assumptions: Called by simPCB.
	//inputs: None.
	//post-conditions: Any frames dynamically allocated to this process now on free list.
	public void freeAllProcessFrames(simMemoryManagerFree memFree)
	{
		Integer frameNbr;
		log.println("simPageTable.freeAllProcessFrames: number of frames in page table=" + pageTable.size());
		for (int pageNbr=0; pageNbr < pageTable.size(); pageNbr++)
		{
			frameNbr = pageTable.get(pageNbr);
			memFree.addFreeFrame(frameNbr);
		}
	}

	//purpose: Obtain free frames for each logical page associated with this process.
	//assumptions: 
	//inputs: memMgr - the simMemoryManager; used to obtain free frames for this process.
	//	nbrProcessPages - the number of pages to be mapped to frames.
	//post-conditions:
	private void mapPagesToFrames(simMemoryManager memMgr, BigInteger nbrProcessPages)
	{
		//log.println("simPageTable.mapPagesToFrames: mapping pages 0..." + nbrProcessPages.toString() + " to frames");
		BigInteger counter = BigInteger.ZERO;
		Integer firstFrameNumber = null;
		Integer lastFrameNumber = null;
		while (counter.compareTo(nbrProcessPages) == -1)
		{
			Integer frameNumber = memMgr.getOneFreeFrameNumber();
			if (frameNumber != null)
			{
				pageTable.add(frameNumber);
				counter = counter.add(BigInteger.ONE);
				//Save first and last frame number used in mapping pages.
				if (firstFrameNumber == null)
					firstFrameNumber = frameNumber;
				lastFrameNumber = frameNumber;
			}
			else
         {
				//No more free frames, stop mapping pages to frames.
				counter = nbrProcessPages;
   			log.println("simPageTable.mapPagesToFrames: could NOT map all logical pages to physical frames; ran out of free frames.");
         }
		}
		//Subtract 1 to accurately display largest page number.
		BigInteger temp = nbrProcessPages.subtract(BigInteger.ONE);
		if (firstFrameNumber == null)
			log.println("simPageTable.mapPagesToFrames: mapping pages 0..." +
					temp.toString() + " to zero frames (no frames are free).");
		else
			log.println("simPageTable.mapPagesToFrames: mapping pages 0..." +
					temp.toString() + " to frames " +
					firstFrameNumber.toString() + "..." + lastFrameNumber.toString());
	}

	//purpose: Get frame number for the logical page number.
	//assumptions: None.
	//inputs: pageNumber - a logical page number.
	//post-conditions: Returns frame number that pageNumber is mapped to.
	//					Returns null (to indicate page fault) when pageNumber is larger than size of page table.
	public Integer getFrameNumber(int pageNumber)
	{
		Integer frameNumber = null;
		if (pageNumber < pageTable.size())
			frameNumber = Integer.valueOf(pageTable.get(pageNumber));
		
		return frameNumber;
	}

	public ArrayList<Integer> get_first_not_null_frame()
	{
		Integer frame = 0;
		Integer page = 0;
		ArrayList<Integer> frame_and_page = new ArrayList<>(2);
		for (Integer i = 0; i< this.pageTable.size(); i++)
		{
			//find smallest, not null page (not null = has a frame in it)
			if (this.pageTable.get(i) != null)
			{
				frame = this.pageTable.get(i); // retrieve and assign the frame number
				page = i; // assign the page number
				this.pageTable.set(i, null); // set the mapped frame to null for that specific page
				frame_and_page.add(frame); // add our frame to the returned value
				frame_and_page.add(page); // add our page to the returned value
				break; // break out of our loop
				
			}
			
		}
		return frame_and_page; // return our frame and page for processing via virtual memory
	}
}
