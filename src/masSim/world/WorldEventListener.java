package masSim.world;

import masSim.taems.IAgent;

public interface WorldEventListener {
	    public void HandleWorldEvent(WorldEvent event);
	    public void RegisterMainAgent(IAgent agent);
}
