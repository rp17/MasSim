package raven;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import masSim.world.MqttMessagingProvider;

public class MqttConsole implements Runnable {

	//This program is used to issue commands to the agents via mqtt. It can be read in a separate JVM, and thus
	//has its own main entry point.
	public static void main(String[] args) {
		MqttConsole cc = new MqttConsole();
		cc.Execute();
	}
	
	@Override
	public void run() {
		Execute();
	}
	
	private void Execute()
	{
		System.out.println("Enter the agent name and the task which you need the agent to execute ");
		System.out.println("in the format: AgentName>TaskName ");
		System.out.println("Type \"exit\" to quit");
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String commandText = null;
		MqttMessagingProvider mqtt = MqttMessagingProvider.GetMqttProvider();
		while(true)
		{
			try {
				commandText = br.readLine();
				if (commandText.equalsIgnoreCase("exit")) break;
				else if (commandText.equalsIgnoreCase("pickpatient"))
				{
					commandText = "Ambulance,ASSIGNTASK,----PickPatient";
				}
				else if (commandText.equalsIgnoreCase("droppatient"))
				{
					commandText = "Ambulance,ASSIGNTASK,----DropPatient";
				}
				else if (commandText.equalsIgnoreCase("patrola"))
				{
					commandText = "Police,ASSIGNTASK,----PatrolA";
				}
				else if (commandText.equalsIgnoreCase("patrolb"))
				{
					commandText = "Police,ASSIGNTASK,----PatrolB";
				}
				else if (commandText.equalsIgnoreCase("accident"))
				{
					commandText = "Police,NEGOTIATE,----RespondToAccident";
				}
				mqtt.PublishMessage(commandText);
			} 
			catch (IOException ioe) {
				System.out.println("IO error trying to read the command!");
		        System.exit(1);
		    }
		}
		System.exit(0);
	}
}
