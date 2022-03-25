package dev.robocode.tankroyale.gui.booter

import dev.robocode.tankroyale.gui.model.MessageConstants
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.ResourceUtil
import kotlinx.serialization.decodeFromString
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

object BootProcess {

    val onRunBot = Event<DirAndPid>()
    val onStopBot = Event<DirAndPid>()

    private const val JAR_FILE_NAME = "robocode-tankroyale-booter"

    private val isRunning = AtomicBoolean(false)
    private var runProcess: Process? = null

    private var thread: Thread? = null

    private val json = MessageConstants.json

    private val pidAndDirs = HashMap<Long, String>() // pid, dir

    fun info(): List<BotEntry> {
        val args = mutableListOf(
            "java",
            "-jar",
            getBooterJar(),
            "info",
        )
        val botDirs = getBotDirs().ifEmpty {
            return emptyList()
        }
        botDirs.forEach { args += it }

        val process = ProcessBuilder(args).start()
        startThread(process)
        try {
            val lines = readInputLines(process).joinToString().ifBlank {
                return emptyList()
            }
            return json.decodeFromString(lines)

        } finally {
            stopThread()
        }
    }

    fun run(botDirNames: List<String>) {
        if (isRunning.get()) {
            runBotsWithRunningBotProcess(botDirNames)
        } else {
            startRunningBotProcess(botDirNames)
        }
    }

    fun stop(pids: List<Long?>) {
        stopBotsWithRunningBotProcess(pids)
    }

    private fun startRunningBotProcess(botDirNames: List<String>) {
        val args = mutableListOf(
            "java",
            "-Dserver.url=${ServerSettings.serverUrl}",
            "-Dserver.secret=${ServerSettings.botSecrets.first()}",
            "-jar",
            getBooterJar(),
            "run"
        )
        botDirNames.forEach { args += it }

        runProcess = ProcessBuilder(args).start()

        isRunning.set(true)
        startThread(runProcess!!)
    }

    private fun runBotsWithRunningBotProcess(botDirNames: List<String>) {
        PrintStream(runProcess?.outputStream!!).also { printStream ->
            botDirNames.forEach { printStream.println("run $it") }
            printStream.flush()
        }
    }

    private fun stopBotsWithRunningBotProcess(pids: List<Long?>) {
        PrintStream(runProcess?.outputStream!!).also { printStream ->
            pids.forEach { printStream.println("stop $it") }
            printStream.flush()
        }
    }

    fun stopRunning() {
        if (!isRunning.get())
            return

        stopThread()
        isRunning.set(false)

        stopProcess()

        notifyUnbootBotProcesses()
    }

    private fun stopProcess() {
        if (runProcess?.isAlive == true) {
            // Send quit signal to server
            PrintStream(runProcess?.outputStream!!).use { printStream ->
                printStream.println("quit")
                printStream.flush()
            }
        }
        runProcess = null
    }

    private fun notifyUnbootBotProcesses() {
        pidAndDirs.forEach { onStopBot.fire(DirAndPid(it.value, it.key)) }
    }

    private fun getBooterJar(): String {
        val propertyValue = System.getProperty("booterJar")
        if (propertyValue != null) {
            val path = Paths.get(propertyValue)
            if (!Files.exists(path)) {
                throw FileNotFoundException(path.toString())
            }
            return path.toString()
        }
        val cwd = Paths.get("")
        val pathOpt = Files.list(cwd).filter { it.startsWith(JAR_FILE_NAME) && it.endsWith(".jar") }.findFirst()
        if (pathOpt.isPresent) {
            return pathOpt.get().toString()
        }
        return try {
            ResourceUtil.getResourceFile("$JAR_FILE_NAME.jar")?.absolutePath ?: ""
        } catch (ex: Exception) {
            System.err.println(ex.localizedMessage)
            ""
        }
    }

    private fun getBotDirs(): List<String> {
        return ConfigSettings.botDirectories
    }

    private fun readInputToProcessIds(process: Process) {
        val reader = BufferedReader(InputStreamReader(process.inputStream!!))
        while (thread?.isInterrupted == false) {
            val line = reader.readLine()
            if (line != null && line.isNotBlank()) {
                if (line.startsWith("stopped ")) {
                    removeProcessId(line)
                } else {
                    addProcessId(line)
                }
            }
        }
    }

    private fun readErrorToStdError(process: Process) {
        val reader = BufferedReader(InputStreamReader(process.errorStream!!))
        var line: String?
        while (run {
                line = reader.readLine()
                line
            } != null) {
            System.err.println(line)
        }
    }

    private fun readInputLines(process: Process): List<String> {
        val list = ArrayList<String>()
        val reader = BufferedReader(InputStreamReader(process.inputStream!!))
        var line: String?
        while (run {
                line = reader.readLine()
                line
            } != null) {
            list += line!!
        }
        return list
    }

    private fun startThread(process: Process) {
        thread = Thread {
            while (thread?.isInterrupted == false) {
                try {
                    readInputToProcessIds(process)
                    readErrorToStdError(process)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }
        thread?.start()
    }

    private fun stopThread() {
        thread?.interrupt()
    }

    private fun addProcessId(line: String) {
        val pidAndDir = line.split(";", limit = 2)
        if (pidAndDir.size == 2) {
            val pid = pidAndDir[0].toLong()
            val dir = pidAndDir[1]

            pidAndDirs[pid] = dir

            onRunBot.fire(DirAndPid(dir, pid))
        }
    }

    private fun removeProcessId(line: String) {
        val cmdAndPid = line.split(" ", limit = 2)
        if (cmdAndPid.size == 2) {
            val pid = cmdAndPid[1].toLong()
            val dir = pidAndDirs[pid]

            pidAndDirs.remove(pid)

            if (dir != null) {
                onStopBot.fire(DirAndPid(dir, pid))
            }
        }
    }
}
