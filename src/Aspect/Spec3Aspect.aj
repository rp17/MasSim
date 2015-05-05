package Aspect;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import masSim.schedule.Scheduler;
import masSim.taems.Schedule;
import masSim.taems.Task;
import masSim.world.Agent;
import Brace.DigitialClock;
import Brace.EventAggregator;
import Brace.KeyWordsStore;
import EventMonitor.EventClass;

//Per event has an aspect
//It is very possible to create association between aspect and custome annotation, which are ready avaialbe
//This stage, not in hurry to automate the process, the key is to test whether EventMonitor based on AspectJ
//and ECA monitor synthesized actually work, perform well, and scale
public aspect Spec3Aspect {
//
//	
//	e4:assigned a choice
//	e5:scheduleOptimal With choice
//	
	//pointcut e1PC(SimWorld3 s): target(s) && call(List<IAgent> initAgents());
//	pointcut e1PC(Agent a): this(a) 
//	&& call(void StatementEvent.addTasks() );
	//pointcut e4PC(RavenUI r): target(r) && call(void mousePressed(MouseEvent));
	pointcut e4PC(Agent a): this(a) 
	&& call(void StatementEvent.assignchoice() );
	pointcut e5PC(Scheduler s, Schedule schedule ): this(s) && args(schedule) && call(void StatementEvent.evaluateScheduleOptimal(Schedule));
//	pointcut taskListPC(Scheduler s, List<Task> tasks): this(s) && args(tasks)
//	&& call(void PredicateParameterFilter.addTaskList(List<Task>) );
	pointcut choicePC(Agent a, Task task): this(a) && args(task)
	&& call(void PredicateParameterFilter.addChoiceTask(Task));
//	pointcut worldPC(SimWorld3 world):  args(world)
//	&& call(void PredicateParameterFilter.addWorld(SimWorld3));
	
	after(Agent a): e4PC(a) && !within(Spec3Aspect) {
		try {
			System.out.println("e4");
			//Agent mainInstance = (Agent)EventClass.getComponentInstance("Agent");
		//	if ( mainInstance.equals(a)) {
//				try {
//					EventAggregator.interceptSignal("e4", DigitialClock.returnTicks(System.currentTimeMillis()));
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
				
				try {
					//here hijacking the event generation to produce event surges
					if ( SurgeCreator.createSurge() ) {
						int intEventCount = SurgeCreator.getSurgeNumber();
						for ( int intCounter=0; intCounter<intEventCount; intCounter++) {
							EventAggregator.interceptSignal("e4", DigitialClock.returnTicks(System.currentTimeMillis()));
						}
					} else {
						EventAggregator.interceptSignal("e4", DigitialClock.returnTicks(System.currentTimeMillis()));
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//EventMonitorManager.printoutAllEventMonitorState();
			//}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	after(SimWorld3 world) : worldPC(world) && !within(Spec3Aspect) {
//		try {
//			
//			//Agent mainInstance = (Agent)EventClass.getComponentInstance("Agent");
//			//if ( mainInstance.equals(a)) {
//				System.out.println("World");
//				EventClass.storePredicateParameter("scheduleOptimal", "world", world);
//			
//				
//				//EventClass.storePredicateParameter("containWaypoint", "task", task);
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
//			//}
//		
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	after(Agent a, Task task) : choicePC(a, task) && !within(Spec3Aspect) {
		try {
			
			Agent mainInstance = (Agent)EventClass.getComponentInstance("Agent");
			if ( mainInstance.equals(a)) {
				System.out.println("Choice");
				LinkedList<Object> uncastTasksReach = EventClass.getPredicateQueue("scheduleOptimal", "choice");
				
				LinkedList<Task> tasksToBeChecked = new LinkedList<Task>();
				for ( Object uncastTask: uncastTasksReach ){
					Task existingTasks = (Task)uncastTask;
					tasksToBeChecked.add(existingTasks);
				}
			uncastTasksReach.add(task);
			
				
			EventClass.storePredicateQueue("scheduleOptimal", "choice", uncastTasksReach);
				
				
//				//EventClass.storePredicateParameter("containWaypoint", "task", task);
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
			}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
//	
	
	
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
	
//	after(Agent a): e1PC(a) && !within(Spec1Aspect) {
//		try {
//			System.out.println("e1");
//			Agent mainInstance = (Agent)EventClass.getComponentInstance("Agent");
//			if ( mainInstance.equals(a)) {
//				EventAggregator.interceptSignal("e1", DigitialClock.returnTicks(System.currentTimeMillis()));
//				EventMonitorManager.printoutAllEventMonitorState();
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
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
	
	after(Scheduler s, Schedule schedule): e5PC(s,schedule) && !within(Spec3Aspect) {
		try {
			System.out.println("e5 evaluation");
//			Agent mainInstance = (Agent)EventClass.getComponentInstance("Agent");
//			if ( mainInstance.equals(a)) {
				
				//List<Task> tasks = (List<Task>) EventClass.getPredicateParameter("containWaypoint","task");
				LinkedList<Object> uncastTasks = EventClass.getPredicateQueue("scheduleOptimal", "choice");
				
				
//				LinkedList<Task> tasksToBeChecked = new LinkedList<Task>();
				
//				for ( Object uncastTask: uncastTasks ){
//					Task existingTasks = (Task)uncastTask;
//					tasksToBeChecked.add(existingTasks);
//				}
				
			//	Task task = (Task)EventClass.getPredicateParameter("containWaypoint","task");
				//SimWorld3 world = (SimWorld3)EventClass.getPredicateParameter("scheduleOptimal", "world");
	
//			    boolean findMatch = false;
				boolean isoptimal = false;
				if ( uncastTasks!=null && uncastTasks.size()>0 )  {
					for(Iterator<Object> iterTask = uncastTasks.iterator(); iterTask.hasNext();) {
						Object currentTask = iterTask.next();
						Task taskToBeChecked = (Task) currentTask; 
					    
					    if ( PredicateFunction.isOptimalSchedule(s, schedule, taskToBeChecked) ) {
					    	isoptimal = true;
					    	iterTask.remove();
					    	break;
					    } else {
					    	isoptimal = false;
					    }
				
					}
				}
				
				if ( isoptimal ) {
					try {
						EventAggregator.interceptSignal("e5", DigitialClock.returnTicks(System.currentTimeMillis()));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} 
				else {
					try {
						EventAggregator.interceptSignal(KeyWordsStore.getDefaultNotSign() + "e5", DigitialClock.returnTicks(System.currentTimeMillis()));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
