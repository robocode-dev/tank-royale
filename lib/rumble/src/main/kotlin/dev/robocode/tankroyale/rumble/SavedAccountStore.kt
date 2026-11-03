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

/**
 * Remembers which GitHub account is signed in, one plain file per environment, because the saved sign-in holds only a
 * refresh token. An account name is public, so the file is not protected.
 */
class SavedAccountStore(private val environment: RumbleEnvironment, baseFolder: Path) {
    private val folder: Path = environment.stateRoot(baseFolder)
    private val file: Path = folder.resolve("account.json")
    private val temp: Path = folder.resolve("account.json.tmp")

    @Serializable
    private class Stored(val schemaVersion: Int, val account: String, val environment: String)

    /** Saves [account]. Returns false when it could not be written. */
    fun save(account: String): Boolean =
        try {
            Files.createDirectories(folder)
            Files.write(temp, Json.encodeToString(Stored.serializer(), Stored(1, account, environment.name)).toByteArray(StandardCharsets.UTF_8))
            replaceFile(temp, file)
            true
        } catch (e: IOException) {
            runCatching { Files.deleteIfExists(temp) }
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

    /** True unless the file is known to be absent. */
    fun exists(): Boolean = !Files.notExists(file)

    /** Removes the saved name. Returns false when it is still there. */
    fun forget(): Boolean {
        runCatching { Files.deleteIfExists(file) }
        runCatching { Files.deleteIfExists(temp) }
        return Files.notExists(file)
    }
}

/** Moves [from] to [to] in one step where the file system can, so a reader never sees half a file. */
internal fun replaceFile(from: Path, to: Path) {
    try {
        Files.move(from, to, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
    } catch (e: AtomicMoveNotSupportedException) {
        Files.move(from, to, StandardCopyOption.REPLACE_EXISTING)
    }
}
