package net.robocode2.model.events;

import net.robocode2.model.Point;

public final class BotHitBotEvent implements IEvent {

	private final int botId;
	private final int victimId;
	private final double energy;
	private final Point position;
	private final boolean rammed;

	public BotHitBotEvent(int botId, int victimId, double energy, Point position, boolean rammed) {
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

	public Point getPosition() {
		return position;
	}

	public boolean isRammed() {
		return rammed;
	}
}