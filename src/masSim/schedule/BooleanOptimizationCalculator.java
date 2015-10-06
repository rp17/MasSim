package masSim.schedule;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import java.io.*;
import java.nio.charset.StandardCharsets;
import org.sat4j.ILauncherMode;
import org.sat4j.core.*;
import org.sat4j.pb.IPBSolver;
import org.sat4j.pb.PseudoOptDecorator;
import org.sat4j.pb.SolverFactory;
import org.sat4j.pb.reader.OPBReader2012;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ILogAble;
import org.sat4j.specs.IOptimizationProblem;
import org.sat4j.specs.IProblem;
import masSim.schedule.AgentScheduleQualities;

public class BooleanOptimizationCalculator implements ILogAble {
	final int AGENTID = 0;
	final int TASKID = 1;
	Map<String,int[]> dictionary = new HashMap<String, int[]>();
	private String log = "";
	
	public String BuildOPBInputSingle(ArrayList<ScheduleQualities> scheduleQualities)
	{
		ArrayList<AgentScheduleQualities> aqlList = new ArrayList<AgentScheduleQualities>();
		for(ScheduleQualities sq : scheduleQualities)
		{
			AgentScheduleQualities aql = new AgentScheduleQualities(sq.agentVariableId);
			aql.TaskQualities.add(new TaskScheduleQualities(0,sq.base, sq.incremental));
			aqlList.add(aql);
		}
		return BuildOPBInput(aqlList);
	}
	
	public String BuildOPBInput(ArrayList<AgentScheduleQualities> scheduleQualities)
	{	
		StringBuilder opb = new StringBuilder();
		int numberOfAgents = scheduleQualities.size();
		if (numberOfAgents<=0)
			return "";
		int numberOfTasks = scheduleQualities.get(0).TaskQualities.size();//Assuming all agents report qualities equally for all tasks
		opb.append(  String.format("* #variable= %1$s #constraint= %2$s%3$s", numberOfAgents * numberOfTasks, numberOfTasks, System.lineSeparator()));
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
				int diff = tl.diff();
				if (diff>0)//- sign to convert min function to max
					opb.append(" -" + tl.diff() + " " + variableName);
				else if (diff<0)
					opb.append(" " + Math.abs(diff) + " " + variableName);
				else
					opb.append(" " + diff + " " + variableName);
			}
		}
		opb.append(";" + System.lineSeparator());
		String[] constraints = new String[numberOfTasks];
		for(int j=1;j<=numberOfTasks;j++)
		{
			constraints[j-1] = "";
		}
		int index = 1;
		for(int k=1;k<=numberOfAgents;k++)
		{
			for(int j=1;j<=numberOfTasks;j++)
			{
				constraints[j-1] += "1 x"+index++ + " ";
			}	
		}
		for(int j=1;j<=numberOfTasks;j++)
		{
			opb.append(constraints[j-1] + "= 1;" + System.lineSeparator());
		}
		return opb.toString();
	}
	
	public int Solve(String problemName)//Convert to problem obp content string instead of file name
	{
		int retValue = 0;
		try {
			ASolverFactory<IPBSolver> factory = SolverFactory.instance();
			//setLauncherMode();
			IPBSolver theSolver = factory.defaultSolver();
			theSolver = new PseudoOptDecorator(theSolver);
			theSolver.setVerbose(true);
			OPBReader2012 reader = new OPBReader2012(theSolver);
			IProblem problem = reader.parseInstance(problemName);
			IOptimizationProblem optproblem = (IOptimizationProblem) problem;
			
			PrintWriter out = new PrintWriter(System.out, true);
			long beginTime = System.currentTimeMillis();
			
			ILauncherMode.OPTIMIZATION.solve(problem, reader, this, out, beginTime);
			if (!optproblem.hasNoObjectiveFunction()) {
				String objvalue;
				objvalue = optproblem.getObjectiveValue().toString();
				System.out.println("FinaL" + objvalue);
			}
			int[] result = optproblem.model();
			for(int i=0;i<result.length;i++)
			{
				if (result[i]>0)
					retValue = result[i];
				out.println(result[i]);
			}
		} catch (ParseFormatException | IOException | ContradictionException e) {
			e.printStackTrace();
		}
		return retValue-1;
	}
	
	@Override
	public void log(String arg0) {
		this.log += arg0 + "\n";
	}
}
