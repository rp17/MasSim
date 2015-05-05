package Aspect;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import masSim.schedule.Scheduler;
import masSim.taems.Method;
import masSim.taems.Task;
import masSim.world.Agent;
import Brace.DigitialClock;
import Brace.EventAggregator;
import EventMonitor.EventClass;

//Per event has an aspect
//It is very possible to create association between aspect and custome annotation, which are ready avaialbe
//This stage, not in hurry to automate the process, the key is to test whether EventMonitor based on AspectJ
//and ECA monitor synthesized actually work, perform well, and scale
public aspect Spec2Aspect {
//
//	e3:reachWaypoint With task and execution
//	e1:chosen to reach a way-point
	
	//pointcut e1PC(SimWorld3 s): target(s) && call(List<IAgent> initAgents());
//	pointcut e1PC(Agent a): this(a) 
//	&& call(void StatementEvent.addTasks() );
	//pointcut e4PC(RavenUI r): target(r) && call(void mousePressed(MouseEvent));
	pointcut e3PC(Agent a): this(a) && call(void StatementEvent.evaluateReachAWayPoint());
//	pointcut taskListPC(Scheduler s, List<Task> tasks): this(s) && args(tasks)
//	&& call(void PredicateParameterFilter.addTaskList(List<Task>) );
	pointcut taskPC(Scheduler s, Task task): this(s) && args(task)
	&& call(void PredicateParameterFilter.addTask(Task) );
	pointcut methodPC(Agent a, Method m): this(a) && args(m)
	&& call(void PredicateParameterFilter.addMethod(Method) );
	
	after(Scheduler s, Task task) : taskPC(s, task) && !within(Spec2Aspect) {
		try {
			
			Agent mainInstance = (Agent)EventClass.getComponentInstance("Agent");
			if ( mainInstance.currentSchedule.equals(s)) {
				System.out.println("Task");
				LinkedList<Object> uncastTasksReach = EventClass.getPredicateQueue("reachWaypoint", "task");
				
//				LinkedList<Task> tasksToBeChecked = new LinkedList<Task>();
//				for ( Object uncastTask: uncastTasks ){
//					Task existingTasks = (Task)uncastTask;
//					tasksToBeChecked.add(existingTasks);
//				}
				uncastTasksReach.add(task);
			
				
				EventClass.storePredicateQueue("reachWaypoint", "task", uncastTasksReach);
				
				
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
	
	
	after(Agent a, Method m) : methodPC(a, m) && !within(Spec2Aspect) {
		try {
			
			Agent mainInstance = (Agent)EventClass.getComponentInstance("Agent");
			if ( mainInstance.equals(a)) {
				System.out.println("Method");
				LinkedList<Object> uncastMethod = EventClass.getPredicateQueue("reachWaypoint", "execution");
//				LinkedList<Task> tasksToBeChecked = new LinkedList<Task>();
//				for ( Object uncastTask: uncastTasks ){
//					Task existingTasks = (Task)uncastTask;
//					tasksToBeChecked.add(existingTasks);
//				}
				uncastMethod.add(m);
			
				EventClass.storePredicateQueue("reachWaypoint", "execution", uncastMethod);
				
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
	
	after(Agent a): e3PC(a) && !within(Spec2Aspect) {
		try {
			System.out.println("e3 evaluation");
			Agent mainInstance = (Agent)EventClass.getComponentInstance("Agent");
			if ( mainInstance.equals(a)) {
				
				//List<Task> tasks = (List<Task>) EventClass.getPredicateParameter("containWaypoint","task");
				LinkedList<Object> uncastTasks = EventClass.getPredicateQueue("reachWaypoint", "task");
				
				
//				LinkedList<Task> tasksToBeChecked = new LinkedList<Task>();
				
//				for ( Object uncastTask: uncastTasks ){
//					Task existingTasks = (Task)uncastTask;
//					tasksToBeChecked.add(existingTasks);
//				}
				
			//	Task task = (Task)EventClass.getPredicateParameter("containWaypoint","task");
				LinkedList<Object> uncastMethods = EventClass.getPredicateQueue("reachWaypoint", "execution");
				
				if ( uncastTasks!=null && uncastTasks.size()>0 && uncastMethods != null && uncastMethods.size() > 0)  {
					for(Iterator<Object> iterTask = uncastTasks.iterator(); iterTask.hasNext();) {
						Object currentTask = iterTask.next();
						Task taskToBeChecked = (Task) currentTask; 
					    boolean findMatch = false;
						for(Iterator<Object> iter = uncastMethods.iterator(); iter.hasNext();) {
						    Object current = iter.next();
						    Method converted = (Method)current;
						    if ( PredicateFunction.reachWaypoint(taskToBeChecked, converted)) {
						    	findMatch = true;
						    	iter.remove();
						    	break;
						    }
						}
					
						if ( findMatch ) {
							iterTask.remove();
//							EventAggregator.interceptSignal("e3", DigitialClock.returnTicks(System.currentTimeMillis()));
							
							//inject errors
//							Random rand = new Random();
//				            int x = rand.nextInt(50) + 1;
//				           if ( x > 25) {
				        	   try {
								EventAggregator.interceptSignal("e3", DigitialClock.returnTicks(System.currentTimeMillis()));
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
//				           } else {
//				        	   StatementEvent.injectedSpec2Error++;
//					           	System.out.println("Injected Error spec 2: " + StatementEvent.injectedSpec2Error);
//					           	EventAggregator.interceptSignal(KeyWordsStore.getDefaultNotSign() + "e3", DigitialClock.returnTicks(System.currentTimeMillis()));
//				           }
						} 
//						else {
//							EventAggregator.interceptSignal(KeyWordsStore.getDefaultNotSign() + "e3", DigitialClock.returnTicks(System.currentTimeMillis()));
//						}
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
				
				
			}
		
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
