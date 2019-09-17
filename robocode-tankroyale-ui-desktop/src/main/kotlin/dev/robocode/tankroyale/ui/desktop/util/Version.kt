package dev.robocode.tankroyale.ui.desktop.util

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

object Version {

    private var version: String? = null

    fun getVersion(): String? {
        if (version == null) {
            val inputStream = Version::class.java.classLoader.getResourceAsStream("version.txt")
            if (inputStream != null) {
                try {
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        version = reader.readLine().trim { it <= ' ' }
                        inputStream.close()
                    }
                } catch (e: IOException) {
                    throw IllegalStateException("Cannot read version")
                }
            }
        }
        return version
    }
}
