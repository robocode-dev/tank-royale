package dev.robocode.tankroyale.runner.internal

import dev.robocode.tankroyale.common.util.JavaExec
import dev.robocode.tankroyale.common.util.ResourceUtil
import dev.robocode.tankroyale.runner.BattleException
import java.io.File
import java.io.PrintStream
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.io.path.exists

/**
 * Manages the lifecycle of the Booter process — starting bots, tracking their PIDs,
 * and shutting them down.
 *
 * This class is internal to the runner module and is not part of the public API.
 */
internal class BooterManager(
    private val serverUrl: String,
    private val botSecret: String,
) : AutoCloseable {

    private val logger = Logger.getLogger(BooterManager::class.java.name)

    private val processRef = AtomicReference<Process?>()
    private var booterJarFile: File? = null
    private var stdoutThread: Thread? = null

    /** Maps bot PID → bot directory path. Updated by stdout parsing. */
    private val bootedBots = ConcurrentHashMap<Long, String>()

    /** True if the Booter process is currently alive. */
    val isRunning: Boolean
        get() = processRef.get()?.isAlive == true

    /** Returns a snapshot of currently booted bot PIDs. */
    val botPids: Set<Long>
        get() = bootedBots.keys.toSet()

    // -------------------------------------------------------------------------------------
    // 5.1 — Booter process startup
    // -------------------------------------------------------------------------------------

    /**
     * Starts the Booter process and boots the specified bot directories.
     *
     * @param botDirs directories containing bot configurations
     * @param expectedBotCount number of bots expected to boot (for the readiness latch)
     * @param timeoutMs maximum time to wait for all bots to start
     * @throws BattleException if the Booter JAR cannot be extracted or bots fail to start
     */
    fun boot(botDirs: List<Path>, expectedBotCount: Int = botDirs.size, timeoutMs: Long = BOT_BOOT_TIMEOUT_MS) {
        if (isRunning) {
            // Booter already running — send boot commands for each directory
            botDirs.forEach { sendCommand("boot ${it.toAbsolutePath()}") }
            return
        }

        val jarPath = extractBooterJar()
        val botsReadyLatch = CountDownLatch(expectedBotCount)

        val args = mutableListOf(
            JavaExec.java(),
            "-Dserver.url=$serverUrl",
            "-Dserver.secret=$botSecret",
            "-jar",
            jarPath,
            "boot",
        )
        botDirs.forEach { args += it.toAbsolutePath().toString() }

        val process = ProcessBuilder(args).apply {
            redirectErrorStream(true)
        }.start()
        processRef.set(process)

        // Start stdout reader thread (5.2)
        startStdoutReader(process, botsReadyLatch)

        // Wait for all bots to report their PIDs
        if (!botsReadyLatch.await(timeoutMs, TimeUnit.MILLISECONDS)) {
            val booted = bootedBots.size
            logger.log(Level.WARNING, "Only $booted of $expectedBotCount bots started within ${timeoutMs}ms")
        }
    }

    // -------------------------------------------------------------------------------------
    // 5.2 — Stdout parsing
    // -------------------------------------------------------------------------------------

    private fun startStdoutReader(process: Process, botsReadyLatch: CountDownLatch) {
        val thread = Thread({
            try {
                process.inputStream.bufferedReader().use { reader ->
                    reader.forEachLine { line ->
                        parseLine(line.trim(), botsReadyLatch)
                    }
                }
            } catch (_: Exception) {
                // Stream closed — process exiting
            }
        }, "BooterManager-StdOut-Thread")
        thread.isDaemon = true
        thread.start()
        stdoutThread = thread
    }

    private fun parseLine(line: String, botsReadyLatch: CountDownLatch) {
        if (line.isBlank()) return

        when {
            // "stopped <pid>" — bot process gracefully stopped
            line.startsWith("stopped ") -> {
                val pid = line.substringAfter("stopped ").trim().toLongOrNull() ?: return
                bootedBots.remove(pid)
            }
            // "lost <pid>" — bot process no longer found
            line.startsWith("lost ") -> {
                val pid = line.substringAfter("lost ").trim().toLongOrNull() ?: return
                bootedBots.remove(pid)
            }
            // "<pid>;<directory>" — bot successfully booted
            line.contains(";") -> {
                val parts = line.split(";", limit = 2)
                if (parts.size == 2) {
                    val pid = parts[0].toLongOrNull() ?: return
                    val dir = parts[1]
                    bootedBots[pid] = dir
                    botsReadyLatch.countDown()
                }
            }
        }
    }

    // -------------------------------------------------------------------------------------
    // 5.3 — Stdin commands
    // -------------------------------------------------------------------------------------

    /**
     * Sends a command to the Booter's stdin.
     */
    private fun sendCommand(command: String) {
        val process = processRef.get() ?: return
        try {
            PrintStream(process.outputStream).also { ps ->
                ps.println(command)
                ps.flush()
            }
        } catch (e: Exception) {
            logger.log(Level.FINE, "Failed to send command to Booter: $command", e)
        }
    }

    /**
     * Boots additional bots from the specified directory.
     */
    fun bootBot(botDir: Path) {
        sendCommand("boot ${botDir.toAbsolutePath()}")
    }

    /**
     * Stops a specific bot process by PID.
     */
    fun stopBot(pid: Long) {
        sendCommand("stop $pid")
    }

    // -------------------------------------------------------------------------------------
    // 5.4 — Bot path validation
    // -------------------------------------------------------------------------------------

    companion object {
        private const val BOOTER_JAR_RESOURCE = "robocode-tankroyale-booter.jar"
        private const val GRACEFUL_EXIT_TIMEOUT_MS = 2000L
        private const val FORCE_TERMINATE_TIMEOUT_MS = 3000L
        private const val BOT_BOOT_TIMEOUT_MS = 30_000L

        /**
         * Validates that a bot directory contains a valid bot configuration file.
         * The expected format is `<dir-name>/<dir-name>.json`.
         *
         * @param botDir path to the bot directory
         * @throws BattleException if validation fails
         */
        fun validateBotDir(botDir: Path) {
            val dir = botDir.toFile()
            if (!dir.isDirectory) {
                throw BattleException("Bot path is not a directory: $botDir")
            }
            val configFile = botDir.resolve("${botDir.fileName}.json")
            if (!configFile.exists()) {
                throw BattleException(
                    "Bot directory does not contain a configuration file: $configFile"
                )
            }
        }
    }

    // -------------------------------------------------------------------------------------
    // 5.5 — Shutdown and cleanup
    // -------------------------------------------------------------------------------------

    /**
     * Shuts down the Booter process and all bot processes it manages.
     */
    override fun close() {
        val process = processRef.getAndSet(null) ?: return

        // Send quit to Booter stdin
        try {
            PrintStream(process.outputStream).use { ps ->
                ps.println("quit")
                ps.flush()
            }
        } catch (e: Exception) {
            logger.log(Level.FINE, "Failed to send quit command to Booter", e)
        }

        // Wait for graceful exit
        try {
            if (!process.waitFor(GRACEFUL_EXIT_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                process.destroyForcibly()
                process.waitFor(FORCE_TERMINATE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            }
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
            process.destroyForcibly()
        }

        // Wait for stdout thread to finish
        try {
            stdoutThread?.join(1000)
        } catch (_: InterruptedException) {
            Thread.currentThread().interrupt()
        }
        stdoutThread = null

        bootedBots.clear()

        // Clean up temp JAR file
        cleanupBooterJar()
    }

    // -------------------------------------------------------------------------------------
    // JAR extraction
    // -------------------------------------------------------------------------------------

    private fun extractBooterJar(): String {
        booterJarFile?.let { if (it.exists()) return it.absolutePath }

        val file = ResourceUtil.getResourceFile(BOOTER_JAR_RESOURCE)
            ?: throw BattleException(
                "Could not extract embedded Booter JAR from classpath resource: $BOOTER_JAR_RESOURCE"
            )

        booterJarFile = file
        return file.absolutePath
    }

    private fun cleanupBooterJar() {
        booterJarFile?.let { file ->
            try {
                if (file.exists()) file.delete()
            } catch (e: Exception) {
                logger.log(Level.FINE, "Failed to delete temp Booter JAR: ${file.absolutePath}", e)
            }
        }
        booterJarFile = null
    }
}
