package masSim.world;

import masSim.schedule.AgentScheduleQualities;
import masSim.schedule.IScheduleUpdateEventListener;
import masSim.schedule.MaxSumCalculator;
import masSim.schedule.MultipleTaskScheduleQualities;
import masSim.schedule.ScheduleUpdateEvent;
import masSim.schedule.Scheduler;
import masSim.schedule.SchedulingCommandType;
import masSim.schedule.SchedulingEvent;
import masSim.schedule.SchedulingEventListener;
import masSim.schedule.SchedulingEventParams;
import masSim.schedule.TaskScheduleQualities;
import masSim.taems.*;

import java.io.IOException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import raven.Main;
import raven.ScenarioGenerator;
import raven.math.Vector2D;
import raven.ui.RavenUI;
import raven.utils.SchedulingLog;

public class Agent extends BaseElement implements IAgent, IScheduleUpdateEventListener, SchedulingEventListener, Runnable{

	private String negotiationInstance = "singleton";
	private boolean debugFlag = false;
	private boolean errorFlag = false;
	private static int GloballyUniqueAgentId = 1;
	private int code;
	private Schedule currentSchedule = new Schedule();
	private int taskInd;
	private boolean resetScheduleExecutionFlag = false;
	private ArrayList<IAgent> agentsUnderManagement = null;
	//ArrayList<MaxSumCalculator> negotiations = new ArrayList<MaxSumCalculator>();
	MaxSumCalculator negotiations = new MaxSumCalculator("singleton",0);
	private ConcurrentHashMap<String,String> completedMethods = new ConcurrentHashMap<String,String>();
	private AgentMode mode;
	public double x;
	public double y;
	public boolean flagScheduleRecalculateRequired;
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
	
	public ArrayList<IAgent> getAgentsUnderManagement()
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
	
	public int getAgentId(String agentName)
	{
		return Integer.parseInt(agentName.replaceAll("A", ""));
	}
	
	/** alive, dead or spawning? */
	private Status status;
	
	public Agent(int newCode){
		this(newCode,"Agent"+newCode,false,0,0);
	}
	
	public Agent(String name, boolean isManagingAgent, int x, int y,
			MqttMessagingProvider mq){
		this(GloballyUniqueAgentId++,name, isManagingAgent, x, y);
	}
	
	public Agent(int newCode, String label, boolean isManagingAgent, int x, int y){
		this.code = newCode;
		this.label = label;
		if (label.contains("-")) Main.Message(this, this.debugFlag, "Error: Agent name cannot contain a dash");
		taskInd = 0;
		status = Status.EMPTY;
		flagScheduleRecalculateRequired = true;
		this.x = x;
		this.y = y;
		if (isManagingAgent) agentsUnderManagement = new ArrayList<IAgent>();
		this.mq = MqttMessagingProvider.GetMqttProvider();
		this.mq.SubscribeForAgent(getName());
		this.mq.AddListener(this);
		//schedulerPool = Executors.newFixedThreadPool(3);
		currentTaskGroup = new Task("Task Group",new SumAllQAF(), this);
		taskRepository.ReadTaskDescriptions("TasksDetails.xml");
		this.schedulerPool = Executors.newFixedThreadPool(5);
		localScheduler = new Scheduler(this);
	}
	
	//A public method to feed new tasks to the scheduler
	public void AssignTask(String taskName)
	{
		Main.Message(this, debugFlag, "[Scheduler 62] Added Pending task " + taskName + " to Scheduler " + this.label);
		Task task = this.taskRepository.GetTask(taskName);
		if (task==null) Main.Message(this.debugFlag, "Error: Task " + taskName + " not found in tasks repository");
		task.AssignAgent(this);
		RegisterChildrenWithUI(task);
		this.pendingTasks.add(task);
		schedulerPool.execute(localScheduler);
	}
	
	private boolean IsManagingAgent()
	{
		if(agentsUnderManagement == null) return false;
		else
		return this.agentsUnderManagement.size()>0;
	}
	
	private String GetTaskLabels(List<Task> tasks)
	{
		String name = "-";
		for(Task t: tasks)
		{
			name += t.getLabel() + "-";
		}
		return name;
	}
	
	public void CalculateCost(List<Task> tasks, String requestingAgent)
	{
		List<MultipleTaskScheduleQualities> costs = CalculateIncrementalQualitiesForTask(tasks);
		
		SchedulingEventParams params = new SchedulingEventParams()
		.AddTaskName(GetTaskLabels(tasks))
		.AddAgentId(requestingAgent)
		.AddOriginatingAgent(this.label);
		SchedulingEvent event = new SchedulingEvent(requestingAgent, SchedulingCommandType.COSTBROADCAST, params);
		event.taskQualities = costs;
		mq.PublishMessage(event);
	}
	
	public void Negotiate(List<Task> tasks)
	{
		if (IsManagingAgent())
		{
			this.negotiations = new MaxSumCalculator(negotiationInstance,this.agentsUnderManagement.size()+1);//One additional for managing agent
			List<MultipleTaskScheduleQualities> costs = CalculateIncrementalQualitiesForTask(tasks);
			AgentScheduleQualities aql = new AgentScheduleQualities(this.getAgentId(this.label));
			aql.TaskQualities = costs;
			this.negotiations.AddCostData(aql);
			for(IAgent ag : this.getAgentsUnderManagement())
			{
				SchedulingEventParams params = new SchedulingEventParams()
				.AddAgentId(ag.getName())
				.AddOriginatingAgent(this.label);
				Main.Message(this, this.debugFlag, ag.getName() + " asked to calculate cost for multiple tasks");
				SchedulingEvent event = new SchedulingEvent(ag.getName(), SchedulingCommandType.CALCULATECOST, params);
				event.tasks = tasks;
				mq.PublishMessage(event);
			}
		}
	}
	
	/*Not needed for current support limited to single negotiation instance
	 * public MaxSumCalculator GetMaxSumCalculatorForTask(String taskName)
	{
		for(MaxSumCalculator cal : this.negotiations)
		{
			if (cal.getTaskName().equalsIgnoreCase(taskName))
				return cal;
		}
		return null;
	}*/
	
	public void ProcessCostBroadcast(String sendingAgentWhoseCostHasBeenRecieved, List<MultipleTaskScheduleQualities> ql)
	{	
		if(IsManagingAgent()) {
			//MaxSumCalculator calc = GetMaxSumCalculatorForTask(taskName);
			MaxSumCalculator calc = this.negotiations;
			if(calc == null) {
				System.out.println("calc is null");
			}
			AgentScheduleQualities aql = new AgentScheduleQualities(this.getAgentId(sendingAgentWhoseCostHasBeenRecieved));
			aql.TaskQualities = ql;
			calc.AddCostData(aql);
			if (calc.IsDataCollectionComplete())
			{
				String selectedAgentName = calc.GetBestAgent();
				//TODO Asif revisit
				//Diagnostic call -- Not impacting timers
				String selectedAgentNamePlainMethod = calc.GetBestAgentPlain();
				if (!selectedAgentName.equals(selectedAgentNamePlainMethod)) System.out.println("ERROR: Two methods are recommending different agents");
				//Diangostic call end
				
				SchedulingEventParams params = new SchedulingEventParams()
				.AddAgentId(selectedAgentName);
				SchedulingEvent event = new SchedulingEvent(selectedAgentName, SchedulingCommandType.ASSIGNTASK, params);
				mq.PublishMessage(event);
			}
		}
	}
	
	private List<Integer> ConvertTaskListToTaskIdList(List<Task> arr, Map<Integer, Task> idToTaskDictionary)
	{
		List<Integer> result = new ArrayList<Integer>();
		for(Task t : arr)
		{
			int taskId = t.GetIntId();
			result.add(taskId);
			idToTaskDictionary.put(taskId, t);
		}
		return result;
	}
	
	private List<MultipleTaskScheduleQualities> CalculateIncrementalQualitiesForTask(List<Task> tasks)
	{
		List<MultipleTaskScheduleQualities> ql = new ArrayList<MultipleTaskScheduleQualities>();
		try
		{
			ScenarioGenerator gen = new ScenarioGenerator();
			Map<Integer, Task> idToTaskDictionary = new HashMap<Integer,Task>();
			int base = GetScheduleCostSync(null, this).TotalQuality;
			IAgent previousAgent = tasks.get(0).agent;//Save previous agent, because assignment of agent will change while calculating costs and need to be reset
			List<Integer> taskIdsList = ConvertTaskListToTaskIdList(tasks, idToTaskDictionary);
			List<List<Integer>> combs = gen.GetArrayCombinations(taskIdsList);
			for(List<Integer> taskIdList : combs)
			{	
				List<Task> incrementalTaskcombinations = new ArrayList<Task>();
				for(Integer i : taskIdList)
				{
					Task t = idToTaskDictionary.get(i);
					if (t==null) throw new Exception ("Calculate Incremental Qualities called for a null task");
					t.agent = this;
					incrementalTaskcombinations.add(t);
				}
				int incremental = GetScheduleCostSync(incrementalTaskcombinations, this).TotalQuality;
				Main.Message(debugFlag, getName() + " for " + incrementalTaskcombinations.size() + " tasks Base " + base + " Incremental " + incremental);
				ql.add(new MultipleTaskScheduleQualities(taskIdList,base,incremental));
			}
			for(Task t : tasks)
			{
				//Reset agent change for done for calculation
				t.agent = previousAgent;
			}
		}
		catch(Exception e)
		{
			Main.Message(this, this.debugFlag, e.getMessage() + " " + e.getStackTrace());
		}	
		return ql;
	}
	
	public Schedule GetScheduleCostSync(List<Task> tasks, IAgent taskAgent)
	{
		//Make a copy
		Task tempTaskGroup = new Task("Task Group",new SumAllQAF(), taskAgent);
		Iterator<Node> copyTasks = this.GetCurrentTasks().getSubtasks();
		while(copyTasks.hasNext())
		{
			tempTaskGroup.addTask(copyTasks.next());
		}
		for(Task t : tasks)
		{
			if (t!=null) taskAgent = t.agent;
			//Sometimes we want to calculate base cost of executing existing tasks, without assigning a new one, where this
			//method will be called with a null value. So this check is necessary
			tempTaskGroup.addTask(t);
		}
		tempTaskGroup.Cleanup(MqttMessagingProvider.GetMqttProvider());
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
		while (!AreEnablersInPlace(m))
		{
			Main.Message(debugFlag, "[Agent 88] " + m.label + " enabler not in place. Waiting...");
			Thread.sleep(1000);
		}
		Main.Message(this, debugFlag, "Agent " + this.label + " executing " + m.label);
		if (m.x!=0 && m.y!=0)
		{
			status=Status.AWAITINGTASKCOMPLETION;
			this.currentMethod = m;
			fireSchedulingEvent(RavenUI.schedulingEventListenerName, SchedulingCommandType.DISPLAYTASKEXECUTION, this.getName(), m.label, m.x, m.y);
			this.flagScheduleRecalculateRequired = false;
		}
	}
	
	@Override
	public void MarkMethodCompleted(String methodName)
	{
		if (currentMethod != null && currentMethod.label.equalsIgnoreCase(methodName))
		{
			//schedule.get().RemoveElement(e);Does this need to be done?
			currentMethod.MarkCompleted();
			WorldState.CompletedMethods.add(currentMethod);
			this.completedMethods.put(methodName, methodName);
			Main.Message(debugFlag, "[Agent 130] " + currentMethod.label + " marked completed");
			if (currentSchedule!=null)
			{
				Iterator<ScheduleElement> el = currentSchedule.getItems();
				boolean successfullyRemovedCompletedMethod = false;
				while(el.hasNext())
				{
					ScheduleElement e = el.next();
					if (e.getMethod().label.equals(Method.StartingPoint) && el.hasNext())
						e = el.next();
					if (currentMethod.label.equals(e.getMethod().label))
					{
						currentSchedule.RemoveElement(e);
						Main.Message(debugFlag, "[Agent 135] Removed " + e.getName() + e.hashCode() + " from schedule " + currentSchedule.hashCode());
						successfullyRemovedCompletedMethod = true;
					}	
				}
				if (!successfullyRemovedCompletedMethod)
				{
					Main.Message(debugFlag, "[Agent 136] Could not remove " + currentMethod.label + " from schedule " + currentSchedule.hashCode());
				}
			}
			this.mq.PublishMessage(RavenUI.schedulingEventListenerName,SchedulingCommandType.DISPLAYREMOVEMETHOD, new SchedulingEventParams().AddMethodId(currentMethod.label).AddXCoord(currentMethod.x).AddYCoord(currentMethod.y).toString());
			flagScheduleRecalculateRequired = true;
			status=Status.PROCESSNG;
		}
		//TODO Get siblings for exactly one methods
		//for(String m : this.taskRepository.GetSiblingTasks(methodName))
		//{
			//this.mq.PublishMessage(RavenUI.schedulingEventListenerName,SchedulingCommandType.DISPLAYREMOVEMETHOD, new SchedulingEventParams().AddMethodId(m).AddXCoord(currentMethod.x).AddYCoord(currentMethod.y).toString());
		//}
	}
	
	public void fireSchedulingEvent(String destinationAgentId, SchedulingCommandType type, String subjectAgentId, String methodId, double x2, double y2) {
		SchedulingEventParams params = new SchedulingEventParams().AddAgentId(subjectAgentId).AddMethodId(methodId)
				.AddXCoord(x2).AddYCoord(y2).AddTaskName("");
		SchedulingEvent worldEvent = new SchedulingEvent(destinationAgentId, type, params);
        mq.PublishMessage(worldEvent);
    }
	
	// Returns identifying code, specific for this agent
	public int getCode(){
		return code;
	}
	
	public void UpdateSchedule(Schedule newSchedule)
	{
		this.currentSchedule.Merge(newSchedule, this.completedMethods);
		Main.Message(this.debugFlag, this.label + " updated schedule: " + this.currentSchedule.toString());
	}
	
	private void executeNextTask() {
		try
		{
			if (currentSchedule!=null)
			{
				Iterator<ScheduleElement> el = currentSchedule.getItems();
				if(el.hasNext())
				{
					ScheduleElement e = el.next();
					if (e.getMethod().label.equals(Method.StartingPoint) && el.hasNext())
						e = el.next();
					else
						return;
					Main.Message(this, this.debugFlag, this.label +  " picked next task " + e.getName() + " " + e.hashCode() + " from schedule " + currentSchedule.hashCode());
					Method m = e.getMethod();
					ExecuteTask(m);
				}
			}
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}
	
	public void RegisterChildrenWithUI(Node node)
	{
		if (!node.IsTask())
		{
			Method method = (Method)node;
			completedMethods.remove(method.label);//Remove previously completed task from list because of new issuance of same task
			fireSchedulingEvent(RavenUI.schedulingEventListenerName, SchedulingCommandType.DISPLAYADDMETHOD, this.getName(), method.getLabel(), method.x, method.y);
		}
		else
		{
			Iterator<Node> it = ((Task)node).getSubtasks();
			while(it.hasNext())
			{
				Node nd = it.next();
				RegisterChildrenWithUI(nd);
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
		Main.Message(this.debugFlag, "Executing agent");
		fireSchedulingEvent(RavenUI.schedulingEventListenerName, SchedulingCommandType.DISPLAYADDAGENT, this.getName(), null, x, y);
		RunSchedular();
		status=Status.PROCESSNG;
		//TODO Introduce step to fetch commands from mqtt to govern execution and status
		while(true)
		{
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
			currentSchedule.Merge(scheduleUpdateEvent.Schedule, this.completedMethods);
		else
			currentSchedule = scheduleUpdateEvent.Schedule;
		Main.Message(this, this.debugFlag, this.label + " schedule updated with tasks " + currentSchedule.toString());
	}

	@Override
	public void AddChildAgent(IAgent agent){
		if (agentsUnderManagement==null)
			Main.Message(debugFlag, "Child Agent being added to non-managing agent");
		this.agentsUnderManagement.add(agent);
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
	public SchedulingEvent ProcessSchedulingEvent(SchedulingEvent event) {
		if (event.commandType==SchedulingCommandType.ASSIGNTASK && event.agentName.equalsIgnoreCase(this.getName()))
		{
			AssignTask(event.params.TaskName);
		}
		if (event.commandType==SchedulingCommandType.METHODCOMPLETED && event.agentName.equalsIgnoreCase(this.getName()))
		{
			String completedMethodName = event.params.MethodId;
			if (completedMethodName!=null)
				MarkMethodCompleted(completedMethodName);
		}
		if (event.commandType==SchedulingCommandType.NEGOTIATE && event.agentName.equalsIgnoreCase(this.getName()))
		{
			Main.Message(debugFlag, "Tasks " + event.params.TaskName + " received for negotiation");
			List<Task> tasks = new ArrayList<Task>();
			String[] taskNames = event.params.TaskName.split("-");
			for(String s : taskNames)
			{
				if (s.length()>0)
				{
					tasks.add(this.taskRepository.GetTask(s));
				}
			}
			this.Negotiate(tasks);
		}
		if (event.commandType==SchedulingCommandType.CALCULATECOST && event.agentName.equalsIgnoreCase(this.getName()))
		{
			//Task task = this.taskRepository.GetTask(event.params.TaskName);
			CalculateCost(event.tasks, event.params.OriginatingAgent);
		}
		if (event.commandType==SchedulingCommandType.COSTBROADCAST && event.agentName.equalsIgnoreCase(this.getName()))
		{
			ProcessCostBroadcast(event.params.OriginatingAgent, event.taskQualities);
		}
		
		
		return null;
	}

	@Override
	public boolean IsGlobalListener() {
		return false;
	}
}
