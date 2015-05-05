package masSim.schedule;

import masSim.taems.*;

public interface IScheduleUpdateEventListener {
	void HandleScheduleEvent(ScheduleUpdateEvent scheduleUpdateEvent);
}
