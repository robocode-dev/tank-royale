package dev.robocode.tankroyale.server.model

/** Mutable state of a round in a battle. */
data class MutableRound(
    /** Round number */
    override var roundNumber: Int,

    /** List of turns */
    override val turns: MutableList<MutableTurn> = mutableListOf(),

    /** Flag specifying if round has ended yet */
    override var roundEnded: Boolean = false,

) : IRound {

    /** Returns an immutable copy of this round */
    fun toRound() = Round(roundNumber, copyTurns(), roundEnded)

    /** Returns a copy of the turns */
    private fun copyTurns(): List<Turn> {
        val turnsCopy = mutableListOf<Turn>()
        turns.forEach { turn -> turnsCopy += turn.toTurn() }
        return turnsCopy.toList()
    }
}