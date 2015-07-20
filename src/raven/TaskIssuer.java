package raven;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import masSim.schedule.SchedulingCommandType;
import masSim.schedule.SchedulingEvent;
import masSim.schedule.SchedulingEventListener;
import masSim.world.Agent;
import masSim.world.LapsedTime;
import masSim.world.MqttMessagingProvider;
import masSim.world.WorldState;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class TaskIssuer implements Runnable, SchedulingEventListener {
	private List<String> MasterTaskList = new ArrayList<String>();
	//private List<String> TasksPendingCompletion = new ArrayList<String>();
	
	private List<String> MasterTaskNameList = new ArrayList<String>();
	private List<String> TaskNamesPendingCompletion = new ArrayList<String>();
	//private volatile MqttMessagingProvider mq;
	private volatile MqttMessagingProvider mqReceiver;
	//private String clientName = "taskIssuer";
	public static final String ambName = "Ambulance";
	public static final String polName = "Police";
	public static final String ambName2 = "Ambulance2";
	public static final String ambName3 = "Ambulance3";
	public static String TaskIssuerName = "TaskIssuer";
	private final static ExecutorService commsPool = Executors.newSingleThreadExecutor();
	private final static ExecutorService issuerPool = Executors.newSingleThreadExecutor();
	private volatile boolean paused = false;
	public volatile boolean active = true;

	//test the limits for "too many publishes in progress" error
	private volatile int publishCounter = 0;

	private volatile static int numberOfIteration = 0;
	private volatile static int currentIteration = 0;

	private Object pauseLock = new Object();
	private String ipAddress;
	private int port;
	public TaskIssuer(String ipAddress, int port)
	{
		this.ipAddress = ipAddress;
		this.port = port;
		//Create list of tasks to be executed in a loop
		MasterTaskList.add("Ambulance,ASSIGNTASK,----PickPatient");
		MasterTaskList.add("Ambulance,ASSIGNTASK,----DropPatient");
		
		MasterTaskList.add("Ambulance2,ASSIGNTASK,----PickPatient2");
		MasterTaskList.add("Ambulance2,ASSIGNTASK,----DropPatient2");
		
		MasterTaskList.add("Ambulance3,ASSIGNTASK,----PickPatient3");
		MasterTaskList.add("Ambulance3,ASSIGNTASK,----DropPatient3");
		
		MasterTaskList.add("Police,ASSIGNTASK,----Patrol");
		
		MasterTaskNameList.add("PickPatient");
		MasterTaskNameList.add("DropPatient");
		MasterTaskNameList.add("PickPatient2");
		MasterTaskNameList.add("DropPatient2");
		MasterTaskNameList.add("PickPatient3");
		MasterTaskNameList.add("DropPatient3");
		MasterTaskNameList.add("Patrol");
		MasterTaskNameList.add("RespondToAccident");
		MasterTaskNameList.add("RespondToAccident2");
		
		MasterTaskList.add("Police,NEGOTIATE,----RespondToAccident");
		MasterTaskList.add("Police,NEGOTIATE,----RespondToAccident2");
		//TasksToExecute.add("");
		//Main.Message(this, true, ": have added tasks");
		//MasterTaskList.add("Police,ASSIGNTASK,----RespondToAccident");
		//TasksToExecute.add("");
		Main.Message(this, true, ": have added tasks");
		
		//currentIteration++;
	}
	private void asyncInitSubscribe() {
		commsPool.execute( new Runnable(){
			@Override
			public void run() {
				initSubscribe();
			}
		});
	}

	private void initSubscribe() {
		Main.Message(this, true, ": about to GetMqttProvider");

		//SchedulingEvent evt = new SchedulingEvent(TaskIssuerName, SchedulingCommandType.INITMSG, "started");
	
		//publishSchedulingEvent(evt);
		//Main.Message(this, true, ": about to SubscribeForAgent");
		//mq.SubscribeForAgent(ambName);
		final TaskIssuer taskIssuer = this;
		commsPool.execute( new Runnable(){
			@Override
			public void run() {
				mqReceiver = MqttMessagingProvider.GetMqttProvider(TaskIssuerName + "Subscriber", ipAddress, port);
				mqReceiver.AddListener(taskIssuer);
				mqReceiver.SubscribeForAgent(TaskIssuerName);
				mqReceiver.SubscribeForAgent(polName);
				mqReceiver.SubscribeForAgent(ambName);
				mqReceiver.SubscribeForAgent(ambName2);
				mqReceiver.SubscribeForAgent(ambName3);
			}
		});
	
		//Main.Message(this, true, ": about to add tasks");
	}
	
	private void publishSchedulingEvent(SchedulingEvent evt) {
		//	UUID clientUID = UUID.randomUUID();
		//	mq = MqttMessagingProvider.GetMqttProvider(TaskIssuerName , ipAddress, port);
		//	Main.Message(this, true, ": about to publish");
		//	mq.PublishMessage(evt);
	
		final SchedulingEvent messageEvt = evt;
		//quick fix to implement asynchornous way of sending
		try {
			final MqttAsyncClient client = new MqttAsyncClient("tcp://" + ipAddress + ":" + port, MqttAsyncClient.generateClientId());
			client.connect( null, new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken asyncActionToken) {
					//while (true) {
					try{
						publishCounter++;
						String message = messageEvt.agentName + "," + messageEvt.commandType + "," + messageEvt.params.toString();
						client.publish(messageEvt.agentName, message.getBytes(), 1, false);
						System.out.println("publish #" + publishCounter);
					}
					catch (MqttException e) {
						e.printStackTrace();
					}
					//}
				}

				@Override
				public void onFailure(IMqttToken arg0, Throwable arg1) {
					// TODO Auto-generated method stub
				}
			});
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void publishMessage(String message) {
		publishMessage(message, 2);
	}
	private void publishMessage(String message, final int QOS) {
		//UUID clientUID = UUID.randomUUID();
		//	mq = MqttMessagingProvider.GetMqttProvider(TaskIssuerName , ipAddress, port);
		//	//Main.Message(this, true, ": about to publish");
		//	mq.PublishMessage(message);
		//	
		final String eventMessage = message;
		//quick fix to implement asynchronous way of sending
		try {
			final MqttAsyncClient client = new MqttAsyncClient("tcp://" + ipAddress + ":" + port, MqttAsyncClient.generateClientId());
			client.connect( null, new IMqttActionListener() {
				@Override
				public void onSuccess(IMqttToken asyncActionToken) {
					//	while (true) {
					try{
						publishCounter++;
						String agentName = eventMessage.substring(0, eventMessage.indexOf(","));
						client.publish(agentName, eventMessage.getBytes(), QOS, false);
						System.out.println("publish #" + publishCounter);
					}
					catch (MqttException e) {
						e.printStackTrace();
					}
					//	}
				}

				@Override
				public void onFailure(IMqttToken arg0, Throwable arg1) {
					// TODO Auto-generated method stub
				
				}
			});
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		//Issue dummy task completion message to mqtt to start new cycle of task executions
		//mq.PublishMessage(new SchedulingEvent(TaskIssuer.TaskIssuerName,SchedulingCommandType.TASKCOMPLETED,"----DUMMY"));
		while(active) {
			long startTime = LapsedTime.getStart();

			//Main.Message(this, true, "TaskIssuer.run()");
			Main.Message(this, true, "TaskIssuer.run() numberOfIteration = " + numberOfIteration + " currentIteration " + currentIteration);
			synchronized (pauseLock) {
				while (paused) {
					try {
						pauseLock.wait();
					} catch (InterruptedException e) {
						System.out.println("Thread " + Thread.currentThread().getName() + " interrupted");
					}
				}
			}
			/*
			try {
			// need to invoke this method once the previous scenario is finished - then no need to sleep for 5 sec
				Thread.sleep(10);
				RelaunchExecutionLoop();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			*/

			if ( currentIteration < numberOfIteration ) {
				Main.Message(this, true, "TaskIssuer.run() relaunching execution loop ");
				//long currentLapsedTime = Agent.getLapsedTime();
				RelaunchExecutionLoop();
				currentIteration++;
				onPause(); // pause the thread until all tasks by all agents are completed to repeat a scenario
			}
			else {
				Main.Message(this, true, "TaskIssuer.run() all " + numberOfIteration + " iterations completed, TaskIssuer shutting down");
				active = false;
				double lapsedTime = (double) LapsedTime.getLapsed(startTime) * .001;
				System.out.println("LapsedTime = " + lapsedTime + " seconds");
				
				String ambShutdown = "Ambulance,SHUTDOWN,----PickPatient";
				String polShutdown = "Police,SHUTDOWN,----PickPatient";
				String ambShutdown2 = "Ambulance2,SHUTDOWN,----PickPatient";
				String ambShutdown3 = "Ambulance3,SHUTDOWN,----PickPatient";
				publishMessage(ambShutdown, 2);
				publishMessage(polShutdown, 2);
				publishMessage(ambShutdown2, 2);
				publishMessage(ambShutdown3, 2);
				try {
					Thread.sleep(250);
					shutdown();
				}
				catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
			}
			
		}
	}

	public void onPause() {
		synchronized (pauseLock) {
			paused = true;
		}
	}
	
	public void onResume() {
		synchronized (pauseLock) {
			paused = false;
			pauseLock.notify();
		}
	}
	
	private void startIssuer() {
		Main.Message(this, true, "starting Issuer thread");
		issuerPool.execute( this );
	}
	
	private void shutdown() {
		//shutdownAndAwaitTermination(issuerPool);
		//shutdownAndAwaitTermination(commsPool);
		commsPool.shutdown();
		issuerPool.shutdown();
		System.exit(0);
	}
	void shutdownAndAwaitTermination(ExecutorService pool) {
		   pool.shutdown(); // Disable new tasks from being submitted
		   try {
		     // Wait a while for existing tasks to terminate
		     if (!pool.awaitTermination(60, TimeUnit.SECONDS)) {
		       pool.shutdownNow(); // Cancel currently executing tasks
		       // Wait a while for tasks to respond to being cancelled
		       if (!pool.awaitTermination(60, TimeUnit.SECONDS))
		           System.err.println("Pool did not terminate");
		     }
		   } catch (InterruptedException ie) {
		     // (Re-)Cancel if current thread also interrupted
		     pool.shutdownNow();
		     // Preserve interrupt status
		     Thread.currentThread().interrupt();
		   }
		 }
	private void AsyncRelaunchExecutionLoop()
	{
		commsPool.execute( new Runnable(){
			@Override
			public void run() {
				//TasksPendingCompletion.addAll(MasterTaskList);
				for(String taskMessage : MasterTaskList)
				{
					Main.Message(true, "TaskIssuer.RelaunchExecutionLoop: Issuing message " + taskMessage);
					//mq.PublishMessage(taskMessage);
					publishMessage(taskMessage, 2);
				}
			}
		});
	}
	
	private void RelaunchExecutionLoop()
	{
		//TasksPendingCompletion.addAll(MasterTaskList);
		TaskNamesPendingCompletion.addAll(MasterTaskNameList);
/*		String initMSG = "Ambulance,INITMSG,----PickPatient";
		publishMessage(initMSG, 2);
		String policeInitMSG = "Police,INITMSG,----Patrol";
		publishMessage(policeInitMSG, 2);
		*/
		for(String taskMessage : MasterTaskList)
		{
			Main.Message(true, "TaskIssuer.RelaunchExecutionLoop: Issuing message " + taskMessage);
			final String msg = taskMessage;
			publishMessage(msg, 2);
		}
	}
	
	@Override
	public SchedulingEvent ProcessSchedulingEvent(SchedulingEvent event) {
		if (event.commandType.equals(SchedulingCommandType.TASKCOMPLETED))
		{
			Main.Message(true, "TaskIssuer.ProcessSchedulingEvent: received  " + event.params.TaskName);
			
			if (TaskNamesPendingCompletion.contains(event.params.TaskName)) {
				TaskNamesPendingCompletion.remove(event.params.TaskName);
				System.out.println("[TaskIssuer 58] " + event.params.TaskName + " completed");
			}
		}
		if (TaskNamesPendingCompletion.isEmpty())
		{
			//RelaunchExecutionLoop();
			//WorldState.NamesCompletedMethods.clear();
			Main.Message(true, "TaskIssuer.ProcessSchedulingEvent: all scenario tasks completed");
			onResume(); // unpause the thread with TaskIssuer run method
		}
		return null;
	}
	
	@Override
	public String getName() {return TaskIssuerName;}
	
	@Override
	public boolean IsGlobalListener() {return true;}
	
	//This program is used to issue commands to the agents via mqtt. It can be read in a separate JVM, and thus
	//have its own main entry point.
	public static void main(String[] args) {

		if(args.length < 3) {
			System.out.println("Command line should contain: IPaddress, port, and number of iterations");

		}
		else {
			String ipAddress = args[0];
			String portS = args[1];
			numberOfIteration = Integer.parseInt( args[2] );
			int port = 1883;
			try {
				port = Integer.parseInt(portS);
			}
			catch(NumberFormatException e) {
				System.err.println(e.getMessage() + ", port " + portS + " cannot be parsed to integer");
				System.exit(0);
			}
			TaskIssuer cc = new TaskIssuer(ipAddress, port);
			cc.initSubscribe();
			//cc.run();
			cc.startIssuer();
		}
	}
	

}

