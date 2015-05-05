package masSim.schedule;

public class SchedulingEventParams {
	public String paramsRaw;
	public String AgentId;
	public String MethodId;
	public String XCoordinate;
	public String YCoordinate;
	public String BaseCost;
	public String IncrementalCost;
	public String OriginatingAgent;
	public String TaskName;
	public static String SEPARATOR = ":";
	
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
	
	public SchedulingEventParams AddBaseCost(int baseCost)
	{
		this.BaseCost = baseCost + "";
		return this;
	}
	
	public SchedulingEventParams AddIncrementalCost(int incrementalCost)
	{
		this.IncrementalCost = incrementalCost + "";
		return this;
	}
	
	public SchedulingEventParams AddOriginatingAgent(String originatingAgent)
	{
		this.OriginatingAgent = originatingAgent + "";
		return this;
	}
	
	public SchedulingEventParams AddYCoord(double yCoord)
	{
		this.YCoordinate = yCoord + "";
		return this;
	}
	
	public static SchedulingEventParams Parse(String paramsRaw)
	{
		SchedulingEventParams params = new SchedulingEventParams();
		String[] dataItems = paramsRaw.split(SEPARATOR);
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
			params.BaseCost = dataItems[5];
		}
		if (dataItems.length>=7)
		{
			params.IncrementalCost = dataItems[6];
		}
		if (dataItems.length>=8)
		{
			params.OriginatingAgent = dataItems[7];
		}
		return params;
	}
	
	@Override
	public String toString()
	{
		String result = "";
		if (this.XCoordinate!=null) result += this.XCoordinate;
		result += SEPARATOR;
		if (this.YCoordinate!=null) result += this.YCoordinate;
		result += SEPARATOR;
		if (this.AgentId!=null) result += this.AgentId;
		result += SEPARATOR;
		if (this.MethodId!=null) result += this.MethodId;
		result += SEPARATOR;
		if (this.TaskName!=null) result += this.TaskName;
		result += SEPARATOR;
		if (this.BaseCost!=null) result += this.BaseCost;
		result += SEPARATOR;
		if (this.IncrementalCost!=null) result += this.IncrementalCost;
		result += SEPARATOR;
		if (this.OriginatingAgent!=null) result += this.OriginatingAgent;
		return result;
	}
}
