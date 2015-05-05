package masSim.taems;

public interface IObserver {
	public void NotifyAll();
	public void Update(Node observedTask);
	public void AddObserver(Node observer);
}
