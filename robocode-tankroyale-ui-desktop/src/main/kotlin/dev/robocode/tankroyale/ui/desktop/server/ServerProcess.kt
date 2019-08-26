package dev.robocode.tankroyale.ui.desktop.server

import dev.robocode.tankroyale.ui.desktop.settings.ServerSettings
import dev.robocode.tankroyale.ui.desktop.ui.server.ServerWindow
import dev.robocode.tankroyale.ui.desktop.utils.ResourceUtil
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

object ServerProcess {

    private const val JAR_FILE_NAME = "robocode-tankroyale-server.jar"

    private val jarFileName = ResourceUtil.getResourceFile(JAR_FILE_NAME).toString()

    private val isRunning = AtomicBoolean(false)
    private var process: Process? = null
    private var logThread: Thread? = null
    private val logThreadRunning = AtomicBoolean(false)

    var port: Int = ServerSettings.port

    fun isRunning(): Boolean {
        return isRunning.get()
    }

    fun start() {
        if (isRunning.get())
            return

        ServerWindow.clear()

        port = ServerSettings.port

        val builder = ProcessBuilder("java", "-jar", jarFileName, "--port=$port")
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

    private fun startLogThread() {
        logThread = Thread {
            logThreadRunning.set(true)

            val br = BufferedReader(InputStreamReader(process?.inputStream!!))
            while (logThreadRunning.get()) {
                try {
                    for (line in br.lines()) {
                        ServerWindow.append(line + "\n")
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


