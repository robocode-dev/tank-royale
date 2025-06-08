package dev.robocode.tankroyale.server.util

import dev.robocode.tankroyale.common.util.Version
import picocli.CommandLine

internal object VersionFileProvider : CommandLine.IVersionProvider {

    // This method is used for the Server application
    override fun getVersion(): Array<String> {
        return arrayOf("Robocode Tank Royale Server ${Version.version}")
    }

}