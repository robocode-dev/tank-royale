package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.rules.*

/** Game setup. */
data class GameSetup(
    /** Game type */
    val gameType: String = DEFAULT_GAME_TYPE,

    /** Arena width */
    val arenaWidth: Int = DEFAULT_ARENA_WIDTH,

    /** Arena height */
    val arenaHeight: Int = DEFAULT_ARENA_HEIGHT,

    /** Minimum number of bot participants */
    val minNumberOfParticipants: Int = DEFAULT_MIN_NUMBER_OF_PARTICIPANTS,

    /** Maximum number of bot participants */
    val maxNumberOfParticipants: Int?,

    /** Number of rounds */
    val numberOfRounds: Int = DEFAULT_NUMBER_OF_ROUNDS,

    /** Gun cooling rate */
    val gunCoolingRate: Double = DEFAULT_GUN_COOLING_RATE,

    /** Number of allowed inactivity turns */
    val maxInactivityTurns: Int = DEFAULT_INACTIVITY_TURNS,

    /** Turn timeout in milliseconds */
    val turnTimeout: Int = DEFAULT_TURN_TIMEOUT,

    /** Ready timeout in milliseconds */
    val readyTimeout: Int = DEFAULT_READY_TIMEOUT,

    /** Default turns per second (TPS) in milliseconds */
    val defaultTurnsPerSecond: Int = DEFAULT_TURNS_PER_SECOND,

    /** Flag specifying if the arena width is locked */
    val isArenaWidthLocked: Boolean,

    /** Flag specifying if the arena height is locked */
    val isArenaHeightLocked: Boolean,

    /** Flag specifying if the minimum number of bot participants is locked */
    val isMinNumberOfParticipantsLocked: Boolean,

    /** Flag specifying if the maximum number of bot participants is locked */
    val isMaxNumberOfParticipantsLocked: Boolean,

    /** Flag specifying if the number of rounds is locked */
    val isNumberOfRoundsLocked: Boolean,

    /** Flag specifying if the gun cooling rate is locked */
    val isGunCoolingRateLocked: Boolean,

    /** Flag specifying if the number of allowed inactivity turns is locked */
    val isMaxInactivityTurnsLocked: Boolean,

    /** Flag specifying if the turn timeout is locked */
    val isTurnTimeoutLocked: Boolean,

    /** Flag specifying if the ready timeout is locked */
    val isReadyTimeoutLocked: Boolean,
)