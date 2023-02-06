package dev.robocode.tankroyale.server.event

import dev.robocode.tankroyale.server.model.BotId

/** Event sent when a bot has hit a wall. */
class BotHitWallEvent(
    /** Turn number when event occurred */
    override val turnNumber: Int,

    /** Bot id of the victim that has hit a wall */
    val victimId: BotId,
) : Event()