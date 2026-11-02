package dev.robocode.tankroyale.rumble

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Instant

/** What the Rumble knows about a registered account on this computer. None of it is secret. */
class RegistrationRecord(
    val account: String,
    val clientId: String,
    val environment: String,
    val savedAt: Instant,
)

/**
 * Remembers a registration, one plain file per environment, so later starts do not need the network. A client ID is
 * public (it is in every result batch and in the public registration file), so the file is not protected.
 */
class RegistrationStore(private val environment: RumbleEnvironment, baseFolder: Path) {
    private val folder: Path = environment.stateRoot(baseFolder)
    private val file: Path = folder.resolve("registration.json")
    private val temp: Path = folder.resolve("registration.json.tmp")

    @Serializable
    private class Stored(
        val schemaVersion: Int,
        val account: String,
        val clientId: String,
        val environment: String,
        val savedAt: String,
    )

    /** Saves [record]. Returns false when it could not be written; the registration itself is not lost. */
    fun save(record: RegistrationRecord): Boolean =
        try {
            Files.createDirectories(folder)
            val stored = Stored(1, record.account, record.clientId, record.environment, record.savedAt.toString())
            Files.write(temp, Json.encodeToString(Stored.serializer(), stored).toByteArray(StandardCharsets.UTF_8))
            try {
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
            } catch (e: AtomicMoveNotSupportedException) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING)
            }
            true
        } catch (e: IOException) {
            runCatching { Files.deleteIfExists(temp) }
            false
        }

    /** The saved registration in this environment, whichever account it is for, or null if none or damaged. */
    fun loadAny(): RegistrationRecord? = readStored()?.let {
        try {
            RegistrationRecord(it.account, it.clientId, it.environment, Instant.parse(it.savedAt))
        } catch (e: java.time.format.DateTimeParseException) {
            null
        }
    }

    private fun readStored(): Stored? {
        if (!Files.exists(file)) return null
        return try {
            val stored = Json.decodeFromString(Stored.serializer(), String(Files.readAllBytes(file), StandardCharsets.UTF_8))
            if (stored.schemaVersion != 1 || stored.environment != environment.name) null else stored
        } catch (e: IOException) {
            null
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    /** The saved registration of [account] in this environment, or null if none, damaged, or for another account. */
    fun load(account: String): RegistrationRecord? {
        if (!Files.exists(file)) return null
        return try {
            val stored = Json.decodeFromString(Stored.serializer(), String(Files.readAllBytes(file), StandardCharsets.UTF_8))
            if (stored.schemaVersion != 1 || stored.environment != environment.name || !stored.account.equals(account, ignoreCase = true)) null
            else RegistrationRecord(stored.account, stored.clientId, stored.environment, Instant.parse(stored.savedAt))
        } catch (e: IOException) {
            null
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        } catch (e: java.time.format.DateTimeParseException) {
            null
        }
    }

    /** Removes the saved registration. It is not an error when there is none. */
    fun forget() {
        runCatching { Files.deleteIfExists(file) }
        runCatching { Files.deleteIfExists(temp) }
    }
}
