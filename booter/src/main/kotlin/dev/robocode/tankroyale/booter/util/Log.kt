package dev.robocode.tankroyale.booter.util

import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.text.replace

object Log {

    private fun printBotDir(botDir: Path?) {
        val botDirPath = botDir?.absolutePathString()?.replace("\\", "\\")
        error("ERROR: Bot directory: ${botDirPath ?: "unknown"}")
    }

    fun error(ex: Exception, botDir: Path? = null) {
        printBotDir(botDir)
        error(ex.message ?: "Unknown error")
        ex.stackTrace?.firstOrNull()?.let { println(it.toString()) }
    }

    fun error(message: String, botDir: Path? = null) {
        printBotDir(botDir)
        error(message)
    }

    private fun error(message: String) {
        System.err.println(message.replace("\\", "/"))
    }
}