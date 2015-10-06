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

public class TaskIssuer implements Runnable, SchedulingEventListener {

	private List<String> MasterTaskList = new ArrayList<String>();
	private List<String> TasksPendingCompletion = new ArrayList<String>();
	private MqttMessagingProvider mq;
	public static String TaskIssuerName = "TaskIssuer";
	
	public TaskIssuer()
	{
		mq = MqttMessagingProvider.GetMqttProvider();
		mq.SubscribeForAgent(getName());
		//Create list of tasks to be executed in a loop
		MasterTaskList.add("Police,NEGOTIATE,::::PickAndDrop");
		MasterTaskList.add("Police,NEGOTIATE,::::Visit1");
		//MasterTaskList.add("Police,NEGOTIATE,::::Patrol");
		//MasterTaskList.add("Police,NEGOTIATE,::::RespondToAccident");
	}
	
	//This program is used to issue commands to the agents via mqtt. It can be read in a separate JVM, and thus
	//have its own main entry point.
	public static void main(String[] args) {
		TaskIssuer cc = new TaskIssuer();
		cc.run();
	}
	
	@Override
	public void run() {

		//Issue dummy task completion message to mqtt to start new cycle of task executions
		//mq.PublishMessage(new SchedulingEvent(TaskIssuer.TaskIssuerName,SchedulingCommandType.TASKCOMPLETED,"----DUMMY"));
		try {
			Thread.sleep(5000);
			RelaunchExecutionLoop();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("TaskIssuer started. Hit enter to exit.");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String commandText = null;	
		try {
			br.readLine();
		} 
		catch (IOException ioe) {}
		System.exit(0);
	}

	private void RelaunchExecutionLoop()
	{
		TasksPendingCompletion.addAll(MasterTaskList);
		for(String taskMessage : TasksPendingCompletion)
		{
			Main.Message(true, "[TaskIssuer 69] Issuing message " + taskMessage);
			mq.PublishMessage(taskMessage);
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
}
