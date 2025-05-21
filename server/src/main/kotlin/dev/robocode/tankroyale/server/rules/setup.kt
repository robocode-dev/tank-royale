package dev.robocode.tankroyale.server.rules

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/** Default game type */
const val DEFAULT_GAME_TYPE = "classic"

/** Default arena width */
const val DEFAULT_ARENA_WIDTH = 800

/** Default arena height */
const val DEFAULT_ARENA_HEIGHT = 600

/** Default minimum number of bot participants */
const val DEFAULT_MIN_NUMBER_OF_PARTICIPANTS = 2

/** Default number of rounds */
const val DEFAULT_NUMBER_OF_ROUNDS = 35

/** Default gun cooling rate */
const val DEFAULT_GUN_COOLING_RATE = 0.1

/** Default number of allowed inactivity turns  */
const val DEFAULT_INACTIVITY_TURNS = 450

/** Default turn timeout */
val DEFAULT_TURN_TIMEOUT = 5.milliseconds

/** Default ready timeout */
val DEFAULT_READY_TIMEOUT = 1.seconds

/** Default turns per second (TPS) */
const val DEFAULT_TURNS_PER_SECOND = 30