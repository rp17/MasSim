package masSim.world;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import raven.Main;
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
		Agent agentOne = new Agent("Helicopter0", true, 75, 100, listeners);
		Agent agentTwo = new Agent("Helicopter1", false, 75, 200, listeners);
		mainAgent = agentOne;
		agentOne.AddChildAgent(agentTwo);
		agents.add(agentOne);
		agents.add(agentTwo);
				
		eventListener.RegisterMainAgent(agentOne);
	}
	
	@Override
	public void run()
	{
		//mainAgent.assignTask(new Task("Station 1",new SumAllQAF(), new Method("Visit Station 1",1,100,100), mainAgent));
		//mainAgent.assignTask(new Task("Station 2",new SumAllQAF(), new Method("Visit Station 2",1,100,200), mainAgent));
		//mainAgent.assignTask(new Task("Station 3",new SumAllQAF(), new Method("Visit Station 3",1,350,200), mainAgent));
		//mainAgent.assignTask(new Task("Station 4",new SumAllQAF(), new Method("Visit Station 4",1,350,100), mainAgent));
		
		
		Thread t = (new Thread() {
			  public void run() {
				  try {
						//mainAgent.assignTask(new Task("Station 1",new SumAllQAF(), new Method("Visit Station 1",1,100,100), mainAgent));
						//mainAgent.assignTask(new Task("Station 2",new SumAllQAF(), new Method("Visit Station 2",1,100,200), mainAgent));
						//mainAgent.assignTask(new Task("Station 3",new SumAllQAF(), new Method("Visit Station 3",1,350,200), mainAgent));
						//mainAgent.assignTask(new Task("Station 4",new SumAllQAF(), new Method("Visit Station 4",1,350,100), mainAgent));
						Thread.sleep(1);
						for(int i=0;i<3;i++)
						{
							int interval = 5000;
							int sinterval = 2000;
							mainAgent.assignTask(new Task("Station A1",new SumAllQAF(), new Method("Visit Station A1",1,100,110), mainAgent));
							mainAgent.assignTask(new Task("Station A2",new SumAllQAF(), new Method("Visit Station A2",1,200,90), mainAgent));
							mainAgent.assignTask(new Task("Station A3",new SumAllQAF(), new Method("Visit Station A3",1,300,110), mainAgent));
							mainAgent.assignTask(new Task("Station A4",new SumAllQAF(), new Method("Visit Station A4",1,400,90), mainAgent));
							mainAgent.assignTask(new Task("Station A4",new SumAllQAF(), new Method("Visit Station A5",1,500,110), mainAgent));
							mainAgent.assignTask(new Task("Station A4",new SumAllQAF(), new Method("Visit Station A6",1,600,90), mainAgent));
							
							Thread.sleep(interval);
							mainAgent.assignTask(new Task("Station B1",new SumAllQAF(), new Method("Visit Station A1",1,100,210), agents.get(1)));
							mainAgent.assignTask(new Task("Station B2",new SumAllQAF(), new Method("Visit Station B2",1,200,190), agents.get(1)));
							mainAgent.assignTask(new Task("Station B3",new SumAllQAF(), new Method("Visit Station B3",1,300,210), agents.get(1)));
							mainAgent.assignTask(new Task("Station B4",new SumAllQAF(), new Method("Visit Station B4",1,400,190), agents.get(1)));
							mainAgent.assignTask(new Task("Station B4",new SumAllQAF(), new Method("Visit Station B5",1,500,210), agents.get(1)));
							mainAgent.assignTask(new Task("Station B4",new SumAllQAF(), new Method("Visit Station B6",1,600,190), agents.get(1)));
							Thread.sleep(interval);
							//mainAgent.assignTask(new Task("EMG",new SumAllQAF(), new Method("EMG",1,350,120), null));
							mainAgent.assignTask(new Task("EMG",new SumAllQAF(), new Method("EMG",1,240,220), null));
							Thread.sleep(7*interval);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			  }
			 });
		t.start();
		
		//Initialize a bunch of tasks
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
