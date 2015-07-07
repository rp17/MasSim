package masSim.schedule;

public class SchedulingEventParams {
	public String paramsRaw;
	public String AgentId;
	public String MethodId;
	public String XCoordinate;
	public String YCoordinate;
	public String Data;
	public String TaskName;
	private String AddOriginatingAgent;
	public static String DataItemSeparator = "w";
	
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

	public SchedulingEventParams AddOriginatingAgent(String originatingAgent) {
		this.AddOriginatingAgent = originatingAgent;
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
	
	public SchedulingEventParams AddData(String data)
	{
		this.Data = data + "";
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
		if (dataItems.length>=6)
		{
			params.Data = dataItems[5];
		}
		return params;
	}
	
	@Override
	public String toString()
	{
		String result = "";
		if (this.XCoordinate!=null) result += this.XCoordinate;
		result += "-";
		if (this.YCoordinate!=null) result += this.YCoordinate;
		result += "-";
		if (this.AgentId!=null) result += this.AgentId;
		result += "-";
		if (this.MethodId!=null) result += this.MethodId;
		result += "-";
		if (this.TaskName!=null) result += this.TaskName;
		result += "-";
		if (this.Data!=null) result += this.Data;
		return result;
	}

}
