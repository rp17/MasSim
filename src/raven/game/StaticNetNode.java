package raven.game;

import raven.math.Vector2D;
import raven.ui.GameCanvas;

public class StaticNetNode extends NetNode {

	public StaticNetNode(String name, RavenGame world, Vector2D position) {
		super(name, world, position);
	}
	public StaticNetNode(String name, RavenGame world, Vector2D position, double rad) {
		super(name, world, position, rad);
	}
	@Override
	public void update(double delta) {
		// Moved from render() since this is time dependent!
		numSecondsHitPersistant -= delta;

		// process the currently active goal. Note this is required even if
		// the bot is under user control. This is because a goal is created
		// whenever a user clicks on an area of the map that necessitates a
		// path planning request.
		//brain.process(delta);

		// Calculate the steering force and update the bot's velocity and
		// position; no need to move, it's a static node
		//updateMovement(delta);

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
			if (goalArbitrationRegulator.isReady()) {
				brain.Arbitrate();
			}

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
		//GameCanvas.bluePen();
		//GameCanvas.filledCircle(pos(), 2.5);
		GameCanvas.greenPen();
		GameCanvas.circle(pos(), radius);
	}
}
