package masSim.world;

//import java.util.ArrayList;

import java.util.ArrayList;
import java.util.List;

import masSim.goals.Goal;
import masSim.goals.GoalComposite;
import masSim.goals.GoalThink;
//import masSim.goals.GoalRoverThink;
import masSim.goals.Goal_PIDFollowPath;
//import raven.game.RoverBot;
import raven.game.Waypoints;
import raven.game.interfaces.IBot;
import raven.game.navigation.NavGraphEdge;
import raven.game.navigation.PathEdge;
import raven.math.RandUtils;
import raven.math.Vector2D;
import raven.script.RavenScript;
import raven.utils.PIDcontroller;
import masSim.world.MqttMessagingProvider;
import masSim.taems.IAgent;

public class SimBot implements IBot{
	protected String name;
	protected Vector2D position;
	//protected MqttMessagingProvider mq;
	
	protected Vector2D velocity;
	
	/** a normalized vector pointing in the direction the entity is heading */
	protected Vector2D heading;
	
	/** a vector perpendicular to the heading vector */
	protected Vector2D side;
	
	protected double mass;
	
	/** the maximum speed this entity may travel at */
	protected double maxSpeed;
	
	/** the maximum force this entity can produce to power itself (think
	 * rockets and thrust) */
	protected double maxForce;
	
	/** the maximum rate (radians per second)this vehicle can rotate */
	protected double maxTurnRate;
	
	private final double brakingRate = 20; // pixel/sec^2
	private final double accelRate = 30; // pixel/sec^2
	
	private double steeringDrift = 0.05;
	private double steeringNoise = 0.01;
	private double distanceNoise = 0.001;
	private double frictCoeff = 0.5;
	private double weight = mass*9.81;
	private double frictForceMag = weight*frictCoeff;
	private double speed = 0;
	private boolean doPID = false;
	
	private IAgent agent;
	protected PIDcontroller pid = new PIDcontroller(0.7f, 0.8f, 0.1f);
	// create the steering behavior class
	private BotSteering steering = new BotSteering(this);
	/**
	 * this object handles the arbitration and processing of high level goals
	 */
	protected GoalThink brain;
	
	
	public SimBot(IAgent agent, Vector2D position, Goal.GoalType mode) {
		this.agent = agent;
		name = agent.getName();
		this.position = position;
		velocity = new Vector2D(0,0);
		maxSpeed = RavenScript.getDouble("Bot_MaxSpeed");
		heading = new Vector2D(1, 0);
		mass = RavenScript.getDouble("Bot_Mass");
		maxTurnRate = RavenScript.getDouble("Bot_MaxHeadTurnRate");
		maxForce = RavenScript.getDouble("Bot_MaxForce");
		
		// create the goal queue
		brain = new GoalThink(this, mode);
	}
	
	public GoalComposite addWptsGoal(Waypoints wpts, String methodName){
		System.out.println("SimBot.addWptsGoal " + name + " assigned wpt " + methodName);
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
			System.out.println("SimBot.addWptsGoal " + name + " assigned Goal_PIDFollowPath " + methodName);
			return g;
		}
			System.out.println("SimBot.addWptsGoal " + name + " received waypoint list with size " + wpts.size() + " ! Should be > 1");
			return null;
	}
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
	
	public void update(double delta) {
		if(brain == null) {
			System.out.println("SimBot: brain is null");
		}
		brain.process(delta);
		// Calculate the steering force and update the bot's velocity and
		// position
		updateMovement(delta);
	}
	
	/**
	 * this method is called from the update method. It calculates and applies
	 * the steering force for this time-step.
	 * delta is in seconds
	 */
	
	protected void updateMovement(double delta) { // delta in seconds
		
		//System.out.println("Bot " + getName() + " update movement, delta = " + delta + " secs");
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
	
	/**
	 * returns a value indicating the time in seconds it will take the bot to
	 * reach the given position at its current speed.
	 * 
	 * @param pos
	 *            position to reach
	 * @return seconds until arrival
	 */
	public double calculateTimeToReachPosition(Vector2D pos) {
		return position.distance(pos) / maxSpeed;
	}
	
	public void startPid(){doPID = true;}
	public void stopPid(){doPID = false;}
	public double maxForce() {return maxForce;}
	public double maxSpeed() {return maxSpeed;}
	public Vector2D pos() { return position; }
	public Vector2D velocity() { return velocity; }
	public void setPos(Vector2D pos) { position = pos; }
	public BotSteering getSteering() {return steering;}
	//public void setAgent(IAgent agent){this.agent = agent;}
	public IAgent getAgent(){return this.agent;}
	public String getName() {return name;}
	
}
