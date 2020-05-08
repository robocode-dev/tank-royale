package dev.robocode.tankroyale.bootstrap.commands

import dev.robocode.tankroyale.bootstrap.BootstrapException
import dev.robocode.tankroyale.bootstrap.model.BotInfo
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonDecodingException
import kotlinx.serialization.parse
import java.nio.file.Files
import java.nio.file.Path

@UnstableDefault
@ImplicitReflectionSerializer
abstract class Command(private val botPaths: List<Path>) {

    protected fun getBotInfo(botName: String): BotInfo? {
        var path: Path? = null
        botPaths.forEach { dirPath ->
            run {
                try {
                    path = resolveFullBotPath(dirPath, "$botName.json")?.toAbsolutePath()
                    if (path != null) {
                        val content = path!!.toFile().readText(Charsets.UTF_8)
                        return Json.parse(content)
                    }
                } catch (ex: JsonDecodingException) {
                    throw BootstrapException("Could not parse JSON file: $path")
                } catch (ex: MissingFieldException) {
                    throw BootstrapException("${ex.message}. File: $path")
                }
            }
        }
        return null // not found
    }

    protected fun resolveFullBotPath(botDirPath: Path, botPath: String): Path? {
        val path = botDirPath.resolve(botPath)
        return if (Files.exists(path)) path else null
    }
}