package net.robocode2.events;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.robocode2.BulletState;

/** Event occurring when a bullet has been fired from a bot */
@Value
@EqualsAndHashCode(callSuper = true)
@Builder
public class BulletFiredEvent extends GameEvent {
    /** Bullet that was fired */
    BulletState bullet;
}
