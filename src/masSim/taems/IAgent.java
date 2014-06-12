package masSim.taems;

import java.util.ArrayList;

public interface IAgent {
	public int getCode();
	public void assignTasks(ArrayList<Task> tasks);
	public void addTaskAbility(Task task);
	public void update(int tick);
}
