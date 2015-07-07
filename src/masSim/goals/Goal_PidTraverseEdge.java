package masSim.goals;

import masSim.schedule.SchedulingCommandType;
import masSim.schedule.SchedulingEvent;
import masSim.schedule.SchedulingEventParams;
import masSim.world.MqttMessagingProvider;
import masSim.world.SimBot;
import raven.game.navigation.NavGraphEdge;
import raven.game.navigation.PathEdge;
import raven.script.RavenScript;
import raven.math.Vector2D;
import raven.Main;
import raven.TaskIssuer;

public class Goal_PidTraverseEdge extends GoalComposite {
	private static double distTolerance = 350.0;
	//the edge the bot will follow
	PathEdge  m_Edge;

	private MqttMessagingProvider mq;
	
	Vector2D dest;
	Vector2D source;

	//true if m_Edge is the last in the path.
	boolean      m_bLastEdgeInPath;

	//the estimated time the bot should take to traverse the edge
	double     m_dTimeExpected;

	//this records the time this goal was activated
	double     elapsedTime;
	
	public Goal_PidTraverseEdge(SimBot simBot, PathEdge edge, boolean lastedgeinpath) {

		// Goal<Raven_Bot>(pBot, goal_traverse_edge),
		super(simBot, Goal.GoalType.goal_traverse_edge);
		m_Edge = edge;
		m_dTimeExpected = 0.0;
		m_bLastEdgeInPath = lastedgeinpath;
		mq = MqttMessagingProvider.GetMqttProvider();
	}

	public void activate() { 
		
		
		m_iStatus = Goal.CurrentStatus.active;

		//record the time the bot starts this goal
		elapsedTime = 0;   

		//calculate the expected time required to reach the this waypoint. This value
		//is used to determine if the bot becomes stuck 
		m_dTimeExpected = m_pOwner.calculateTimeToReachPosition(m_Edge.Destination());

		//factor in a margin of error for any reactive behavior
		double MarginOfError = 2.0;

		m_dTimeExpected += MarginOfError;


		//set the steering target
		dest = m_Edge.Destination();
		source = m_Edge.Source();
		float course = (float)Math.atan2(dest.y - source.y, dest.x - source.x);
		m_pOwner.getSteering().setTarget(dest, course);
		
		//System.out.println("Steering to: " + dest.toString() + " course = " + course);
		
	}

	@Override
	public Goal.CurrentStatus process(double delta){
		//if status is inactive, call Activate()
		activateIfInactive();

		//if the bot has become stuck return failure
		/*
		elapsedTime += delta;
		if (isStuck())
		{
			m_iStatus = Goal.CurrentStatus.failed;
		}
		 */
		//if the bot has reached the end of the edge return completed
		//else { 
		/*
			if (m_pOwner.isAtPosition(m_Edge.Destination())) {
				m_iStatus = Goal.CurrentStatus.completed;
			}
		*/
		if(m_iStatus != Goal.CurrentStatus.active) return m_iStatus;
		//System.out.println("Going to x = " + m_Edge.Destination().x + " y = " + m_Edge.Destination().y);
		double dist = m_pOwner.pos().distanceSq(m_Edge.Destination());
		//System.out.println("Distance from destination: " + dist);
			if (m_pOwner.pos().distanceSq(m_Edge.Destination()) < distTolerance) {
				m_iStatus = Goal.CurrentStatus.completed;
				Main.Message(this, true, ": agent " + this.m_pOwner.getName() + "  METHODCOMPLETED: " + m_Edge.MethodRepresentedByEdge() + " , coords: " + m_pOwner.pos());
				
				// is AddAgentId taking the destination agent ? can taskIssuer be an agent ?
				SchedulingEventParams params = new SchedulingEventParams()
				.AddTaskName(m_Edge.MethodRepresentedByEdge())
				.AddAgentId(TaskIssuer.TaskIssuerName)
				.AddOriginatingAgent(this.m_pOwner.getName());
				
				SchedulingEvent event = new SchedulingEvent(TaskIssuer.TaskIssuerName, SchedulingCommandType.METHODCOMPLETED, params);
				//SchedulingEventParams params = new SchedulingEventParams(this.m_pOwner.getName(), m_Edge.MethodRepresentedByEdge(), "0", "0", "");
				// what are "0", "0", "" for ? dummy vals for coordinates ?
				
				//SchedulingEventParams params = new SchedulingEventParams(this.m_pOwner.getName(), m_Edge.MethodRepresentedByEdge(), "0", "0", "");
				//SchedulingEvent event = new SchedulingEvent(this.m_pOwner.getName(), SchedulingCommandType.METHODCOMPLETED, params);
				m_pOwner.getAgent().MarkMethodCompleted(m_Edge.MethodRepresentedByEdge());
				
				//SchedulingEvent event = new SchedulingEvent(TaskIssuer.TaskIssuerName, SchedulingCommandType.METHODCOMPLETED, params);
				LaunchedByMasSim = false;
				/*
				if(mq == null) {
					Main.Message(this, true, ": mq provider is null, cannot publish METHODCOMPLETED event");
				}
				else {
					//mq.asyncPublishMessage(event);
				}
				*/
			}
		//}
		return m_iStatus;
	}
	
	@Override
	public void terminate(){
		//return max speed back to normal
		//m_pOwner.setMaxSpeed(RavenScript.getDouble("Bot_MaxSpeed"));
		
		// set goal status to completed.
		m_iStatus = Goal.CurrentStatus.completed;

	}


}
