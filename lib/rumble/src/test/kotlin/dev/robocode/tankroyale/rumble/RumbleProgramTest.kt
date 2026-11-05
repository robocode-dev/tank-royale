package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest

class RumbleProgramTest {

    private val version = "1.4.0"
    private val fileName = "robocode-tankroyale-rumble-cli-$version.jar"
    private val url = "https://github.com/robocode-dev/tank-royale/releases/download/v$version/$fileName"

    /** A "jar" is a text file that says its own version; the fake asking reads it. */
    private fun jarBytes(v: String = version) = "rumble $v\n".toByteArray()

    private fun sha(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private val asking = ProgramVersionAsking { jar ->
        try {
            Files.readString(jar).trim().takeIf { it.startsWith("rumble ") }?.removePrefix("rumble ")
        } catch (e: IOException) {
            null
        }
    }

    /** The Tank Royale releases: what GitHub answers about a release, and what a download gives. */
    private class Releases(var bytes: ByteArray, var release: ((String) -> ApiResponse)? = null) : GitHubApi {
        val requests = mutableListOf<Pair<String, String?>>()
        var offline = false
        var status = 200
        var assetsJson: String? = null
        var downloaded = 0
        var downloadFails: IOException? = null
        var downloadBytes: ByteArray? = null
        var downloadedUrls = mutableListOf<String>()

        override fun get(path: String, token: String?): ApiResponse {
            requests += path to token
            if (offline) throw IOException("offline")
            release?.let { return it(path) }
            if (status != 200) return ApiResponse(status, "{}")
            val sha = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
            val assets = assetsJson ?: """[{"name":"robocode-tankroyale-gui-1.4.0.jar","size":10,"digest":"sha256:${"0".repeat(64)}"},{"name":"robocode-tankroyale-rumble-cli-1.4.0.jar","size":${bytes.size},"digest":"sha256:$sha"}]"""
            return ApiResponse(200, """{"tag_name":"v1.4.0","assets":$assets}""")
        }

        override fun postJson(path: String, token: String, json: String): ApiResponse = error("Only reads are expected")
        override fun patchJson(path: String, token: String, json: String): ApiResponse = error("Only reads are expected")

        val download = ProgramDownload { address ->
            downloaded++
            downloadedUrls += address
            downloadFails?.let { throw it }
            ByteArrayInputStream(downloadBytes ?: bytes)
        }
    }

    private fun program(dir: Path, releases: Releases, developer: String? = null, ceiling: Long = 200L * 1024 * 1024) =
        RumbleProgram(dir, version, releases, releases.download, asking, { developer }, ceiling)

    private fun place(dir: Path, bytes: ByteArray) {
        val jar = dir.resolve("rumble").resolve("rumble.jar")
        Files.createDirectories(jar.parent)
        Files.write(jar, bytes)
    }

    private fun parts(dir: Path): List<String> =
        if (!Files.isDirectory(dir.resolve("rumble"))) emptyList()
        else Files.list(dir.resolve("rumble")).use { s -> s.map { it.fileName.toString() }.filter { it != "rumble.jar" }.collect(java.util.stream.Collectors.toList()) }

    // RCL-043

    @Test
    @Tag("RCL-043")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL043_UnitPositive_theProgramIsLookedForAtAFixedPlaceAndTheRightVersionIsReady(@TempDir dir: Path) {
        val releases = Releases(jarBytes())
        val program = program(dir, releases)

        assertEquals(dir.resolve("rumble").resolve("rumble.jar"), program.jar)
        assertEquals(ProgramStatus.Missing(program.jar), program.look())

        place(dir, jarBytes())
        assertEquals(ProgramStatus.Ready(program.jar, version, developerChoice = false), program.look())
        assertTrue(releases.requests.isEmpty(), "looking asks nobody")
    }

    @Test
    @Tag("RCL-043")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL043_UnitNegative_anotherVersionOrAFileThatDoesNotAnswerIsWrongAndNothingIsChangedByLooking(@TempDir dir: Path) {
        val releases = Releases(jarBytes())
        val program = program(dir, releases)

        place(dir, jarBytes("1.3.0"))
        assertEquals(ProgramStatus.WrongVersion(program.jar, "1.3.0"), program.look())
        place(dir, "not a program".toByteArray())
        assertEquals(ProgramStatus.WrongVersion(program.jar, null), program.look())
        assertEquals("not a program", Files.readString(program.jar))
        assertEquals(0, releases.downloaded)
    }

    // RCL-044

    @Test
    @Tag("RCL-044")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL044_UnitPositive_aFetchIsPlannedFromExactlyThisReleaseAndNothingIsDownloaded(@TempDir dir: Path) {
        val bytes = jarBytes()
        val releases = Releases(bytes)

        val plan = program(dir, releases).plan()

        assertEquals(FetchPlan.Needed(version, fileName, bytes.size.toLong(), sha(bytes), url), plan)
        assertEquals(listOf("/repos/robocode-dev/tank-royale/releases/tags/v1.4.0" to null), releases.requests, "that exact release, with no sign-in, never the latest")
        assertEquals(0, releases.downloaded)
        assertFalse(Files.exists(dir.resolve("rumble")), "planning writes nothing")
    }

    @Test
    @Tag("RCL-044")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL044_UnitPositive_anAddressInGitHubsAnswerIsNeverUsedAndAReadyProgramNeedsNoFetch(@TempDir dir: Path) {
        val bytes = jarBytes()
        val releases = Releases(bytes)
        releases.assetsJson = """[{"name":"$fileName","size":${bytes.size},"digest":"sha256:${sha(bytes)}","browser_download_url":"https://evil.example/x.jar"}]"""

        val plan = program(dir, releases).plan() as FetchPlan.Needed
        assertEquals(url, plan.url)

        place(dir, bytes)
        val ready = program(dir, releases).plan()
        assertEquals(FetchPlan.NotNeeded(ProgramStatus.Ready(dir.resolve("rumble").resolve("rumble.jar"), version, false)), ready)
    }

    @Test
    @Tag("RCL-044")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL044_UnitNegative_everyWayThePlanCanFailGivesATypedAnswerWithAPlainMessage(@TempDir dir: Path) {
        fun problem(arrange: (Releases) -> Unit, ceiling: Long = 200L * 1024 * 1024): ProgramProblem {
            val releases = Releases(jarBytes())
            arrange(releases)
            val plan = program(dir.resolve("case"), releases, ceiling = ceiling).plan()
            val unavailable = (plan as FetchPlan.Unavailable).reason
            assertTrue(unavailable.message.isNotBlank() && !unavailable.message.contains("Exception"), unavailable.message)
            return unavailable.problem
        }
        val bytes = jarBytes()
        assertEquals(ProgramProblem.OFFLINE, problem({ it.offline = true }))
        assertEquals(ProgramProblem.NO_RELEASE, problem({ it.status = 404 }))
        assertEquals(ProgramProblem.RATE_LIMITED, problem({ it.status = 403 }))
        assertEquals(ProgramProblem.RATE_LIMITED, problem({ it.status = 429 }))
        assertEquals(ProgramProblem.OFFLINE, problem({ it.status = 503 }))
        assertEquals(ProgramProblem.BAD_ANSWER, problem({ it.status = 418 }))
        assertEquals(ProgramProblem.BAD_ANSWER, problem({ it.release = { ApiResponse(200, "not json") } }))
        assertEquals(ProgramProblem.NO_FILE, problem({ it.assetsJson = """[{"name":"robocode-tankroyale-gui-1.4.0.jar","size":5,"digest":"sha256:${"0".repeat(64)}"}]""" }))
        assertEquals(ProgramProblem.NO_HASH, problem({ it.assetsJson = """[{"name":"$fileName","size":${bytes.size}}]""" }))
        assertEquals(ProgramProblem.NO_HASH, problem({ it.assetsJson = """[{"name":"$fileName","size":${bytes.size},"digest":"md5:abc"}]""" }))
        assertEquals(ProgramProblem.TOO_BIG, problem({ }, ceiling = 3))
        assertEquals(ProgramProblem.OFFLINE, problem({ it.release = { throw IllegalStateException("boom") } }))
    }

    // RCL-045

    private fun fetch(dir: Path, releases: Releases, ceiling: Long = 200L * 1024 * 1024): ProgramFetch {
        val program = program(dir, releases, ceiling = ceiling)
        return program.fetch(program.plan() as FetchPlan.Needed)
    }

    @Test
    @Tag("RCL-045")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL045_UnitPositive_aGoodFileIsDownloadedCheckedAskedForItsVersionAndPlacedAsTheProgram(@TempDir dir: Path) {
        val releases = Releases(jarBytes())

        val result = fetch(dir, releases)

        assertEquals(ProgramFetch.Fetched(dir.resolve("rumble").resolve("rumble.jar"), version), result)
        assertEquals(listOf(url), releases.downloadedUrls)
        assertEquals(String(jarBytes()), Files.readString(dir.resolve("rumble").resolve("rumble.jar")))
        assertEquals(emptyList<String>(), parts(dir), "no temporary file is left")
        assertInstanceOf(FetchPlan.NotNeeded::class.java, program(dir, releases).plan(), "a program of the right version is not fetched again")
    }

    @Test
    @Tag("RCL-045")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL045_UnitPositive_aProgramOfAnotherVersionIsReplacedByTheRightOne(@TempDir dir: Path) {
        place(dir, jarBytes("1.3.0"))
        val releases = Releases(jarBytes())

        assertInstanceOf(ProgramFetch.Fetched::class.java, fetch(dir, releases))

        assertEquals(String(jarBytes()), Files.readString(dir.resolve("rumble").resolve("rumble.jar")))
    }

    @Test
    @Tag("RCL-045")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL045_UnitNegative_aBadDownloadLeavesNoFileAndNeverTouchesAnEarlierGoodCopy(@TempDir dir: Path) {
        val old = jarBytes("1.3.0")
        fun problem(arrange: (Releases) -> Unit, ceiling: Long = 200L * 1024 * 1024): ProgramProblem {
            place(dir, old)
            val releases = Releases(jarBytes())
            arrange(releases)
            val result = fetch(dir, releases, ceiling) as ProgramFetch.Failed
            assertTrue(result.reason.message.isNotBlank())
            assertEquals(String(old), Files.readString(dir.resolve("rumble").resolve("rumble.jar")), "the earlier copy is untouched")
            assertEquals(emptyList<String>(), parts(dir), "no temporary file is left")
            return result.reason.problem
        }

        // Another file of the right size: the hash does not match.
        assertEquals(ProgramProblem.HASH_MISMATCH, problem({ it.downloadBytes = "rumble 9.9.9\n".toByteArray().copyOf(jarBytes().size) }))
        // A file of the wrong version that GitHub's hash agrees with.
        assertEquals(ProgramProblem.WRONG_VERSION, problem({ it.bytes = jarBytes("2.0.0") }))
        // More bytes than reported, fewer bytes than reported, and an interrupted download.
        assertEquals(ProgramProblem.TOO_BIG, problem({ it.downloadBytes = jarBytes() + ByteArray(50) }))
        assertEquals(ProgramProblem.DOWNLOAD_FAILED, problem({ it.downloadBytes = jarBytes().copyOf(3) }))
        assertEquals(ProgramProblem.DOWNLOAD_FAILED, problem({ it.downloadFails = IOException("reset") }))
    }

    @Test
    @Tag("RCL-045")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL045_UnitNegative_aFileInUseAnswersThatItIsInUseAndAFolderThatCannotBeWrittenIsSaid(@TempDir dir: Path) {
        val releases = Releases(jarBytes())
        // The program's place is a folder, so the move onto it cannot succeed, the way a file in use cannot be replaced.
        Files.createDirectories(dir.resolve("rumble").resolve("rumble.jar").resolve("inside"))
        val program = RumbleProgram(dir, version, releases, releases.download, asking, { null })

        val result = program.fetch(program.plan() as FetchPlan.Needed)

        val reason = (result as ProgramFetch.Failed).reason
        assertTrue(reason.problem == ProgramProblem.IN_USE || reason.problem == ProgramProblem.NOT_WRITABLE, reason.toString())
        assertTrue(Files.isDirectory(dir.resolve("rumble").resolve("rumble.jar")))
        assertEquals(emptyList<String>(), parts(dir))

        val notAFolder = dir.resolve("file")
        Files.writeString(notAFolder, "x")
        val stuck = RumbleProgram(notAFolder, version, releases, releases.download, asking, { null })
        val plan = FetchPlan.Needed(version, fileName, jarBytes().size.toLong(), sha(jarBytes()), url)
        assertEquals(ProgramProblem.NOT_WRITABLE, (stuck.fetch(plan) as ProgramFetch.Failed).reason.problem)
    }

    @Test
    @Tag("RCL-045")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL045_UnitNegative_aPlanThatIsNotForThisVersionIsRefusedAndTheDownloadOnlyGoesToGitHub(@TempDir dir: Path) {
        val releases = Releases(jarBytes())
        val program = program(dir, releases)
        val good = FetchPlan.Needed(version, fileName, jarBytes().size.toLong(), sha(jarBytes()), url)

        listOf(
            good.copy(url = "https://evil.example/rumble.jar"),
            good.copy(version = "9.9.9"),
            good.copy(fileName = "other.jar"),
            good.copy(sha256 = "xyz"),
        ).forEach { plan ->
            assertEquals(ProgramProblem.BAD_ANSWER, (program.fetch(plan) as ProgramFetch.Failed).reason.problem)
        }
        assertEquals(0, releases.downloaded)
        assertFalse(Files.exists(dir.resolve("rumble")))
        assertTrue(runCatching { HttpProgramDownload().open("https://evil.example/rumble.jar") }.exceptionOrNull() is IOException)
        assertTrue(runCatching { HttpProgramDownload().open("http://github.com/robocode-dev/tank-royale/releases/download/v1/x") }.exceptionOrNull() is IOException)
    }

    @Test
    @Tag("RCL-045")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL045_UnitNegative_aRedirectOnlyGoesToGitHubOverHttpsAndAFileThatIsNoProgramHasNoVersion(@TempDir dir: Path) {
        val http = HttpProgramDownload()
        assertTrue(http.allowed("https://release-assets.githubusercontent.com/github-production-release-asset/1/2?x=y"))
        assertTrue(http.allowed("https://objects.githubusercontent.com/a"))
        assertTrue(http.allowed("https://github.com/robocode-dev/tank-royale/releases/download/v1/x"))
        assertFalse(http.allowed("http://github.com/x"))
        assertFalse(http.allowed("https://evil.example/githubusercontent.com"))
        assertFalse(http.allowed("https://github.com.evil.example/x"))
        assertFalse(http.allowed("https://notgithub.com/x"))

        val garbage = dir.resolve("garbage.jar")
        Files.write(garbage, "not a jar".toByteArray())
        assertEquals(null, JavaProgramVersion.ask(garbage), "a file that is no program does not answer a version")
        assertEquals(null, JavaProgramVersion.ask(dir.resolve("missing.jar")))
    }

    // RCL-047

    @Test
    @Tag("RCL-047")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL047_UnitPositive_aDevelopersJarIsUsedAndNothingIsFetchedOrWritten(@TempDir dir: Path) {
        val mine = dir.resolve("mine.jar")
        Files.write(mine, "anything".toByteArray())
        val releases = Releases(jarBytes())

        val plan = program(dir.resolve("data"), releases, developer = mine.toString()).plan()

        assertEquals(FetchPlan.NotNeeded(ProgramStatus.Ready(mine, null, developerChoice = true)), plan)
        assertTrue(releases.requests.isEmpty())
        assertEquals(0, releases.downloaded)
        assertFalse(Files.exists(dir.resolve("data")))
    }

    @Test
    @Tag("RCL-047")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL047_UnitNegative_aSettingThatNamesNothingUsableIsAnsweredAndNeverFallsBackToFetching(@TempDir dir: Path) {
        val releases = Releases(jarBytes())

        listOf(dir.resolve("missing.jar").toString(), dir.toString(), "\u0000").forEach { named ->
            val plan = program(dir.resolve("data"), releases, developer = named).plan()
            assertEquals(ProgramProblem.DEVELOPER_JAR_UNUSABLE, (plan as FetchPlan.Unavailable).reason.problem, named)
            assertTrue(plan.reason.message.contains("rumble.jar"))
        }
        assertTrue(releases.requests.isEmpty())
        assertEquals(0, releases.downloaded)

        // A blank setting means no setting.
        assertInstanceOf(FetchPlan.Needed::class.java, program(dir.resolve("data"), releases, developer = " ").plan())
    }

    @Test
    @Tag("RCL-047")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL047_UnitPositive_theSettingIsReadFromTheSystemProperty(@TempDir dir: Path) {
        val mine = dir.resolve("mine.jar")
        Files.write(mine, "anything".toByteArray())
        val releases = Releases(jarBytes())
        val before = System.getProperty(RumbleProgram.PROPERTY)
        try {
            System.setProperty(RumbleProgram.PROPERTY, mine.toString())
            val plan = RumbleProgram(dir.resolve("data"), version, releases, releases.download, asking).plan()
            assertEquals(FetchPlan.NotNeeded(ProgramStatus.Ready(mine, null, developerChoice = true)), plan)
        } finally {
            if (before == null) System.clearProperty(RumbleProgram.PROPERTY) else System.setProperty(RumbleProgram.PROPERTY, before)
        }
        assertEquals("rumble.jar", RumbleProgram.PROPERTY)
    }
}
