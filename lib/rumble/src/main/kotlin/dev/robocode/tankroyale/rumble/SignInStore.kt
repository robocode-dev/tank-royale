package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.common.util.UserDataDirectory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/** Who and where the saved sign-in is tied to. The key is derived from this, so a copy elsewhere cannot be opened. */
class SignInIdentity(val userName: String, val homeFolder: String, val osName: String) {
    internal fun keyMaterial(): String = "$userName|$homeFolder|$osName"

    companion object {
        fun current() = SignInIdentity(
            System.getProperty("user.name").orEmpty(),
            System.getProperty("user.home").orEmpty(),
            System.getProperty("os.name").orEmpty(),
        )
    }
}

sealed interface SaveResult {
    data object Saved : SaveResult

    /** The sign-in was not saved; [reason] says why in plain words. */
    data class NotSaved(val reason: String) : SaveResult
}

sealed interface LoadResult {
    /** A saved refresh token. Its text form hides the token. */
    class Found(val refreshToken: String, val refreshTokenExpiresAt: Instant?) : LoadResult {
        override fun toString(): String = "Found(refreshToken=hidden, refreshTokenExpiresAt=$refreshTokenExpiresAt)"
    }

    /** Nothing is saved. This is normal, not a problem. */
    data object Missing : LoadResult

    /** Saving is switched off. */
    data object Disabled : LoadResult

    /** A file exists but cannot be trusted or read; the person should sign in again. */
    data class Unusable(val reason: String) : LoadResult
}

/**
 * Keeps the sign-in between runs in one file per Rumble environment, with standard Java on every operating system.
 *
 * Only the refresh token is saved. The file is created so that only its owner may use it, its rights are read back,
 * its content is encrypted with AES-256-GCM using a key derived from the user, the home folder and the operating
 * system, and the environment name is protected context so a file from one environment is useless in another. This
 * keeps a copied file closed, but a program running as the same user on the same computer can derive the same key.
 * Every problem ends the same way: nothing is saved or used, and the person signs in again.
 */
class SignInStore(
    private val environment: RumbleEnvironment,
    baseFolder: Path,
    private val identity: SignInIdentity = SignInIdentity.current(),
    private val protection: FileProtection = JavaFileProtection,
    private val enabled: Boolean = true,
) {
    private val folder: Path = environment.stateRoot(baseFolder)
    private val file: Path = folder.resolve(FILE_NAME)
    private val temp: Path = folder.resolve("$FILE_NAME.tmp")
    private val lockFile: Path = folder.resolve("signin.lock")
    private val key: SecretKey by lazy { deriveKey(identity.keyMaterial()) }

    @Serializable
    private class Payload(val refreshToken: String, val expiresAt: String? = null)

    /** Saves [refreshToken], or reports in plain words why it could not. */
    fun save(refreshToken: String, expiresAt: Instant?): SaveResult {
        if (!enabled) return SaveResult.NotSaved("Saving the sign-in is switched off.")
        try {
            Files.createDirectories(folder)
            Files.deleteIfExists(temp)
            protection.createOwnerOnly(temp)
            if (!protection.isOwnerOnly(temp)) {
                Files.deleteIfExists(temp)
                return SaveResult.NotSaved(PRIVATE)
            }
            val payload = Json.encodeToString(Payload.serializer(), Payload(refreshToken, expiresAt?.toString()))
            Files.write(temp, encrypt(payload.toByteArray(StandardCharsets.UTF_8)))
            moveIntoPlace()
            if (!protection.isOwnerOnly(file)) {
                Files.deleteIfExists(file)
                return SaveResult.NotSaved(PRIVATE)
            }
            return SaveResult.Saved
        } catch (e: IOException) {
            discard()
            return SaveResult.NotSaved("The sign-in could not be saved to this computer.")
        } catch (e: GeneralSecurityException) {
            discard()
            return SaveResult.NotSaved("The sign-in could not be encrypted on this computer.")
        } catch (e: UnsupportedOperationException) {
            discard()
            return SaveResult.NotSaved(PRIVATE)
        }
    }

    /** Loads the saved refresh token, or says that there is none or why it cannot be used. */
    fun load(): LoadResult {
        if (!enabled) return LoadResult.Disabled
        if (!Files.exists(file)) return LoadResult.Missing
        return try {
            if (!protection.isOwnerOnly(file)) {
                LoadResult.Unusable("The saved sign-in can be used by other people, so it was ignored. Sign in again.")
            } else {
                val plain = decrypt(Files.readAllBytes(file))
                val payload = Json.decodeFromString(Payload.serializer(), String(plain, StandardCharsets.UTF_8))
                LoadResult.Found(payload.refreshToken, payload.expiresAt?.let { Instant.parse(it) })
            }
        } catch (e: GeneralSecurityException) {
            LoadResult.Unusable(
                "The saved sign-in could not be opened. It may belong to another user or computer, or it may have been changed. Sign in again."
            )
        } catch (e: IOException) {
            LoadResult.Unusable("The saved sign-in could not be read. Sign in again.")
        } catch (e: IllegalArgumentException) {
            LoadResult.Unusable("The saved sign-in is damaged. Sign in again.")
        } catch (e: java.time.format.DateTimeParseException) {
            LoadResult.Unusable("The saved sign-in is damaged. Sign in again.")
        }
    }

    /** Removes the saved sign-in. It is not an error when there is none. */
    fun forget() {
        discard()
        try {
            Files.deleteIfExists(file)
        } catch (e: IOException) {
            // Nothing more can be done; load() refuses a file it cannot trust.
        }
    }

    /**
     * Runs [action] while holding a lock on the saved sign-in, so programs that share it take turns. The lock holds
     * across programs through a file lock and across threads of this program through a normal lock.
     */
    fun <T> locked(action: () -> T): T {
        Files.createDirectories(folder)
        val inProgram = IN_PROGRAM_LOCKS.computeIfAbsent(lockFile.toAbsolutePath().toString()) { ReentrantLock() }
        inProgram.lock()
        try {
            FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE).use { channel ->
                channel.lock().use { return action() }
            }
        } finally {
            inProgram.unlock()
        }
    }

    private fun discard() {
        try {
            Files.deleteIfExists(temp)
        } catch (e: IOException) {
            // The temporary file holds no readable token text; leave it for the next save to replace.
        }
    }

    private fun moveIntoPlace() {
        try {
            Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: AtomicMoveNotSupportedException) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    private fun associatedData(): ByteArray = MAGIC + environment.name.toByteArray(StandardCharsets.UTF_8)

    private fun encrypt(plain: ByteArray): ByteArray {
        val nonce = ByteArray(NONCE_BYTES).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_BITS, nonce))
        cipher.updateAAD(associatedData())
        val encrypted = cipher.doFinal(plain)
        return ByteBuffer.allocate(MAGIC.size + nonce.size + encrypted.size).put(MAGIC).put(nonce).put(encrypted).array()
    }

    private fun decrypt(stored: ByteArray): ByteArray {
        if (stored.size < MAGIC.size + NONCE_BYTES + TAG_BITS / 8) throw GeneralSecurityException("too short")
        if (!stored.copyOfRange(0, MAGIC.size).contentEquals(MAGIC)) throw GeneralSecurityException("not a sign-in file")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, stored, MAGIC.size, NONCE_BYTES))
        cipher.updateAAD(associatedData())
        val start = MAGIC.size + NONCE_BYTES
        return cipher.doFinal(stored, start, stored.size - start)
    }

    private fun deriveKey(material: String): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val bytes = factory.generateSecret(PBEKeySpec(material.toCharArray(), APP_SALT, ITERATIONS, 256)).encoded
        return SecretKeySpec(bytes, "AES")
    }

    companion object {
        private const val FILE_NAME = "signin.bin"
        private const val NONCE_BYTES = 12
        private const val TAG_BITS = 128
        private const val ITERATIONS = 100_000
        private const val PRIVATE = "This computer cannot keep the saved sign-in private to you, so it was not saved. You will sign in again next time."
        private val MAGIC = "TRS1".toByteArray(StandardCharsets.US_ASCII)
        private val APP_SALT = "robocode-tank-royale/signin-file/v1".toByteArray(StandardCharsets.UTF_8)
        private val IN_PROGRAM_LOCKS = ConcurrentHashMap<String, ReentrantLock>()

        /** A store in the Tank Royale user folder for this operating system. */
        fun inUserFolder(environment: RumbleEnvironment, enabled: Boolean = true) =
            SignInStore(environment, UserDataDirectory.get().toPath(), enabled = enabled)
    }
}
