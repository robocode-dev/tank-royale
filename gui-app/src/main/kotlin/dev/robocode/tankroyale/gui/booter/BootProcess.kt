package dev.robocode.tankroyale.gui.booter

import dev.robocode.tankroyale.gui.model.MessageConstants
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.ResourceUtil
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

object BootProcess {

    val onBootBot = Event<DirAndPid>()
    val onUnbootBot = Event<DirAndPid>()

    private const val JAR_FILE_NAME = "robocode-tankroyale-booter"

    private val isBooted = AtomicBoolean(false)
    private var booterProcess: Process? = null
    private var thread: Thread? = null

    private val json = MessageConstants.json

    private val bootedBotsList = mutableListOf<DirAndPid>()

    private val pidAndDirs = ConcurrentHashMap<Long, String>() // pid, dir

    private var pingTimer: Timer? = null

    fun info(botsOnly: Boolean? = false, teamsOnly: Boolean? = false): List<BootEntry> {
        val args = mutableListOf(
            "java",
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
            stopThread()
        }
    }

    fun boot(botDirNames: Collection<String>) {
        if (isBooted.get()) {
            bootBotsWithAlreadyBootedProcess(botDirNames)
        } else {
            bootBotProcess(botDirNames)
        }
        startPinging()
    }

    fun stop(pids: Collection<Long>) {
        stopBotsWithBootedProcess(pids)
    }

    fun ping(pids: Collection<Long>) {
        pingBotsWithBootedProcess(pids)
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
            "-Dserver.url=${ServerSettings.serverUrl()}",
            "-Dserver.secret=${ServerSettings.botSecret}",
            "-jar",
            getBooterJar(),
            "boot"
        )
        botDirNames.forEach { args += it }

        booterProcess = ProcessBuilder(args).start()?.also {
            startThread(it, true)
            isBooted.set(true)
        }
    }

    private fun bootBotsWithAlreadyBootedProcess(pathsOfBots: Collection<String>) {
        sendCommandToBootedProcess("boot", pathsOfBots)
    }

    private fun stopBotsWithBootedProcess(pids: Collection<Long>) {
        sendCommandToBootedProcess("stop", pids)
    }

    private fun pingBotsWithBootedProcess(pids: Collection<Long>) {
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
            PrintStream(it).also {  printStream ->
                arguments.forEach { pid -> printStream.println("$command $pid") }
                printStream.flush()
            }
        }
    }

    fun stop() {
        if (!isBooted.get())
            return

        stopThread()
        stopPinging()

        isBooted.set(false)

        stopProcess()

        notifyUnbootBotProcesses()

        bootedBotsList.clear()
    }

    private fun stopProcess() {
        booterProcess?.apply {
            if (isAlive) {
                PrintStream(outputStream).apply {
                    println("quit")
                    flush()
                }
            }
        }
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
        Paths.get("").apply {
            Files.list(this).filter { it.startsWith(JAR_FILE_NAME) && it.endsWith(".jar") }.findFirst().apply {
                if (isPresent) {
                    return get().toString()
                }
            }
        }
        return try {
            ResourceUtil.getResourceFile("${JAR_FILE_NAME}.jar")?.absolutePath ?: ""
        } catch (ex: Exception) {
            System.err.println(ex.message)
            ""
        }
    }

    private fun readInputToPids(process: Process) {
        process.inputStream?.let {
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            while (thread?.isInterrupted == false) {
                val line = reader.readLine()
                if (line != null && line.isNotBlank()) {
                    if (line.startsWith("stopped ")) {
                        removePid(line)
                    } else {
                        addPid(line)
                    }
                }
            }
        }
    }

    private fun readErrorToStdError(process: Process) {
        val reader = BufferedReader(InputStreamReader(process.errorStream!!, StandardCharsets.UTF_8))
        var line: String?
        while (run {
                line = reader.readLine()
                line
            } != null) {
            System.err.println(line)
        }
    }

    private fun startThread(process: Process, doReadInputToProcessIds: Boolean) {
        thread = Thread {
            while (thread?.isInterrupted == false) {
                try {
                    if (doReadInputToProcessIds)
                        readInputToPids(process)
                    readErrorToStdError(process)
                } catch (_: InterruptedException) {
                    break
                }
            }
        }.apply { start() }
    }

    private fun stopThread() {
        thread?.interrupt()
    }

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
