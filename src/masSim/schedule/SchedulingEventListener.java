package masSim.schedule;


public interface SchedulingEventListener {
	
	public SchedulingEvent ProcessSchedulingEvent(SchedulingEvent event);
	public String getName();
	public boolean IsGlobalListener();
}
