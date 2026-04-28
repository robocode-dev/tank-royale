package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.common.rules.DEFAULT_GAME_TYPES
import dev.robocode.tankroyale.common.rules.DEFAULT_TURNS_PER_SECOND
import dev.robocode.tankroyale.common.util.UserDataDirectory
import java.io.File
import java.util.Properties

/**
 * Server configuration loaded from server.properties file.
 *
 * Search order (first wins, no warnings if file doesn't exist):
 * 1. Working directory: ${user.dir}/server.properties
 * 2. User home directory: ${user.home}/server.properties
 * 3. Platform user data directory
 *
 * Values can be overridden via CLI arguments.
 */
object ServerProperties {

    private val props = Properties()

    /**
     * Load properties from server.properties files.
     * Must be called before accessing any properties.
     */
    fun load() {
        val searchPaths = listOf(
            File(System.getProperty("user.dir")),
            File(System.getProperty("user.home")),
            UserDataDirectory.get()
        )

        for (path in searchPaths) {
            val file = File(path, "server.properties")
            if (file.isFile) {
                file.inputStream().use { input ->
                    props.load(input)
                }
                break // First file wins
            }
        }
    }

    val port: Int
        get() = props.getProperty("port", "7654").toInt()

    val gameTypes: Set<String>
        get() = props.getProperty("games", DEFAULT_GAME_TYPES)
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

    val controllerSecrets: Set<String>
        get() = props.getProperty("controllerSecrets", "")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

    val botSecrets: Set<String>
        get() = props.getProperty("botSecrets", "")
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toSet()

    val initialPositionEnabled: Boolean
        get() = props.getProperty("initialPositionEnabled", "false").toBoolean()

    val tps: Int
        get() = props.getProperty("tps", DEFAULT_TURNS_PER_SECOND.toString()).toInt()

    val debugModeSupported: Boolean
        get() = props.getProperty("debugModeSupported", "true").toBoolean()

    val breakpointModeSupported: Boolean
        get() = props.getProperty("breakpointModeSupported", "true").toBoolean()
}
