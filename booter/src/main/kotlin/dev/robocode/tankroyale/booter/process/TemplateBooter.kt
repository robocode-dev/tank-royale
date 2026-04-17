package dev.robocode.tankroyale.booter.process

import dev.robocode.tankroyale.booter.model.BootEntry
import dev.robocode.tankroyale.booter.util.Log
import java.nio.file.Path
import java.util.*

internal class TemplateBooter(
    private val setupEnvironment: (MutableMap<String, String?>, BootEntry, Team?) -> Unit = BotEnvironment::setup
) {
    fun boot(botDir: Path, botEntry: BootEntry, team: Team?): Process? {
        val base = botEntry.base ?: return null
        val botName = botEntry.name

        val platform = botEntry.platform ?: PlatformDetector.detectPlatform(botDir) ?: return null

        val template = TemplateManager.getTemplate(platform) ?: return null
        val command = parseTemplate(template) ?: return null

        val replacedCommand = command.map { part ->
            part.replace("\${base}", base)
                .replace("\${botName}", botName)
                .replace("\${classPath}", "../lib/*")
        }

        val finalCommand = if (System.getProperty("os.name").lowercase(Locale.ROOT).contains("win")) {
            listOf("cmd.exe", "/c") + replacedCommand
        } else {
            replacedCommand
        }

        return try {
            val pb = ProcessBuilder(finalCommand)
            pb.directory(botDir.toFile())
            pb.redirectOutput(ProcessBuilder.Redirect.DISCARD)
            setupEnvironment(pb.environment(), botEntry, team)
            pb.start()
        } catch (ex: Exception) {
            Log.error(ex, botDir)
            null
        }
    }

    private fun parseTemplate(template: String): List<String>? {
        val os = System.getProperty("os.name").lowercase(Locale.ROOT)
        val section = if (os.contains("win")) "[cmd]" else "[sh]"

        val lines = template.lines()
        val sectionIndex = lines.indexOfFirst { it.trim() == section }
        if (sectionIndex == -1) return null

        for (i in sectionIndex + 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty() || line.startsWith("#")) continue
            if (line.startsWith("[")) break

            // Simple command line splitter (handles quotes roughly)
            return splitCommand(line)
        }
        return null
    }

    private fun splitCommand(command: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        for (char in command) {
            if (char == '\"') {
                inQuotes = !inQuotes
            } else if (char == ' ' && !inQuotes) {
                if (current.isNotEmpty()) {
                    result.add(current.toString())
                    current.clear()
                }
            } else {
                current.append(char)
            }
        }
        if (current.isNotEmpty()) {
            result.add(current.toString())
        }
        return result
    }

}
