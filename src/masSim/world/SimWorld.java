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
	
	@Override
	public void run()
	{
		//Initialize two agents, and specify their initial positions
		Agent agentOne = new Agent("Helicopter1", true, 20, 100, listeners);
		Agent agentTwo = new Agent("Helicopter2", false, 20, 300, listeners);
		agents.add(agentOne);
		agents.add(agentTwo);
		
		//Initialize a bunch of tasks
		agentOne.assignTask(new Task("Station 1",new SumAllQAF(), new Method("1",1,100,100), agentOne));
		agentOne.assignTask(new Task("Station 2",new SumAllQAF(), new Method("2",1,300,100), agentOne));
		agentOne.assignTask(new Task("Station 3",new SumAllQAF(), new Method("3",1,300,150), agentOne));
		agentOne.assignTask(new Task("Station 4",new SumAllQAF(), new Method("4",1,100,150), agentOne));
		
		agentTwo.assignTask(new Task("Station 5",new SumAllQAF(), new Method("5",1,100,300), agentTwo));
		agentTwo.assignTask(new Task("Station 6",new SumAllQAF(), new Method("6",1,300,300), agentTwo));
		agentTwo.assignTask(new Task("Station 7",new SumAllQAF(), new Method("7",1,300,350), agentTwo));
		agentTwo.assignTask(new Task("Station 8",new SumAllQAF(), new Method("8",1,100,350), agentTwo));
		
		//Start Agents
		Iterator it = agents.iterator();
		while(it.hasNext())
		{
			Agent agent = (Agent) it.next();
			Thread agentThread = new Thread(agent,agent.label);
			agentThread.start();
		}
		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		agentOne.assignTask(new Task("Emergency",new SumAllQAF(), new Method("Emergency",1,200,200)));
	}
	
	public synchronized void addListener(WorldEventListener sl) {
        listeners.add(sl);
    }
 
    public synchronized void removeListener(WorldEventListener sl) {
        listeners.remove(sl);
    }
    
    

}
