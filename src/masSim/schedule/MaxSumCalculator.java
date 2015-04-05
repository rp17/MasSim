package masSim.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MaxSumCalculator {

	private String negotiatedTaskName = "";
	private int numberOfAgentsInNegotiation = 0;
	public Map<String,Integer> agentsIndex = new HashMap<String,Integer>();
	private class ScheduleQualities {public int agentVariableId; public int base; public int incremental;}
	private ArrayList<ScheduleQualities> scheduleQualities = new ArrayList<ScheduleQualities>();
	int agentsIdIndex = 0;
	
	public MaxSumCalculator(String negotiatedTaskName, int numberOfAgentsBeingNegotiatedWith)
	{
		this.negotiatedTaskName = negotiatedTaskName;
		this.numberOfAgentsInNegotiation = numberOfAgentsBeingNegotiatedWith;
	}
	
	public boolean IsDataCollectionComplete()
	{
		return scheduleQualities.size()==this.numberOfAgentsInNegotiation;
	}
	
	public void AddCostData(String agentName, int base, int increment)
	{
		if (!agentsIndex.containsKey(agentName))
		{
			ScheduleQualities s = new ScheduleQualities();
			s.agentVariableId = agentsIdIndex++;
			s.base = base;
			s.incremental = increment;
			scheduleQualities.add(s);	
		}
	}
	
	private void AddLine(StringBuilder b, String line)
	{
		b.append(line + System.lineSeparator());
	}
	
	private String GetVariablesBlock()
	{
		StringBuilder variables = new StringBuilder();
		for(ScheduleQualities ql : this.scheduleQualities)
		{
			//Number of agents (maxsum agent, not rover agents) is fixed, 
			//as well as variable states i.e. 2, assign task or not assign task
			AddLine(variables, "VARIABLE " + ql.agentVariableId + " 1 2");
		}
		return variables.toString();
	}
	
	
	private String GetFunctionsBlock()
	{
		StringBuilder f = new StringBuilder();
		int totalVariables = this.scheduleQualities.size();
		
		String c = "CONSTRAINT 0 1";
		for(int i = 0; i<totalVariables; i++)
		{
			c += " " + i;
		}
		AddLine(f, c);
		
		for(ScheduleQualities ql : this.scheduleQualities)
		{
			f.append("F ");
			for(int i = 0; i<totalVariables; i++)
			{
				f.append( (i==ql.agentVariableId) ? "1 " : "0 " );
			}
			f.append(ql.incremental);
		}
		return f.toString();
	}
	
	@Override
	public String toString()
	{
		StringBuilder cop = new StringBuilder();
		AddLine(cop,"AGENT 1");
		cop.append(GetVariablesBlock());
		cop.append(GetFunctionsBlock());
		return cop.toString();
	}
}
