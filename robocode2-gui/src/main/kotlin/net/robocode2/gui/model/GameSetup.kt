package net.robocode2.gui.model

data class GameSetup(
        val gameType: String,
        val arenaWidth: Int,
        val isArenaWidthLocked: Boolean,
        val arenaHeight: Int,
        val isArenaHeightLocked: Boolean,
        val minNumberOfParticipants: Int,
        val isMinNumberOfParticipantsLocked: Boolean,
        val maxNumberOfParticipants: Int? = null,
        val isMaxNumberOfParticipantsLocked: Boolean,
        val numberOfRounds: Int,
        val isNumberOfRoundsLocked: Boolean,
        val gunCoolingRate: Double,
        val isGunCoolingRateLocked: Boolean,
        val maxInactivityTurns: Int,
        val isMaxInactivityTurnsLocked: Boolean,
        val turnTimeout: Int,
        val isTurnTimeoutLocked: Boolean,
        val readyTimeout: Int,
        val isReadyTimeoutLocked: Boolean
)