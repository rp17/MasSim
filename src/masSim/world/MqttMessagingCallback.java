package masSim.world;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import masSim.schedule.SchedulingEvent;
import masSim.schedule.SchedulingEventListener;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
//import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
//import org.eclipse.paho.client.sample.MQTTAgent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MqttMessagingCallback implements MqttCallback {
	private String callbackName;
	private List<SchedulingEventListener> schedulingEventListeners = new ArrayList<SchedulingEventListener>();
	private final ExecutorService evtPool = Executors.newSingleThreadExecutor();
	
	public MqttMessagingCallback(String name) {
		callbackName = name;
	}
	public void AddListener(SchedulingEventListener listener)
	{
		if(!schedulingEventListeners.contains(listener))
			schedulingEventListeners.add(listener);
	}
	public String getName(){return callbackName;}
	@Override
	public void connectionLost(Throwable arg0) {
		System.out.println("Connection has been lost " + arg0.toString());
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		System.out.println("Message delivered successfully");
	}

	@Override
	public void messageArrived(String topic, MqttMessage message) {
		try {
			ProcessArrivedMessage(new String(message.getPayload(), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	void ProcessArrivedMessage(final String message)
	{
		evtPool.execute( new Runnable(){
		 		@Override
		 		public void run() {
		 			final SchedulingEvent event = SchedulingEvent.Parse(message);
		//System.out.println("Message Received :" + event);
		//Passing of events to individual listeners selectively is done because on a single machine, we cannot simulate
		//the running of separate mqtt listeners in each agent thread, because the same TCP port number gets tied down.
		//However, the logic expects that not all events go to all listeners, hence filtering is being done here
		 			for(final SchedulingEventListener listener : schedulingEventListeners)
		 			{
		 				if (event.agentName.equals(listener.getName()) || listener.IsGlobalListener()) {
		 					listener.ProcessSchedulingEvent(event);
		 				}
		 			}
		 		}
		});
		
	}
}
