package dev.robocode.tankroyale.gui.booter

import dev.robocode.tankroyale.client.model.MessageConstants
import dev.robocode.tankroyale.common.Event
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.console.BooterErrorConsole
import dev.robocode.tankroyale.gui.util.EDT
import dev.robocode.tankroyale.gui.util.FileUtil
import dev.robocode.tankroyale.gui.util.LineReaderThread
import dev.robocode.tankroyale.gui.util.ResourceUtil
import dev.robocode.tankroyale.gui.util.ProcessUtil
import java.io.FileNotFoundException
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

object BootProcess {

    val onBootBot = Event<DirAndPid>()
    val onUnbootBot = Event<DirAndPid>()

    // Ensure bots are stopped if the JVM shuts down unexpectedly
    @Volatile
    private var shutdownHookRegistered = false
    private var shutdownHook: Thread? = null

    private const val JAR_FILE_NAME = "robocode-tankroyale-booter"

    private var booterProcess: Process? = null
    private var stdoutReaderRef = AtomicReference<LineReaderThread>()
    private var stderrReaderRef = AtomicReference<LineReaderThread>()

    private val json = MessageConstants.json

    private val bootedBotsList = mutableListOf<DirAndPid>()

    private val pidAndDirs = ConcurrentHashMap<Long, String>() // pid, dir

    private var pingTimer: Timer? = null

    fun info(botsOnly: Boolean? = false, teamsOnly: Boolean? = false): List<BootEntry> {
        val args = mutableListOf(
            "java",
            "-Dapp.processName=RobocodeTankRoyale-Booter",
            "-jar",
            getBooterJar(),
            "info",
            "--game-types=${ConfigSettings.gameType.displayName}"
        )
        if (botsOnly == true) {
            args += "--bots-only"
        }
        if (teamsOnly == true) {
            args += "--teams-only"
        }
        botDirs.forEach { args += it }

        val process = ProcessBuilder(args).start()
        startThread(process, false)
        try {
            val jsonStr = String(process.inputStream.readAllBytes(), StandardCharsets.UTF_8)
            return json.decodeFromString(jsonStr)
        } finally {
            stopThreads()
        }
    }

    fun boot(botDirNames: Collection<String>) {
        if (isRunning()) {
            bootBotsWithAlreadyBootedProcess(botDirNames)
        } else {
            bootBotProcess(botDirNames)
        }
        registerShutdownHookIfNeeded()
        startPinging()
    }

    fun stop() {
        if (!isRunning())
            return

        stopPinging()

        stopAllBootedBots()

        stopProcess()

        stopThreads()

        notifyUnbootBotProcesses()

        bootedBotsList.clear()

        unregisterShutdownHookIfPossible()
    }

    fun stop(pids: Collection<Long>) {
        stopBotsWithBootedProcess(pids)
    }

    private fun stopAllBootedBots() {
        // Try to gracefully stop all booted bot processes first
        val pids = pidAndDirs.keys.toList()
        if (pids.isNotEmpty()) {
            stopBotsWithBootedProcess(pids)
            // Give booter a short time to stop bots gracefully
            try {
                Thread.sleep(1000)
            } catch (_: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            // Force kill any still-alive bot processes
            pids.forEach { pid ->
                val opt = ProcessHandle.of(pid)
                val alive = opt.isEmpty || opt.get().isAlive
                if (alive) {
                    ProcessUtil.killProcessTreeByPid(pid)
                }
            }
        }
    }

    private fun registerShutdownHookIfNeeded() {
        if (shutdownHookRegistered) return
        synchronized(this) {
            if (shutdownHookRegistered) return
            val hook = Thread({
                try {
                    // Ensure all bots are stopped on JVM shutdown
                    stop()
                } catch (_: Throwable) {
                    // Ignore all exceptions during shutdown
                }
            }, "BootProcess-ShutdownHook")
            return try {
                Runtime.getRuntime().addShutdownHook(hook)
                shutdownHook = hook
                shutdownHookRegistered = true
            } catch (_: IllegalStateException) {
                // JVM is already shutting down
            } catch (_: SecurityException) {
                // Cannot install hook due to security manager
            }
        }
    }

    private fun unregisterShutdownHookIfPossible() {
        val hook = shutdownHook ?: return
        try {
            if (shutdownHookRegistered) {
                Runtime.getRuntime().removeShutdownHook(hook)
                shutdownHookRegistered = false
                shutdownHook = null
            }
        } catch (_: IllegalStateException) {
            // Ignored: JVM is shutting down
        } catch (_: SecurityException) {
            // Ignored: not permitted
        }
    }

    val bootedBots: List<DirAndPid>
        get() {
            return bootedBotsList
        }

    val botDirs: List<String>
        get() {
            return ConfigSettings.botDirectories.filter { it.enabled }.map { it.path }
        }

    private fun bootBotProcess(botDirNames: Collection<String>) {
        val args = mutableListOf(
            "java",
            "-Dapp.processName=RobocodeTankRoyale-Booter",
            "-Dserver.url=${ServerSettings.serverUrl()}",
            "-Dserver.secret=${ServerSettings.botSecret()}",
            "-jar",
            getBooterJar(),
            "boot"
        )
        botDirNames.forEach { args += it }

        booterProcess = ProcessBuilder(args).start()?.also {
            startThread(it, true)
        }
    }

    private fun bootBotsWithAlreadyBootedProcess(pathsOfBots: Collection<String>) {
        sendCommandToBootedProcess("boot", pathsOfBots)
    }

    private fun stopBotsWithBootedProcess(pids: Collection<Long>) {
        sendCommandToBootedProcess("stop", pids)
    }

    private fun ping(pids: Collection<Long>) {
        pids.forEach { pid ->

            val optProcessHandle = ProcessHandle.of(pid)

            if (optProcessHandle.isEmpty || !optProcessHandle.get().isAlive) {
                val dir = pidAndDirs[pid]
                dir?.let {
                    val dirAndPid = DirAndPid(dir, pid)

                    if (bootedBotsList.contains(dirAndPid)) {
                        bootedBotsList.remove(dirAndPid)

                        onUnbootBot.fire(dirAndPid)
                    }
                }
            }
        }
    }

    private fun sendCommandToBootedProcess(command: String, arguments: Collection<Any>) {
        booterProcess?.outputStream?.let {
            PrintStream(it).also { printStream ->
                arguments.forEach { pid -> printStream.println("$command $pid") }
                printStream.flush()
            }
        }
    }

    private fun stopProcess() {
        ProcessUtil.stopProcess(booterProcess, "quit", true, true)
        booterProcess = null
    }

    private fun notifyUnbootBotProcesses() {
        pidAndDirs.forEach { onUnbootBot.fire(DirAndPid(it.value, it.key)) }
    }

    private fun getBooterJar(): String {
        System.getProperty("booterJar")?.let {
            Paths.get(it).apply {
                if (Files.exists(this)) {
                    throw FileNotFoundException(toString())
                }
                return toString()
            }
        }

        FileUtil.findFirstInCurrentDirectory(JAR_FILE_NAME, ".jar")?.let { return it }

        return try {
            ResourceUtil.getResourceFile("${JAR_FILE_NAME}.jar")?.absolutePath ?: ""
        } catch (ex: Exception) {
            System.err.println(ex.message)
            ""
        }
    }

    private fun isValidLine(line: String?): Boolean {
        return !line.isNullOrBlank()
    }

    private fun displayErrorLine(line: String) {
        EDT.enqueue {
            showBooterConsole()
            appendLineToConsole(line)
        }
    }

    private fun showBooterConsole() {
        BooterErrorConsole.isVisible = true
    }

    private fun appendLineToConsole(line: String) {
        BooterErrorConsole.append(line + "\n")
    }

    private fun startThread(process: Process, doReadInputToProcessIds: Boolean) {
        if (isRunning()) return

        // Start reader for standard output if needed (to parse PID messages)
        if (doReadInputToProcessIds) {
            val stdoutReader = LineReaderThread(
                "BootProcess-StdOut-Thread",
                process.inputStream
            ) { line ->
                if (isValidLine(line)) {
                    if (line.startsWith("stopped ")) {
                        removePid(line)
                    } else {
                        addPid(line)
                    }
                }
            }
            stdoutReaderRef.set(stdoutReader)
            stdoutReader.start()
        }

        // Always start reader for standard error (to show error console output)
        val stderrReader = LineReaderThread(
            "BootProcess-StdErr-Thread",
            process.errorStream
        ) { line ->
            if (isValidLine(line)) {
                displayErrorLine(line)
            }
        }
        stderrReaderRef.set(stderrReader)
        stderrReader.start()
    }

    private fun stopThreads() {
        stdoutReaderRef.get()?.stop()
        stderrReaderRef.get()?.stop()
    }

    private fun isRunning(): Boolean =
        (stdoutReaderRef.get()?.isRunning() ?: false) ||
                (stderrReaderRef.get()?.isRunning() ?: false)

    private fun startPinging() {
        val pingTask = object : TimerTask() {
            override fun run() {
                ping(pidAndDirs.keys)
            }
        }
        pingTimer = Timer().apply {
            scheduleAtFixedRate(pingTask, Date(), 1000L)
        }
    }

    private fun stopPinging() {
        pingTimer?.cancel()
    }

    private fun addPid(line: String) {
        val pidAndDir = line.split(";", limit = 2)
        if (pidAndDir.size == 2) {
            val pid = pidAndDir[0].toLong()
            val dir = pidAndDir[1]

            pidAndDirs[pid] = dir

            val dirAndPid = DirAndPid(dir, pid)
            bootedBotsList.add(dirAndPid)

            onBootBot.fire(dirAndPid)
        }
    }

    private fun removePid(line: String) {
        val actionAndPid = line.split(" ", limit = 2)
        if (actionAndPid.size == 2) {
            val pid = actionAndPid[1].toLong()
            val dir = pidAndDirs[pid]

            pidAndDirs.remove(pid)

            if (dir != null) {
                val dirAndPid = DirAndPid(dir, pid)
                bootedBotsList.remove(dirAndPid)

                onUnbootBot.fire(dirAndPid)
            }
        }
    }
}
