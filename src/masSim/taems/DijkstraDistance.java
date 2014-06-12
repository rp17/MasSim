package masSim.taems;

public class DijkstraDistance {
	public double distance = 0;
	public double duration = 0;
	
	public DijkstraDistance(double dist, double dur)
	{
		distance = dist;
		duration = dur;
	}
	
	public DijkstraDistance Add(DijkstraDistance d2)
	{
		return new DijkstraDistance(this.distance + d2.distance,this.duration + d2.duration);
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
