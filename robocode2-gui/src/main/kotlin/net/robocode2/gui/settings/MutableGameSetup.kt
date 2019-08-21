package net.robocode2.gui.settings

import net.robocode2.gui.model.GameSetup

data class MutableGameSetup(
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
    var maxInactivityTurns: Int,
    var isMaxInactivityTurnsLocked: Boolean,
    var turnTimeout: Int,
    var isTurnTimeoutLocked: Boolean,
    var readyTimeout: Int,
    var isReadyTimeoutLocked: Boolean
) {
    fun toGameSetup(): GameSetup {
        return GameSetup(
            gameType,
            arenaWidth,
            isArenaWidthLocked,
            arenaHeight,
            isArenaHeightLocked,
            minNumberOfParticipants,
            isMinNumberOfParticipantsLocked,
            maxNumberOfParticipants,
            isMaxNumberOfParticipantsLocked,
            numberOfRounds,
            isNumberOfRoundsLocked,
            gunCoolingRate,
            isGunCoolingRateLocked,
            maxInactivityTurns,
            isMaxInactivityTurnsLocked,
            turnTimeout,
            isTurnTimeoutLocked,
            readyTimeout,
            isReadyTimeoutLocked
        )
    }
}