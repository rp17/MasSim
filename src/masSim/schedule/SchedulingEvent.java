package masSim.schedule;

import java.util.EventObject;
import java.util.List;

import masSim.taems.Task;

public class SchedulingEvent {

	public String agentName;
	public SchedulingCommandType commandType;
	public SchedulingEventParams params;
	public String rawMessage;
	public List<MultipleTaskScheduleQualities> taskQualities;//Not serialized yet
	public List<Task> tasks;//Not yet serialized
	public List<Integer> assignedTasks;//Not yet serialized
	
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
		this.rawMessage = agentName + "," + commandType + "," + params;
	}
	
	public static SchedulingEvent Parse(String message)
	{
		String[] messageParts = message.split(",");
		SchedulingEvent event = new SchedulingEvent(messageParts[0],messageParts[1],messageParts[2]);
		event.rawMessage = message;
		return event;
	}
	
	@Override
	public String toString()
	{
		return rawMessage;
	}
}
