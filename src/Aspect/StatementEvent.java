package Aspect;

import masSim.taems.IAgent;
import masSim.taems.Schedule;
import masSim.taems.Task;
import BAEventMonitor.AttributeType;
import BAEventMonitor.DistributedAttribute;
import BAEventMonitor.DistributedEvent;
import BAEventMonitor.Event;

public class StatementEvent {
	
	public  static volatile  int injectedSpec2Error = 0;
	
	@DistributedAttribute(name="theconsensus", type=AttributeType.String, variableName="consensusMessage")
	public static void getConsensus(String consensusMessage) {
		int a =1;
		System.out.println("the consensus instrumented method is called");
	}
	
	@DistributedAttribute(name="ExecutionPlan", type=AttributeType.Object, variableName="sh")
	public static void getExecutionPlan(Schedule sh) {
		int a =1;
		System.out.println("ExecutionPlan instrumented method is called");
	}
	
	@DistributedAttribute(name="thecompletedtask", type=AttributeType.String, variableName="completedMethodName")
	public static void getCompletedTasks(String completedMethodName) {
		int a =1;
		System.out.println("the CompletedTask instrumented method is called");
	}
	
	@DistributedEvent(name="assigned to reach a way-point", eventObjects = { @DistributedAttribute(type=AttributeType.Object, name="ChosenPoint", variableName="task") })
	public static void addTasks(Task task){
		int a =1;
		System.out.println("assigned to reach a way-point instrumented event and method is called");
	}
	
	
	@Event(name="chosen to reach a way-point")
	public static void addTasks(){
		int a=1;
	}
	
	
	@Event(name="assigned a choice")
	public static void assignchoice(){
		int a=1;
	}
	
	
	public static void executeScheduleContainsTask(){
		System.out.println("Evaluate scheduler contains the task");
		int a=1;
	}
	
	public static void evaluateReachAWayPoint() {
		System.out.println("Evaluate reachWayPoint");
	}
	
	public static void evaluateScheduleOptimal(Schedule schedule) {
		System.out.println("Evaluate scheduleIsOptimal");
	}
	
}
