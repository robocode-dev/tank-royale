package dev.robocode.tankroyale.ui.desktop.server

import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.ui.server.ServerLogWindow
import dev.robocode.tankroyale.ui.desktop.util.ResourceUtil
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.crypto.KeyGenerator

object ServerProcess {

    private const val JAR_FILE_NAME = "robocode-tankroyale-server"

    private val isRunning = AtomicBoolean(false)
    private var process: Process? = null
    private var logThread: Thread? = null
    private val logThreadRunning = AtomicBoolean(false)

    var port: Int = ServerSettings.port

    var secret: String? = null
        private set

    fun isRunning(): Boolean {
        return isRunning.get()
    }

    fun start() {
        if (isRunning.get())
            return

        ServerLogWindow.clear()

        port = ServerSettings.port
        secret = generateSecret()

        val builder = ProcessBuilder("java", "-jar", getServerJar(), "--port=$port", "--secret=$secret")
        builder.redirectErrorStream(true)
        process = builder.start()

        isRunning.set(true)

        startLogThread()
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
            out.flush() // important!
        }

        process = null
        logThread = null
    }

    private fun generateSecret(): String {
        val secretKey = KeyGenerator.getInstance("AES").generateKey()
        val encodedKey = Base64.getEncoder().encodeToString(secretKey.encoded)
        // Remove trailing "=="
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
        return ResourceUtil.getResourceFile("${JAR_FILE_NAME}.jar")?.absolutePath ?: ""
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
    ServerProcess.start()
    println("Server started")
    System.`in`.read()
    ServerProcess.stop()
    println("Server stopped ")
}


