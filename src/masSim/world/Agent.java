package masSim.world;

import masSim.world.WorldEvent;
import masSim.world.WorldEventListener;
import masSim.world.WorldEvent.TaskType;
import masSim.schedule.IScheduleUpdateEventListener;
import masSim.schedule.ScheduleUpdateEvent;
import masSim.schedule.Scheduler;
import masSim.taems.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import raven.Main;
import raven.math.Vector2D;
import raven.utils.SchedulingLog;

public class Agent extends BaseElement implements IAgent, IScheduleUpdateEventListener, Runnable{

	private boolean debugFlag = true;
	private static int GloballyUniqueAgentId = 1;
	private int code;
	ExecutorService schedulerPool;
	private Schedule currentSchedule = new Schedule();
	private int taskInd;
	private boolean resetScheduleExecutionFlag = false;
	private ArrayList<IAgent> agentsUnderManagement = null;
	private AgentMode mode;
	public ArrayList<WorldEventListener> listeners;
	public double x;
	public double y;
	public boolean flagScheduleRecalculateRequired;
	public Queue<Method> queue = new LinkedList<Method>();
	private TaskRepository taskRepository = new TaskRepository();
	//Represents the top level task, called task group in taems, which contains all child tasks to be scheduled for this agent
	private Task currentTaskGroup;
	public ArrayList<Task> pendingTasks = new ArrayList<Task>();
	
	public ArrayList<IAgent> getAgentsUnderManagement()
	{
		return agentsUnderManagement;
	}
	
	private enum Status {
		IDLE, PROCESSNG, EMPTY
	}
	
	@Override
	public String getName()
	{
		return this.label;
	}
	
	/** alive, dead or spawning? */
	private Status status;
	
	public Agent(int newCode){
		this(newCode,"Agent"+newCode,false,0,0,null);
	}
	
	public Agent(String name, boolean isManagingAgent, int x, int y, ArrayList<WorldEventListener> listeners){
		this(GloballyUniqueAgentId++,name, isManagingAgent, x, y, listeners);
	}
	
	public Agent(int newCode, String label, boolean isManagingAgent, int x, int y, ArrayList<WorldEventListener> listeners){
		this.code = newCode;
		this.label = label;
		taskInd = 0;
		status = Status.EMPTY;
		flagScheduleRecalculateRequired = true;
		if (listeners==null)
			this.listeners = new ArrayList<WorldEventListener>();
		else
			this.listeners = listeners;
		this.x = x;
		this.y = y;
		if (isManagingAgent) agentsUnderManagement = new ArrayList<IAgent>();
		fireWorldEvent(TaskType.AGENTCREATED, label, null, x, y, null);
		schedulerPool = Executors.newFixedThreadPool(3);
		currentTaskGroup = new Task("Task Group",new SumAllQAF(), this);
	}
	
	@Override
	public Task GetCurrentTasks()
	{
		return currentTaskGroup;
	}
	
	public void RunSchedularForAgent(IAgent agent)
	{
		Scheduler newLocalSchedularThread = new Scheduler(agent);
		this.schedulerPool.submit(newLocalSchedularThread);
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
			fireAgentMovedEvent(TaskType.EXECUTEMETHOD, this.label, m.label, m.x, m.y, this, m);
			Main.Message(true, "[Agent 76] Agent " + this.label + " executing " + m.label);
			this.flagScheduleRecalculateRequired = false;
		}
	}
	
	@Override
	public void MarkMethodCompleted(Method m)
	{
		//schedule.get().RemoveElement(e);Does this need to be done?
		m.MarkCompleted();
		WorldState.CompletedMethods.add(m);
		Main.Message(true, "[Agent 130] " + m.label + " added to completed queue");
		if (currentSchedule!=null)
		{
			Iterator<ScheduleElement> el = currentSchedule.getItems();
			if(el.hasNext())
			{
				ScheduleElement e = el.next();
				if (e.getMethod().label.equals(Method.StartingPoint) && el.hasNext())
					e = el.next();
				if (m.equals(e.getMethod()))
				{
					currentSchedule.RemoveElement(e);
					Main.Message(true, "[Agent 135] Removed " + e.getName() + " from schedule");
				}
			}
		}
		this.fireWorldEvent(TaskType.METHODCOMPLETED, null, m.label, m.x, m.y, m);
		flagScheduleRecalculateRequired = true;
		Main.Message(true, "[Agent 87] " + m.label + " completed and recalc flag set to " + flagScheduleRecalculateRequired);
	}
	
	public void fireAgentMovedEvent(TaskType type, String agentId, String methodId, double x2, double y2, IAgent agent, Method method) {
        Main.Message(debugFlag, "[Agent 78] Firing Execute Method for " + methodId);
		WorldEvent worldEvent = new WorldEvent(this, TaskType.EXECUTEMETHOD, agentId, methodId, x2, y2, agent, method);
        Iterator it = listeners.iterator();
        WorldEventListener listener;
        while(it.hasNext())
        {
        	listener = (WorldEventListener)it.next();
        	listener.HandleWorldEvent(worldEvent);
        }
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
		try{	
			Main.Message(debugFlag, "[Agent 186] Executing Schedule");
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
		//TODO Remove method and do this via mqtt/ui directly. not task or agent's job to do this
		if (!node.IsTask())
		{
			Method method = (Method)node;
			this.fireWorldEvent(TaskType.METHODCREATED, null, method.label, method.x, method.y, method);
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
		//Running the agent means that the agent starts doing two things, and does them indefinitely unless it is killed or suspended.
		//First, it creates a background thread to keep checking for new tasks, and to calculate an optimum schedule for those.
		//Second, it executes those tasks whose schedule had already been created.
		//Thread agentScheduler = new Thread(this.scheduler,"Scheduler " + this.label);
		//agentScheduler.start();
		RunSchedularForAgent(this);
		status=Status.PROCESSNG;
		//TODO Introduce step to fetch commands from mqtt to govern execution and status
		while(status==Status.PROCESSNG)
			executeNextTask();
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
	
	public synchronized void fireWorldEvent(TaskType type, String agentId, String methodId, double x2, double y2, Method method) {
        WorldEvent worldEvent = new WorldEvent(this, type, agentId, methodId, x2, y2, this, method);
        WorldEventListener listener;
        for(int i=0;i<listeners.size();i++)
        {
        	Object o = listeners.get(i);
        	listener = (WorldEventListener) o;
        	listener.HandleWorldEvent(worldEvent);
        }
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
}
