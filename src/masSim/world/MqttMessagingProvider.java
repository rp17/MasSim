package masSim.world;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.sample.MQTTAgent;

import masSim.schedule.SchedulingCommandType;
import masSim.schedule.SchedulingEvent;
import masSim.schedule.SchedulingEventListener;
import masSim.taems.IAgent;
import masSim.taems.Task;

//import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
//import org.eclipse.paho.client.mqttv3.MqttCallback;
//import org.eclipse.paho.client.mqttv3.MqttClient;
//import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
//import org.eclipse.paho.client.mqttv3.MqttTopic;
//import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
//import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class MqttMessagingProvider extends TestMQTTAgent {

	private boolean simulationMode = true;
	private String baseTopic;
	private int qos;
	private String broker;
	//private MemoryPersistence persistence;
	private MQTTAgent client;
	private static MqttMessagingProvider provider;
	private List<SchedulingEventListener> schedulingEventListeners = new ArrayList<SchedulingEventListener>();
	
	public static synchronized MqttMessagingProvider GetMqttProvider()
	{
		if (provider==null){
			try {
				provider = new MqttMessagingProvider();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("Mqtt Ready");
		}
		return provider;
	}
	
	public void AddListener(SchedulingEventListener listener)
	{
		if(!schedulingEventListeners.contains(listener))
			schedulingEventListeners.add(listener);
	}
	
	private MqttMessagingProvider() throws MqttException
	{
		if (!simulationMode)
			try {
					String nodeID = getNodeMacAddress();
					String brokerIPAddress =  "127.0.0.1"; //broken in this case is running in the same computer, it can be running in a different machine 
					int brokerPort = 1883; //default port for Really Small Message Broker - RSMB https://www.ibm.com/developerworks/community/groups/service/html/communityview?communityUuid=d5bedadd-e46f-4c97-af89-22d65ffee070
					String topic = "txstate/rp/masSim/"; //each MQTT agent can subscribe to multiple topics.  MacAddress + "In" is a default topic for each agent
					//String brokerUrl, String clientId, boolean cleanSession, boolean quietMode, String userName, String password
					String protocol = "tcp://";
					String url = protocol + brokerIPAddress + ":" + brokerPort;
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Calendar cal = Calendar.getInstance();
					
					String publishMessage = "Node: " + nodeID + " Time: " + dateFormat.format(cal.getTime());
					
					client = new MQTTAgent(url,nodeID,false, true, null, null);
					client.setCallBack(this);
					
					//QoS (0-means FireAndForget which is fastest; 1- means StoreAndForwardWithDuplicate which is bit slow; 2- means StoreAndForwardWithoutDuplciate which is slowest
					
					
//					
//					MessageQulityOfService[] subscriptionTopicQoS = { MessageQulityOfService.StoreAndForwardWithPossibleDuplicates }; //for each subscribed topic, there is an assigned QoS level, which is intuitive.
//					String publishTopic = nodeID + "Out"; //each MQTT agent can publish to any topics.  Default value is Mac
//					MessageQulityOfService publishTopicQoS = MessageQulityOfService.StoreAndForwardWithPossibleDuplicates;
//					
//					MQTTManager mqAgent = new MQTTManager( nodeID, brokerIPAddress, brokerPort);
//					//as a library, the MQTT manager has to be both subscriber and publisher.  Well it is a non-intuitive requirement
					//but given the task for both the evaluation application and the runtime verification middleware, to combine both together
					//is quickest and turns out most efficient in saving threads/avoiding concurrent common pitfalls -> especially when 
					//mqtt is required for Android.
//					mqAgent.setSubscribeTopics(subscriptionTopic, subscriptionTopicQoS);
//					mqAgent.connectToBroker(); //synchronous call, only when connected, when proceed to the next step.  Asynchronous connection is built, but not exposed yet
//					mqAgent.publishResponse(publishTopic, "Hello World".getBytes(), publishTopicQoS);
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	public void SubscribeForAgent(String agentName)
	{
		if (!this.simulationMode)
		{
			try {
				client.subscribe(GetAgentSpecificTopic(agentName), 1);
			} catch(MqttException me) {
	        	DisplayMqttException(me);
	        }
		}
	}
	
	public void PublishMessage(String agentName, SchedulingCommandType commandType, String commandText)
	{
		PublishMessage(agentName+","+commandType+","+commandText);
	}
	
	public void PublishMessage(SchedulingEvent event)
	{
		PublishMessage(event.agentName, event.commandType, event.params.toString());
	}
	
	public void PublishMessage(String messageString)
	{
		if (!simulationMode)
		{
			try {
				String agentName = messageString.substring(0, messageString.indexOf(","));
		        MqttMessage message = new MqttMessage();
		        message.setPayload(messageString.getBytes());
		        message.setQos(qos);
		        String topicForAgent = baseTopic + agentName;
		        client.publish(topicForAgent, 1, messageString.getBytes());
		    } catch (MqttException e) {
		        e.printStackTrace();
		    }
		}
		else
		{
			ProcessArrivedMessage(messageString);
		}
	}

	
	private String GetAgentSpecificTopic(String agentName)
	{
		return baseTopic + agentName;
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
	
	private void ProcessArrivedMessage(String message)
	{
		SchedulingEvent event = SchedulingEvent.Parse(message);
		//System.out.println("Message Recieved :" + event);
		//Passing of events to individual listeners selectively is done because on a single machine, we cannot simulate
		//the running of separate mqtt listeners in each agent thread, because the same TCP port number gets tied down.
		//However, the logic expects that not all events go to all listeners, hence a filtering is being done here
		for(SchedulingEventListener listener : schedulingEventListeners)
		{
			if (event.agentName.equals(listener.getName()) || listener.IsGlobalListener())
				listener.ProcessSchedulingEvent(event);
		}
	}
}
