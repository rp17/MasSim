package raven.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import masSim.taems.IAgent;
import raven.Main;
import raven.game.Waypoints;
import raven.game.navigation.NavGraphEdge;
import raven.game.navigation.PathEdge;
import raven.goals.Goal;
import raven.goals.GoalComposite;
import raven.goals.Goal_PIDFollowPath;
import raven.goals.GoalRoverThink;
import raven.goals.Goal_PidTraverseEdge;
import raven.goals.Goal_SeekToPosition;
import raven.math.Vector2D;
import raven.math.RandUtils;
import raven.ui.GameCanvas;
import raven.utils.PIDcontroller;

public class RoverBot extends RavenBot {
	//protected GoalRoverThink reason;
	private final double brakingRate = 20; // pixel/sec^2
	private final double accelRate = 30; // pixel/sec^2
	private IAgent agent;
	private double steeringDrift = 0.05;
	private double steeringNoise = 0.01;
	private double distanceNoise = 0.001;
	private double frictCoeff = 0.5;
	private double weight = mass*9.81;
	private double frictForceMag = weight*frictCoeff;
	private double speed = 0;
	private boolean doPID = false;
	protected PIDcontroller pid = new PIDcontroller(0.7f, 0.8f, 0.1f);
	
	public void setAgent(IAgent agent){this.agent = agent;}
	public IAgent getAgent(){return this.agent;}
	public RoverBot(RavenGame world, Vector2D pos, Goal.GoalType mode) {
		super(world, pos, mode);
		steering.wallAvoidanceOff();
		steering.separationOff();
	}
	public void setNoise(double steerDrift, double steerNoise, double distNoise) {
		steeringDrift = steerDrift;
		steeringNoise = steerNoise;
		distanceNoise = distNoise;
	}
	public GoalComposite<RoverBot> addWptsGoal(Waypoints wpts, String methodName){
		if(wpts.size() > 1) {
			List<PathEdge>  m_Path = new ArrayList<PathEdge>();
			Vector2D src = wpts.get(0).pos;
			Vector2D dest = null;
			for(int i=1; i < wpts.size(); i++) {
				Waypoints.Wpt wpt = wpts.get(i);
				dest = wpt.pos;
				PathEdge edge = new PathEdge(src, dest, NavGraphEdge.NORMAL, 0, methodName);
				m_Path.add(edge);
				src = dest;
			}
			Goal_PIDFollowPath g = new Goal_PIDFollowPath(this, m_Path);
			//Goal_SeekToPosition g = new Goal_SeekToPosition(this,new Vector2D(m_Path.get(0).Destination()));
			//brain.AddSubgoal(g);
			brain.ClearAndAddSubgoal(g);
			return g;
		}
		return null;
	}
	
	public void startPid(){doPID = true;}
	public void stopPid(){doPID = false;}
	
	/*
	 * Calculation of Cross Track Error as a difference between desired course and current bearing (in degrees)
	 * 
	 */
	private float getCTE(){
		Vector2D tgt = steering.target();
		if(tgt == null) return 0;
		else {
			float bearing = (float)Math.atan2(tgt.y - position.y, tgt.x - position.x);
			float error = bearing - steering.course;
			error = (float)Math.toDegrees((float)error);
			//System.out.println("Course " + Math.toDegrees(steering.course) + ", bearing " + Math.toDegrees(bearing) + ", error " + error);
			return error;
		}
	}
	
	/**
	 * this method is called from the update method. It calculates and applies
	 * the steering force for this time-step.
	 * delta is in seconds
	 */
	@Override
	protected void updateMovement(double delta) { // delta in seconds
		
		
		// (2do) pid control, acceleration, deceleration depending on doPID value
		
		if(!doPID && speed == 0) return;
		// apply steering noise and drift
		double steerAngle = Math.atan2(velocity.y, velocity.x);
		double steerAngleDeg = Math.toDegrees(steerAngle);
		double noiseSteerAngleDeg = RandUtils.nextGaussian(steerAngleDeg, steeringNoise);
		noiseSteerAngleDeg += steeringDrift;
		
		//System.out.println("Old steer angle: " + steerAngleDeg);
		//System.out.println("New steer angle: " + noiseSteerAngleDeg);
		
		if (doPID) {
			if(speed < maxSpeed*1.0) {
				speed += accelRate*delta;
				//System.out.println("Accelerating to " + speed);
				}
			
			float error = getCTE();
			float out = pid.pidCycle(error, (float)delta);
			float turnRate = out*2.0f; //  deg/sec
			noiseSteerAngleDeg += turnRate*delta;
		}
		else {
			//System.out.println("No PID");
			if( speed > 1){speed -= brakingRate*delta;}
			else {
				speed = 0;
				return;
			}
		}
		double noiseSteerAngle = Math.toRadians(noiseSteerAngleDeg);
		heading.x = Math.cos(noiseSteerAngle);
		heading.y = Math.sin(noiseSteerAngle);
		side = heading.perp();
		//double velX = Math.cos(noiseSteerAngle)*speed;
		//double velY = Math.sin(noiseSteerAngle)*speed;
		
		double velX = heading.x*speed;
		double velY = heading.y*speed;
		
		// calculate delta distance due to distance noise
		double distNoise = RandUtils.nextGaussian(0, distanceNoise);
		//double distNoiseX = distNoise*Math.cos(noiseSteerAngle);
		//double distNoiseY = distNoise*Math.sin(noiseSteerAngle);
				
		velocity.x = velX;
		velocity.y = velY;
		position.x += velX*delta + distNoise*heading.x;
		position.y += velY*delta + distNoise*heading.y;
		//TODO see if this is really needed or not. this.agent.setPosition(position);
		//if the vehicle has a non zero velocity the heading and side vectors must 
		//be updated
		/*
		if (speed > 1)
		{    
			heading.x = velocity.x;
			heading.y = velocity.y;
			heading.normalize();
			side = heading.perp();
		}
		*/
	}
	
	/*
	@Override
	protected void updateMovement(double delta) {
		//calculate the combined steering force
		Vector2D forceIni = steering.calculate();

		double steerAngle = Math.atan2(forceIni.y, forceIni.x);
		double steerAngleDeg = Math.toDegrees(steerAngle);
		double noiseSteerAngleDeg = RandUtils.nextGaussian(steerAngleDeg, steeringNoise);
		noiseSteerAngleDeg += steeringDrift;
		double noiseSteerAngle = Math.toRadians(noiseSteerAngleDeg);
		System.out.println("Old steer angle: " + steerAngleDeg);
		System.out.println("New steer angle: " + noiseSteerAngleDeg);
		double forceIniLength = forceIni.length();
		Vector2D force = new Vector2D(Math.cos(noiseSteerAngle)*forceIniLength, Math.sin(noiseSteerAngle)*forceIniLength);
		//force.normalize();
		System.out.println("Old steer length: " + forceIniLength);
		System.out.println();
		//force.mul(forceIni.length());
		System.out.println("Old steer: x = " + forceIni.x + ", y = " + forceIni.y);
		System.out.println("New steer: x = " + force.x + ", y = " + force.y);
		System.out.println("\n");
		//if no steering force is produced decelerate the player by applying a
		//braking force
		if (steering.force().isZero())
		{
			final double BrakingRate = 0.8; 

			velocity = velocity.mul(BrakingRate);                                     
		}
		
		//calculate the acceleration
		Vector2D revForce = force.getReverse();
		revForce.normalize();
		revForce.mul(frictForceMag);
		force = force.sub(revForce);
		Vector2D accel = force.div(mass);

		//update the velocity
		velocity = velocity.add(accel.mul(delta));
		//velocity = velocity.add(accel);
		
		//make sure vehicle does not exceed maximum velocity per second
		velocity.truncate(maxSpeed * delta);
		double dist = velocity.length();
		dist = RandUtils.nextGaussian(dist, distanceNoise);
		velocity.normalize();
		velocity.mul(dist);

		//update the position
		position = position.add(velocity);

		//if the vehicle has a non zero velocity the heading and side vectors must 
		//be updated
		if (!velocity.isZero())
		{    
			heading = new Vector2D(velocity);
			heading.normalize();

			side = heading.perp();
		}
	
	}
	*/
	
	
}
