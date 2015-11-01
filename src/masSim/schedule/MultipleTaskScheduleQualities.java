package masSim.schedule;

import java.util.List;

public class MultipleTaskScheduleQualities {
	public List<Integer> TaskIds;
	public int base; 
	public int incremental; 
	public MultipleTaskScheduleQualities(List<Integer> taskIds, int base, int incremental)
	{
		this.TaskIds = taskIds;
		this.base = base;
		this.incremental = incremental;
	}
	
	public int diff()
	{
		return incremental-base;
	}
	
	public boolean IsMatch(List<Integer> tasks)
	{
		if (tasks.size()!=TaskIds.size()) return false;
		boolean matches = true;
		for(Integer i : tasks)
		{
			if (!TaskIds.contains(i)) 
				matches = false;
		}
		return matches;
	}
}
