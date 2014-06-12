package masSim.world;

import java.util.EventObject;

public class WorldEvent extends EventObject {

	public int xCoordinate;
	public int yCoordinate;
	public TaskType taskType; 
	public enum TaskType {METHODCREATED, AGENTCREATED, EXECUTEMETHOD}
	public String agentId;
	public String methodId;
	
	public WorldEvent(Object source, TaskType type, String agentId, String methodId, int x2, int y2) {
		super(source);
		xCoordinate = x2;
		yCoordinate = y2;
		taskType = type;
		this.agentId = agentId;
		this.methodId = methodId;
	}

}
