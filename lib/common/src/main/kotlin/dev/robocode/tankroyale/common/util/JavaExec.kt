package dev.robocode.tankroyale.common.util

import dev.robocode.tankroyale.common.util.Platform.isWindows

object JavaExec {
    fun java() = if (isWindows) "javaw" else "java"
}