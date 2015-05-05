package Aspect;

import java.util.List;

import masSim.taems.Method;
import masSim.taems.Schedule;
import masSim.taems.Task;


public class PredicateParameterFilter {
		
	public static void addTaskList(List<Task> taskList){
		int a = 1;
	};
	public static void addSchedule(Schedule sh){
		int a = 1;
	};
	
	public static void addTask(Task task) {
		System.out.println("Task added in filter:" + task.getLabel());
	}
	
	public static void addMethod(Method m) {
		System.out.println("Method added in filter:" + m.getLabel() );
	}
	
	public static void addChoiceTask(Task task) {
		int a= 1;
	}
	

}
