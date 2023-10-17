package SimulateOS;


import Model.scenario;
import Model.simLog;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;
import java.util.HashMap;

public class simVirtualMemory 
{
    private simLog log;
    private HashMap<String, LinkedList<Integer>> allocated_pages; //hashmap to hold entries for our pages (pages where we stole the frame)


    public simVirtualMemory(simLog log)
    {
        this.log = log;
        this.allocated_pages = new HashMap<String, LinkedList<Integer>>();
    }


    // use the PCB thats passed in to steal a frame from the page table
    public Integer steal_frame(simPCB executingPCB)
    {
        ArrayList<Integer> result =  executingPCB.get_frame_and_page(); //retrieve frame number and page number
        Integer frame = result.get(0); // assign frame number 
        Integer page = result.get(1); //assign page number

        //keep track of pages we are "swapping" out of memory to simulate the swap file
        if (allocated_pages.containsKey(executingPCB.getId().toString()))// the process already exists in our hashmap
        {
            allocated_pages.get(executingPCB.getId().toString()).add(page); // we can just add the page to the key's value (list of pages)
        }
        else // the process has not been added to the table and needs to be added as an entry to our hashmap
        {
            allocated_pages.put(executingPCB.getId().toString(), new LinkedList<Integer>()); // add the new entry to the hashmap
            allocated_pages.get(executingPCB.getId().toString()).add(page); // add our value to the key's list of values (Pages)
        }
        return frame; // give the frame to the dynamic memory as requested
    }

    // remove the stored pages on our "disk" to be removed completely when process terminates
    public void remove_pages(simPCB pcb)
    {
        log.println("simVirtualMemory: Remove all virtual pages for process " + pcb.getId().toString() + " from disk.");
        try
        {
            allocated_pages.remove(pcb.getId().toString()); // remove our entry for the specific process from our hashmap
        }
        catch (Exception ex)
        {
            log.println("SimVirtualMemory: No virtual pages exist for this process!");
        }
       
        log.println("simVirtualMemory: Removed all virtual pages for process " + pcb.getId().toString() + " from disk.");
    }
}
