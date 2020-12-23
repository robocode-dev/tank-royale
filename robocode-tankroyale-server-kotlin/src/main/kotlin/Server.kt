package dev.robocode.tankroyale.server

import dev.robocode.tankroyale.server.Server.VersionFileProvider
import dev.robocode.tankroyale.server.core.GameServer
import dev.robocode.tankroyale.server.rules.DEFAULT_GAME_TYPE
import org.fusesource.jansi.AnsiConsole
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.IVersionProvider
import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Option
import picocli.CommandLine.Spec
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import kotlin.system.exitProcess

private const val DEFAULT_PORT: Short = 80

@Command(
    name = "Server",
    versionProvider = VersionFileProvider::class,
    header = [
        "               __________",
        "              /          |DDD==============",
        "    _________|___________|____________",
        "  _/_ ______________________________ _\\_",
        " / _ ) ___  ___  ___  ___  ___  ___ / __|",
        " \\_\\_\\/ _ \\| __)/ _ \\/ __// _ \\| _ \\\\__|",
        "      \\___/|___)\\___/\\___|\\___/|___/",
        "",
        "           Robocode Tank Royale",
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
            type = [Short::class],
            description = ["Port number (default: $DEFAULT_PORT)"]
        )
        var port: Short = DEFAULT_PORT

        @Option(
            names = ["-g", "--games"],
            type = [String::class],
            description = ["Comma-separated list of game types (default: $DEFAULT_GAME_TYPE)"]
        )
        private var gameTypes: String = DEFAULT_GAME_TYPE

        @Option(names = ["-s", "--secret"], description = ["Client secret used for access control"])
        private var secret: String? = null

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
                    Default port is 80 used for http.
                """.trimIndent()
            )
            exitProcess(-1)
        }

        // Run thread that checks standard input (stdin) for an exit signal ("q")
        Thread {
            val sc = Scanner(System.`in`)
            while (sc.hasNextLine()) {
                val line = sc.nextLine()
                if (line.trim().equals("q", ignoreCase = true)) {
                    gameServer.stop()
                    exitProcess(1)
                }
            }
        }.start()

        // Start game server on main thread
        gameServer = GameServer(gameTypes, secret)
        gameServer.start()
    }

    private fun printAnsiLine(s: String?) {
        AnsiConsole.systemInstall()
        println(CommandLine.Help.Ansi.AUTO.string(s))
        AnsiConsole.systemUninstall()
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

fun main(args: Array<String>) {
//    System.setProperty("picocli.ansi", "true");
    exitProcess(Server.cmdLine.execute(*args))
}