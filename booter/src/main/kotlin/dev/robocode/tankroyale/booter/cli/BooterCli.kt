package dev.robocode.tankroyale.booter.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.options.versionOption
import dev.robocode.tankroyale.booter.util.VersionFileProvider

internal class BooterCli : CliktCommand(
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

