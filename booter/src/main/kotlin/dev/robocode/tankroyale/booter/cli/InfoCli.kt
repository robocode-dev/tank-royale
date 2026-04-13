package dev.robocode.tankroyale.booter.cli

import com.github.ajalt.clikt.core.Context
import dev.robocode.tankroyale.booter.commands.DirCommand
import kotlinx.serialization.json.Json

internal class InfoCli : BotFilterCli(name = "info") {
    override fun help(context: Context): String = "List info of all available bots in JSON format."

    override fun run() {
        val entries = DirCommand(toPaths(botRootDirs.toTypedArray()))
            .listBootEntries(gameTypes, botsOnly, teamsOnly)
        echo(Json.encodeToString(entries))
    }
}

