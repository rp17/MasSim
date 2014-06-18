package masSim.world;

import masSim.main.MasSim;
import masSim.world.WorldEvent;
import masSim.world.WorldEventListener;
import masSim.world.WorldEvent.TaskType;
import masSim.schedule.IScheduleUpdateEventListener;
import masSim.schedule.ScheduleUpdateEvent;
import masSim.schedule.Scheduler;
import masSim.taems.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import raven.math.Vector2D;

public class Agent extends BaseElement implements IAgent, IScheduleUpdateEventListener, Runnable{

	private static int GloballyUniqueAgentId = 1;
	private int code;
	private Scheduler scheduler;
	private final AtomicReference<Schedule> schedule = new AtomicReference<Schedule>();
	private int taskInd;
	private boolean resetScheduleExecutionFlag = false;
	private ArrayList<IAgent> agentsUnderManagement = null;
	public ArrayList<WorldEventListener> listeners;
	public double x;
	public double y;
	
	private enum Status {
		IDLE, PROCESSNG, EMPTY
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
		if (listeners==null)
			this.listeners = new ArrayList<WorldEventListener>();
		else
			this.listeners = listeners;
		scheduler = new Scheduler(this);
		this.scheduler.AddScheduleUpdateEventListener(this);
		this.x = x;
		this.y = y;
		if (isManagingAgent) agentsUnderManagement = new ArrayList<IAgent>();
		fireWorldEvent(TaskType.AGENTCREATED, label, null, x, y);
		System.out.println("Agent " + label + " created with code" + code);
	}
	
	public void Execute(Method m)
	{
		if (m.x!=0 && m.y!=0)
			fireAgentMovedEvent(TaskType.EXECUTEMETHOD, this.label, m.label, m.x, m.y);
	}
	
	public synchronized void fireAgentMovedEvent(TaskType type, String agentId, String methodId, double x2, double y2) {
        WorldEvent worldEvent = new WorldEvent(this, TaskType.EXECUTEMETHOD, agentId, methodId, x2, y2,null);
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
		while(true)
		{
			if (schedule.get()!=null)
			{
				Iterator<ScheduleElement> el = schedule.get().getItems();
				while(el.hasNext())
				{
					ScheduleElement e = el.next();
					Method m = e.getMethod();
					Execute(m);
					m.MarkCompleted();
				}
			}
		}
	}
	
	/**
	 * this method handles the assignment goals
	 */
	public void assignTask(Task task){
		if (task.agent!=null)
		{
			if (this.equals(task.agent))
			{
				System.out.println("Agent: " + label + " - Assigned " + task.label);
				Iterator<Node> it = task.getSubtasks();
				while(it.hasNext())
				{
					Node node = it.next();
					if (!node.IsTask())
					{
						Method method = (Method)node;
						this.fireWorldEvent(TaskType.METHODCREATED, null, method.label, method.x, method.y);
					}
				}
				this.scheduler.AddTask(task);
			}
			else if (this.agentsUnderManagement.contains(task.agent)) 
			{
				task.agent.assignTask(task);
			}
			else
			{
				System.out.println(task.agent.getCode() + " is not a child of " + this.label);
			}
		}
		else
		{
			//Calculate which agent is best to assign
			int highestQuality = this.getExpectedScheduleQuality(task, this);
			IAgent selectedAgent = this;
			for(IAgent ag : this.agentsUnderManagement)
			{
				int qualityWithThisAgent = ag.getExpectedScheduleQuality(task, ag);
				if (qualityWithThisAgent>highestQuality)
				{
					highestQuality = qualityWithThisAgent;
					selectedAgent = ag;
				}
			}
			task.agent = selectedAgent;
			assignTask(task);
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
		Thread agentScheduler = new Thread(this.scheduler,"Scheduler " + this.label);
		agentScheduler.start();
		executeSchedule();
		//negotiate();
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
	
	public synchronized void fireWorldEvent(TaskType type, String agentId, String methodId, double x2, double y2) {
        WorldEvent worldEvent = new WorldEvent(this, type, agentId, methodId, x2, y2, this);
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
		IAgent previousAgent = task.agent;
		task.agent = agent;
		int cost = this.scheduler.GetScheduleCostSync(task);
		task.agent = previousAgent;
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
}
