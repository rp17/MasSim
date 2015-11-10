package raven;

public class MeasureTime {
	private long startTime;
	private long total = 0;
	boolean isStarted = false;
	
	public static MeasureTime Timer = new MeasureTime();
	
	public long GetTotal()//microseconds
	{
		return total / 1000;
	}
	
	public void Resume()
	{
		if (isStarted) {
			System.out.println("Timer Error");
			System.exit(0);
		}
		startTime = System.nanoTime();
		isStarted = true;
	}
	
	public void Stop()
	{
		if (!isStarted) {
			System.out.println("Timer Stop Error");
			System.exit(0);
		}
		total += System.nanoTime() - startTime;
		isStarted = false;
	}
	
	public void Reset()
	{
		total = 0;
	}
	
	public void Start()
	{
		if (isStarted) {
			System.out.println("Timer Start Error");
			System.exit(0);
		}
		isStarted = true;
		startTime = System.nanoTime();
	}
	
}
