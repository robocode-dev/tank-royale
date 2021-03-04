package dev.robocode.tankroyale.server.model

/** Immutable state of a round in a battle. */
data class Round(
    /** Round number */
    override val roundNumber: Int,

    /** List of turns */
    override val turns: List<ITurn>,

    /** Flag specifying if round has ended yet */
    override val roundEnded: Boolean,

) : IRound