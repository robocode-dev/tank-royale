package dev.robocode.tankroyale.common.util

import java.io.InputStreamReader
import java.lang.IllegalStateException
import java.util.Properties

object Version {

    private const val VERSION_PROPERTIES = "version.properties"

    val version: String by lazy { fetchVersion() }

    private fun fetchVersion(): String {
        val properties = readVersionProperties()
        return properties.getProperty("version")
    }

    private fun readVersionProperties(): Properties {
        val inputStream = this.javaClass.classLoader.getResourceAsStream(VERSION_PROPERTIES)
            ?: throw IllegalStateException("Unable to locate internal $VERSION_PROPERTIES file")

        return Properties().apply {
            InputStreamReader(inputStream).use { reader ->
                load(reader)
            }
        }
    }
}