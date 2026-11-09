package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.BotProjects
import dev.robocode.tankroyale.botproject.Inspection
import dev.robocode.tankroyale.botproject.TreeHash
import dev.robocode.tankroyale.botproject.ValidationRules
import java.io.IOException
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions
import java.util.Comparator
import java.util.stream.Collectors

/** What the download folder holds for a bot, compared with the catalog. */
enum class DownloadedState { NEW, UPDATE, UP_TO_DATE, CHANGED_HERE, KEPT_APART, NO_LONGER_ACTIVE }

/** One bot on the screen. [bot] is the active catalog entry, and null for an earlier download that is no longer active. */
data class DownloadRow(
    val name: String,
    val version: String?,
    val platform: String?,
    val state: DownloadedState,
    /** The license the bot's own configuration file declares, known only for a bot that is already in the folder. */
    val license: String?,
    val bot: CatalogBot?,
    /** The version the bot's own configuration file in the folder declares, known only for a bot the screen placed there. */
    val versionHere: String? = null,
)

/** How the download of one bot ended. */
sealed interface DownloadResult {
    val bot: CatalogBot

    /** The bot was placed with its marker; [updated] when it replaced an earlier download. */
    data class Placed(override val bot: CatalogBot, val updated: Boolean, val license: String?) : DownloadResult
    data class UpToDate(override val bot: CatalogBot) : DownloadResult
    data class HashDiffers(override val bot: CatalogBot) : DownloadResult
    data class NotInArchive(override val bot: CatalogBot) : DownloadResult

    /** A folder with this name that the screen did not place. It was left exactly as it was. */
    data class KeptOwn(override val bot: CatalogBot) : DownloadResult

    /** An earlier download the person has changed, and did not ask to replace. It was left exactly as it was. */
    data class KeptChanged(override val bot: CatalogBot) : DownloadResult

    /** The bot was fetched and checked, but could not be put in the folder. What was there before is still there. */
    data class NotPlaced(override val bot: CatalogBot, val message: String) : DownloadResult
}

/**
 * The download folder. An empty marker file in a bot folder says that this screen placed the folder, and nothing else:
 * the version and the license come from the bot's own configuration file, and whether it is unchanged from the same
 * checksum the catalog records. Nothing is recorded anywhere else, so deleting the folder forgets everything. A folder
 * without the marker is never changed, replaced or deleted, whatever its name.
 */
object BotDownloads {
    const val MARKER = ".rumble-download"
    private const val DEFAULT_FOLDER = "rumble-bots"

    /** The folder beside the person's first bot folder, or in [home] when they have none. Nothing is created. */
    fun defaultFolder(botFolders: List<Path>, home: Path): Path =
        (botFolders.firstOrNull()?.toAbsolutePath()?.parent ?: home).resolve(DEFAULT_FOLDER)

    /** The active bots with their states, then the earlier downloads that are no longer active. Reads only. */
    fun rows(catalog: Catalog, target: Path): List<DownloadRow> {
        val active = catalog.activeBots
        val activeFolders = active.map { folderName(it) }.toSet()
        val current = active.map { row(it, catalog, target) }.sortedBy { it.name.lowercase() }
        val gone = markedFolders(target).filter { it.fileName.toString() !in activeFolders }.map { folder ->
            val project = project(folder)
            val name = folder.fileName.toString()
            DownloadRow(project?.name ?: name, project?.version, project?.platform, DownloadedState.NO_LONGER_ACTIVE, project?.license, null, project?.version)
        }.sortedBy { it.name.lowercase() }
        return current + gone
    }

    /** True when the folder can be made and written to. It is asked before anything is fetched. */
    fun writable(target: Path): Boolean =
        try {
            Files.createDirectories(target)
            Files.delete(Files.createTempFile(target, ".rumble-write-check-", null))
            true
        } catch (e: IOException) {
            false
        } catch (e: SecurityException) {
            false
        }

    /**
     * Fetches [bots] from the catalog's own commit and puts each in [target] with its marker, after its source matched
     * the catalog. One failing bot never stops the others. An unchanged earlier download is replaced by a newer version,
     * a changed one only when its name is in [replaceChanged], and a folder without the marker never.
     */
    fun download(source: CatalogSource, catalog: Catalog, bots: List<CatalogBot>, replaceChanged: Set<String>, target: Path): List<DownloadResult> {
        Files.createDirectories(target)
        val results = HashMap<String, DownloadResult>()
        val wanted = bots.filter { bot ->
            val stays = leftAsItIs(bot, catalog, replaceChanged, target)
            if (stays != null) results[bot.path] = stays
            stays == null
        }
        if (wanted.isNotEmpty()) {
            val work = Files.createTempDirectory(target, ".rumble-download-")
            try {
                BotFetcher.fetch(source, catalog.commit, wanted, work.resolve("new")).forEach { fetched ->
                    results[fetched.bot.path] = place(fetched, catalog, replaceChanged, target, work)
                }
            } finally {
                deleteRecursively(work)
            }
        }
        return bots.map { results.getValue(it.path) }
    }

    /** The answer for a bot that is not to be fetched or replaced, or null when it is. */
    private fun leftAsItIs(bot: CatalogBot, catalog: Catalog, replaceChanged: Set<String>, target: Path): DownloadResult? =
        when (row(bot, catalog, target).state) {
            DownloadedState.UP_TO_DATE -> DownloadResult.UpToDate(bot)
            DownloadedState.KEPT_APART -> DownloadResult.KeptOwn(bot)
            DownloadedState.CHANGED_HERE -> if (bot.name in replaceChanged) null else DownloadResult.KeptChanged(bot)
            DownloadedState.NEW, DownloadedState.UPDATE, DownloadedState.NO_LONGER_ACTIVE -> null
        }

    private fun place(fetched: FetchResult, catalog: Catalog, replaceChanged: Set<String>, target: Path, work: Path): DownloadResult {
        val bot = fetched.bot
        return when (fetched) {
            is FetchResult.HashMismatch -> DownloadResult.HashDiffers(bot)
            is FetchResult.NotInArchive -> DownloadResult.NotInArchive(bot)
            is FetchResult.AlreadyExists -> DownloadResult.KeptOwn(bot)
            is FetchResult.Placed -> {
                // Looked at again now, because the person may have changed the folder while the archive was on its way.
                leftAsItIs(bot, catalog, replaceChanged, target)?.let { return it }
                try {
                    Files.createFile(fetched.directory.resolve(MARKER))
                    makeLaunchersRunnable(fetched.directory)
                    val updated = moveIntoPlace(fetched.directory, target.resolve(folderName(bot)), work)
                    DownloadResult.Placed(bot, updated, project(target.resolve(folderName(bot)))?.license)
                } catch (e: IOException) {
                    DownloadResult.NotPlaced(bot, e.message.orEmpty())
                }
            }
        }
    }

    /** Moves the new folder in. An old folder is moved away first and is put back when the new one cannot be moved in. */
    private fun moveIntoPlace(new: Path, destination: Path, work: Path): Boolean {
        if (!exists(destination)) {
            Files.move(new, destination)
            return false
        }
        val old = Files.createDirectories(work.resolve("old")).resolve(destination.fileName)
        Files.move(destination, old)
        try {
            Files.move(new, destination)
        } catch (e: IOException) {
            Files.move(old, destination)
            throw e
        }
        return true
    }

    /** The archive does not say which files can be run, and the launcher scripts must be for the bot to start. */
    private fun makeLaunchersRunnable(folder: Path) {
        if ("posix" !in folder.fileSystem.supportedFileAttributeViews()) return
        files(folder).filter { it.fileName.toString().endsWith(".sh") }.forEach {
            Files.setPosixFilePermissions(it, PosixFilePermissions.fromString("rwxr-xr-x"))
        }
    }

    private fun row(bot: CatalogBot, catalog: Catalog, target: Path): DownloadRow {
        val folder = target.resolve(folderName(bot))
        if (!exists(folder)) return DownloadRow(bot.name, bot.version, bot.platform, DownloadedState.NEW, null, bot)
        if (!isMarked(folder)) return DownloadRow(bot.name, bot.version, bot.platform, DownloadedState.KEPT_APART, null, bot)
        val project = project(folder)
        fun answer(state: DownloadedState) = DownloadRow(bot.name, bot.version, bot.platform, state, project?.license, bot, project?.version)
        val known = catalog.bots.firstOrNull { folderName(it) == folderName(bot) && it.version == project?.version }
        return when {
            known == null || !unchanged(folder, known) -> answer(DownloadedState.CHANGED_HERE)
            known.version == bot.version -> answer(DownloadedState.UP_TO_DATE)
            else -> answer(DownloadedState.UPDATE)
        }
    }

    /** True when the folder holds what the catalog records, not counting the marker or what running the bot leaves behind. */
    private fun unchanged(folder: Path, known: CatalogBot): Boolean {
        val found = files(folder).associate { folder.relativize(it).joinToString("/") to Files.readAllBytes(it) } - MARKER
        return TreeHash.of(found) == known.sourceHash || TreeHash.of(found.filterKeys { !leftByRunning(it) }) == known.sourceHash
    }

    /** The folders and file endings the bot repository never accepts, so they cannot be part of a bot's source. */
    private fun leftByRunning(path: String): Boolean {
        val rules = ValidationRules.current
        return path.split('/').dropLast(1).any { it in rules.forbiddenDirectories } || rules.forbiddenSuffixes.any { path.endsWith(it) }
    }

    private fun markedFolders(target: Path): List<Path> {
        if (!Files.isDirectory(target)) return emptyList()
        return Files.list(target).use { it.collect(Collectors.toList()) }.filter { isMarked(it) }
    }

    private fun isMarked(folder: Path): Boolean =
        Files.isDirectory(folder, LinkOption.NOFOLLOW_LINKS) && Files.isRegularFile(folder.resolve(MARKER), LinkOption.NOFOLLOW_LINKS)

    private fun exists(path: Path) = Files.exists(path, LinkOption.NOFOLLOW_LINKS)

    private fun project(folder: Path) = (BotProjects.inspect(folder) as? Inspection.Found)?.project

    private fun folderName(bot: CatalogBot) = bot.path.substringAfterLast('/')

    private fun files(folder: Path): List<Path> =
        Files.walk(folder).use { paths -> paths.filter { Files.isRegularFile(it, LinkOption.NOFOLLOW_LINKS) }.collect(Collectors.toList()) }

    private fun deleteRecursively(directory: Path) {
        if (!Files.exists(directory, LinkOption.NOFOLLOW_LINKS)) return
        Files.walk(directory).use { paths -> paths.sorted(Comparator.reverseOrder()).forEach { Files.deleteIfExists(it) } }
    }
}
