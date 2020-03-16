package dev.robocode.tankroyale.ui.desktop.bootstrap

import dev.robocode.tankroyale.ui.desktop.model.MessageConstants
import dev.robocode.tankroyale.ui.desktop.settings.MiscSettings
import dev.robocode.tankroyale.ui.desktop.settings.MiscSettings.BOT_DIRS_SEPARATOR
import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.util.ResourceUtil
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.parseList
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

@UnstableDefault
@ImplicitReflectionSerializer
object BootstrapProcess {

    private const val JAR_FILE_NAME = "robocode-tankroyale-bootstrap"

    private val isRunning = AtomicBoolean(false)
    private var runProcess: Process? = null

    private var errorThread: Thread? = null
    private val errorThreadRunning = AtomicBoolean(false)

    private val json = MessageConstants.Json

    fun list(): List<BotEntry> {
        val builder = ProcessBuilder(
            "java",
            "-jar",
            getBootstrapJar(),
            "list",
            "--bot-dirs=${getBotDirs()}"
        )
        val process = builder.start()
        readErrorToStdError(process)
        val entries = readInputLines(process).joinToString()
        return json.parseList(entries)
    }

    fun run(entries: List<String>) {
        if (isRunning.get())
            stopRunning()

        val args = arrayListOf(
            "java",
            "-Dserver.url=${ServerSettings.defaultUrl}",
            "-jar",
            getBootstrapJar(),
            "run",
            "--bot-dirs=${getBotDirs()}"
        )
        entries.forEach { args += it }

        val builder = ProcessBuilder(args)
        runProcess = builder.start()

        isRunning.set(true)

        startErrorThread()
    }

    fun stopRunning() {
        if (!isRunning.get())
            return

        stopErrorThread()
        isRunning.set(false)

        val p = runProcess
        if (p != null && p.isAlive) {

            // Send quit signal to server
            val out = p.outputStream
            out.write("q\n".toByteArray())
            out.flush() // important!
        }

        runProcess = null
    }

    private fun getBootstrapJar(): String {
        val propertyValue = System.getProperty("bootstrapJar")
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
        return ResourceUtil.getResourceFile("$JAR_FILE_NAME.jar")?.absolutePath ?: ""
    }

    private fun getBotDirs(): String {
        // Use 'bots' dir from current and parent directory
        var dirs = System.getProperty("botDir", "")
        if (dirs.isNotBlank()) {
            dirs += BOT_DIRS_SEPARATOR
        }

        // Add bot directories from settings
        dirs += MiscSettings.botsDirectories.joinToString(separator = BOT_DIRS_SEPARATOR)
        return dirs.trim()
    }

    private fun readErrorToStdError(process: Process) {
        val reader = BufferedReader(InputStreamReader(process.errorStream!!))
        var line: String? = null
        while ({ line = reader.readLine(); line }() != null) {
            System.err.println(line)
        }
    }

    private fun readInputLines(process: Process): List<String> {
        val list = ArrayList<String>()
        val reader = BufferedReader(InputStreamReader(process.inputStream!!))
        var line: String? = null
        while ({ line = reader.readLine(); line }() != null) {
            list += line!!
        }
        return list
    }

    private fun startErrorThread() {
        errorThread = Thread {
            errorThreadRunning.set(true)

            while (errorThreadRunning.get()) {
                try {
                    readErrorToStdError(runProcess!!)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
        }
        errorThread?.start()
    }

    private fun stopErrorThread() {
        errorThreadRunning.set(false)
        errorThread?.interrupt()
    }
}


@UnstableDefault
@ImplicitReflectionSerializer
fun main() {
//    println(BootstrapProcess.list())

    BootstrapProcess.run(listOf("TestBot", "TestBot"))
    readLine()
    BootstrapProcess.stopRunning()
}