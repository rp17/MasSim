package masSim.world;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.sample.MQTTAgent;



public abstract class TestMQTTAgent implements MqttCallback {
		
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

	@Override
	public abstract void connectionLost(Throwable arg0);

	@Override
	public abstract void deliveryComplete(IMqttDeliveryToken arg0);

	@Override
	public abstract void messageArrived(String arg0, MqttMessage arg1);

}