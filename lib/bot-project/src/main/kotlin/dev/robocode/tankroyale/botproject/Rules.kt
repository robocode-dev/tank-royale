package dev.robocode.tankroyale.botproject

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** How one of the four official languages is recognised in a bot folder. */
@Serializable
data class PlatformRule(
    /** Source file ending, for example `.java`. */
    val extension: String,
    /** The value of `platform` in the bot's configuration file. */
    val platform: String,
    /** Text that source files of the language contain when they use the official Bot API. */
    val apiToken: String,
)

/**
 * The checks the bot repository applies to a single bot folder, kept in `rules.json` so they can be compared with the
 * repository's validator at a glance.
 */
@Serializable
data class ValidationRules(
    val allowedLicenses: List<String>,
    /** Keyed by language: `java`, `csharp`, `python` or `typescript`. */
    val platforms: Map<String, PlatformRule>,
    val launcherSuffixes: List<String>,
    val forbiddenDirectories: List<String>,
    val forbiddenSuffixes: List<String>,
    val forbiddenTokens: List<String>,
    val typescriptApi: String,
    val typescriptRuntimePeers: List<String>,
) {
    companion object {
        private const val RESOURCE = "rules.json"

        val current: ValidationRules = run {
            val text = ValidationRules::class.java.getResourceAsStream(RESOURCE)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                ?: error("Missing resource $RESOURCE")
            Json.decodeFromString(text)
        }
    }
}
