package masSim.world;

import java.util.List;

import masSim.schedule.IScheduleUpdateEventListener;
import masSim.schedule.ScheduleUpdateEvent;
import masSim.schedule.Scheduler;
import masSim.taems.*;

import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import raven.Main;
import raven.math.Vector2D;
import raven.ui.GameCanvas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.eclipse.paho.client.sample.MQTTAgent;

import masSim.world.SimBot;
import raven.game.interfaces.IBot;
import masSim.goals.Goal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AgentProcess {

	private final ExecutorService botUpdatePool = Executors.newSingleThreadExecutor();
    private MQTTAgent client;
    private SimBot bot;
    private IAgent agent;
    private volatile boolean active = true;
    private BotRunnable botRunnable;
	public AgentProcess(String name, boolean isManagingAgent, int x, int y,
			MqttMessagingProvider mq)
	{
		this.agent = new Agent(name, isManagingAgent, x, y, mq);
		Vector2D pos = new Vector2D(x, y);
		this.bot = new SimBot(agent, pos, Goal.GoalType.goal_roverthink);
		botRunnable = new BotRunnable(bot);
	}
	public IAgent getAgent() {return agent;}
	public SimBot getBot() {return bot;}
	public void startBot() {
		botUpdatePool.execute(botRunnable);
	}
	public void stopBot() {
		active = false;
		botUpdatePool.shutdown();
	}
	protected class BotRunnable implements Runnable {
		private final SimBot bot;
		public BotRunnable(SimBot bot) {
			this.bot = bot;
		}
		public void run() {
	    	long lastTime = System.nanoTime();
	    	
	    	while (active) {
	    		// TODO Resize UI if the map changes!
	    		
	    		long currentTime = System.nanoTime();

	    		bot.update((currentTime - lastTime) * 1.0e-9); // converts nano to seconds
	    		lastTime = currentTime;
	    		

	    		long millisToNextUpdate = (long) Math.max(0, 16.66667 - (System.nanoTime() - currentTime)*1.0e-6);
				
				try {
					Thread.sleep(millisToNextUpdate);
				} catch (InterruptedException e) {
					break;
				}
	    	}
		}
	}
	public static void main(String[] args) {
		
		if(args.length < 4) {
			System.out.println("Command line should contain: name, true|false, x, y");
		}
		else {
			
			String name = args[0];
			String isManagingS = args[1];
			String xS = args[2];
			String yS = args[3];
			
			int x = 0;
			int y = 0;
			
			boolean isManaging = false;
			try {
				x = Integer.parseInt(xS);
		    }
		    catch(NumberFormatException e) {
		    	System.err.println(e.getMessage() + ", X coord cannot be parsed to integer");
		    }
			try {
				y = Integer.parseInt(yS);
		    }
		    catch(NumberFormatException e) {
		    	System.err.println(e.getMessage() + ", Y coord cannot be parsed to integer");
		    }
			
			try {
				isManaging = Boolean.parseBoolean(isManagingS);
		    }
		    catch(NumberFormatException e) {
		    	System.err.println(e.getMessage() + ", boolean isManaging cannot be parsed to boolean");
		    }
			
			MqttMessagingProvider mq = MqttMessagingProvider.GetMqttProvider();
	    	AgentProcess agentProc = new AgentProcess(name, isManaging, x, y, mq);
	    	IAgent agent = agentProc.getAgent();
	    	if(args.length > 4) {
	    		for(int i = 4; i < args.length; i++) {
	    			String childAgentName = args[i];
	    			agent.AddChildAgent(childAgentName);
	    		}
			}
			
			
			
		}
	}
}

