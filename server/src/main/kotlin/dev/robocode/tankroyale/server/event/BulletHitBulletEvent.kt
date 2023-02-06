package dev.robocode.tankroyale.server.event

import dev.robocode.tankroyale.server.model.IBullet

/** Event sent when a bullet hits another bullet. */
class BulletHitBulletEvent(
    /** Turn number when event occurred */
    override val turnNumber: Int,

    /** Bullet that hit another bullet */
    val bullet: IBullet,

    /** Bullet that got hit by the bullet */
    val hitBullet: IBullet,
) : Event()