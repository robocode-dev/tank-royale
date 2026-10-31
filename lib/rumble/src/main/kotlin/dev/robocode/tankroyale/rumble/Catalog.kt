package dev.robocode.tankroyale.rumble

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Raised when a catalog cannot be read, with a message the person can act on. */
class CatalogUnreadable(message: String, cause: Throwable? = null) : Exception(message, cause)

/** One bot entry of the catalog's `bots/index.json`. */
@Serializable
data class CatalogBot(
    val name: String,
    val version: String,
    val platform: String,
    /** Slash-separated folder of the bot below the repository root, for example `bots/java/Walls`. */
    val path: String,
    /** `sha256:` followed by the hash of the bot's whole source tree. */
    val sourceHash: String,
    val owner: String,
    val authors: List<String>,
    val addedAt: String,
    val status: String,
    /** For a team, its members written as `name version`; empty for an individual bot. */
    val teamMembers: List<String> = emptyList(),
) {
    val isActive: Boolean get() = status == ACTIVE

    companion object {
        const val ACTIVE = "active"
    }
}

/** The catalog of a Rumble's bot repository. Superseded, retired and disqualified entries stay as history. */
@Serializable
data class Catalog(
    val schemaVersion: Int,
    val generatedAt: String,
    /** The immutable commit of the bot repository the catalog was generated from. */
    val commit: String,
    val bots: List<CatalogBot>,
) {
    /** The entries that can be used for new battles and downloads. */
    val activeBots: List<CatalogBot> get() = bots.filter { it.isActive }

    companion object {
        const val SUPPORTED_SCHEMA_VERSION = 1

        private val json = Json { ignoreUnknownKeys = true }

        /** Reads a catalog document, refusing malformed documents and schema versions this library does not know. */
        fun parse(text: String): Catalog {
            val catalog = try {
                json.decodeFromString<Catalog>(text)
            } catch (e: SerializationException) {
                throw CatalogUnreadable("The Rumble catalog is not valid: ${e.message?.lineSequence()?.firstOrNull()}", e)
            } catch (e: IllegalArgumentException) {
                throw CatalogUnreadable("The Rumble catalog is not valid: ${e.message?.lineSequence()?.firstOrNull()}", e)
            }
            if (catalog.schemaVersion != SUPPORTED_SCHEMA_VERSION) {
                throw CatalogUnreadable(
                    "The Rumble catalog uses schema version ${catalog.schemaVersion}, " +
                        "but this program understands version $SUPPORTED_SCHEMA_VERSION. Update the program."
                )
            }
            return catalog
        }
    }
}
