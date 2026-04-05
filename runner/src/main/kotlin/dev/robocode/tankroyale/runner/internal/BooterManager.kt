package dev.robocode.tankroyale.runner.internal

import dev.robocode.tankroyale.common.util.JavaExec
import dev.robocode.tankroyale.common.util.ResourceUtil
import dev.robocode.tankroyale.runner.BattleException
import dev.robocode.tankroyale.runner.BotIdentity
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
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
    private val captureOutput: Boolean = true,
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
                        if (captureOutput) logger.info("[BOOTER] $line")
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
                return // Optional .json is fine for non-teams
            }
            // Validate team member directories if this is a team config
            val json = try {
                Json.parseToJsonElement(configFile.toFile().readText()).jsonObject
            } catch (_: Exception) {
                return // malformed JSON will be caught later by readBotIdentities
            }
            val teamMembers = json["teamMembers"]?.jsonArray ?: return
            val parentDir = botDir.parent
            for (memberElement in teamMembers) {
                val memberName = try {
                    memberElement.jsonPrimitive.content
                } catch (_: Exception) {
                    continue
                }
                val memberDir = parentDir.resolve(memberName)
                if (!memberDir.toFile().isDirectory) {
                    throw BattleException(
                        "Team member directory not found: $memberName (expected at $memberDir)"
                    )
                }
            }
        }

        /**
         * Reads the bot identities from a bot or team directory.
         *
         * For a regular bot directory, parses `<dir>/<dir>.json` and returns a single
         * [BotIdentity]. For a team directory (JSON contains a `teamMembers` array), resolves
         * each member to a sibling directory and returns one [BotIdentity] per member,
         * preserving duplicates.
         *
         * @param botDir path to the bot or team directory
         * @return list of [BotIdentity] instances (one per expected bot connection)
         * @throws BattleException if any JSON file is missing or malformed
         */
        fun readBotIdentities(botDir: Path): List<BotIdentity> {
            val configFile = botDir.resolve("${botDir.fileName}.json")
            if (!configFile.exists()) {
                // If no .json is present, we assume it's a config-less bot.
                // We return an empty identity list because we can't know the name/version yet.
                // The BattleRunner will wait for ANY bot to connect instead of matching identities.
                return emptyList()
            }
            val json = try {
                Json.parseToJsonElement(configFile.toFile().readText()).jsonObject
            } catch (e: Exception) {
                throw BattleException("Malformed bot configuration file: $configFile", e)
            }

            // Team directory: expand teamMembers into individual identities
            val teamMembers = json["teamMembers"]?.jsonArray
            if (teamMembers != null) {
                val parentDir = botDir.parent
                return teamMembers.map { memberElement ->
                    val memberName = try {
                        memberElement.jsonPrimitive.content
                    } catch (e: Exception) {
                        throw BattleException("Malformed teamMembers entry in: $configFile", e)
                    }
                    val memberDir = parentDir.resolve(memberName)
                    val memberConfig = memberDir.resolve("$memberName.json")
                    if (!memberConfig.exists()) {
                        throw BattleException(
                            "Team member configuration file not found: $memberConfig"
                        )
                    }
                    parseBotIdentity(memberConfig.toFile().readText(), memberConfig.toString())
                }
            }

            // Regular bot directory
            return listOf(parseBotIdentity(configFile.toFile().readText(), configFile.toString()))
        }

        private fun parseBotIdentity(jsonText: String, sourcePath: String): BotIdentity {
            val obj = try {
                Json.parseToJsonElement(jsonText).jsonObject
            } catch (e: Exception) {
                throw BattleException("Malformed bot configuration file: $sourcePath", e)
            }
            val name = obj["name"]?.jsonPrimitive?.content
                ?: throw BattleException("Missing 'name' field in bot configuration: $sourcePath")
            val version = obj["version"]?.jsonPrimitive?.content
                ?: throw BattleException("Missing 'version' field in bot configuration: $sourcePath")

            val authorsElement = obj["authors"]
            val authors = when {
                authorsElement == null -> throw BattleException("Missing 'authors' field in bot configuration: $sourcePath")
                authorsElement is JsonArray -> authorsElement.joinToString(", ") { it.jsonPrimitive.content }
                else -> authorsElement.jsonPrimitive.content
            }

            return BotIdentity(name, version, authors)
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

        // Only track temp files for cleanup; filesystem resources must not be deleted
        if (file.absolutePath.startsWith(System.getProperty("java.io.tmpdir"))) {
            booterJarFile = file
        }
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
