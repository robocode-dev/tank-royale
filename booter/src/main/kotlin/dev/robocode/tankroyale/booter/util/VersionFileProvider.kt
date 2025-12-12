package dev.robocode.tankroyale.booter.util

import dev.robocode.tankroyale.common.util.Version

internal object VersionFileProvider {
    fun getVersion(): String = "Robocode Tank Royale Booter ${Version.version}"
}