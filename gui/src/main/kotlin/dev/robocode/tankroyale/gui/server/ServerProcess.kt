package dev.robocode.tankroyale.gui.server

import dev.robocode.tankroyale.common.util.JavaExec
import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.server.ServerActions
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import dev.robocode.tankroyale.gui.ui.server.ServerLogFrame
import dev.robocode.tankroyale.gui.util.EDT
import dev.robocode.tankroyale.gui.util.FileUtil
import dev.robocode.tankroyale.gui.util.ResourceUtil
import dev.robocode.tankroyale.gui.util.ProcessUtil
import dev.robocode.tankroyale.gui.util.LineReaderThread
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicReference

object ServerProcess {

    private const val JAR_FILE_NAME = "robocode-tankroyale-server"

    private var processRef = AtomicReference<Process?>()
    private var logReaderRef = AtomicReference<LineReaderThread?>()

    init {
        @Suppress("UnusedExpression")
        ServerActions // trigger initialization of ServerEvents
    }

    fun isRunning(): Boolean {
        val process = processRef.get()
        return process != null && process.isAlive
    }

    fun start() {
        if (isRunning()) return

        var command: MutableList<String>
        ServerSettings.apply {
            command = mutableListOf(
                JavaExec.java(),
                "-Dapp.processName=RobocodeTankRoyale-Server",
                "-jar",
                getServerJar(),
                "--port=${localPort}",
                "--games=classic,melee,1v1",
                "--tps=${ConfigSettings.tps}",
                "--controller-secrets=${controllerSecrets.joinToString(",")}",
                "--bot-secrets=${botSecrets.joinToString(",")}"
            )
            if (initialPositionsEnabled) {
                command += "--enable-initial-position"
            }
        }
        ProcessBuilder(command).apply {
            redirectErrorStream(true)
            val process = start()
            processRef.set(process)
        }

        startLogThread()

        ServerEvents.onStarted(Unit)
    }

    fun stop() {
        if (!isRunning()) return

        // Stop the server process first to close its stdout (lets the log thread exit)
        val process = processRef.get()
        ProcessUtil.stopProcess(process, "quit", true, true)
        processRef.set(null)

        // Now stop the log thread if still running
        stopLogThread()

        ServerEvents.onStopped(Unit)
    }

    private fun getServerJar(): String {
        System.getProperty("serverJar")?.let {
            Paths.get(it).apply {
                if (!Files.exists(this)) {
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

    private fun startLogThread() {
        val process = processRef.get() ?: return
        val logReader = LineReaderThread(
            "ServerProcess-Log-Thread",
            process.inputStream
        ) { line ->
            // Forward raw ANSI to the GUI console; AnsiEditorPane will render colors
            EDT.enqueue { ServerLogFrame.append(line + "\n") }
        }
        logReaderRef.set(logReader)
        logReader.start()
    }

    private fun stopLogThread() {
        logReaderRef.get()?.stop()
        logReaderRef.set(null)
    }

    // Deprecated: kept for reference if we need no-color fallback later
    @Suppress("unused")
    private fun stripAnsi(s: String): String {
        val ansiRegex = Regex("\u001B\\[[0-9;?]*[ -/]*[@-~]")
        val escRegex = Regex("\u001B[@-_]")
        return s.replace(ansiRegex, "").replace(escRegex, "")
    }
}
