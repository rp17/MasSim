package masSim.taems;

public interface IMethod {
	DijkstraDistance getPathUtilityRepresentedAsDistance(DijkstraDistance distanceTillPreviousNode);
	void AddInterrelationship(Interrelationship relationship);
	
}
