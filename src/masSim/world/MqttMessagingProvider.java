package masSim.world;
import java.util.ArrayList;
import java.util.List;

import masSim.schedule.SchedulingEvent;
import masSim.schedule.SchedulingEventListener;
import masSim.taems.IAgent;
import masSim.taems.Task;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class MqttMessagingProvider implements MqttCallback{

	private String baseTopic;
	private int qos;
	private String broker;
	private MemoryPersistence persistence;
	private MqttClient client;
	private static MqttMessagingProvider provider;
	private List<SchedulingEventListener> schedulingEventListeners = new ArrayList<SchedulingEventListener>();
	
	public static MqttMessagingProvider GetMqttProvider()
	{
		if (provider==null)
			provider = new MqttMessagingProvider();
		return provider;
	}
	
	public void AddListener(SchedulingEventListener listener)
	{
		if(!schedulingEventListeners.contains(listener))
			schedulingEventListeners.add(listener);
	}
	
	private MqttMessagingProvider()
	{
		baseTopic = "txstate/rp/masSim/";
		qos = 2;
		broker = "tcp://test.mosquitto.org:1883";
		persistence = new MemoryPersistence();
		try {
			client = new MqttClient("tcp://test.mosquitto.org:1883", "MasSimMqttClient", persistence);
			//client = new MqttClient("tcp://dev.rabbitmq.com:1883", "MasSimMqttClient", persistence);
			client.setCallback(this);
			MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
			client.connect(connOpts);
			Thread.sleep(5000);//Wait for client to connect. TODO convert to synchronous method call for mqtt so it proceeds only when connected.
		} catch(MqttException me) {
        	DisplayMqttException(me);
        } catch(InterruptedException ex){
        	//Do nothing
        }
	}
	
	public void SubscribeForAgent(String agentName)
	{
		try {
			client.subscribe(GetAgentSpecificTopic(agentName));
		} catch(MqttException me) {
        	DisplayMqttException(me);
        }
	}
	
	public void PublishMessage(String agentName, String commandType, String commandText)
	{
		PublishMessage(agentName+","+commandType+","+commandText);
	}
	
	public void PublishMessage(String messageString)
	{
		try {
	        MqttMessage message = new MqttMessage();
	        message.setPayload(messageString.getBytes());
	        message.setQos(qos);
	        String topicForAgent = baseTopic + "/" + "schedulingMessages";
	        client.publish(topicForAgent, message);
	    } catch (MqttException e) {
	        e.printStackTrace();
	    }
	}

	
	private String GetAgentSpecificTopic(String agentName)
	{
		return baseTopic + "/" + agentName;
	}
	
	private void DisplayMqttException(MqttException me)
	{
		System.out.println("Mqtt call failed.");
		System.out.println("reason "+me.getReasonCode());
        System.out.println("msg "+me.getMessage());
        System.out.println("loc "+me.getLocalizedMessage());
        System.out.println("cause "+me.getCause());
        System.out.println("excep "+me);
        me.printStackTrace();
	}
	
	@Override
	public void connectionLost(Throwable arg0) {
		System.out.println("Connection has been lost " + arg0.toString());
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		System.out.println("Delivery complete");
		// TODO Auto-generated method stub
		
	}

	@Override
	public void messageArrived(String topic, MqttMessage message)
	        throws Exception {
		String messageContent = message.toString();
		String[] messageParts = messageContent.split(",");
		for(SchedulingEventListener listener : schedulingEventListeners)
		{
			SchedulingEvent event = new SchedulingEvent(messageParts[0],messageParts[1],messageParts[2]);
			listener.ProcessSchedulingEvent(event);
		}
	}
}
