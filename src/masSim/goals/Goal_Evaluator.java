package masSim.goals;

import masSim.world.SimBot;
import raven.goals.Goal.GoalType;

public abstract class Goal_Evaluator {

	private Double bias;

	private GoalType goalTypeToAdd;

	public Goal_Evaluator(Double bias, GoalType type) {
		this.bias = bias;
		goalTypeToAdd = type;
	}


	public abstract double calculateDesirability(SimBot m_pOwner);


	public abstract void setGoal(SimBot m_pOwner);

	public GoalType getGoalType() { return goalTypeToAdd; }
	public void setGoalType(GoalType type) { goalTypeToAdd = type; }
	
	public Double getBias() {
		return bias;
	}


}
