package dev.robocode.tankroyale.booter.util

object Log {

    fun error(ex: Exception) {
        System.err.println("Error: ${ex.message}")
    }
}