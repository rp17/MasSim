package masSim.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import masSim.world.*;
import masSim.world.WorldEvent.TaskType;
import masSim.taems.*;

public class SimWorld implements Runnable {

	private ArrayList<IAgent> agents;
	private ArrayList<Task> tasks;
	private ArrayList<WorldEventListener> listeners;
	
	public SimWorld(WorldEventListener eventListener)
	{
		agents = new ArrayList<IAgent>();
		tasks = new ArrayList<Task>();
		listeners = new ArrayList<WorldEventListener>();
		listeners.add(eventListener);
	}
	
	private void AddAgentToWorld(IAgent agent, int x, int y)
	{
		agents.add(agent);
		fireWorldEvent(TaskType.AGENTCREATED, ((Agent)agent).label, null, x, y);
		
	}
	
	private void AddTaskToWorld(Task task)
	{
		tasks.add(task);
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
	}
	
	@Override
	public void run()
	{
		//Initialize two agents
		Agent one = new Agent("Helicopter1",null);
		Agent two = new Agent("Helicopter2", one);
		AddAgentToWorld(one, 20, 100);
		AddAgentToWorld(two, 20, 300);
		
		//Initialize a bunch of tasks
		AddTaskToWorld(new Task("Station 1",new SumAllQAF(), new Method("1",1,100,100)));
		AddTaskToWorld(new Task("Station 2",new SumAllQAF(), new Method("2",1,300,100)));
		AddTaskToWorld(new Task("Station 3",new SumAllQAF(), new Method("3",1,300,150)));
		AddTaskToWorld(new Task("Station 4",new SumAllQAF(), new Method("4",1,100,150)));
		
		AddTaskToWorld(new Task("Station 5",new SumAllQAF(), new Method("5",1,100,300)));
		AddTaskToWorld(new Task("Station 6",new SumAllQAF(), new Method("6",1,300,300)));
		AddTaskToWorld(new Task("Station 7",new SumAllQAF(), new Method("7",1,300,350)));
		AddTaskToWorld(new Task("Station 8",new SumAllQAF(), new Method("8",1,100,350)));
		
		AssignTasksToAgent(4,7,agents.get(0));
		
		for(int i=4;i<8;i++)
		{
			//agents.get(1).assignTask(tasks.get(i));
		}
		
		//Start Agents
		Iterator it = agents.iterator();
		while(it.hasNext())
		{
			Agent agent = (Agent) it.next();
			agent.listeners.add(listeners.get(0));//TODO remove hard coding
			Thread agentThread = new Thread(agent);
			agentThread.start();
		}
		try {
			Thread.sleep(8000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		AssignTasksToAgent(4,7,agents.get(0));
	}
	
	private void AssignTasksToAgent(int start, int end, IAgent agent)
	{
		//Assign Tasks to agents
		ArrayList<Task> tasksForAgent = new ArrayList<Task>();
		for(int i=start;i<=end;i++)
		{
			tasksForAgent.add(tasks.get(i));
		}
		agent.assignTasks(tasksForAgent);
	}
	
	public synchronized void addListener(WorldEventListener sl) {
        listeners.add(sl);
    }
 
    public synchronized void removeListener(WorldEventListener sl) {
        listeners.remove(sl);
    }
    
    public synchronized void fireWorldEvent(TaskType type, String agentId, String methodId, int x, int y) {
        WorldEvent worldEvent = new WorldEvent(this, type, agentId, methodId, x, y);
        WorldEventListener listener;
        for(int i=0;i<listeners.size();i++)
        {
        	Object o = listeners.get(i);
        	listener = (WorldEventListener) o;
        	listener.HandleWorldEvent(worldEvent);
        }
    }

}
