package masSim.schedule;

public class TaskScheduleQualities {
	public int VariableId;
	public int base; 
	public int incremental;
	public int [] Tasks;
	public TaskScheduleQualities(int VariableId, int base, int incremental, int[] tasks)
	{
		this.VariableId = VariableId;
		this.base = base;
		this.incremental = incremental;
		this.Tasks = tasks;
	}
	
	public int diff()
	{
		return incremental-base;
	}
}
