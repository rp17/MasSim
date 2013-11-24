package raven.goals;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import raven.game.RavenBot;
import raven.game.RoverBot;
import raven.game.navigation.PathEdge;
import raven.goals.Goal.CurrentStatus;

public class Goal_PIDFollowPath extends GoalComposite<RoverBot> {
	private List<PathEdge>  m_Path = new ArrayList<PathEdge>();

	public Goal_PIDFollowPath(RoverBot m_pOwner, List<PathEdge> list) {
		super(m_pOwner, Goal.GoalType.goal_follow_path);
		this.m_Path = list;
		for(int i=0; i< (m_Path.size() - 1); i++) {
			PathEdge edge = m_Path.get(i);
			AddSubgoal(new Goal_PidTraverseEdge(m_pOwner, edge, false));
		}
		if(m_Path.size() > 0) {
			PathEdge edge = m_Path.get(m_Path.size() - 1);
			AddSubgoal(new Goal_PidTraverseEdge(m_pOwner, edge, true));
		}
		
	}

	@Override
	public void activate() { 
		// Clean subgoals of finished goals.
		List<Goal> toRemove = new ArrayList<Goal>();
		for(Goal goal : m_SubGoals) {
			if(goal.m_iStatus == CurrentStatus.completed){
				toRemove.add(goal);
			}
		}
		m_SubGoals.removeAll(toRemove);
		m_pOwner.startPid();
	}

	@Override
	public CurrentStatus process(double delta) {
		//if status is inactive, call Activate()
		activateIfInactive();
		m_iStatus = ProcessSubgoals(delta);

		//if there are no subgoals present check to see if the path still has edges.
		//remaining. If it does then call activate to grab the next edge.
		if (m_iStatus == Goal.CurrentStatus.completed && !m_Path.isEmpty()) {
			activate(); 
		}
		return m_iStatus;
	}

	public void render() {

		// forward the request to the subgoals
		// in this case we imitate goalComposite . render()
		for(Goal goal : m_SubGoals) {
			goal.render();
		}
	}

	@Override
	public void terminate() {
		m_iStatus = Goal.CurrentStatus.completed;
		m_pOwner.stopPid();
	}
}
