package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.process.ProcessManager
import java.util.*
import kotlin.io.path.Path

/**
 * Command for running bots and managing their processes.
 */
class BootCommand : Command() {

    private val processManager = ProcessManager()

    /**
     * Main entry point to boot bots from specified paths and process command line input.
     */
    fun boot(bootPaths: Array<String>?) {
        // Register cleanup hook for graceful shutdown
        processManager.registerShutdownHook()

        // Start initial bots if provided
        bootInitialBots(bootPaths)

        // Start interactive command loop
        processCommandLineInput()
    }

    // COMMAND PROCESSING

    /**
     * Process interactive commands from standard input.
     */
    private fun processCommandLineInput() {
        while (true) {
            val line = readlnOrNull()?.trim()
            val cmdAndArgs = line?.split("\\s+".toRegex(), limit = 2)

            if (cmdAndArgs?.isNotEmpty() != true) continue

            val command = cmdAndArgs[0].lowercase(Locale.getDefault()).trim()

            // Handle quit command
            if (command == "quit") break

            // Handle commands with arguments
            if (cmdAndArgs.size >= 2) {
                handleCommand(command, cmdAndArgs[1])
            }
        }
    }

    /**
     * Handle a command with its argument.
     */
    private fun handleCommand(command: String, arg: String) {
        when (command) {
            "boot" -> processManager.createBotProcess(Path(arg), this::getBootEntry)
            "stop" -> processManager.stopBotProcess(arg.toLong())
        }
    }

    /**
     * Boot initial bots from provided paths.
     */
    private fun bootInitialBots(bootPaths: Array<String>?) {
        bootPaths?.forEach { processManager.createBotProcess(Path(it), this::getBootEntry) }
    }
}
