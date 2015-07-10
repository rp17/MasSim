
package masSim.taems;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import masSim.schedule.SchedulingCommandType;
import masSim.schedule.SchedulingEvent;
import masSim.schedule.SchedulingEventParams;
import masSim.world.MqttMessagingProvider;
import masSim.world.WorldState;
import raven.Main;
import raven.TaskIssuer;

public class Task extends Node {

	private boolean debugFlag = true;
	private QAF qaf;
	public Date earliest_start_time;
	public Date deadline;
	public boolean isComplete = false;
	public static Object Lock = new Object();
	
	public int GetUtility()
	{
		return 0;
	}
	
	public boolean IsTask(){return true;}
	
	public boolean hasChildren()
	{
		return this.children.size()>0;
	}
	
	public ArrayList<Method> GetMethods()
	{
		ArrayList<Method> methods = new ArrayList<Method>();
		synchronized(Task.Lock){
		for(Node n: children)
		{
			if (!n.IsTask())
				methods.add((Method)n);
		}}
		return methods;
	}
	
	public void AssignAgent(IAgent ag)
	{
		Main.Message(this, debugFlag, "Assigning " + this.label + " to " + ag.getName());
		this.agent=ag;
		synchronized(Task.Lock){
		for(Node n: children)
		{
			if (n.agent==null)
				n.agent=ag;
		}}
	}
	
	public boolean IsFullyAssigned()
	{
		if (this.agent==null) return false;
		synchronized(Task.Lock){for(Node n: children)
		{
			if (n.agent==null)
				return false;
		}}
		return true;
	}
		
	// Constructor
	public Task(String label, QAF qaf, Date earliest_start, Date deadline, IAgent agent, Node[] m, boolean recurring){
		this.label = label;
		children = new java.util.concurrent.CopyOnWriteArrayList<Node>();
		this.qaf = qaf;
		this.earliest_start_time = earliest_start;
		this.deadline = deadline;
		if (m!=null)
		{
			for(Node mm : m){
				this.children.add(mm);
			}
		}
		this.agent = agent;
		this.recurring = recurring;
	}
	
	public Task(String label, QAF qaf, Date earliest_start, Date deadline, IAgent agent ,Node m){
		this(label, qaf, earliest_start, deadline, agent, new Node[]{m}, false);
	}
	
	public Task(String name, QAF qaf, IAgent agent){
		this(name, qaf, new Date(), new Date(2015,1,1), agent, new Method[]{}, false);
	}
	
	public Task(String name, QAF qaf, IAgent agent, boolean recurring){
		this(name, qaf, new Date(), new Date(2015,1,1), agent, new Method[]{}, recurring);
	}
	
	public Task(String name, QAF qaf, IAgent agent, Node m){
		this(name, qaf, new Date(), new Date(2015,1,1), agent, m);
	}
	
	public Task(String name, QAF qaf, IAgent agent, Node[] m){
		this(name, qaf, new Date(), new Date(2015,1,1), agent, m, false);
	}
	
	
	public void addTask(Node task){
		synchronized(Task.Lock){
		this.children.add(task);}
		System.out.println("Task.addTask : children ");
		for(Node n : children) {
			System.out.println(n.label);
		}
	}
	
	public QAF getQAF(){
		return qaf;
	}
	
	
	@Override
	public void MarkCompleted()
	{
		super.MarkCompleted();
		Main.Message(debugFlag, "[Task 63] Task " + label + " completed.");
		WorldState.CompletedTasks.add(this);
		this.NotifyAll();
		//ReIssueIfNecessary();
	}
	
	private void ReIssueIfNecessary()
	{
		if (this.recurring)
		{
			Main.Message(debugFlag, "Reissuing recurring task " + this.label);
			MqttMessagingProvider mq = MqttMessagingProvider.GetMqttProvider();
			if(mq == null) {
				Main.Message(this, true, ": mq provider is null, cannot publish METHODCOMPLETED event");
			}
			else {
				mq.PublishMessage(this.agent.getName() + ",ASSIGNTASK,----" + this.label);
			}	
		}
	}
	
	@Override
	public synchronized void Cleanup(MqttMessagingProvider mq)
	{
		if (this.hasChildren())
		{
			synchronized(Task.Lock)
			{
				Main.Message(true, "entered lock 1");
			
				for(Node n : children)
				{
					if (n!=null)
					{
						if (n.IsComplete())
						{
							children.remove(n);
							mq.PublishMessage(new SchedulingEvent(TaskIssuer.TaskIssuerName,SchedulingCommandType.TASKCOMPLETED,new SchedulingEventParams().AddTaskName(n.getLabel())));
						}
						else
						{
							if (n.IsTask())
							{
								n.Cleanup(mq);
								if (n.IsComplete())//Recheck after cleanup
								{
									children.remove(n);
									mq.PublishMessage(new SchedulingEvent(TaskIssuer.TaskIssuerName,SchedulingCommandType.TASKCOMPLETED,new SchedulingEventParams().AddTaskName(n.getLabel())));
								}
							}
						}
					}
				}
			}
			Main.Message(true, "exited lock 1");
		}
	}
	/*
	public synchronized void Cleanup()
	{
		if (this.hasChildren())
		{
			synchronized(Task.Lock)
			{
				Main.Message(true, "entered lock 1");
			
				for(Node n : children)
				{
					if (n!=null)
					{
						if (n.IsComplete())
						{
							children.remove(n);
							//mq.PublishMessage(new SchedulingEvent(TaskIssuer.TaskIssuerName,SchedulingCommandType.TASKCOMPLETED,new SchedulingEventParams().AddTaskName(n.getLabel())));
						}
						else
						{
							if (n.IsTask())
							{
								//n.Cleanup();
								if (n.IsComplete())//Recheck after cleanup
								{
									children.remove(n);
									//mq.PublishMessage(new SchedulingEvent(TaskIssuer.TaskIssuerName,SchedulingCommandType.TASKCOMPLETED,new SchedulingEventParams().AddTaskName(n.getLabel())));
								}
							}
						}
					}
				}
			}
			Main.Message(true, "exited lock 1");
		}
	}
	*/
	
	public synchronized void Cleanup()
	{
		if (this.hasChildren())
		{
				for(Node n : children)
				{
					if (n!=null)
					{
						if (n.IsComplete())
						{
							children.remove(n);
							//mq.PublishMessage(new SchedulingEvent(TaskIssuer.TaskIssuerName,SchedulingCommandType.TASKCOMPLETED,new SchedulingEventParams().AddTaskName(n.getLabel())));
						}
						else
						{
							if (n.IsTask())
							{
								n.Cleanup();
								if (n.IsComplete())//Recheck after cleanup
								{
									children.remove(n);
									//mq.PublishMessage(new SchedulingEvent(TaskIssuer.TaskIssuerName,SchedulingCommandType.TASKCOMPLETED,new SchedulingEventParams().AddTaskName(n.getLabel())));
								}
							}
						}
					}
				}
		}
	}
	
	public static Task CreateDefaultTask(int counter, double x, double y)
	{
		return new Task("Station " + counter,new SumAllQAF(), null, new Method[]{
			new Method("DM"+counter,10,0,x,y,0,null)
		});
	}
}
