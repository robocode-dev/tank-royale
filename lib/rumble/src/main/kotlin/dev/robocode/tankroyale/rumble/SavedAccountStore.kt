package dev.robocode.tankroyale.rumble

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Remembers which GitHub account is signed in, one plain file per environment, because the saved sign-in holds only a
 * refresh token. An account name is public, so the file is not protected.
 */
class SavedAccountStore(private val environment: RumbleEnvironment, baseFolder: Path) {
    private val folder: Path = environment.stateRoot(baseFolder)
    private val file: Path = folder.resolve("account.json")

    @Serializable
    private class Stored(val schemaVersion: Int, val account: String, val environment: String)

    /** Saves [account]. Returns false when it could not be written. */
    fun save(account: String): Boolean =
        try {
            Files.createDirectories(folder)
            Files.write(file, Json.encodeToString(Stored.serializer(), Stored(1, account, environment.name)).toByteArray(StandardCharsets.UTF_8))
            true
        } catch (e: IOException) {
            false
        }

    /** The saved account name, or null when none is saved or the file is damaged. */
    fun load(): String? {
        if (!Files.exists(file)) return null
        return try {
            val stored = Json.decodeFromString(Stored.serializer(), String(Files.readAllBytes(file), StandardCharsets.UTF_8))
            if (stored.schemaVersion != 1 || stored.environment != environment.name) null else stored.account
        } catch (e: IOException) {
            null
        } catch (e: SerializationException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun exists(): Boolean = Files.exists(file)

    /** Removes the saved name. Returns false when it is still there. */
    fun forget(): Boolean {
        runCatching { Files.deleteIfExists(file) }
        return !Files.exists(file)
    }
}
