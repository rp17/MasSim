package masSim.schedule;

import java.util.EventObject;

public class SchedulingEvent {

	public String agentName;
	public SchedulingCommandType commandType;
	public SchedulingEventParams params;
	
	public SchedulingEvent(String agentName, String commandType, String paramsRaw) {
		this(agentName,SchedulingCommandType.valueOf(commandType),paramsRaw);
	}
	
	public SchedulingEvent(String agentName, SchedulingCommandType commandType, String paramsRaw) {
		this(agentName, commandType, SchedulingEventParams.Parse(paramsRaw));
	}
	
	public SchedulingEvent(String agentName, SchedulingCommandType commandType, SchedulingEventParams params) {
		this.agentName = agentName;
		this.commandType = commandType;
		this.params = params;
	}
}
