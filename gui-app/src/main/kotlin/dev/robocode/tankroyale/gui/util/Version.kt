package dev.robocode.tankroyale.gui.util

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object Version {

    val version: String? by lazy { fetchVersion() }

    private fun fetchVersion(): String? {
        Version::class.java.classLoader.getResourceAsStream("version.txt").use {
            inputStream ->
                try {
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        return reader.readLine().trim { it <= ' ' }
                    }
                } catch (e: IOException) {
                    throw IllegalStateException("Cannot read version")
                }
        }
    }
}
