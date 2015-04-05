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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.eclipse.paho.client.sample.MQTTAgent;
import raven.game.RoverBot;

public class AgentProcess extends Agent {

    private MQTTAgent client;
	public AgentProcess(String name, boolean isManagingAgent, int x, int y,
			MqttMessagingProvider mq)
	{
		super(name, isManagingAgent, x, y, mq);
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
	    	AgentProcess agent = new AgentProcess(name, isManaging, x, y, mq);
	    	if(args.length > 4) {
	    		for(int i = 4; i < args.length; i++) {
	    			String childAgentName = args[i];
	    			agent.AddChildAgent(childAgentName);
	    		}
			}
			
			}
			
		}
}

