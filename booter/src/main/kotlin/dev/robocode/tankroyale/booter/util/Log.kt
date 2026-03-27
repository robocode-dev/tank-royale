package dev.robocode.tankroyale.booter.util

import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.text.replace

object Log {

    private fun printBotDir(botDir: Path?) {
        botDir?.let { writeError("ERROR: Bot directory: ${it.absolutePathString()}") }
    }

    fun error(ex: Throwable, botDir: Path? = null) {
        printBotDir(botDir)
        ex.stackTraceToString().let { if (it.isNotBlank()) writeError(it) }
    }

    fun error(message: String, botDir: Path? = null) {
        printBotDir(botDir)
        writeError(message)
    }

    private fun writeError(message: String) {
        System.err.println(message.replace("\\", "/"))
    }
}