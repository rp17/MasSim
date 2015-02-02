package masSim.world;

import masSim.schedule.IScheduleUpdateEventListener;
import masSim.schedule.ScheduleUpdateEvent;
import masSim.schedule.Scheduler;
import masSim.schedule.SchedulingCommandType;
import masSim.schedule.SchedulingEvent;
import masSim.schedule.SchedulingEventListener;
import masSim.schedule.SchedulingEventParams;
import masSim.taems.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import raven.Main;
import raven.math.Vector2D;
import raven.ui.RavenUI;
import raven.utils.SchedulingLog;

public class Agent extends BaseElement implements IAgent, IScheduleUpdateEventListener, SchedulingEventListener, Runnable{

	private boolean debugFlag = true;
	private static int GloballyUniqueAgentId = 1;
	private int code;
	private Schedule currentSchedule = new Schedule();
	private int taskInd;
	private boolean resetScheduleExecutionFlag = false;
	private ArrayList<IAgent> agentsUnderManagement = null;
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
		taskRepository.ReadTaskDescriptions(getName()+".xml");
		this.schedulerPool = Executors.newFixedThreadPool(5);
		localScheduler = new Scheduler(this);
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
	
	//TODO This method call will be removed to include an internal loop to check mqtt for new assignments
	public void AddPendingTask(Task task)
	{
		pendingTasks.add(task);
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
			Main.Message(true, "[Agent 88] " + m.label + " enabler not in place. Waiting...");
			Thread.sleep(1000);
		}
		if (m.x!=0 && m.y!=0)
		{
			status=Status.AWAITINGTASKCOMPLETION;
			this.currentMethod = m;
			fireSchedulingEvent(RavenUI.schedulingEventListenerName, SchedulingCommandType.DISPLAYTASKEXECUTION, this.getName(), m.label, m.x, m.y);
			Main.Message(true, "[Agent 76] Agent " + this.label + " executing " + m.label);
			this.flagScheduleRecalculateRequired = false;
		}
	}
	
	@Override
	public void MarkMethodCompleted(String methodName)
	{
		Main.Message(true, "[Agent 176] Agent Calling complete for " + methodName + " for agent " + this.label);
		if (currentMethod != null && currentMethod.label.equalsIgnoreCase(methodName))
		{
			//schedule.get().RemoveElement(e);Does this need to be done?
			currentMethod.MarkCompleted();
			WorldState.CompletedMethods.add(currentMethod);
			Main.Message(true, "[Agent 130] " + currentMethod.label + " added to completed queue");
			if (currentSchedule!=null)
			{
				Iterator<ScheduleElement> el = currentSchedule.getItems();
				if(el.hasNext())
				{
					ScheduleElement e = el.next();
					if (e.getMethod().label.equals(Method.StartingPoint) && el.hasNext())
						e = el.next();
					if (currentMethod.equals(e.getMethod()))
					{
						currentSchedule.RemoveElement(e);
						Main.Message(true, "[Agent 135] Removed " + e.getName() + " from schedule");
					}
				}
			}
			this.mq.PublishMessage(RavenUI.schedulingEventListenerName,SchedulingCommandType.DISPLAYREMOVEMETHOD, new SchedulingEventParams().AddMethodId(currentMethod.label).AddXCoord(currentMethod.x).AddYCoord(currentMethod.y).toString());
			flagScheduleRecalculateRequired = true;
			Main.Message(true, "[Agent 87] " + currentMethod.label + " completed and recalc flag set to " + flagScheduleRecalculateRequired);
			status=Status.PROCESSNG;
		}
	}
	
	public void fireSchedulingEvent(String destinationAgentId, SchedulingCommandType type, String subjectAgentId, String methodId, double x2, double y2) {
		SchedulingEventParams params = new SchedulingEventParams(subjectAgentId, methodId, Double.toString(x2), Double.toString(y2), "");
		SchedulingEvent worldEvent = new SchedulingEvent(destinationAgentId, type, params);
        mq.PublishMessage(worldEvent);
    }
	
	// Returns identifying code, specific for this agent
	public int getCode(){
		return code;
	}
	
	public void UpdateSchedule(Schedule newSchedule)
	{
		this.currentSchedule = newSchedule;
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
					Method m = e.getMethod();
					Main.Message(debugFlag, "[Agent 197] Next method to be executed from schedule " + m.label);
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
				System.out.println("Agent " + label + " completed item " + el.getName());
				currentSchedule.poll();
			}
		}
		else {
			System.out.println("Agent " + label + " idle");
		}
	}

	@Override
	public void run() {
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
			currentSchedule.Merge(scheduleUpdateEvent.Schedule);
		else
			currentSchedule = scheduleUpdateEvent.Schedule;
	}

	@Override
	public void AddChildAgent(IAgent agent){
		if (agentsUnderManagement==null)
			System.out.println("Child Agent being added to non-managing agent");
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
			Task task = this.taskRepository.GetTask(event.params.TaskName);
			task.agent = this;
			RegisterChildrenWithUI(task);
			this.pendingTasks.add(task);
			schedulerPool.execute(localScheduler);
		}
		if (event.commandType==SchedulingCommandType.METHODCOMPLETED && event.agentName.equalsIgnoreCase(this.getName()))
		{
			String completedMethodName = event.params.MethodId;
			if (completedMethodName!=null)
				MarkMethodCompleted(completedMethodName);
		}
		return null;
	}

	@Override
	public boolean IsGlobalListener() {
		return false;
	}
}
