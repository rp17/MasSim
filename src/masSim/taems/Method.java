package masSim.taems;

import java.util.Date;
import java.util.Iterator;
import java.util.Observable;

import masSim.schedule.Scheduler;

public class Method extends Node implements IMethod {

	private static int Index = 1;
	private int index;
	private Outcome outcome;//Change to Vector
	public int deadline = 0;
	public int x, y;
	
	// Constructor
	public Method(String nm, double outcomeQuality, int x, int y){
		this(nm,outcomeQuality, x, y, 0);
	}
	public Method(String nm, double outcomeQuality, int x, int y, int dl){
		label = nm;
		outcome = new Outcome(outcomeQuality, -1, 0);
		index = Index++;
		deadline = dl;
		this.x = x;
		this.y = y;
	}
	public Method(Method m){
		this(m.label,m.outcome.getQuality(), m.x, m.y, m.deadline);
	}
	public boolean IsTask(){return false;}
	
	@Override
	public void MarkCompleted()
	{
		super.MarkCompleted();
		//Only print if its an actual task, and not an FSM connective created by the scheduler
		if (outcome.quality>0) System.out.println("Method " + label + " completed in duration " + outcome.duration + " with quality " + outcome.quality);
		this.NotifyAll();
	}
	
	public DijkstraDistance getPathUtilityRepresentedAsDistance()
	{
		DijkstraDistance previous = new DijkstraDistance(0,0);
		return getPathUtilityRepresentedAsDistance(previous);
	}
	public DijkstraDistance getPathUtilityRepresentedAsDistance(DijkstraDistance distanceTillPreviousNode)
	{
		//This is distance calculation for this step only. Previous distance used for calculation, but not appended
		DijkstraDistance d = new DijkstraDistance(0,0);
		//If task can be performed, return utility value through the function. But if its deadline has passed
		//then return an abnormally large negative utility value to force Dijkstra to reject it.
		if ((distanceTillPreviousNode.duration+this.outcome.duration)>deadline && deadline!=0) 
			d.distance = 10000;
		else
		{
			//This can be any formula combining different outcomes and objectively comparing them
			double maxUtility = 0;
			if (outcome.quality!=0) maxUtility = 10*(1/outcome.quality);
			d.distance = maxUtility;
		}
		d.duration = outcome.duration;
		return d;
	}
	
	
	public int getIndex() {return index;}
	public Outcome getOutcome(){return outcome;}
	public int getDeadline(){return deadline;}
	
	public String generateName(){
		String resName = label + Integer.toString(index);
		index++;
		return resName;
	}
	
	  @Override
	  public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + index;
	    return result;
	  }
	  
	  @Override
	  public boolean equals(Object obj) {
	    if (this == obj)
	      return true;
	    if (obj == null)
	      return false;
	    if (getClass() != obj.getClass())
	      return false;
	    Method other = (Method) obj;
	    if (index == 0) {
	      if (other.index != 0)
	        return false;
	    } else if (!(index == other.index))
	      return false;
	    return true;
	  }

	  @Override
	  public String toString() {
	    return label;
	  }
	  
	@Override
	public Iterator<Node> getSubtasks() {
		return null;
	}
		
	
}
