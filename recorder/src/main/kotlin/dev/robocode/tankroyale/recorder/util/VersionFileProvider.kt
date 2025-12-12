package dev.robocode.tankroyale.recorder.util

import dev.robocode.tankroyale.common.util.Version

internal object VersionFileProvider {
    fun getVersion(): String = "Robocode Tank Royale Recorder ${Version.version}"
}
