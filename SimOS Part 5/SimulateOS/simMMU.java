package SimulateOS;

import Model.simLog;

import java.math.BigInteger;

public class simMMU
{
	//purpose: Translate CCC page#,offset into frame#,offset. Report page fault when one occurs.
	//assumptions: Method called first when instruction is fetched to be executed.
	//				All instructions call this method.
	//inputs: executingPCB - the PCB for the currently executing simulated process.
	//			instruction - the instruction that was just fetched.
	//			log - the simLog object.
	//post-conditions: For a CCC instruction: page#,offset translated to frame#,offset and reported via simLog.
	//					For a CCC instruction, a memory protection violation is reported to simLog when:
	//						(1) the page# and offset exceeds the logical address space for the simulated process.
	//					For a CCC instruction, a page fault is reported to simLog when:
	//						(2) The page# cannot be translated to a frame#.
	public static void MMU(simMemoryManager memMgr, simPCB executingPCB, simCPUInstruction instruction, simLog log)
	{
		if (executingPCB != null && instruction != null && instruction.getOpcode() == simCPUInstruction.OPCODE.CCC)
		{
			//Get page number and offset from instruction.
			BigInteger pageNbr = new BigInteger(Integer.toString(instruction.getOperand2()));
			BigInteger offset = new BigInteger(Integer.toString(instruction.getOperand3()));
			BigInteger imageSize = executingPCB.getImageSize();
			BigInteger pageSize = memMgr.getPageSize();
			//Compute logical address (pageNbr * pageSize + offset).
			BigInteger logicalAddress = pageNbr.multiply(pageSize);
			logicalAddress = logicalAddress.add(offset);
			if (logicalAddress.compareTo(imageSize) > 0)
				//The page#,offset is larger than imageSize
				//Display memory violation; trying to access memory beyond end of logical address space.
				log.println("simMMU.MMU: memory protection violation; logical address " +
						logicalAddress.toString() + " or (" +
						pageNbr.toString() + "," + offset.toString() +
						") exceeds size of process address space.");
			else
			{
				//get frame number using page number (new pcb method)
				Integer frameNbr = executingPCB.getFrameNumber(pageNbr.intValue());
				if (frameNbr == null)
					//Display page fault
					log.println("simMMU.MMU: page fault; page number " + pageNbr.toString() +
							" is NOT mapped to a physical frame.");
				else
					//Display page#,offset -> frame#,offset
					log.println("simMMU.MMU: logical address (" + pageNbr.toString() + "," + offset.toString() +
							") mapped to physical address (" + frameNbr.toString() + "," + offset.toString() + ").");
			}
			//Add code based on the method description above.
			//You should modify the println statements below based on the logic you add.
			//log.println("simMMU: translate logical address to physical address");
			//log.println("simMMU: report page fault if one would occur.");
		}
	}
}
