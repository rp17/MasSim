package masSim.taems;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import raven.math.Vector2D;
import masSim.schedule.Scheduler;
import masSim.world.AgentMode;
import masSim.world.SimBot;

public interface IAgent extends Runnable {
	List<Task> getPendingTasks();
	public int getCode();
	public void AddChildAgent(String agentName);
	//public void assignTask(Task task);
	public void update(int tick);
	//public int getExpectedScheduleQuality(Task task, IAgent agent);
	public void setPosition(Vector2D pos);
	public Vector2D getPosition();
	public String getName();
	public void MarkMethodCompleted(String methodName);
	public AgentMode getMode();
	public void setMode(AgentMode mode);
	public void UpdateSchedule(Schedule schedule);
	public Task GetCurrentTasks();
	public void RegisterChildrenWithUI(Node node, List<String> methodNames);
	public ArrayList<String> getAgentsUnderManagement();
	void setBot(SimBot bot);
	public void startEventProcessing();
}