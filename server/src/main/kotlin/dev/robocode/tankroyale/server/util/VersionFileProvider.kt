package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.util

import picocli.CommandLine
import java.io.InputStreamReader
import java.lang.IllegalStateException
import java.util.*

internal object VersionFileProvider : CommandLine.IVersionProvider {

    private const val VERSION_PROPERTIES = "version.properties"

    // Use this property to get the version only
    val version get(): String {
        val properties = readVersionProperties()
        val version = properties.getProperty("version")
        return version
    }

    // This method is used for the Server application
    override fun getVersion(): Array<String> {
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