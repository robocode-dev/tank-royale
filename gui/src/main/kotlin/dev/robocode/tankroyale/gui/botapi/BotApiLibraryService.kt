package dev.robocode.tankroyale.gui.botapi

import dev.robocode.tankroyale.common.util.Platform
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object BotApiLibraryService {

    fun update(entry: BotApiLibEntry) {
        val platform = entry.platform
        val subDir = entry.botRootDir.resolve(platform.subDir)
        Files.createDirectories(subDir)

        deleteExistingLibraryFiles(subDir, platform)

        val newFile = subDir.resolve(platform.newFileName())
        writeResourceToFile(platform.resourceName, newFile)

        if (Platform.operatingSystemType == Platform.PlatformType.Mac) {
            clearMacOsQuarantine(newFile)
        }
        if (platform == BotApiPlatform.TYPESCRIPT) {
            patchTypeScriptPackageJson(entry.botRootDir, platform.newFileName())
        }
    }

    private fun deleteExistingLibraryFiles(subDir: Path, platform: BotApiPlatform) {
        Files.list(subDir).use { stream ->
            stream.filter { platform.extractVersion(it.fileName.toString()) != null }
                .forEach { Files.deleteIfExists(it) }
        }
    }

    private fun writeResourceToFile(resourceName: String, target: Path) {
        val stream = BotApiLibraryService::class.java.classLoader.getResourceAsStream(resourceName)
            ?: error("Bundled resource not found: $resourceName")
        stream.use { Files.copy(it, target, StandardCopyOption.REPLACE_EXISTING) }
    }

    private fun clearMacOsQuarantine(file: Path) {
        try {
            Runtime.getRuntime().exec(arrayOf("xattr", "-d", "com.apple.quarantine", file.toString())).waitFor()
        } catch (_: Exception) {
            // Ignore: file may not have the quarantine attribute set
        }
    }

    private fun patchTypeScriptPackageJson(botRootDir: Path, newTgzFileName: String) {
        val packageJson = botRootDir.resolve("package.json")
        if (!Files.exists(packageJson)) return

        val content = Files.readString(packageJson)
        val patched = content.replace(
            Regex(""""@robocode\.dev/tank-royale-bot-api":\s*"file:\./deps/robocode-tank-royale-bot-api-.+?\.tgz""""),
            """"@robocode.dev/tank-royale-bot-api": "file:./deps/$newTgzFileName""""
        )
        if (patched != content) {
            Files.writeString(packageJson, patched)
        }
    }
}
