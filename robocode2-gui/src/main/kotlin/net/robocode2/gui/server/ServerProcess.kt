package net.robocode2.gui.server

import java.io.InputStreamReader
import java.io.BufferedReader


object ServerProcess {

    val builder = ProcessBuilder("D:\\robocode2-server\\robocode2-server.bat")
    var process: Process? = null

    init {
        builder.redirectError()
    }

    fun start() {
        process = builder.start()

        val isr = InputStreamReader(process?.inputStream)
        isr.use {
            val br = BufferedReader(isr)
            br.use {
                for (line in br.lines()) {
                    ServerWindow.append(line + "\n")
                }
            }
        }
        process?.waitFor()
    }

    fun stop() {
        val p = process
        if (p != null && p.isAlive)
            p.destroyForcibly()
    }
}

fun main() {
    ServerProcess.start()
    println("Server started")
    System.`in`.read()
    ServerProcess.stop()
    println("Server stopped ")
}


