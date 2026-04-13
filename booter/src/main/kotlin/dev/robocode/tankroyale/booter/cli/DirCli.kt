package dev.robocode.tankroyale.booter.cli

import com.github.ajalt.clikt.core.Context
import dev.robocode.tankroyale.booter.commands.DirCommand

internal class DirCli : BotFilterCli(name = "dir") {
    override fun help(context: Context): String = "List all available bot and team directories."

    override fun run() {
        DirCommand(toPaths(botRootDirs.toTypedArray()))
            .listBotDirectories(gameTypes, botsOnly, teamsOnly)
            .forEach { echo(it) }
    }
}

