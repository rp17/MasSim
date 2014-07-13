package masSim.schedule;

import java.util.ArrayList;
import java.util.Iterator;

import masSim.taems.*;

import java.util.*;

import raven.Main;
import raven.math.Vector2D;

public class Scheduler implements Runnable {
	
	private boolean debugFlag = false;
	//Represents the start time when this schedule is being calculated, 
	public static Date startTime = new Date();
	
	//New pending tasks that need to be added to the taskGroup, whose schedule needs to be calculated, usually
	//during runtime when a previous schedule has already been calculated and is available, but which needs
	//to be updated now with arrival of these new tasks
	public ArrayList<Task> PendingTasks = new ArrayList<Task>();
	
	//Represents the top level task, called task group in taems, which contains all child tasks to be scheduled
	private Task taskGroup;
	private IAgent agent;
	
	//Represents the current final optimum schedule calculated for the taskGroup member
	private Schedule schedule;
	
	//Collection of schedule event listeners, who will be notified when the schedule changes
	private ArrayList<IScheduleUpdateEventListener> listeners = new ArrayList<IScheduleUpdateEventListener>();
	
	public Scheduler(IAgent agent)
	{
		this.agent = agent;
		taskGroup = new Task("Task Group",new SumAllQAF(), null, agent);
	}
	
	//A public method to feed new tasks to the scheduler
	public void AddScheduleUpdateEventListener(IScheduleUpdateEventListener listener)
	{
		listeners.add(listener);
	}
	
	public int GetScheduleCostSync(Task task, IAgent taskAgent)
	{
		//Make a copy
		if (task!=null) taskAgent = task.agent;
		Task tempTaskGroup = new Task("Task Group",new SumAllQAF(), null, taskAgent);
		Iterator<Node> copyTasks = taskGroup.getSubtasks();
		while(copyTasks.hasNext())
		{
			tempTaskGroup.addTask(copyTasks.next());
		}
		//Sometimes we want to calculate base cost of executing existing tasks, without assiging a new one, where this
		//method will be called with a null value. So this check is necessary
		if (task!=null)
		{
			tempTaskGroup.addTask(task);
			Main.Message(debugFlag, "[Scheduler 56] task added to tempTaskGroup " + task.label + " in " + agent.getName());
		} else
			Main.Message(debugFlag, "[Scheduler 56] no new task added. Just calculating base cost");
		tempTaskGroup.Cleanup();
		schedule = CalculateScheduleFromTaems(tempTaskGroup);
		return schedule.TotalQuality;
	}
		
	//A public method to feed new tasks to the scheduler
	public void AddTask(Task pendingTask)
	{
		Main.Message(debugFlag, "[Scheduler 62] Added Pending task " + pendingTask.label + " to Scheduler " + this.hashCode());
		PendingTasks.add(pendingTask);
	}
	
	//This is the main method of the scheduler, which implements runnable interface of java thread
	//It will run continuously when the scheduler thread is started. It will initially calculate the schedule
	//for current taskGroup, and then subsequently keep checking for arrival of new pending tasks, so that
	//the schedule is updated whenever they arrive
	@Override
	public void run() {
		while(true)
		{
			//Do nothing for now. Calling this statically.
		}
	}
	
	public Schedule RunStatic()
	{
		try {
			//Read all new tasks
			int numberOfPendingTasks = this.PendingTasks.size();
			if (numberOfPendingTasks<=0) return null;
			String debugMessage = "";
			for(int i=0;i<numberOfPendingTasks;i++)
			{
				Task newTask = this.PendingTasks.get(0);
				debugMessage += " > " + newTask.label;
				this.PendingTasks.remove(0);
				if (newTask.agent.equals(agent)){
					taskGroup.addTask(newTask);
					Main.Message(debugFlag, "[Scheduler 95] task added " + newTask.label + " in " + agent.getName());
					
				}
			}
			Main.Message(debugFlag, "[Scheduler 95] Pending Tasks found " + debugMessage + " for " + agent.getName());
			//Remove completed tasks
			taskGroup.Cleanup();
			if(taskGroup.hasChildren())
			{
				schedule = CalculateScheduleFromTaems(taskGroup);
				Main.Message(debugFlag, "[Scheduler 94] " + schedule.toString());
				return schedule;
				/*for(IScheduleUpdateEventListener listener: listeners)
				{
					listener.HandleScheduleEvent(new ScheduleUpdateEvent(schedule));
				}*/
			}
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return null;
	}

	//Method takes a Teams structure as input and outputs all the possible schedules resulting from that
	//task structure. This output is then fed to a generic Dijkstra's algorithm to calculate the optimum schedule
	//corresponding to the optimum path from the starting task to the ending task
	private Schedule CalculateScheduleFromTaems(Task topLevelTask)
	{
		Iterator ii = topLevelTask.getSubtasks();
		while(ii.hasNext())
		{
			Main.Message(debugFlag, "[Scheduler 123] " + ((Node)ii.next()).label);
		}
		//Reinitialize the schedule item
	  	schedule = new Schedule();
	  	//Reinitialize the start time of calculation
	  	startTime = new Date();
	
		//Create set of Nodes representing all methods that can be executed in the eventual schedule
		ArrayList<Method> nodes = new ArrayList<Method>();
		//Create set of all possible transitions of execution from one method to another, which represents an actual
		//pass through a possible schedule
		ArrayList<MethodTransition> edges = new ArrayList<MethodTransition>();
		//Add an initial node, which will act as our starting point
		Vector2D agentPos = topLevelTask.agent.getPosition();
		Method initialMethod = new Method("Starting Point",0,agentPos.x,agentPos.y);
		nodes.add(initialMethod);
		Method finalMethod = new Method("Final Point",0,agentPos.x,agentPos.y);
		nodes.add(finalMethod);
		//Append all possible schedule options to this set, after parsing the input Taems structure
		Method[] finalMethodList = AppendAllMethodExecutionRoutes(nodes, edges, topLevelTask, new Method[]{initialMethod}, null);
		for(int i=0;i<finalMethodList.length;i++)
		{
			MethodTransition t = new MethodTransition(
					"From " + finalMethodList[i].label + " to " + finalMethod.label, 
					finalMethodList[i], finalMethod);
			Main.Message(debugFlag, "[Scheduler 135]" + t.getId());
			edges.add(t);
		}
		
		//Create a Graph of these methods and run Dijkstra Algorithm on it
		Graph graph = new Graph(nodes, edges);
		graph.Print();
	    DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
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
		    		Main.Message(true, "[Scheduler 167] " + vertex.label + " " + vertex.getOutcome().getQuality());
		        }
		    }
	    schedule.TotalQuality = totalquality;
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
			m += s.label;
		}
		Main.Message(debugFlag, "[Scheduler 185]" + m );
	}
	
	//A helper method used internally by CalculateScheduleFromTaems method
	private Method[] AppendAllMethodExecutionRoutes(ArrayList<Method> nodes, ArrayList<MethodTransition> edges, Node task,
			Method[] appendTo, Node Parent)
	{
		Main.Message(debugFlag, "[Scheduler 185] Calculating subroutes for " + task.label + " " + nodes.size());
		ArrayList<Method> lastMethodList = new ArrayList<Method>();
		for(int mIndex = 0; mIndex<appendTo.length; mIndex++)
		{
			//For subtasks, look at the relevant QAF, which will constrain how the tasks must be scheduled (and executed)
			Method lastMethod = appendTo[mIndex];
			Main.Message(debugFlag, "[Scheduler 189] Routes to append to " + lastMethod.label);
			if (!task.IsTask())
			{
				Method m = new Method((Method)task);
				m.AddObserver(Parent);
				nodes.add(m);
				MethodTransition t = new MethodTransition("From " + lastMethod.label + " to " + m.label, lastMethod, m);
				Main.Message(debugFlag, "[Scheduler 200] Route "+t.getId());
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
						localLastMethodList = AppendAllMethodExecutionRoutes(nodes, edges, subtask, localLastMethodList, tk);
					}
					for(int i=0;i<localLastMethodList.length;i++)
					{
						if (!lastMethodList.contains(localLastMethodList[i]))
							lastMethodList.add(localLastMethodList[i]);
						Main.Message(debugFlag, "[Scheduler 222] Local Last Method " + localLastMethodList[i].label);
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
						Main.Message(debugFlag, "[Scheduler 244] " + subtask.label);
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
						Main.Message(debugFlag, "[Scheduler] Permuted " + permutations.size() + " SumAllQAF for appending to " + tk.label);
						for(int i=0;i<s.length;i++)
						{
							String debugMessage = "";
							for(Node n : s)
							{
								debugMessage += n.label;
							}
							Main.Message(debugFlag, debugMessage);
						}
						Method[] m = new Method[]{lastMethod};
						for(int i=0;i<s.length;i++)
						{
							permutationLinkMethodsList = AppendAllMethodExecutionRoutes(nodes, edges, s[i], permutationLinkMethodsList, tk);
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
						localLastMethodList = AppendAllMethodExecutionRoutes(nodes, edges, subtask, localLastMethodList, tk);
						for(int i=0;i<localLastMethodList.length;i++)
						{
							lastMethodList.add(localLastMethodList[i]);
						}
					}
				}
			}
		}
		Method[] result = lastMethodList.toArray(new Method[lastMethodList.size()]);
		return result;
	}

}


