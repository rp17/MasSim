package masSim.taems;

import java.util.ArrayList;

import raven.math.Vector2D;
import masSim.schedule.Scheduler;

public interface IAgent {
	public int getCode();
	public void AddChildAgent(IAgent agent);
	public void assignTask(Task task);
	public void update(int tick);
	public int getExpectedScheduleQuality(Task task, IAgent agent);
	public void setPosition(Vector2D pos);
	public Vector2D getPosition();
}
