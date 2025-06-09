package dev.robocode.tankroyale.booter.util

import dev.robocode.tankroyale.common.util.Version
import picocli.CommandLine

internal class VersionFileProvider : CommandLine.IVersionProvider {

    override fun getVersion(): Array<String> {
        return arrayOf("Robocode Tank Royale Booter ${Version.version}")
    }

}