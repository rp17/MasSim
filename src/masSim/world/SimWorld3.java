package masSim.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import raven.Main;
import raven.game.RavenGame;
import masSim.world.*;
import masSim.world.WorldEvent.TaskType;
import masSim.taems.*;

public class SimWorld3 {

	private List<IAgent> agents;
	private List<Task> tasks;
	private List<WorldEventListener> listeners;
	private IAgent mainAgent;
	
	public SimWorld3(WorldEventListener eventListener)
	{
		agents = new ArrayList<IAgent>();
		tasks = new ArrayList<Task>();
		listeners = new ArrayList<WorldEventListener>();
		listeners.add(eventListener);
		
		//Initialize two agents, and specify their initial positions
		Agent agentOne = new Agent("Helicopter0", true, 40, 100, listeners);
		Agent agentTwo = new Agent("Helicopter1", false, 40, 200, listeners);
		mainAgent = agentOne;
		agentOne.AddChildAgent(agentTwo);
		agents.add(agentOne);
		agents.add(agentTwo);
				
		eventListener.RegisterMainAgent(agentOne);
	}
	
	public List<IAgent> initAgents()
	{
					
				Method m_from = new Method("Visit Station A1",10,100,110);
				mainAgent.assignTask(new Task("Station A1",new SumAllQAF(), mainAgent, m_from));
				mainAgent.assignTask(new Task("Station A2",new SumAllQAF(), mainAgent, new Method("Visit Station A2",10,200,90)));
				mainAgent.assignTask(new Task("Station A3",new SumAllQAF(), mainAgent, new Method("Visit Station A3",10,300,110)));
				mainAgent.assignTask(new Task("Station A4",new SumAllQAF(), mainAgent, new Method("Visit Station A4",10,400,90)));
				mainAgent.assignTask(new Task("Station A5",new SumAllQAF(), mainAgent, new Method("Visit Station A5",10,500,110)));
				mainAgent.assignTask(new Task("Station A6",new SumAllQAF(), mainAgent, new Method("Visit Station A6",10,600,90)));
				
				//Thread.sleep(interval);
				Method m_to = new Method("Visit Station B1",1,100,210);
				m_to.AddInterrelationship(new Interrelationship(m_from, m_to, new Outcome(100,1,1)));
				mainAgent.assignTask(new Task("Station B1",new SumAllQAF(), agents.get(1), m_to));
				mainAgent.assignTask(new Task("Station B2",new SumAllQAF(), agents.get(1), new Method("Visit Station B2",1,200,190)));
				mainAgent.assignTask(new Task("Station B3",new SumAllQAF(), agents.get(1), new Method("Visit Station B3",1,300,210)));
				mainAgent.assignTask(new Task("Station B4",new SumAllQAF(), agents.get(1), new Method("Visit Station B4",1,400,190)));
				mainAgent.assignTask(new Task("Station B5",new SumAllQAF(), agents.get(1), new Method("Visit Station B5",1,500,210)));
				mainAgent.assignTask(new Task("Station B6",new SumAllQAF(), agents.get(1), new Method("Visit Station B6",1,600,190)));

				
				
		//Start Agents
				
				
		Iterator<IAgent> it = agents.iterator();
		while(it.hasNext())
		{
			Agent agent = (Agent) it.next();
			Thread agentThread = new Thread(agent,agent.label);
			agentThread.start();
		}
		return agents;
		
		//agentOne.assignTask(new Task("Emergency Station",new SumAllQAF(), new Method("Emergency Method",1,300,90), null));
	}
	
	public synchronized void addListener(WorldEventListener sl) {
        listeners.add(sl);
    }
 
    public synchronized void removeListener(WorldEventListener sl) {
        listeners.remove(sl);
    }
    
    

}
