package masSim.world;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import raven.Main;
import raven.TaskIssuer;
import raven.math.Vector2D;
import raven.ui.RavenUI;
import masSim.world.*;
import masSim.schedule.SchedulingCommandType;
import masSim.schedule.SchedulingEvent;
import masSim.taems.*;

public class SimWorld {

	private ArrayList<IAgent> agents;
	private ArrayList<Task> tasks;
	private MqttMessagingProvider mq;
	private ExecutorService agentPool;
	
	public SimWorld(RavenUI ui, ExecutorService threadPool)
	{
		this.agentPool = threadPool;
		agents = new ArrayList<IAgent>();
		tasks = new ArrayList<Task>();
		mq = MqttMessagingProvider.GetMqttProvider();
		mq.AddListener(ui);
	}
	
	public static ArrayList<IAgent> CreateAgents(MqttMessagingProvider mq)
	{
		ArrayList<IAgent> agents = new ArrayList<IAgent>();
		
		int min = 100;
		int max = 600;
		
		for(int i=1;i<=Main.NumberOfAgents;i++)
		{
			int x = (int)(Math.random() * (max - min) + min);
			IAgent agent = new Agent("A" + i, true, x, 500, mq);//right, down from top
			if (agents.size()>0)
			{
				agents.get(0).AddChildAgent(agent);
			}
			agents.add(agent);	
		}
		return agents;
	}
	
	public void InitializeAndRun()
	{
		//Initialize two agents, and specify their initial positions
		mq.SubscribeForAgent(RavenUI.schedulingEventListenerName);//GUI application simulates as a listening agent to catch events
		agents = CreateAgents(mq);
		for(IAgent ag : this.agents)
		{
			agentPool.execute((Runnable) ag);	
		}	
	}
    
}
