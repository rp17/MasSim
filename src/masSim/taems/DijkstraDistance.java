package masSim.taems;

import raven.math.Vector2D;

public class DijkstraDistance {
	public double distance = 0;
	public double duration = 0;
	public Vector2D vector;
	
	public DijkstraDistance(double dist, double dur, double x, double y)
	{
		distance = dist;
		duration = dur;
		vector = new Vector2D(x,y);
	}
	
	public DijkstraDistance Add(DijkstraDistance d2)
	{
		return new DijkstraDistance(this.distance + d2.distance,this.duration + d2.duration, vector.x, vector.y);
	}
	
	public boolean IsGreaterThen(DijkstraDistance d2)
	{
		return this.distance>d2.distance;
	}
	
	public boolean HasGreaterUtilityThen(DijkstraDistance d2)
	{
		return this.distance > d2.distance;
	}
}
