package masSim.world;
import masSim.taems.IAgent;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

public class MqttMessagingProvider{

	private String baseTopic;
	private int qos;
	private String broker;
	private MemoryPersistence persistence;
	private MqttClient client;
	
	public MqttMessagingProvider(MqttCallback listener)
	{
		baseTopic = "txstate/rp/masSim/";
		qos = 2;
		broker = "tcp://test.mosquitto.org:1883";
		persistence = new MemoryPersistence();
		try {
			client = new MqttClient("tcp://test.mosquitto.org:1883", "MasSimMqttClient", persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
			client.connect(connOpts);
	        client.setCallback(listener);
		} catch(MqttException me) {
        	DisplayMqttException(me);
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
	
	public void PublishMessage(String messageText, String agentName)
	{
		try {
	        MqttMessage message = new MqttMessage();
	        message.setPayload(messageText.getBytes());
	        message.setQos(qos);
	        String topicForAgent = baseTopic + "/" + agentName;
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
}
