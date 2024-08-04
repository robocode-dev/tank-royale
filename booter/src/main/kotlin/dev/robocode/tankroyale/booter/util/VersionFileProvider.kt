package dev.robocode.tankroyale.booter.util

import picocli.CommandLine
import java.io.InputStreamReader
import java.lang.IllegalStateException
import java.util.*

internal class VersionFileProvider : CommandLine.IVersionProvider {

    private val versionProperties = "version.properties"

    override fun getVersion(): Array<String> {
        val properties = readVersionProperties()
        val version = properties.getProperty("version")
        return arrayOf("Robocode Tank Royale Booter $version")
    }

    private fun readVersionProperties(): Properties {
        val inputStream = this.javaClass.classLoader.getResourceAsStream(versionProperties)
            ?: throw IllegalStateException("Unable to locate internal $versionProperties file")

        return Properties().apply {
            InputStreamReader(inputStream).use { reader ->
                load(reader)
            }
        }
    }
}