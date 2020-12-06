package dev.robocode.tankroyale.server.version

import dev.robocode.tankroyale.server.Server
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

private var version: String? = null

fun getVersion(): String? {
    if (version == null) {
        val inputStream = Server::class.java.classLoader.getResourceAsStream("version.txt")
        inputStream?.use {
            try {
                val isr = InputStreamReader(inputStream);
                isr.use {
                    BufferedReader(isr).use { reader -> version = reader.readLine().trim() }
                }
            } catch (e: IOException) {
                throw IllegalStateException("Cannot read version")
            }
        }
    }
    return version
}