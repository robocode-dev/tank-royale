package dev.robocode.tankroyale.server.event

import dev.robocode.tankroyale.server.model.IBullet

/** Event sent when a bullet has hit the wall of the battle arena. */
class BulletHitWallEvent(
    /** Turn number when event occurred */
    override val turnNumber: Int,

    /** Bullet that missed */
    val bullet: IBullet
) : Event()