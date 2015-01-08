package masSim.world;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import raven.Main;
import raven.math.Vector2D;
import masSim.world.*;
import masSim.taems.*;

public class SimWorld implements Runnable {

	private ArrayList<IAgent> agents;
	private ArrayList<Task> tasks;
	private IAgent agentAmbulance;
	private IAgent agentPolice;
	private MqttMessagingProvider mq;
	
	public SimWorld()
	{
		agents = new ArrayList<IAgent>();
		tasks = new ArrayList<Task>();
		mq = MqttMessagingProvider.GetMqttProvider();
	}
	
	public void Initialize()
	{
		//Initialize two agents, and specify their initial positions
		agentPolice = new Agent("Police", true, 40, 300, mq);
		agentAmbulance = new Agent("Ambulance", false, 40, 100, mq);
		agentPolice.AddChildAgent(agentAmbulance);
		agents.add(agentPolice);
		agents.add(agentAmbulance);
	}
	
	public MqttMessagingProvider getMqttMessagingProvider()
	{
		return mq;
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
    
}
