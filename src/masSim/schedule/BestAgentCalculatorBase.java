package masSim.schedule;

import java.util.ArrayList;
import java.util.List;

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
	
	public BestAgentCalculatorBase(BestAgentCalculatorBase calc)
	{
		this(calc.instanceName, calc.numberOfAgentsInNegotiation, calc.numberOfTasksInNegotiation);
		this.agentScheduleQualities = calc.agentScheduleQualities;
	}
	
	public List<List<Integer>> GetBestAgent()
	{
		return null;
	}
	
	public boolean IsDataCollectionComplete()
	{
		return agentScheduleQualities.size()==this.numberOfAgentsInNegotiation;
	}
	
	public String ToString(List<List<Integer>> allAgentAssignments)
	{
		String str = "";
		for(List<Integer> assignment : allAgentAssignments)
		{
			//if (assignment.size()!=1)
			{
				for(int i=1;i<assignment.size();i++)
				{
					str += " A" + assignment.get(0) + "[" + assignment.get(i) + ",]";
				}
			}
		}
		return str.trim();
	}
	
	public void AddCostData(AgentScheduleQualities aql)
	{
		this.agentScheduleQualities.add(aql);
	}
	
}
