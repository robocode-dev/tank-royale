package net.robocode2.gui.model

data class GameSetup(
        var gameType: String,
        var arenaWidth: Int,
        var isArenaWidthLocked: Boolean,
        var arenaHeight: Int,
        var isArenaHeightLocked: Boolean,
        var minNumberOfParticipants: Int,
        var isMinNumberOfParticipantsLocked: Boolean,
        var maxNumberOfParticipants: Int? = null,
        var isMaxNumberOfParticipantsLocked: Boolean,
        var numberOfRounds: Int,
        var isNumberOfRoundsLocked: Boolean,
        var gunCoolingRate: Double,
        var isGunCoolingRateLocked: Boolean,
        var inactivityTurns: Int,
        var isInactivityTurnsLocked: Boolean,
        var turnTimeout: Int,
        var isTurnTimeoutLocked: Boolean,
        var readyTimeout: Int,
        var isReadyTimeoutLocked: Boolean
)