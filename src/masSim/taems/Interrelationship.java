package masSim.taems;

public class Interrelationship extends Element {
	public Method from;
	public Method to;
	public Outcome from_outcome;
	
	public Interrelationship(Method from, Method to, Outcome from_outcome)
	{
		this.from = from;
		this.to = to;
		this.from_outcome = from_outcome;
	}
}
