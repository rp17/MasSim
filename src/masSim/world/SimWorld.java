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

public class SimWorld implements Runnable, WorldEventListener {

	private ArrayList<IAgent> agents;
	private ArrayList<Task> tasks;
	private ArrayList<WorldEventListener> listeners;
	private IAgent agentAmbulance;
	private IAgent agentPolice;
	private MqttMessagingProvider mq;
	
	public SimWorld(WorldEventListener eventListener)
	{
		agents = new ArrayList<IAgent>();
		tasks = new ArrayList<Task>();
		listeners = new ArrayList<WorldEventListener>();
		listeners.add(eventListener);
		listeners.add(this);
		
		//Initialize two agents, and specify their initial positions
		agentPolice = new Agent("Police", true, 40, 300, listeners, mq);
		agentAmbulance = new Agent("Ambulance", false, 40, 100, listeners, mq);
		agentPolice.AddChildAgent(agentAmbulance);
		agents.add(agentPolice);
		agents.add(agentAmbulance);
		eventListener.RegisterMainAgent(agentPolice);
	}
	
	@Override
	public void run()
	{	
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
