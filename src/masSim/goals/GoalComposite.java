package masSim.goals;

import java.util.ArrayList;
import java.util.ListIterator;

import masSim.world.SimBot;

import raven.math.Vector2D;


abstract public class GoalComposite extends Goal {
	
	protected ArrayList <Goal> m_SubGoals;
	//public boolean LaunchedByMasSim = false;
	public boolean LaunchedByMasSim = true; // masSim.goals.GoalComposite can only be launched by MasSim
	public GoalComposite(SimBot PE, masSim.goals.Goal.GoalType type) {
		super(PE, type);
		m_SubGoals = new ArrayList<Goal>();
	}
	public CurrentStatus ProcessSubgoals(double delta){ 
		//remove all completed and failed goals from the front of the subgoal list
		while (!m_SubGoals.isEmpty() &&
				(m_SubGoals.get(0).isComplete() || m_SubGoals.get(0).hasFailed()))
		{    
			m_SubGoals.get(0).terminate();
			m_SubGoals.remove(0);
		}

		//if any subgoals remain, process the one at the front of the list
		if (!m_SubGoals.isEmpty())
		{ 
			//grab the status of the front-most subgoal
			Goal.CurrentStatus StatusOfSubGoals = m_SubGoals.get(0).process(delta);
			
			//we have to test for the special case where the front-most subgoal
			//reports 'completed' *and* the subgoal list contains additional goals.When
			//this is the case, to ensure the parent keeps processing its subgoal list
			//we must return the 'active' status.
			if (StatusOfSubGoals == Goal.CurrentStatus.completed && m_SubGoals.size() > 1)
			{
				return Goal.CurrentStatus.active;
			}
			return StatusOfSubGoals;
		}

		//no more subgoals to process - return 'completed'
		else
		{
			this.m_iStatus = Goal.CurrentStatus.completed;
			return Goal.CurrentStatus.completed;
		}
	}

	public void removeAllSubgoals() {
		for (Goal goal : m_SubGoals)
			goal.terminate();
		
		m_SubGoals.clear();
	}

	public void AddSubgoal(Goal g)
	{   
		//add the new goal to the front of the list
		//m_SubGoals.add(0, g);
		m_SubGoals.add(g);
	}
	
	public synchronized void ClearAndAddSubgoal(Goal g)
	{   
		m_SubGoals.clear();
		m_SubGoals.add(g);
	}


}
