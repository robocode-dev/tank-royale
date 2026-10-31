package dev.robocode.tankroyale.rumble

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path

/** Raised when an operation would reach, or mix up, the wrong Rumble environment. */
class EnvironmentRefused(message: String) : Exception(message)

/**
 * One Rumble: the public Beta that real participants use, or the test environment with repositories of its own.
 *
 * The names come from `environments.json`, which the reset script in `scripts/rumble` reads too, so there is only
 * one list of repositories to keep right.
 */
@Serializable
data class RumbleEnvironment(
    val name: String,
    val displayName: String,
    val botsRepository: String,
    val dataRepository: String,
    val dashboard: String,
    val resettable: Boolean,
    /** True when screens must show the permanent development warning. */
    val development: Boolean,
) {
    /** The folder for this environment's cache, queue, receipts and registrations below [base]. */
    fun stateRoot(base: Path): Path = base.resolve("environments").resolve(name)
}

@Serializable
private data class EnvironmentFile(val owner: String, val environments: List<RumbleEnvironment>)

/** The known Rumble environments, and the rules for telling which one a repository belongs to. */
object RumbleEnvironments {
    private const val RESOURCE = "environments.json"

    private val file: EnvironmentFile = run {
        val text = RumbleEnvironments::class.java.getResourceAsStream(RESOURCE)
            ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
            ?: error("Missing resource $RESOURCE")
        Json.decodeFromString<EnvironmentFile>(text)
    }

    /** The owner of every Rumble repository. */
    val owner: String get() = file.owner

    /** All known environments. */
    val all: List<RumbleEnvironment> get() = file.environments

    val PUBLIC: RumbleEnvironment = byName("public")
    val TEST: RumbleEnvironment = byName("test")

    /** An installation that names no environment uses the public Beta. */
    val DEFAULT: RumbleEnvironment get() = PUBLIC

    private fun byName(name: String): RumbleEnvironment =
        file.environments.singleOrNull { it.name == name } ?: error("Missing environment $name")

    private val remote = Regex(
        """^(?:https://github\.com/|git@github\.com:|ssh://git@github\.com/)""" +
            """(?<owner>[A-Za-z0-9_.-]+)/(?<name>[A-Za-z0-9_.-]+?)(?:\.git)?/?$"""
    )
    private val slug = Regex("""[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+""")

    /** Returns the lower-case `owner/name` for a short name or GitHub address, or refuses. */
    fun repositorySlug(reference: String): String {
        val text = reference.trim()
        val match = remote.matchEntire(text)
        val (owner, name) = when {
            match != null -> match.groups["owner"]!!.value to match.groups["name"]!!.value
            slug.matches(text) -> text.substringBefore('/') to text.substringAfter('/')
            else -> throw EnvironmentRefused("Cannot tell which repository '$reference' is.")
        }
        return "$owner/$name".lowercase()
    }

    /** The environment owning this repository on an exact owner and name match, or null when it is in neither. */
    fun of(reference: String): RumbleEnvironment? {
        val wanted = repositorySlug(reference)
        return all.firstOrNull { wanted in repositories(it) }
    }

    private fun repositories(environment: RumbleEnvironment): Set<String> =
        setOf(environment.botsRepository, environment.dataRepository).map { "$owner/$it".lowercase() }.toSet()
}
