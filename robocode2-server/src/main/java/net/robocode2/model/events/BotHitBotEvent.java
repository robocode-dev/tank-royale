package net.robocode2.model.events;

import net.robocode2.model.Position;

public final class BotHitBotEvent implements Event {

	private final int botId;
	private final int victimId;
	private final double energy;
	private final Position position;
	private final boolean rammed;

	public BotHitBotEvent(int botId, int victimId, double energy, Position position, boolean rammed) {
		this.botId = botId;
		this.victimId = victimId;
		this.energy = energy;
		this.position = position;
		this.rammed = rammed;
	}

	public int getBotId() {
		return botId;
	}

	public int getVictimId() {
		return victimId;
	}

	public double getEnergy() {
		return energy;
	}

	public Position getPosition() {
		return position;
	}

	public boolean isRammed() {
		return rammed;
	}
}