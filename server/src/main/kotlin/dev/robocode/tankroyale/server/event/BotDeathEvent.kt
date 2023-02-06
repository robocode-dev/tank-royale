package dev.robocode.tankroyale.server.event

import dev.robocode.tankroyale.server.model.BotId

/** Event sent when a bot has been killed. */
class BotDeathEvent(
    /** Turn number when event occurred */
    override val turnNumber: Int,

    /** Bot id of the victim that got killed */
    val victimId: BotId,
) : Event()
