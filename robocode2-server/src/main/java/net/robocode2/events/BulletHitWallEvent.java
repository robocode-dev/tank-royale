package net.robocode2.events;

import lombok.Value;
import net.robocode2.model.Bullet;

/**
 * Event sent when a bullet has hit the wall of the battle arena.
 * 
 * @author Flemming N. Larsen
 */
@Value
public class BulletHitWallEvent implements Event {

	/** Bullet that missed */
	Bullet bullet;
}