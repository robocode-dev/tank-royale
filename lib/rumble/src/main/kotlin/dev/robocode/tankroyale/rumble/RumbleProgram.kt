package dev.robocode.tankroyale.rumble

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.longOrNull
import java.io.IOException
import java.io.InputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.AccessDeniedException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.FileSystemException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.time.Duration
import java.util.concurrent.TimeUnit

/** Where the Rumble command line is, and whether it is the version the desktop program needs. */
sealed interface ProgramStatus {
    /** The program is there and fits. [version] is null for a developer's own jar, which is not run to ask. */
    data class Ready(val jar: Path, val version: String?, val developerChoice: Boolean) : ProgramStatus

    /** Nothing is at [jar]. */
    data class Missing(val jar: Path) : ProgramStatus

    /** A file is at [jar] but it is not the wanted version, or it did not answer its version ([found] is null). */
    data class WrongVersion(val jar: Path, val found: String?) : ProgramStatus
}

/** Why the program could not be planned for or fetched. Each has a plain message in [ProgramUnavailable]. */
enum class ProgramProblem {
    NO_RELEASE, NO_FILE, NO_HASH, TOO_BIG, RATE_LIMITED, OFFLINE, BAD_ANSWER,
    DOWNLOAD_FAILED, HASH_MISMATCH, WRONG_VERSION, IN_USE, NOT_WRITABLE, DEVELOPER_JAR_UNUSABLE,
}

/** The program cannot be had right now, for [problem], in the plain words of [message]. Nothing was changed. */
data class ProgramUnavailable(val problem: ProgramProblem, val message: String)

/** What fetching the program would involve, so a screen can ask the person first and show the size. */
sealed interface FetchPlan {
    /** Nothing needs fetching: the program is [status], whether installed or the developer's own jar. */
    data class NotNeeded(val status: ProgramStatus.Ready) : FetchPlan

    /** The release [version] holds the file [fileName] of [size] bytes with the SHA-256 [sha256], to be fetched from [url]. */
    data class Needed(val version: String, val fileName: String, val size: Long, val sha256: String, val url: String) : FetchPlan

    data class Unavailable(val reason: ProgramUnavailable) : FetchPlan
}

/** How fetching the program ended. Only [Fetched] leaves anything at the program's place. */
sealed interface ProgramFetch {
    data class Fetched(val jar: Path, val version: String) : ProgramFetch

    data class Failed(val reason: ProgramUnavailable) : ProgramFetch
}

/** Opens the bytes of a file on the release. Throws [IOException] when it cannot. */
fun interface ProgramDownload {
    fun open(url: String): InputStream
}

/** Asks a program file for its version by running it. Returns null when it does not answer as the Rumble command line. */
fun interface ProgramVersionAsking {
    fun ask(jar: Path): String?
}

/**
 * Finds, plans the fetching of, and fetches the Rumble command line, `rumble.jar`, for the desktop program of
 * [version]. The program is kept at `<userData>/rumble/rumble.jar`.
 *
 * It reads exactly the release `v<version>` of the Tank Royale repository and the one file named for that version,
 * and builds the download address itself. The file is downloaded under a temporary name, compared with the size and
 * the SHA-256 that GitHub reports for it, asked for its version, and only then moved into place. Any failure leaves
 * no file and never touches an earlier good copy. The hash protects against a broken or changed file, not against a
 * release that has been taken over.
 *
 * A developer's jar named by the system property [PROPERTY] is used instead, and then nothing is fetched.
 */
class RumbleProgram(
    private val userData: Path,
    val version: String,
    private val api: GitHubApi,
    private val download: ProgramDownload = HttpProgramDownload(),
    private val asking: ProgramVersionAsking = JavaProgramVersion,
    private val developerJar: () -> String? = { System.getProperty(PROPERTY) },
    private val sizeCeiling: Long = SIZE_CEILING,
) {
    init {
        require(VERSION.matches(version)) { "Not a version: $version" }
    }

    /** Where the program is kept. */
    val jar: Path = userData.resolve(FOLDER).resolve(FILE)

    private val fileName = "robocode-tankroyale-rumble-cli-$version.jar"

    /** Looks, and changes nothing. It runs the installed file to ask its version, so a screen calls it off its event thread. */
    fun look(): ProgramStatus {
        if (!Files.isRegularFile(jar)) return ProgramStatus.Missing(jar)
        val found = asking.ask(jar)
        return if (found == version) ProgramStatus.Ready(jar, version, developerChoice = false) else ProgramStatus.WrongVersion(jar, found)
    }

    /** What fetching would involve. Nothing is downloaded or written. Like [look] it can run the installed file, and it asks GitHub. */
    fun plan(): FetchPlan {
        developer()?.let { return it }
        val status = look()
        if (status is ProgramStatus.Ready) return FetchPlan.NotNeeded(status)

        val answer = try {
            api.get("/repos/$REPOSITORY/releases/tags/v$version", null)
        } catch (e: IOException) {
            return unavailable(ProgramProblem.OFFLINE, "GitHub could not be reached, so the Rumble program could not be looked up.")
        } catch (e: RuntimeException) {
            return unavailable(ProgramProblem.OFFLINE, "GitHub could not be asked about the Rumble program.")
        }
        when (answer.status) {
            200 -> Unit
            404 -> return unavailable(ProgramProblem.NO_RELEASE, "There is no release v$version, so there is no Rumble program to fetch for this version of Tank Royale.")
            403, 429 -> return unavailable(ProgramProblem.RATE_LIMITED, "GitHub refused the request (HTTP ${answer.status}), most often because of its limit on requests. Try again later.")
            in 500..599 -> return unavailable(ProgramProblem.OFFLINE, "GitHub is not answering properly right now (HTTP ${answer.status}). Try again later.")
            else -> return unavailable(ProgramProblem.BAD_ANSWER, "GitHub gave an unexpected answer (HTTP ${answer.status}) about the release v$version.")
        }
        val assets = try {
            ((Json.parseToJsonElement(answer.body) as? JsonObject)?.get("assets") as? JsonArray)
        } catch (e: IllegalArgumentException) {
            null
        } ?: return unavailable(ProgramProblem.BAD_ANSWER, "GitHub's answer about the release v$version could not be understood.")
        val asset = assets.mapNotNull { it as? JsonObject }.firstOrNull { text(it["name"]) == fileName }
            ?: return unavailable(ProgramProblem.NO_FILE, "The release v$version holds no Rumble program ($fileName). Only releases made after the program was added have it.")
        val digest = text(asset["digest"])
        val sha256 = digest?.removePrefix("sha256:")?.takeIf { digest.startsWith("sha256:") && SHA256.matches(it) }
            ?: return unavailable(ProgramProblem.NO_HASH, "GitHub reports no SHA-256 for $fileName, so it cannot be checked and is not fetched.")
        val size = ((asset["size"] as? JsonPrimitive)?.longOrNull)?.takeIf { it > 0 }
            ?: return unavailable(ProgramProblem.BAD_ANSWER, "GitHub reports no usable size for $fileName.")
        if (size > sizeCeiling) return unavailable(ProgramProblem.TOO_BIG, "$fileName is $size bytes, more than the $sizeCeiling bytes allowed, so it is not fetched.")
        return FetchPlan.Needed(version, fileName, size, sha256, "https://github.com/$REPOSITORY/releases/download/v$version/$fileName")
    }

    /** Fetches what [plan] describes and places it as `rumble.jar`, or says why not and leaves nothing behind. */
    fun fetch(plan: FetchPlan.Needed): ProgramFetch {
        if (plan.version != version || plan.fileName != fileName || plan.url != "https://github.com/$REPOSITORY/releases/download/v$version/$fileName" || !SHA256.matches(plan.sha256)) {
            return failed(ProgramProblem.BAD_ANSWER, "The plan does not belong to this version of Tank Royale, so nothing was fetched.")
        }
        val folder = jar.parent
        val part = try {
            Files.createDirectories(folder)
            Files.createTempFile(folder, "rumble-", ".part")
        } catch (e: IOException) {
            return failed(ProgramProblem.NOT_WRITABLE, "The folder for the Rumble program could not be written to.")
        }
        try {
            when (val received = receive(plan, part)) {
                null -> Unit
                else -> return ProgramFetch.Failed(received)
            }
            val found = asking.ask(part)
            if (found != version) {
                return failed(ProgramProblem.WRONG_VERSION, "The downloaded file did not say it is version $version (it said ${found ?: "nothing"}), so it was not kept.")
            }
            return place(part)
        } finally {
            try {
                Files.deleteIfExists(part)
            } catch (e: IOException) {
                // A temporary file that cannot be removed holds nothing the program uses.
            }
        }
    }

    private fun receive(plan: FetchPlan.Needed, part: Path): ProgramUnavailable? {
        val digest = MessageDigest.getInstance("SHA-256")
        var total = 0L
        try {
            download.open(plan.url).use { input ->
                Files.newOutputStream(part).use { output ->
                    val buffer = ByteArray(64 * 1024)
                    while (true) {
                        val n = input.read(buffer)
                        if (n < 0) break
                        total += n
                        if (total > plan.size || total > sizeCeiling) {
                            return ProgramUnavailable(ProgramProblem.TOO_BIG, "The download is larger than the ${plan.size} bytes GitHub reports, so it was stopped and nothing was kept.")
                        }
                        digest.update(buffer, 0, n)
                        output.write(buffer, 0, n)
                    }
                }
            }
        } catch (e: IOException) {
            return ProgramUnavailable(ProgramProblem.DOWNLOAD_FAILED, "The download of ${plan.fileName} did not finish, so nothing was kept.")
        } catch (e: RuntimeException) {
            return ProgramUnavailable(ProgramProblem.DOWNLOAD_FAILED, "The download of ${plan.fileName} failed, so nothing was kept.")
        }
        if (total != plan.size) {
            return ProgramUnavailable(ProgramProblem.DOWNLOAD_FAILED, "The download of ${plan.fileName} was $total bytes, not the ${plan.size} GitHub reports, so nothing was kept.")
        }
        val actual = digest.digest().joinToString("") { "%02x".format(it) }
        if (!actual.equals(plan.sha256, ignoreCase = true)) {
            return ProgramUnavailable(ProgramProblem.HASH_MISMATCH, "The downloaded file does not match the SHA-256 GitHub reports for it, so it was not kept.")
        }
        return null
    }

    private fun place(part: Path): ProgramFetch =
        try {
            try {
                Files.move(part, jar, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
            } catch (e: AtomicMoveNotSupportedException) {
                Files.move(part, jar, StandardCopyOption.REPLACE_EXISTING)
            }
            ProgramFetch.Fetched(jar, version)
        } catch (e: AccessDeniedException) {
            failed(ProgramProblem.IN_USE, "The Rumble program is in use, so it could not be replaced. Close what is using it and try again.")
        } catch (e: FileSystemException) {
            failed(ProgramProblem.IN_USE, "The Rumble program could not be replaced, probably because it is in use. Close what is using it and try again.")
        } catch (e: IOException) {
            failed(ProgramProblem.NOT_WRITABLE, "The Rumble program could not be put in place.")
        }

    /** The developer's own jar, when the property names one: ready if it is a file, unusable if not. */
    private fun developer(): FetchPlan? {
        val named = developerJar()?.takeIf { it.isNotBlank() } ?: return null
        val path = try {
            Path.of(named)
        } catch (e: RuntimeException) {
            return unavailable(ProgramProblem.DEVELOPER_JAR_UNUSABLE, "The setting $PROPERTY is not a path, so it is not used and nothing is fetched.")
        }
        return if (Files.isRegularFile(path)) FetchPlan.NotNeeded(ProgramStatus.Ready(path, null, developerChoice = true))
        else unavailable(ProgramProblem.DEVELOPER_JAR_UNUSABLE, "The setting $PROPERTY names a file that does not exist, so nothing is fetched. Remove the setting or name the jar.")
    }

    private fun unavailable(problem: ProgramProblem, message: String) = FetchPlan.Unavailable(ProgramUnavailable(problem, message))

    private fun failed(problem: ProgramProblem, message: String) = ProgramFetch.Failed(ProgramUnavailable(problem, message))

    private fun text(element: kotlinx.serialization.json.JsonElement?): String? = (element as? JsonPrimitive)?.takeIf { it.isString }?.contentOrNull

    companion object {
        /** The system property that names a developer's jar to use instead of fetching. */
        const val PROPERTY = "rumble.jar"

        private const val REPOSITORY = "robocode-dev/tank-royale"
        private const val FOLDER = "rumble"
        private const val FILE = "rumble.jar"
        private const val SIZE_CEILING = 200L * 1024 * 1024
        private val VERSION = Regex("[0-9A-Za-z][0-9A-Za-z.+-]*")
        private val SHA256 = Regex("[0-9a-fA-F]{64}")
    }
}

/**
 * Downloads over HTTPS from the Tank Royale releases on github.com. GitHub serves a release file from its own storage,
 * so up to five redirects are followed, each only over HTTPS and only to github.com or githubusercontent.com. The
 * file is trusted for nothing but its size and hash, which are checked by the caller.
 */
class HttpProgramDownload(
    private val client: HttpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).followRedirects(HttpClient.Redirect.NEVER).build(),
) : ProgramDownload {
    override fun open(url: String): InputStream {
        if (!url.startsWith("https://github.com/robocode-dev/tank-royale/releases/download/")) throw IOException("Only the Tank Royale releases on github.com are downloaded from.")
        var address = url
        repeat(MAX_REDIRECTS + 1) {
            val request = HttpRequest.newBuilder(URI.create(address)).timeout(Duration.ofMinutes(10)).header("User-Agent", "robocode-tank-royale").build()
            val response = try {
                client.send(request, HttpResponse.BodyHandlers.ofInputStream())
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
                throw IOException("The download was interrupted.")
            }
            when (response.statusCode()) {
                200 -> return response.body()
                301, 302, 303, 307, 308 -> {
                    response.body().close()
                    val next = response.headers().firstValue("Location").orElse(null) ?: throw IOException("GitHub redirected the download without saying where.")
                    address = URI.create(address).resolve(next).toString()
                    if (!allowed(address)) throw IOException("GitHub redirected the download somewhere that is not trusted, so it was stopped.")
                }
                else -> {
                    response.body().close()
                    throw IOException("GitHub answered HTTP ${response.statusCode()} for the download.")
                }
            }
        }
        throw IOException("GitHub redirected the download too many times.")
    }

    internal fun allowed(address: String): Boolean {
        val uri = try {
            URI.create(address)
        } catch (e: IllegalArgumentException) {
            return false
        }
        val host = uri.host?.lowercase() ?: return false
        return uri.scheme == "https" && (host == "github.com" || host.endsWith(".github.com") || host.endsWith(".githubusercontent.com"))
    }

    private companion object {
        const val MAX_REDIRECTS = 5
    }
}

/**
 * Runs `java -jar <file> --version` and reads the line `rumble <version>` from its standard output, ignoring any
 * other line (a Java tool message, say). The file is only run after its hash matched. Starting a Java process takes
 * a moment, so a screen calls this off its event thread.
 */
object JavaProgramVersion : ProgramVersionAsking {
    private val ANSWER = Regex("rumble (\\S+)")

    override fun ask(jar: Path): String? {
        val javaExe = Path.of(System.getProperty("java.home"), "bin", "java").toString()
        val process = try {
            ProcessBuilder(javaExe, "-jar", jar.toString(), "--version")
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
        } catch (e: IOException) {
            return null
        }
        val lines = java.util.Collections.synchronizedList(mutableListOf<String>())
        // The whole output is read, so the process never waits on a full pipe; only the first lines are kept.
        val reader = Thread { runCatching { process.inputStream.bufferedReader().forEachLine { if (lines.size < 50) lines += it } } }
        reader.isDaemon = true
        try {
            process.outputStream.close()
            reader.start()
            if (!process.waitFor(30, TimeUnit.SECONDS)) return null
            reader.join(5000)
            if (process.exitValue() != 0) return null
            return synchronized(lines) { lines.mapNotNull { ANSWER.matchEntire(it.trim())?.groupValues?.get(1) }.lastOrNull() }
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            return null
        } finally {
            process.destroyForcibly()
            // On Windows a file that a process still has open cannot be replaced, so wait for the process to be gone.
            try {
                process.waitFor(5, TimeUnit.SECONDS)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
    }
}
