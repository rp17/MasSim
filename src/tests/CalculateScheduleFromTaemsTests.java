package tests;

import static org.junit.Assert.*;
import junit.framework.Assert;
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
		agentMain = new Agent("Helicopter0", true, 10, 10, null);
		agentTwo = new Agent("Helicopter1", false, 40, 200, null);
		topLevelTask = new Task("Container Task",new SumAllQAF(), agentMain);
	}
	
	private void checkTaskName(Schedule s, String nm)
	{
		String name = s.poll().getName();
		System.out.println(name + " : " + nm);
		Assert.assertEquals(true, name.equals(nm));
	}

	@Test
	public void SimpleScheduletest()
	{
		topLevelTask.addTask(new Task("Station A1",new SumAllQAF(), agentMain, new Method("Visit Station A1",10,10,10,20,30,null)));
		topLevelTask.addTask(new Task("Station A2",new SumAllQAF(), agentMain, new Method("Visit Station A2",11,11,10,30,10,null)));
		topLevelTask.addTask(new Task("Station A3",new SumAllQAF(), agentMain, new Method("Visit Station A3",12,10,40)));
		topLevelTask.addTask(new Task("Station A4",new SumAllQAF(), agentMain, new Method("Visit Station A4",13,10,50)));
		
		Scheduler scheduler = new Scheduler(agentMain);
		Schedule s = scheduler.CalculateScheduleFromTaems(topLevelTask);
		s.poll();
		checkTaskName(s,"Visit Station A2");
		checkTaskName(s,"Visit Station A1");
		checkTaskName(s,"Visit Station A4");
		checkTaskName(s,"Visit Station A3");
	}

}
