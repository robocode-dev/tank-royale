package dev.robocode.tankroyale.server

import dev.robocode.tankroyale.server.core.GameServer
import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.util.VersionFileProvider
import dev.robocode.tankroyale.server.rules.DEFAULT_GAME_TYPE
import dev.robocode.tankroyale.server.rules.DEFAULT_TURNS_PER_SECOND
import org.fusesource.jansi.AnsiConsole
import picocli.CommandLine
import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec
import java.nio.channels.ServerSocketChannel
import java.util.*
import kotlin.system.exitProcess

private const val DEFAULT_PORT: Int = 7654

fun main(args: Array<String>) {
    System.setProperty("jansi.force", "true")
    AnsiConsole.systemInstall()

    Server.cmdLine.apply {
        isSubcommandsCaseInsensitive = true
        isOptionsCaseInsensitive = true

        exitProcess(execute(*args))
    }
}

@Command(
    name = "Server",
    versionProvider = VersionFileProvider::class,
    header = [
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
    ],
    descriptionHeading = "Description:%n",
    description = ["Runs a Robocode Tank Royale server"]
)
class Server : Runnable {

    companion object {

        private const val EXIT_COMMAND = "q"
        private const val MIN_PORT = 1000
        private const val MAX_PORT = 65535

        const val INHERIT = "inherit"

        @Option(names = ["-v", "--version"], description = ["Display version info"])
        private var isVersionInfoRequested = false

        @Option(names = ["-h", "--help"], description = ["Display this help message"])
        private var isUsageHelpRequested = false

        @Option(
            names = ["-p", "--port"],
            type = [String::class],
            description = ["Port number (default: $DEFAULT_PORT) or '$INHERIT' to use socket activation (if supported by the system)"]
        )
        private var port: String = DEFAULT_PORT.toString()

        val useInheritedChannel: Boolean
            get() = port.equals(INHERIT, ignoreCase = true)

        val portNumber: Int
            get() = if (useInheritedChannel) getInheritedPort() else port.toIntOrNull() ?: DEFAULT_PORT

        @Option(
            names = ["-g", "--games"],
            type = [String::class],
            description = ["Comma-separated list of game types (default: $DEFAULT_GAME_TYPE)"]
        )
        private var gameTypes: String = DEFAULT_GAME_TYPE

        @Option(
            names = ["-c", "--controller-secrets"],
            type = [String::class],
            description = ["Comma-separated list of controller secrets used for access control"]
        )
        private var controllerSecrets: String? = null

        @Option(
            names = ["-b", "--bot-secrets"],
            type = [String::class],
            description = ["Comma-separated list of bot secrets used for access control"]
        )
        private var botSecrets: String? = null

        @Option(
            names = ["-i", "--enable-initial-position"],
            description = ["Enable initial position for bots (default: false)"]
        )
        var initialPositionEnabled = false

        @Option(
            names = ["-t", "--tps"],
            type = [Short::class],
            description = ["Initial Turns Per Second (TPS) (default: $DEFAULT_TURNS_PER_SECOND) in the range [-1..999], where -1 means maximum TPS, and 0 means paused."]
        )
        var tps: Int = DEFAULT_TURNS_PER_SECOND

        val cmdLine = CommandLine(Server())

        private fun getInheritedPort(): Int {
            val channel = System.inheritedChannel() as? ServerSocketChannel
            return channel?.socket()?.localPort ?: -1
        }
    }

    @Spec
    private val spec: CommandSpec? = null
    private lateinit var gameServer: GameServer

    override fun run() {
        handleCommandLineOptions()
        validatePort()
        startExitInputMonitorThread()
        startGameServer()
    }

    private fun handleCommandLineOptions() {
        val cmdLine = CommandLine(this)

        when {
            isUsageHelpRequested -> {
                printUsageHelp(cmdLine)
            }
            isVersionInfoRequested -> {
                printVersionHelp(cmdLine)
            }
            else -> {
                displayBanner()
                printVersionHelp(cmdLine)
            }
        }
    }

    private fun printUsageHelp(cmdLine: CommandLine) {
        cmdLine.usage(System.out)
    }

    private fun printVersionHelp(cmdLine: CommandLine) {
        cmdLine.printVersionHelp(System.out)
    }

    private fun displayBanner() {
        val banner = spec?.usageMessage()?.header() ?: return
        banner.forEach { line ->
            printAnsiLine(line)
        }
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
        gameServer = GameServer(
            gameTypes.toSetOfTrimmedStrings(),
            controllerSecrets.toSetOfTrimmedStrings(),
            botSecrets.toSetOfTrimmedStrings()
        )
        gameServer.start()
    }

    private fun String?.toSetOfTrimmedStrings(): Set<String> =
        HashSet(this?.replace("\\s".toRegex(), "")?.split(",")?.filter { it.isNotBlank() }.orEmpty())

    private fun printAnsiLine(line: String?) {
        println(Help.Ansi.AUTO.string(line))
    }
}