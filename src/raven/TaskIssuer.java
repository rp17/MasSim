package raven;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import masSim.schedule.SchedulingCommandType;
import masSim.schedule.SchedulingEvent;
import masSim.schedule.SchedulingEventListener;
import masSim.world.MqttMessagingProvider;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskIssuer implements Runnable, SchedulingEventListener {

	private List<String> MasterTaskList = new ArrayList<String>();
	private List<String> TasksPendingCompletion = new ArrayList<String>();
	private volatile MqttMessagingProvider mq;
	//private String clientName = "taskIssuer";
	private String ambName = "Ambulance";
	private String polName = "Police";
	public static String TaskIssuerName = "TaskIssuer";
	private final ExecutorService commsPool = Executors.newSingleThreadExecutor();
	
	public TaskIssuer()
	{
		
		//Create list of tasks to be executed in a loop
		MasterTaskList.add("Ambulance,ASSIGNTASK,----PickPatient");
		MasterTaskList.add("Ambulance,ASSIGNTASK,----DropPatient");
		MasterTaskList.add("Police,ASSIGNTASK,----Patrol");
		//MasterTaskList.add("Police,NEGOTIATE,----RespondToAccident");
		//TasksToExecute.add("");
		Main.Message(this, true, ": have added tasks");
	}
	
	private void initSubscribe() {
		commsPool.execute( new Runnable(){
	 		@Override
	 		public void run() {
	 			Main.Message(this, true, ": about to GetMqttProvider");
	 			mq = MqttMessagingProvider.GetMqttProvider(TaskIssuerName);
	 			Main.Message(this, true, ": about to publish");
	 			SchedulingEvent evt = new SchedulingEvent(TaskIssuerName, SchedulingCommandType.INITMSG, "started");
	 			mq.PublishMessage(evt);
	 			Main.Message(this, true, ": about to SubscribeForAgent");
	 			mq.SubscribeForAgent(ambName);
	 			Main.Message(this, true, ": about to add tasks");
	 		}
		});
	}

	
	@Override
	public void run() {

		//Issue dummy task completion message to mqtt to start new cycle of task executions
		//mq.PublishMessage(new SchedulingEvent(TaskIssuer.TaskIssuerName,SchedulingCommandType.TASKCOMPLETED,"----DUMMY"));
		try {
			// need to invoke this method once the previous scenario is finished - then no need to sleep for 5 sec
			Thread.sleep(5000);
			//RelaunchExecutionLoop();
			AsyncRelaunchExecutionLoop();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("TaskIssuer started. Hit enter to exit.");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		//String commandText = null;	
		try {
			br.readLine();
		} 
		catch (IOException ioe) {}
		System.exit(0);
	}

	private void AsyncRelaunchExecutionLoop()
	{
		commsPool.execute( new Runnable(){
	 		@Override
	 		public void run() {
	 			TasksPendingCompletion.addAll(MasterTaskList);
	 			for(String taskMessage : TasksPendingCompletion)
	 			{
	 				Main.Message(true, "TaskIssuer.RelaunchExecutionLoop: Issuing message " + taskMessage);
	 				mq.PublishMessage(taskMessage);
	 			}
	 		}
		});
	}
	private void RelaunchExecutionLoop()
	{
		
	 			TasksPendingCompletion.addAll(MasterTaskList);
	 			//SchedulingEvent evt = new SchedulingEvent(TaskIssuerName, SchedulingCommandType.INITMSG, "test");
	 			//mq.PublishMessage(evt);
	 			for(String taskMessage : TasksPendingCompletion)
	 			{
	 				Main.Message(true, "TaskIssuer.RelaunchExecutionLoop: Issuing message " + taskMessage);
	 				final String msg = taskMessage;
	 				commsPool.execute( new Runnable(){
	 			 		@Override
	 			 		public void run() {
	 			 			mq.PublishMessage(msg);
	 			 		}});
	 			}
	 		
	}
	@Override
	public SchedulingEvent ProcessSchedulingEvent(SchedulingEvent event) {
		if (event.commandType.equals(SchedulingCommandType.TASKCOMPLETED))
		{
			if (TasksPendingCompletion.contains("event.params.TaskName"))
			TasksPendingCompletion.remove(event.params.TaskName);
			System.out.println("[TaskIssuer 58] " + event.params.TaskName + " completed");
		}
		if (TasksPendingCompletion.isEmpty())
		{
			RelaunchExecutionLoop();
		}
		return null;
	}

	@Override
	public String getName() {
		return TaskIssuerName;
	}

	@Override
	public boolean IsGlobalListener() {
		return true;
	}
	//This program is used to issue commands to the agents via mqtt. It can be read in a separate JVM, and thus
	//have its own main entry point.
	public static void main(String[] args) {
		TaskIssuer cc = new TaskIssuer();
		cc.initSubscribe();
		cc.run();
	}
}
