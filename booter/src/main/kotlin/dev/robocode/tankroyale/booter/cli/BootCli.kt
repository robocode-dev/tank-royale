package dev.robocode.tankroyale.booter.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.Context
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import dev.robocode.tankroyale.booter.commands.BootCommand

internal class BootCli : CliktCommand() {
    override fun help(context: Context): String = """
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
              stopped {pid} is written out when the bot was stopped.
              lost {pid}    is written out if the process id could not be found.
        """.trimIndent()

    private val botDirs by argument("BOT_DIRS").multiple()

    override fun run() {
        val arr: Array<String>? = if (botDirs.isEmpty()) null else botDirs.toTypedArray()
        BootCommand().boot(arr)
    }
}

