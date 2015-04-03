package masSim.taems;

import raven.math.Vector2D;

public interface IMethod {
	DijkstraDistance getPathUtilityRepresentedAsDistance(DijkstraDistance distanceTillPreviousNode, Vector2D agentPos);
	void AddInterrelationship(Interrelationship relationship);
	
}
