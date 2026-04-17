package dev.robocode.tankroyale.booter.process

import java.nio.file.Path
import kotlin.io.path.exists

internal object PlatformDetector {

    fun detectPlatform(botDir: Path): String? {
        val botName = botDir.fileName.toString()
        if (botDir.resolve("$botName.jar").exists() || botDir.resolve("$botName.class").exists() || botDir.resolve("$botName.java").exists()) return "jvm"
        if (botDir.resolve("$botName.py").exists()) return "python"
        if (botDir.resolve("$botName.cs").exists() || botDir.resolve("$botName.csproj").exists() || botDir.resolve("$botName.dll").exists()) return "dotnet"
        return null
    }
}
