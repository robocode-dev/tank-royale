package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.common.util.UserDataDirectory
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.charset.StandardCharsets
import java.nio.file.AccessDeniedException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.security.GeneralSecurityException
import java.security.SecureRandom
import java.time.Instant
import java.util.UUID
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
    /**
     * A saved sign-in and what belongs to it: [signInId] is the same from the person's approval until they sign out, and
     * [account] and [registration] are what is known of it so far. Its text form hides the token.
     */
    class Found(
        val refreshToken: String,
        val refreshTokenExpiresAt: Instant?,
        val signInId: String = "",
        val account: String? = null,
        val registration: RegistrationRecord? = null,
    ) : LoadResult {
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
 * A sign-in code waiting for the person's approval, kept between runs of a program. The device code is a secret and
 * is never shown; the short code the person types is useless without their signed-in browser and may be shown again.
 */
class WaitingSignIn internal constructor(
    /** The short code to show the person. */
    val userCode: String,
    /** The address the person opens to enter the code. */
    val verificationUri: String,
    /** When the code stops working. */
    val expiresAt: Instant,
    /** The fewest seconds GitHub wants between checks now. */
    val intervalSeconds: Int,
    /** The earliest time GitHub may be asked again. */
    val nextCheckAt: Instant,
    internal val deviceCode: String,
) {
    internal fun paced(interval: Int, next: Instant) = WaitingSignIn(userCode, verificationUri, expiresAt, interval, next, deviceCode)

    override fun toString(): String = "WaitingSignIn(userCode=$userCode, expiresAt=$expiresAt, nextCheckAt=$nextCheckAt)"
}

/**
 * Keeps the sign-in between runs in one file per Rumble environment, with standard Java on every operating system.
 *
 * The file holds the refresh token and what belongs to that sign-in: its identity, the account name and the
 * registration, and a code waiting for approval. Because it is one file written in one step, those parts cannot get out
 * of step and signing out removes them all. The access token is never saved. The file is created so that only its owner may use it, its rights are read back,
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
    private val lockFile: Path = folder.resolve(LOCK_FILE_NAME)
    private val key: SecretKey by lazy { deriveKey(identity.keyMaterial()) }

    @Serializable
    private class SavedCode(
        val userCode: String,
        val verificationUri: String,
        val expiresAt: String,
        val intervalSeconds: Int,
        val nextCheckAt: String,
        val deviceCode: String,
    )

    /** Everything saved for this environment, written as one, so its parts can never get out of step. */
    @Serializable
    private data class Payload(
        val refreshToken: String? = null,
        val expiresAt: String? = null,
        val signInId: String? = null,
        val account: String? = null,
        val clientId: String? = null,
        val registeredAt: String? = null,
        val waiting: SavedCode? = null,
    )

    /**
     * Saves a renewed [refreshToken] for the sign-in already saved, keeping what belongs to it, or starts a new one when
     * none is saved. Reports in plain words why it could not.
     */
    fun save(refreshToken: String, expiresAt: Instant?): SaveResult = changing { now ->
        if (now?.refreshToken == null) beginSignIn(refreshToken, expiresAt)
        else write(now.copy(refreshToken = refreshToken, expiresAt = expiresAt?.toString()))
    }

    /** Saves a sign-in the person has just approved. It has identity [signInId] and nothing from any earlier sign-in. */
    fun beginSignIn(refreshToken: String, expiresAt: Instant?, signInId: String = UUID.randomUUID().toString()): SaveResult =
        write(Payload(refreshToken, expiresAt?.toString(), signInId))

    /**
     * Saves [account] as the name of sign-in [signInId], unless that is no longer the saved sign-in. A registration is
     * for one account name, so a name that has changed on GitHub leaves it behind. Takes the lock.
     */
    fun saveAccount(signInId: String, account: String): Boolean = updating(signInId) { now ->
        if (account.equals(now.account, ignoreCase = true)) now.copy(account = account)
        else now.copy(account = account, clientId = null, registeredAt = null)
    }

    /**
     * Where a registration is remembered for sign-in [signInId]. Once that is no longer the saved sign-in, a registration
     * finishing late saves nothing, so it can never be taken for another account's. It takes the lock.
     */
    fun registrationFor(signInId: String): RegistrationMemory = object : RegistrationMemory {
        override fun load(account: String): RegistrationRecord? =
            (this@SignInStore.load() as? LoadResult.Found)?.takeIf { it.signInId == signInId }?.registration
                ?.takeIf { it.account.equals(account, ignoreCase = true) }

        override fun save(record: RegistrationRecord): Boolean = updating(signInId) { now ->
            now.copy(account = record.account, clientId = record.clientId, registeredAt = record.savedAt.toString())
        }
    }

    /** Changes the saved sign-in if it is still [signInId]. Returns false when it is not, or it could not be done. */
    private fun updating(signInId: String, change: (Payload) -> Payload): Boolean =
        try {
            locked {
                val now = forChanging()
                now?.refreshToken != null && now.signInId.orEmpty() == signInId && write(change(now)) == SaveResult.Saved
            }
        } catch (e: IOException) {
            false
        }

    /** Runs [action] on what is saved. A file that cannot be read just now is left alone rather than written over. */
    private fun changing(action: (Payload?) -> SaveResult): SaveResult =
        try {
            action(forChanging())
        } catch (e: IOException) {
            SaveResult.NotSaved("The saved sign-in could not be read just now, so it was left as it is.")
        }

    /** Saves a code that is waiting for the person's approval. */
    fun saveWaiting(waiting: WaitingSignIn): SaveResult = changing { now ->
        write(
            (now ?: Payload()).copy(
                waiting = SavedCode(
                    waiting.userCode, waiting.verificationUri, waiting.expiresAt.toString(), waiting.intervalSeconds,
                    waiting.nextCheckAt.toString(), waiting.deviceCode,
                ),
            ),
        )
    }

    /** The code that is waiting, or null when none is, or the file cannot be trusted. */
    fun loadWaiting(): WaitingSignIn? {
        val code = current()?.waiting ?: return null
        return try {
            WaitingSignIn(code.userCode, code.verificationUri, Instant.parse(code.expiresAt), code.intervalSeconds, Instant.parse(code.nextCheckAt), code.deviceCode)
        } catch (e: java.time.format.DateTimeParseException) {
            null
        }
    }

    /** Removes the waiting code and keeps the rest. */
    fun forgetWaiting() {
        changing { now -> if (now?.waiting != null) write(now.copy(waiting = null)) else SaveResult.Saved }
    }

    /** True unless nothing is known to be saved in this store, without creating anything. */
    fun hasSavedFiles(): Boolean = listOf(file, temp).any { !Files.notExists(it) }

    /** True when saving is on and no sign-in is known to be saved, so no lock is needed to find that out. */
    fun knownMissing(): Boolean = enabled && Files.notExists(file)

    /**
     * True when a file in this folder can be kept private to its owner, checked with a small file that is removed
     * again, so a person is never asked to approve a sign-in that could not be kept. It creates the state folder.
     */
    fun canKeepPrivate(): Boolean {
        if (!enabled) return false
        // A name of its own, so programs starting at the same time do not disturb each other's check.
        val probe = folder.resolve("$PROBE_PREFIX${UUID.randomUUID()}")
        return try {
            Files.createDirectories(folder)
            protection.createOwnerOnly(probe)
            protection.isOwnerOnly(probe)
        } catch (e: IOException) {
            false
        } catch (e: UnsupportedOperationException) {
            false
        } finally {
            runCatching { Files.deleteIfExists(probe) }
        }
    }

    private fun write(payload: Payload): SaveResult {
        if (!enabled) return SaveResult.NotSaved("Saving the sign-in is switched off.")
        if (payload.refreshToken == null && payload.waiting == null) {
            return if (forget()) SaveResult.Saved else SaveResult.NotSaved("The sign-in could not be saved to this computer.")
        }
        try {
            Files.createDirectories(folder)
            Files.deleteIfExists(temp)
            protection.createOwnerOnly(temp)
            if (!protection.isOwnerOnly(temp)) {
                Files.deleteIfExists(temp)
                return SaveResult.NotSaved(PRIVATE)
            }
            val text = Json.encodeToString(Payload.serializer(), payload)
            Files.write(temp, encrypt(text.toByteArray(StandardCharsets.UTF_8)))
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

    /** What is saved, or null when nothing is, it cannot be trusted or it cannot be read. */
    private fun current(): Payload? =
        try {
            forChanging()
        } catch (e: IOException) {
            null
        }

    /**
     * What is saved, for changing it. A file that cannot be trusted counts as nothing, so the person can sign in again
     * and replace it. A file that cannot be read right now throws, because it may hold a good sign-in.
     */
    private fun forChanging(): Payload? {
        if (!enabled || !Files.exists(file)) return null
        return try {
            if (protection.isOwnerOnly(file)) read() else null
        } catch (e: GeneralSecurityException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun read(): Payload =
        Json.decodeFromString(Payload.serializer(), String(decrypt(Files.readAllBytes(file)), StandardCharsets.UTF_8))

    private fun found(payload: Payload): LoadResult {
        val refreshToken = payload.refreshToken ?: return LoadResult.Missing
        val account = payload.account
        val clientId = payload.clientId
        val registeredAt = payload.registeredAt
        val registration =
            if (account != null && clientId != null && registeredAt != null) RegistrationRecord(account, clientId, environment.name, Instant.parse(registeredAt)) else null
        return LoadResult.Found(refreshToken, payload.expiresAt?.let { Instant.parse(it) }, payload.signInId.orEmpty(), account, registration)
    }

    /** Loads the saved refresh token, or says that there is none or why it cannot be used. */
    fun load(): LoadResult {
        if (!enabled) return LoadResult.Disabled
        if (!Files.exists(file)) return LoadResult.Missing
        return try {
            if (!protection.isOwnerOnly(file)) {
                LoadResult.Unusable("The saved sign-in can be used by other people, so it was ignored. Sign in again.")
            } else {
                found(read())
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

    /** Removes everything saved here. It is not an error when nothing is. Returns false when it is still there. */
    fun forget(): Boolean {
        discard()
        try {
            Files.deleteIfExists(file)
        } catch (e: IOException) {
            // Nothing more can be done; load() refuses a file it cannot trust.
        }
        return Files.notExists(file)
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

    /**
     * Moves the new file into place. On Windows a move can be refused for a moment while another program, such as a virus
     * scanner, has the old file open, and failing here can lose a sign-in GitHub has already replaced, so it is tried again.
     */
    private fun moveIntoPlace() {
        var triesLeft = MOVE_TRIES
        while (true) {
            try {
                try {
                    Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
                } catch (e: AtomicMoveNotSupportedException) {
                    Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING)
                }
                return
            } catch (e: AccessDeniedException) {
                if (--triesLeft == 0) throw e
                Thread.sleep(MOVE_PAUSE_MILLIS)
            }
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
        private const val MOVE_TRIES = 5
        private const val MOVE_PAUSE_MILLIS = 100L
        private const val LOCK_FILE_NAME = "signin.lock"
        private const val PROBE_PREFIX = "signin.probe."

        /** True for the store's own files that hold nothing: its lock and a check left behind by a stopped program. */
        fun holdsNothing(fileName: String): Boolean = fileName == LOCK_FILE_NAME || fileName.startsWith(PROBE_PREFIX)
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
