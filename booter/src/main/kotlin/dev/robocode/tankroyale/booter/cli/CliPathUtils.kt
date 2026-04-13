package dev.robocode.tankroyale.booter.cli

import dev.robocode.tankroyale.booter.util.Log
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

internal fun toPaths(botRootDirs: Array<String>?): List<Path> =
    botRootDirs?.toSet()?.mapNotNull {
        val path = Paths.get(it.trim())
        if (Files.exists(path)) path
        else {
            Log.error("WARNING: Bot root directory does not exist and will be ignored: $path")
            null
        }
    } ?: emptyList()

