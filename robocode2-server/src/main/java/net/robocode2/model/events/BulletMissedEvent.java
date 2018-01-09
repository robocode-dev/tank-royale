package net.robocode2.model.events;

import lombok.Value;
import net.robocode2.model.Bullet;

/**
 * Event sent when a bullet has missed (reached the walls).
 * 
 * @author Flemming N. Larsen
 */
@Value
public class BulletMissedEvent implements IEvent {

	/** Bullet that missed */
	Bullet bullet;
}