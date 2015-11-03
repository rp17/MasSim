package masSim.schedule;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.sat4j.ILauncherMode;
import org.sat4j.core.*;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.ObjectiveFunction;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.reader.OPBReader2012;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ILogAble;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;

import masSim.schedule.AgentScheduleQualities;
import raven.MeasureTime;

public class BooleanOptimizationCalculator extends BestAgentCalculatorBase implements ILogAble {
	private boolean debugFlag = false;
	final int AGENTID = 0;
	final int TASKID = 1;
	private String log = "";
	
	public BooleanOptimizationCalculator(String instanceName, int numberOfAgents, int numberOfTasks)
	{
		super(instanceName, numberOfAgents, numberOfTasks);
	}
	
	@Override
	public List<List<Integer>> GetBestAgent()
	{
		List<List<Integer>> selectedAgentsForTasks = new ArrayList<List<Integer>>();
		Map<String, List<Integer>> variableNameMappingToAgentTaskCombination = new HashMap<String, List<Integer>>();
		
		String result = BuildOPBInput( this.agentScheduleQualities, variableNameMappingToAgentTaskCombination, this.numberOfTasksInNegotiation );
		String filename = "E:\\EclipseWorkspace\\RoverSim\\TaskRepository\\problemDynamic.opb";
		try (Writer writer = new BufferedWriter(new OutputStreamWriter(
	        new FileOutputStream(filename), "US-ASCII"))) {
			writer.write(result);
		}
		catch(Exception ex)
		{
			System.out.print(ex);
		}
		int[] result2 = Solve("E:\\EclipseWorkspace\\RoverSim\\TaskRepository\\problemDynamic.opb");
		List<Integer> resultList = new ArrayList<Integer>();
		System.out.println(Arrays.toString(resultList.toArray()));
		for(int r=0; r<result2.length; r++)
		{
			if (result2[r]>0) 
			{
				resultList.add(result2[r]);
				List<Integer> selectedAgent = variableNameMappingToAgentTaskCombination.get("x" + result2[r]);
				selectedAgentsForTasks.add(selectedAgent);
			}
		}
		return selectedAgentsForTasks;
	}
	
	public String BuildOPBInput(ArrayList<AgentScheduleQualities> input, Map<String, List<Integer>> variableNameMappingToAgentTastCombination, int numberOfTasks)
	{	
		StringBuilder opb = new StringBuilder();
				
		int taskCombinationsSize = input.get(0).TaskQualities.size();
		
		//Get all unique agents
		List<Integer> agentIds = new ArrayList<Integer>();
		for(int i=0;i<input.size();i++)
		{
			agentIds.add(input.get(i).AgentVariableId);
		}
		int agentsSize = agentIds.size();
				
		Map<Integer,List<String>> constraintsPerTask = new HashMap<Integer, List<String>>();
		
		opb.append(  String.format("* #variable= %1$s #constraint= %2$s%3$s", agentsSize * taskCombinationsSize, numberOfTasks, System.lineSeparator()));
		opb.append("min:");
		int i = 1;
		String variableMappingCommentBlock = "* ";
		for(int agent=0; agent<agentsSize; agent++)
		{
			int agentVariable = input.get(agent).AgentVariableId;
			List<MultipleTaskScheduleQualities> qls = input.get(agent).TaskQualities;
			for(int j=0;j<qls.size();j++)
			{
				List<Integer> mapping = new ArrayList<Integer>();
				mapping.add(agentVariable);
				mapping.addAll(qls.get(j).TaskIds);
				String variableName = "x" + i++;
				variableNameMappingToAgentTastCombination.put(variableName,mapping);
				
				//Calculate quality for this combination
				MultipleTaskScheduleQualities t = qls.get(j);
				int quality = t.diff();
				if (quality>0)//- sign to convert min function to max
				{	
					opb.append(" -" + quality + " " + variableName);
				}
				else if (quality<0)
					opb.append(" " + Math.abs(quality) + " " + variableName);
				else
					opb.append(" " + quality + " " + variableName);
				variableMappingCommentBlock += variableName + "=" + agentVariable + Arrays.toString(t.TaskIds.toArray()) + " ";
				AddPBVariableToConstraintsList(constraintsPerTask, variableName, t.TaskIds);
			}
		}
		opb.append(";" + System.lineSeparator());
		opb.append(variableMappingCommentBlock + System.lineSeparator());
		for(List<String> constraintsForATask : constraintsPerTask.values())
		{
			for(String str : constraintsForATask)
			{
				opb.append( "1 " + str + " "  );
			}
			opb.append( "= 1;" + System.lineSeparator() );
		}
		return opb.toString();
	}
	
	private void AddPBVariableToConstraintsList(Map<Integer,List<String>> constraintsPerTask, String variable, List<Integer> tasksForThisVariable)
	{
		for(Integer _int : tasksForThisVariable)
		{
			if (constraintsPerTask.containsKey(_int))
			{
				constraintsPerTask.get(_int).add(variable);
			}
			else
			{
				List<String> arr = new ArrayList<String>();
				arr.add(variable);
				constraintsPerTask.put(_int, arr);
			}
		}
	}
	
	public int[] SolveOptimizationProblem(ArrayList<AgentScheduleQualities> qualities)
	{
		int[] result;
		try 
		{
			int numberOfAgents = qualities.size();
			int numberOfTasks = qualities.get(0).TaskQualities.size();//Assuming all agents report qualities equally for all tasks
			int numberOfVariables = numberOfAgents * numberOfTasks;
			
			ASolverFactory<IPBSolver> factory = SolverFactory.instance();
			IPBSolver solver = factory.defaultSolver();
			solver = new PseudoOptDecorator(solver);
			solver.setVerbose(false);
			solver.reset();
			solver.newVar(numberOfVariables);
			
			IVecInt objectiveVars = new VecInt();
			IVec<BigInteger> objectiveCoeffs = new Vec<BigInteger>();
			
			StringBuilder opb = new StringBuilder();
			int i = 1;
			for(AgentScheduleQualities ql : qualities)
			{
				/*for(TaskScheduleQualities tl : ql.TaskQualities)
				{
					int [] mapping = new int[2];
					mapping[AGENTID] = ql.AgentVariableId;
					mapping[TASKID] = tl.TaskId;
					int variableNumber = i++;
					String variableName = "x" + variableNumber;
					dictionary.put(variableName,mapping);
					int diff = tl.diff() * -1;
					objectiveCoeffs.push( BigInteger.valueOf(diff) );
					objectiveVars.push(variableNumber);
				}*/
			}
			IVecInt[] constraintCoeffs = new VecInt[numberOfTasks];
			IVecInt[] constraintVars = new VecInt[numberOfTasks];
			for(int j=1;j<=numberOfTasks;j++)
			{
				constraintVars[j-1] = new VecInt();
				constraintCoeffs[j-1] = new VecInt();
			}
			int index = 1;
			for(int k=1;k<=numberOfAgents;k++)
			{
				for(int j=1;j<=numberOfTasks;j++)
				{
					constraintVars[j-1].push(index++);
					constraintCoeffs[j-1].push(1);
				}	
			}
			for(int j=1;j<=numberOfTasks;j++)
			{
				
					solver.addExactly(constraintVars[j-1], constraintCoeffs[j-1], 1);//weight is always 1 in our problems
				
			}
			solver.setObjectiveFunction(new ObjectiveFunction(objectiveVars,objectiveCoeffs));
			IOptimizationProblem problem = (IOptimizationProblem)solver;
			PrintWriter out = new PrintWriter(System.out, true);
			long beginTime = 0;
			long nano = System.nanoTime();
			MeasureTime.Timer2 = new MeasureTime();
			MeasureTime.Timer2.Start();
			problem.isSatisfiable(new VecInt(), true);
			//ILauncherMode.OPTIMIZATION.solve(problem, new OPBReader2012(solver), this, out, beginTime);
			MeasureTime.Timer2.Stop();
			//System.out.println("Boolean Solver (direct) Took (microseconds) " + MeasureTime.Timer2.GetTotal() );
			if (false) throw new TimeoutException();
			return problem.model();
		} catch (ContradictionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return null;
		
	} 
	
	public int[] Solve(String problemName)//Convert to problem obp content string instead of file name
	{
		int[] result = null;
		try {
			ASolverFactory<IPBSolver> factory = SolverFactory.instance();
			
			IPBSolver theSolver = factory.defaultSolver();
			theSolver = new PseudoOptDecorator(theSolver);
			theSolver.setVerbose(true);
			theSolver.setTimeoutMs(1000);
			
			OPBReader2012 reader = new OPBReader2012(theSolver);
			IProblem problem = reader.parseInstance(problemName);
			
			IOptimizationProblem optproblem = (IOptimizationProblem) problem;
			
			PrintWriter out = new PrintWriter(System.out, true);
			long beginTime = System.currentTimeMillis();
			MeasureTime.Timer1 = new MeasureTime();
			MeasureTime.Timer1.Start();
			ILauncherMode.OPTIMIZATION.solve(problem, reader, this, out, beginTime);
			//problem.isSatisfiable(new VecInt(),true);
			MeasureTime.Timer1.Stop();
			System.out.println("Boolean Solver Took (microseconds) " + MeasureTime.Timer1.GetTotal());
			if (!optproblem.hasNoObjectiveFunction()) {
				String objvalue;
				objvalue = optproblem.getObjectiveValue().toString();
				if (true) System.out.println("FinaL" + objvalue);
			}
			result = optproblem.model();
		} catch (ParseFormatException | IOException | ContradictionException e) {// | TimeoutException
			e.printStackTrace();
		}
		return result;
	}
	
	@Override
	public void log(String arg0) {
		this.log += arg0 + "\n";
	}
}
