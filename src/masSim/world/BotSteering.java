package masSim.world;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import raven.game.RavenSteering.SummingMethod;
import raven.game.interfaces.IBot;
import raven.math.Geometry;
import raven.math.Transformations;
import raven.math.Vector2D;
import raven.script.RavenScript;

public class BotSteering {
	
	private enum BehaviorType{
		NONE(1), 
		SEEK(2),
		ARRIVE(4),
		WANDER(8),
		SEPARATION(16),
		WALL_AVOIDANCE(32);

		private int value;
		private BehaviorType(int i) {value = i;}
		public int getValue() {return value;}
	};
	/** Arrive makes use of these to determine how quickly a Raven_Bot should
	 * decelerate to its target */
	private enum Deceleration {
		FAST(0),
		NORMAL(1),
		SLOW(2);
		
		private int value;
		private Deceleration(int i) {value = i;}
		public int getValue() {return value;}
	};
	private SimBot bot;
	
	/** the steering force created by the combined effect of all the selected
	 * behaviors */
	private Vector2D steeringForce;

	/** these can be used to keep track of friends, pursuers, or prey */
	private SimBot targetAgent1;
	
	/** the current target */
	private Vector2D target;
	float course = 0;

	/** multipliers. These can be adjusted to effect strength of the
	 * appropriate behavior. */
	private double        weightSeparation;
	private double        weightWander;
	private double        weightWallAvoidance;
	private double        weightSeek;
	private double        weightArrive;

	/** binary flags to indicate whether or not a behavior should be active */
	private int flags;
	
	private BehaviorType behaviorType;
	private Deceleration deceleration;
	
	public BotSteering(SimBot bot) {
		this.bot = bot;

		flags						= 0;
		weightSeparation			= RavenScript.getDouble("SeparationWeight");
		weightWander				= RavenScript.getDouble("WanderWeight");
		weightWallAvoidance			= RavenScript.getDouble("WallAvoidanceWeight");
		//viewDistance				= RavenScript.getDouble("ViewDistance");
		//wallDetectionFeelerLength	= RavenScript.getDouble("WallDetectionFeelerLength");
		steeringForce				= new Vector2D();
		//feelers						= new Vector<Vector2D>(3);
		deceleration				= Deceleration.NORMAL;
		targetAgent1				= null;
		//wanderDistance				= wanderDist;
		//wanderJitter				= wanderJitterPerSec;
		//wanderRadius				= wanderRad;
		weightSeek					= RavenScript.getDouble("SeekWeight");
		weightArrive				= RavenScript.getDouble("ArriveWeight");
		//cellSpaceOn					= false;
		//summingMethod				= SummingMethod.PRIORITIZED;

		//stuff for the wander behavior
		//double theta = Math.random() * (2* Math.PI);

		//create a vector to a target position on the wander circle
		//wanderTarget = new Vector2D(wanderRadius * Math.cos(theta), wanderRadius * Math.sin(theta));
		
		// These defaults were put int as assumptions.  TODO: Validate my assumptions.
		//cellSpaceOn = false;
		behaviorType = BehaviorType.NONE;
		
	}
	/** this function tests if a specific bit of flags is set */
	private boolean On(BehaviorType bt) {
		return (flags & bt.getValue()) == bt.getValue();
	}
	
	/**
	 * Handles the max speed of the bot.
	 * @param runningTot how fast the bot is going so far.
	 * @param forceToAdd how much velocity to add.
	 * @return A true if force was added successfully to the bot, false if bot is going max speed.
	 */
	public boolean accumulateForce(Vector2D runningTot, Vector2D forceToAdd) {
		//calculate how much steering force the vehicle has used so far
		double magnitudeSoFar = runningTot.length();

		//calculate how much steering force remains to be used by this vehicle
		double magnitudeRemaining = bot.maxForce() - magnitudeSoFar;

		//return false if there is no more force left to use
		if (magnitudeRemaining <= 0.0)
			return false;

		//calculate the magnitude of the force we want to add
		double magnitudeToAdd = forceToAdd.length();

		//if the magnitude of the sum of ForceToAdd and the running total
		//does not exceed the maximum force available to this vehicle, just
		//add together. Otherwise add as much of the ForceToAdd vector is
		//possible without going over the max.
		if (magnitudeToAdd < magnitudeRemaining) {
			runningTot.setValue(runningTot.add(forceToAdd));
		} else {
			magnitudeToAdd = magnitudeRemaining;

			//add it to the steering force
			forceToAdd.normalize();
			// Dirty hack due to the way it was ported.
			runningTot.setValue(runningTot.add(forceToAdd.mul(magnitudeToAdd))); 
		}

		return true;
	}
	/* 
	 * BEGIN BEHAVIOR DECLARATIONS
	 */


	/** this behavior moves the agent towards a target position */
	private Vector2D seek(final Vector2D target) {

		//Vector2D desiredVelocity = target.sub(ravenBot.pos());
		//desiredVelocity.normalize();
		//desiredVelocity = desiredVelocity.mul(ravenBot.maxForce());

		Vector2D desiredVelocity = target.sub(bot.pos());
		double ratio = bot.maxForce()/desiredVelocity.length();
		desiredVelocity = desiredVelocity.mul(ratio);
		//desiredVelocity = desiredVelocity.sub(ravenBot.velocity());
		//System.out.println("seek(): going to x = " + target.x + ", y = " + target.y);
		//System.out.println("seek(): force vec: x = " + desiredVelocity.x + ", y = " + desiredVelocity.y);
		return desiredVelocity;

	}

	/** this behavior moves the agent towards a target position */
	private Vector2D seekPID(final Vector2D target) {

		//Vector2D desiredVelocity = target.sub(ravenBot.pos());
		//desiredVelocity.normalize();
		//desiredVelocity = desiredVelocity.mul(ravenBot.maxForce());

		Vector2D desiredVelocity = target.sub(bot.pos());
		double ratio = bot.maxForce()/desiredVelocity.length();
		desiredVelocity = desiredVelocity.mul(ratio);
		//desiredVelocity = desiredVelocity.sub(ravenBot.velocity());
		System.out.println("seek(): going to x = " + target.x + ", y = " + target.y);
		System.out.println("seek(): force vec: x = " + desiredVelocity.x + ", y = " + desiredVelocity.y);
		return desiredVelocity;

	}
	
	
	/** this behavior is similar to seek but it attempts to arrive at the
	 * target with a zero velocity */
	private Vector2D arrive(final Vector2D target, final Deceleration deceleration){
		Vector2D toTarget = target.sub(bot.pos());

		//calculate the distance to the target
		double dist = toTarget.length();

		if (dist < 1.0) return new Vector2D(0,0);
		else
		{
			//because Deceleration is enumerated as an int, this value is required
			//to provide fine tweaking of the deceleration..
			final double DecelerationTweaker = 0.3;

			//calculate the speed required to reach the target given the desired
			//deceleration
			//   double speed =  dist / (deceleration* decelerationTweaker);     
			double speed= target.distance(bot.pos())/ (Double.valueOf(deceleration.getValue())*DecelerationTweaker);
			//make sure the velocity does not exceed the max
			speed = Math.min(speed, bot.maxSpeed());


			//from here proceed just like Seek except we don't need to normalize 
			//the ToTarget vector because we have already gone to the trouble
			//of calculating its length: dist. 
			Vector2D DesiredVelocity =  toTarget.mul(speed / dist);

			return (DesiredVelocity.sub(bot.velocity()));
		}

		//return new Vector2D(0,0);

	}
	
	public void seekOn() { flags |= BehaviorType.SEEK.getValue(); }
	public void arriveOn() { flags |= BehaviorType.ARRIVE.getValue(); }
	
	public void seekOff() { if(On(BehaviorType.SEEK)) flags ^= BehaviorType.SEEK.getValue(); }
	public void arriveOff() { if(On(BehaviorType.ARRIVE)) flags ^= BehaviorType.ARRIVE.getValue(); }
	
	public boolean seekIsOn() { return On(BehaviorType.SEEK); }
	public boolean arriveIsOn() { return On(BehaviorType.ARRIVE); }
	
	
	public void setTarget(Vector2D t) { target = t; }
	public void setTarget(Vector2D t, float course) { target = t; this.course = course;}
	public final Vector2D target() { return target; }
}
