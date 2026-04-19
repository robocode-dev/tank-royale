package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.common.util.Version
import dev.robocode.tankroyale.server.connection.ConnectionHandler
import dev.robocode.tankroyale.server.connection.GameServerConnectionListener
import com.google.gson.Gson
import dev.robocode.tankroyale.server.cli.SERVER_BANNER_LINES
import dev.robocode.tankroyale.server.cli.convertPicocliMarkupToAnsi
import dev.robocode.tankroyale.common.rules.DEFAULT_GAME_TYPE
import dev.robocode.tankroyale.common.rules.DEFAULT_TURNS_PER_SECOND
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

        // These vars are written ONCE by the CLI (picocli) before any game thread starts.
        // After startup, they are effectively read-only — no synchronisation needed for reads.
        // @GuardedBy("written before game threads start; read-only thereafter")

        /** Server port or "inherit" to use an inherited socket channel. */
        var port: String = DEFAULT_PORT.toString()

        val useInheritedChannel: Boolean
            get() = port.equals(INHERIT, ignoreCase = true)

        val portNumber: Int
            get() = if (useInheritedChannel) getInheritedPort() else port.toIntOrNull() ?: DEFAULT_PORT

        /** Comma-separated list of game types the server will accept. */
        var gameTypes: String = DEFAULT_GAME_TYPE

        /** Secret tokens required for controller connections; null means any controller may connect. */
        var controllerSecrets: String? = null

        /** Secret tokens required for bot connections; null means any bot may connect. */
        var botSecrets: String? = null

        /** When true, bots may request specific initial positions and headings. */
        var initialPositionEnabled = false

        /** Initial turns-per-second rate for new games. */
        var tps: Int = DEFAULT_TURNS_PER_SECOND

        /** Flag specifying if debug mode is supported. */
        var debugModeSupported: Boolean = true

        /** Flag specifying if breakpoint mode is supported. */
        var breakpointModeSupported: Boolean = true

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
        // Load server.properties from file system (if exists)
        ServerProperties.load()

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

        val config = ServerConfig(
            port = portNumber,
            gameTypes = gameTypes.toSetOfTrimmedStrings(),
            controllerSecrets = controllerSecretsSet,
            botSecrets = botSecretsSet,
            initialPositionEnabled = initialPositionEnabled,
            tps = tps,
            debugModeSupported = debugModeSupported,
            breakpointModeSupported = breakpointModeSupported
        )

        val gson = Gson()
        var gameServerPtr: GameServer? = null
        val connectionHandler = ConnectionHandler(
            ServerSetup(config.gameTypes),
            GameServerConnectionListener { gameServerPtr!! },
            config.controllerSecrets,
            config.botSecrets,
            config.debugModeSupported,
            config.breakpointModeSupported
        )
        val participantRegistry = ParticipantRegistry(connectionHandler)
        val broadcaster = MessageBroadcaster(connectionHandler, gson)
        val lifecycleManager = GameLifecycleManager()
        val resultsBuilder = ResultsBuilder({ gameServerPtr?.modelUpdater }, participantRegistry)

        gameServer = GameServer(
            config,
            connectionHandler,
            participantRegistry,
            lifecycleManager,
            broadcaster,
            resultsBuilder,
            gson
        )
        gameServerPtr = gameServer

        gameServer.start()
    }

    private fun String?.toSetOfTrimmedStrings(): Set<String> =
        HashSet(this?.replace("\\s".toRegex(), "")?.split(",")?.filter { it.isNotBlank() }.orEmpty())
}
