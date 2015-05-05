package raven.game.interfaces;
import raven.goals.GoalThink;
import raven.math.Vector2D;

public interface IBot {
	public Vector2D pos();
	public void update(double delta);

}
