package masSim.schedule;

public class SchedulingEventParams {
	public String paramsRaw;
	public String AgentId;
	public String MethodId;
	public String XCoordinate;
	public String YCoordinate;
	public String TaskName;
	
	public SchedulingEventParams()
	{}
	
	public SchedulingEventParams AddAgentId(String agentId)
	{
		this.AgentId = agentId;
		return this;
	}
	
	public SchedulingEventParams AddTaskName(String taskName)
	{
		this.TaskName = taskName;
		return this;
	}
	
	public SchedulingEventParams AddMethodId(String methodId)
	{
		this.MethodId = methodId;
		return this;
	}
	
	public SchedulingEventParams AddXCoord(String xCoord)
	{
		this.XCoordinate = xCoord;
		return this;
	}
	
	public SchedulingEventParams AddYCoord(String yCoord)
	{
		this.YCoordinate = yCoord;
		return this;
	}
	
	public SchedulingEventParams AddXCoord(double xCoord)
	{
		this.XCoordinate = xCoord + "";
		return this;
	}
	
	public SchedulingEventParams AddYCoord(double yCoord)
	{
		this.YCoordinate = yCoord + "";
		return this;
	}
	
	public SchedulingEventParams(String subjectAgentId, String methodId, String xCoord, String yCoord, String taskName)
	{
		this.AgentId = subjectAgentId;
		this.MethodId = methodId;
		this.XCoordinate = xCoord;
		this.YCoordinate = yCoord;
		this.TaskName = taskName;
	}
	
	public static SchedulingEventParams Parse(String paramsRaw)
	{
		SchedulingEventParams params = new SchedulingEventParams();
		String[] dataItems = paramsRaw.split("-");
		if (dataItems.length>=2)
		{
			params.XCoordinate = dataItems[0];
			params.YCoordinate = dataItems[1];
		}
		if (dataItems.length>=3)
		{
			params.AgentId = dataItems[2];
		}
		if (dataItems.length>=4)
		{
			params.MethodId = dataItems[3];
		}
		if (dataItems.length>=5)
		{
			params.TaskName = dataItems[4];
		}
		return params;
	}
	
	@Override
	public String toString()
	{
		return this.XCoordinate + "-" + this.YCoordinate + "-" +
				this.AgentId + "-" + this.MethodId + "-" + this.TaskName;
	}
}
