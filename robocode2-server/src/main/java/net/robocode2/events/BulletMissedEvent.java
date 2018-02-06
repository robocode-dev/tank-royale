package net.robocode2.events;

import lombok.Value;
import net.robocode2.model.Bullet;

/**
 * Event sent when a bullet has missed (reached the walls).
 * 
 * @author Flemming N. Larsen
 */
@Value
public class BulletMissedEvent implements Event {

	/** Bullet that missed */
	Bullet bullet;
}