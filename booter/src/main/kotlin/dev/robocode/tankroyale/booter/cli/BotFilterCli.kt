package dev.robocode.tankroyale.booter.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

/**
 * Abstract base command that owns the shared filtering options for listing bot directories.
 * Subclasses supply [name] and implement [run] to produce the desired output format.
 */
internal abstract class BotFilterCli(name: String) : CliktCommand(name = name) {

    protected val botRootDirs by argument("BOT_ROOT_DIRS").multiple(required = true)

    protected val gameTypes by option(
        "--game-types", "-g",
        help = "Comma-separated string for filtering on game types that a bot or team need to support in order to appear in the listing."
    )

    protected val botsOnly by option(
        "--bots-only", "-b",
        help = "Flag set when only bots should be included in the listing."
    ).flag(default = false)

    protected val teamsOnly by option(
        "--teams-only", "-t",
        help = "Flag set when only teams should be included in the listing."
    ).flag(default = false)
}
