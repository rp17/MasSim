package masSim.taems;

import java.util.*;
import java.io.*;
import java.awt.*;

public class ScheduleItem {
	  private String name;
	  private Method method;
	  private int duration;
	  private int elapsedTime;
	  private Status status;
	  public enum Status {
		    ACTIVE, COMPLETED 
		}
	  
	  // Constructor
	  public ScheduleItem(){
		  
	  }
	  
	  public ScheduleItem(Method mt){
		  
		  if (mt == null){
			  throw new NullMethod("ScheduleItem.Constructor: Null method passed");
		  }

		  this.name = mt.label;
		  this.method = mt;
		  //duration = mt.getDuration();
		  elapsedTime = 0;
		  status = Status.ACTIVE;
		  
		  System.out.println("Schedule Item created with name: " + name);
	  }

	  public String getName(){
		  return name;
	  }	  
	  
	  public Status update(int dt){
		  int expectedTime = elapsedTime + dt;
		  if ( expectedTime >= duration){
			  elapsedTime = duration;
			  status = Status.COMPLETED;
		  }
		  else{
			  elapsedTime += dt;
		  }
		  return status;
	  }
	  
	  public Status getStatus(){return status;}
	  
}
