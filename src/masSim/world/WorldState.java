package masSim.world;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import masSim.taems.*;

public class WorldState {
	public static List<Method> CompletedMethods = new CopyOnWriteArrayList<Method>();
	public static List<Task> CompletedTasks = new CopyOnWriteArrayList<Task>();
}
