package dev.robocode.tankroyale.server.event

import dev.robocode.tankroyale.server.model.Bullet

/** Event sent when a bullet has fired a bullet. */
data class BulletFiredEvent(
    /** Turn number when event occurred */
    override val turnNumber: Int,

    /** Fired bullet */
    val bullet: Bullet,

) : Event()