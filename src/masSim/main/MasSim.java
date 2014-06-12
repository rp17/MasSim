package masSim.main;

import masSim.world.*;
import masSim.schedule.Scheduler;
import masSim.taems.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import javax.swing.JFrame;
 
public class MasSim {
	
		private static SimWorld simWorld; 
		
		public static void EscortTest() throws InterruptedException
		{	
			
			
			//Thread.sleep(2000);
			//Task tg3 = new Task("Middle Station",new SeqSumQAF());
			//tg3.addTask(new Method("Middle Station M",2,1));
			//helicopter1.assignTask(tg3);//Include ability to ensure that one task is only executed by one agent, when both are capable of executing it
			//helicopter2.assignTask(tg3);
		}
		
		/*public static void Test1(Scheduler s)
		{
			Task t1 = new Task("Task Group",new SeqSumQAF());
	    		Task t2 = new Task("Get Fuel", new ExactlyOneQAF());
	    		t2.addTask(new Method("Get Fuel from Station 1",1,4));
	    		t2.addTask(new Method("Get Fuel from Station 2",2,2));
	    	t1.addTask(t2);
	    		Task t3 = new Task("Perform Transporation",new SumAllQAF());
	    			Task t4 = new Task("Pick A",new SeqSumQAF());
	    			t4.addTask(new Method("Fly To A Origin",1,2));
	    			t4.addTask(new Method("Pickup A",1,2));
	    		t3.addTask(t4);
	    			Task t5 = new Task("Pick B",new SeqSumQAF());
	    			t5.addTask(new Method("Fly To B Origin",1,2));
	    			t5.addTask(new Method("Pickup B",1,2));
	    		t3.addTask(t5);	
	    	t1.addTask(t3);
	    	//s.CalculateScheduleFromTaems(t1);
		}*/

	    //public static void main(String args[]) {
	    	//EscortTest();
	    	//simWorld = new SimWorld();
	    //}

		
}