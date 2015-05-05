package Aspect;

import java.util.Iterator;

import masSim.schedule.Scheduler;
import masSim.taems.Method;
import masSim.taems.Schedule;
import masSim.taems.ScheduleElement;
import masSim.taems.Task;
import BAEventMonitor.Predicate;

public class PredicateFunction {

	@Predicate(name="containWaypoint")
	public static boolean containWaypointSingle(Schedule sc, Task task) {
		
		//print out schedule and tasklist
		//must be something wrong with quantification
		
		Iterator<ScheduleElement> itr = sc.getItems();
		while ( itr.hasNext() ) {
			ScheduleElement element = itr.next();
			System.out.println("Schedule Element: " + element);
			if ( element.getName().contains(task.label)) {
				if ( LocalErrorController.hasInjectedErrors() ) {
					if ( LocalErrorController.getInjectedErrors() ) {
						return false;
					}
				}
				return true;
			}
		}
		
		return false;
				//inject errors
//			Random rand = new Random();
//            int x = rand.nextInt(50) + 1;
//           if ( x > 25) {
//        	  return true;
//           } else {
//	           	injectedError++;
//	           	System.out.println("Injected Error Spec 1: " + injectedError);
//	           	return false;
//           }
		
	}	
	
	//instrumentation for spec 2
	@Predicate(name="reachWaypoint")
	public static boolean reachWaypoint(Task task, Method m) {
		if ( m.getLabel().equals("Visit " + task.getLabel() ) ) {
			if ( LocalErrorController.hasInjectedErrors() ) {
				if ( LocalErrorController.getInjectedErrors() ) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	
	@Predicate(name="scheduleOptimal")
	public static boolean isOptimalSchedule(Scheduler s, Schedule sh, Task choice){
		if ( LocalErrorController.hasInjectedErrors() ) {
			if ( LocalErrorController.getInjectedErrors() ) {
				return false;
			}
		}
		return true;
	}
	

//	 @Predicate(name="scheduleOptimal")
//	 public boolean isOptimalAgent(IAgent ag, double x, double y) {
//	    	Vector2D loc = new Vector2D(x, y);
//	    	IAgent bestAg = ag;
//	    	RoverBot bestBot = (RoverBot)Main.game.getBotByName(ag.getName());
//	    	if(bestBot == null) return false;
//	    	double shortestSqDist = loc.distanceSq(bestBot.pos());
//	    	
//	    	for(IAgent agent : agents) {
//	    		RoverBot bot = (RoverBot)Main.game.getBotByName(agent.getName());
//	    		double sqDist = loc.distanceSq(bot.pos());
//	    		if(sqDist < shortestSqDist) {
//	    			shortestSqDist = sqDist;
//	    			bestAg = agent;
//	    		}
//	    	}
//	    	boolean result = (ag == bestAg);
//	    	if ( result ) {
//	    		//inject errors
//				Random rand = new Random();
//	            int chance = rand.nextInt(50) + 1;
//	           if ( chance > 25) {
//	        	   injectedSpec3Error++;
//	        	   System.out.println("Inject Spec 3 Error: " + injectedSpec3Error);
//	        	   return false;
//	           } else {
//	        	  return true;
//	           }
//	    	}
//	    	return result;
//	    }
	
}
