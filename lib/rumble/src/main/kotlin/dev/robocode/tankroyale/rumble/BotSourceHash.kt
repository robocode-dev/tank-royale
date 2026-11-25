package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.TreeHash
import dev.robocode.tankroyale.botproject.ValidationRules
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.util.stream.Collectors

/**
 * The hash of a bot's source in a folder where it may have run, compared with the catalog's: the files a run leaves
 * behind, in the folders and with the endings the bot repository never accepts (such as `bin`, `obj`, `node_modules` or
 * `__pycache__`), are not part of the source. A bot run from its folder is therefore not taken for a changed bot.
 */
internal object BotSourceHash {

    /** The hash of the source in [folder], leaving out what a run leaves behind and the files [skip] names. */
    fun of(folder: Path, skip: (String) -> Boolean = { false }): String {
        val found = Files.walk(folder).use { paths ->
            paths.filter { Files.isRegularFile(it, LinkOption.NOFOLLOW_LINKS) }.collect(Collectors.toList())
        }
            .map { folder.relativize(it).joinToString("/") to it }
            .filter { (path, _) -> !skip(path) && !leftByRunning(path) }
            .associate { (path, file) -> path to Files.readAllBytes(file) }
        return TreeHash.of(found)
    }

    /** True for a file in a folder, or with an ending, that the bot repository never accepts, so a run left it. */
    fun leftByRunning(path: String): Boolean {
        val rules = ValidationRules.current
        return path.split('/').dropLast(1).any { it in rules.forbiddenDirectories } || rules.forbiddenSuffixes.any { path.endsWith(it) }
    }
}
