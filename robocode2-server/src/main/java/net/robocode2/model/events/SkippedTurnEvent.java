package net.robocode2.model.events;

/**
 * Event sent when a bot has skipped a turn
 * 
 * @author Flemming N. Larsen
 */
public final class SkippedTurnEvent implements IEvent {

	/** Turn the got skipped */
	private final int skippedTurn;

	/**
	 * Creates a new skipped turn event
	 * 
	 * @param skippedTurn
	 *            is the turn that got skipped
	 */
	public SkippedTurnEvent(int skippedTurn) {
		this.skippedTurn = skippedTurn;
	}

	/** Returns the turn that got skipped */
	public int getSkippedTurn() {
		return skippedTurn;
	}
}