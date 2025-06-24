package dev.robocode.tankroyale.booter.process

import java.nio.file.Path
import java.util.function.Predicate

/**
 * Predicate for filtering files that match the bot name pattern.
 * Used to find bot script files in a directory.
 * 
 * @param botName The name of the bot to match files against
 */
internal class IsBotFile(private val botName: String) : Predicate<Path> {
    override fun test(path: Path): Boolean {
        val filename = path.fileName.toString().lowercase()
        val botName = botName.lowercase()

        return filename == botName || filename.startsWith("$botName.")
    }
}
