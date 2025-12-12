package dev.robocode.tankroyale.booter

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.versionOption
import dev.robocode.tankroyale.booter.commands.BootCommand
import dev.robocode.tankroyale.booter.commands.DirCommand
import dev.robocode.tankroyale.booter.util.VersionFileProvider
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

fun main(args: Array<String>) = BooterCli().subcommands(DirCli(), InfoCli(), BootCli()).main(args)

private class BooterCli : CliktCommand(
    name = "booter",
    help = "Tool for booting up Robocode bots.",
    printHelpOnEmptyArgs = true
) {
    init {
        versionOption(VersionFileProvider.getVersion(), names = setOf("-v", "--version"))
    }

    override fun run() {
        // No-op; help is printed automatically on empty args
    }
}

private class DirCli : CliktCommand(name = "dir", help = "List all available bot and team directories.") {
    private val botRootDirs by argument("BOT_ROOT_DIRS").multiple(required = true)
    private val gameTypes by option(
        "--game-types",
        "-g",
        help = "Comma-separated string for filtering on game types that a bot need to support in order to appear in the listing."
    )
    private val botsOnly by option(
        "--bots-only",
        "-b",
        help = "Flag set when only bots should be included in the listing."
    ).flag(default = false)
    private val teamsOnly by option(
        "--teams-only",
        "-t",
        help = "Flag set when only teams should be included in the listing."
    ).flag(default = false)

    override fun run() {
        DirCommand(toPaths(botRootDirs.toTypedArray()))
            .listBotDirectories(gameTypes, botsOnly, teamsOnly)
            .forEach { echo(it) }
    }
}

private class InfoCli : CliktCommand(name = "info", help = "List info of all available bots in JSON format.") {
    private val botRootDirs by argument("BOT_ROOT_DIRS").multiple(required = true)
    private val gameTypes by option(
        "--game-types",
        "-g",
        help = "Comma-separated string for filtering on game types that a bot or team need to support in order to appear in the listing."
    )
    private val botsOnly by option(
        "--bots-only",
        "-b",
        help = "Flag set when only bots should be included in the listing."
    ).flag(default = false)
    private val teamsOnly by option(
        "--teams-only",
        "-t",
        help = "Flag set when only teams should be included in the listing."
    ).flag(default = false)

    override fun run() {
        val entries = DirCommand(toPaths(botRootDirs.toTypedArray()))
            .listBootEntries(gameTypes, botsOnly, teamsOnly)
        echo(Json.encodeToString(entries))
    }
}

private class BootCli : CliktCommand(
    name = "boot",
    help = """
            Boot one or multiple bot or team entries into individual bot processes.
            If a team is specified, the each bot members will be booted (not the team itself).

            Information about each started bot process is written to standard out with a line per process in the following format:
            {pid};{dir}
            where
              {pid} is the process id
              {dir} is the bot directory

            The following commands can be given via standard in:
              quit        Terminates this command, and stops all running processes.
              boot {dir}  Boots the bot from the specified bot directory.
              stop {pid}  Stops the bot running with the specific process id.
                          'stopped {pid}' is written out when the bot was stopped.
                          'lost {pid}' is written out if the process id could not be found.
        """.trimIndent()
) {
    private val botDirs by argument("BOT_DIRS").multiple()

    override fun run() {
        val arr: Array<String>? = if (botDirs.isEmpty()) null else botDirs.toTypedArray()
        BootCommand().boot(arr)
    }
}

private fun toPaths(botRootDirs: Array<String>?): List<Path> =
    botRootDirs?.toSet()?.map {
        val path = Paths.get(it.trim())
        if (Files.exists(path)) path else null
    }?.mapNotNull { it } ?: emptyList()