package masSim.world;

import masSim.schedule.IScheduleUpdateEventListener;
import masSim.schedule.MaxSumCalculator;
import masSim.schedule.ScheduleUpdateEvent;
import masSim.schedule.Scheduler;
import masSim.schedule.SchedulingCommandType;
import masSim.schedule.SchedulingEvent;
import masSim.schedule.SchedulingEventListener;
import masSim.schedule.SchedulingEventParams;
import masSim.taems.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
//import java.io.IOException;
import java.util.*;
//import java.util.AbstractMap.SimpleEntry;
//import java.util.concurrent.Callable;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.ConcurrentLinkedQueue;
//import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Aspect.PredicateParameterFilter;
import Aspect.StatementEvent;
import BAEventMonitor.Event;
import BAEventMonitor.Execution;
import BAEventMonitor.Param;
import BAEventMonitor.Execution.ExecutionMode;
import BAEventMonitor.Param.StoreMode;

//import java.util.concurrent.Future;
//import java.util.concurrent.FutureTask;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.TimeoutException;
//import java.util.concurrent.atomic.AtomicReference;

import raven.Main;
import raven.TaskIssuer;
//import raven.game.RoverBot;
//import raven.game.RoverBot;
import raven.game.Waypoints;
import raven.game.Waypoints.Wpt;
import masSim.goals.GoalComposite;
import raven.math.Vector2D;
import raven.ui.RavenUI;
//import raven.ui.RavenUI;
//import raven.utils.SchedulingLog;

public class Agent extends BaseElement implements IAgent, IScheduleUpdateEventListener, SchedulingEventListener, Runnable{


	private Map<String, List<String>> mapTaskToMethods = new HashMap<String, List<String>>();
	private Map<String, String> mapMethodToRootTask = new HashMap<String, String>();

	private SimBot simBot;
	volatile Waypoints wpts = new Waypoints();
	private final static String schedulingEventListenerName = "RavenUI";
	public boolean noUI = true;
	private boolean debugFlag = true;
	private boolean errorFlag = true;
	private boolean scenarioDone = false;
	private static int GloballyUniqueAgentId = 1;
	private int code;
	private long lapsedTime;


	// MQTT thread pool and mqtt reference
	private final static ExecutorService commsPool = Executors.newSingleThreadExecutor();
	private volatile MqttMessagingProvider mqReceiver;


	//private Schedule currentSchedule = new Schedule();

	public Schedule currentSchedule = new Schedule();

	private List<String> methodsList = new ArrayList<String>();

	private int taskInd;
	//private boolean resetScheduleExecutionFlag = false;

	private ArrayList<String> agentsUnderManagement = null;

	ArrayList<MaxSumCalculator> negotiations = new ArrayList<MaxSumCalculator>();

	private AgentMode mode;
	public double x;
	public double y;
	//public boolean flagScheduleRecalculateRequired;
	public Queue<Method> queue = new LinkedList<Method>();
	//Represents the top level task, called task group in taems, which contains all child tasks to be scheduled for this agent
	private Task currentTaskGroup;
	//New pending tasks that need to be added to the taskGroup, whose schedule needs to be calculated, usually
	//during runtime when a previous schedule has already been calculated and is available, but which needs
	//to be updated now with arrival of these new tasks
	private List<Task> pendingTasks = new ArrayList<Task>();
	private MqttMessagingProvider mq;
	private TaskRepository taskRepository = new TaskRepository();
	private ExecutorService schedulerPool;
	private Scheduler localScheduler;
	private Method currentMethod = null;
	//Represents the current final optimum schedule calculated for the taskGroup member
	private Schedule schedule;

	public static void main(String[] args) {
		//Agent to be run via this method in its own jvm
	}

	@Override
	public synchronized List<Task> getPendingTasks()
	{
		return this.pendingTasks;
	}

	public ArrayList<String> getAgentsUnderManagement()
	{
		return agentsUnderManagement;
	}

	private enum Status {
		IDLE, PROCESSNG, EMPTY, AWAITINGTASKCOMPLETION
	}

	@Override
	public String getName()
	{
		return this.label;
	}

	/** alive, dead or spawning? */
	private Status status;
	/*
	public Agent(int newCode){
		this(newCode,"Agent"+newCode,false,0,0);
	}
	 */
	public Agent(String name, boolean isManagingAgent, int x, int y,
			MqttMessagingProvider mq){
		this(GloballyUniqueAgentId++,name, isManagingAgent, x, y, mq);
		lapsedTime = 0;
	}

	public Agent(int newCode, String label, boolean isManagingAgent, int x, int y, final MqttMessagingProvider mq){
		this.code = newCode;
		this.label = label;
		taskInd = 0;
		status = Status.EMPTY;
		this.x = x;
		this.y = y;
		if (isManagingAgent) agentsUnderManagement = new ArrayList<String>(2);


		//this.mq.SubscribeForAgent(TaskIssuer.TaskIssuerName); 

		//schedulerPool = Executors.newFixedThreadPool(3);
		currentTaskGroup = new Task("Task Group",new SumAllQAF(), this);
		taskRepository.ReadTaskDescriptions(getName()+".xml");
		this.schedulerPool = Executors.newFixedThreadPool(2);
		localScheduler = new Scheduler(this);
		this.mq = mq;

		/*
		commsPool.execute( new Runnable(){
			@Override
			public void run() {
				mq.SubscribeForAgent(TaskIssuer.TaskIssuerName);
				//mqReceiver.SubscribeForAgent(polName);
				//mqReceiver.SubscribeForAgent(ambName);
			}
		});
		 */

	}

	private void reinitAgent() {
		taskInd = 0;
		status = Status.EMPTY;
		//flagScheduleRecalculateRequired = true;
		pendingTasks.clear();
		currentTaskGroup = new Task("Task Group",new SumAllQAF(), this);
		currentSchedule = new Schedule();
		taskRepository.ReadTaskDescriptions(getName()+".xml");
	}

	public void startEventProcessing() {

		this.mq.AddListener(this); // start listening for events from its own instance of MqttMessagingProvider
		//this.mq = MqttMessagingProvider.GetMqttProvider();
		this.mq.SubscribeForAgent(label);// needs to listen to topic of the agent's own name to which TaskIssuer posts task assignments
	}
	public void setBot(SimBot bot) {
		this.simBot = bot;
	}

	//A public method to feed new tasks to the scheduler
	@Event(name="agent was chosen to reach a way-point")
	@Param(name="choice", variable="task", pred="scheduleOptimal", mode=StoreMode.List)
	public void AssignTask(String taskName)
	{
		Main.Message(this, debugFlag, "[Scheduler 62] Added Pending task " + taskName + " to Scheduler " + this.label);
		Task task = this.taskRepository.GetTask(taskName);

		//Instrumentation
		//PredicateParameterFilter.addChoiceTask(task);

		if (task==null) Main.Message(true, "Error: Task " + taskName + " not found in tasks repository");
		task.AssignAgent(this);

		List<String> methodNames = new ArrayList<String>();

		System.out.println("Task name " + taskName + " was registered");
		RegisterChildrenWithUI(task, methodNames);

		mapTaskToMethods.put(taskName, methodNames);

		for(String methodN : methodNames) {
			mapMethodToRootTask.put(methodN, taskName);
		}

		this.pendingTasks.add(task);

		schedulerPool.execute(localScheduler);

		//Instrumentation for distributed event
		//StatementEvent.addTasks(task);
		//schedulerPool.execute(localScheduler);
	}

	private boolean IsManagingAgent()
	{
		if(agentsUnderManagement == null) return false;
		else
			return this.agentsUnderManagement.size()>0;
	}

	public void CalculateCost(Task task)
	{
		long start = LapsedTime.getStart();
		int[] costs = CalculateIncrementalQualitiesForTask(task);
		String costsString = costs[0] + SchedulingEventParams.DataItemSeparator + costs[1];
		SchedulingEventParams params = new SchedulingEventParams()
		.AddTaskName(task.getLabel())
		.AddAgentId(getName())
		.AddData(costsString);
		SchedulingEvent event = new SchedulingEvent(TaskIssuer.polName, SchedulingCommandType.COSTBROADCAST, params);
		mq.PublishMessage(event);
		lapsedTime = lapsedTime + LapsedTime.getLapsed(start);
	}

	public void Negotiate(Task task)
	{
		long startTime = LapsedTime.getStart();
		if (IsManagingAgent())
		{		
			MaxSumCalculator maxSumInstance = new MaxSumCalculator(task.label,this.agentsUnderManagement.size()+1);//One additional for managing agent
			int[] costs = CalculateIncrementalQualitiesForTask(task);
			maxSumInstance.AddCostData(this.label, costs[0], costs[1]);
			this.negotiations.add(maxSumInstance);
			for(String agName : this.getAgentsUnderManagement())
			{
				SchedulingEventParams params = new SchedulingEventParams()
				.AddTaskName(task.getLabel())
				.AddAgentId(agName)
				.AddOriginatingAgent(this.label);
				SchedulingEvent event = new SchedulingEvent(agName, SchedulingCommandType.CALCULATECOST, params);
				mq.PublishMessage(event);

			}
			/*			SchedulingEventParams params = new SchedulingEventParams()
			.AddTaskName(task.getLabel())
			.AddAgentId(this.getName())
			.AddOriginatingAgent(this.label);
			SchedulingEvent managingEvent = new SchedulingEvent(this.getName(), SchedulingCommandType.CALCULATECOST, params);
			mq.PublishMessage(managingEvent);*/
			lapsedTime = lapsedTime + LapsedTime.getLapsed(startTime);
		}
	}

	public MaxSumCalculator GetMaxSumCalculatorForTask(String taskName)
	{

		Main.Message(debugFlag, "[Agent 168] Finding Best Agent for " + taskName);
		/*
		StringBuilder cop = new StringBuilder("AGENT 1");
		cop.append("VARIABLE 1 1 3");
		cop.append("VARIABLE 0 1 3");
		cop.append("CONSTRAINT 1 1 0 1");
		cop.append("F 2 2 13");
		cop.append("F 2 1 13");
		cop.append("F 2 0 13");
		cop.append("F 1 2 13");
		cop.append("F 1 1 13");
		cop.append("F 1 0 13");
		cop.append("F 0 2 13");
		cop.append("F 0 1 14");
		cop.append("F 0 0 8");
		 */
		//test.Main jmaxMain = new test.Main();
		//ArrayList<SimpleEntry<String, String>> result = jmaxMain.CalculateMaxSumAssignments(cop.toString());
		//Calculate which agent is best to assign

		for(MaxSumCalculator cal : this.negotiations)
		{
			if (cal.getTaskName().equalsIgnoreCase(taskName)) {
				return cal;
			}
		}
		return null;
	}


	/*

	private int getIncrementalQualityWhenThisAgentIsAssignedAnExtraTask(Task task, IAgent agent)
	{
		int result = 0;
		try {
			//Future<Integer> baseQualityFuture = getExpectedScheduleQuality(null, this);
			//Future<Integer> incrementalQualityFuture = getExpectedScheduleQuality(task, this);
			Main.Message(true, "----------one");
			//int base = baseQualityFuture.get(10, TimeUnit.SECONDS);
			int base = getExpectedScheduleQuality(null, this);
			Main.Message(true, base + "");
			//int incremental = incrementalQualityFuture.get(10, TimeUnit.SECONDS);
			int incremental = getExpectedScheduleQuality(task, this);
			result = incremental-base;
			Main.Message(debugFlag, "Quality increase for " + agent.getName() + " if assigned "	+ task.getLabel() + " is " + result);
		} catch ( Exception e//IOException | InterruptedException | ExecutionException| TimeoutException e
				) {
			Main.Message(debugFlag, "Failed to obtain Quality increase for " + agent.getName() + " if assigned "	+ task.getLabel() + ". Using default of 0");
			return 0;
	 */
	public void ProcessCostBroadcast(String taskName, String agentName, String data)
	{	
		long startTime = LapsedTime.getStart();
		if(IsManagingAgent()) {
			String[] arr = data.split(SchedulingEventParams.DataItemSeparator,2);
			MaxSumCalculator calc = GetMaxSumCalculatorForTask(taskName);
			if(calc == null) {
				System.out.println("calc is null");
			}
			calc.AddCostData(agentName, Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
			if (calc.IsDataCollectionComplete())
			{
				String selectedAgentName = calc.GetBestAgent();
				System.out.println("Agent.ProcessCostBroadcast : selectedAgentName is " + selectedAgentName);
				SchedulingEventParams params = new SchedulingEventParams()
				.AddTaskName(taskName)
				.AddAgentId(selectedAgentName);
				SchedulingEvent event = new SchedulingEvent(selectedAgentName, SchedulingCommandType.ASSIGNTASK, params);
				mq.PublishMessage(event);
				lapsedTime = lapsedTime + LapsedTime.getLapsed(startTime);
			}

		} else {
			//
		}
	}

	private int[] CalculateIncrementalQualitiesForTask(Task task)
	{
		try
		{
			if (task==null) throw new Exception ("Calculate Incremental Qualities called for a null task");
			IAgent previousAgent = task.agent;//Save previous agent, because assignment of agent will change while calculating costs and need to be reset
			task.agent = this;
			int base = GetScheduleCostSync(task, this).TotalQuality;
			//Reset agent change for done for calculation
			task.agent = previousAgent;
			int incremental = GetScheduleCostSync(null, this).TotalQuality;
			Main.Message(debugFlag, getName() + " for task " + task.getLabel() + " Base " + base + " Incremental " + incremental);
			return new int[]{base,incremental};	
		}
		catch(Exception e)
		{
			Main.Message(this, true, e.getMessage() + " " + e.getStackTrace());
			return new int[]{0,0};
		}	
	}

	public synchronized Schedule GetScheduleCostSync(Task task, IAgent taskAgent)
	{
		//Make a copy
		if (task!=null) taskAgent = task.agent;
		Task tempTaskGroup = new Task("Task Group",new SumAllQAF(), taskAgent);
		Iterator<Node> copyTasks = this.GetCurrentTasks().getSubtasks();
		while(copyTasks.hasNext())
		{
			tempTaskGroup.addTask(copyTasks.next());
		}
		//Sometimes we want to calculate base cost of executing existing tasks, without assigning a new one, where this
		//method will be called with a null value. So this check is necessary
		if (task!=null)
		{
			tempTaskGroup.addTask(task);
		}
		//tempTaskGroup.Cleanup(MqttMessagingProvider.GetMqttProvider());
		tempTaskGroup.Cleanup(mq);
		this.schedule = this.localScheduler.CalculateScheduleFromTaems(tempTaskGroup);
		//send schedule quality back to mqtt
		//this.mq.PublishMessage(RavenUI.schedulingEventListenerName,SchedulingCommandType.PUBLISHCOST, new SchedulingEventParams().AddMethodId(currentMethod.label).AddXCoord(currentMethod.x).AddYCoord(currentMethod.y).toString());

		return schedule;

	}

	@Override
	public Task GetCurrentTasks()
	{
		return currentTaskGroup;
	}

	public void RunSchedular()
	{
		schedulerPool.execute(localScheduler);
	}

	public synchronized boolean AreEnablersInPlace(Method m)
	{
		boolean methodEnablersCompleted = false;
		if (m.Interrelationships.size()>0)
		{
			for(Interrelationship ir: m.Interrelationships)
			{
				if (!ir.from.IsTask())
				{
					Method from = (Method)ir.from;
					for(Method mc : WorldState.CompletedMethods)
					{
						if (mc.label==from.label)
							methodEnablersCompleted = true;
					}
				}
				else
				{
					Task from = (Task)ir.from;
					for(Task mc : WorldState.CompletedTasks)
					{
						if (mc.label==from.label)
							methodEnablersCompleted = true;
					}
				}
			}	
		}
		else
		{
			methodEnablersCompleted = true;
		}
		return methodEnablersCompleted;
	}

	public void ExecuteTask(Method m) throws InterruptedException
	{
		System.out.println("Agent.ExecuteTask " + label + ": starting execution of " + m.label);
		while (!AreEnablersInPlace(m))
		{
			Main.Message(debugFlag, "[Agent 88] " + m.label + " enabler not in place. Waiting...");
			Thread.sleep(1000);
		}
		Main.Message(this, debugFlag, "Agent " + this.label + " executing " + m.label);
		boolean isComplete = WorldState.NamesCompletedMethods.contains(m.getLabel());
		if (!isComplete && m.x!=0 && m.y!=0)
		//if (m.x!=0 && m.y!=0)
		{
			status=Status.AWAITINGTASKCOMPLETION;
			this.currentMethod = m;

			// the event SchedulingCommandType.DISPLAYTASKEXECUTION make RavenUI assign a pid traverse goal to a bot
			// instead, this assignment must be done directly to SimBot associated with this Agent
			/*
			if(m.label.equals("Finish")) {
				System.out.println("Agent.ExecuteTask " + label + ": IGNORING execution of " + m.label);
				//this.currentMethod = null;
				MarkMethodCompleted(m.label);
			}
			else {
				Waypoints matchedWaypoints = getWptsForMethodExecution(m.label, simBot);
				GoalComposite g = simBot.addWptsGoal(matchedWaypoints, m.label);

				fireSchedulingEvent(Agent.schedulingEventListenerName, SchedulingCommandType.DISPLAYTASKEXECUTION, this.getName(), m.label, m.x, m.y);
				this.flagScheduleRecalculateRequired = false;
			}
			 */
			Waypoints matchedWaypoints = getWptsForMethodExecution(m.label, simBot);

			GoalComposite g = simBot.addWptsGoal(matchedWaypoints, m.label);

			fireSchedulingEvent(Agent.schedulingEventListenerName, SchedulingCommandType.DISPLAYTASKEXECUTION, this.getName(), m.label, m.x, m.y);
			//this.flagScheduleRecalculateRequired = false;

		} else {
					System.out.println("Agent.ExecuteTask() - Method " + m.getLabel() + " has already been completed, ignoring execution");
					if (currentSchedule!=null)
					{
						Iterator<ScheduleElement> el = currentSchedule.getItems();
						while(el.hasNext())
						{
							ScheduleElement e = el.next();
							if (m.label.equals(e.getMethod().label))
							{
								currentSchedule.RemoveElement(e);
								WorldState.NamesCompletedMethods.remove(m.label);
								Main.Message(debugFlag, "Agent.ExecuteTask() - Removed " + e.getName() + e.hashCode() + " from schedule " + currentSchedule.hashCode());
							}
						}
					}}
	}

	public Waypoints getWptsForMethodExecution(String methodName, SimBot bot)
	{
		//Main.Message(debugFlag, "[RavenGame 790] getting waypoints for " + methodName );
		Vector2D currentPosition = bot.pos();
		Waypoints local = new Waypoints();
		String waypointNamesForDebugging = "";

		/* why this loop ? HashMap much better
		for(int i=0;i<wpts.size();i++)
		{
			Waypoints.Wpt wp = wpts.get(i);
			waypointNamesForDebugging += wp.name + ".";
			//Main.Message(debugFlag, "[RavenGame 790] Testing waypoint " + wp.name );
			if (wp.name.equals(methodName))
			{
				local.addWpt(new Vector2D(currentPosition.x, currentPosition.y));
				local.addWpt(new Vector2D(wp.x, wp.y));
				break;
			}
		}
		 */
		System.out.println("Agent.getWptsForMethodExecution : methodName is : " + methodName);
		Wpt wpt = wpts.get(methodName);
		if(wpt != null) {
			local.addWpt(new Vector2D(currentPosition.x, currentPosition.y));
			local.addWpt(new Vector2D(wpt.x, wpt.y));
		} else {
			System.out.println("Wpt is null");
		}
		if (local.size()==0 && !methodName.equalsIgnoreCase(Method.FinalPoint))
		{
			waypointNamesForDebugging = "Possible Error: " + methodName + " not found in " + waypointNamesForDebugging + " by " + bot.name;
			Main.Message(this, true, waypointNamesForDebugging);
		}
		return local;
	}

	@Override

	@Execution(name="reachWaypoint", mode=ExecutionMode.After)
	@Param(name="execution", variable="currentMethod", pred="reachWaypoint", mode=StoreMode.List)

	public void MarkMethodCompleted(String methodName)
	{
		Main.Message(debugFlag, "Agent.MarkMethodCompleted " + label + " notified of completion of method " + methodName + " from Goal_PidTraverseEdge");
		if (currentMethod != null && currentMethod.label.equalsIgnoreCase(methodName))
		{

			//schedule.get().RemoveElement(e);Does this need to be done?
			currentMethod.MarkCompleted();
			methodsList.remove(methodName);
			//instrumentation
			//PredicateParameterFilter.addMethod(currentMethod);

			//schedule.get().RemoveElement(e);Does this need to be done?
			//currentMethod.MarkCompleted();

			//instrumentation
			//spec 2
			//StatementEvent.evaluateReachAWayPoint();

			WorldState.CompletedMethods.add(currentMethod);
			WorldState.NamesCompletedMethods.add(methodName);
			Main.Message(debugFlag, "[Agent 130] " + currentMethod.label + " marked completed");


			if (currentSchedule!=null)
			{
				Iterator<ScheduleElement> el = currentSchedule.getItems();
				while(el.hasNext())
				{
					ScheduleElement e = el.next();
					if (e.getMethod().label.equals(Method.StartingPoint) && el.hasNext())
						e = el.next();
					if (currentMethod.equals(e.getMethod()))
					{
						currentSchedule.RemoveElement(e);
						Main.Message(debugFlag, "[Agent 135] Removed " + e.getName() + e.hashCode() + " from schedule " + currentSchedule.hashCode());
					}
				}
			}
			wpts.removeWpt(currentMethod.label);

			String taskName = mapMethodToRootTask.get(methodName);
			List<String> methodNames = mapTaskToMethods.get(taskName);
			methodNames.remove(methodName);
			mapMethodToRootTask.remove(methodName);
			if(methodNames.size() == 0) {
				Main.Message(debugFlag, "Agent.MarkMethodCompleted " + label + " all methods of task " + taskName + " have been completed");
				Main.Message(debugFlag, "Agent.MarkMethodCompleted " + label + " task " + taskName + " is completed");
				mapTaskToMethods.remove(taskName);

				// sending an event about TASKCOMPLETION to TaskIssuer
				SchedulingEventParams params = new SchedulingEventParams().AddAgentId(label)
						.AddTaskName(taskName);
				SchedulingEvent event = new SchedulingEvent(TaskIssuer.TaskIssuerName, SchedulingCommandType.TASKCOMPLETED, params);
				mq.publishMessage(event.rawMessage, 2);
			}

			if(mapTaskToMethods.size() == 0) {
				scenarioDone = true;
				Main.Message(debugFlag, "Agent.MarkMethodCompleted " + label + " all tasks have been completed");
				Main.Message(debugFlag, "Agent.MarkMethodCompleted " + label + " re-initializing");
				reinitAgent();
			}

			Main.Message(debugFlag, "Agent.MarkMethodCompleted " + label + " waypoints left " + wpts.size());
			for(int i = 0; i < wpts.size(); i++) {
				Waypoints.Wpt wp = wpts.get(i);
				Main.Message(debugFlag, "Agent.MarkMethodCompleted " + label + " left waypoint " + i + " : " + wp.name);
			}
			Main.Message(debugFlag, "Agent.MarkMethodCompleted " + label + " methods left " + methodsList.size());
			for(int i = 0; i < methodsList.size(); i++) {
				Main.Message(debugFlag, "Agent.MarkMethodCompleted " + label + " left method " + i + " : " + methodsList.get(i));
			}


			if(wpts.size() == 0) {
				Main.Message(debugFlag, "Agent.MarkMethodCompleted " + label + " NO MORE WAYPOINTS ");
			}
			//this.mq.PublishMessage(Agent.schedulingEventListenerName,SchedulingCommandType.DISPLAYREMOVEMETHOD, new SchedulingEventParams().AddMethodId(currentMethod.label).AddXCoord(currentMethod.x).AddYCoord(currentMethod.y).toString());

			//flagScheduleRecalculateRequired = true;
			status=Status.PROCESSNG;
		}
	}

	public void fireSchedulingEvent(String destinationAgentId, SchedulingCommandType type, String subjectAgentId, String methodId, double x2, double y2) {

		if(destinationAgentId == Agent.schedulingEventListenerName && noUI) {return;} // since schedulingEventListenerName String is final static, reference comparison is enough
		else {
			//SchedulingEventParams params = new SchedulingEventParams(subjectAgentId, methodId, Double.toString(x2), Double.toString(y2), "");
			SchedulingEventParams params = new SchedulingEventParams().AddAgentId(subjectAgentId).AddMethodId(methodId)
					.AddXCoord(x2).AddYCoord(y2).AddTaskName("");
			SchedulingEvent worldEvent = new SchedulingEvent(destinationAgentId, type, params);
			mq.PublishMessage(worldEvent);


		}
	}

	// Returns identifying code, specific for this agent
	public int getCode(){
		return code;
	}

	public void UpdateSchedule(Schedule newSchedule)
	{
		//long startTime = LapsedTime.getStart();
		this.currentSchedule.Merge(newSchedule);
		//lapsedTime = lapsedTime + LapsedTime.getLapsed(startTime);
	}

	private void executeNextTask() {
		try
		{
			//Main.Message(true, "Agent.executeNextTask ");
			if (currentSchedule!=null && currentSchedule.size() > 0)
			{
				Iterator<ScheduleElement> el = currentSchedule.getItems();
				System.out.println("Agent.executeNextTask - Current Schedule is : " + currentSchedule.toString()); 
				if(el.hasNext())
				{
					Main.Message(true, "Executing tasks from schedule " + currentSchedule.hashCode());
					ScheduleElement e = el.next();
					if (e.getMethod().label.equals(Method.StartingPoint) && el.hasNext())
						e = el.next();
					else
						return;
					Main.Message(this, true, this.label +  " picked next task " + e.getName() + " " + e.hashCode() + " from schedule " + currentSchedule.hashCode());
					Method m = e.getMethod();
					ExecuteTask(m);
				}
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	public void RegisterChildrenWithUI(Node node, List<String> methodNames)
	{
		if (!node.IsTask())
		{
			Method method = (Method)node;
			wpts.addWpt(new Vector2D(method.x, method.y), method.getLabel());
			methodsList.add(method.label);
			methodNames.add(method.label);
			System.out.println("Agent.RegisterChildrenWithUI : Method " + methodNames + " was added to Wpts");
			fireSchedulingEvent(Agent.schedulingEventListenerName, SchedulingCommandType.DISPLAYADDMETHOD, this.getName(), method.getLabel(), method.x, method.y);
		}
		else
		{
			Iterator<Node> it = ((Task)node).getSubtasks();
			while(it.hasNext())
			{
				Node nd = it.next();
				RegisterChildrenWithUI(nd, methodNames);
			}
		}
	}

	public void update(int tick) {

		if(currentSchedule.hasNext(taskInd)) {
			ScheduleElement el = currentSchedule.peek();
			ScheduleElement.Status status = el.update(tick);
			if(status == ScheduleElement.Status.COMPLETED) {
				Main.Message(debugFlag, "Agent " + label + " completed item " + el.getName());
				currentSchedule.poll();
			}
		}
		else {
			Main.Message(debugFlag, "Agent " + label + " idle");
		}
	}

	@Override
	public void run() {
		fireSchedulingEvent(Agent.schedulingEventListenerName, SchedulingCommandType.DISPLAYADDAGENT, this.getName(), null, x, y);
		//		RunSchedular();
		status=Status.PROCESSNG;
		int i = 0;
		//TODO Introduce step to fetch commands from mqtt to govern execution and status
		while(true)
		{
			//Main.Message(this, true, this.label + " run() while loop iteration " + i + " status " + status);
			i++;
			if (status==Status.PROCESSNG){
				executeNextTask();
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void HandleScheduleEvent(ScheduleUpdateEvent scheduleUpdateEvent) {
		if (currentSchedule!=null)
			currentSchedule.Merge(scheduleUpdateEvent.Schedule);
		else
			currentSchedule = scheduleUpdateEvent.Schedule;
		Main.Message(this, true, this.label + " schedule updated with tasks " + currentSchedule.toString());
	}

	@Override
	public void AddChildAgent(String agentName){
		if (agentsUnderManagement==null)
			Main.Message(debugFlag, "Child Agent being added to non-managing agent");
		this.agentsUnderManagement.add(agentName);
	}

	@Override
	public void setPosition(Vector2D pos) {
		this.x = pos.x;
		this.y = pos.y;
	}

	@Override
	public Vector2D getPosition() {
		return new Vector2D(x,y);
	}

	@Override
	public AgentMode getMode() {
		return mode;
	}

	@Override
	public void setMode(AgentMode mode) {
		this.mode = mode;
	}

	@Override

	@Event(name="a task is added")

	public SchedulingEvent ProcessSchedulingEvent(SchedulingEvent event) {
		System.out.println("Agent.ProcessSchedulingEvent " + label + " received event " + event.commandType);
		if(event.agentName.equalsIgnoreCase(this.getName())) {
			if (event.commandType==SchedulingCommandType.ASSIGNTASK )
			{
				//StatementEvent.assignchoice();

				//Instrumentation
				//StatementEvent.getConsensus(event.params.TaskName);
				AssignTask(event.params.TaskName);

			}
			if (event.commandType==SchedulingCommandType.METHODCOMPLETED )
			{
				String completedMethodName = event.params.MethodId;

				if (completedMethodName!=null) {
					MarkMethodCompleted(completedMethodName);
					//Instrumentation
					//StatementEvent.getCompletedTasks(completedMethodName);
				}

			}
			if (event.commandType==SchedulingCommandType.NEGOTIATE )
			{
				Main.Message(debugFlag, "Task " + event.params.TaskName + " received for negotiation");
				Task task = this.taskRepository.GetTask(event.params.TaskName);

				//Instrumentation
				//StatementEvent.getConsensus(event.params.TaskName);
				this.Negotiate(task);
			}
			if (event.commandType==SchedulingCommandType.CALCULATECOST )
			{
				Task task = this.taskRepository.GetTask(event.params.TaskName);

				//Instrumentation
				//StatementEvent.getConsensus(event.params.TaskName);
				CalculateCost(task);
			}
			if (event.commandType==SchedulingCommandType.COSTBROADCAST )
			{
				//Instrumentation
				//StatementEvent.getConsensus(event.params.TaskName);
				ProcessCostBroadcast(event.params.TaskName, event.params.AgentId, event.params.Data);
			}
			if(event.commandType==SchedulingCommandType.INITMSG) 
			{
				System.out.println("Received event INITMSG");
				reinitAgent();
			}
			if(event.commandType==SchedulingCommandType.SHUTDOWN) {

				//SchedulingEvent evt = new SchedulingEvent(label, SchedulingCommandType.INITMSG, "re-init");
				//mq.publishMessage(evt.rawMessage, 2);

				/*
				commsPool.execute( new Runnable(){
					@Override
					public void run() {
						//SchedulingEvent evt = new SchedulingEvent(label, SchedulingCommandType.INITMSG, "re-init");
						//Main.Message(debugFlag, "Agent " + label + " sending " + evt.rawMessage);
						//mq.publishMessage(evt.rawMessage, 2);
						Main.Message(debugFlag, "Agent " + label + " sending INITMSG");
						mq.publishMessage("Ambulance,INITMSG,----re-init", 1);
					}
				});
				 */
				/*
				if(scenarioDone) {
					try {
						Main.Message(debugFlag, "Agent " + label + " scenario done, SHUTDOWN received, shutting down");
						Thread.sleep(250); // give time for message to be sent
						System.exit(0);
					}
					catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
					}
				}
				else {
					Main.Message(debugFlag, "Agent " + label + " scenario NOT done, SHUTDOWN received, but NOT shutting down");
				}
				 */

				Main.Message(debugFlag, "Agent " + label + " SHUTDOWN received, shutting down");
				getLapsedTime();
				double lt = lapsedTime * .001;
				System.out.println("LapsedTime = " + lt + " seconds");
				System.exit(0);
			}
		}

		return null;
	}

	public void getLapsedTime() {
		lapsedTime = localScheduler.lapsedTime + lapsedTime + currentSchedule.lapsedTime;
		
		String filePath = "C:" + "\\" + "Users" + "\\" + "k_h247" + "\\" + "Data" + "\\" + label;

		try {
			File file = new File(filePath);
			if(!file.exists()) {
				System.out.println("New file created");
				file.createNewFile();

			}
			FileWriter fw = new FileWriter(filePath, true);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write((double)lapsedTime * .001 + " seconds" + "\n");
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public boolean IsGlobalListener() {
		return false;
	}
}
