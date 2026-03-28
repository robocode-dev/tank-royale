package dev.robocode.tankroyale.server.core

/** Server configuration. */
data class ServerConfig(
    /** Server port number */
    val port: Int,
    /** Supported game types */
    val gameTypes: Set<String>,
    /** Optional controller secrets */
    val controllerSecrets: Set<String>,
    /** Optional bot secrets */
    val botSecrets: Set<String>,
    /** Flag specifying if initial position is enabled */
    val initialPositionEnabled: Boolean,
    /** Default turns per second (TPS) */
    val tps: Int
)
