package masSim.taems;

public class Interrelationship extends Element {
	public Node from;
	public Node to;
	public Outcome from_outcome;
	
	public Interrelationship(Node from, Node to, Outcome from_outcome)
	{
		this.from = from;
		this.to = to;
		this.from_outcome = from_outcome;
	}
}
