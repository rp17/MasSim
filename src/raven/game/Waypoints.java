package raven.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import raven.math.Transformations;
import raven.math.Vector2D;
import raven.ui.GameCanvas;

public class Waypoints {
	private List<Wpt> wpts = new ArrayList<Wpt>(10);
	public class Wpt {
		public Vector2D pos;
		public String name;
		public double x,y;
		public Wpt(Vector2D pos) {
			this(pos,"WP" + wpts.size());
		}
		public Wpt(Vector2D pos, String wayPointName) {
			this.pos = pos;
			x = pos.x;
			y = pos.y;
			name = wayPointName;
		}
	}
	public void addWpt(Vector2D pos) {
		wpts.add(new Wpt(pos));
	}
	public void addWpt(Vector2D pos, String name) {
		wpts.add(new Wpt(pos, name));
	}
	public void removeWpt(Vector2D pos, String name) {
		Wpt selectedWp = null;
		for(Wpt wp: wpts)
		{
			if (wp.name.equals(name))
			{
				selectedWp = wp;
			}
		}
		wpts.remove(selectedWp);
	}
	public void clearWpts(){
		wpts.clear();
	}
	public int size(){return wpts.size();}
	public Waypoints.Wpt get(int i) {return wpts.get(i);}
	public Waypoints.Wpt get(String name)
	{
		for(Waypoints.Wpt wp: wpts)
		{
			if (wp.name==name)
				return wp;
		}
		return null;
	}
	public void render() {		
		GameCanvas.bluePen();
		for (Wpt wpt : wpts) {
			GameCanvas.filledCircle(wpt.x, wpt.y, 3);
			GameCanvas.textAtPos(wpt.x - 10, wpt.y - 5, wpt.name);
		}
		//TODO ASIF CHANGE
		//GameCanvas.greenPen();
		//for (int i=0; i<wpts.size()-1; i++) {
		//	GameCanvas.lineWithArrow(wpts.get(i).pos, wpts.get(i+1).pos, 2.0);
		//}
		
	}	
}
