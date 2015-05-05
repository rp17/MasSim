package raven.game;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

import raven.game.interfaces.INetNode;
import raven.goals.GoalNetThink;
import raven.math.Vector2D;
import raven.ui.GameCanvas;

import java.util.Queue;
import java.util.PriorityQueue;

public class NetNode extends RavenBot implements INetNode, Comparator<INetNode> {
	protected double radius;
	protected GoalNetThink reason;
	protected Queue<INetNode> neighbors = new PriorityQueue<INetNode>(10, this);
	public NetNode(String name, RavenGame world, Vector2D position) {
		this(name, world, position, 10.0);
	}
	public NetNode(String name, RavenGame world, Vector2D position, double rad) {
		super(world, position);
		radius = rad;
		reason = new GoalNetThink(this);
	}
	public int compare(INetNode ag1, INetNode ag2){
		double dist1 = this.pos().distanceSq(ag1.pos());
		double dist2 = this.pos().distanceSq(ag2.pos());
		int res = 0;
		if(dist1 < dist2) res = -1;
		else if(dist1 > dist2) res = 1;
		return res;
	}
	public double getRadius(){
		return radius;
	}
	public String getName(){
		return name;
	}
	public void addNeighbor(INetNode ag) {
		neighbors.add(ag);
	}
	public void removeNeighbor(INetNode ag) {
		neighbors.remove(ag);
	}
	public INetNode peekClosest(){
		return neighbors.peek();
	}
	public INetNode pollClosest(){
		return neighbors.poll();
	}
	/*
	public INetNode[] getSortedNeighbors(){
		INetNode[] localAgents = ((INetNode[])neighbors.toArray());
		Arrays.sort(localAgents, this);
		return localAgents;
	}
	*/
	
	@Override
	public void update(double delta) {
		// Moved from render() since this is time dependent!
		numSecondsHitPersistant -= delta;

		// process the currently active goal. Note this is required even if
		// the bot is under user control. This is because a goal is created
		// whenever a user clicks on an area of the map that necessitates a
		// path planning request.
		//brain.process(delta);
		reason.process(delta);

		// Calculate the steering force and update the bot's velocity and
		// position
		updateMovement(delta);

		// if the bot is under AI control but not scripted
		if (!isPossessed()) {
			weaponSelectionRegulator.update(delta);
			goalArbitrationRegulator.update(delta);
			targetSelectionRegulator.update(delta);
			triggerTestRegulator.update(delta);

			// examine all the opponents in the bots sensory memory and select
			// one to be the current target
			/*
			if (targetSelectionRegulator.isReady()) {
				targSys.update();
			}
			 */
			// appraise and arbitrate between all possible high level goals
			/*
			if (goalArbitrationRegulator.isReady()) {
				reason.Arbitrate();
			}
			 */
			// update the sensory memory with any visual stimulus
			//sensoryMem.updateVision(delta);

			// select the appropriate weapon to use from the weapons currently
			// in the inventory
			/*
			if (weaponSelectionRegulator.isReady()) {
				weaponSys.selectWeapon();
			}
			 */
			// this method aims the bot's current weapon at the current target
			// and takes a shot if a shot is possible
			//weaponSys.takeAimAndShoot(delta);
		}
	}
	@Override
	public void render() {
		super.render();
		GameCanvas.redPen();
		GameCanvas.circle(pos(), radius);
	}
}
