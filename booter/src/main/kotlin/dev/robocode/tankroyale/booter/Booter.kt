package dev.robocode.tankroyale.booter

import dev.robocode.tankroyale.booter.commands.DirCommand
import dev.robocode.tankroyale.booter.commands.RunCommand
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import picocli.CommandLine
import picocli.CommandLine.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable
import kotlin.system.exitProcess

val cmdLine = CommandLine(Booter())

fun main(args: Array<String>) {
    exitProcess(cmdLine.execute(*args))
}

@Command(
    name = "booter",
    versionProvider = VersionFileProvider::class,
    description = ["Tool for booting up Robocode bots"],
    mixinStandardHelpOptions = true
)
class Booter : Callable<Int> {

    override fun call(): Int {
        when {
            cmdLine.isUsageHelpRequested -> cmdLine.usage(System.out)
            cmdLine.isVersionHelpRequested -> cmdLine.printVersionHelp(System.out)
            else -> cmdLine.usage(System.out)
        }
        return 0
    }

    @Command(name = "dir", description = ["List all available bot directories."])
    private fun dir(
        @Parameters(
            arity = "1..*", paramLabel = "BOT_ROOT_DIRS",
            description = ["Absolute file paths, where each path is a root directory containing bot entries"]
        ) botRootDirs: Array<String>,
        @Option(
            names = ["--game-types", "-T"], paramLabel = "GAME_TYPES",
            description = ["Comma-separated string of game types that the bot entries must support in order to be included in the list"]
        ) gameTypes: String?
    ) {
        DirCommand(toPaths(botRootDirs)).listBotDirectories(gameTypes).forEach { println(it) }
    }

    @Command(name = "info", description = ["List info for all available bots in JSON format."])
    private fun info(
        @Parameters(
            arity = "1..*", paramLabel = "BOT_ROOT_DIRS",
            description = ["Absolute file paths, where each path is a root directory containing bot entries"]
        ) botRootDirs: Array<String>,
        @Option(
            names = ["--game-types", "-T"], paramLabel = "GAME_TYPES",
            description = ["Comma-separated list of game types that the bot entries must support in order to be included in the list."]
        ) gameTypes: String?
    ) {
        val entries = DirCommand(toPaths(botRootDirs)).listBotEntries(gameTypes)
        println(Json.encodeToString(entries))
    }

    @Command(
        name = "run", description = [
            "Starts running the bots in individual processes.",
            "",
            "Information about each started process is written to standard out with a line per process in " +
                    "one of the following formats, depending if a unique identifier was provided when booting a bot:",
            "{pid};{dir}",
            "where",
            "  {pid} is the process id",
            "  {dir} is the bot directory",
            "",
            "The following commands can be given via standard in:",
            "  quit              Terminates this command, and stops all running processes",
            "  boot {dir}        Boots the bot from the specified bot directory",
            "  kill {pid}        Kills the bot running with the specific process id",
        ]
    )
    private fun run(
        @Parameters(
            arity = "0..*", paramLabel = "BOT_DIRS",
            description = [
                "Absolute file paths, where each path is a bot directory.",
            ]
        ) botDirs: Array<String>,
    ) {
        RunCommand().runBots(botDirs)
    }

    companion object {
        /**
         * Returns file paths to specified bot directoriesCSV (semicolon separated list).
         * If no file paths are provided, the file path of current working directory is returned
         */
        private fun toPaths(botRootDirs: Array<String>?): List<Path> =
            botRootDirs?.toSet()?.map {
                val path = Paths.get(it.trim())
                if (Files.exists(path)) path else null
            }?.mapNotNull { it } ?: emptyList()
    }
}