package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.util

import picocli.CommandLine
import java.io.InputStreamReader
import java.lang.IllegalStateException
import java.util.*

internal object VersionFileProvider : CommandLine.IVersionProvider {

    private const val VERSION_PROPERTIES = "version.properties"

    val version get() = getVersion()[0]

    override fun getVersion(): Array<String> {
        val properties = readVersionProperties()
        val version = properties.getProperty("version")
        return arrayOf("Robocode Tank Royale Server $version")
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