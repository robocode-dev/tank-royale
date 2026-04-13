package dev.robocode.tankroyale.booter.process

import dev.robocode.tankroyale.booter.model.BootEntry
import dev.robocode.tankroyale.booter.util.Log
import dev.robocode.tankroyale.common.util.Platform.isWindows
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentSkipListMap
import kotlin.io.path.absolutePathString

/**
 * Manages the lifecycle of running bot processes.
 *
 * Thread-safety: [processes] is a [ConcurrentSkipListMap], so all public methods are safe
 * to call from multiple threads concurrently — specifically the main thread, the JVM
 * shutdown-hook thread registered in [registerShutdownHook], and [CompletableFuture] callbacks
 * created by [captureProcessErrorOutput] and [registerProcessCleanup].
 *
 * @param bootMaker Handles bot and team boot orchestration. Defaults to a [BotBooter] with
 *   standard dependencies.
 */
internal class ProcessManager(private val bootMaker: BotBooter = BotBooter()) {

    private val processes = ConcurrentSkipListMap<Pid, Process>()

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
        bootMaker.boot(bootDir, getBootEntry).forEach { (process, botDir) ->
            registerProcess(process, botDir)
            captureProcessErrorOutput(process, botDir)
        }
    }

    /**
     * Stop a bot process by its process ID.
     */
    fun stopBotProcess(pid: Pid) {
        val process = processes[pid] ?: run {
            println("lost $pid")
            return
        }

        stopProcess(process)
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
        }.exceptionally { Log.error(it, botDir); null }
    }
}

typealias Pid = Long
typealias TeamId = Long
