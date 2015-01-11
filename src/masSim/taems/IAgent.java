package masSim.taems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import raven.math.Vector2D;
import masSim.schedule.Scheduler;
import masSim.world.AgentMode;

public interface IAgent {
	List<Task> getPendingTasks();
	public int getCode();
	public void AddChildAgent(IAgent agent);
	//public void assignTask(Task task);
	public void update(int tick);
	//public int getExpectedScheduleQuality(Task task, IAgent agent);
	public void setPosition(Vector2D pos);
	public Vector2D getPosition();
	public String getName();
	public void MarkMethodCompleted(Method method);
	public AgentMode getMode();
	public void setMode(AgentMode mode);
	public void UpdateSchedule(Schedule schedule);
	public Task GetCurrentTasks();
	public void RegisterChildrenWithUI(Node node);
	public ArrayList<IAgent> getAgentsUnderManagement();
	public void AddPendingTask(Task task);
}