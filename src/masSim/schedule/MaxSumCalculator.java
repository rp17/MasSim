package masSim.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
	
	public String GetBestAgentFromMaxSum()
	{
		String selectedAgent = "";
		//Commenting out maxsum calculation, to do a manual one for now.
		test.Main jmaxMain = new test.Main();
		ArrayList<SimpleEntry<String,String>> result = jmaxMain.CalculateMaxSumAssignments(this.toString());
		for(SimpleEntry<String,String> ent : result)
		{
			if (ent.getValue().equals("1"))
			{
				selectedAgent = ent.getKey().replace("NodeVariable_", "");
			}
		}
		return this.agentsIndex.get(Integer.parseInt(selectedAgent));
	}
	
	public String GetBestAgent()
	{
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
		
		
		GenerateLines(0, totalVariables, "", f, 0);
		
		
		return f.toString();
	}
	
	private int GetQualityForAgent(int agentVariable)
	{
		for(ScheduleQualities ql : this.scheduleQualities)
		{
			if (agentVariable==ql.agentVariableId)
			{
				return ql.incremental;
			}
		}
		return 0;
	}
	
	private String SpaceOut(String input)
	{
		StringBuilder builder = new StringBuilder();
		char[] chars = input.toCharArray();
		for(char c : chars)
		{
			builder.append(c);
			builder.append(" ");
		}
		return builder.substring(0, builder.length()-1);
	}
	
	private int LogBase2(int variable)
	{
		return (int) (Math.log(variable)/Math.log(2));
	}
	
	private void GenerateLines(int currentVariableRepresentingAgentName, int totalVariables, String prefixBuiltSoFar, 
			StringBuilder f, int numberOfOnes)
	{
		List<Integer> singleVariableValues = new ArrayList<Integer>();
		for(int i=0;i<totalVariables;i++)
		{
			singleVariableValues.add( (int) Math.pow(2, i));
		}
		for(int i=0;i<(Math.pow(2,totalVariables));i++)
		{
			int quality = 0;
			if (singleVariableValues.contains(i))
				quality = GetQualityForAgent(LogBase2(i));
			String line = "F" + String.format("%0"+totalVariables+"d", Integer.parseInt(Integer.toString(i, 2)));
			f.append(SpaceOut(line) + " " + quality);//Don't want quality to be spaced out
			f.append(System.lineSeparator());
		}
		/*//if (totalVariables)
		{
			
			
			
			f.append(prefixBuiltSoFar + " ");
			f.append("0");
			numberOfOnes = numberOfOnes + 0;
			if (numberOfOnes!=1) f.append(" 0");//schedule quality is also zero for this case
			else f.append(" " + quality);
			
			
			f.append("F");
			f.append(prefixBuiltSoFar + " ");
			f.append("1");
			numberOfOnes = numberOfOnes + 1;
			
			if (numberOfOnes!=1) f.append(" 0");//schedule quality is also zero for this case
			else f.append(" " + quality);
			f.append(System.lineSeparator());
		}
		else
		{
			GenerateLines(currentVariableRepresentingAgentName+1, totalVariables, prefixBuiltSoFar + " 0", f, numberOfOnes+0);
			GenerateLines(currentVariableRepresentingAgentName+1, totalVariables, prefixBuiltSoFar + " 1", f, numberOfOnes+1);
		}
		*/
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
