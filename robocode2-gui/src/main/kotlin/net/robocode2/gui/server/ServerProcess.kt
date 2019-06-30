package net.robocode2.gui.server

import net.robocode2.gui.ui.server.ServerWindow
import java.io.BufferedReader
import java.io.InputStreamReader


object ServerProcess {

    private const val BAT_FILE_NAME = "server.bat"

    private var builder: ProcessBuilder? = null
    private var process: Process? = null

    init {
        val filename = BAT_FILE_NAME

        val url = javaClass.classLoader.getResource(filename)
                ?: throw IllegalStateException("Could not find the file: $filename")

        builder = ProcessBuilder(url.file)
        builder?.redirectErrorStream(true)
    }

    fun start() {
        process = builder?.start()

        InputStreamReader(process?.inputStream).use { isr ->
            BufferedReader(isr).use { br ->
                for (line in br.lines()) {
                    ServerWindow.append(line + "\n")
                }
            }
        }
    }

    fun stop() {
        val proc = ServerProcess.process
        if (proc != null && proc.isAlive) {

            // Send quit signal to server
            val out = proc.outputStream
            out.write("q\n".toByteArray())
            out.flush() // important!
        }
    }
}

fun main() {
    ServerProcess.start()
    println("Server started")
    System.`in`.read()
    ServerProcess.stop()
    println("Server stopped ")
}


