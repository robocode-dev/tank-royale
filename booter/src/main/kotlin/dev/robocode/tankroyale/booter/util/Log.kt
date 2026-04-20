package dev.robocode.tankroyale.booter.util

import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.text.replace

object Log {

    private fun botDirPrefix(botDir: Path?) =
        botDir?.let { "ERROR: Bot directory: ${it.absolutePathString()}\n" } ?: ""

    fun error(ex: Throwable, botDir: Path? = null) {
        ex.stackTraceToString().let { if (it.isNotBlank()) writeError("${botDirPrefix(botDir)}$it") }
    }

    fun error(message: String, botDir: Path? = null) {
        writeError("${botDirPrefix(botDir)}$message")
    }

    private fun writeError(message: String) {
        System.err.println(message.replace("\\", "/"))
    }
}