package SimulateOS;


import Model.scenario;
import Model.scenarioFile;
import Model.simLog;


public class simCPUSchedulerRR extends simCPUScheduler
{
	private scenario scen; // 
	private int current_quantum;

	public simCPUSchedulerRR(scenario scen, simCPU cpu, simProcessManager procMgr, simInterrupt interrupts, simLog log)
	{
		super(cpu, procMgr, interrupts, log); // DV code
		// initialize our quantum counter 
		current_quantum = scen.getSchedulerQuantum();
		this.scen = scen; // initialize our scenario object
		log.println("simCPUSchedulerRR started"); // DV code
		
	}

	//purpose: return true when the executingPCB should be preempted.
	//assumptions: None.
	//inputs: None.
	//post-conditions: returns true if it is time to preempt the executing PCB.
	public boolean preemptExecutingCPU()
	{
		//Add code to this method, based on the method comments
		current_quantum = current_quantum - 1; // tick our quantum down by one to simulate a clock tick
		boolean preempt = false;
		if(current_quantum == 0) // check if quantum has ticked down to 0 (time slice has been used up by our process)
		{
			preempt = true;
			log.println("We Preempted!!!!!");
		}
		return preempt; 
	}

	//purpose: Update scheduler statistics for this preempted PCB.
	//assumptions: 
	//inputs: executingPCB - the PCB being preempted.
	//	timer - the simTimer object.
	//post-conditions: PCB.totalExecuteTime updated, PCB.startWaitTime updated.
	public void updatePreemptedPCBstatistics(simPCB executingPCB, simTimer timer)
	{
		//Add code to this method, based on the method comments
		executingPCB.addExecuteTime(timer.getClock()); // add execution time that has occurred since the start of PCB's execution
		executingPCB.setStartWaitTime(timer.getClock()); // begin the wait time again for the preempted PCB
	}

	//purpose: Update two statistics in the PCB which is executing.
	//assumptions: Executing PCB just started using the CPU.
	//inputs: timer - simTimer object.
	//post-conditions: PCB.totalWaitTime updated, PCB.startExecuteTime updated, and
	//			quantum value reset for PCB just starting to execute.
	public void updateStartingPCBstatistics(simPCB executingPCB, simTimer timer)
	{
		//Add code to this method, based on the method comments
		executingPCB.addWaitTime(timer.getClock()); // add the wait time to the total wait time for the starting PCB
		executingPCB.setStartExecuteTime(timer.getClock()); // executing time for this PCB is started
		current_quantum = scen.getSchedulerQuantum(); // reset the quantum to its initial value
		
	}

	//purpose: Update two statistics in the PCB which is executing.
	//assumptions: Executing PCB just started using the CPU.
	//inputs: timer - simTimer object.
	//post-conditions: PCB.totalExecuteTime updated, PCB.turnaroundTime updated.
	public void updateTerminatedPCBstatistics(simPCB executingPCB, simTimer timer)
	{
		//Add code to this method, based on the method comments
		executingPCB.addExecuteTime(timer.getClock()); // add our execution time to the total execution time
		executingPCB.setTurnaroundTime(executingPCB.getWaitTime() + executingPCB.getExecuteTime()); // compute turnaround time
		
		
	}
}
