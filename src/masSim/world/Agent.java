package masSim.world;

import masSim.world.WorldEvent;
import masSim.world.WorldEventListener;
import masSim.world.WorldEvent.TaskType;
import masSim.schedule.IScheduleUpdateEventListener;
import masSim.schedule.ScheduleUpdateEvent;
import masSim.schedule.Scheduler;
import masSim.taems.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import raven.Main;
import raven.math.Vector2D;
import raven.utils.SchedulingLog;

public class Agent extends BaseElement implements IAgent, IScheduleUpdateEventListener, Runnable{

	private boolean debugFlag = true;
	private static int GloballyUniqueAgentId = 1;
	private int code;
	private Scheduler scheduler;
	private final AtomicReference<Schedule> schedule = new AtomicReference<Schedule>();
	private int taskInd;
	private boolean resetScheduleExecutionFlag = false;
	private ArrayList<IAgent> agentsUnderManagement = null;
	private AgentMode mode;
	public ArrayList<WorldEventListener> listeners;
	public double x;
	public double y;
	public boolean flagScheduleRecalculateRequired;
	public Queue<Method> queue = new LinkedList<Method>();
	private TaskRepository = new TaskRepository();
	
	
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
		scheduler = new Scheduler(this);
		this.scheduler.AddScheduleUpdateEventListener(this);
		this.x = x;
		this.y = y;
		if (isManagingAgent) agentsUnderManagement = new ArrayList<IAgent>();
		fireWorldEvent(TaskType.AGENTCREATED, label, null, x, y, null);
	}
	
	public void Initialize()
	{
		
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
	
	public void Execute(Method m) throws InterruptedException
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
		if (schedule.get()!=null)
		{
			Iterator<ScheduleElement> el = schedule.get().getItems();
			if(el.hasNext())
			{
				ScheduleElement e = el.next();
				if (e.getMethod().label.equals(Method.StartingPoint) && el.hasNext())
					e = el.next();
				if (m.equals(e.getMethod()))
				{
					schedule.get().RemoveElement(e);
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
	
	private void executeSchedule() {
		while(flagScheduleRecalculateRequired)
		{
			Main.Message(debugFlag, "[Agent 111] Executing Schedule");
			flagScheduleRecalculateRequired = false;
			Main.Message(debugFlag, "[Agent 111] Running again");
			Schedule newSchedule = this.scheduler.RunStatic();
			if (newSchedule!=null)
				SchedulingLog.info(this.getName() + " Selected: " + newSchedule.toString() + System.lineSeparator());
			if (newSchedule!=null) {
				schedule.set(newSchedule);
				//Main.Message(debugFlag, "[Agent 119] Schedule Updated. New first method " + schedule.get().peek().getMethod().label);
			}
			if (schedule.get()!=null)
			{
				Iterator<ScheduleElement> el = schedule.get().getItems();
				if(el.hasNext())
				{
					try {
						ScheduleElement e = el.next();
						if (e.getMethod().label.equals(Method.StartingPoint) && el.hasNext())
							e = el.next();
						else
							continue;
						Method m = e.getMethod();
						Main.Message(debugFlag, "[Agent 132] Next method to be executed from schedule " + m.label);
						Execute(m);
						while(!flagScheduleRecalculateRequired)
						{	
							//Main.Message(debugFlag, "[Agent 126] Waiting completion of " + m.label + " with flag " + flagScheduleRecalculateRequired);
							Thread.sleep(1000);
						}
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}
	
	private void RegisterChildrenWithUI(Node node)
	{
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
	
	/**
	 * this method handles the assignment goals
	 */
	public void assignTask(Task task){
		try{
			if (task.IsFullyAssigned())
			{
				if (this.equals(task.agent))
				{
					Main.Message(debugFlag, "[Agent] " + label + " assigned " + task.label);
					RegisterChildrenWithUI(task);
					this.scheduler.AddTask(task);
					flagScheduleRecalculateRequired = true;
				}
				else if (this.agentsUnderManagement.contains(task.agent)) 
				{
					Main.Message(debugFlag, "[Agent] 150" + task.label + " already has agent assigned");
					task.agent.assignTask(task);
				}
				else
				{
					Main.Message(debugFlag, task.agent.getCode() + " is not a child of " + this.label);
				}
			}
			else
			{
				//Italian guy practical applications to quadrovers. Look at that.dellefave-IAAI-12.pdf
				//Calculate which agent is best to assign
				int baseQuality = this.getExpectedScheduleQuality(null, this);
				int qualityWithThisAgent = this.getExpectedScheduleQuality(task, this);
				int addedQuality = qualityWithThisAgent - baseQuality;
				Main.Message(true, "[Agent 162] Quality with agent " + this.getName() + " " + qualityWithThisAgent + " + " + baseQuality + " = " + addedQuality);
				IAgent selectedAgent = this;
				for(IAgent ag : this.agentsUnderManagement)
				{
					baseQuality = this.getExpectedScheduleQuality(null, ag);
					qualityWithThisAgent = ag.getExpectedScheduleQuality(task, ag);
					int newAddedQuality = qualityWithThisAgent-baseQuality;
					Main.Message(true, "[Agent 162] Quality with agent " + this.getName() + " " + qualityWithThisAgent + " + " + baseQuality + " = " + newAddedQuality);
					if (newAddedQuality>addedQuality)
					{
						addedQuality = newAddedQuality;
						selectedAgent = ag;
					}
				}
			//TODO Assigning a task to an agent means its methods will also be performed by the same agent. But this needs to be revisited
			task.AssignAgent(selectedAgent);
				Main.Message(true, "[Agent 175] Assigning " + task.label + " to " + task.agent.getName());
				flagScheduleRecalculateRequired = true;
				assignTask(task);
			}
		}
		catch(Exception ex)
		{
			Main.Message(true, "[Agent 282] Exception: " + ex.toString());
		}
	}
	
	public void update(int tick) {
		
		if(schedule.get().hasNext(taskInd)) {
			ScheduleElement el = schedule.get().peek();
			ScheduleElement.Status status = el.update(tick);
			if(status == ScheduleElement.Status.COMPLETED) {
				System.out.println("Agent " + label + " completed item " + el.getName());
				schedule.get().poll();
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
		while(true)
		{
			executeSchedule();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void HandleScheduleEvent(ScheduleUpdateEvent scheduleUpdateEvent) {
		Schedule currentSchedule = schedule.get();
		if (currentSchedule!=null)
			schedule.get().Merge(scheduleUpdateEvent.Schedule);
		else
			schedule.set(scheduleUpdateEvent.Schedule);
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
	public int getExpectedScheduleQuality(Task task, IAgent agent) {
		int cost = 0;
		Schedule sc;
		if (task!=null)
		{
			IAgent previousAgent = task.agent;
			task.agent = agent;
			sc = this.scheduler.GetScheduleCostSync(task, agent);
			cost = sc.TotalQuality;
			task.agent = previousAgent;
		}
		else{
			sc = this.scheduler.GetScheduleCostSync(null, agent);
			cost = sc.TotalQuality;
		}
		SchedulingLog.info(this.getName() + " Negotiated: " + sc.toString() + System.lineSeparator());
		return cost;
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
