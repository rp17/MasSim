package tests;

import static org.junit.Assert.*;
import masSim.schedule.Scheduler;
import masSim.taems.Method;
import masSim.taems.Schedule;
import masSim.taems.SumAllQAF;
import masSim.taems.Task;
import masSim.world.Agent;

import org.junit.Before;
import org.junit.Test;

public class CalculateScheduleFromTaemsTests {

	Agent agentMain;
	Agent agentTwo;
	Task topLevelTask;
	
	@Before
	public void setUp() throws Exception {
		agentMain = new Agent("Helicopter0", true, 40, 100, null);
		agentTwo = new Agent("Helicopter1", false, 40, 200, null);
		topLevelTask = new Task("Container Task",new SumAllQAF(), null, agentMain);
	}

	@Test
	public void SimpleScheduletest() 
	{
		topLevelTask.addTask(new Task("Station A1",new SumAllQAF(), new Method("Visit Station A1",10,100,110),agentMain));
		topLevelTask.addTask(new Task("Station A2",new SumAllQAF(), new Method("Visit Station A2",10,100,110),agentMain));
		topLevelTask.addTask(new Task("Station B1",new SumAllQAF(), new Method("Visit Station B1",10,100,110),agentTwo));
		topLevelTask.addTask(new Task("Station B2",new SumAllQAF(), new Method("Visit Station B2",10,100,110),agentTwo));
		topLevelTask.addTask(new Task("Station B3",new SumAllQAF(), new Method("Visit Station B3",10,100,110),agentTwo));
		
		Scheduler scheduler = new Scheduler(agentMain);
		Schedule s = scheduler.CalculateScheduleFromTaems(topLevelTask);
		System.out.println(s.TotalQuality);
		assert(s.TotalQuality>0);
	}

}
