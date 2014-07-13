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
	private IAgent mainAgent;
	
	public SimWorld(WorldEventListener eventListener)
	{
		agents = new ArrayList<IAgent>();
		tasks = new ArrayList<Task>();
		listeners = new ArrayList<WorldEventListener>();
		listeners.add(eventListener);
		
		//Initialize two agents, and specify their initial positions
		Agent agentOne = new Agent("Helicopter0", true, 20, 100, listeners);
		Agent agentTwo = new Agent("Helicopter1", false, 100, 300, listeners);
		mainAgent = agentOne;
		agentOne.AddChildAgent(agentTwo);
		agents.add(agentOne);
		agents.add(agentTwo);
				
		eventListener.RegisterMainAgent(agentOne);
	}
	
	@Override
	public void run()
	{
				
		//Initialize a bunch of tasks
		//mainAgent.assignTask(new Task("Station 1",new SumAllQAF(), new Method("Visit Station 1",1,100,100), mainAgent));
		//mainAgent.assignTask(new Task("Station 2",new SumAllQAF(), new Method("Visit Station 2",1,100,200), null));
		//mainAgent.assignTask(new Task("Station 3",new SumAllQAF(), new Method("Visit Station 3",1,250,200), null));
		//mainAgent.assignTask(new Task("Station 4",new SumAllQAF(), new Method("Visit Station 4",1,100,200), mainAgent));
		
		//mainAgent.assignTask(new Task("Station 5",new SumAllQAF(), new Method("Visit Station 5",1,100,250), mainAgent));
		//mainAgent.assignTask(new Task("Station 6",new SumAllQAF(), new Method("Visit Station 6",1,300,250), mainAgent));
		//mainAgent.assignTask(new Task("Station 7",new SumAllQAF(), new Method("Visit Station 7",1,300,350), mainAgent));
		//mainAgent.assignTask(new Task("Station 8",new SumAllQAF(), new Method("Visit Station 8",1,100,350), mainAgent));
		
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
    
    

}
