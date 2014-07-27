package masSim.schedule;
import masSim.taems.*;
import raven.Main;
import java.util.List;

public class Graph {
  private boolean debugFlag = false;
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
	  Main.Message(debugFlag,"[Graph 26] Printing Graph:");
	  for (Method m : methods) {
	  }
	  for (MethodTransition t : transitions) {
		  Main.Message(debugFlag,"[Graph] 28] >"+t.toStringLong());
	  }
  }
  
} 