package dev.robocode.tankroyale.booter

import dev.robocode.tankroyale.booter.commands.FilenamesCommand
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

    @Command(name = "filenames", description = ["List filenames of available bots"])
    private fun filenames(
        @Option(
            names = ["--bot-dirs", "-D"], paramLabel = "BOT_DIR",
            description = ["Comma-separated string of file paths to directories containing bots"]
        ) botDirs: String?,
        @Option(
            names = ["--game-types", "-T"], paramLabel = "GAME_TYPES",
            description = ["Comma-separated string of game types that the bot entries must support in order to be included in the list"]
        ) gameTypes: String?
    ) {
        FilenamesCommand(getBotDirectories(botDirs))
            .listBotEntries(gameTypes).forEach { entry -> println(entry.filename) }
    }

    @Command(name = "list", description = ["List available bot entries"])
    private fun list(
        @Option(
            names = ["--bot-dirs", "-D"], paramLabel = "BOT_DIR",
            description = ["Comma-separated string of file paths to directories containing bots"]
        ) botDirs: String?,
        @Option(
            names = ["--game-types", "-T"], paramLabel = "GAME_TYPES",
            description = ["Comma-separated string of game types that the bot entries must support in order to be included in the list"]
        ) gameTypes: String?
    ) {
        val entries = FilenamesCommand(getBotDirectories(botDirs))
            .listBotEntries(gameTypes)
        println(Json.encodeToString(entries))
    }

    @Command(
        name = "run", description = [
            "Starts running the specified bots in individual processes. Press the Enter key",
            "to stop all started bots and quit this tool. Information about each started",
            "process is written to standard out with a line per process in the following format:",
            "<process id>:<hash code>:<bot name>",
            "- `process id` is used for identifying the process",
            "- `hash code` is used for (uniquely) identifying the bot",
            "- `bot name` is the filename of the bot",
        ]
    )
    private fun run(
        @Option(
            names = ["--bot-dirs", "-D"], paramLabel = "BOT_DIR",
            description = ["Comma-separated string of file paths to directories containing bots"]
        ) botDirs: String?,
        @Parameters(
            arity = "1..*", paramLabel = "FILE",
            description = ["Filenames of the bots to start without file extensions"]
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