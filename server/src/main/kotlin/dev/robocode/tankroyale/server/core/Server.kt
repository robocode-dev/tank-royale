package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.common.util.Version
import dev.robocode.tankroyale.server.core.GameServer
import dev.robocode.tankroyale.server.cli.SERVER_BANNER_LINES
import dev.robocode.tankroyale.server.cli.convertPicocliMarkupToAnsi
import dev.robocode.tankroyale.server.rules.DEFAULT_GAME_TYPE
import dev.robocode.tankroyale.server.rules.DEFAULT_TURNS_PER_SECOND
import org.slf4j.LoggerFactory
import java.nio.channels.ServerSocketChannel
import java.util.HashSet
import java.util.Scanner
import kotlin.system.exitProcess

class Server : Runnable {

    companion object {
        const val DEFAULT_PORT: Int = 7654
        private const val EXIT_COMMAND = "quit"
        private const val MIN_PORT = 1000
        private const val MAX_PORT = 65535
        const val INHERIT = "inherit"

        // Set by CLI
        var port: String = DEFAULT_PORT.toString()

        val useInheritedChannel: Boolean
            get() = port.equals(INHERIT, ignoreCase = true)

        val portNumber: Int
            get() = if (useInheritedChannel) getInheritedPort() else port.toIntOrNull() ?: DEFAULT_PORT

        var gameTypes: String = DEFAULT_GAME_TYPE

        var controllerSecrets: String? = null

        var botSecrets: String? = null

        var initialPositionEnabled = false

        var tps: Int = DEFAULT_TURNS_PER_SECOND

        private fun getInheritedPort(): Int {
            val channel = System.inheritedChannel() as? ServerSocketChannel
            return channel?.socket()?.localPort ?: -1
        }
    }

    private lateinit var gameServer: GameServer

    private val log = LoggerFactory.getLogger(this::class.java)

    private val ANSI_GREEN = "\u001B[32m"
    private val ANSI_RED = "\u001B[31m"
    private val ANSI_DEFAULT = "\u001B[39m"

    override fun run() {
        printBannerAndVersion()
        validatePort()
        startExitInputMonitorThread()
        startGameServer()
    }

    private fun printBannerAndVersion() {
        SERVER_BANNER_LINES.forEach { println(convertPicocliMarkupToAnsi(it)) }
        println("Robocode Tank Royale Server ${Version.version}")
    }

    private fun validatePort() {
        if (!useInheritedChannel && portNumber !in MIN_PORT..MAX_PORT) {
            reportInvalidPort()
            exitProcess(1) // general error
        }
    }

    private fun reportInvalidPort() {
        System.err.println(
            """
                Port must be either 'inherit' or a number between $MIN_PORT and $MAX_PORT.
                Default port is $DEFAULT_PORT used for http.
            """.trimIndent()
        )
    }

    private fun startExitInputMonitorThread() {
        // When inheriting a channel, it is passed as FD3, i.e. stdin. In this case, it does not
        // make sense to monitor for an exit command.
        if (useInheritedChannel) return

        Thread {
            monitorStandardInputForExit()
        }.apply {
            isDaemon = true
            start()
        }
    }

    private fun monitorStandardInputForExit() {
        Scanner(System.`in`).use { scanner ->
            while (scanner.hasNextLine()) {
                val input = scanner.nextLine().trim()
                if (input.equals(EXIT_COMMAND, ignoreCase = true)) {
                    gameServer.stop()
                    exitProcess(1)
                }
            }
        }
    }

    private fun startGameServer() {
        val controllerSecretsSet = controllerSecrets.toSetOfTrimmedStrings()
        val botSecretsSet = botSecrets.toSetOfTrimmedStrings()
        val secretsEnabled = controllerSecretsSet.isNotEmpty() || botSecretsSet.isNotEmpty()

        // Log whether server secrets (keys) are enabled at startup
        if (secretsEnabled) {
            log.info(
                "Server secrets: ${ANSI_GREEN}ENABLED${ANSI_DEFAULT} (controllers={}, bots={})",
                controllerSecretsSet.size,
                botSecretsSet.size
            )
        } else {
            log.info("Server secrets: ${ANSI_RED}DISABLED${ANSI_DEFAULT}")
        }

        gameServer = GameServer(
            gameTypes.toSetOfTrimmedStrings(),
            controllerSecretsSet,
            botSecretsSet
        )
        gameServer.start()
    }

    private fun String?.toSetOfTrimmedStrings(): Set<String> =
        HashSet(this?.replace("\\s".toRegex(), "")?.split(",")?.filter { it.isNotBlank() }.orEmpty())
}