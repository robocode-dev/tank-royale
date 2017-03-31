package net.robocode2.model.events;

public final class BotDeathEvent implements IEvent {

	private final int victimId;

	public BotDeathEvent(int victimId) {
		this.victimId = victimId;
	}

	public int getVictimId() {
		return victimId;
	}
}