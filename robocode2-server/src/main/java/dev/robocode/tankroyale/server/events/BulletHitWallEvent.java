package dev.robocode.tankroyale.server.events;

import lombok.Value;
import dev.robocode.tankroyale.server.model.Bullet;

/**
 * Event sent when a bullet has hit the wall of the battle arena.
 * 
 * @author Flemming N. Larsen
 */
@Value
public class BulletHitWallEvent implements Event {

	/** Turn number when event occurred */
	int turnNumber;

	/** Bullet that missed */
	Bullet bullet;
}