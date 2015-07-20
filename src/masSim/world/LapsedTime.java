package masSim.world;

public class LapsedTime {

	public static long getStart() {
		return System.currentTimeMillis();
	}

	public static long getLapsed(long startTime) {
		return (System.currentTimeMillis() - startTime);

	}
}


