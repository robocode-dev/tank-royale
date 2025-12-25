package dev.robocode.tankroyale.booter.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.robocode.tankroyale.booter.commands.DirCommand

internal class DirCli : CliktCommand() {
    override fun help(context: Context): String = "List all available bot and team directories."
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

