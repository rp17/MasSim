package masSim.schedule;

import java.util.ArrayList;

public class BestAgentCalculatorBase {
	protected boolean debugFlag = true;
	protected String instanceName = "";
	protected int numberOfAgentsInNegotiation = 0;
	protected int numberOfTasksInNegotiation = 0;
	protected ArrayList<AgentScheduleQualities> agentScheduleQualities = new ArrayList<AgentScheduleQualities>();
	
	public BestAgentCalculatorBase(String instanceName, int numberOfAgentsBeingNegotiatedWith, int numberOfTasksInNegotiation)
	{
		this.instanceName = instanceName;
		this.numberOfAgentsInNegotiation = numberOfAgentsBeingNegotiatedWith;
		this.numberOfTasksInNegotiation = numberOfTasksInNegotiation;
	}
	
	public boolean IsDataCollectionComplete()
	{
		return agentScheduleQualities.size()==this.numberOfAgentsInNegotiation;
	}
	
	public void AddCostData(AgentScheduleQualities aql)
	{
		this.agentScheduleQualities.add(aql);
	}
	
}
