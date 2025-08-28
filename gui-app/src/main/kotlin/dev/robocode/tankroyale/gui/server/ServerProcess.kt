package dev.robocode.tankroyale.gui.server

import dev.robocode.tankroyale.gui.settings.ConfigSettings
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.server.ServerActions
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import dev.robocode.tankroyale.gui.ui.server.ServerLogFrame
import dev.robocode.tankroyale.gui.util.EDT
import dev.robocode.tankroyale.gui.util.FileUtil
import dev.robocode.tankroyale.gui.util.ResourceUtil
import dev.robocode.tankroyale.gui.util.ProcessUtil
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

object ServerProcess {

    private const val JAR_FILE_NAME = "robocode-tankroyale-server"

    private var processRef = AtomicReference<Process?>()
    private var logThread: Thread? = null
    private val logThreadRunning = AtomicBoolean(false)

    init {
        ServerActions // trigger initialization of ServerEvents. Is not "unused"
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
                "java",
                "-Dpicocli.ansi=true", // to show server logo in ANSI colors
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

        ServerEvents.onStarted.fire(Unit)
    }

    fun stop() {
        if (!isRunning()) return

        // Stop the server process first to close its stdout (lets the log thread exit)
        val process = processRef.get()
        ProcessUtil.stopProcess(process, "q", true, true)
        processRef.set(null)

        // Now stop the log thread if still running
        stopLogThread()
        logThread = null

        ServerEvents.onStopped.fire(Unit)
    }

    private fun getServerJar(): String {
        System.getProperty("serverJar")?.let {
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

    private fun startLogThread() {
        logThread = Thread {
            logThreadRunning.set(true)

            val process = processRef.get()
            BufferedReader(InputStreamReader(process?.inputStream!!)).use { reader ->
                try {
                    while (logThreadRunning.get()) {
                        val line = reader.readLine() ?: break
                        EDT.enqueue { ServerLogFrame.append(line + "\n") }
                    }
                } catch (_: Exception) {
                    // Ignore; occurs when process closes stream or thread is interrupted
                } finally {
                    logThreadRunning.set(false)
                }
            }
        }.apply {
            name = "ServerProcess-Log-Thread"
            start()
        }
    }

    private fun stopLogThread() {
        logThreadRunning.set(false)
        logThread?.interrupt()
    }
}

fun main() {
    ServerProcess.start()
    println("Server started")
    System.`in`.read()
    ServerProcess.stop()
    println("Server stopped ")
}