package masSim.taems;

import raven.Main;
import raven.math.Vector2D;

public class DijkstraDistance {
	public double quality = 0;
	public double duration = 0;
	public Vector2D vector;
	public boolean debugFlag = false;
	public String nodeName;
	
	public DijkstraDistance(double qual, double dur, double x, double y)
	{
		quality = qual;
		duration = dur;
		vector = new Vector2D(x,y);
	}
	
	public DijkstraDistance Add(DijkstraDistance d2)
	{
		return new DijkstraDistance(this.quality + d2.quality,this.duration + d2.duration, vector.x, vector.y);
	}
	
	public boolean HasGreaterUtility(DijkstraDistance d2, Method source)
	{
		//Current implementation is quality based only
		double thisQuality = this.quality - source.getPosition().distance(this.vector);
		double d2Quality = d2.quality - source.getPosition().distance(d2.vector);
		boolean result = thisQuality>d2Quality;
		if (result)
		{
			Main.Message(debugFlag, "[DijkstraDistance] Utility " + source.label + "-" + nodeName + " (" + thisQuality + ")>"
					+ source.label + "-" + d2.nodeName + "(" + d2Quality + ")");
		}
		return result;
	}
	
}
