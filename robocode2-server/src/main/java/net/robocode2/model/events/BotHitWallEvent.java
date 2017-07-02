package net.robocode2.model.events;

/**
 * Event sent when a bot has hit a wall
 * 
 * @author Flemming N. Larsen
 */
public final class BotHitWallEvent implements IEvent {

	/** Bot id of the victim that has hit a wall */
	private final int victimId;

	/**
	 * Creates a new bot hit wall event
	 * 
	 * @param victimId
	 *            is the bot id of the victim that has hit a wall
	 */
	public BotHitWallEvent(int victimId) {
		this.victimId = victimId;
	}

	/** Returns the bot id of the victim that has hit a wall */
	public int getVictimId() {
		return victimId;
	}
}