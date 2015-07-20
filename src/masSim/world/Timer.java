package masSim.world;

public class Timer {

	public long getStart() {
		return System.currentTimeMillis();
	}

	public long getLapsed(long startTime) {
		return (System.currentTimeMillis() - startTime)*1000;

	}
}
