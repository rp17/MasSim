package masSim.schedule;

import java.util.ArrayList;
import java.util.Iterator;

import masSim.taems.*;
import masSim.taems.ScheduleElement;

import java.util.*;

public class Scheduler implements Runnable {
	
	//Represents the start time when this schedule is being calculated, 
	public static Date startTime = new Date();
	
	//New pending tasks that need to be added to the taskGroup, whose schedule needs to be calculated, usually
	//during runtime when a previous schedule has already been calculated and is available, but which needs
	//to be updated now with arrival of these new tasks
	public ArrayList<Task> PendingTasks = new ArrayList<Task>();
	
	//Represents the top level task, called task group in taems, which contains all child tasks to be scheduled
	private Task taskGroup = new Task("Task Group",new SumAllQAF(), null);
	
	//Represents the current final optimum schedule calculated for the taskGroup member
	private Schedule schedule;
	
	//Collection of schedule event listeners, who will be notified when the schedule changes
	private ArrayList<IScheduleUpdateEventListener> listeners = new ArrayList<IScheduleUpdateEventListener>();
	
	//A public method to feed new tasks to the scheduler
	public void AddScheduleUpdateEventListener(IScheduleUpdateEventListener listener)
	{
		listeners.add(listener);
	}
		
	//A public method to feed new tasks to the scheduler
	public void AddTasks(ArrayList<Task> pendingTasks)
	{
		PendingTasks.clear();
		PendingTasks.addAll(pendingTasks);
	}
	
	//This is the main method of the scheduler, which implements runnable interface of java thread
	//It will run continuously when the scheduler thread is started. It will initially calculate the schedule
	//for current taskGroup, and then subsequently keep checking for arrival of new pending tasks, so that
	//the schedule is updated whenever they arrive
	@Override
	public void run() {
		while(true)
		{
			try {
				//Read all new tasks
				int numberOfPendingTasks = this.PendingTasks.size();
				for(int i=0;i<numberOfPendingTasks;i++)
				{
					Task newTask = this.PendingTasks.get(0);
					this.PendingTasks.remove(0);
					taskGroup.addTask(newTask);	
				}
				//Remove completed tasks
				taskGroup.Cleanup();
				if(taskGroup.hasChildren())
				{
					schedule = CalculateScheduleFromTaems(taskGroup);
					for(IScheduleUpdateEventListener listener: listeners)
					{
						listener.HandleScheduleEvent(new ScheduleUpdateEvent(schedule));
					}
				}
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	//Method takes a Teams structure as input and outputs all the possible schedules resulting from that
	//task structure. This output is then fed to a generic Dijkstra's algorithm to calculate the optimum schedule
	//corresponding to the optimum path from the starting task to the ending task
	private Schedule CalculateScheduleFromTaems(Task topLevelTask)
	{
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
		Method initialMethod = new Method("Dummy Starting Point",0,0,0);
		nodes.add(initialMethod);
		Method finalMethod = new Method("Dummy Final Point",0,0,0);
		nodes.add(finalMethod);
		//Append all possible schedule options to this set, after parsing the input Taems structure
		Method[] finalMethodList = AppendAllMethodExecutionRoutes(nodes, edges, topLevelTask, new Method[]{initialMethod}, null);
		for(int i=0;i<finalMethodList.length;i++)
		{
			MethodTransition t = new MethodTransition(
					"From " + finalMethodList[i].label + " to " + finalMethod.label, 
					finalMethodList[i], finalMethod);
			edges.add(t);
		}
		
		//Create a Graph of these methods and run Dijkstra Algorithm on it
		Graph graph = new Graph(nodes, edges);
		//graph.Print();
	    DijkstraAlgorithm dijkstra = new DijkstraAlgorithm(graph);
	    dijkstra.execute(initialMethod);
	    LinkedList<Method> path = dijkstra.getPath(finalMethod);
	    //Print the determined schedule
	    int totalquality = 0;
	    for (Method vertex : path) {
	        totalquality += vertex.getOutcome().getQuality();
	        schedule.addItem(new masSim.taems.ScheduleElement(vertex));
	    }
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
	
	//A helper method used internally by CalculateScheduleFromTaems method
	private Method[] AppendAllMethodExecutionRoutes(ArrayList<Method> nodes, ArrayList<MethodTransition> edges, Node task,
			Method[] appendTo, Node Parent)
	{
		ArrayList<Method> lastMethodList = new ArrayList<Method>();
		for(int mIndex = 0; mIndex<appendTo.length; mIndex++)
		{
			//For subtasks, look at the relevant QAF, which will constrain how the tasks must be scheduled (and executed)
			Method lastMethod = appendTo[mIndex];
			if (!task.IsTask())
			{
				Method m = new Method((Method)task);
				m.AddObserver(Parent);
				nodes.add(m);
				MethodTransition t = new MethodTransition(
					"From " + lastMethod.label + " to " + m.label,
					lastMethod, m);
				edges.add(t);
				lastMethodList.add(m);
			}
			else
			{
				Method[] localLastMethodList = new Method[]{lastMethod};
				Task tk = (Task)task;
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
						lastMethodList.add(localLastMethodList[i]);
					}
				}
				if (qaf instanceof SumAllQAF)
				{
					//All tasks must be executed, though not necessarily in sequence
					//Create task list whose permutations need to be found
					ArrayList<Node> subtasksList = new ArrayList<Node>();
					for(Iterator<Node> subtasks = tk.getSubtasks(); subtasks.hasNext(); ) {
						Node subtask = subtasks.next();
						subtasksList.add(subtask);
					}
					Node[] subTaskListForSumPermutation = subtasksList.toArray(new Node[subtasksList.size()]);
					//Find permutations for this task list
					ArrayList<Node[]> permutations = new ArrayList<Node[]>(); 
					Permute(subTaskListForSumPermutation,0,subTaskListForSumPermutation.length-1,permutations);
					//Now create paths for each permutation possible
					for (Node[] s : permutations)
					{
						Method[] m = new Method[]{lastMethod};
						for(int i=0;i<s.length;i++)
						{
							localLastMethodList = AppendAllMethodExecutionRoutes(nodes, edges, s[i], localLastMethodList, tk);
							//nodes.add(lastMethod);
						}
						for(int i=0;i<localLastMethodList.length;i++)
						{
							lastMethodList.add(localLastMethodList[i]);
						}
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


