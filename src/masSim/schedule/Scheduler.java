package masSim.schedule;

import java.util.ArrayList;
import java.util.Iterator;

import masSim.taems.*;
import masSim.world.LapsedTime;
import masSim.world.MqttMessagingProvider;
import masSim.world.TaskRepository;

import java.util.*;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import Aspect.PredicateParameterFilter;
import Aspect.StatementEvent;
import BAEventMonitor.Execution;
import BAEventMonitor.Param;
import BAEventMonitor.Params;
import BAEventMonitor.Predicate;
import BAEventMonitor.Execution.ExecutionMode;
import BAEventMonitor.Param.StoreMode;
import raven.Main;
import raven.math.Vector2D;
import raven.utils.SchedulingLog;

public class Scheduler implements Runnable {

	private boolean debugFlag = true;
	//Represents the start time when this schedule is being calculated, 
	public static Date startTime = new Date();
	public static long lapsedTime;


	private IAgent agent;

	public Scheduler(IAgent agent)
	{
		this.agent = agent;
		lapsedTime = 0;
	}

	//This is the main method of the scheduler, which implements runnable interface of java thread
	//It will run continuously when the scheduler thread is started. It will initially calculate the schedule
	//for current taskGroup, and then subsequently keep checking for arrival of new pending tasks, so that
	//the schedule is updated whenever they arrive
	@Override
	public void run() 
	{
		List<Task> pendingTasks = this.agent.getPendingTasks();
		if (!pendingTasks.isEmpty())
		{
			for(int i=0;i<pendingTasks.size();i++)
			{
				Main.Message(this, debugFlag, "Pending task " + pendingTasks.get(i).label + " found for agent " + this.agent.getName());
			}
			Schedule schedule = CalculateSchedule();
			//StatementEvent.evaluateScheduleOptimal(schedule);
			if (schedule!=null) {
				this.agent.UpdateSchedule(schedule);	
				//Instrumentation
				//StatementEvent.getExecutionPlan(schedule);
			} else {
				Main.Message(true, this.agent.getName() + " schedule came out to be null");
			}
		}
	}


	@Params({@Param(name="schedule", variable="schedule", pred="containWaypoint", mode=StoreMode.Single), @Param(name="task", variable="newTask", pred="containWaypoint", mode=StoreMode.List)})
	@Execution(name="containWaypoint", mode=ExecutionMode.After)
	@Param(name="task", variable="newTask", pred="reachWaypoint", mode=StoreMode.List)
	public synchronized Schedule CalculateSchedule()
	{
		long start = LapsedTime.getStart();
		//try {
		//Read all new tasks
		int numberOfPendingTasks = this.agent.getPendingTasks().size();
		if (numberOfPendingTasks<=0) return null;
		//boolean newTasksAssigned = assignTask(null);
		String debugMessage = "";
		for(int i=0;i<numberOfPendingTasks;i++)
		{
			Task newTask = this.agent.getPendingTasks().get(0);
			debugMessage += " > " + newTask.label;
			this.agent.getPendingTasks().remove(0);
			if (newTask.agent.equals(agent)){
				synchronized(Task.Lock)
				{
					Main.Message(true, "entered lock 2");
					agent.GetCurrentTasks().addTask(newTask);
					List<Method> methods = agent.GetCurrentTasks().GetMethods();
					System.out.println("Current task structure updated, methods are : ");
					for(Method m : methods) {
						System.out.println(m.label);
					}
					//instrumentation
					//PredicateParameterFilter.addTask(newTask);
				}
				Main.Message(true, "exited lock 2");
			}
		}
		//Remove completed tasks
		/*
			synchronized(Task.Lock){
					agent.GetCurrentTasks().Cleanup(MqttMessagingProvider.GetMqttProvider());
			}
		 */
		synchronized(Task.Lock){
			agent.GetCurrentTasks().Cleanup();
		}
		if(agent.GetCurrentTasks().hasChildren())
		{
			Schedule schedule = CalculateScheduleFromTaems(agent.GetCurrentTasks());
			//instrumentation
			//PredicateParameterFilter.addSchedule(schedule);
			//instrumentation
			//StatementEvent.executeScheduleContainsTask();
			lapsedTime = lapsedTime + LapsedTime.getLapsed(start);
			return schedule;
		}

		//Thread.sleep(10000); // what is this sleep for ??

		//} catch (InterruptedException e) {
		//Main.Message(debugFlag, "[Schedular 109]" + e.toString());
		//}
		lapsedTime = lapsedTime + LapsedTime.getLapsed(start);
		return null;
	}

	//Method takes a Teams structure as input and outputs all the possible schedules resulting from that
	//task structure. This output is then fed to a generic Dijkstra's algorithm to calculate the optimum schedule
	//corresponding to the optimum path from the starting task to the ending task
	public Schedule CalculateScheduleFromTaems(Task topLevelTask)
	{
		long start = LapsedTime.getStart();
		Iterator ii = topLevelTask.getSubtasks();
		//Reinitialize the schedule item
		Schedule schedule = new Schedule();
		//Reinitialize the start time of calculation
		startTime = new Date();

		//Create set of Nodes representing all methods that can be executed in the eventual schedule
		ArrayList<Method> nodes = new ArrayList<Method>();
		//Create set of all possible transitions of execution from one method to another, which represents an actual
		//pass through a possible schedule
		ArrayList<MethodTransition> edges = new ArrayList<MethodTransition>();
		//Add an initial node, which will act as our starting point
		Vector2D agentPos = topLevelTask.agent.getPosition();
		Method initialMethod = new Method(Method.StartingPoint,0,agentPos.x,agentPos.y);
		nodes.add(initialMethod);
		Method finalMethod = new Method(Method.FinalPoint,0,agentPos.x,agentPos.y);
		nodes.add(finalMethod);
		//Append all possible schedule options to this set, after parsing the input Taems structure
		Method[] finalMethodList = AppendAllMethodExecutionRoutes(nodes, edges, topLevelTask, new Method[]{initialMethod}, null, true);
		for(int i=0;i<finalMethodList.length;i++)
		{
			MethodTransition t = new MethodTransition(
					"From " + finalMethodList[i].label + " to " + finalMethod.label, 
					finalMethodList[i], finalMethod);
			edges.add(t);
		}

		//Create a Graph of these methods and run Dijkstra Algorithm on it
		Graph graph = new Graph(nodes, edges);
		graph.Print();
		DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph, agentPos);
		dijkstra.execute(initialMethod);
		LinkedList<Method> path = dijkstra.getPath(finalMethod);
		//Print the determined schedule
		int totalquality = 0;
		if (path!=null)
			for (Method vertex : path) {
				if (!vertex.label.equals("Final Point"))//Ignore final point as its only necessary to complete graph for dijkstra, but we don't need to visit it
				{
					totalquality += vertex.getOutcome().getQuality();
					schedule.addItem(new masSim.taems.ScheduleElement(vertex));
				}
			}
		schedule.TotalQuality = totalquality;
		lapsedTime = lapsedTime + LapsedTime.getLapsed(start);
		return schedule;
	}

	//A helper method used internally by CalculateScheduleFromTaems method
	private void Permute(Node[] inputArray, int start, int end, ArrayList<Node[]> permutations)
	{
		if(start==end) 
		{
			permutations.add(inputArray.clone());
		}
		else
		{
			for(int i=start;i<=end;i++)
			{
				Node t = inputArray[start];
				inputArray[start]=inputArray[i]; 
				inputArray[i]=t;
				Permute(inputArray,start+1,end,permutations);
				t=inputArray[start];
				inputArray[start]=inputArray[i]; 
				inputArray[i]=t;
			}
		}
	}

	private void PrintNodeArray(Node[] n)
	{
		String m = "";
		for(Node s:n)
		{
			m += s.label + " > ";
		}
		Main.Message(debugFlag, "[Scheduler 185] Node Methods: " + m );
	}

	//A helper method used internally by CalculateScheduleFromTaems method
	private Method[] AppendAllMethodExecutionRoutes(ArrayList<Method> nodes, ArrayList<MethodTransition> edges, Node task,
			Method[] appendTo, Node Parent, boolean makeMethodsUnique)
	{
		ArrayList<Method> lastMethodList = new ArrayList<Method>();
		for(int mIndex = 0; mIndex<appendTo.length; mIndex++)
		{
			//For subtasks, look at the relevant QAF, which will constrain how the tasks must be scheduled (and executed)
			Method lastMethod = appendTo[mIndex];
			if (!task.IsTask())
			{
				Method m;
				if (makeMethodsUnique)
					m = new Method((Method)task);
				else
					m = (Method)task;
				m.AddObserver(Parent);
				nodes.add(m);
				MethodTransition t = new MethodTransition("From " + lastMethod.label + " to " + m.label, lastMethod, m);
				edges.add(t);
				lastMethodList.add(m);
			}
			else
			{
				Method[] localLastMethodList = new Method[]{lastMethod};
				Task tk = (Task)task;
				//Main.Message(debugFlag, "[Scheduler 212] Node is Task. Enumerating children for " + tk.label);
				//Designate a parent for this task, which can be used for completion notifications up the hierarchy
				if (Parent!=null) tk.AddObserver(Parent);
				masSim.taems.QAF qaf = tk.getQAF();
				if (qaf instanceof SeqSumQAF)
				{
					//All tasks must be executed in sequence
					//TODO We can cater for earliest start time introducing wait
					for(Iterator<Node> subtasks = tk.getSubtasks(); subtasks.hasNext(); ) {
						Node subtask = subtasks.next();
						localLastMethodList = AppendAllMethodExecutionRoutes(nodes, edges, subtask, localLastMethodList, tk, true);
					}
					for(int i=0;i<localLastMethodList.length;i++)
					{
						if (!lastMethodList.contains(localLastMethodList[i]))
							lastMethodList.add(localLastMethodList[i]);
					}
				}
				if (qaf instanceof SumAllQAF)
				{
					//All tasks must be executed, though not necessarily in sequence
					//Create task list whose permutations need to be found
					ArrayList<Node> subtasksList = new ArrayList<Node>();
					Iterator<Node> subtasks = tk.getSubtasks();
					while(subtasks.hasNext()) {
						Node subtask = subtasks.next();
						subtasksList.add(subtask);
					}
					Node[] subTaskListForSumPermutation = subtasksList.toArray(new Node[subtasksList.size()]);
					PrintNodeArray(subTaskListForSumPermutation);
					//Find permutations for this task list
					ArrayList<Node[]> permutations = new ArrayList<Node[]>(); 
					Permute(subTaskListForSumPermutation,0,subTaskListForSumPermutation.length-1,permutations);
					//Now create paths for each permutation possible
					Method[] permutationLinkMethodsList = localLastMethodList;
					for (Node[] s : permutations)
					{
						Method[] m = new Method[]{lastMethod};
						//If there are multiple methods, we want them to be separated out in the graph to avoid cross linkages of permuted values. But if there is
						//only one, then for aesthetic purposes, we can have the same object repeated
						boolean multiplePermutationRequringUniqueMethodsForGraph = true;
						//						if (s.length<2) multiplePermutationRequringUniqueMethodsForGraph = false;
						for(int i=0;i<s.length;i++)
						{
							permutationLinkMethodsList = AppendAllMethodExecutionRoutes(nodes, edges, s[i], permutationLinkMethodsList, tk, multiplePermutationRequringUniqueMethodsForGraph);
						}
						for(int y=0;y<permutationLinkMethodsList.length;y++)
						{
							lastMethodList.add(permutationLinkMethodsList[y]);
						}
						//Reset permutation starting point
						permutationLinkMethodsList = localLastMethodList;
					}
				}
				if (qaf instanceof ExactlyOneQAF)
				{
					//Only one task must be executed
					//Define a new Lastmethod for this QAF where all possible tasks will converge, 
					//after only "only one" of them has executed
					for(Iterator<Node> subtasks = task.getSubtasks(); subtasks.hasNext(); ) {
						Node subtask = (Node) subtasks.next();
						Method[] m = AppendAllMethodExecutionRoutes(nodes, edges, subtask, localLastMethodList, tk, false);
						for(int i=0;i<m.length;i++)
						{
							lastMethodList.add(m[i]);
						}
					}
				}
			}
		}
		Method[] result = lastMethodList.toArray(new Method[lastMethodList.size()]);
		return result;
	}


}


