package masSim.schedule;
import java.util.ArrayList;
import java.util.List;

public class AgentScheduleQualities {
	
	public int AgentVariableId; 
	public List<MultipleTaskScheduleQualities> TaskQualities = new ArrayList<MultipleTaskScheduleQualities>();
	public List<Integer> TaskIds;
	public AgentScheduleQualities(int agentVariableId)
	{
		this.AgentVariableId = agentVariableId;
	}
}
