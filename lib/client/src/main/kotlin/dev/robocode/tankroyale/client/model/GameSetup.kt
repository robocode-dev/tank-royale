package dev.robocode.tankroyale.client.model

import kotlinx.serialization.Serializable

@Serializable
data class GameSetup(
    override val gameType: String,
    override val arenaWidth: Int,
    override val isArenaWidthLocked: Boolean,
    override val arenaHeight: Int,
    override val isArenaHeightLocked: Boolean,
    override val minNumberOfParticipants: Int,
    override val isMinNumberOfParticipantsLocked: Boolean,
    override val maxNumberOfParticipants: Int? = null,
    override val isMaxNumberOfParticipantsLocked: Boolean,
    override val numberOfRounds: Int,
    override val isNumberOfRoundsLocked: Boolean,
    override val gunCoolingRate: Double,
    override val isGunCoolingRateLocked: Boolean,
    override val maxInactivityTurns: Int,
    override val isMaxInactivityTurnsLocked: Boolean,
    override val turnTimeout: Int,
    override val isTurnTimeoutLocked: Boolean,
    override val readyTimeout: Int,
    override val isReadyTimeoutLocked: Boolean,
    override val defaultTurnsPerSecond: Int
) : IGameSetup
