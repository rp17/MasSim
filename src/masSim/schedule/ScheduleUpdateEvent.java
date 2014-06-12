
package masSim.schedule;
import masSim.taems.*;

public class ScheduleUpdateEvent {
	public Schedule Schedule;
	
	public ScheduleUpdateEvent(Schedule schedule)
	{
		this.Schedule = schedule;
	}
}
