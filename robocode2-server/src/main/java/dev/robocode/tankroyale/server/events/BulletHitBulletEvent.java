package dev.robocode.tankroyale.server.events;

import lombok.Value;
import dev.robocode.tankroyale.server.model.Bullet;

/**
 * Event sent when a bullet hits another bullet.
 *
 * @author Flemming N. Larsen
 */
@Value
public class BulletHitBulletEvent implements Event {

	/** Turn number when event occurred */
	int turnNumber;

	/** Bullet that hit another bullet */
	Bullet bullet;

	/** Bullet that got hit by the bullet */
	Bullet hitBullet;
}