package dev.robocode.tankroyale.booter.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import dev.robocode.tankroyale.booter.commands.DirCommand
import kotlinx.serialization.json.Json

internal class InfoCli : CliktCommand() {
    override fun help(context: Context): String = "List info of all available bots in JSON format."
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

