package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.common.util.Platform
import java.nio.file.Path

/** One of the person's bot directories as the desktop program keeps it: a path and whether it is switched on. */
data class BotFolderSetting(val path: String, val enabled: Boolean)

/** What remembering a download folder did, so the screen can say where the bots are. */
enum class FolderOutcome {
    /** Added to the bot directories, switched on and labelled. */
    ADDED,

    /** Already among the bot directories and switched on; now labelled. */
    LABELLED,

    /** Already among the bot directories but switched off by the person, which is kept; now labelled. */
    SWITCHED_OFF,

    /** One of the person's own bot directories, which also holds bots of their own; left as it was, without a label. */
    OWN_FOLDER,

    /** One of the person's own bot directories, left as it was, without a label, and switched off, so its bots are not in New Battle. */
    OWN_FOLDER_SWITCHED_OFF,
}

/** The bot directories and their labels after a download folder was remembered, and what that did. */
data class RememberedFolder(val folders: List<BotFolderSetting>, val labels: Map<String, String>, val outcome: FolderOutcome)

/**
 * How folders of bots downloaded from the Rumble live among the person's bot directories. The desktop program keeps the
 * list of bot directories exactly as before; labels live in a setting of their own, keyed by the folder's path in one
 * form, so the same folder written another way is the same folder and an older version reads the same bot directories.
 * Nothing here touches Swing or the disk.
 */
object DownloadFolders {
    /** The label of a folder of bots downloaded from the Rumble. The text a person sees is a resource. */
    const val LABEL = "rumble-downloads"

    private const val ENTRY_SEPARATOR = ','
    private const val PAIR_SEPARATOR = ';'

    /** True where the file system usually ignores letter case in names: on Windows and macOS. */
    val IGNORE_CASE: Boolean = Platform.isWindows || Platform.operatingSystemType == Platform.PlatformType.Mac

    /**
     * A folder's path in one form: forward slashes, as the setting of bot directories has always stored it, no trailing
     * slash, and in lower case where the file system usually ignores letter case.
     */
    fun key(path: String, ignoreCase: Boolean = IGNORE_CASE): String {
        val key = path.trim().replace('\\', '/').trimEnd('/')
        return if (ignoreCase) key.lowercase() else key
    }

    /**
     * Reads the labels setting: entries of a path and a label, each entry on its own between commas. A comma, a semicolon
     * or a percent sign inside a path or a label is written escaped, so a folder whose name holds one keeps its label. An
     * entry that is not such a pair is left out on its own, so it never shifts the entries after it.
     */
    fun parseLabels(text: String, ignoreCase: Boolean = IGNORE_CASE): Map<String, String> =
        text.split(ENTRY_SEPARATOR).map { it.split(PAIR_SEPARATOR) }
            .filter { it.size == 2 && it[0].isNotBlank() && it[1].isNotBlank() }
            .associate { key(unescape(it[0]), ignoreCase) to unescape(it[1]).trim() }

    fun formatLabels(labels: Map<String, String>): String =
        labels.entries.joinToString(ENTRY_SEPARATOR.toString()) { escape(it.key) + PAIR_SEPARATOR + escape(it.value) }

    private fun escape(text: String) = text.replace("%", "%25").replace(",", "%2C").replace(";", "%3B")

    private fun unescape(text: String) = text.replace("%2C", ",", ignoreCase = true).replace("%3B", ";", ignoreCase = true).replace("%25", "%")

    /** The labels of only the folders still among [folders], so a removed folder leaves no label behind. */
    fun keepLabelsOf(folders: List<BotFolderSetting>, labels: Map<String, String>, ignoreCase: Boolean = IGNORE_CASE): Map<String, String> {
        val present = folders.map { key(it.path, ignoreCase) }.toSet()
        return labels.filterKeys { it in present }
    }

    /**
     * Remembers [folder] as a download folder after a download placed bots in it. A folder not yet among [folders] is
     * added at the end, switched on and labelled. A folder already there keeps its place and its switch, and gets the
     * label only when it is labelled already or [onlyDownloads] says it holds nothing but downloaded bots, so one of the
     * person's own bot directories never has their own bots moved into the group of downloaded bots.
     */
    fun remember(
        folders: List<BotFolderSetting>, labels: Map<String, String>, folder: Path,
        onlyDownloads: Boolean = false, ignoreCase: Boolean = IGNORE_CASE,
    ): RememberedFolder {
        val key = key(folder.toString(), ignoreCase)
        val existing = folders.firstOrNull { key(it.path, ignoreCase) == key }
            ?: return RememberedFolder(folders + BotFolderSetting(folder.toString(), true), labels + (key to LABEL), FolderOutcome.ADDED)
        if (labels[key] != LABEL && !onlyDownloads) {
            return RememberedFolder(folders, labels, if (existing.enabled) FolderOutcome.OWN_FOLDER else FolderOutcome.OWN_FOLDER_SWITCHED_OFF)
        }
        return RememberedFolder(folders, labels + (key to LABEL), if (existing.enabled) FolderOutcome.LABELLED else FolderOutcome.SWITCHED_OFF)
    }

    /**
     * Remembers [folder] after a download as [remember] does, reading the settings with [read] and writing them with
     * [save]. When the folder cannot be looked into or the settings cannot be read or written, nothing is remembered,
     * [onFailure] is told why, so it can be logged, and the result is null, so the screen still shows what the download
     * did and asks the person to add the folder by hand.
     */
    fun rememberAfterDownload(
        folder: Path, onlyDownloads: () -> Boolean,
        read: () -> Pair<List<BotFolderSetting>, Map<String, String>>,
        save: (RememberedFolder) -> Unit,
        onFailure: (Exception) -> Unit = {},
        ignoreCase: Boolean = IGNORE_CASE,
    ): FolderOutcome? = try {
        val (folders, labels) = read()
        remember(folders, labels, folder, onlyDownloads(), ignoreCase).also(save).outcome
    } catch (e: Exception) {
        onFailure(e)
        null
    }

    /** The person's own switched-on bot directories, which the suggested download folder is worked out from. */
    fun ownFolders(folders: List<BotFolderSetting>, labels: Map<String, String>, ignoreCase: Boolean = IGNORE_CASE): List<Path> =
        folders.filter { it.enabled && labels[key(it.path, ignoreCase)] != LABEL }.map { Path.of(it.path) }

    /** True when the folder at [path] is labelled as a download folder. */
    fun isLabelled(path: String, labels: Map<String, String>, ignoreCase: Boolean = IGNORE_CASE): Boolean = labels[key(path, ignoreCase)] == LABEL

    /**
     * The text each bot of the group of downloaded bots is shown by, for the bot folders [botDirs]: the bot's own folder
     * name, or its whole path when another bot in the group has a folder of the same name, so two are never alike.
     */
    fun shownNames(botDirs: Collection<String>): Map<String, String> {
        val names = botDirs.associateWith { Path.of(it).fileName?.toString() ?: it }
        val repeated = names.values.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
        return names.mapValues { (dir, name) -> if (name in repeated) dir else name }
    }

    /** True when the bot in [botDir] lies in a labelled download folder. */
    fun isDownloaded(botDir: String, labels: Map<String, String>, ignoreCase: Boolean = IGNORE_CASE): Boolean {
        val parent = Path.of(botDir).parent ?: return false
        return labels[key(parent.toString(), ignoreCase)] == LABEL
    }
}

/** The official text of a license a bot declares, when it is written as an identifier on the SPDX License List. */
object LicenseLinks {
    const val SPDX_PAGES = "https://spdx.org/licenses/"
    private const val RESOURCE = "spdx-license-ids.txt"

    /** The identifiers of the SPDX License List kept with this library, by lower case, as written on the list. */
    private val IDENTIFIERS: Map<String, String> = run {
        val text = LicenseLinks::class.java.getResourceAsStream(RESOURCE)
            ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            ?: error("Missing resource $RESOURCE")
        text.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.associateBy { it.lowercase() }
    }

    /**
     * The SPDX page of [license], or null when it is not an identifier on the SPDX License List, such as a license
     * of the bot's own, a LicenseRef or an expression of several licenses. Letter case does not matter, as on the list.
     * The page is made only from an identifier on the list, so no other address can come from here.
     */
    fun spdxPage(license: String?): String? = license?.trim()?.lowercase()?.let { IDENTIFIERS[it] }?.let { "$SPDX_PAGES$it.html" }
}
