package net.robocode2.gui.settings

data class GameType(
    val width: Int = 800,
    val height: Int = 600,
    val minNumParticipants: Int = 2,
    val maxNumParticipants: Int? = null,
    val numberOfRounds: Int = 35,
    val inactivityTurns: Int = 450,
    val gunCoolingRate: Double = 0.1
)
