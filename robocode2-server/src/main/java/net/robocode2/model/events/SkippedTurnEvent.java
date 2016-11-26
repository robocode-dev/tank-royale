package net.robocode2.model.events;

public final class SkippedTurnEvent implements Event {

	private final int skippedTurn;

	public SkippedTurnEvent(int skippedTurn) {
		this.skippedTurn = skippedTurn;
	}

	public int getSkippedTurn() {
		return skippedTurn;
	}
}