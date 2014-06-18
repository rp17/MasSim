package masSim.world;

import java.util.EventObject;

import masSim.taems.IAgent;

public class WorldEvent extends EventObject {

	public double xCoordinate;
	public double yCoordinate;
	public TaskType taskType; 
	public enum TaskType {METHODCREATED, AGENTCREATED, EXECUTEMETHOD}
	public String agentId;
	public String methodId;
	public IAgent agent;
	
	public WorldEvent(Object source, TaskType type, String agentId, String methodId, double x2, double y2, IAgent agent) {
		super(source);
		xCoordinate = x2;
		yCoordinate = y2;
		taskType = type;
		this.agentId = agentId;
		this.methodId = methodId;
		this.agent = agent;
	}

}
