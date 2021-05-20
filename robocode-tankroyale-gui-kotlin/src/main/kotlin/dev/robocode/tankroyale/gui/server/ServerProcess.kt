package dev.robocode.tankroyale.gui.server

import dev.robocode.tankroyale.gui.settings.GameType
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.MainWindowMenu
import dev.robocode.tankroyale.gui.ui.server.ServerLogWindow
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.ResourceUtil
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.KeyGenerator
import kotlin.collections.ArrayList

object ServerProcess {

    val onStarted = Event<Unit>()
    val onStopped = Event<Unit>()

    private const val JAR_FILE_NAME = "robocode-tankroyale-server"

    private val isRunning = AtomicBoolean(false)
    private var process: Process? = null
    private var logThread: Thread? = null
    private val logThreadRunning = AtomicBoolean(false)

    var gameType: GameType = GameType.CLASSIC
        private set

    var port: Int = ServerSettings.serverPort
        private set

    var secret: String? = null
        private set

    init {
        MainWindowMenu.onStopServer.subscribe { stop() }
        MainWindowMenu.onRestartServer.subscribe { restart() }
    }

    fun isRunning(): Boolean {
        return isRunning.get()
    }

    fun start(gameType: GameType = GameType.CLASSIC, port: Int = ServerSettings.serverPort) {
        if (isRunning.get())
            return

        this.gameType = gameType
        this.port = port

        ServerLogWindow.clear()

        secret = generateSecret()

        val command = ArrayList<String>()
        command += "java"
        command += "-jar"
        command += getServerJar()
        command += "--port=$port"
        command += "--secret=$secret"
        command += "--games=$gameType"

        val builder = ProcessBuilder(command)

        builder.redirectErrorStream(true)
        process = builder.start()

        isRunning.set(true)

        startLogThread()

        onStarted.publish(Unit)
    }

    fun stop() {
        if (!isRunning.get())
            return

        stopLogThread()
        isRunning.set(false)

        val p = process
        if (p != null && p.isAlive) {

            // Send quit signal to server
            val out = p.outputStream
            out.write("q\n".toByteArray())
            out.flush()
        }

        process = null
        logThread = null

        onStopped.publish(Unit)
    }

    fun restart() {
        stop()
        start(gameType, port)
    }

    private fun generateSecret(): String {
        val secretKey = KeyGenerator.getInstance("AES").generateKey()
        val encodedKey = Base64.getEncoder().encodeToString(secretKey.encoded)
        // Remove trailing '=='
        return encodedKey.substring(0, encodedKey.length - 2)
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


