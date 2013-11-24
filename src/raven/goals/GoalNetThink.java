package raven.goals;

import raven.game.RavenBot;
import raven.utils.Log;
import java.util.Vector;

import raven.game.RavenBot;
import raven.game.NetNode;
import raven.game.RavenObject;
import raven.game.interfaces.INetNode;
import raven.goals.Goal.GoalType;
import raven.math.Vector2D;
import raven.utils.Log;

public class GoalNetThink extends GoalComposite<NetNode> {
	
	public GoalNetThink(NetNode bot) {
		super(bot, Goal.GoalType.goal_netthink);
		Log.debug("GoalNetThink", "created new reasoning module attached to node " + bot.ID());
		
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
		/*
		INetNode[] neighbors = m_pOwner.getSortedNeighbors();
		if(neighbors.length > 0) {
			INetNode node = neighbors[0];
			addGoal_moveToPosition(node.pos());
		}
				
		for(int i = 1; i < neighbors.length; i++){
			INetNode node = neighbors[i];
			queueGoal_moveToPosition(node.pos());
		}
		*/
		INetNode nextNode = m_pOwner.pollClosest();
		if(nextNode != null) {
			addGoal_moveToPosition(nextNode.pos());
		}
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
	
	public boolean notPresent(GoalType goal)
	{
		if (!m_SubGoals.isEmpty()) {
			return !m_SubGoals.get(0).GetType().equals(goal);
		}
		
		return true;
	}

	public void queueGoal_moveToPosition(Vector2D pos) {
		Goal_MoveToPosition moveToPosGoal = new Goal_MoveToPosition(m_pOwner, pos);
		m_SubGoals.add(moveToPosGoal);
		Log.debug("GoalThink", "Queued new Goal_MoveToPosition to bot " + m_pOwner.ID());
	}

	public void addGoal_moveToPosition(Vector2D pos) {
		Goal_MoveToPosition moveToPosGoal = new Goal_MoveToPosition(m_pOwner, pos);
		AddSubgoal(moveToPosGoal);
		Log.debug("GoalThink", "Added new Goal_MoveToPosition to bot " + m_pOwner.ID());
	}
	
}
