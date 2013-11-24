package raven.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import raven.game.Waypoints;
import raven.game.navigation.NavGraphEdge;
import raven.game.navigation.PathEdge;
import raven.goals.Goal;
import raven.goals.Goal_FollowPath;
import raven.goals.GoalRoverThink;
import raven.math.Vector2D;
import raven.math.RandUtils;
import raven.ui.GameCanvas;

public class RoverBot extends RavenBot {
	//protected GoalRoverThink reason;
	private double steeringDrift = 0.01;
	private double steeringNoise = 0.01;
	private double distanceNoise = 0.001;
	private double frictCoeff = 0.5;
	private double weight = mass*9.81;
	private double frictForceMag = weight*frictCoeff;
	private double speed = 50;
	
	
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
	public void addWptsGoal(Waypoints wpts){
		if(wpts.size() > 1) {
			List<PathEdge>  m_Path = new ArrayList<PathEdge>();
			Vector2D src = wpts.get(0).pos;
			Vector2D dest = null;
			for(int i=1; i < wpts.size(); i++) {
				Waypoints.Wpt wpt = wpts.get(i);
				dest = wpt.pos;
				PathEdge edge = new PathEdge(src, dest, NavGraphEdge.NORMAL, 0);
				m_Path.add(edge);
				System.out.println("Edge " + i + " src: " + src.toString() + " dest: " + dest.toString());
				src = dest;
			}
			brain.AddSubgoal(new Goal_FollowPath(this, m_Path));
		}
	}
	
	/**
	 * this method is called from the update method. It calculates and applies
	 * the steering force for this time-step.
	 */
	
	@Override
	protected void updateMovement(double delta) {
		double steerAngle = Math.atan2(heading.y, heading.x);
		double steerAngleDeg = Math.toDegrees(steerAngle);
		double noiseSteerAngleDeg = RandUtils.nextGaussian(steerAngleDeg, steeringNoise);
		noiseSteerAngleDeg += steeringDrift;
		double noiseSteerAngle = Math.toRadians(noiseSteerAngleDeg);
		System.out.println("Old steer angle: " + steerAngleDeg);
		System.out.println("New steer angle: " + noiseSteerAngleDeg);
		double velX = Math.cos(noiseSteerAngle)*speed;
		double velY = Math.sin(noiseSteerAngle)*speed;
		
		double distNoise = RandUtils.nextGaussian(0, distanceNoise);
		double distNoiseX = distNoise*Math.cos(noiseSteerAngle);
		double distNoiseY = distNoise*Math.sin(noiseSteerAngle);
		if (steering.force().isZero())
		{
			final double BrakingRate = 0.8; 
			velX *= BrakingRate;
			velY *= BrakingRate;
		}
		Vector2D vel = new Vector2D(velX, velY);
		velocity = vel;
		position.x += (velX + distNoiseX)*delta;
		position.y += (velY + distNoiseY)*delta;
		
		//if the vehicle has a non zero velocity the heading and side vectors must 
		//be updated
		if (!velocity.isZero())
		{    
			heading = new Vector2D(velocity);
			heading.normalize();
			side = heading.perp();
		}
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
