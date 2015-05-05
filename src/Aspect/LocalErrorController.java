package Aspect;

public class LocalErrorController {

	private static volatile int numberOfInjectedErrors = 0;
	
	public static void setInjectedErrors(int totalInjectedErrors) {
		numberOfInjectedErrors = totalInjectedErrors;
	}
	
	public static boolean hasInjectedErrors() {
		return (numberOfInjectedErrors > 0);
	}
	
	public static synchronized boolean getInjectedErrors(){
		if ( numberOfInjectedErrors > 0 ) {
			numberOfInjectedErrors--;
			return true;
		} else {
			return false;
		}
	}
	
}
