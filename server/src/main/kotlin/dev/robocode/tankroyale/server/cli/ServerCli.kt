package dev.robocode.tankroyale.server.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import com.github.ajalt.clikt.parameters.types.int
import dev.robocode.tankroyale.common.util.Version
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.core.Server
import dev.robocode.tankroyale.server.rules.DEFAULT_GAME_TYPE
import dev.robocode.tankroyale.server.rules.DEFAULT_TURNS_PER_SECOND

internal class ServerCli : CliktCommand() {
    override fun help(context: Context): String = "Runs a Robocode Tank Royale server"
    private val portOpt by option(
        "-p",
        "--port",
        help = "Port number (default: ${Server.DEFAULT_PORT}) or 'inherit' to use socket activation (if supported by the system)"
    )
    private val games by option(
        "-g",
        "--games",
        help = "Comma-separated list of game types (default: $DEFAULT_GAME_TYPE)"
    )
    private val controllerSecrets by option(
        "-c",
        "--controller-secrets",
        help = "Comma-separated list of controller secrets used for access control"
    )
    private val botSecrets by option(
        "-b",
        "--bot-secrets",
        help = "Comma-separated list of bot secrets used for access control"
    )
    private val enableInitialPosition by option(
        "-i",
        "--enable-initial-position",
        help = "Enable initial position for bots (default: false)"
    ).flag(default = false)
    private val tps by option(
        "-t",
        "--tps",
        help = "Initial Turns Per Second (TPS) (default: $DEFAULT_TURNS_PER_SECOND) in the range [-1..999], where -1 means maximum TPS, and 0 means paused."
    ).int()

    init {
        versionOption("Robocode Tank Royale Server ${Version.version}", names = setOf("-v", "--version"))
    }

    override fun run() {
        Server.port = portOpt ?: Server.DEFAULT_PORT.toString()
        Server.gameTypes = games ?: DEFAULT_GAME_TYPE
        Server.controllerSecrets = controllerSecrets
        Server.botSecrets = botSecrets
        Server.initialPositionEnabled = enableInitialPosition
        tps?.let { Server.tps = it }

        Server().run()
    }
}
