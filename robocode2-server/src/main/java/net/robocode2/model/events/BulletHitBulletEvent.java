package net.robocode2.model.events;

import lombok.Value;
import net.robocode2.model.Bullet;

/**
 * Event sent when a bullet hits another bullet.
 * 
 * @author Flemming N. Larsen
 */
@Value
public class BulletHitBulletEvent implements Event {

	/** Bullet that hit another bullet */
	Bullet bullet;

	/** Bullet that got hit by the bullet */
	Bullet hitBullet;
}