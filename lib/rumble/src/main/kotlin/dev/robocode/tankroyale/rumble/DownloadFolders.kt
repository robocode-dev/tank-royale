package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.common.util.Platform
import java.nio.file.Path

/** One of the person's bot directories as the desktop program keeps it: a path and whether it is switched on. */
data class BotFolderSetting(val path: String, val enabled: Boolean)

/** The bot directories and their labels after a download folder was remembered, and whether it was added. */
data class RememberedFolder(val folders: List<BotFolderSetting>, val labels: Map<String, String>, val added: Boolean)

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

    /** Reads the labels setting: pairs of path and label. Text that is not such a pair is left out. */
    fun parseLabels(text: String, windows: Boolean = Platform.isWindows): Map<String, String> =
        text.split(SEPARATOR).chunked(2)
            .filter { it.size == 2 && it[0].isNotBlank() && it[1].isNotBlank() }
            .associate { key(it[0], windows) to it[1].trim() }

    fun formatLabels(labels: Map<String, String>): String =
        labels.entries.joinToString(SEPARATOR.toString()) { it.key + SEPARATOR + it.value }

    /** The labels of only the folders still among [folders], so a removed folder leaves no label behind. */
    fun keepLabelsOf(folders: List<BotFolderSetting>, labels: Map<String, String>, windows: Boolean = Platform.isWindows): Map<String, String> {
        val present = folders.map { key(it.path, windows) }.toSet()
        return labels.filterKeys { it in present }
    }

    /**
     * Remembers [folder] as a download folder after a download placed bots in it. A folder not yet among [folders] is
     * added at the end, switched on; a folder already there keeps its place and its switch. Either way it gets the label.
     */
    fun remember(folders: List<BotFolderSetting>, labels: Map<String, String>, folder: Path, windows: Boolean = Platform.isWindows): RememberedFolder {
        val key = key(folder.toString(), windows)
        val known = folders.any { key(it.path, windows) == key }
        val all = if (known) folders else folders + BotFolderSetting(folder.toString(), true)
        return RememberedFolder(all, labels + (key to LABEL), !known)
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
