package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.rules.*

/** Game setup. */
data class GameSetup(
    /** Game type */
    var gameType: String = DEFAULT_GAME_TYPE,

    /** Arena width */
    var arenaWidth: Int = DEFAULT_ARENA_WIDTH,

    /** Arena height */
    var arenaHeight: Int = DEFAULT_ARENA_HEIGHT,

    /** Minimum number of bot participants */
    var minNumberOfParticipants: Int = DEFAULT_MIN_NUMBER_OF_PARTICIPANTS,

    /** Maximum number of bot participants */
    var maxNumberOfParticipants: Int?,

    /** Number of rounds */
    var numberOfRounds: Int = DEFAULT_NUMBER_OF_ROUNDS,

    /** Gun cooling rate */
    var gunCoolingRate: Double = DEFAULT_GUN_COOLING_RATE,

    /** Number of allowed inactivity turns */
    var maxInactivityTurns: Int = DEFAULT_INACTIVITY_TURNS,

    /** Turn timeout in milliseconds */
    var turnTimeout: Int = DEFAULT_TURN_TIMEOUT,

    /** Ready timeout in milliseconds */
    var readyTimeout: Int = DEFAULT_READY_TIMEOUT,

    /** Default turns per second (TPS) in milliseconds */
    var defaultTurnsPerSecond: Int = DEFAULT_TURNS_PER_SECOND,

    /** Flag specifying if the arena width is locked */
    var isArenaWidthLocked: Boolean,

    /** Flag specifying if the arena height is locked */
    var isArenaHeightLocked: Boolean,

    /** Flag specifying if the minimum number of bot participants is locked */
    var isMinNumberOfParticipantsLocked: Boolean,

    /** Flag specifying if the maximum number of bot participants is locked */
    var isMaxNumberOfParticipantsLocked: Boolean,

    /** Flag specifying if the number of rounds is locked */
    var isNumberOfRoundsLocked: Boolean,

    /** Flag specifying if the gun cooling rate is locked */
    var isGunCoolingRateLocked: Boolean,

    /** Flag specifying if the number of allowed inactivity turns is locked */
    var isMaxInactivityTurnsLocked: Boolean,

    /** Flag specifying if the turn timeout is locked */
    var isTurnTimeoutLocked: Boolean,

    /** Flag specifying if the ready timeout is locked */
    var isReadyTimeoutLocked: Boolean,
)