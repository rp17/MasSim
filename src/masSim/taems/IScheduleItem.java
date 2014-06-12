package masSim.taems;

import masSim.taems.ScheduleItem.Status;

public interface IScheduleItem {
	  public String getName();
	  public Status update(int dt);
	  public Status getStatus();
}
