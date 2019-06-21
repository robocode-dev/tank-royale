package net.robocode2;

import lombok.Builder;
import lombok.Value;

/** Current bullet state */
@Value
@Builder
public final class BulletState {
    /** ID of the bullet */
    int bulletId;
    /** ID of the bot that fired the bullet */
    int ownerId;
    /** Bullet firepower level */
    double power;
    /** X coordinate */
    double x;
    /** Y coordinate */
    double y;
    /** Direction in degrees */
    double direction;
    /** Speed measured in pixels per turn */
    double speed;
}
