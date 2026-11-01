package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.TreeHash
import java.nio.file.Files
import java.nio.file.Path
import java.util.Comparator
import java.util.zip.ZipInputStream

/** The outcome of fetching one bot. Only [Placed] leaves anything in the target folder. */
sealed interface FetchResult {
    val bot: CatalogBot

    /** The bot's source matched the catalog and was placed in [directory]. */
    data class Placed(override val bot: CatalogBot, val directory: Path) : FetchResult

    /** The source did not match the catalog hash, so nothing was placed. */
    data class HashMismatch(override val bot: CatalogBot, val actual: String) : FetchResult

    /** The archive holds no files for this bot, so nothing was placed. */
    data class NotInArchive(override val bot: CatalogBot) : FetchResult

    /** A folder with the bot's name already exists in the target and was left untouched. */
    data class AlreadyExists(override val bot: CatalogBot, val directory: Path) : FetchResult
}

/** Raised when an archive or catalog entry tries to reach outside the target folder. Nothing is written. */
class UnsafeFetch(message: String) : Exception(message)

/** Fetches bot sources from a catalog commit and places each one only when its hash matches the catalog. */
object BotFetcher {

    /**
     * Fetches [bots] as they were at [commit] into folders below [target], named after each bot's own folder.
     * Each bot is checked on its own; one failing bot does not stop the others.
     */
    fun fetch(source: CatalogSource, commit: String, bots: List<CatalogBot>, target: Path): List<FetchResult> {
        bots.forEach { requireSafeRelativePath(it.path, "The catalog entry for ${it.name} ${it.version}") }
        Files.createDirectories(target)
        val work = Files.createTempDirectory(target, ".rumble-fetch-")
        try {
            extract(source, commit, bots.map { it.path }, work)
            return bots.map { place(it, work, target) }
        } finally {
            deleteRecursively(work)
        }
    }

    private fun extract(source: CatalogSource, commit: String, wanted: List<String>, work: Path) {
        source.archive(commit).use { stream ->
            val zip = ZipInputStream(stream)
            while (true) {
                val entry = zip.nextEntry ?: break
                // The archive has one top folder named after the repository and commit.
                val inside = entry.name.substringAfter('/', "").trimEnd('/')
                if (inside.isEmpty()) continue
                if (escapes(inside)) throw UnsafeFetch("The archive entry '${entry.name}' leaves the target folder, so nothing was fetched.")
                if (entry.isDirectory || wanted.none { inside.startsWith("$it/") }) continue
                requireSafeRelativePath(inside, "The archive entry '${entry.name}'")
                val file = work.resolve(inside).normalize()
                if (!file.startsWith(work)) throw UnsafeFetch("The archive entry '${entry.name}' leaves the target folder.")
                Files.createDirectories(file.parent)
                Files.copy(zip, file)
            }
        }
    }

    private fun place(bot: CatalogBot, work: Path, target: Path): FetchResult {
        val source = work.resolve(bot.path)
        if (!Files.isDirectory(source)) return FetchResult.NotInArchive(bot)
        val actual = TreeHash.ofDirectory(source)
        if (actual != bot.sourceHash) return FetchResult.HashMismatch(bot, actual)
        val destination = target.resolve(bot.path.substringAfterLast('/'))
        if (Files.exists(destination)) return FetchResult.AlreadyExists(bot, destination)
        Files.move(source, destination)
        return FetchResult.Placed(bot, destination)
    }

    /** True for absolute paths, backslashes and `..` segments, which could reach outside a folder. */
    private fun escapes(path: String): Boolean =
        path.startsWith("/") || '\\' in path || path.split('/').any { it == ".." }

    private fun requireSafeRelativePath(path: String, what: String) {
        val segments = path.split('/')
        val unsafe = path.isEmpty() || path.startsWith("/") || '\\' in path || ':' in path ||
            segments.any { it.isEmpty() || it == "." || it == ".." }
        if (unsafe) throw UnsafeFetch("$what has an unsafe path '$path', so nothing was fetched.")
    }

    private fun deleteRecursively(directory: Path) {
        if (!Files.exists(directory)) return
        Files.walk(directory).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) } }
    }
}
