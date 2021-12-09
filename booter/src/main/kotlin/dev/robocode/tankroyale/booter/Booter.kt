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

    @Command(name = "dir", description = ["List directories of all available bots."])
    private fun filenames(
        @Option(
            names = ["--bot-dirs", "-D"], paramLabel = "BOT_DIRS",
            description = ["Comma-separated string of file paths to directories containing bots."]
        ) botDirs: String?,
        @Option(
            names = ["--game-types", "-T"], paramLabel = "GAME_TYPES",
            description = ["Comma-separated string of game types that the bot entries must support in order to be included in the list"]
        ) gameTypes: String?
    ) {
        DirCommand(getBotDirectories(botDirs)).listBotDirectories(gameTypes).forEach { println(it) }
    }

    @Command(name = "info", description = ["List info for all available bots in JSON format."])
    private fun list(
        @Option(
            names = ["--dirs", "-D"], paramLabel = "BOT_DIRS",
            description = ["Comma-separated list of absolute file paths to bot root directories."]
        ) botDirs: String?,
        @Option(
            names = ["--game-types", "-T"], paramLabel = "GAME_TYPES",
            description = ["Comma-separated list of game types that the bot entries must support in order to be included in the list."]
        ) gameTypes: String?
    ) {
        val entries = DirCommand(getBotDirectories(botDirs)).listBotEntries(gameTypes)
        println(Json.encodeToString(entries))
    }

    @Command(
        name = "run", description = [
            "Starts running the bots in individual processes.",
            "Press the Enter key to stop all started bots and quit this tool.",
            "Information about each started process is written to standard out",
            "with a line per process in the following format:",
            "<process id>:<hash code>:<bot name>",
            "- `process id` is used for identifying the process",
            "- `hash code` is used for (uniquely) identifying the bot",
            "- `bot name` is the filename of the bot",
        ]
    )
    private fun run(
        @Option(
            names = ["--dirs", "-D"], paramLabel = "BOT_DIRS",
            description = ["Comma-separated list of absolute file paths to bot root directories."]
        ) botDirs: String?,
        @Parameters(
            arity = "1..*", paramLabel = "DIR",
            description = ["Absolute file paths, where each path is a bot directory."]
        ) filenames: Array<String>
    ) {
        RunCommand(getBotDirectories(botDirs)).runBots(filenames)
    }

    companion object {
        /**
         * Returns file paths to specified bot directoriesCSV (semicolon separated list).
         * If no file paths are provided, the file path of current working directory is returned
         */
        private fun getBotDirectories(directoriesCSV: String?): List<Path> {
            if (directoriesCSV == null)
                return listOf(Paths.get("").toAbsolutePath())

            val paths = ArrayList<Path>()
            directoriesCSV.split(",").forEach {
                val path = Paths.get(it.trim())
                if (Files.exists(path)) {
                    paths.add(path)
                }
            }
            return paths
        }
    }
}