package dev.robocode.tankroyale.booter.util

import java.nio.file.Path
import kotlin.io.path.absolutePathString

object Log {

    private fun printBotDir(botDir: Path?) {
        System.err.println("ERROR: Bot directory: ${botDir?.absolutePathString() ?: "unknown"}")
    }

    fun error(ex: Exception, botDir: Path? = null) {
        printBotDir(botDir)
        System.err.println(ex.message ?: "Unknown error")
        ex.stackTrace?.firstOrNull()?.let { System.err.println(it.toString()) }
    }

    fun error(message: String, botDir: Path? = null) {
        printBotDir(botDir)
        System.err.println(message)
    }
}