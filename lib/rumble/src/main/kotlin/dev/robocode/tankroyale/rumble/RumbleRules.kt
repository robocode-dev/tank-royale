package dev.robocode.tankroyale.rumble

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
private data class OwnersFile(val schemaVersion: Int, val owners: List<OwnerRecord> = emptyList())

@Serializable
private data class BannedOwner(val account: String)

@Serializable
private data class DisqualifiedBot(val bot: String)

@Serializable
private data class BansFile(
    val schemaVersion: Int,
    val bannedOwners: List<BannedOwner> = emptyList(),
    val disqualifiedBots: List<DisqualifiedBot> = emptyList(),
)

/** One owner in the bot repository's owner list: the accounts that may submit for the owner's bots. */
@Serializable
data class OwnerRecord(val ownerId: String, val accounts: List<String>, val bots: List<String>)

/**
 * The parts of a Rumble's bot repository that decide who may submit what: who owns which bot, which accounts are
 * banned and which bots are disqualified, read at one commit beside the catalog.
 */
data class RumbleRules(
    val owners: List<OwnerRecord>,
    val bannedAccounts: Set<String>,
    val disqualifiedBots: Set<String>,
) {
    /** The owner of the bot called [name], or null when nobody owns that name yet. */
    fun ownerOf(name: String): OwnerRecord? = owners.firstOrNull { name in it.bots }

    companion object {
        const val SUPPORTED_SCHEMA_VERSION = 1
        const val OWNERS_PATH = "bots/owners.json"
        const val BANS_PATH = "bots/banned.json"

        private val json = Json { ignoreUnknownKeys = true }

        /** Reads the owner list and the ban list at [commit]. A repository with no owner list yet has no owners. */
        fun read(source: CatalogSource, commit: String): RumbleRules {
            val owners = source.fileText(commit, OWNERS_PATH)
            val bans = source.fileText(commit, BANS_PATH) ?: throw CatalogUnreadable("The Rumble has no ban list at this commit.")
            return parse(owners, bans)
        }

        fun parse(ownersText: String?, bansText: String): RumbleRules {
            val ownersFile = ownersText?.let { text -> decode<OwnersFile>(text, "owner list") { it.schemaVersion } }
            val bansFile = decode<BansFile>(bansText, "ban list") { it.schemaVersion }
            return RumbleRules(
                owners = ownersFile?.owners.orEmpty(),
                bannedAccounts = bansFile.bannedOwners.map { it.account }.toSet(),
                disqualifiedBots = bansFile.disqualifiedBots.map { it.bot }.toSet(),
            )
        }

        private inline fun <reified T> decode(text: String, what: String, version: (T) -> Int): T {
            val value = try {
                json.decodeFromString<T>(text)
            } catch (e: SerializationException) {
                throw CatalogUnreadable("The Rumble $what is not valid: ${e.message?.lineSequence()?.firstOrNull()}", e)
            } catch (e: IllegalArgumentException) {
                throw CatalogUnreadable("The Rumble $what is not valid: ${e.message?.lineSequence()?.firstOrNull()}", e)
            }
            if (version(value) != SUPPORTED_SCHEMA_VERSION) {
                throw CatalogUnreadable(
                    "The Rumble $what uses schema version ${version(value)}, " +
                        "but this program understands version $SUPPORTED_SCHEMA_VERSION. Update the program."
                )
            }
            return value
        }
    }
}
