package masSim.schedule;
import masSim.taems.*;

public class MethodTransition  {
	  private final String id; 
	  private final Method source;
	  private final Method destination; 
	  
	  public MethodTransition(String id, Method source, Method destination) {
	    this.id = id;
	    this.source = source;
	    this.destination = destination;
	  }
	  
	  public String getId() {
	    return id;
	  }
	  public Method getDestination() {
	    return destination;
	  }

	  public Method getSource() {
	    return source;
	  }
	  public DijkstraDistance getPathUtility( DijkstraDistance distanceTillPreviousNode) {
	    return destination.getPathUtilityRepresentedAsDistance(distanceTillPreviousNode);
	  }
	  
	  @Override
	  public String toString() {
	    return source + " " + destination;
	  }
	  
	  
	} 