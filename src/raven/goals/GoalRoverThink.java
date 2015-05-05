package raven.goals;
import raven.game.RavenBot;
import raven.utils.Log;
import java.util.Vector;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.Queue;

import raven.game.RavenBot;
import raven.game.RoverBot;
import raven.game.RavenObject;
import raven.game.interfaces.INetNode;
import raven.goals.Goal.GoalType;
import raven.math.Vector2D;
import raven.utils.Log;

public class GoalRoverThink extends GoalComposite<RoverBot> {

	//private ConcurrentLinkedDeque<Goal> schedule = new ConcurrentLinkedDeque<Goal>();
	public GoalRoverThink(RoverBot bot) {
		super(bot, Goal.GoalType.goal_roverthink);
		Log.debug("GoalRoverThink", "created new Rover reasoning module attached to bot " + bot.ID());
		
	}

	public void Arbitrate() {
		//----------------------------- Update ----------------------------------------
		// 
		//  this method iterates through each goal option to determine which one has
		//  the highest desirability.
		//-----------------------------------------------------------------------------
		double best = 0;
		
		//Log.debug("GoalThink", "Evaluator " + MostDesirable.getGoalType() + " was found for bot " + m_pOwner.ID());
		//MostDesirable.setGoal(m_pOwner);
	}
	@Override
	public void activate() {
		m_iStatus = Goal.CurrentStatus.active;
	}

	@Override
	public raven.goals.Goal.CurrentStatus process(double delta) {
		activateIfInactive();

		raven.goals.Goal.CurrentStatus SubgoalStatus = ProcessSubgoals(delta);
		
		if (SubgoalStatus == Goal.CurrentStatus.completed || SubgoalStatus == Goal.CurrentStatus.failed)
		{
			if (!m_pOwner.isPossessed())
			{
				m_iStatus = Goal.CurrentStatus.inactive;
			}
		}

		return m_iStatus;
	}

	@Override
	public void terminate() {
		// TODO Auto-generated method stub
		
	}
	/*
	public void addGoal(Goal goal) {
		schedule.addLast(goal);
	}
	*/
	public boolean notPresent(GoalType goal)
	{
		if (!m_SubGoals.isEmpty()) {
			return !m_SubGoals.get(0).GetType().equals(goal);
		}
		
		return true;
	}
}
