package dev.robocode.tankroyale.server

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.int
import dev.robocode.tankroyale.common.util.Version
import dev.robocode.tankroyale.server.core.GameServer
import dev.robocode.tankroyale.server.rules.DEFAULT_GAME_TYPE
import dev.robocode.tankroyale.server.rules.DEFAULT_TURNS_PER_SECOND
import org.slf4j.LoggerFactory
import java.nio.channels.ServerSocketChannel
import java.util.*
import kotlin.system.exitProcess

private const val DEFAULT_PORT: Int = 7654

fun main(args: Array<String>) = ServerCli().main(args)

class Server : Runnable {

    companion object {

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

private class ServerCli : CliktCommand(name = "server", help = "Runs a Robocode Tank Royale server") {
    private val portOpt by option(
        "-p",
        "--port",
        help = "Port number (default: $DEFAULT_PORT) or 'inherit' to use socket activation (if supported by the system)"
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
        // Apply options to Server companion object as the rest of the server reads from there
        Server.port = portOpt ?: DEFAULT_PORT.toString()
        Server.gameTypes = games ?: DEFAULT_GAME_TYPE
        Server.controllerSecrets = controllerSecrets
        Server.botSecrets = botSecrets
        Server.initialPositionEnabled = enableInitialPosition
        tps?.let { Server.tps = it }

        Server().run()
    }
}

// ------- Banner utilities (Picocli ANSI markup -> real ANSI) -------
private const val RESET_ANSI = "\u001B[0m"
private val PICOCli_TAG = Regex("@\\|([^ ]+) (.*?)\\|@")

private fun convertPicocliMarkupToAnsi(s: String): String {
    // Replace Picocli markup tags with ANSI and collapse escaped '@@' to '@'
    val withAnsi = PICOCli_TAG.replace(s) { m ->
        val attrs = m.groupValues[1].split(",")
        val text = m.groupValues[2]
        val codes = attrs.mapNotNull { aRaw ->
            val a = aRaw.trim()
            when {
                a.equals("bold", true) -> "1"
                a.startsWith("fg(", ignoreCase = true) -> {
                    val inside = a.substringAfter('(').substringBeforeLast(')')
                    val parts = inside.split(';')
                    if (parts.size == 3) {
                        val (r, g, b) = parts.map { it.toIntOrNull() }
                        if (r != null && g != null && b != null) {
                            if (r in 0..5 && g in 0..5 && b in 0..5) {
                                val idx = 16 + 36 * r + 6 * g + b
                                "38;5;$idx"
                            } else {
                                "38;2;${r.coerceIn(0, 255)};${g.coerceIn(0, 255)};${b.coerceIn(0, 255)}"
                            }
                        } else null
                    } else {
                        when (inside.lowercase()) {
                            "green" -> "32"
                            "red" -> "31"
                            "blue" -> "34"
                            else -> null
                        }
                    }
                }

                else -> null
            }
        }
        val start = if (codes.isNotEmpty()) codes.joinToString(separator = "") { code -> "\u001B[${code}m" } else ""
        "$start${text}$RESET_ANSI"
    }
    return withAnsi.replace("@@", "@")
}

private val SERVER_BANNER_LINES = listOf(
    "@|bold,fg(0;0;5)               ___________|@",
    "@|bold,fg(0;0;5)              /           ||@@|bold,fg(2;2;2) [[[]========((()|@",
    "@|bold,fg(0;0;5)    _________|____________|___________|@",
    "@|bold,fg(2;2;2)  _|@@|bold,fg(0;0;5) /|@@|bold,fg(2;2;2) _|@@|bold,fg(0;0;5) ________________________________|@@|bold,fg(2;2;2) _|@@|bold,fg(0;0;5) \\|@@|bold,fg(2;2;2) _|@",
    "@|bold,fg(2;2;2) / _ ) ___  ___  ___  ___  ___  ___ / ,_||@",
    "@|bold,fg(2;2;2) \\_\\_\\/ _ \\| __)/ _ \\/ __// _ \\| _ \\\\__||@",
    "@|bold,fg(2;2;2)      \\___/|___)\\___/\\___|\\___/|___/|@",
    "",
    "@|bold,green           Robocode Tank Royale|@",
    ""
)