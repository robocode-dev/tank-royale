package dev.robocode.tankroyale.rumble

/** One license the Rumble allows, with the two fixed pages about it. No license text is held by the program. */
data class RumbleLicense(val id: String, private val explanationSlug: String) {
    /** The official text of the license. */
    val officialPage: String get() = "https://spdx.org/licenses/$id.html"

    /** A fuller explanation of the license, in [language] where that page exists and in English otherwise. */
    fun explanationPage(language: String): String =
        if (language in RumbleLicenses.EXPLANATION_LANGUAGES) "https://choosealicense.com/$language/licenses/$explanationSlug/"
        else "https://choosealicense.com/licenses/$explanationSlug/"

    internal fun pages(): List<String> =
        listOf(officialPage, explanationPage("")) + RumbleLicenses.EXPLANATION_LANGUAGES.map { explanationPage(it) }
}

/** The licenses the Rumble allows, the one list of the pages that may be opened about them. */
object RumbleLicenses {
    /** The languages of the desktop program that the fuller explanation also exists in, besides English. */
    internal val EXPLANATION_LANGUAGES = setOf("es")

    val ALL: List<RumbleLicense> = listOf(
        RumbleLicense("MIT", "mit"),
        RumbleLicense("Apache-2.0", "apache-2.0"),
        RumbleLicense("BSD-3-Clause", "bsd-3-clause"),
        RumbleLicense("GPL-3.0-or-later", "gpl-3.0"),
    )

    private val PAGES: Set<String> = ALL.flatMap { it.pages() }.toSet()

    /** True only for one of the fixed pages above, so the screen can open nothing else. */
    fun isFixedPage(address: String): Boolean = address in PAGES
}
