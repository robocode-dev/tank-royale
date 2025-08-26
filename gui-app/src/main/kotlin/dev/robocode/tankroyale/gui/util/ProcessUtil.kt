package dev.robocode.tankroyale.gui.util

import java.io.PrintStream
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

object ProcessUtil {
    private val logger = Logger.getLogger(ProcessUtil::class.java.name)

    /**
     * Sends a quit command to the given process and optionally waits for it to exit.
     *
     * @param process The process to stop.
     * @param quitCommand The command string to send to the process' standard input to request shutdown.
     * @param waitForExit If true, waits for the process to terminate after sending the command.
     * @param forceTerminate If true, forcibly terminates the process if it doesn't exit within the timeout.
     * @return True, if the process was successfully stopped, false otherwise.
     */
    @JvmStatic
    @JvmOverloads
    fun stopProcess(
        process: Process?, 
        quitCommand: String, 
        waitForExit: Boolean = false,
        forceTerminate: Boolean = false
    ): Boolean {
        if (process == null || !process.isAlive) {
            return true // Nothing to do, a process is already stopped
        }

        // Send quit command
        var quitCommandSent = false
        try {
            PrintStream(process.outputStream).use { ps ->
                ps.println(quitCommand)
                ps.flush()
                quitCommandSent = true
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed to send quit command to process", e)
        }

        // If we don't need to wait, return whether the command was sent
        if (!waitForExit) {
            return quitCommandSent
        }

        // Wait for a process to exit
        try {
            val exited = process.waitFor(500, TimeUnit.MILLISECONDS)

            // If a process didn't exit and force termination is requested
            if (!exited && forceTerminate) {
                process.destroyForcibly()
                return process.waitFor(1000, TimeUnit.MILLISECONDS) // Give it 3 more seconds to terminate
            }
            return exited

        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            logger.log(Level.WARNING, "Thread interrupted while waiting for process to exit")
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed while waiting for process to exit", e)
        }

        return !process.isAlive
    }
}
