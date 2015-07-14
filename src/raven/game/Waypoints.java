package raven.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.SwingUtilities;

import raven.math.Transformations;
import raven.math.Vector2D;
import raven.ui.GameCanvas;

public class Waypoints {
	private List<Wpt> wpts = new ArrayList<Wpt>(10);
	private Map<String, Wpt> wptsMap = new HashMap<String, Wpt>();
	private static int nextNum = 0;
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
	public synchronized void addWpt(Vector2D pos) {
		String nextWptName = "selfGenWaypoint" + nextNum;
		nextNum++;
		Wpt wpt = new Wpt(pos, nextWptName);
		wpts.add(wpt);
		wptsMap.put(nextWptName, wpt);
		notifyAll();
	}
	public synchronized void addWpt(Vector2D pos, String name) {
		Wpt wpt = new Wpt(pos, name);
		wpts.add(wpt);
		wptsMap.put(name, wpt);
		notifyAll();
	}
	public void removeWpt(String name) {
		Wpt wpt = wptsMap.get(name);
		if(wpt != null) {
			wpts.remove(wpt);
			wptsMap.remove(name);
		}
	}
	public void removeWpt(Vector2D pos, String name) {
		removeWpt(name);
	}

	public void clearWpts(){
		wpts.clear();
		wptsMap.clear();
	}
	public int size(){return wpts.size();}
	public Waypoints.Wpt get(int i) {return wpts.get(i);}
	public Waypoints.Wpt get(String name) {
/*		try {
			while(wptsMap.get(name) == null) {

//				SwingUtilities.invokeLater(new Runnable(){public void run(){System.out.println("Waypoints.Wpt : Thread goes to sleep because method name not found");}});

				wait();
			} }
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		return wptsMap.get(name);
	}
	public synchronized void render() {		
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
