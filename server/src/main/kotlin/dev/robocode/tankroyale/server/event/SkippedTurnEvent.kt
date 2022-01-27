package dev.robocode.tankroyale.server.event

/** Event sent when a bot has skipped a turn. */
class SkippedTurnEvent(
    /** Turn number when turn was skipped */
    override val turnNumber: Int,
) : Event()