package Aspect;

import java.util.ArrayList;

import masSim.taems.Schedule;
import masSim.taems.Task;
import masSim.world.Agent;
import BAEventMonitor.AttributeType;
import Brace.DigitialClock;
import Brace.DistributedAttribute;
import Brace.DistributedEventWriter;
import EventMonitor.EventClass;

public aspect Global1Aspect {

	//e2:assigned to reach a way-point
	
	//pointcut e1PC(SimWorld3 s): target(s) && call(List<IAgent>getConsensus initAgents());
	
	pointcut globale2PC(Agent a, Task task): this(a) && args(task)
	&& call(void StatementEvent.addTasks(Task) );

	pointcut globale3PC(Agent a, Schedule sh): this(a) && args(sh)
	&& call(void StatementEvent.getConsensus(Schedule) );
	
	after(Agent a, Schedule sh): globale3PC(a, sh) && !within(Global1Aspect) {
		try {
			System.out.println("global property sh evaluation");
			Agent mainInstance = (Agent)EventClass.getComponentInstance("Agent");
			if ( mainInstance.equals(a)) {
				//assigned to reach a way-point (ChosenPoint)
				//generate distributed event and object
				double time = DigitialClock.returnTicks(System.currentTimeMillis());
				DistributedAttribute da = new DistributedAttribute();
				da.setName("theconsensus");
				da.setTime(time);
				da.setType(AttributeType.Object);
				da.setValue(sh);
			    DistributedEventWriter.writeAttribute("theconsensus", AttributeType.Object, da, time);
				
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//EventAggregator.interceptSignal(KeyWordsStore.getDefaultNotSign() + "e2", DigitialClock.returnTicks(System.currentTimeMillis()));
			e.printStackTrace();
		}
	}
	
	
	after(Agent a, Task task): globale2PC(a, task) && !within(Global1Aspect) {
		try {
			System.out.println("global event e1 evaluation");
			Agent mainInstance = (Agent)EventClass.getComponentInstance("Agent");
			if ( mainInstance.equals(a)) {
				//assigned to reach a way-point (ChosenPoint)
				//generate distributed event and object
				double time = DigitialClock.returnTicks(System.currentTimeMillis());
				DistributedAttribute da = new DistributedAttribute();
				da.setName("ChosenPoint");
				da.setTime(time);
				da.setType(AttributeType.Object);
				da.setValue(task);
			    ArrayList<DistributedAttribute> das = new ArrayList<DistributedAttribute>();
			    das.add(da);
			    DistributedEventWriter.writeEvent(das, "e1", time);
//				DistributedEventWriter.writeEvent(eventObjectList, eventID, time)
				
//				//List<Task> tasks = (List<Task>) EventClass.getPredicateParameter("containWaypoint","task");
//				LinkedList<Object> uncastTasks = EventClass.getPredicateQueue("containWaypoint", "task");
//				
//				
////				LinkedList<Task> tasksToBeChecked = new LinkedList<Task>();
//				
////				for ( Object uncastTask: uncastTasks ){
////					Task existingTasks = (Task)uncastTask;
////					tasksToBeChecked.add(existingTasks);
////				}
//				//EventClass.storePredicateQueue("containWaypoint", "task", uncastTasks);
//			//	Task task = (Task)EventClass.getPredicateParameter("containWaypoint","task");
//				Schedule schedule = (Schedule) EventClass.getPredicateParameter("containWaypoint", "schedule");
//				
//				if ( uncastTasks!=null && uncastTasks.size()>0 && schedule != null ) {
//					while ( uncastTasks.peek() != null) {
//						Task taskToBeChecked = (Task) uncastTasks.remove(); 
//					
//						if ( sh.containWaypointSingle(schedule, taskToBeChecked)) {
//							try {
//								EventAggregator.interceptSignal("e2", DigitialClock.returnTicks(System.currentTimeMillis()));
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						} else {
//							try {
//								EventAggregator.interceptSignal(KeyWordsStore.getDefaultNotSign() + "e2", DigitialClock.returnTicks(System.currentTimeMillis()));
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						}
//					}
//				}
//				for ( Object unTask: uncastTask ) {
//				    Task task = (Task)unTask;
//					if ( !s.containWaypoint(schedule, task) ) {
//						System.out.println("e3 evaluation false");
//						EventAggregator.interceptSignal(KeyWordsStore.getDefaultNotSign() + "e3", DigitialClock.returnTicks(System.currentTimeMillis()));
//					}
//				}
			//	EventAggregator.interceptSignal("e3", DigitialClock.returnTicks(System.currentTimeMillis()));
				
//				if ( sh.containWaypoint(schedule, tasks)) {
//					EventAggregator.interceptSignal("e2", DigitialClock.returnTicks(System.currentTimeMillis()));
//				} else {
//					EventAggregator.interceptSignal(KeyWordsStore.getDefaultNotSign() + "e2", DigitialClock.returnTicks(System.currentTimeMillis()));
//				}
				
				
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//EventAggregator.interceptSignal(KeyWordsStore.getDefaultNotSign() + "e2", DigitialClock.returnTicks(System.currentTimeMillis()));
			e.printStackTrace();
		}
	}
	
//	
//	e3:scheduleOptimal With task
//	e1:assigned a task
//	e2:new task is added
//	pointcut e1PC(): target(Scheduler) && execution(void AddTask(Task));
//	pointcut e2PC(): target(Scheduler) && call(int GetScheduleCostSync(Task, IAgent));
//	pointcut e3PC(Scheduler s): target(s) && call(Schedule CalculateScheduleFromTaems(Task));
//	
//	after(): e1PC() && !within(EventMonitorAspect) {
//		System.out.println("e1");
//		try {
//			EventAggregator.interceptSignal("e1", DigitialClock.returnTicks(System.currentTimeMillis()));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	after(): e2PC() && !within(EventMonitorAspect) {
//		try {
//			System.out.println("e2");
//			EventAggregator.interceptSignal("e2", DigitialClock.returnTicks(System.currentTimeMillis()));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	after(Scheduler s): e3PC(s) && !within(EventMonitorAspect) {
//		try {
//			System.out.println("e3 evaluation");
//			Task topLevelTask = (Task) EventClass.getPredicateParameter("checkOptimal", "tasks");
//			if ( s.checkSchedulerOptimized(s, topLevelTask ) ) {
//				EventAggregator.interceptSignal("e3", DigitialClock.returnTicks(System.currentTimeMillis()));
//			}
//			else {
//				EventAggregator.interceptSignal(KeyWordsStore.getDefaultNotSign() + "e3", DigitialClock.returnTicks(System.currentTimeMillis()));
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
}
