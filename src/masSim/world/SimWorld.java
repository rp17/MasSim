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
	private IAgent agentAmbulance;
	private IAgent agentPolice;
	private MqttMessagingProvider mq;
	private ExecutorService agentPool;
	private String clientName = "simWorld";
	public SimWorld(RavenUI ui, ExecutorService threadPool)
	{
		this.agentPool = threadPool;
		agents = new ArrayList<IAgent>();
		tasks = new ArrayList<Task>();
		mq = MqttMessagingProvider.GetMqttProvider(clientName, "127.0.0.1", 1883);
		mq.AddListener(ui);
	}
	
	public void InitializeAndRun()
	{
		//Initialize two agents, and specify their initial positions
		mq.SubscribeForAgent(RavenUI.schedulingEventListenerName);//GUI application simulates as a listening agent to catch events
		agentPolice = new Agent("Police", true, 40, 300, mq);
		agentAmbulance = new Agent("Ambulance", false, 40, 100, mq);
		agentPolice.AddChildAgent(agentAmbulance.getName());
		agents.add(agentPolice);
		agents.add(agentAmbulance);
		agentPool.execute((Runnable) agentAmbulance);
		agentPool.execute((Runnable) agentPolice);
	}
    
}
