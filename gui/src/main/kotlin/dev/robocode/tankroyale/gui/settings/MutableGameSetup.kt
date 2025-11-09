package dev.robocode.tankroyale.gui.settings

import dev.robocode.tankroyale.client.model.GameSetup
import dev.robocode.tankroyale.client.model.IGameSetup

data class MutableGameSetup(
    override var gameType: String,
    override var arenaWidth: Int,
    override var isArenaWidthLocked: Boolean,
    override var arenaHeight: Int,
    override var isArenaHeightLocked: Boolean,
    override var minNumberOfParticipants: Int,
    override var isMinNumberOfParticipantsLocked: Boolean,
    override var maxNumberOfParticipants: Int? = null,
    override var isMaxNumberOfParticipantsLocked: Boolean,
    override var numberOfRounds: Int,
    override var isNumberOfRoundsLocked: Boolean,
    override var gunCoolingRate: Double,
    override var isGunCoolingRateLocked: Boolean,
    override var maxInactivityTurns: Int,
    override var isMaxInactivityTurnsLocked: Boolean,
    override var turnTimeout: Int,
    override var isTurnTimeoutLocked: Boolean,
    override var readyTimeout: Int,
    override var isReadyTimeoutLocked: Boolean,
    override var defaultTurnsPerSecond: Int
) : IGameSetup {

    fun copyFields(other: IGameSetup) {
        gameType = other.gameType
        arenaWidth = other.arenaWidth
        isArenaWidthLocked = other.isArenaWidthLocked
        arenaHeight = other.arenaHeight
        isArenaHeightLocked = other.isArenaHeightLocked
        minNumberOfParticipants = other.minNumberOfParticipants
        isMinNumberOfParticipantsLocked = other.isMinNumberOfParticipantsLocked
        maxNumberOfParticipants = other.maxNumberOfParticipants
        isMaxNumberOfParticipantsLocked = other.isMaxNumberOfParticipantsLocked
        numberOfRounds = other.numberOfRounds
        isNumberOfRoundsLocked = other.isNumberOfRoundsLocked
        gunCoolingRate = other.gunCoolingRate
        isGunCoolingRateLocked = other.isGunCoolingRateLocked
        maxInactivityTurns = other.maxInactivityTurns
        isMaxInactivityTurnsLocked = other.isMaxInactivityTurnsLocked
        turnTimeout = other.turnTimeout
        isTurnTimeoutLocked = other.isTurnTimeoutLocked
        readyTimeout = other.readyTimeout
        isReadyTimeoutLocked = other.isReadyTimeoutLocked
        defaultTurnsPerSecond = other.defaultTurnsPerSecond
    }
}

fun GameSetup.toMutableGameSetup(): MutableGameSetup {
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
        isReadyTimeoutLocked,
        defaultTurnsPerSecond
    )
}
