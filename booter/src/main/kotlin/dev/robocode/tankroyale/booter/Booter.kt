package dev.robocode.tankroyale.booter

import dev.robocode.tankroyale.booter.commands.DirCommand
import dev.robocode.tankroyale.booter.commands.RunCommand
import dev.robocode.tankroyale.booter.util.VersionFileProvider
import kotlinx.serialization.json.Json
import org.fusesource.jansi.AnsiConsole
import picocli.CommandLine
import picocli.CommandLine.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable
import kotlin.system.exitProcess

val cmdLine = CommandLine(Booter())

fun main(args: Array<String>) {
    System.setProperty("jansi.force", "true")
    AnsiConsole.systemInstall()

    cmdLine.apply {
        isSubcommandsCaseInsensitive = true
        isOptionsCaseInsensitive = true

        exitProcess(execute(*args))
    }}

@Command(
    name = "booter",
    versionProvider = VersionFileProvider::class,
    description = ["Tool for booting up Robocode bots."],
    mixinStandardHelpOptions = true
)
class Booter : Callable<Int> {

    override fun call(): Int {
        cmdLine.apply {
            when {
                isUsageHelpRequested -> usage(System.out)
                isVersionHelpRequested -> printVersionHelp(System.out)
                else -> usage(System.out)
            }
        }
        return 0
    }

    @Command(name = "dir", description = ["List all available bot and team directories."])
    @Suppress("unused")
    private fun dir(
        @Parameters(
            arity = "1..*",
            paramLabel = "BOT_ROOT_DIRS",
            description = ["Absolute file paths, where each path is a root directory containing bot or team entries."]
        )
        botRootDirs: Array<String>,

        @Option(
            names = ["--game-types", "-g"],
            paramLabel = "GAME_TYPES",
            description = ["Comma-separated string for filtering on game types that a bot need to support in order to appear in the listing."]
        )
        gameTypes: String?,

        @Option(
            names = ["--bots-only", "-b"],
            description = ["Flag set when only bots should be included in the listing."]
        )
        botsOnly: Boolean? = false,

        @Option(
            names = ["--teams-only", "-t"],
            description = ["Flag set when only teams should be included in the listing."]
        )
        teamsOnly: Boolean? = false,
    ) {
        DirCommand(toPaths(botRootDirs))
            .listBotDirectories(gameTypes, botsOnly == true, teamsOnly == true)
            .forEach { println(it) }
    }

    @Command(name = "info", description = ["List info of all available bots in JSON format."])
    @Suppress("unused")
    private fun info(
        @Parameters(
            arity = "1..*", paramLabel = "BOT_ROOT_DIRS",
            description = ["Absolute file paths, where each path is a root directory containing bot entries."]
        )
        botRootDirs: Array<String>,

        @Option(
            names = ["--game-types", "-g"], paramLabel = "GAME_TYPES",
            description = ["Comma-separated string for filtering on game types that a bot or team need to support in order to appear in the listing."]
        )
        gameTypes: String?,

        @Option(
            names = ["--bots-only", "-b"],
            description = ["Flag set when only bots should be included in the listing."]
        )
        botsOnly: Boolean? = false,

        @Option(
            names = ["--teams-only", "-t"],
            description = ["Flag set when only teams should be included in the listing."]
        )
        teamsOnly: Boolean? = false,
    ) {
        val entries = DirCommand(toPaths(botRootDirs))
            .listBootEntries(gameTypes, botsOnly == true, teamsOnly == true)
        println(Json.encodeToString(entries))
    }

    @Command(
        name = "boot",
        description = [
            "Boot one or multiple bot or team entries into individual bot processes.",
            "If a team is specified, the each bot members will be booted (not the team itself).",
            "",
            "Information about each started bot process is written to standard out with a line per process in the following format:",
            "{pid};{dir}",
            "where",
            "  {pid} is the process id",
            "  {dir} is the bot directory",
            "",
            "The following commands can be given via standard in:",
            "  quit        Terminates this command, and stops all running processes.",
            "  boot {dir}  Boots the bot from the specified bot directory.",
            "  stop {pid}  Stops the bot running with the specific process id.\n" +
            "              'stopped {pid}' is written out when the bot was stopped.\n" +
            "              'lost {pid}' is written out if the process id could not be found.",
        ]
    )
    @Suppress("unused")
    private fun boot(
        @Parameters(
            arity = "0..*",
            paramLabel = "BOT_DIRS",
            description = ["Absolute file paths, where each path is a bot directory containing a bot or team that must be run"]
        )
        botDirectories: Array<String>?
    ) {
        RunCommand().boot(botDirectories)
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