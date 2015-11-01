package masSim.schedule;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
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

public class BooleanOptimizationCalculator implements ILogAble {
	private boolean debugFlag = false;
	final int AGENTID = 0;
	final int TASKID = 1;
	private String log = "";
	
	public String BuildOPBInputSingle(ArrayList<ScheduleQualities> scheduleQualities)
	{
		ArrayList<AgentScheduleQualities> aqlList = new ArrayList<AgentScheduleQualities>();
		for(ScheduleQualities sq : scheduleQualities)
		{
			AgentScheduleQualities aql = new AgentScheduleQualities(sq.agentVariableId);
			//aql.TaskQualities.add(new TaskScheduleQualities(0,sq.base, sq.incremental));
			aqlList.add(aql);
		}
		return null;//BuildOPBInput(aqlList);
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
