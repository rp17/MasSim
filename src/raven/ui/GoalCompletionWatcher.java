package raven.ui;

import masSim.taems.IAgent;
import masSim.taems.Method;
import raven.Main;
import raven.game.RoverBot;
import raven.goals.GoalComposite;

public class GoalCompletionWatcher implements Runnable {

	private boolean debugFlag = false;
	private GoalComposite<RoverBot> goal;
	private IAgent agent;
	private Method method;
	
	public GoalCompletionWatcher(GoalComposite<RoverBot> goal, IAgent agent, Method method)
	{
		this.goal = goal;
		if (goal==null)
			Main.Message(debugFlag, "[GoalCompletionWatcher 19] Goal is null");
		this.agent = agent;
		Main.Message(debugFlag, "[GoalCompletionWatcher 19] Agent is " + agent.getName() + " in " + Thread.currentThread().getName());
		this.method = method;
	}
	
	@Override
	public void run() {
		while(!goal.isComplete())//Wait for completion
		{
			Main.Message(debugFlag, "[GoalCompletionWatcher 29] Goal not yet completed");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (this.agent!=null)
			this.agent.MarkMethodCompleted(method);
		else
			Main.Message(debugFlag, "[GoalCompletionWatcher 35] Agent found to be null in " + Thread.currentThread().getName());
	}

}
