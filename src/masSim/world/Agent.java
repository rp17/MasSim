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

public class Agent extends BaseElement implements IAgent, IScheduleUpdateEventListener, Runnable{

	private static int GloballyUniqueAgentId = 1;
	private int code;
	private Scheduler scheduler = new Scheduler();
	private final AtomicReference<Schedule> schedule = new AtomicReference<Schedule>();
	private List<Task> taskAbilities;
	private int taskInd;
	private boolean resetScheduleExecutionFlag = false;
	private Agent managingAgent = null;
	
	public ArrayList<WorldEventListener> listeners;//Possibly refactor to AgentEventListeners, if we want to differentiate world and agent events
	public int x, y;
	
	private enum Status {
		IDLE, PROCESSNG, EMPTY
	}
	
	/** alive, dead or spawning? */
	private Status status;
	
	public Agent(int newCode){
		this(newCode,"Agent"+newCode,null);
	}
	
	public Agent(String name, Agent managingAgent){
		this(GloballyUniqueAgentId++,name, managingAgent);
	}
	
	public Agent(int newCode, String label, Agent managingAgent){
		this.code = newCode;
		this.label = label;
		//taskGroup = new Task(label + " Tasks", new ExactlyOneQAF(), new Method("Default",0,0,0));
		taskAbilities = new ArrayList<Task>();
		taskInd = 0;
		status = Status.EMPTY;
		listeners = new ArrayList<WorldEventListener>();
		this.scheduler.AddScheduleUpdateEventListener(this);
		x = 20;
		y = 20;
		this.managingAgent = managingAgent;
		System.out.println("Agent " + label + " created with code" + code);
	}
	
	public void Execute(Method m)
	{
		//int xDistanceToTarget = m.x - this.x;
		//int yDistanceToTarget = m.y - this.y;
		//int absoluteDistanceIntervals = (int) Math.sqrt(Math.pow(xDistanceToTarget, 2) + Math.pow(yDistanceToTarget, 2));
		//for(int i=0;i<absoluteDistanceIntervals;i++)
		//{
		//	this.x += (int)Math.ceil(i * ((double)xDistanceToTarget/absoluteDistanceIntervals));
		//	this.y += (int)Math.ceil(i * ((double)yDistanceToTarget/absoluteDistanceIntervals));
		fireAgentMovedEvent(TaskType.EXECUTEMETHOD, this.label, m.label, m.x, m.y);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//}
	}
	
	public synchronized void fireAgentMovedEvent(TaskType type, String agentId, String methodId, int x2, int y2) {
        WorldEvent worldEvent = new WorldEvent(this, TaskType.EXECUTEMETHOD, agentId, methodId, x2, y2);
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
	
	public void addTaskAbility(Task task){
		taskAbilities.add(task);
	}
	
	/**
	 * this method handles the assignment goals
	 */
	public void assignTasks(ArrayList<Task> tasks){
		System.out.println("Agent: " + code + " - " + tasks.size() + " Tasks Assigned");
		this.scheduler.AddTasks(tasks);
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
		//Schedule newLocalSchedule = scheduleUpdateEvent.Schedule;
		schedule.set(scheduleUpdateEvent.Schedule);
	}
}
