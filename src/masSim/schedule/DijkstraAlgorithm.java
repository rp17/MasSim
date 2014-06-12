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

public class DijkstraAlgorithm {

  private final List<Method> nodes;
  private final List<MethodTransition> edges;
  private Set<Method> settledNodes;
  private Set<Method> unSettledNodes;
  private Map<Method, Method> predecessors;
  private Map<Method, DijkstraDistance> distance;

  public DijkstraAlgorithm(Graph graph) {
    // create a copy of the array so that we can operate on this array
    this.nodes = new ArrayList<Method>(graph.getMethods());
    this.edges = new ArrayList<MethodTransition>(graph.getTransitions());
  }
 
  public void execute(Method source) {
    settledNodes = new HashSet<Method>();
    unSettledNodes = new HashSet<Method>();
    distance = new HashMap<Method, DijkstraDistance>();
    predecessors = new HashMap<Method, Method>();
    distance.put(source, new DijkstraDistance(0,0));
    unSettledNodes.add(source);
    while (unSettledNodes.size() > 0) {
    	Method node = getMinimum(unSettledNodes);
      settledNodes.add(node);
      unSettledNodes.remove(node);
      findMinimalDistances(node);
    }
  }

  private void findMinimalDistances(Method node) {
	long accumulatedDuration = 0;
    List<Method> adjacentNodes = getNeighbors(node);
    for (Method target : adjacentNodes) {
      DijkstraDistance shortestDistanceToNode = getShortestDistance(node);
      DijkstraDistance singleStepDistanceFromNodeToTarget = getDistance(node, target, shortestDistanceToNode);
      DijkstraDistance currentShortestDistanceFromNodeToTarget = getShortestDistance(target);
      DijkstraDistance newDistanceFromNodeToTargetFromCurrentRoute = shortestDistanceToNode.Add(singleStepDistanceFromNodeToTarget);
      if (currentShortestDistanceFromNodeToTarget.IsGreaterThen(newDistanceFromNodeToTargetFromCurrentRoute)) {
        distance.put(target, newDistanceFromNodeToTargetFromCurrentRoute);
        predecessors.put(target, node);
        unSettledNodes.add(target);
      }
    }
  }

  private DijkstraDistance getDistance(Method node, Method target, DijkstraDistance distanceTillPreviousNode) {
    for (MethodTransition edge : edges) {
      if (edge.getSource().equals(node)
          && edge.getDestination().equals(target)) {
        return edge.getPathUtility(distanceTillPreviousNode);
      }
    }
    throw new RuntimeException("Should not happen");
  }

  private List<Method> getNeighbors(Method node) {
    List<Method> neighbors = new ArrayList<Method>();
    for (MethodTransition edge : edges) {
      if (edge.getSource().equals(node)
          && !isSettled(edge.getDestination())) {
        neighbors.add(edge.getDestination());
      }
    }
    return neighbors;
  }

  private Method getMinimum(Set<Method> vertexes) {
	  Method minimum = null;
    for (Method vertex : vertexes) {
      if (minimum == null) {
        minimum = vertex;
      } else {
        if (getShortestDistance(minimum).IsGreaterThen(getShortestDistance(vertex))) {
          minimum = vertex;
        }
      }
    }
    return minimum;
  }

  private boolean isSettled(Method vertex) {
    return settledNodes.contains(vertex);
  }

  private DijkstraDistance getShortestDistance(Method destination) {
    DijkstraDistance d = distance.get(destination);
    if (d == null) {
      return new DijkstraDistance(Long.MAX_VALUE,0);
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
    while (predecessors.get(step) != null) {
      step = predecessors.get(step);
      path.add(step);
    }
    // Put it into the correct order
    Collections.reverse(path);
    return path;
  }

} 