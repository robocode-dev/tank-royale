package net.robocode2.model.events;

import net.robocode2.model.Point;

/**
 * Event sent when a bot collides with another bot
 * 
 * @author Flemming N. Larsen
 */
public final class BotHitBotEvent implements IEvent {

	/** Bot id of the bot hitting another bot */
	private final int botId;
	/** Bot id of the victim bot that got hit */
	private final int victimId;
	/** Energy level of the victim */
	private final double victimEnergy;
	/** Position of the victim */
	private final Point VictimPosition;
	/** Flag specifying if the victim was rammed */
	private final boolean rammed;

	/**
	 * Creates a bot hit bot event
	 * 
	 * @param botId
	 *            is the bot id of the bot hitting another bot
	 * @param victimId
	 *            is the bot id of the victim bot that got hit
	 * @param victimEnergy
	 *            is the energy level of the victim
	 * @param victimPosition
	 *            is the position of the victim
	 * @param rammed
	 *            is specifying if the victim was rammed
	 */
	public BotHitBotEvent(int botId, int victimId, double victimEnergy, Point victimPosition, boolean rammed) {
		this.botId = botId;
		this.victimId = victimId;
		this.victimEnergy = victimEnergy;
		this.VictimPosition = victimPosition;
		this.rammed = rammed;
	}

	/** Returns the bot id of the bot hitting another bot */
	public int getBotId() {
		return botId;
	}

	/** Returns the bot id of the victim bot that got hit */
	public int getVictimId() {
		return victimId;
	}

	/** Returns the energy level of the victim */
	public double getEnergy() {
		return victimEnergy;
	}

	/** Returns the position of the victim */
	public Point getPosition() {
		return VictimPosition;
	}

	/** Checks if the victim was rammed */
	public boolean isRammed() {
		return rammed;
	}
}