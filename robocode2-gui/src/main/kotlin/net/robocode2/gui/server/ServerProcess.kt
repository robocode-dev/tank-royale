package net.robocode2.gui.server

import net.robocode2.gui.settings.ServerSettings
import net.robocode2.gui.ui.server.ServerWindow
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

object ServerProcess {

    private const val JAR_FILE_NAME = "robocode2-server.jar"

    private val isRunning = AtomicBoolean(false)
    private var builder: ProcessBuilder? = null
    private var process: Process? = null
    private var logThread: Thread? = null
    private val logThreadRunning = AtomicBoolean(false)

    private val jarFileUrl =  javaClass.classLoader.getResource(JAR_FILE_NAME)
            ?: throw IllegalStateException("Could not find the file: $JAR_FILE_NAME")


    var port: UShort = ServerSettings.port

    fun isRunning(): Boolean {
        return isRunning.get()
    }

    fun start() {
        if (isRunning.get())
            return

        ServerWindow.clear()

        port = ServerSettings.port;

        builder = ProcessBuilder("java", "-jar", File(jarFileUrl.toURI()).toString(), "--port=$port")
        builder?.redirectErrorStream(true)
        process = builder?.start()

        isRunning.set(true)

        startLogThead()
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
    }

    private fun startLogThead() {
        logThread = Thread {
            logThreadRunning.set(true)

            while (logThreadRunning.get()) {
                try {
                    InputStreamReader(process?.inputStream!!).use { isr ->
                        BufferedReader(isr).use { br ->
                            try {
                                for (line in br.lines()) {
                                    ServerWindow.append(line + "\n")
                                }
                            } catch (ex: Error) { // "Stream closed" or "Could not acquire lock"
                                Thread.currentThread().interrupt()
                            }
                        }
                    }
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
            }
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


