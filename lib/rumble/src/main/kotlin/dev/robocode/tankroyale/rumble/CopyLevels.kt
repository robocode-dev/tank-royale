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

    /** The licenses on each level by SPDX identifier in lower case, and deprecated identifiers with what replaces them. */
    private val LEVELS: Map<String, CopyLevel>
    private val DEPRECATED: Map<String, String>

    init {
        val text = CopyLevels::class.java.getResourceAsStream(RESOURCE)
            ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            ?: error("Missing resource $RESOURCE")
        val levels = mutableMapOf<String, CopyLevel>()
        val deprecated = mutableMapOf<String, String>()
        var section = ""
        text.lines().map { it.trim() }.filter { it.isNotEmpty() && !it.startsWith("#") }.forEach { line ->
            when {
                line.startsWith("[") -> section = line
                section == "[free-to-copy]" -> levels[line.lowercase()] = CopyLevel.FREE_TO_COPY
                section == "[copy-with-conditions]" -> levels[line.lowercase()] = CopyLevel.COPY_WITH_CONDITIONS
                section == "[deprecated]" -> deprecated[line.substringBefore(' ').lowercase()] = line.substringAfter(' ').trim()
                else -> error("A license outside a known section in $RESOURCE: $line")
            }
        }
        LEVELS = levels
        DEPRECATED = deprecated
    }

    // SPDX license expressions write their operators in capitals; a lower-case "or" in free text is no operator.
    private val OR = Regex("\\s+OR\\s+")
    private val AND = Regex("\\s+AND\\s+")
    private val WITH = Regex("\\s+WITH\\s+")
    private val OPERATOR = Regex("(^|\\s)(OR|AND|WITH)(\\s|$)")

    /** The deprecated SPDX identifiers of the GNU licenses without `-only` or `-or-later`, which mean `-only`. */
    private val GNU_WITHOUT_SUFFIX = Regex("(?i)(A?GPL|LGPL)-\\d\\.\\d")

    /**
     * The level of [license] as the bot declares it, or null when the license is not known, such as for a bot that
     * joined a battle on its own. An empty license means none is declared, which is at the do-not-copy level.
     *
     * `A OR B` is at the level of its most permissive choice, as the reader may choose; `A AND B` at the level of its
     * most careful part; `A WITH exception` at the level of `A`. The operators count only in capitals, as SPDX writes
     * them. An expression with parentheses is not read and is at the do-not-copy level, as an unrecognised license.
     */
    fun of(license: String?): CopyLevel? = license?.let { ofDeclared(it) }

    /** The level of a declared [license]; an empty one means none is declared. See [of]. */
    fun ofDeclared(license: String): CopyLevel = expression(license.trim(), deprecatedHops = 0)

    private fun expression(text: String, deprecatedHops: Int): CopyLevel {
        if (text.isEmpty() || '(' in text || ')' in text) return CopyLevel.DO_NOT_COPY
        return text.split(OR).minOf { choice ->
            choice.split(AND).maxOf { part -> identifier(part.split(WITH).first(), deprecatedHops) }
        }
    }

    /** The level of one SPDX identifier, following a deprecated identifier to what replaces it. */
    private fun identifier(id: String, deprecatedHops: Int): CopyLevel {
        val trimmed = id.trim()
        val key = trimmed.lowercase()
        if (key.isEmpty() || OPERATOR.containsMatchIn(trimmed)) return CopyLevel.DO_NOT_COPY
        LEVELS[key]?.let { return it }
        DEPRECATED[key]?.takeIf { deprecatedHops == 0 }?.let { return expression(it, deprecatedHops + 1) }
        if (key.endsWith("+")) {
            // "This version or later" only adds choices, so it is at the level of the version itself.
            val base = key.removeSuffix("+")
            (LEVELS["$base-or-later"] ?: LEVELS[base] ?: LEVELS["$base-only"])?.let { return it }
        }
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
