package raven.game.interfaces;

public interface INetNode extends IRavenBot {
	public double getRadius();
	public String getName();
	public void addNeighbor(INetNode ag);
	public void removeNeighbor(INetNode ag);
	public INetNode peekClosest();
	public INetNode pollClosest();
	//public INetNode[] getSortedNeighbors();
}
