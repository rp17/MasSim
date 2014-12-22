package masSim.world;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import raven.Main;
import raven.math.Vector2D;
import masSim.world.*;
import masSim.world.WorldEvent.TaskType;
import masSim.taems.*;

public class SimWorld5 implements Runnable, WorldEventListener {

	private ArrayList<IAgent> agents;
	private ArrayList<Task> tasks;
	private ArrayList<WorldEventListener> listeners;
	private IAgent agentZero;
	private IAgent agentOne;
	
	public SimWorld5(WorldEventListener eventListener)
	{
		agents = new ArrayList<IAgent>();
		tasks = new ArrayList<Task>();
		listeners = new ArrayList<WorldEventListener>();
		listeners.add(eventListener);
		listeners.add(this);
		
		//Initialize two agents, and specify their initial positions
		agentZero = new Agent("AgentZero", true, 40, 100, listeners);
		agentOne = new Agent("AgentOne", false, 40, 300, listeners);
		agentZero.AddChildAgent(agentOne);
		agents.add(agentZero);
		agents.add(agentOne);
				
		eventListener.RegisterMainAgent(agentZero);
	}
	
	@Override
	public void run()
	{
		Thread t = (new Thread() {
			  public void run() {
				  try {
						Thread.sleep(1);
						for(int i=0;i<3;i++)
						{
							int interval = 5000;
							int sinterval = 2000;
							
							Task taskGroup1 = new Task("TaskGroup1", new SeqSumQAF(), null);
							Task task1agent1 = new Task("Task1agent1",new SumAllQAF(), null, new Method[]{
								new Method("M1",13,0,100,100,8,null),
								new Method("M2",10,0,200,100,0,null),
								new Method("M3",11,0,300,100,0,null)
							});
							Task task2agent1 = new Task("Task2agent1",new ExactlyOneQAF(), null, new Method[]{
								new Method("M4",10,0,400,50,0,null),
								new Method("M5",70,0,400,150,0,null)
							});		
							taskGroup1.addTask(task1agent1);
							taskGroup1.addTask(task2agent1);
							//agentZero.assignTask(taskGroup1);
							
							Task taskGroup2 = new Task("TaskGroup2", new ExactlyOneQAF(), agentOne);
							Task task1agent2 = new Task("Task1agent2",new SumAllQAF(), agentOne, new Method[]{
								new Method("B1",8,0,100,250,0,null),
								new Method("B2",12,0,300,250,0,null)
							});
							Task task2agent2 = new Task("Task2agent2",new SumAllQAF(), agentOne, new Method[]{
								new Method("B3",10,0,100,350,0,null),
								new Method("B4",70,0,300,350,0,null)
							});		
							taskGroup2.addTask(task1agent2);
							taskGroup2.addTask(task2agent2);
							agentZero.AddPendingTask(taskGroup2);
							
							Thread.sleep(9*interval);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			  }
			 });
		t.start();
		
		//Start Agents
		Iterator it = agents.iterator();
		while(it.hasNext())
		{
			Agent agent = (Agent) it.next();
			Thread agentThread = new Thread(agent,agent.label);
			agentThread.start();
		}
		while(true)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//agentOne.assignTask(new Task("Emergency Station",new SumAllQAF(), new Method("Emergency Method",1,300,90), null));
	}
	
	public synchronized void addListener(WorldEventListener sl) {
        listeners.add(sl);
    }
 
    public synchronized void removeListener(WorldEventListener sl) {
        listeners.remove(sl);
    }

	@Override
	public void HandleWorldEvent(WorldEvent event) {
		if (event.taskType==TaskType.METHODCOMPLETED)
		{
			Main.Message(true, "[SimWorld4] " + event.methodId + " completed");
			//mainAgent.assignTask(new Task(event.methodId + " Task",new SumAllQAF(), new Method(event.methodId + " Method",1,1,event.xCoordinate,event.yCoordinate, 4, null), mainAgent));
		}
	
	}

	@Override
	public void RegisterMainAgent(IAgent agent) {
		// TODO Auto-generated method stub
		
	}
    
    

}
