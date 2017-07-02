package net.robocode2.model.events;

/**
 * Event sent when a bot has been killed.
 * 
 * @author Flemming N. Larsen
 */
public final class BotDeathEvent implements IEvent {

	/** Bot id of the victim that got killed */
	private final int victimId;

	/**
	 * Creates a new bot death event
	 * 
	 * @param victimId
	 *            is the bot id of the victim that got killed
	 */
	public BotDeathEvent(int victimId) {
		this.victimId = victimId;
	}

	/** Returns the bot id of the victim that got killed */
	public int getVictimId() {
		return victimId;
	}
}