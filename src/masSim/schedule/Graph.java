package masSim.schedule;
import masSim.taems.*;

import java.util.List;

public class Graph {
  private final List<Method> methods;
  private final List<MethodTransition> transitions;

  public Graph(List<Method> methods, List<MethodTransition> transitions) {
    this.methods = methods;
    this.transitions = transitions;
  }

  public List<Method> getMethods() {
    return methods;
  }

  public List<MethodTransition> getTransitions() {
    return transitions;
  }
  
  public void Print()
  {
	  System.out.println("[Graph] Printing Graph");
	  System.out.println("Nodes:");
	  for (Method m : methods) {
	    System.out.println(">"+m.toStringLong());
	  }
	  System.out.println("Edges:");
	  for (MethodTransition t : transitions) {
		  System.out.println(">"+t.toStringLong());
	  }
	  System.out.println("");
  }
  
} 