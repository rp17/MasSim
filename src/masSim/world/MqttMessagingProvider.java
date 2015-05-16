package masSim.world;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.sample.MQTTAgent;

import masSim.schedule.SchedulingCommandType;
import masSim.schedule.SchedulingEvent;
import masSim.schedule.SchedulingEventListener;
import masSim.taems.IAgent;
import masSim.taems.Task;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import raven.Main;
//import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
//import org.eclipse.paho.client.mqttv3.MqttCallback;
//import org.eclipse.paho.client.mqttv3.MqttClient;
//import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
//import org.eclipse.paho.client.mqttv3.MqttException;
//import org.eclipse.paho.client.mqttv3.MqttMessage;
//import org.eclipse.paho.client.mqttv3.MqttTopic;
//import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
//import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class MqttMessagingProvider implements MqttCallback {

	private boolean simulationMode = false;
	private String baseTopic = "";
	private int qos;
	private String broker;
	//private MemoryPersistence persistence;
	private MQTTAgent client;
	private String clientName;
	private String ipAddress;
	private int port;
	private static MqttMessagingProvider provider;

	private List<SchedulingEventListener> schedulingEventListeners = new ArrayList<SchedulingEventListener>();
	private final ExecutorService evtPool = Executors.newSingleThreadExecutor();
	protected volatile int publishCounter;
	
	public static synchronized MqttMessagingProvider GetMqttProvider(){return provider;}
	
	public static synchronized MqttMessagingProvider GetMqttProvider(String name, String ipAddress, int port)
	{
		if (provider==null){
			if(name == null) {
				Main.Message(true, "MqttMessagingProvider.GetMqttProvider: no client name passed, returning null");
				//System.exit(0);
				return null;
			}
			try {
				provider = new MqttMessagingProvider(name, ipAddress, port);
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
		System.out.println("MqttMessagingProvider.AddListener added " + listener.getName());
	}
	public String getName() {return clientName;}
	public MQTTAgent getClient() {return client;}
	private MqttMessagingProvider(String name, String ipAddress, int port) throws MqttException
	{
		clientName = name;
		this.ipAddress = ipAddress;
		this.port = port;
		if (!simulationMode)
			try {
					String nodeID = getNodeMacAddress() + "_" + name;
					//String brokerIPAddress =  "127.0.0.1"; //broker in this case is running in the same computer, it can be running in a different machine 
					//int brokerPort = 1883; //default port for Really Small Message Broker - RSMB https://www.ibm.com/developerworks/community/groups/service/html/communityview?communityUuid=d5bedadd-e46f-4c97-af89-22d65ffee070
					//String topic = "txstate/rp/masSim/"; //each MQTT agent can subscribe to multiple topics.  MacAddress + "In" is a default topic for each agent
					//String brokerUrl, String clientId, boolean cleanSession, boolean quietMode, String userName, String password
					String protocol = "tcp://";
					String url = protocol + ipAddress + ":" + port;
					client = new MQTTAgent(url,nodeID,true, true, null, null); // clean messages
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
	/*
	public void createCallback() {
		try {
			client.setCallBack(new MqttMessagingProvider(callbackName));
		} catch (MqttException e) {
			DisplayMqttException(e);
		}
	}
	*/
	public void SubscribeForAgent(String agentName)
	{
		
			try {
				//String topic = GetAgentSpecificTopic(agentName);
				String topic = agentName;
				//Main.Message(true, "MqttMessagingProvider.GetMqttProvider: client.subscribe to topic " + topic);
				
				client.subscribe(topic, 2);
				//Main.Message(true, "MqttMessagingProvider.GetMqttProvider: have subscribed to client");
			} catch(MqttException me) {
	        	DisplayMqttException(me);
	        }
		
	}
	
	public static String getNodeMacAddress(){
		 try {
			    InetAddress ip = InetAddress.getLocalHost();
			    System.out.println("Current IP address : " + ip.getHostAddress());

			    Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
			    while(networks.hasMoreElements()) {
			      NetworkInterface network = networks.nextElement();
			      byte[] mac = network.getHardwareAddress();

			      if(mac != null) {
			        System.out.print("Current MAC address : ");

			        StringBuilder sb = new StringBuilder();
			        for (int i = 0; i < mac.length; i++) {
			          sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
			        }
			        System.out.println(sb.toString());
			        return sb.toString();
			      }
			    }
			    return null;
		  } catch (UnknownHostException e) {
		    e.printStackTrace();
		    return null;
		  } catch (SocketException e){
		    e.printStackTrace();
		    return null;
		  }
	}

	/*
	public void PublishMessage(String agentName, SchedulingCommandType commandType, String commandText)
	{
		PublishMessage(agentName+","+commandType+","+commandText);
	}
	*/
	public void PublishMessage(SchedulingEvent event)
	{
		//PublishMessage(event.agentName, event.commandType, event.params.toString());
		PublishMessage(event.rawMessage);
	}
	
	public void asyncPublishMessage(final SchedulingEvent event)
	{
		evtPool.execute( new Runnable(){
	 		@Override
	 		public void run() {
	 			//PublishMessage(event.agentName, event.commandType, event.params.toString());
	 			PublishMessage(event);
	 		}
		});
	}
	/*
	public void publishMsg(String agentName, String msg) {
		try {
	
	        MqttMessage message = new MqttMessage();
	        message.setPayload(msg.getBytes());
	        message.setQos(qos);
	        String topicForAgent = baseTopic + agentName;
	        client.publish(topicForAgent, 1, msg.getBytes());
	    } catch (MqttException e) {
	        e.printStackTrace();
	    }
	}
	*/
	
	/*
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
		        Main.Message(this, true, ": about to client.publish " + messageString + " to topic " + topicForAgent);
		        client.publish(topicForAgent, 1, messageString.getBytes());
		        Main.Message(this, true, ": have published " + messageString + " to topic " + topicForAgent);
		    } catch (MqttException e) {
		        e.printStackTrace();
		    }
		}
		else
		{
			ProcessArrivedMessage(messageString);
		}
	}
	 */
	
	public void PublishMessage(String messageString)
	{
		publishMessage(messageString);
	}
	
	/*
	public void asyncPublishMessage(final String messageString){
		evtPool.execute( new Runnable(){
	 		@Override
	 		public void run() {
	 			try {
	 				String agentName = messageString.substring(0, messageString.indexOf(","));
	 				MqttMessage message = new MqttMessage();
	 				message.setPayload(messageString.getBytes());
	 				message.setQos(qos);
	 				String topicForAgent = baseTopic + agentName;
	 				Main.Message(this, true, ": about to client.publish " + messageString + " to topic " + topicForAgent);
	 				client.publish(topicForAgent, 1, messageString.getBytes());
	 				Main.Message(this, true, ": have published " + messageString + " to topic " + topicForAgent);
	 			} catch (MqttException e) {
	 				e.printStackTrace();
	 			}
	 		}
		});
	}
	*/
	public void asyncPublishMessage(final String messageString){
		evtPool.execute( new Runnable(){
	 		@Override
	 		public void run() {
	 			publishMessage(messageString);
	 		}
	 		
	 	});
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
			String msg = new String(message.getPayload(), "UTF-8");
			System.out.println("MqttMessagingProvider.messageArrived  Topic: " + topic);
			System.out.println("MqttMessagingProvider.messageArrived  Message: " + msg);
			ProcessArrivedMessage(msg);
			System.out.println("MqttMessagingProvider.messageArrived message processed");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// James' async publish message
	public void publishMessage(String message) {
		publishMessage(message, 2);
	}
	
	// James' async publish message
		public void publishMessage(String message, final int QOS) {
			//UUID clientUID = UUID.randomUUID();
			// mq = MqttMessagingProvider.GetMqttProvider(TaskIssuerName , ipAddress, port);
			// //Main.Message(this, true, ": about to publish");
			// mq.PublishMessage(message);
			//
			final String eventMessage = message;
			//quick fix to implement asynchronous way of sending
			try {
				final MqttAsyncClient client = new MqttAsyncClient("tcp://" + ipAddress + ":" + port, MqttAsyncClient.generateClientId());
				client.connect( null, new IMqttActionListener() {
					@Override
					public void onSuccess(IMqttToken asyncActionToken) {
						// while (true) {
						try{
							publishCounter++;
							String agentName = eventMessage.substring(0, eventMessage.indexOf(","));
							client.publish(agentName, eventMessage.getBytes(), QOS, false);
							System.out.println("publish #" + publishCounter);
						}
						catch (MqttException e) {
							e.printStackTrace();
						}
						// }
					}
				
					@Override
					public void onFailure(IMqttToken arg0, Throwable arg1) {
						// TODO Auto-generated method stub

					}
				});
				} catch (MqttException me) {
					//e.printStackTrace();
					DisplayMqttException(me);
				}
			}
	/*
	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("Topic: " + arg0);
		System.out.println("Message: " + new String(arg1.getPayload(), "UTF-8"));
	}
	*/
	
	private void asyncProcessArrivedMessage(final String message)
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
	
	
	private void ProcessArrivedMessage(String message)
	{
		SchedulingEvent event = SchedulingEvent.Parse(message);
		System.out.println("MqttMessagingProvider.ProcessArrivedMessage Message Received :" + event);
		//Passing of events to individual listeners selectively is done because on a single machine, we cannot simulate
		//the running of separate mqtt listeners in each agent thread, because the same TCP port number gets tied down.
		//However, the logic expects that not all events go to all listeners, hence filtering is being done here
		for(SchedulingEventListener listener : schedulingEventListeners)
		{
			if (event.agentName.equals(listener.getName()) || listener.IsGlobalListener()) {
				System.out.println("MqttMessagingProvider.ProcessArrivedMessage invoked ProcessSchedulingEvent on " + listener.getName());
				listener.ProcessSchedulingEvent(event);
			}
		}
	}
	
	// this main method is for testing
	public static void main(String[] args) throws Exception {
		//String nodeID = getNodeMacAddress();
		String nodeID = "agentSub";
		String brokerIPAddress =  "127.0.0.1"; //broken in this case is running in the same computer, it can be running in a different machine 
		int brokerPort = 1883; //default port for Really Small Message Broker - RSMB https://www.ibm.com/developerworks/community/groups/service/html/communityview?communityUuid=d5bedadd-e46f-4c97-af89-22d65ffee070
		//String topic = "shared"; //each MQTT agent can subscribe to multiple topics.  MacAddress + "In" is a default topic for each agent
		String topic = "Ambulance";
		
		//String brokerUrl, String clientId, boolean cleanSession, boolean quietMode, String userName, String password
		String protocol = "tcp://";
		String url = protocol + brokerIPAddress + ":" + brokerPort;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Calendar cal = Calendar.getInstance();
		
		String publishMessage = "Node: " + nodeID + " Time: " + dateFormat.format(cal.getTime());
		
		//MQTTAgent mqAgent = new MQTTAgent(url,nodeID,false, true, null, null );
		MqttMessagingProvider providerMqtt = new MqttMessagingProvider(nodeID, brokerIPAddress, brokerPort);
		//MQTTAgent mqAgent = providerMqtt.getClient();
		//mqAgent.setCallBack(providerMqtt);
		//QoS (0-means FireAndForget which is fastest; 1- means StoreAndForwardWithDuplicate which is bit slow; 2- means StoreAndForwardWithoutDuplciate which is slowest
		
		SchedulingEvent evt = new SchedulingEvent(topic, SchedulingCommandType.INITMSG, "started");
		providerMqtt.PublishMessage(evt);
		//mqAgent.publish(topic, 1, publishMessage.getBytes());
		//mqAgent.subscribe(topic, 1);
		
		providerMqtt.SubscribeForAgent(topic);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));	
		try {
			br.readLine();
		} 
		catch (IOException ioe) {}
		
		for(int i=0; i<10; i++) {
			//String hellowS = "Hello " + i;
			
			//mqAgent.publish(topic, 1, hellowS.getBytes());
			evt = new SchedulingEvent(topic, SchedulingCommandType.INITMSG, "msg# " + i);
			providerMqtt.PublishMessage(evt);
		}
		
		try {
			br.readLine();
		} 
		catch (IOException ioe) {}
//		
		
//		MessageQulityOfService[] subscriptionTopicQoS = { MessageQulityOfService.StoreAndForwardWithPossibleDuplicates }; //for each subscribed topic, there is an assigned QoS level, which is intuitive.
//		String publishTopic = nodeID + "Out"; //each MQTT agent can publish to any topics.  Default value is Mac
//		MessageQulityOfService publishTopicQoS = MessageQulityOfService.StoreAndForwardWithPossibleDuplicates;
//		
//		MQTTManager mqAgent = new MQTTManager( nodeID, brokerIPAddress, brokerPort);
//		//as a library, the MQTT manager has to be both subscriber and publisher.  Well it is a non-intuitive requirement
		//but given the task for both the evaluation application and the runtime verification middleware, to combine both together
		//is quickest and turns out most efficient in saving threads/avoiding concurrent common pitfalls -> especially when 
		//mqtt is required for Android.
//		mqAgent.setSubscribeTopics(subscriptionTopic, subscriptionTopicQoS);
//		mqAgent.connectToBroker(); //synchronous call, only when connected, when proceed to the next step.  Asynchronous connection is built, but not exposed yet
//		mqAgent.publishResponse(publishTopic, "Hello World".getBytes(), publishTopicQoS);
	}
}
