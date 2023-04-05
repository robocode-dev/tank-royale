package dev.robocode.tankroyale.server

import dev.robocode.tankroyale.server.Server.VersionFileProvider
import dev.robocode.tankroyale.server.core.GameServer
import dev.robocode.tankroyale.server.rules.DEFAULT_GAME_TYPE
import dev.robocode.tankroyale.server.rules.DEFAULT_TURNS_PER_SECOND
import org.fusesource.jansi.AnsiConsole
import picocli.CommandLine
import picocli.CommandLine.*
import picocli.CommandLine.Model.CommandSpec
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.system.exitProcess

private const val DEFAULT_PORT: Int = 7654

fun main(args: Array<String>) {
    System.setProperty("jansi.force", "true")
    AnsiConsole.systemInstall()
    exitProcess(Server.cmdLine.execute(*args))
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

        @Option(names = ["-V", "--version"], description = ["Display version info"])
        private var isVersionInfoRequested = false

        @Option(names = ["-h", "--help"], description = ["Display this help message"])
        private var isUsageHelpRequested = false

        @Option(
            names = ["-p", "--port"],
            type = [Int::class],
            description = ["Port number (default: $DEFAULT_PORT)"]
        )
        var port: Int = DEFAULT_PORT

        @Option(
            names = ["-g", "--games"],
            type = [String::class],
            description = ["Comma-separated list of game types (default: $DEFAULT_GAME_TYPE)"]
        )
        private var gameTypes: String = DEFAULT_GAME_TYPE

        @Option(
            names = ["-C", "--controllerSecrets"],
            type = [String::class],
            description = ["Comma-separated list of controller secrets used for access control"]
        )
        private var controllerSecrets: String? = null

        @Option(
            names = ["-B", "--botSecrets"],
            type = [String::class],
            description = ["Comma-separated list of bot secrets used for access control"]
        )
        private var botSecrets: String? = null

        @Option(
            names = ["-I", "--enable-initial-position"],
            description = ["Enable initial position for bots (default: false)"]
        )
        var initialPositionEnabled = false

        @Option(
            names = ["-t", "--tps"],
            type = [Short::class],
            description = ["Initial Turns Per Second (TPS) (default: $DEFAULT_TURNS_PER_SECOND) in the range [-1..999], where -1 means maximum TPS"]
        )
        var tps: Int = DEFAULT_TURNS_PER_SECOND

        val cmdLine = CommandLine(Server())
    }

    @Spec
    private val spec: CommandSpec? = null
    private lateinit var gameServer: GameServer

    override fun run() {
        val cmdLine = CommandLine(Server())
        when {
            isUsageHelpRequested -> {
                cmdLine.usage(System.out)
                exitProcess(0)
            }
            isVersionInfoRequested -> {
                cmdLine.printVersionHelp(System.out)
                exitProcess(0)
            }
            else -> {
                val banner = spec!!.usageMessage().header()
                for (line in banner) {
                    printAnsiLine(line)
                }
                cmdLine.printVersionHelp(System.out)
            }
        }

        // Handle port
        if (port !in 1..65535) {
            System.err.println(
                """
                    Port must not be lower than 1 or bigger than 65535.
                    Default port is $DEFAULT_PORT used for http.
                """.trimIndent()
            )
            exitProcess(-1)
        }

        // Run thread that checks standard input (stdin) for an exit signal ("q")
        Thread {
            Scanner(System.`in`).apply {
                while (hasNextLine()) {
                    nextLine().apply {
                        if (trim().equals("q", ignoreCase = true)) {
                            gameServer.stop()
                            exitProcess(1)
                        }
                    }
                }
            }
        }.start()

        // Start game server on main thread
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

    internal class VersionFileProvider : IVersionProvider {
        @Throws(Exception::class)
        override fun getVersion(): Array<String> {
            val inputStream = this.javaClass.classLoader.getResourceAsStream("version.txt")
            var version = "?"
            if (inputStream != null) {
                BufferedReader(InputStreamReader(inputStream)).use { br -> version = br.readLine() }
            }
            return arrayOf("Robocode Tank Royale Server $version")
        }
    }
}