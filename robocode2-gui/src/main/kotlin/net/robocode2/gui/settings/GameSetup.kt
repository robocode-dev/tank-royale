package net.robocode2.gui.settings

data class GameSetup(
        var width: Int = 800,
        var height: Int = 600,
        var minNumParticipants: Int = 2,
        var maxNumParticipants: Int? = null,
        var numberOfRounds: Int = 35,
        var inactivityTurns: Int = 450,
        var gunCoolingRate: Double = 0.1
)
