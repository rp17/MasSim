package masSim.schedule;

public class TaskScheduleQualities {
	public int TaskId;
	public int base; 
	public int incremental; 
	public TaskScheduleQualities(int taskId, int base, int incremental)
	{
		this.TaskId = taskId;
		this.base = base;
		this.incremental = incremental;
	}
	
	public int diff()
	{
		return incremental-base;
	}
}
