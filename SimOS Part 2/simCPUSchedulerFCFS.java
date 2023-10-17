package SimulateOS;

import Model.simLog;

public class simCPUSchedulerFCFS extends simCPUScheduler
{
	public simCPUSchedulerFCFS(simCPU cpu, simProcessManager procMgr, simInterrupt interrupts, simLog log)
	{
		super(cpu, procMgr, interrupts, log);
		log.println("simCPUSchedulerFCFS started");
	}

	//purpose: Determine if a different process should start executing.
	//assumptions: This method implements a FCFS scheduling algorithm.
	//inputs: timer - simTimer object.
	//post-conditions: the executingPCB has terminated and the simulation is ready to schedule another process.
	public void determineContextSwitchNeeded(simTimer timer)
	{
		if (executingPCB != null)
		{
			if (executingPCB.getInstructionPointer() >= executingPCB.getInstructionsSize())
			{
				//Process has no more instructions to execute; it has ended.
				procMgr.addTermQueue(executingPCB);

				//Add code here to update executingPCB statistics so that
				//totalExecuteTime and turnaroundTime are correct.

				//Update currently running process's total execution time
				executingPCB.addExecuteTime(timer.getClock());
				//Update turnaround time for this terminated process.
				int turnaroundTime = timer.getClock() - executingPCB.getArrivalTime();
				executingPCB.setTurnaroundTime(turnaroundTime);

				//End of added code for FCFS assignment

				//Force switch to next process in ready queue.
				executingPCB = null;
				cpu.clearInstruction();
			}
		}
	}

	//purpose: Update two statistics in the executingPCB.
	//assumptions: The executingPCB just started using the CPU.
	//inputs: timer - simTimer object.
	//post-conditions: executingPCB.totalWaitTime and executingPCB.startExecuteTime have been updated.
	public void updateProcessStatistics(simTimer timer)
	{
		//Add code here to update executingPCB statistics so that
		//totalExecuteTime and totalWaitTime are correct.

		//update total wait time for this process
		executingPCB.addWaitTime(timer.getClock());
		//Update time that process is starting to execute.
		executingPCB.setStartExecuteTime(timer.getClock());
	}
}
