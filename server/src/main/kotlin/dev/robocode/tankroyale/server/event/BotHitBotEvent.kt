package dev.robocode.tankroyale.server.event

import dev.robocode.tankroyale.server.model.BotId

/** Event sent when a bot collides with another bot. */
class BotHitBotEvent(
    /** Turn number when event occurred */
    override val turnNumber: Int,

    /** Bot id of the bot hitting another bot */
    val botId: BotId,

    /** Bot id of the victim bot that got hit */
    val victimId: BotId,

    /** Energy level of the victim */
    val energy: Double,

    /** X coordinate of the victim */
    val x: Double,

    /** Y coordinate of the victim */
    val y: Double,

    /** Flag specifying if the victim was rammed */
    val isRammed: Boolean,
) : Event()