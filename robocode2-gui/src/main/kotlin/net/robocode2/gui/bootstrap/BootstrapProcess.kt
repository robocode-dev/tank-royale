package net.robocode2.gui.bootstrap

import net.robocode2.gui.server.ServerProcess.stop
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.atomic.AtomicBoolean

object BootstrapProcess {

    private const val BOOT_DIR = "D:/robocode-2-work/robocode2-boot" // FIXME

    private const val JAR_FILE_NAME = "robocode2-bootstrap.jar"

    private val jarFileUrl =  javaClass.classLoader.getResource(JAR_FILE_NAME)
            ?: throw IllegalStateException("Could not find the file: $JAR_FILE_NAME")

    private val isRunning = AtomicBoolean(false)
    private var runProcess: Process? = null

    private var errorThread: Thread? = null
    private val errorThreadRunning = AtomicBoolean(false)

    fun list(): String {
        val builder = ProcessBuilder("java", "-jar", File(jarFileUrl.toURI()).toString(),
                "list", "--boot-dir=$BOOT_DIR")
        val process = builder.start()
        readErrorToStdError(process)
        return readInputLines(process).joinToString()
    }

    fun run(entries: List<String>) {
        if (isRunning.get())
            stop()

        val args = ArrayList<String>()
        args += "java"
        args += "-jar"
        args += File(jarFileUrl.toURI()).toString()
        args += "run"
        args += "--boot-dir=$BOOT_DIR"
        args += entries

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

fun main() {
//    println(BootstrapProcess.list())

    BootstrapProcess.run(listOf("TestBot", "TestBot"))
    readLine()
    BootstrapProcess.stopRunning()
}


