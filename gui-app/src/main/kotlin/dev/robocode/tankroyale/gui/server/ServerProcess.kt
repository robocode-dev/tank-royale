package dev.robocode.tankroyale.gui.server

import dev.robocode.tankroyale.gui.settings.GameType
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.server.ServerActions
import dev.robocode.tankroyale.gui.ui.server.ServerEvents
import dev.robocode.tankroyale.gui.ui.server.ServerLogWindow
import dev.robocode.tankroyale.gui.util.ResourceUtil
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object ServerProcess {

    private const val JAR_FILE_NAME = "robocode-tankroyale-server"

    private val isRunning = AtomicBoolean(false)
    private var process: Process? = null
    private var logThread: Thread? = null
    private val logThreadRunning = AtomicBoolean(false)

    var gameType: GameType = GameType.CLASSIC
        private set

    var port: Int = ServerSettings.serverPort
        private set

    init {
        ServerActions
    }

    fun isRunning(): Boolean = isRunning.get()

    fun start(gameType: GameType = GameType.CLASSIC, port: Int = ServerSettings.serverPort) {
        if (isRunning.get())
            return

        this.gameType = gameType
        this.port = port

        var command: MutableList<String>
        with(ServerSettings) {
            command = mutableListOf(
                "java",
                "-jar",
                getServerJar(),
                "--port=$port",
                "--games=$gameType",
                "--controllerSecrets=${controllerSecrets.joinToString(",")}",
                "--botSecrets=${botSecrets.joinToString(",")}"
            )
            if (initialPositionsEnabled) {
                command += "--enable-initial-position"
            }
        }
        val builder = ProcessBuilder(command)
        builder.redirectErrorStream(true)
        process = builder.start()

        isRunning.set(true)

        startLogThread()

        ServerEvents.onStarted.fire(Unit)
    }

    fun stop() {
        if (!isRunning.get())
            return
        isRunning.set(false)

        stopLogThread()

        process?.let { process ->
            if (process.isAlive) {
                process.outputStream.apply {
                    write("quit\n".toByteArray())
                    flush()
                }
                process.waitFor(1, TimeUnit.SECONDS)
            }
        }

        process = null
        logThread = null

        ServerEvents.onStopped.fire(Unit)
    }

    fun restart() {
        stop()
        start(gameType, port)
    }

    private fun getServerJar(): String {
        val propertyValue = System.getProperty("serverJar")
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
            ResourceUtil.getResourceFile("${JAR_FILE_NAME}.jar")?.absolutePath ?: ""
        } catch (ex: Exception) {
            System.err.println(ex.message)
            ""
        }
    }

    private fun startLogThread() {
        logThread = Thread {
            logThreadRunning.set(true)

            val br = BufferedReader(InputStreamReader(process?.inputStream!!))
            while (logThreadRunning.get()) {
                try {
                    for (line in br.lines()) {
                        ServerLogWindow.append(line + "\n")
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
            br.close()
        }
        logThread?.start()
    }

    private fun stopLogThread() {
        logThreadRunning.set(false)
        logThread?.interrupt()
    }
}

fun main() {
    ServerProcess.start(GameType.CLASSIC)
    println("Server started")
    System.`in`.read()
    ServerProcess.stop()
    println("Server stopped ")
}