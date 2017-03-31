package net.robocode2.model.events;

public final class BotHitWallEvent implements IEvent {

	private final int victimId;

	public BotHitWallEvent(int victimId) {
		this.victimId = victimId;
	}

	public int getVictimId() {
		return victimId;
	}
}