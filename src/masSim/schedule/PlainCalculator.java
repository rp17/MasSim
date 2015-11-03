package masSim.schedule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sat4j.specs.ILogAble;

import raven.Main;
import raven.MeasureTime;

public class PlainCalculator extends BestAgentCalculatorBase implements ILogAble {
	private boolean debugFlag = false;
	final int AGENTID = 0;
	final int TASKID = 1;
	private String log = "";
	
	public PlainCalculator(String instanceName, int numberOfAgents, int numberOfTasks)
	{
		super(instanceName, numberOfAgents, numberOfTasks);
	}
	
	public PlainCalculator(BestAgentCalculatorBase calc)
	{
		super(calc);
	}
	
	@Override
	public List<List<Integer>> GetBestAgent()
	{
		//ArrayList<AgentScheduleQualities> input, List<List<Integer>> taskCombinations
		
		MeasureTime.Timer2 = new MeasureTime();
		MeasureTime.Timer2.Start();
	
		int taskCombinationsSize = this.agentScheduleQualities.get(0).TaskQualities.size();
		
		//Get all unique agents
		List<Integer> agentIds = new ArrayList<Integer>();
		for(int i=0;i<this.agentScheduleQualities.size();i++)
		{
			agentIds.add(this.agentScheduleQualities.get(i).AgentVariableId);
		}
		int agentsSize = agentIds.size();
		
		//Variable to hold all possible combinations of tasks for any agent
		//Map<Integer[], Integer> agentTaskCombinationQualities = new HashMap<Integer[],Integer>();
		
		double totalPossibleValues = Math.pow(taskCombinationsSize, agentsSize);
		//Populate all combinations
		
		int bestQuality = -9999999;
		int[] bestCombination = new int[0];
		
		int[] lastValidAssignment = new int[agentsSize];
		int[] nextAssignment = new int[agentsSize];
		
		for(int i=0; i<totalPossibleValues; i++)
		{
			nextAssignment = ConvertBase10ToBaseNumberOfTasks(i,agentsSize, taskCombinationsSize);
			//System.out.println(Arrays.toString(assignments));
			
			if (!IsAssignmentValid(nextAssignment, agentsSize, this.agentScheduleQualities))
			{
				Main.Message(debugFlag, ToString(MapToResult(nextAssignment))+ " INVALID");
				continue;
			}
			lastValidAssignment = nextAssignment;
			//System.out.println(RemapAssignmentToAgentTasks(lastValidAssignment, taskCombinations));
			int tempQuality = 0;
			//Find combination quality
			for(int agentIndex=0; agentIndex<agentsSize; agentIndex++)
			{
				int inn = lastValidAssignment[agentIndex];
				try
				{
					MultipleTaskScheduleQualities ql = this.agentScheduleQualities.get(agentIndex).TaskQualities.get(inn);
					tempQuality += ql.diff();
				}
				catch(Exception ex)
				{
					System.out.println(inn + " " + ex);
				}
				
			}
			Main.Message(debugFlag, ToString(MapToResult(nextAssignment)) + " " + tempQuality);
			//If this is best, assign
			if (tempQuality>bestQuality)
			{
				bestQuality = tempQuality;
				bestCombination = lastValidAssignment;
			}
		}
		
		MeasureTime.Timer2.Stop();
		System.out.println("Plain Calculation Took " + MeasureTime.Timer2.GetTotal());
		List<List<Integer>> result = MapToResult(bestCombination);
		return result;
	}
	
	private int[] ConvertBase10ToBaseNumberOfTasks(double number, int numAgents, int numTasksCombinations)
	{
		int[] result = new int[numAgents+1];
		double remainder = number;
		for(int i=numAgents; i>=0; i--)
		{
			double currentBase = Math.pow(numTasksCombinations,i);
			result[numAgents-i] = (int)(remainder / currentBase);
			remainder = remainder % currentBase;
		}
		return Arrays.copyOfRange(result, 1, result.length);
	}
	
	private boolean IsAssignmentValid(int[] assignments, int agentsSize, List<AgentScheduleQualities> agentScheduleQualities)
	{
		boolean result = true;
		List<Integer> tasksAlreadyTakenUpByAnAgent = new ArrayList<Integer>();
		
		//List<Integer> tasksSetPrevious = null;
		for(int agentIndex=0; agentIndex<agentsSize; agentIndex++)
		{
			int inn = assignments[agentIndex];
			try
			{
				MultipleTaskScheduleQualities ql = agentScheduleQualities.get(agentIndex).TaskQualities.get(inn);
				for(Integer task : ql.TaskIds)
				{
					if (tasksAlreadyTakenUpByAnAgent.contains(task))
					{
						return false;
					}
					else
					{
						tasksAlreadyTakenUpByAnAgent.add(task);
					}
				}
			}
			catch(Exception ex)
			{
				System.out.println(inn + " " + ex);
			}	
		}
		return result;
	}

	private List<List<Integer>> MapToResult(int[] chosenAssignment)
	{
		List<List<Integer>> allAgentAssignments = new ArrayList<List<Integer>>();
		for(int i=0;i<chosenAssignment.length;i++)
		{
			List<Integer> agentAssignment = new ArrayList<Integer>();
			agentAssignment.add(agentScheduleQualities.get(i).AgentVariableId);
			MultipleTaskScheduleQualities ql = agentScheduleQualities.get(i).TaskQualities.get(chosenAssignment[i]);
			for(Integer in : ql.TaskIds)
			{
				agentAssignment.add(in);
			}
			allAgentAssignments.add(agentAssignment);
		}
		return allAgentAssignments;
	}
	
	@Override
	public void log(String message) {
		// TODO Auto-generated method stub
		
	}

}
