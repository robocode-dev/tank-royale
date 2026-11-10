package dev.robocode.tankroyale.rumble

/**
 * How freely a reader may copy from a bot's code, read from the license the bot declares. It is a reading aid: it reads
 * only the declaration, cannot check that it is true, and is not legal advice. The levels are ordered from the most
 * permissive to the most careful.
 */
enum class CopyLevel {
    /** A permissive license: copy, keeping the copyright notice and the license. */
    FREE_TO_COPY,

    /** A copyleft license, an attribution license, or a permissive license with unusual terms: copy only on its terms. */
    COPY_WITH_CONDITIONS,

    /** No license, or one that is not known, or one that forbids changes or commercial use: all rights are reserved. */
    DO_NOT_COPY,
}

/**
 * Works out the [CopyLevel] of a declared license. The licenses on each level are the Blue Oak Council's lists of
 * permissive and copyleft licenses, kept with this library as SPDX identifiers. Creative Commons licenses are on
 * neither list, as they are not software licenses, so their level follows their own terms. Nothing here touches Swing.
 */
object CopyLevels {
    private const val RESOURCE = "license-levels.txt"

    /** The licenses on each level by SPDX identifier in lower case, as the resource lists them. */
    private val LEVELS: Map<String, CopyLevel> = run {
        val text = CopyLevels::class.java.getResourceAsStream(RESOURCE)
            ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            ?: error("Missing resource $RESOURCE")
        val levels = mutableMapOf<String, CopyLevel>()
        var level: CopyLevel? = null
        text.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.forEach { line ->
            when (line) {
                "[free-to-copy]" -> level = CopyLevel.FREE_TO_COPY
                "[copy-with-conditions]" -> level = CopyLevel.COPY_WITH_CONDITIONS
                else -> levels[line.lowercase()] = level ?: error("A license before any level in $RESOURCE: $line")
            }
        }
        levels
    }

    private val OPERATOR = Regex("\\s+(OR|AND|WITH)\\s+", RegexOption.IGNORE_CASE)

    /** The deprecated SPDX identifiers of the GNU licenses without `-only` or `-or-later`, which mean `-only`. */
    private val GNU_WITHOUT_SUFFIX = Regex("(?i)(A?GPL|LGPL)-\\d\\.\\d")

    /**
     * The level of [license] as the bot declares it, or null when the license is not known, such as for a bot that
     * joined a battle on its own. An empty license means none is declared, which is at the do-not-copy level.
     *
     * `A OR B` is at the level of its most permissive choice, as the reader may choose; `A AND B` at the level of its
     * most careful part; `A WITH exception` at the level of `A`. An expression with parentheses is not read and is at
     * the do-not-copy level, as an unrecognised license.
     */
    fun of(license: String?): CopyLevel? {
        val text = license?.trim() ?: return null
        if (text.isEmpty() || '(' in text || ')' in text) return CopyLevel.DO_NOT_COPY
        return text.split(Regex("\\s+OR\\s+", RegexOption.IGNORE_CASE)).minOf { choice ->
            choice.split(Regex("\\s+AND\\s+", RegexOption.IGNORE_CASE)).maxOf { part ->
                identifier(part.split(Regex("\\s+WITH\\s+", RegexOption.IGNORE_CASE)).first())
            }
        }
    }

    /** The level of one SPDX identifier, following a deprecated identifier to the one it stands for. */
    private fun identifier(id: String): CopyLevel {
        val key = id.trim().lowercase()
        if (key.isEmpty() || OPERATOR.containsMatchIn(" $key ")) return CopyLevel.DO_NOT_COPY
        LEVELS[key]?.let { return it }
        if (key.endsWith("+")) LEVELS[key.removeSuffix("+") + "-or-later"]?.let { return it }
        if (GNU_WITHOUT_SUFFIX.matches(key)) LEVELS["$key-only"]?.let { return it }
        return creativeCommons(key) ?: CopyLevel.DO_NOT_COPY
    }

    /** Creative Commons attribution licenses ask for credit; their non-commercial and no-derivatives forms forbid copying. */
    private fun creativeCommons(key: String): CopyLevel? {
        if (!key.startsWith("cc-by-")) return null
        val terms = key.removePrefix("cc-by-").split('-')
        return if ("nc" in terms || "nd" in terms) CopyLevel.DO_NOT_COPY else CopyLevel.COPY_WITH_CONDITIONS
    }
}
