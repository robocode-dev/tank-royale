package dev.robocode.tankroyale.booter.cli

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

internal fun toPaths(botRootDirs: Array<String>?): List<Path> =
    botRootDirs?.toSet()?.map {
        val path = Paths.get(it.trim())
        if (Files.exists(path)) path else null
    }?.mapNotNull { it } ?: emptyList()

