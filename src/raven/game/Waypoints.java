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
			this.pos = pos;
			x = pos.x;
			y = pos.y;
			name = "WP" + wpts.size();
		}
	}
	public void addWpt(Vector2D pos) {
		wpts.add(new Wpt(pos));
	}
	public void clearWpts(){
		wpts.clear();
	}
	public int size(){return wpts.size();}
	public Waypoints.Wpt get(int i) {return wpts.get(i);}
	public void render() {		
		GameCanvas.bluePen();
		for (Wpt wpt : wpts) {
			GameCanvas.filledCircle(wpt.x, wpt.y, 3);
			GameCanvas.textAtPos(wpt.x - 10, wpt.y - 5, wpt.name);
		}
		GameCanvas.greenPen();
		for (int i=0; i<wpts.size()-1; i++) {
			GameCanvas.lineWithArrow(wpts.get(i).pos, wpts.get(i+1).pos, 2.0);
		}
		
	}	
}
