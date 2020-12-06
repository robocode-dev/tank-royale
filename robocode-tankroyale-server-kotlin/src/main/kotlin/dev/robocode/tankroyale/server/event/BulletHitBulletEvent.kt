package dev.robocode.tankroyale.server.event

import dev.robocode.tankroyale.server.model.Bullet

/** Event sent when a bullet hits another bullet. */
data class BulletHitBulletEvent(
    /** Turn number when event occurred */
    override val turnNumber: Int,

    /** Bullet that hit another bullet */
    val bullet: Bullet?,

    /** Bullet that got hit by the bullet */
    val hitBullet: Bullet?,

) : Event()