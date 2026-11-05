package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.botproject.BotStaging
import dev.robocode.tankroyale.botproject.StagedSubmission
import dev.robocode.tankroyale.botproject.StagingResult
import dev.robocode.tankroyale.rumble.ApiResponse
import dev.robocode.tankroyale.rumble.GitHubApi
import dev.robocode.tankroyale.rumble.GitHubTokens
import dev.robocode.tankroyale.rumble.LauncherCheck
import dev.robocode.tankroyale.rumble.LauncherState
import dev.robocode.tankroyale.rumble.RumbleEnvironments
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.Base64

/** The comparison of a bot's launchers with the Rumble's starter launchers, as the moderation does it. */
class RumbleCliLauncherTest {

    private val starterSh = "#!/bin/sh\nexec java -cp src StarterBot \"\$@\"\n"
    private val starterCmd = "@echo off\r\njava -cp src StarterBot %*\r\n"
    private val tokens = GitHubTokens("ghu_FAKEACCESSTOKEN0123456789", "ghr_FAKEREFRESHTOKEN0123456789", null, null)

    /** The bot repository's starter launchers, and what goes wrong when they are asked for. */
    private class Repository(private val files: Map<String, String>) : GitHubApi {
        val requests = mutableListOf<Pair<String, String?>>()
        var status: Int? = null
        var offline = false
        var body: String? = null

        override fun get(path: String, token: String?): ApiResponse {
            requests += path to token
            if (offline) throw IOException("offline")
            status?.let { return ApiResponse(it, "{}") }
            body?.let { return ApiResponse(200, it) }
            val file = path.substringAfter("/contents/templates/").substringAfterLast("/StarterBot/")
            val language = path.substringAfter("/contents/templates/").substringBefore("/")
            val text = files["$language/$file"] ?: return ApiResponse(404, "{}")
            // GitHub breaks the encoded text into lines.
            val encoded = Base64.getMimeEncoder(60, "\n".toByteArray()).encodeToString(text.toByteArray())
            return ApiResponse(200, """{"content":"${encoded.replace("\n", "\\n")}","encoding":"base64"}""")
        }

        override fun postJson(path: String, token: String, json: String): ApiResponse = error("Only reads are expected")
        override fun patchJson(path: String, token: String, json: String): ApiResponse = error("Only reads are expected")
    }

    private fun repository() = Repository(mapOf("java/StarterBot.sh" to starterSh, "java/StarterBot.cmd" to starterCmd))

    /** A staged bot called [name] whose launchers are the given texts. */
    private fun staged(dir: Path, name: String = "MyBot", sh: String = starterSh.replace("StarterBot", name), cmd: String = starterCmd.replace("StarterBot", name)): StagedSubmission {
        val folder = dir.resolve("work").resolve(name)
        write(folder.resolve("$name.json"), """{ "name": "$name", "version": "1.0.0", "authors": ["A. Author"], "license": "MIT", "description": "A test bot", "platform": "JVM" }""")
        write(folder.resolve("$name.sh"), sh)
        write(folder.resolve("$name.cmd"), cmd)
        write(folder.resolve("src/$name.java"), "import dev.robocode.tankroyale.botapi.Bot;\nclass $name\n")
        val result = BotStaging.stage(folder, dir.resolve("staging"))
        return (result as? StagingResult.Staged)?.submission ?: error(result.toString())
    }

    private fun write(file: Path, text: String) {
        Files.createDirectories(file.parent)
        Files.write(file, text.toByteArray())
    }

    private fun compare(repository: Repository, staged: StagedSubmission) = LauncherCheck(RumbleEnvironments.TEST, repository).compare(tokens, staged)

    // RCI-044

    @Test
    @Tag("RCI-044")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI044_UnitPositive_identicalWindowsLineEndingAndRenamedLaunchersAreTheSame(@TempDir dir: Path) {
        val repository = repository()

        assertEquals(LauncherState.Same, compare(repository, staged(dir.resolve("a"))))
        assertEquals(LauncherState.Same, compare(repository, staged(dir.resolve("b"), "Other")), "the name is replaced")
        val windows = staged(dir.resolve("c"), sh = starterSh.replace("StarterBot", "MyBot").replace("\n", "\r\n"), cmd = starterCmd.replace("StarterBot", "MyBot").replace("\r\n", "\n"))
        assertEquals(LauncherState.Same, compare(repository, windows), "line endings are read the same")
    }

    @Test
    @Tag("RCI-044")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI044_UnitNegative_anEditedLauncherDiffersAndNothingElseIsNormalised(@TempDir dir: Path) {
        val repository = repository()

        val edited = compare(repository, staged(dir.resolve("a"), sh = starterSh.replace("StarterBot", "MyBot") + "echo hello\n"))
        assertEquals(LauncherState.Differs(listOf("MyBot.sh")), edited)

        val both = compare(repository, staged(dir.resolve("b"), sh = "#!/bin/sh\n", cmd = "@echo off\r\n"))
        assertEquals(LauncherState.Differs(listOf("MyBot.sh", "MyBot.cmd")), both)

        val trailing = compare(repository, staged(dir.resolve("c"), cmd = starterCmd.replace("StarterBot", "MyBot") + " "))
        assertEquals(LauncherState.Differs(listOf("MyBot.cmd")), trailing, "trailing spaces are not ignored")
    }

    @Test
    @Tag("RCI-044")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI044_UnitNegative_aTemplateThatCannotBeReadIsNotCheckedWithAReasonAndNeverThrows(@TempDir dir: Path) {
        val bot = staged(dir)
        val cases = listOf<(Repository) -> Unit>(
            { it.offline = true }, { it.status = 404 }, { it.status = 403 }, { it.status = 500 },
            { it.body = "not json" }, { it.body = """{"content":"!!!"}""" }, { it.body = """{"content":"/w=="}""" },
        )

        cases.forEach { arrange ->
            val repository = repository()
            arrange(repository)
            val state = compare(repository, bot)
            assertInstanceOf(LauncherState.NotChecked::class.java, state, state.toString())
            assertTrue((state as LauncherState.NotChecked).reason.isNotBlank())
        }
    }

    @Test
    @Tag("RCI-044")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI044_UnitNegative_anUnexpectedFailureIsNotCheckedAndALauncherThatIsNotTextDiffers(@TempDir dir: Path) {
        val crashing = object : GitHubApi {
            override fun get(path: String, token: String?): ApiResponse = throw IllegalStateException("boom")
            override fun postJson(path: String, token: String, json: String): ApiResponse = error("Only reads are expected")
            override fun patchJson(path: String, token: String, json: String): ApiResponse = error("Only reads are expected")
        }
        val bot = staged(dir.resolve("a"))
        val state = LauncherCheck(RumbleEnvironments.TEST, crashing).compare(tokens, bot)
        assertInstanceOf(LauncherState.NotChecked::class.java, state, state.toString())

        val broken = staged(dir.resolve("b"))
        Files.write(broken.botFolder.resolve("MyBot.sh"), byteArrayOf(0x23, 0x21, 0xFF.toByte(), 0x0A))
        assertEquals(LauncherState.Differs(listOf("MyBot.sh")), compare(repository(), broken))
    }

    @Test
    @Tag("RCI-044")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI044_UnitPositive_theStarterLaunchersAreReadFromTheDefaultBranchWithTheSignIn(@TempDir dir: Path) {
        val repository = repository()

        compare(repository, staged(dir))

        assertEquals(
            listOf(
                "/repos/robocode-dev/rumble-bots-test/contents/templates/java/StarterBot/StarterBot.sh",
                "/repos/robocode-dev/rumble-bots-test/contents/templates/java/StarterBot/StarterBot.cmd",
            ),
            repository.requests.map { it.first },
        )
        assertEquals(setOf<String?>(tokens.accessToken), repository.requests.map { it.second }.toSet())
    }
}
