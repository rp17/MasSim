package masSim.schedule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import masSim.schedule.AgentScheduleQualities;

public class BooleanOptimizationCalculator {
	final int AGENTID = 0;
	final int TASKID = 1;
	Map<String,int[]> dictionary = new HashMap<String, int[]>();
	
	public String BuildOPBInput(ArrayList<AgentScheduleQualities> scheduleQualities)
	{	
		StringBuilder opb = new StringBuilder();
		int numberOfAgents = scheduleQualities.size();
		if (numberOfAgents<=0)
			return "";
		int numberOfTasks = scheduleQualities.get(0).TaskQualities.size();//Assuming all agents report qualities equally for all tasks
		opb.append(  String.format("* #variable= %1$s #constraint= %2$s\n", numberOfAgents, numberOfTasks));
		opb.append("min:");
		int i = 1;
		for(AgentScheduleQualities ql : scheduleQualities)
		{
			for(TaskScheduleQualities tl : ql.TaskQualities)
			{
				int [] mapping = new int[2];
				mapping[AGENTID] = ql.AgentVariableId;
				mapping[TASKID] = tl.TaskId;
				String variableName = "x" + i++;
				dictionary.put(variableName,mapping);
				opb.append(" -" + tl.diff());//- sign to convert min function to max
				opb.append(";\n");
			}
		}
		int index = 1;
		for(int j=1;j<=numberOfTasks;j++)
		{
			//Add one constraint for each task
			for(int k=1;k<=numberOfAgents;k++)
			{
				opb.append("1 x"+index + " ");
			}
			opb.append("= 1\n");
		}
		return opb.toString();
	}
}
