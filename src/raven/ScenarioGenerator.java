package raven;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import masSim.schedule.AgentScheduleQualities;
import masSim.schedule.MultipleTaskScheduleQualities;
import masSim.taems.Task;

public class ScenarioGenerator {

	public void CreateTestTasks(int numberOfTasks)
	{
		int min = 100;
		int max = 500;
		StringBuilder tasksMain = new StringBuilder();
		StringBuilder tasksDetails = new StringBuilder();
		tasksMain.append("<Taems>" + System.lineSeparator());
		tasksDetails.append("<Taems>" + System.lineSeparator());
		for(int i = 1; i<=numberOfTasks; i++)
		{
			int x = (int)(Math.random() * (max - min) + min);
			int y = (int)(Math.random() * (max - min) + min);
			String task = String.format("<Task id=\"T%1$d\" name=\"T%1$d\" qaf=\"SumAll\" ><Method id=\"M%1$d\" name=\"M%1$d\" Quality=\"500\" Duration=\"10\" XCoord=\"%2$d\" YCoord=\"%3$d\"></Method></Task>", 
					i, x, y);
			tasksDetails.append( task + System.lineSeparator());
			tasksMain.append( String.format("<Task id=\"T%1$d\" name=\"T%1$d\" qaf=\"SumAll\" />", i) + System.lineSeparator());
		}
		tasksMain.append("</Taems>");
		tasksDetails.append("</Taems>");
		
		String fileMain = "E:\\EclipseWorkspace\\RoverSim\\TaskRepository\\TasksMain.xml";
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	        new FileOutputStream(fileMain), "US-ASCII"))) {
			writer.write(tasksMain.toString());
		}
		catch(Exception ex)
		{
			System.out.print(ex);
		}
		
		String fileDetails = "E:\\EclipseWorkspace\\RoverSim\\TaskRepository\\TasksDetails.xml";
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
		        new FileOutputStream(fileDetails), "US-ASCII"))) {
				writer.write(tasksDetails.toString());
		}
		catch(Exception ex)
		{
			System.out.print(ex);
		}
	}
	
	
	
	protected int GetRandomBaseQuality()
	{
		int min = 80;
		int max = 100;
		return (int)(Math.random() * (max - min) + min);
	}
	
	protected int GetRandomIncrementalQuality()
	{
		int min = 100;
		int max = 120;
		return (int)(Math.random() * (max - min) + min);
	}
	
	private List<Integer> Clone(List<Integer> arr)
	{
		List<Integer> copy = new ArrayList<Integer>();
		copy.addAll(arr);
		return copy;
	}
	
	public List<List<Task>> GetArrayCombinationsTasks(List<Task> arr)
	{
		List<Integer> arrInt = new ArrayList<Integer>();
		for(Task t: arr)
		{
			arrInt.add(t.GetIntId());
		}
		List<List<Integer>> combos = GetArrayCombinations(arrInt);
		List<List<Task>> result = new ArrayList<List<Task>>();
		return result; 
	}
	
	public List<List<Integer>> GetArrayCombinations(List<Integer> arr)
	{
		List<List<Integer>> result = new ArrayList<List<Integer>>();
		if (arr.size()<=1)
		{ 
			result.add(Clone(arr));
		}
		else
		{
			int firstInteger = arr.get(0);
			List<Integer> subArrayWithOneLessItem = arr.subList(1, arr.size());
			List<List<Integer>> combinations = GetArrayCombinations(subArrayWithOneLessItem);
			for(List<Integer> aCombination : combinations)
			{
				result.add(aCombination);
				List<Integer> aCombinationWithFirstIntegerAdded = Clone(aCombination);
				aCombinationWithFirstIntegerAdded.add(firstInteger);
				result.add(aCombinationWithFirstIntegerAdded);
			}
			List<Integer> aCombinationWithOnlFirstInteger = new ArrayList<Integer>();
			aCombinationWithOnlFirstInteger.add(firstInteger);
			result.add(aCombinationWithOnlFirstInteger);
		}
		//Final empty one
		//result.add(new ArrayList<Integer>());
		return result;
	}
	
	private ArrayList<MultipleTaskScheduleQualities> GetRandomMultipleTaskScheduleQualities(List<Integer> taskIds)
	{
		ArrayList<MultipleTaskScheduleQualities> result = new ArrayList<MultipleTaskScheduleQualities>();
		int base = GetRandomBaseQuality();
		List<List<Integer>> taskCombinations = GetArrayCombinations(taskIds);
		for(List<Integer> taskCombination : taskCombinations)
		{
			result.add(new MultipleTaskScheduleQualities(taskCombination,base,GetRandomIncrementalQuality()));	
		}
		result.add(new MultipleTaskScheduleQualities(new ArrayList<Integer>(),base,base));	
		return result;
	}
	
	private List<Integer> CreateRandomArrayOfTaskIds(int numberOfTasks)
	{
		List<Integer> tasks = new ArrayList<Integer>();
		for(int i=0; i<numberOfTasks; i++)
		{
			tasks.add(1000 +i);
		}
		return tasks;
	}
	
	
	public ArrayList<AgentScheduleQualities> CreateRamdomScheduleQualities(int numberOfAgents, int numberOfTasks)
	{
		ArrayList<AgentScheduleQualities> scheduleQualities = new ArrayList<AgentScheduleQualities>();
		for(int i = 1; i<=numberOfAgents; i++)
		{
			AgentScheduleQualities a = new AgentScheduleQualities(100+i);
			a.TaskQualities = new ArrayList<MultipleTaskScheduleQualities>();
			List<Integer> taskIds = CreateRandomArrayOfTaskIds(numberOfTasks);
			for(int j = 1; j<=numberOfTasks; j++)
			{
				a.TaskIds = taskIds;
				a.TaskQualities = GetRandomMultipleTaskScheduleQualities(taskIds);
			}
			scheduleQualities.add(a);
		}
		return scheduleQualities;
	}
}
