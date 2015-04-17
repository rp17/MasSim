package masSim.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

import raven.Main;

public class MaxSumCalculator {

	private boolean debugFlag = true;
	private String negotiatedTaskName = "";
	private int numberOfAgentsInNegotiation = 0;
	public Map<Integer,String> agentsIndex = new HashMap<Integer,String>();
	private class ScheduleQualities {public int agentVariableId; public int base; public int incremental;}
	private ArrayList<ScheduleQualities> scheduleQualities = new ArrayList<ScheduleQualities>();
	int agentsIdIndex = 0;
	
	public MaxSumCalculator(String negotiatedTaskName, int numberOfAgentsBeingNegotiatedWith)
	{
		this.negotiatedTaskName = negotiatedTaskName;
		this.numberOfAgentsInNegotiation = numberOfAgentsBeingNegotiatedWith;
	}
	
	public String GetBestAgent()
	{
		String selectedAgent = "";
		//Commenting out maxsum calculation, to do a manual one for now.
		//test.Main jmaxMain = new test.Main();
		//ArrayList<SimpleEntry<String,String>> result = jmaxMain.CalculateMaxSumAssignments(calc.toString());
		//for(SimpleEntry<String,String> ent : result)
		//{
		//	if (ent.getValue().equals("1"))
		//	{
		//		selectedAgent = ent.getKey();
		//	}
		//}
		int maxImprovement = -9999999;
		ScheduleQualities selectedQuality = null;
		boolean compareIdleAgentsOnly = false;
		for(ScheduleQualities ql : this.scheduleQualities)
		{
			if (!compareIdleAgentsOnly && ql.base==0)
			{
				//Found an idle agent, so this agent must be given preference over non idle ones
				compareIdleAgentsOnly = true;
			}
			int improvement = ql.incremental - ql.base;
			Main.Message(debugFlag, "Quality Improvement for " + this.negotiatedTaskName + " with " + this.agentsIndex.get(ql.agentVariableId) + " is " + improvement);
			if (improvement > maxImprovement)
			{
				if (compareIdleAgentsOnly)
				{	
					if (ql.base==0)
					{
						maxImprovement = improvement;
						selectedQuality = ql;	
					}
				}
				else
				{
					maxImprovement = improvement;
					selectedQuality = ql;
				}
			}
		}
		return this.agentsIndex.get(selectedQuality.agentVariableId);
	}
	
	public String getTaskName()
	{
		return this.negotiatedTaskName;
	}
	
	public boolean IsDataCollectionComplete()
	{
		return scheduleQualities.size()==this.numberOfAgentsInNegotiation;
	}
	
	public void AddCostData(String agentName, int base, int increment)
	{
		if (!agentsIndex.containsValue(agentName))
		{
			ScheduleQualities s = new ScheduleQualities();
			s.agentVariableId = agentsIdIndex++;
			s.base = base;
			s.incremental = increment;
			scheduleQualities.add(s);
			this.agentsIndex.put(s.agentVariableId, agentName);
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
