package raven;

public class MeasureTime {
	private long startTime;
	private long total = 0;
	boolean isStarted = false;
	
	
	public static MeasureTime Timer1 = new MeasureTime();
	public static MeasureTime Timer2 = new MeasureTime();
	
	public long GetTotal()//microseconds
	{
		return total / 1000;
	}
	
	public void Resume()
	{
		startTime = System.nanoTime();
	}
	
	public void Stop()
	{
		isStarted = false;
		total += System.nanoTime() - startTime;
	}
	
	public void Reset()
	{
		total = 0;
	}
	
	public void Start()
	{
		isStarted = true;
		startTime = System.nanoTime();
	}
	
}
