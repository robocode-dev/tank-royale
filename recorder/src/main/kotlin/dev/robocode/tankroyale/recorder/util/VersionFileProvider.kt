package dev.robocode.tankroyale.recorder.util

import dev.robocode.tankroyale.common.util.Version
import picocli.CommandLine

internal object VersionFileProvider : CommandLine.IVersionProvider {

    override fun getVersion(): Array<String> {
        return arrayOf("Robocode Tank Royale Recorder ${Version.version}")
    }

}
