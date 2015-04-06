package masSim.goals;

import masSim.world.SimBot;
import raven.math.Vector2D;

public abstract class Goal {
	public static enum CurrentStatus{active, inactive, completed, failed}
	public static enum GoalType{goal_follow_path, 
		goal_seek_to_position, goal_traverse_edge, goal_wander,  
		goal_roverthink, unknown_type}


	// reference to owner of this object.

	SimBot m_pOwner;
	//an enumerated value indicating the goal's status (active, inactive,
	//completed, failed)
	CurrentStatus m_iStatus;

	GoalType m_iType;

	//if m_iStatus is failed this method sets it to inactive so that the goal
	//will be reactivated (replanned) on the next update-step.
	protected void reactivateIfFailed()
	{
		if (hasFailed())
		{
			m_iStatus = Goal.CurrentStatus.inactive;
		}
	}
	
	public void SetCompleted()
	{
		m_iStatus = Goal.CurrentStatus.completed;
	}
	
	protected void activateIfInactive()
	{
		if (isInactive())
		{
			activate();   
		}
	}
	
	public Goal(SimBot PE, GoalType type ){
		m_iType = type;
		m_pOwner  = PE;
		m_iStatus = Goal.CurrentStatus.inactive;
	}

	public abstract void activate();
	public abstract CurrentStatus process(double delta);
	public abstract void terminate();

	public boolean isComplete() { return m_iStatus == Goal.CurrentStatus.completed; }
	public boolean isActive(){ return m_iStatus == Goal.CurrentStatus.active; }
	boolean isInactive(){ return m_iStatus == Goal.CurrentStatus.inactive; }
	boolean hasFailed(){ return m_iStatus == Goal.CurrentStatus.failed; }

	public GoalType GetType(){ return m_iType;}

}
