package masSim.taems;

public class Interrelationship extends Element {
	Task from;
	Task to;
	Outcome from_outcome;
	
	public Interrelationship(Task from, Task to, Outcome from_outcome)
	{
		this.from = from;
		this.to = to;
		this.from_outcome = from_outcome;
	}
}
