package net.robocode2.events;

import lombok.Value;
import net.robocode2.model.Bullet;

/**
 * Event sent when a bullet hits a bot.
 * 
 * @author Flemming N. Larsen
 */
@Value
public class BulletHitBotEvent implements Event {

	/** Turn number when event occurred */
	int turnNumber;

	/** Bullet that hit the bot */
	Bullet bullet;

	/** Bot id of the victim that was hit by the bullet */
	int victimId;

	/** Damage dealt to the victim */
	double damage;

	/** New energy level of the victim after damage */
	double energy;
}