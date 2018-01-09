package net.robocode2.model.events;

import lombok.Value;
import net.robocode2.model.Bullet;

/**
 * Event sent when a bot is hit by a bullet.
 * 
 * @author Flemming N. Larsen
 */
@Value
public class HitByBulletEvent implements IEvent {

	/** Bullet that hit the bot */
	Bullet bullet;

	/** Damage dealt to the bot */
	double damage;

	/** New energy level of the bot after damage */
	double energy;
}