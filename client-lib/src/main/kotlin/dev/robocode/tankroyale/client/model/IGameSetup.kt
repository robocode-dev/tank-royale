package dev.robocode.tankroyale.client.model

import kotlinx.serialization.SerialName

@SerialName("GameSetup")
interface IGameSetup {
    val gameType: String
    val arenaWidth: Int
    val isArenaWidthLocked: Boolean
    val arenaHeight: Int
    val isArenaHeightLocked: Boolean
    val minNumberOfParticipants: Int
    val isMinNumberOfParticipantsLocked: Boolean
    val maxNumberOfParticipants: Int?
    val isMaxNumberOfParticipantsLocked: Boolean
    val numberOfRounds: Int
    val isNumberOfRoundsLocked: Boolean
    val gunCoolingRate: Double
    val isGunCoolingRateLocked: Boolean
    val maxInactivityTurns: Int
    val isMaxInactivityTurnsLocked: Boolean
    val turnTimeout: Int
    val isTurnTimeoutLocked: Boolean
    val readyTimeout: Int
    val isReadyTimeoutLocked: Boolean
    val defaultTurnsPerSecond: Int

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
            isReadyTimeoutLocked,
            defaultTurnsPerSecond
        )
    }
}