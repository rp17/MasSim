package masSim.schedule;

import org.sat4j.specs.ILogAble;

public class PlainCalculator extends BestAgentCalculatorBase implements ILogAble {
	private boolean debugFlag = false;
	final int AGENTID = 0;
	final int TASKID = 1;
	private String log = "";
	
	public PlainCalculator(String instanceName, int numberOfAgents, int numberOfTasks)
	{
		super(instanceName, numberOfAgents, numberOfTasks);
	}
	
	public void GetBaseAgent(){}

	@Override
	public void log(String message) {
		// TODO Auto-generated method stub
		
	}

}
