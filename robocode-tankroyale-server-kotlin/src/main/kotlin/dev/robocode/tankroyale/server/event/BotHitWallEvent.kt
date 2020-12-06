package dev.robocode.tankroyale.server.event

/** Event sent when a bot has hit a wall. */
data class BotHitWallEvent(
    /** Turn number when event occurred */
    override val turnNumber: Int,

    /** Bot id of the victim that has hit a wall */
    val victimId: Int,

) : Event()