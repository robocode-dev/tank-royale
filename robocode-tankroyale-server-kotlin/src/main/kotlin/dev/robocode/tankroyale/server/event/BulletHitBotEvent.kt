package dev.robocode.tankroyale.server.event

import dev.robocode.tankroyale.server.model.Bullet

/** Event sent when a bullet hits a bot. */
data class BulletHitBotEvent (
    /** Turn number when event occurred */
    override val turnNumber: Int,

    /** Bullet that hit the bot */
    val bullet: Bullet?,

    /** Bot id of the victim that was hit by the bullet */
    val victimId: Int,

    /** Damage dealt to the victim */
    val damage: Int,

    /** New energy level of the victim after damage */
    val energy: Int,

) : Event()