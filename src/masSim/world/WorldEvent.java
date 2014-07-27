package masSim.world;

import java.util.EventObject;

import masSim.taems.IAgent;
import masSim.taems.Method;

public class WorldEvent extends EventObject {

	public double xCoordinate;
	public double yCoordinate;
	public TaskType taskType; 
	public enum TaskType {METHODCREATED, AGENTCREATED, EXECUTEMETHOD, METHODCOMPLETED}
	public String agentId;
	public String methodId;
	public IAgent agent;
	public Method method;
	
	public WorldEvent(Object source, TaskType type, String agentId, String methodId, double x2, double y2, IAgent agent,
			Method method) {
		super(source);
		xCoordinate = x2;
		yCoordinate = y2;
		taskType = type;
		this.agentId = agentId;
		this.methodId = methodId;
		this.agent = agent;
		this.method = method;
	}

}
