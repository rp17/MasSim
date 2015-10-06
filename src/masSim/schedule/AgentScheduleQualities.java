package masSim.schedule;
import java.util.ArrayList;

public class AgentScheduleQualities {
	
	public int AgentVariableId; 
	public ArrayList<TaskScheduleQualities> TaskQualities = new ArrayList<TaskScheduleQualities>();
	
	public AgentScheduleQualities(int agentVariableId)
	{
		this.AgentVariableId = agentVariableId;
	}
}
