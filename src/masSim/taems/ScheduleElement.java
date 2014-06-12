package masSim.taems;

import java.util.*;
import java.io.*;
import java.awt.*;

public class ScheduleElement {
	  private String name;
	  private Method method;
	  private double elapsedTime;
	  private Status status;
	  public static int sleepTime = 300; 
	  public enum Status {
		    ACTIVE, COMPLETED 
		}
	  
	  public Method getMethod()
	  {
		  return method;
	  }
	  
	  // Constructor
	  public ScheduleElement(Method mt){
		  
		  if (mt == null){
			  throw new NullMethod("ScheduleElement.Constructor: Null method passed");
		  }

		  this.name = mt.generateName();
		  this.method = mt;
		  elapsedTime = 0;
		  status = Status.ACTIVE;
		  
		  //System.out.println("Schedule Element created with name: " + name);
	  }

	  public String getName(){
		  return name;
	  }	  
	  
	  public Status update(int dt){
		  double expectedTime = elapsedTime + dt;
		  if ( expectedTime >= method.getOutcome().duration){
			  elapsedTime = method.getOutcome().duration;
			  status = Status.COMPLETED;
		  }
		  else{
			  elapsedTime += dt;
		  }
		  try {
			  Thread.sleep(sleepTime); 
		  } catch (InterruptedException e) {
			System.out.println("Thread sched el " + name + " interrupted");
		  }
		  return status;
	  }
	  
	  public Status getStatus(){return status;}
	  
}
