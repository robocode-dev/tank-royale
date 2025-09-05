package dev.robocode.tankroyale.gui.util

import java.io.PrintStream
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

object ProcessUtil {
    private val logger = Logger.getLogger(ProcessUtil::class.java.name)

    // Timeout constants
    private const val GRACEFUL_EXIT_TIMEOUT_MS = 2000L
    private const val FORCE_TERMINATE_TIMEOUT_MS = 3 * GRACEFUL_EXIT_TIMEOUT_MS

    private val isWindows: Boolean = System.getProperty("os.name").lowercase().contains("win")

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

        // Send quit command and close stdin to signal EOF
        val quitCommandSent = sendQuitCommand(process, quitCommand)

        if (!waitForExit) {
            return quitCommandSent
        }

        // Wait for the process to exit gracefully
        try {
            if (process.waitFor(GRACEFUL_EXIT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                return true
            }

            if (forceTerminate) {
                val ok = terminateProcessTree(process)
                if (!ok && isWindows) {
                    // Fallback: use taskkill to force kill the process tree
                    return taskKillTree(process.pid())
                }
                return ok
            }
            return false
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            logger.log(Level.WARNING, "Thread interrupted while waiting for process to exit")
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed while waiting for process to exit", e)
        }

        return !process.isAlive
    }

    /**
     * Sends a quit command to the process.
     *
     * @param process The process to send the command to
     * @param quitCommand The command to send
     * @return True if the command was sent successfully
     */
    private fun sendQuitCommand(process: Process, quitCommand: String): Boolean {
        return try {
            PrintStream(process.outputStream).use { ps ->
                ps.println(quitCommand)
                ps.flush()
                true
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed to send quit command to process", e)
            false
        }
    }

    /**
     * Public helper to kill a process tree by PID. Best effort with Windows fallback.
     */
    @JvmStatic
    fun killProcessTreeByPid(pid: Long): Boolean {
        // Try with Java APIs first
        val handleOpt = ProcessHandle.of(pid)
        if (handleOpt.isPresent) {
            val handle = handleOpt.get()
            try {
                handle.descendants().forEach { child ->
                    try { child.destroy() } catch (_: Exception) { }
                }
                if (handle.isAlive) {
                    handle.destroy()
                }
                if (handle.isAlive) {
                    handle.descendants().forEach { child ->
                        if (child.isAlive) {
                            try { child.destroyForcibly() } catch (_: Exception) { }
                        }
                    }
                    if (handle.isAlive) {
                        handle.destroyForcibly()
                    }
                }
                if (!handle.isAlive) return true
            } catch (_: Exception) {
                // fall through to taskkill
            }
        }
        // Windows hard kill fallback
        return if (isWindows) taskKillTree(pid) else false
    }

    /**
     * Terminates a process tree gracefully first, then forcibly if needed.
     *
     * @param process The root process to terminate
     * @return True if the process was terminated successfully
     */
    private fun terminateProcessTree(process: Process): Boolean {
        // First, attempt to terminate the process tree gracefully
        try {
            val handle = process.toHandle()
            handle.descendants().forEach { child ->
                try { child.destroy() } catch (_: Exception) { }
            }

            // Give children a brief moment to exit
            if (process.isAlive && !process.waitFor(GRACEFUL_EXIT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                // If still alive, forcibly terminate the process tree
                handle.descendants().forEach { child ->
                    if (child.isAlive) {
                        try { child.destroyForcibly() } catch (_: Exception) { }
                    }
                }

                if (process.isAlive) {
                    process.destroyForcibly()
                }
            }

            // Wait for final termination
            return process.waitFor(FORCE_TERMINATE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed to terminate process tree", e)

            // Last resort: try to terminate just the main process
            try {
                if (process.isAlive) {
                    process.destroyForcibly()
                    return process.waitFor(FORCE_TERMINATE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                }
            } catch (_: Exception) { }

            return !process.isAlive
        }
    }

    private fun taskKillTree(pid: Long): Boolean {
        return try {
            val pb = ProcessBuilder("taskkill", "/PID", pid.toString(), "/T", "/F")
            val proc = pb.start()
            val finished = proc.waitFor(FORCE_TERMINATE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            finished && proc.exitValue() == 0
        } catch (e: Exception) {
            logger.log(Level.WARNING, "taskkill failed for PID $pid", e)
            false
        }
    }
}
