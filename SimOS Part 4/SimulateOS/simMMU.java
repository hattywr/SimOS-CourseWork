package SimulateOS;

import java.math.BigInteger;

import Model.simLog;

public class simMMU
{
	//purpose: Translate CCC page#,offset into frame#,offset. Report page fault when one occurs.
	//assumptions: Method called first when instruction is fetched to be executed.
	//				All instructions call this method.
	//inputs: executingPCB - the PCB for the currently executing simulated process.
	//			instruction - the instruction that was just fetched.
	//			log - the simLog object.
	//post-conditions: For a CCC instruction: page#,offset translated to frame#,offset and reported via simLog.
	//					For a CCC instruction, page fault is reported to simLog when:
	//						(1) The page# cannot be translated to a frame#.
	//						(2) the page# and offset exceeds the logical address space for the simulated process.
	public static void MMU(simMemoryManager memMgr, simPCB executingPCB, simCPUInstruction instruction, simLog log)
	{
		if (executingPCB != null && instruction != null && instruction.getOpcode() == simCPUInstruction.OPCODE.CCC)
		{
			//Add code based on the method description above.
			//You should modify the println statements below based on the logic you add.
			log.println("simMMU: translate logical address to physical address");

			System.out.println("Our current page to access is " + instruction.getOperand2() + ", " + instruction.getOperand3());

			Integer page_number = instruction.getOperand2();
			int offset = instruction.getOperand3();

			//BigInteger yes =  memMgr.pageSize;

			try
			{
				Integer frame = executingPCB.PageTable.get(page_number);
				if(frame == null)
				{
					log.println("Do we ever hit this?");
				}
			 	else
				{
					log.println("WE FOUND OUR ADDRESS: Logical Address = " + page_number.toString() + " Physical Address =  " + frame.toString());;
				}
			}
			 
			catch (Exception ex)
			{
				log.println("simMMU: PAGE FAULT!!!!!!");
			}
			 


			//log.println("simMMU: report page fault if one would occur.");
		}
	}
}
