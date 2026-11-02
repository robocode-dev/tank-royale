package dev.robocode.tankroyale.rumble

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.IOException
import java.io.InputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption
import java.security.MessageDigest
import java.time.Clock

/** The parts of the old Rumble client's configuration file that the import understands. */
class LegacyClientConfiguration(
    val botsRepository: String,
    val dataRepository: String,
    /** The registered client ID, or null when it is empty or still the example's placeholder. */
    val clientId: String?,
    val myBots: List<String>,
    val gameTypes: List<String>,
    val battlesPerSession: Int,
    val mode: String,
    val workDirectory: String,
)

/** One file the import would copy, or has copied: its path below the old work folder, size and SHA-256 hash. */
class LegacyFile(val path: String, val size: Long, val sha256: String? = null)

/** Something in the old work folder that is not copied, and why. */
class NotCopied(val path: String, val reason: String)

/** What an import would do. */
class LegacyImportPlan(
    val environment: RumbleEnvironment,
    val configuration: LegacyClientConfiguration,
    val workFolder: Path,
    val target: Path,
    val files: List<LegacyFile>,
    val notCopied: List<NotCopied>,
)

sealed interface LegacyPreview {
    data class Plan(val plan: LegacyImportPlan) : LegacyPreview

    /** There is nothing to copy. */
    data class NothingToImport(val message: String) : LegacyPreview

    /** The configuration or the folder cannot be used; nothing was copied. */
    data class Refused(val message: String) : LegacyPreview
}

sealed interface LegacyImportResult {
    /** Every file is copied and checked and the record is written. [copied] were new or changed, [alreadyCopied] were unchanged. */
    data class Imported(val environment: String, val copied: Int, val alreadyCopied: Int, val target: Path) : LegacyImportResult

    data class NothingToImport(val message: String) : LegacyImportResult

    /** The configuration or the folder cannot be used; nothing was copied. */
    data class Refused(val message: String) : LegacyImportResult

    /** Some files changed while they were read, so the record was not written. Run the import again. */
    data class Incomplete(val copied: Int, val changedDuringImport: List<String>) : LegacyImportResult

    data class Failed(val message: String) : LegacyImportResult
}

/** The preferences read from the old configuration, saved for the environment they belong to. */
class ImportedPreferences(
    val clientId: String?,
    val myBots: List<String>,
    val gameTypes: List<String>,
    val battlesPerSession: Int,
    val mode: String,
) {
    companion object {
        /** The saved preferences of [environment], or null when none are saved or the file cannot be read. */
        fun load(environment: RumbleEnvironment, baseFolder: Path): ImportedPreferences? {
            val file = LegacyClientImport.targetOf(environment, baseFolder).resolve(LegacyClientImport.PREFERENCES_FILE)
            return try {
                val stored = Json { ignoreUnknownKeys = true }.decodeFromString<StoredPreferences>(Files.readString(file))
                if (stored.schemaVersion != 1) null else ImportedPreferences(stored.clientId, stored.myBots, stored.gameTypes, stored.battlesPerSession, stored.mode)
            } catch (e: IOException) {
                null
            } catch (e: SerializationException) {
                null
            }
        }
    }
}

@Serializable
internal class StoredPreferences(
    val schemaVersion: Int,
    val environment: String,
    val clientId: String?,
    val myBots: List<String>,
    val gameTypes: List<String>,
    val battlesPerSession: Int,
    val mode: String,
)

/**
 * Copies the old, separate Rumble client's state into the new Rumble state folder without changing or losing any of it.
 * The old configuration file and work folder are only ever read. The journal and the replay evidence are copied byte
 * for byte, each under a temporary name, checked by SHA-256 against the source and only then given its final name, and
 * a record is written last: it is the only thing that says the import is complete. Nothing is converted. Running the
 * import again completes an interrupted one and copies nothing twice.
 */
class LegacyClientImport(
    private val stateBase: Path,
    private val clock: Clock = Clock.systemUTC(),
    private val programVersion: String = "unknown",
    /** A seam for tests: called with the source and the temporary copy after copying and before the check. */
    private val betweenCopyAndCheck: (source: Path, temp: Path) -> Unit = { _, _ -> },
) {
    private class Refusal(message: String) : Exception(message, null, false, false)

    fun preview(configurationFile: Path, workFolder: Path? = null): LegacyPreview =
        try {
            val plan = plan(configurationFile, workFolder)
            if (plan.files.isEmpty()) LegacyPreview.NothingToImport("The old work folder holds no journal or replay evidence to copy.")
            else LegacyPreview.Plan(plan)
        } catch (refusal: Refusal) {
            LegacyPreview.Refused(refusal.message.orEmpty())
        } catch (e: IOException) {
            LegacyPreview.Refused("The old client's files could not be read.")
        }

    fun import(configurationFile: Path, workFolder: Path? = null): LegacyImportResult {
        val plan = try {
            plan(configurationFile, workFolder)
        } catch (refusal: Refusal) {
            return LegacyImportResult.Refused(refusal.message.orEmpty())
        } catch (e: IOException) {
            return LegacyImportResult.Refused("The old client's files could not be read.")
        }
        if (plan.files.isEmpty()) return LegacyImportResult.NothingToImport("The old work folder holds no journal or replay evidence to copy.")

        val target = plan.target
        val filesFolder = target.resolve(FILES_FOLDER)
        try {
            Files.createDirectories(filesFolder)
            // An earlier record says an earlier import completed. This one has not, so it must not be left standing.
            Files.deleteIfExists(target.resolve(RECORD_FILE))
            removeLeftovers(filesFolder)
        } catch (e: IOException) {
            return LegacyImportResult.Failed("The folder for the imported old state could not be made.")
        }

        var copied = 0
        var already = 0
        val changed = mutableListOf<String>()
        val done = mutableListOf<LegacyFile>()
        for (file in plan.files) {
            val source = plan.workFolder.resolve(file.path)
            val destination = filesFolder.resolve(file.path)
            try {
                when (val outcome = copyOne(source, destination)) {
                    is Copy.Skipped -> { already++; done += LegacyFile(file.path, outcome.size, outcome.sha256) }
                    is Copy.Copied -> { copied++; done += LegacyFile(file.path, outcome.size, outcome.sha256) }
                    Copy.Changed -> changed += file.path
                }
            } catch (e: IOException) {
                return LegacyImportResult.Failed("The file ${file.path} could not be copied. Check the free space and try again.")
            }
        }
        if (changed.isNotEmpty()) return LegacyImportResult.Incomplete(copied, changed)

        return try {
            savePreferences(plan)
            writeRecord(plan, done)
            LegacyImportResult.Imported(plan.environment.name, copied, already, target)
        } catch (e: IOException) {
            LegacyImportResult.Failed("The record of the import could not be written.")
        }
    }

    // ---- Planning ----

    private fun plan(configurationFile: Path, workFolderOverride: Path?): LegacyImportPlan {
        val configuration = readConfiguration(configurationFile)
        val environment = environmentOf(configuration)
        val workFolder = (workFolderOverride ?: resolveWorkFolder(configurationFile, configuration.workDirectory)).toAbsolutePath().normalize()
        if (!Files.isDirectory(workFolder)) throw Refusal("The old client's work folder does not exist: it should be where the configuration says (${configuration.workDirectory}).")
        val target = targetOf(environment, stateBase).toAbsolutePath().normalize()
        if (target.startsWith(workFolder) || workFolder.startsWith(target)) {
            throw Refusal("The old client's work folder and the folder for the imported old state must be separate folders.")
        }
        val files = mutableListOf<LegacyFile>()
        val notCopied = mutableListOf<NotCopied>()
        Files.list(workFolder).use { entries ->
            for (entry in entries.sorted().collect(java.util.stream.Collectors.toList())) {
                val name = entry.fileName.toString()
                if (name in COPIED_FOLDERS && Files.isDirectory(entry, java.nio.file.LinkOption.NOFOLLOW_LINKS)) {
                    Files.walk(entry).use { walk ->
                        walk.sorted().forEach { path ->
                            val relative = workFolder.relativize(path).toString().replace("\\", "/")
                            when {
                                Files.isSymbolicLink(path) -> notCopied += NotCopied(relative, "A symbolic link is never followed.")
                                Files.isRegularFile(path) -> files += LegacyFile(relative, Files.size(path))
                            }
                        }
                    }
                } else {
                    notCopied += NotCopied(name, reasonNotCopied(name, entry))
                }
            }
        }
        return LegacyImportPlan(environment, configuration, workFolder, target, files, notCopied)
    }

    private fun reasonNotCopied(name: String, entry: Path): String = when {
        Files.isSymbolicLink(entry) -> "A symbolic link is never followed."
        name in CACHE_FOLDERS -> "The bot cache can be downloaded again and checked against the catalog."
        else -> "The import does not know this file or folder, so it is left where it is."
    }

    private fun resolveWorkFolder(configurationFile: Path, workDirectory: String): Path {
        val path = Path.of(workDirectory)
        return if (path.isAbsolute) path else (configurationFile.toAbsolutePath().parent ?: Path.of(".")).resolve(path)
    }

    private fun readConfiguration(file: Path): LegacyClientConfiguration {
        val text = try {
            Files.readString(file)
        } catch (e: IOException) {
            throw Refusal("The old client's configuration file could not be read.")
        }
        val stored = try {
            Json { ignoreUnknownKeys = true }.decodeFromString<StoredConfiguration>(text)
        } catch (e: SerializationException) {
            throw Refusal("The old client's configuration is not valid: ${e.message?.lineSequence()?.firstOrNull()}")
        } catch (e: IllegalArgumentException) {
            throw Refusal("The old client's configuration is not valid: ${e.message?.lineSequence()?.firstOrNull()}")
        }
        if (stored.schemaVersion != SUPPORTED_CONFIGURATION_VERSION) {
            throw Refusal(
                "The old client's configuration uses schema version ${stored.schemaVersion}, " +
                    "but this program understands version $SUPPORTED_CONFIGURATION_VERSION. Update the program."
            )
        }
        if (stored.workDirectory.isBlank()) throw Refusal("The old client's configuration does not say where its work folder is.")
        val id = stored.clientId?.trim().orEmpty()
        val clientId = if (id.isEmpty() || id == PLACEHOLDER_CLIENT_ID) null else id
        if (clientId != null && !CLIENT_ID.matches(clientId)) throw Refusal("The old client's client ID is not a valid identifier.")
        return LegacyClientConfiguration(
            stored.botsRepo, stored.dataRepo, clientId, stored.myBots, stored.gameTypes, stored.battlesPerSession, stored.mode, stored.workDirectory,
        )
    }

    private fun environmentOf(configuration: LegacyClientConfiguration): RumbleEnvironment {
        val bots = environmentOfAddress(configuration.botsRepository, "bot repository")
        val data = environmentOfAddress(configuration.dataRepository, "result repository")
        if (bots != data) {
            throw Refusal("The old client's bot repository belongs to the ${bots.displayName} Rumble but its result repository to the ${data.displayName} Rumble. Nothing was copied.")
        }
        if (RumbleEnvironments.repositorySlug(configuration.botsRepository) != "${RumbleEnvironments.owner}/${bots.botsRepository}".lowercase()) {
            throw Refusal("The old client's bot repository address is a result repository of the ${bots.displayName} Rumble.")
        }
        if (RumbleEnvironments.repositorySlug(configuration.dataRepository) != "${RumbleEnvironments.owner}/${bots.dataRepository}".lowercase()) {
            throw Refusal("The old client's result repository address is a bot repository of the ${bots.displayName} Rumble.")
        }
        return bots
    }

    private fun environmentOfAddress(address: String, what: String): RumbleEnvironment =
        try {
            RumbleEnvironments.of(address)
        } catch (e: EnvironmentRefused) {
            null
        } ?: throw Refusal("The old client's $what address ($address) is not a repository of a known Rumble. Nothing was copied.")

    // ---- Copying ----

    private sealed interface Copy {
        class Skipped(val size: Long, val sha256: String) : Copy
        class Copied(val size: Long, val sha256: String) : Copy
        data object Changed : Copy
    }

    private fun copyOne(source: Path, destination: Path): Copy {
        val before = Snapshot.of(source)
        val sourceHash = hashOf(source)
        if (Files.isRegularFile(destination) && hashOf(destination) == sourceHash && Snapshot.of(source) == before) {
            return Copy.Skipped(before.size, sourceHash)
        }
        Files.createDirectories(destination.parent)
        val temp = destination.resolveSibling(destination.fileName.toString() + PART_SUFFIX)
        val copiedHash = try {
            copyAndHash(source, temp)
        } catch (e: IOException) {
            Files.deleteIfExists(temp)
            throw e
        }
        betweenCopyAndCheck(source, temp)
        val after = Snapshot.of(source)
        if (copiedHash != sourceHash || after != before || hashOf(temp) != copiedHash) {
            Files.deleteIfExists(temp)
            return Copy.Changed
        }
        try {
            Files.move(temp, destination, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: AtomicMoveNotSupportedException) {
            Files.move(temp, destination, StandardCopyOption.REPLACE_EXISTING)
        }
        return Copy.Copied(before.size, copiedHash)
    }

    /** What is seen of a file from outside: its size and when it was last changed. */
    private data class Snapshot(val size: Long, val modified: Long) {
        companion object {
            fun of(path: Path) = Snapshot(Files.size(path), Files.getLastModifiedTime(path).toMillis())
        }
    }

    private fun copyAndHash(source: Path, temp: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(source).use { input ->
            Files.newOutputStream(temp, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE).use { output ->
                val buffer = ByteArray(BUFFER)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    digest.update(buffer, 0, count)
                    output.write(buffer, 0, count)
                }
            }
        }
        return hex(digest.digest())
    }

    private fun hashOf(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).use { digestAll(it, digest) }
        return hex(digest.digest())
    }

    private fun digestAll(input: InputStream, digest: MessageDigest) {
        val buffer = ByteArray(BUFFER)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            digest.update(buffer, 0, count)
        }
    }

    private fun hex(bytes: ByteArray) = "sha256:" + bytes.joinToString("") { "%02x".format(it) }

    /** Removes the temporary files an interrupted import left in the imported-old-state folder, never anything else. */
    private fun removeLeftovers(folder: Path) {
        Files.walk(folder).use { walk ->
            walk.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(PART_SUFFIX) }.forEach { Files.deleteIfExists(it) }
        }
    }

    // ---- Preferences and the record ----

    private fun savePreferences(plan: LegacyImportPlan) {
        val c = plan.configuration
        val stored = StoredPreferences(1, plan.environment.name, c.clientId, c.myBots, c.gameTypes, c.battlesPerSession, c.mode)
        writeAtomically(plan.target.resolve(PREFERENCES_FILE), Json { prettyPrint = true }.encodeToString(StoredPreferences.serializer(), stored))
    }

    @Serializable
    private class RecordFile(val path: String, val size: Long, val sha256: String)

    @Serializable
    private class Record(
        val schemaVersion: Int,
        val complete: Boolean,
        val environment: String,
        val importedAt: String,
        val programVersion: String,
        val files: List<RecordFile>,
    )

    private fun writeRecord(plan: LegacyImportPlan, files: List<LegacyFile>) {
        val record = Record(
            1, true, plan.environment.name, clock.instant().toString(), programVersion,
            files.map { RecordFile(it.path, it.size, it.sha256.orEmpty()) },
        )
        writeAtomically(plan.target.resolve(RECORD_FILE), Json { prettyPrint = true }.encodeToString(Record.serializer(), record))
    }

    private fun writeAtomically(file: Path, text: String) {
        val temp = file.resolveSibling(file.fileName.toString() + PART_SUFFIX)
        Files.writeString(temp, text + "\n")
        try {
            Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } catch (e: AtomicMoveNotSupportedException) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    @Serializable
    private class StoredConfiguration(
        val schemaVersion: Int,
        val botsRepo: String,
        val dataRepo: String,
        val clientId: String? = null,
        val myBots: List<String> = emptyList(),
        val gameTypes: List<String> = emptyList(),
        val battlesPerSession: Int = 0,
        val mode: String = "ranked",
        val workDirectory: String = "",
    )

    companion object {
        const val SUPPORTED_CONFIGURATION_VERSION = 1
        const val RECORD_FILE = "import-record.json"
        const val PREFERENCES_FILE = "preferences.json"
        const val FILES_FOLDER = "files"
        private const val PART_SUFFIX = ".part"
        private const val BUFFER = 64 * 1024
        private const val PLACEHOLDER_CLIENT_ID = "replace-with-registered-client-id"
        private val COPIED_FOLDERS = setOf("journal", "evidence")
        private val CACHE_FOLDERS = setOf("cache", "bots", "sources")
        private val CLIENT_ID = Regex("[A-Za-z0-9._-]{1,80}")

        /** The folder of imported old state for [environment]. */
        fun targetOf(environment: RumbleEnvironment, baseFolder: Path): Path = environment.stateRoot(baseFolder).resolve("legacy-client")
    }
}
