package Aspect;

public class SurgeCreator {
	
	private static volatile int surgeCount = 0;
	
	public static void assignSurge(int injectedEvents) {
		surgeCount = injectedEvents;
	}
	
	public static int getSurgeNumber() {
		return surgeCount;
	}
	
	public static boolean createSurge(){
		
		if ( surgeCount >0) {
			return true;
		} else {
			return false;
		}
		
	}

}
