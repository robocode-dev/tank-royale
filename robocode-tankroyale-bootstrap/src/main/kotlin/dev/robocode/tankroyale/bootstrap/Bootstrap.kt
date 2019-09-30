package dev.robocode.tankroyale.bootstrap

import dev.robocode.tankroyale.bootstrap.util.BootUtil
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.stringify
import picocli.CommandLine
import picocli.CommandLine.Command
import picocli.CommandLine.Option
import picocli.CommandLine.Parameters
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

val cmdLine = CommandLine(Bootstrap())

fun main(args: Array<String>) {
    exitProcess(cmdLine.execute(*args))
}

@Command(
    name = "bootstrap",
    versionProvider = VersionFileProvider::class,
    description = ["Tool for booting up Robocode bots"],
    mixinStandardHelpOptions = true
)
class Bootstrap : Callable<Int> {

    override fun call(): Int {
        when {
            cmdLine.isUsageHelpRequested -> cmdLine.usage(System.out)
            cmdLine.isVersionHelpRequested -> cmdLine.printVersionHelp(System.out)
            else -> cmdLine.usage(System.out)
        }
        return 0
    }

    @Command(name = "filenames", description = ["List filenames of available bots"])
    @ImplicitReflectionSerializer
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
        BootUtil(getBotDirectories(botDirs)).findBotEntries(gameTypes).forEach { entry -> println(entry.filename) }
    }

    @Command(name = "list", description = ["List available bot entries"])
    @UnstableDefault
    @ImplicitReflectionSerializer
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
        val entries = BootUtil(getBotDirectories(botDirs)).findBotEntries(gameTypes)
        println(Json(JsonConfiguration.Default).stringify(entries))
    }

    @ImplicitReflectionSerializer
    @Command(
        name = "run", description = [
            "Start running the specified bots in individual processes.",
            "Press enter key to stop all started bots and quit this tool."
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
        val processes = BootUtil(getBotDirectories(botDirs)).startBots(filenames)

        readLine()

        processes.parallelStream().forEach { p ->

            p.descendants().forEach { d ->
                d.destroy()
                if (d.isAlive)
                    d.destroyForcibly()
            }
            p.destroy()
            p.waitFor(10, TimeUnit.SECONDS)
            if (p.isAlive) {
                p.destroyForcibly()
                p.waitFor(10, TimeUnit.SECONDS)
            }
        }
    }

    /** Returns file paths to specified bot directoriesCSV (semicolon separated list).
     * If no file paths are provided, the file path of current working directory is returned */
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

internal class VersionFileProvider : CommandLine.IVersionProvider {

    override fun getVersion(): Array<String> {
        val inputStream = this.javaClass.classLoader.getResourceAsStream("version.txt")
        var version = "?"
        if (inputStream != null) {
            BufferedReader(InputStreamReader(inputStream)).use { br -> version = br.readLine() }
        }
        return arrayOf("Robocode Tank Royale Bootstrap $version")
    }
}