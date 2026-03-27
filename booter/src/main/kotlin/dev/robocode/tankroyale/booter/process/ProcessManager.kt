package dev.robocode.tankroyale.booter.process

import dev.robocode.tankroyale.booter.model.BootEntry
import dev.robocode.tankroyale.booter.util.Log
import dev.robocode.tankroyale.common.util.Platform.isWindows
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.atomic.AtomicLong
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists

/**
 * Manages the lifecycle of running bot processes.
 *
 * Thread-safety: [processes] is a [ConcurrentSkipListMap] and [teamId] is an [AtomicLong],
 * so all public methods are safe to call from multiple threads concurrently — specifically
 * the main thread, the JVM shutdown-hook thread registered in [registerShutdownHook], and
 * [CompletableFuture] callbacks created by [captureProcessErrorOutput] and [registerProcessCleanup].
 */
class ProcessManager {

    private val processes = ConcurrentSkipListMap<Pid, Process>()
    private val teamId = AtomicLong(1)

    /**
     * Register a shutdown hook to clean up processes when JVM exits.
     */
    fun registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(Thread { killAllProcesses() })
    }

    /**
     * Create processes for a bot or team located at the specified path.
     */
    fun createBotProcess(bootDir: Path, getBootEntry: (Path) -> BootEntry?) {
        if (!isValidBotDirectory(bootDir)) {
            return
        }
        boot(bootDir, getBootEntry).forEach { process ->
            processes[process.pid()] = process
        }
    }

    /**
     * Validates if a path is a valid bot directory.
     * @return true if the directory exists and is a directory, false otherwise
     */
    private fun isValidBotDirectory(botDir: Path): Boolean {
        if (!botDir.exists()) {
            Log.error(Exception("Bot directory not found"), botDir)
            return false
        }
        if (!Files.isDirectory(botDir)) {
            Log.error("Path is not a valid bot directory", botDir)
            return false
        }
        return true
    }

    /**
     * Stop a bot process by its process ID.
     * @return true if the process was found and stopped, false otherwise
     */
    fun stopBotProcess(pid: Pid): Boolean {
        val process = processes[pid] ?: run {
            println("lost $pid")
            return false
        }

        stopProcess(process)
        return true
    }

    /**
     * Kill all running processes.
     */
    fun killAllProcesses() {
        processes.values.parallelStream().forEach { stopProcess(it) }
    }

    /**
     * Stop a specific process and remove it from the process map.
     */
    private fun stopProcess(process: Process) {
        val pid = process.pid()

        registerProcessCleanup(process, pid)
        terminateProcessTree(process)
        verifyAndForceTermination(process, pid)
    }

    private fun registerProcessCleanup(process: Process, pid: Long) {
        process.onExit().thenAccept {
            processes.remove(pid)
            println("stopped $pid")
        }
    }

    private fun terminateProcessTree(process: Process) {
        terminateDescendants(process)
        gracefullyTerminateProcess(process)
        forceTerminateRemainingProcesses(process)
    }

    private fun terminateDescendants(process: Process) {
        runCatching {
            process.toHandle().descendants().forEach { child ->
                runCatching { child.destroy() }.onFailure { Log.error(it) }
            }
        }.onFailure { Log.error(it) }
    }

    private fun gracefullyTerminateProcess(process: Process) {
        runCatching { process.destroy() }.onFailure { Log.error(it) }
    }

    private fun forceTerminateRemainingProcesses(process: Process) {
        runCatching {
            process.toHandle().descendants().forEach { child ->
                if (child.isAlive) {
                    runCatching { child.destroyForcibly() }.onFailure { Log.error(it) }
                }
            }
        }.onFailure { Log.error(it) }

        runCatching {
            if (process.isAlive) {
                process.destroyForcibly()
            }
        }.onFailure { Log.error(it) }
    }

    private fun verifyAndForceTermination(process: Process, pid: Long) {
        if (isProcessStillAlive(process) && isWindows) {
            runTaskKillTree(pid)
        }
    }

    private fun isProcessStillAlive(process: Process): Boolean {
        return runCatching {
            process.toHandle().isAlive
        }.getOrDefault(true)
    }

    /**
     * Uses Windows taskkill to terminate a process tree as a last resort.
     */
    private fun runTaskKillTree(pid: Long) {
        try {
            ProcessBuilder("taskkill", "/PID", pid.toString(), "/T", "/F")
                .redirectErrorStream(true)
                .start()
                .waitFor()
        } catch (ex: Exception) {
            Log.error(ex)
        }
    }

    // BOT BOOTING

    /**
     * Boot either a single bot or a team from the specified directory.
     * Returns a set of started processes.
     */
    private fun boot(bootDir: Path, getBootEntry: (Path) -> BootEntry?): Set<Process> {
        try {
            val bootEntry = getBootEntry(bootDir) ?: run {
                Log.error("No valid boot entry found", bootDir)
                return emptySet()
            }

            if (isTeamEntry(bootEntry)) {
                val team = createTeam(bootEntry) ?: run {
                    Log.error("Team entry has no members", bootDir)
                    return emptySet()
                }
                return bootTeam(bootDir, team, getBootEntry)
            }

            return bootSingleBot(bootDir, getBootEntry)
        } catch (ex: Exception) {
            Log.error(ex, bootDir)
            return emptySet()
        }
    }

    private fun isTeamEntry(bootEntry: BootEntry): Boolean =
        bootEntry.teamMembers?.isNotEmpty() == true

    private fun createTeam(bootEntry: BootEntry): Team? {
        val members = bootEntry.teamMembers ?: return null
        return Team(teamId.getAndIncrement(), bootEntry.name, bootEntry.version, members)
    }

    private fun bootSingleBot(bootDir: Path, getBootEntry: (Path) -> BootEntry?): Set<Process> {
        bootBot(bootDir, null, getBootEntry)?.let { return setOf(it) }
        Log.error("Failed to boot bot - no suitable boot method found", bootDir)
        return emptySet()
    }

    /**
     * Boot a team of bots with the specified team information.
     * Returns a set of started processes.
     */
    private fun bootTeam(bootDir: Path, team: Team, getBootEntry: (Path) -> BootEntry?): Set<Process> {
        val parentPath = bootDir.parent
        val botProcesses = HashSet<Process>()

        try {
            team.members.forEach { botName ->
                bootTeamMember(parentPath, botName, team, getBootEntry, botProcesses)
            }
            return botProcesses
        } catch (ex: Exception) {
            Log.error(ex, bootDir)
            return botProcesses
        }
    }

    private fun bootTeamMember(
        parentPath: Path,
        botName: String,
        team: Team,
        getBootEntry: (Path) -> BootEntry?,
        botProcesses: MutableSet<Process>
    ) {
        try {
            val botDir = parentPath.resolve(botName)

            if (!isValidBotDirectory(botDir)) {
                return
            }

            findBootScriptOrNull(botDir)?.let {
                bootBot(botDir, team, getBootEntry)?.let { process ->
                    botProcesses.add(process)
                } ?: run {
                    Log.error("Failed to boot team member bot", botDir)
                }
            }
        } catch (ex: Exception) {
            val botDir = parentPath.resolve(botName)
            Log.error(ex, botDir)
        }
    }

    /**
     * Boot a single bot with optional team information.
     * Returns the started process or null if booting failed.
     */
    private fun bootBot(botDir: Path, team: Team? = null, getBootEntry: (Path) -> BootEntry?): Process? {
        try {
            val botEntry = getBootEntry(botDir) ?: run {
                Log.error("Failed to get boot entry for bot", botDir)
                return null
            }

            val scriptPath = findBootScriptOrNull(botDir) ?: return null

            return createAndStartBotProcess(scriptPath, botDir, botEntry, team)
        } catch (ex: Exception) {
            Log.error(ex, botDir)
            return null
        }
    }

    private fun createAndStartBotProcess(
        scriptPath: Path,
        botDir: Path,
        botEntry: BootEntry,
        team: Team?
    ): Process? {
        try {
            val processBuilder = ProcessLauncher.createProcessBuilder(scriptPath.toString())
            processBuilder.directory(scriptPath.parent.toFile())

            // Bug fix #188:
            // Discard stdout from bot processes. Bot APIs (Java, .NET, Python) write to the OS stdout pipe
            // AND capture to an in-memory buffer for delivery via WebSocket. If nothing reads the OS pipe,
            // the pipe buffer fills up (~4 KB) and writes block, freezing the bot thread.
            // The GUI already receives bot stdout via WebSocket (BotIntent.stdOut), so the raw pipe is redundant.
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD)

            BotEnvironment.setup(processBuilder.environment(), botEntry, team)

            return startProcess(processBuilder, botDir)
        } catch (ex: Exception) {
            Log.error(ex, botDir)
            return null
        }
    }

    /**
     * Start a process from the given process builder, register it, and capture its stderr.
     *
     * @param processBuilder The ProcessBuilder to start the process from
     * @param botDir The directory of the bot being started
     * @return The started process
     * @throws Exception if the process could not be started
     */
    private fun startProcess(processBuilder: ProcessBuilder, botDir: Path): Process {
        try {
            val process = processBuilder.start()
            registerProcess(process, botDir)
            captureProcessErrorOutput(process, botDir)
            return process
        } catch (ex: Exception) {
            Log.error(ex, botDir)
            throw ex
        }
    }

    private fun registerProcess(process: Process, botDir: Path) {
        val pid = process.pid()
        println("$pid;${botDir.absolutePathString()}")
        processes[pid] = process
    }

    /**
     * Captures stderr from a bot process line-by-line, logging each line as it arrives.
     * Uses the common ForkJoinPool to avoid raw thread creation.
     */
    private fun captureProcessErrorOutput(process: Process, botDir: Path) {
        CompletableFuture.runAsync {
            process.errorStream.bufferedReader().use { reader ->
                reader.forEachLine { line -> Log.error(line, botDir) }
            }
        }
    }

    // SCRIPT FINDING

    /**
     * Validates the bot directory and finds its boot script.
     * Logs an error if no script is found.
     */
    private fun findBootScriptOrNull(botDir: Path): Path? {
        if (!isValidBotDirectory(botDir)) return null
        val scriptPath = ScriptFinder.findScript(botDir)
        if (scriptPath == null) {
            Log.error("No script found within the bot directory", botDir)
        }
        return scriptPath
    }
}

typealias Pid = Long
typealias TeamId = Long


