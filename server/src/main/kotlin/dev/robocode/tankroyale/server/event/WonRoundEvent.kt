package dev.robocode.tankroyale.server.event

/** Event sent when a bot has won the round. */
class WonRoundEvent(
    /** Turn number when turn was skipped */
    override val turnNumber: Int,
) : Event()