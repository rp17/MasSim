package masSim.taems;

import raven.Main;
import raven.math.Vector2D;

public class DijkstraDistance {
	public boolean debugFlag = false;
	public double quality = 0;
	public double duration = 0;
	public Vector2D position;
	public String nodeName;
	
	public DijkstraDistance(double qual, double dur, double x, double y, String nodeName)
	{
		quality = qual;
		duration = dur;
		position = new Vector2D(x,y);
	}
	
	public DijkstraDistance Add(DijkstraDistance d2)
	{
		return new DijkstraDistance(this.quality + d2.quality,this.duration + d2.duration, position.x, position.y, this.toString());
	}
	
	public boolean HasGreaterUtility(DijkstraDistance d2, Method source)
	{
		//Current implementation is quality based only
		double thisQuality = this.quality - source.getPosition().distance(this.position);
		double d2Quality = d2.quality - source.getPosition().distance(d2.position);
		boolean result = thisQuality>d2Quality;
		if (result)
		{
			Main.Message(debugFlag, "[DijkstraDistance] Utility " + source.label + "-" + nodeName + " (" + thisQuality + ")>"
					+ source.label + "-" + d2.nodeName + "(" + d2Quality + ")");
		}
		return result;
	}
	
}
