package dev.robocode.tankroyale.server.event

import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.IBullet

/** Event sent when a bullet hits a bot. */
class BulletHitBotEvent(
    /** Turn number when event occurred */
    override val turnNumber: Int,

    /** Bullet that hit the bot */
    val bullet: IBullet,

    /** Bot id of the victim that was hit by the bullet */
    val victimId: BotId,

    /** Damage dealt to the victim */
    val damage: Double,

    /** New energy level of the victim after damage */
    val energy: Double,
) : Event()