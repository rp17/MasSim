package raven.utils;

public class Regulator {

	/** updatePeriod in seconds */
	private double updatePeriod = 0.0;
	
	/** time remaining until next update */
	private double updatesPerSecondRequested;
	private double nextUpdateTime;
	private double itsDeltaSec;
	private long lastTime;
	private boolean firstRun = true;
	
	public Regulator(double updatesPerSecondRequested) {
		// The original implementation had it randomly wait 1 second too
		this.updatesPerSecondRequested = updatesPerSecondRequested;
		nextUpdateTime = Math.random();
		itsDeltaSec = 1/updatesPerSecondRequested;
		if (updatesPerSecondRequested > 0) {
			updatePeriod = 1 / updatesPerSecondRequested;
		} else if (updatesPerSecondRequested < 0) {
			updatePeriod = -1;
		}
		lastTime = System.currentTimeMillis();
	}
	
	public void update(double delta) {
		nextUpdateTime -= delta;
	}
	public double getItsDelta(){return updatePeriod;}
	public boolean isReady() {
		// if a regulator is instantiated with a zero freq then it goes into
		// stealth mode (doesn't regulate)
		if (updatePeriod == 0.0) {
			return true;
		}
		
		// if the regulator is instantiated with a negative freq then it will
		// never allow the code to flow
		if (updatePeriod < 0.0) {
			return false;
		}
		
		// the number of milliseconds the update period can vary per required
		// update-step. This is here to make sure any multiple clients of this
		// class have their updates spread evenly
		final double updatePeriodVariator = 0.010; // 10 mS
		
		if (nextUpdateTime <= 0) {
			// Offset is randomly between -1.0 and 1.0
			//double offset = Math.random() * 2.0 - 1.0;
			//nextUpdateTime = updatePeriod + offset * updatePeriodVariator;
			nextUpdateTime = updatePeriod;
			long thisTime = System.currentTimeMillis();
			if(firstRun) {
				itsDeltaSec = updatePeriod;
				firstRun = false;
			}
			else {
				itsDeltaSec = ((double)(thisTime - lastTime))*1.0e-03;
			}
			lastTime = thisTime;
			return true;
		}
		
		return false;
	}
}
