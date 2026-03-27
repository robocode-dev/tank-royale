package dev.robocode.tankroyale.booter.util

import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.text.replace

object Log {

    private fun printBotDir(botDir: Path?) {
        val botDirPath = botDir?.absolutePathString()
        writeError("ERROR: Bot directory: ${botDirPath ?: "unknown"}")
    }

    fun error(ex: Throwable, botDir: Path? = null) {
        printBotDir(botDir)
        ex.stackTraceToString().let { if (it.isNotBlank()) writeError(it) }
        writeError(ex.message ?: "Unknown error")
    }

    fun error(message: String, botDir: Path? = null) {
        printBotDir(botDir)
        writeError(message)
    }

    private fun writeError(message: String) {
        System.err.println(message.replace("\\", "/"))
    }
}