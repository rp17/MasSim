package masSim.schedule;
import masSim.taems.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import raven.Main;
import raven.math.Vector2D;


public class DijkstraAlgorithm {

	public enum OptimizationMode {
	    QUALITY,  DURATION
	}
	
  private boolean debugFlag = false;
  private final List<Method> nodes;
  private final List<MethodTransition> edges;
  private Set<Method> settledNodes;
  private Set<Method> unSettledNodes;
  private Map<Method, Method> predecessors;
  private Map<Method, DijkstraDistance> distance;
  private Vector2D agentPos;
  private final OptimizationMode optimizationMode = OptimizationMode.QUALITY;

  public DijkstraAlgorithm(Graph graph, Vector2D agentPos) {
    // create a copy of the array so that we can operate on this array
    this.nodes = new ArrayList<Method>(graph.getMethods());
    this.edges = new ArrayList<MethodTransition>(graph.getTransitions());
    this.agentPos = agentPos;
  }
 
  public void execute(Method source) {
    settledNodes = new HashSet<Method>();
    unSettledNodes = new HashSet<Method>();
    distance = new HashMap<Method, DijkstraDistance>();
    predecessors = new HashMap<Method, Method>();
    distance.put(source, new DijkstraDistance(0,0, source.x, source.y));
    unSettledNodes.add(source);
    while (unSettledNodes.size() > 0) {
    	Method node = getMaximumUtility(unSettledNodes);
      settledNodes.add(node);
      unSettledNodes.remove(node);
      findMaximumUtilities(node);
    }
  }

  private void findMaximumUtilities(Method node) {
	long accumulatedDuration = 0;
    List<Method> adjacentNodes = getNeighbors(node);
    for (Method target : adjacentNodes) {
      DijkstraDistance highestUtilityToNode = getHighestUtility(node);
      DijkstraDistance singleStepDistanceFromNodeToTarget = getDistance(node, target, highestUtilityToNode);
      DijkstraDistance currentHighestUtilityFromNodeToTarget = getHighestUtility(target);
      DijkstraDistance newUtilityFromNodeToTargetFromCurrentRoute = singleStepDistanceFromNodeToTarget;//shortestDistanceToNode.Add(singleStepDistanceFromNodeToTarget);
      if (newUtilityFromNodeToTargetFromCurrentRoute.HasGreaterUtility(currentHighestUtilityFromNodeToTarget, target)) {
    	Main.Message(debugFlag, "[DijkstraAlgorithm 55] Adding route " + node.toStringLong() + " to " + target.toStringLong() + " new:" + newUtilityFromNodeToTargetFromCurrentRoute.quality + " old:" + currentHighestUtilityFromNodeToTarget.quality);
        distance.put(target, newUtilityFromNodeToTargetFromCurrentRoute);
        predecessors.put(target, node);
        unSettledNodes.add(target);
      }
    }
  }

  private DijkstraDistance getDistance(Method node, Method target, DijkstraDistance distanceTillPreviousNode) {
    for (MethodTransition edge : edges) {
      if (edge.getSource().equals(node)
          && edge.getDestination().equals(target)) {
        return edge.getPathUtility(distanceTillPreviousNode, agentPos);
      }
    }
    throw new RuntimeException("Should not happen");
  }

  private List<Method> getNeighbors(Method node) {
    List<Method> neighbors = new ArrayList<Method>();
    for (MethodTransition edge : edges) {
      Method source = edge.getSource();
      //if (debugFlag) System.out.println("Checking if edge " + source.label + " belongs to node " + node.label);
      if (source.equals(node)){
    	  Method dest = edge.getDestination();
          if (!isSettled(dest)) {
        	  if (debugFlag) System.out.println("[DijkstraAlgorithm 87] Adding neighbor: " + edge.getSource().label + " to " + dest.label);
        	  neighbors.add(dest);
          }
          else
          {
        	  if (debugFlag) System.out.println("[DijkstraAlgorithm 93] neighbor: " + dest.label + " already added");
    	  }
      }
    }
    return neighbors;
  }

  private Method getMaximumUtility(Set<Method> vertexes) {
	  Method maximum = null;
    for (Method vertex : vertexes) {
      if (maximum == null) {
        maximum = vertex;
      } else {
        if (getHighestUtility(vertex).HasGreaterUtility(getHighestUtility(maximum), vertex)) {
          maximum = vertex;
        }
      }
    }
    return maximum;
  }

  private boolean isSettled(Method vertex) {
    return settledNodes.contains(vertex);
  }

  private DijkstraDistance getHighestUtility(Method destination) {
    DijkstraDistance d = distance.get(destination);
    if (d == null) {
      return new DijkstraDistance(Long.MIN_VALUE,0,destination.x, destination.y);
    } else {
      return d;
    }
  }

  /*
   * This method returns the path from the source to the selected target and
   * NULL if no path exists
   */
  public LinkedList<Method> getPath(Method target) {
    LinkedList<Method> path = new LinkedList<Method>();
    Method step = target;
    // check if a path exists
    if (predecessors.get(step) == null) {
      return null;
    }
    path.add(step);
    Main.Message(debugFlag, "[DijkstraAlgorithm 123] Added to Path " + step.label);
    while (predecessors.get(step) != null) {
      step = predecessors.get(step);
      Main.Message(debugFlag, "[DijkstraAlgorithm 126] Added to Path " + step.label);
      path.add(step);
    }
    // Put it into the correct order
    Collections.reverse(path);
    return path;
  }

} 