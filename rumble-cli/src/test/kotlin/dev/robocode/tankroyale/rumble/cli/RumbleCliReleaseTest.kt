package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.rumble.ApiResponse
import dev.robocode.tankroyale.rumble.FetchPlan
import dev.robocode.tankroyale.rumble.GitHubApi
import dev.robocode.tankroyale.rumble.ProgramVersionAsking
import dev.robocode.tankroyale.rumble.RumbleProgram
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

/** The release carries the Rumble command line under the name the desktop program fetches it by. */
class RumbleCliReleaseTest {

    private val root = Path.of(System.getProperty("repo.root") ?: error("System property 'repo.root' not set — run via :rumble-cli:test"))

    private fun read(path: String) = Files.readString(root.resolve(path))

    /** The text between the first line holding [start] and the next line holding a closing brace on its own, of a Gradle task. */
    private fun block(text: String, start: String): String {
        val from = text.indexOf(start)
        assertTrue(from >= 0, "$start is missing")
        return text.substring(from, text.indexOf("\n        )", from).takeIf { it > 0 } ?: text.length)
    }

    // RCI-047

    @Test
    @Tag("RCI-047")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI047_UnitPositive_theReleaseUploadsTheCommandLineAndItsSmokeTestRunsIt() {
        val upload = read("buildSrc/src/main/kotlin/build/release/CreateRelease.kt")
        assertTrue(upload.contains("rumble-cli/build/libs/robocode-tankroyale-rumble-cli-\$version.jar"), "the release uploads the command line jar by the name the fetching expects")

        val build = read("build.gradle.kts")
        assertTrue(block(build, "register(\"build-release\")").contains("\"rumble-cli:assemble\""), "the release build assembles it")
        assertTrue(block(build, "register(\"smokeTestRelease\")").contains("\"rumble-cli:smokeTest\""), "the release smoke test runs it")
        assertTrue(read("rumble-cli/build.gradle.kts").contains("\"--version\""), "the smoke test runs it with --version")
        assertTrue(read(".agents/skills/release/SKILL.md").contains("rumble-cli"), "the release instructions name it")
    }

    @Test
    @Tag("RCI-047")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI047_UnitNegative_theNameTheReleaseUploadsIsTheNameTheFetchingLooksFor(@TempDir dir: Path) {
        val upload = read("buildSrc/src/main/kotlin/build/release/CreateRelease.kt")
        val uploaded = Regex("robocode-tankroyale-rumble-cli-\\\$version\\.jar").find(upload)!!.value.replace("\$version", "9.9.9")
        val api = object : GitHubApi {
            override fun get(path: String, token: String?) =
                ApiResponse(200, """{"assets":[{"name":"$uploaded","size":10,"digest":"sha256:${"a".repeat(64)}"}]}""")

            override fun postJson(path: String, token: String, json: String): ApiResponse = error("Only reads are expected")
            override fun patchJson(path: String, token: String, json: String): ApiResponse = error("Only reads are expected")
        }

        val plan = RumbleProgram(dir, "9.9.9", api, { error("Nothing is downloaded") }, ProgramVersionAsking { null }, { null }).plan()

        assertInstanceOf(FetchPlan.Needed::class.java, plan, plan.toString())
        assertEquals(uploaded, (plan as FetchPlan.Needed).fileName)
    }
}
