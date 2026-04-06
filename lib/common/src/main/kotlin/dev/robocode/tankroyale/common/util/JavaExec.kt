package dev.robocode.tankroyale.common.util

import dev.robocode.tankroyale.common.util.Platform.isWindows

object JavaExec {
    fun java() = if (isWindows) "javaw" else "java"

    /**
     * Returns JVM arguments required to suppress restricted-access warnings for unnamed modules
     * when running JNA or other native-access libraries on Java 16+.
     *
     * On Java 15 and earlier the flag does not exist and must be omitted.
     */
    fun nativeAccessArgs(): List<String> =
        if (Runtime.version().feature() >= 16)
            listOf("--enable-native-access=ALL-UNNAMED")
        else
            emptyList()
}