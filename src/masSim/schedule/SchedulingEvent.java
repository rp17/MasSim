package masSim.schedule;

import java.util.EventObject;

public class SchedulingEvent {

	public String agentName;
	public String commandType;
	public String commandText;
	
	public SchedulingEvent(String agentName, String commandType, String commandText) {
		this.agentName = agentName;
		this.commandType = commandType;
		this.commandText = commandText;
	}

}
