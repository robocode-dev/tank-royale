package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.robocode2.BulletState;

/** Event occurring when a bullet has hit a wall */
@Value
@EqualsAndHashCode(callSuper = true)
@Builder
public class BulletHitWallEvent extends GameEvent {
  /** Bullet that has hit a wall */
  BulletState bullet;
}
