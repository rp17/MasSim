package Aspect;

import java.io.IOException;
import java.util.LinkedList;

import masSim.schedule.Scheduler;
import masSim.taems.Schedule;
import masSim.taems.Task;
import masSim.world.Agent;
import Brace.DigitialClock;
import Brace.EventAggregator;
import Brace.EventMonitorManager;
import Brace.KeyWordsStore;
import EventMonitor.EventClass;

//Per event has an aspect
//It is very possible to create association between aspect and custome annotation, which are ready avaialbe
//This stage, not in hurry to automate the process, the key is to test whether EventMonitor based on AspectJ
//and ECA monitor synthesized actually work, perform well, and scale
public aspect Spec1Aspect {
//
//	e1:chosen to reach a way-point
//	e2:containWaypoint With schedule And task
	
	//pointcut e1PC(SimWorld3 s): target(s) && call(List<IAgent> initAgents());
	pointcut e1PC(Agent a): this(a) 
	&& call(void StatementEvent.addTasks() );
	//pointcut e4PC(RavenUI r): target(r) && call(void mousePressed(MouseEvent));
	pointcut e2PC(Scheduler sh): this(sh) && call(void StatementEvent.executeScheduleContainsTask());
//	pointcut taskListPC(Scheduler s, List<Task> tasks): this(s) && args(tasks)
//	&& call(void PredicateParameterFilter.addTaskList(List<Task>) );
	pointcut schedulePC(Scheduler s, Schedule sh): this(s) && args(sh)
	&& call(void PredicateParameterFilter.addSchedule(Schedule) );
	pointcut taskPC(Scheduler s, Task task): this(s) && args(task)
	&& call(void PredicateParameterFilter.addTask(Task) );

	
	after(Scheduler s, Task task) : taskPC(s, task) && !within(Spec1Aspect) {
		try {
			
			Agent mainInstance = (Agent)EventClass.getComponentInstance("Agent");
			if ( mainInstance.currentSchedule.equals(s)) {
				System.out.println("Task");
				
				LinkedList<Object> uncastTasks = EventClass.getPredicateQueue("containWaypoint", "task");
//				LinkedList<Task> tasksToBeChecked = new LinkedList<Task>();
//				for ( Object uncastTask: uncastTasks ){
//					Task existingTasks = (Task)uncastTask;
//					tasksToBeChecked.add(existingTasks);
//				}
				
				uncastTasks.add(task);
				
			
				EventClass.storePredicateQueue("containWaypoint", "task", uncastTasks);
				
				//EventClass.storePredicateParameter("containWaypoint", "task", task);
			
//				for ( Object unTask: uncastTask ) {
//				    Task task = (Task)unTask;
//					if ( !s.containWaypoint(schedule, task) ) {
//						System.out.println("e3 evaluation false");
//						EventAggregator.interceptSignal(KeyWordsStore.getDefaultNotSign() + "e3", DigitialClock.returnTicks(System.currentTimeMillis()));
//					}
//				}
			//	EventAggregator.interceptSignal("e3", DigitialClock.returnTicks(System.currentTimeMillis()));
				
			
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	after(Scheduler s, Schedule sh) : schedulePC(s, sh) && !within(Spec1Aspect) {
		try {
			
			Agent mainInstance = (Agent)EventClass.getComponentInstance("Agent");
			if ( mainInstance.currentSchedule.equals(s)) {
				System.out.println("schedule added");
				//Clone the schedule
				Schedule newSchedule = new Schedule();
				newSchedule.Merge(sh);
				
				
				EventClass.storePredicateParameter("containWaypoint", "schedule", newSchedule);
			
//				for ( Object unTask: uncastTask ) {
//				    Task task = (Task)unTask;
//					if ( !s.containWaypoint(schedule, task) ) {
//						System.out.println("e3 evaluation false");
//						EventAggregator.interceptSignal(KeyWordsStore.getDefaultNotSign() + "e3", DigitialClock.returnTicks(System.currentTimeMillis()));
//					}
//				}
			//	EventAggregator.interceptSignal("e3", DigitialClock.returnTicks(System.currentTimeMillis()));
				
			
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
//	after(Scheduler s, List<Task> tasks) : taskListPC(s, tasks) && !within(Spec1Aspect) {
//		try {
//			
//			Agent mainInstance = (Agent)EventClass.getComponentInstance("Spec1");
//			if ( mainInstance.getScheduler().equals(s)) {
//				System.out.println("Task added");
//				EventClass.storePredicateParameter("containWaypoint", "task", tasks);
//			
////				for ( Object unTask: uncastTask ) {
////				    Task task = (Task)unTask;
////					if ( !s.containWaypoint(schedule, task) ) {
////						System.out.println("e3 evaluation false");
////						EventAggregator.interceptSignal(KeyWordsStore.getDefaultNotSign() + "e3", DigitialClock.returnTicks(System.currentTimeMillis()));
////					}
////				}
//			//	EventAggregator.interceptSignal("e3", DigitialClock.returnTicks(System.currentTimeMillis()));
//				
//			
//			}
//		
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
//	before(SimWorld3 s): e1PC(s) && !within(Spec1Aspect) {
//		System.out.println("e1");
//		try {
//			EventAggregator.interceptSignal("e1", DigitialClock.returnTicks(System.currentTimeMillis()));
//			EventMonitorManager.printoutAllEventMonitorState();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	after(Agent a): e1PC(a) && !within(Spec1Aspect) {
		try {
			System.out.println("e1");
			Agent mainInstance = (Agent)EventClass.getComponentInstance("Agent");
			if ( mainInstance.equals(a)) {
				try {
					EventAggregator.interceptSignal("e1", DigitialClock.returnTicks(System.currentTimeMillis()));
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				
//				try {
//					//here hijacking the event generation to produce event surges
//					int intEventCount = 1000;
//					for ( int intCounter=0; intCounter<intEventCount; intCounter++) {
//						EventAggregator.interceptSignal("e1", DigitialClock.returnTicks(System.currentTimeMillis()));
//					}
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				EventMonitorManager.printoutAllEventMonitorState();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	after(RavenUI r): e4PC(r) && !within(Spec1Aspect) {
//		try {
//			System.out.println("e4");
//			EventAggregator.interceptSignal("e4", DigitialClock.returnTicks(System.currentTimeMillis()));
//			EventMonitorManager.printoutAllEventMonitorState();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	after(Scheduler sh): e2PC(sh) && !within(Spec1Aspect) {
		try {
			System.out.println("e2 evaluation");
			Agent mainInstance = (Agent)EventClass.getComponentInstance("Agent");
			//if ( mainInstance.getScheduler().equals(sh)) {
				
				//List<Task> tasks = (List<Task>) EventClass.getPredicateParameter("containWaypoint","task");
				LinkedList<Object> uncastTasks = EventClass.getPredicateQueue("containWaypoint", "task");
				
				
//				LinkedList<Task> tasksToBeChecked = new LinkedList<Task>();
				
//				for ( Object uncastTask: uncastTasks ){
//					Task existingTasks = (Task)uncastTask;
//					tasksToBeChecked.add(existingTasks);
//				}
				//EventClass.storePredicateQueue("containWaypoint", "task", uncastTasks);
			//	Task task = (Task)EventClass.getPredicateParameter("containWaypoint","task");
				Schedule schedule = (Schedule) EventClass.getPredicateParameter("containWaypoint", "schedule");
				
				if ( uncastTasks!=null && uncastTasks.size()>0 && schedule != null ) {
					while ( uncastTasks.peek() != null) {
						Task taskToBeChecked = (Task) uncastTasks.remove(); 
					
						if ( PredicateFunction.containWaypointSingle(schedule, taskToBeChecked)) {
							try {
								EventAggregator.interceptSignal("e2", DigitialClock.returnTicks(System.currentTimeMillis()));
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						} else {
							try {
								EventAggregator.interceptSignal(KeyWordsStore.getDefaultNotSign() + "e2", DigitialClock.returnTicks(System.currentTimeMillis()));
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
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
				
				
		//	}
		
		} catch (IOException e) {
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
