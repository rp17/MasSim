package masSim.schedule;

public class TaskScheduleQualities {
	public int TaskId;
	public int base; 
	public int incremental; 
	public int diff()
	{
		return incremental-base;
	}
}
