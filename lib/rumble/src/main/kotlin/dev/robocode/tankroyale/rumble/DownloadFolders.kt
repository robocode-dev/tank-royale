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
}

/** The bot directories and their labels after a download folder was remembered, and what that did. */
data class RememberedFolder(val folders: List<BotFolderSetting>, val labels: Map<String, String>, val outcome: FolderOutcome) {
    val added: Boolean get() = outcome == FolderOutcome.ADDED
}

/**
 * How folders of bots downloaded from the Rumble live among the person's bot directories. The desktop program keeps the
 * list of bot directories exactly as before; labels live in a setting of their own, keyed by the folder's path in one
 * form, so the same folder written another way is the same folder and an older version reads the same bot directories.
 * Nothing here touches Swing or the disk.
 */
object DownloadFolders {
    /** The label of a folder of bots downloaded from the Rumble. The text a person sees is a resource. */
    const val LABEL = "rumble-downloads"

    private const val SEPARATOR = ','

    /** A folder's path in one form: forward slashes, no trailing slash, and on Windows in lower case. */
    fun key(path: String, windows: Boolean = Platform.isWindows): String {
        val key = path.trim().replace('\\', '/').trimEnd('/')
        return if (windows) key.lowercase() else key
    }

    /**
     * Reads the labels setting: pairs of path and label. A comma or a percent sign inside a path or a label is written
     * escaped, so a folder whose name holds a comma keeps its label. Text that is not such a pair is left out.
     */
    fun parseLabels(text: String, windows: Boolean = Platform.isWindows): Map<String, String> =
        text.split(SEPARATOR).map { unescape(it) }.chunked(2)
            .filter { it.size == 2 && it[0].isNotBlank() && it[1].isNotBlank() }
            .associate { key(it[0], windows) to it[1].trim() }

    fun formatLabels(labels: Map<String, String>): String =
        labels.entries.joinToString(SEPARATOR.toString()) { escape(it.key) + SEPARATOR + escape(it.value) }

    private fun escape(text: String) = text.replace("%", "%25").replace(",", "%2C")

    private fun unescape(text: String) = text.replace("%2C", ",", ignoreCase = true).replace("%25", "%")

    /** The labels of only the folders still among [folders], so a removed folder leaves no label behind. */
    fun keepLabelsOf(folders: List<BotFolderSetting>, labels: Map<String, String>, windows: Boolean = Platform.isWindows): Map<String, String> {
        val present = folders.map { key(it.path, windows) }.toSet()
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
        onlyDownloads: Boolean = false, windows: Boolean = Platform.isWindows,
    ): RememberedFolder {
        val key = key(folder.toString(), windows)
        val existing = folders.firstOrNull { key(it.path, windows) == key }
            ?: return RememberedFolder(folders + BotFolderSetting(folder.toString(), true), labels + (key to LABEL), FolderOutcome.ADDED)
        if (labels[key] != LABEL && !onlyDownloads) return RememberedFolder(folders, labels, FolderOutcome.OWN_FOLDER)
        return RememberedFolder(folders, labels + (key to LABEL), if (existing.enabled) FolderOutcome.LABELLED else FolderOutcome.SWITCHED_OFF)
    }

    /** The person's own switched-on bot directories, which the suggested download folder is worked out from. */
    fun ownFolders(folders: List<BotFolderSetting>, labels: Map<String, String>, windows: Boolean = Platform.isWindows): List<Path> =
        folders.filter { it.enabled && labels[key(it.path, windows)] != LABEL }.map { Path.of(it.path) }

    /** True when the folder at [path] is labelled as a download folder. */
    fun isLabelled(path: String, labels: Map<String, String>, windows: Boolean = Platform.isWindows): Boolean = labels[key(path, windows)] == LABEL

    /** True when the bot in [botDir] lies in a labelled download folder. */
    fun isDownloaded(botDir: String, labels: Map<String, String>, windows: Boolean = Platform.isWindows): Boolean {
        val parent = Path.of(botDir).parent ?: return false
        return labels[key(parent.toString(), windows)] == LABEL
    }
}

/** The official text of a license a bot declares, when it is written as a standard SPDX identifier. */
object LicenseLinks {
    const val SPDX_PAGES = "https://spdx.org/licenses/"
    private val IDENTIFIER = Regex("[A-Za-z0-9][A-Za-z0-9.+-]*")

    /** The SPDX page of [license], or null when it is not written as an SPDX identifier. */
    fun spdxPage(license: String?): String? = license?.trim()?.takeIf { IDENTIFIER.matches(it) }?.let { "$SPDX_PAGES$it.html" }

    /** True only for a page this program makes for a license, so nothing else is opened from the field. */
    fun isSpdxPage(address: String): Boolean =
        address.startsWith(SPDX_PAGES) && address.endsWith(".html") && IDENTIFIER.matches(address.removePrefix(SPDX_PAGES).removeSuffix(".html"))
}
