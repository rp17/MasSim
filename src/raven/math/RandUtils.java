/**
 * 
 */
package raven.math;

import java.util.Random;

/**
 * @author chester
 *
 */
public class RandUtils {
	private static Random rand = new Random();
	/**
	 * Generates a random double from the start to the end provided, exclusive.
	 * @param start
	 * @param end
	 * @return
	 */
	public static double RandInRange(double start, double end)
	{
		return Math.random() * (end - start) + start;
	}
	
	public static double nextGaussian(double mean, double deviation) {
		double res = rand.nextGaussian()*deviation + mean;
		return res;
	}
}
