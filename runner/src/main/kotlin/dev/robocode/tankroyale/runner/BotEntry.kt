package dev.robocode.tankroyale.runner

import java.nio.file.Path

/**
 * Identifies a bot participant by its directory path on the file system.
 *
 * The directory must exist and is expected to contain a bot configuration file (*.json).
 * Full validation (presence of config file, required fields) is performed at battle-start time.
 *
 * @property path path to the bot directory
 * @throws IllegalArgumentException if [path] does not point to an existing directory
 */
data class BotEntry(val path: Path) {

    init {
        require(path.toFile().isDirectory) { "Bot path must be a directory: $path" }
    }

    companion object {
        /** Creates a [BotEntry] from a [Path]. */
        @JvmStatic
        fun of(path: Path): BotEntry = BotEntry(path)

        /** Creates a [BotEntry] from a path string. */
        @JvmStatic
        fun of(path: String): BotEntry = BotEntry(Path.of(path))
    }
}
