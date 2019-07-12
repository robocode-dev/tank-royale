package net.robocode2.gui.bootstrap

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

object BootstrapProcess {

    private const val BOOT_DIR = "D:/robocode-2-work/robocode2-boot" // FIXME

    private const val JAR_FILE_NAME = "robocode2-bootstrap.jar"

    private var builder: ProcessBuilder? = null
    var process: Process? = null

    private val jarFileUrl =  javaClass.classLoader.getResource(JAR_FILE_NAME)
            ?: throw IllegalStateException("Could not find the file: $JAR_FILE_NAME")

    fun list(): String {
        builder = ProcessBuilder("java", "-jar", File(jarFileUrl.toURI()).toString(),
                "list", "--boot-dir=$BOOT_DIR")
        process = builder?.start()
        readErrorToStdError()
        return readInputLines().joinToString()
    }

    private fun readErrorToStdError() {
        val reader = BufferedReader(InputStreamReader(process?.errorStream!!))
        var line: String? = null
        while ({ line = reader.readLine(); line }() != null) {
            System.err.println(line)
        }
    }

    private fun readInputLines(): List<String> {
        val list = ArrayList<String>()
        val reader = BufferedReader(InputStreamReader(process?.inputStream!!))
        var line: String? = null
        while ({ line = reader.readLine(); line }() != null) {
            list += line!!
        }
        return list
    }
}

fun main() {
    println(BootstrapProcess.list())
}


