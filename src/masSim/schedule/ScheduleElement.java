package masSim.schedule;

import java.util.*;
import java.io.*;
import java.awt.*;

public class ScheduleElement {
	  private String title = "";
	  	  
	  public ScheduleElement(String newTitle){
		  title = newTitle;
		  //System.out.println("Schedule Element created with title: " + title);		  
	  }
	  
	  public void setTitle(String newTitle){
		  title = newTitle;
	  }	  

	  public String getTitle(){
		  return title;
	  }	  
}
