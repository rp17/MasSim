package raven.goals;

import masSim.schedule.SchedulingCommandType;
import masSim.schedule.SchedulingEvent;
import masSim.schedule.SchedulingEventParams;
import masSim.world.MqttMessagingProvider;
import raven.Main;
import raven.game.RavenBot;
import raven.game.RoverBot;
import raven.game.navigation.NavGraphEdge;
import raven.game.navigation.PathEdge;
import raven.script.RavenScript;
import raven.ui.GameCanvas;
import raven.math.Vector2D;

public class Goal_PidTraverseEdge extends GoalComposite<RoverBot> {
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
	
	public Goal_PidTraverseEdge(RoverBot roverBot, PathEdge edge, boolean lastedgeinpath) {

		// Goal<Raven_Bot>(pBot, goal_traverse_edge),
		super(roverBot, Goal.GoalType.goal_traverse_edge);
		m_Edge = edge;
		m_dTimeExpected = 0.0;
		m_bLastEdgeInPath = lastedgeinpath;
		mq = MqttMessagingProvider.GetMqttProvider();
	}
	
	@Override
	public void activate() {
		m_iStatus = Goal.CurrentStatus.active;

		//the edge behavior flag may specify a type of movement that necessitates a 
		//change in the bot's max possible speed as it follows this edge
		switch(m_Edge.Behavior()) {
			case NavGraphEdge.SWIM:
				m_pOwner.setMaxSpeed(RavenScript.getDouble("Bot_MaxSwimmingSpeed"));
				break;
			case NavGraphEdge.CRAWL:
				m_pOwner.setMaxSpeed(RavenScript.getDouble("Bot_MaxCrawlingSpeed"));
				break;
		}


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
	public raven.goals.Goal.CurrentStatus process(double delta){
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
				SchedulingEventParams params = new SchedulingEventParams(this.m_pOwner.getName(), m_Edge.MethodRepresentedByEdge(), "0", "0", "");
				SchedulingEvent event = new SchedulingEvent(this.m_pOwner.getName(), SchedulingCommandType.METHODCOMPLETED, params);
				LaunchedByMasSim = false;
				if(mq == null) {
					Main.Message(this, true, ": mq provider is null, cannot publish METHODCOMPLETED event");
				}
				else {
					mq.PublishMessage(event);
				}
			}
		//}
		return m_iStatus;
	}
	
	@Override
	public void terminate(){
		//return max speed back to normal
		m_pOwner.setMaxSpeed(RavenScript.getDouble("Bot_MaxSpeed"));
		
		// set goal status to completed.
		m_iStatus = Goal.CurrentStatus.completed;

	}


	@Override
	public void render(){
		if (m_iStatus == Goal.CurrentStatus.active)
		{
			GameCanvas.bluePen();
			GameCanvas.line(m_pOwner.pos(), m_Edge.Destination());
			GameCanvas.greenBrush();
			GameCanvas.blackPen();
			GameCanvas.circle(m_Edge.Destination(), 3);
		}
	}

}
