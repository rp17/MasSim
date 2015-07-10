package masSim.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.AbstractMap.SimpleEntry;

public class MaxSumCalculator {

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
		//for(SimpleEntry<String,String> ent : result)
		//{
		//	if (ent.getValue().equals("1"))
		//	{
		//		selectedAgent = ent.getKey();
		//	}
		//}
		int maxImprovement = 0;
		ScheduleQualities selectedQuality = null;
		System.out.println("schedule qualities size " + this.scheduleQualities.size());
/*		for(ScheduleQualities ql : this.scheduleQualities)
		{
			int improvement = ql.incremental - ql.base;
			if (improvement > maxImprovement)
			{
				maxImprovement = improvement;
				selectedQuality = ql;
			}
		}*/
		Iterator iterQual = this.scheduleQualities.iterator();
		if(iterQual.hasNext()) {
			ScheduleQualities q1 = (ScheduleQualities) iterQual.next();
			selectedQuality = q1;
			int maxVal = q1.incremental - q1.base;
			while(iterQual.hasNext()) {
				ScheduleQualities q2 = (ScheduleQualities) iterQual.next();
				int val = q2.incremental - q2.base;
				if(val > maxVal) {
					maxVal = val;
					selectedQuality = q2;
				}
				System.out.println("maxVal = " + maxVal);
			}
		} else {
			System.out.println("ERROR : ScheduleQualities size is 0");
		}

		if(selectedQuality == null) {
			System.out.println("selectedQuality is null");
		} else {
		System.out.println("variable is " + selectedQuality.agentVariableId);
		}
		System.out.println("Agent " + selectedQuality.agentVariableId + " was selected to complete the dynamic task");
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
