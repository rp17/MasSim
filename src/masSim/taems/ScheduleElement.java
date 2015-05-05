package masSim.taems;

import java.util.*;
import java.io.*;
import java.awt.*;

import raven.math.Vector2D;

public class ScheduleElement implements Comparable<ScheduleElement>
{
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

		  this.name = mt.label;
		  this.method = mt;
		  elapsedTime = 0;
		  status = Status.ACTIVE;
		  
		  //System.out.println("Schedule Element created with name: " + name);
	  }

	  public String getName(){
		  return name;
	  }
	  
	  @Override
	  public String toString(){
		  return getName() + "[" + hashCode() + "]";
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

	@Override
	public int compareTo(ScheduleElement arg0) {
		if (this.method==null) return 0;
		if (this.method.agent==null) return 0;
		if (arg0.method==null) return 0;
		if (arg0.method.agent==null) return 0;
		Vector2D agentPos1 = this.getMethod().agent.getPosition();
		double dist1 = agentPos1.distance(this.getMethod().getPosition());
		Vector2D agentPost2 = arg0.getMethod().agent.getPosition();
		double dist2 = agentPost2.distance(arg0.getMethod().getPosition());
		return (int)(dist1 - dist2);
	}
	  
}
