package dev.robocode.tankroyale.ui.desktop.model

import dev.robocode.tankroyale.ui.desktop.settings.MutableGameSetup
import kotlinx.serialization.Serializable

@Serializable
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
) {
    fun toMutableGameSetup(): MutableGameSetup {
        return MutableGameSetup(
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