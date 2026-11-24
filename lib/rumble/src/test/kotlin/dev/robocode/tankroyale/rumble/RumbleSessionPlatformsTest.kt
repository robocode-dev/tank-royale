package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration

/** What a session finds on this computer before its first battle: the platforms its bots need, on the path. */
class RumbleSessionPlatformsTest {

    @TempDir
    lateinit var dir: Path

    /** Answers like a computer with the given tools; a null answer is a tool that is not there. */
    private class Tools(var java: String? = "openjdk version \"25.0.1\" 2025-10-21", var dotnet: String? = "10.0.100 [C:\\dotnet\\sdk]", var node: String? = "v24.1.0", var npm: String? = "11.0.0") : ToolRunner {
        val asked = mutableListOf<String>()
        override fun run(command: List<String>, timeout: Duration): ToolOutput? {
            asked += command.first()
            val text = when (command.first()) {
                "java" -> java
                "dotnet" -> dotnet
                "node" -> node
                "npm", "npm.cmd" -> npm
                else -> null
            }
            return text?.let { ToolOutput(0, it) }
        }
    }

    /** A checker whose path holds the given tools by their bare names, or no Java at all. */
    private fun checker(tools: Tools, javaOnPath: Boolean = true) =
        PlatformChecker(tools, windows = true, locate = { name -> if (name == "java" && !javaOnPath) null else name })

    private val versions = SupportedVersions(java = 25, dotnet = 10, python = listOf(3, 14), node = 24)

    private fun bot(name: String, platform: String, members: List<String> = emptyList()) =
        CatalogBot(name, "1.0", platform, "bots/x/$name", "sha256:aa", "owner", listOf("A"), "2026-01-01", CatalogBot.ACTIVE, teamMembers = members)

    private val bots = listOf(
        bot("Walls", "JVM"), bot("Sharp", "C#"), bot("Node", "TypeScript"), bot("Snake", "Python"),
        bot("SharpTeam", "C#", listOf("Sharp 1.0", "Walls 1.0")), bot("MixedTeam", "JVM", listOf("Walls 1.0", "Node 1.0")),
    )

    // RCL-165

    @Test
    @Tag("RCL-165")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL165_UnitPositive_javaIsLookedForOnThePathByItsFullPathAsTheBotsLaunchersFindIt() {
        val bin = Files.createDirectories(dir.resolve("jdk/bin"))
        val exe = Files.createFile(bin.resolve("java.exe"))
        val path = listOf(dir.resolve("empty").toString(), "\"$bin\"").joinToString(java.io.File.pathSeparator)
        assertEquals(exe.toString(), PlatformChecker.onPath("java", windows = true, path = path))

        // The tool is run by the full path found, never by its bare name.
        val tools = object : ToolRunner {
            val asked = mutableListOf<List<String>>()
            override fun run(command: List<String>, timeout: Duration): ToolOutput? {
                asked += command
                return ToolOutput(0, "openjdk version \"25.0.1\"")
            }
        }
        val found = PlatformChecker(tools, windows = true, locate = { exe.toString() }).check(listOf(BotPlatform.JVM), versions, dir)
        assertEquals(PlatformReason.READY, found.getValue(BotPlatform.JVM).reason)
        assertEquals(listOf(exe.toString(), "-version"), tools.asked.single())
    }

    @Test
    @Tag("RCL-165")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL165_UnitNegative_noJavaOnThePathIsMissingEvenWhenTheProgramsOwnJavaWouldAnswer() {
        // The tools would answer for "java", as Windows lets the program's own Java do, but the path holds none.
        val tools = Tools()
        val found = SessionPlatforms.check(checker(tools, javaOnPath = false), versions, bots, dir)
        assertEquals(PlatformCheck(BotPlatform.JVM, PlatformReason.MISSING, "25", null), found.unavailable.first())
        assertFalse("java" in tools.asked, "a Java that is not on the path is never asked")
        assertNull(PlatformChecker.onPath("java", windows = true, path = dir.toString()))
        assertNull(PlatformChecker.onPath("java", windows = true, path = null))
        // Only the Java bots, and the teams that hold one, are left out; the others run.
        assertEquals(setOf("Walls 1.0", "SharpTeam 1.0", "MixedTeam 1.0"), found.leftOut)
    }

    // RCL-166

    @Test
    @Tag("RCL-166")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL166_UnitPositive_theBotsOfAMissingOrTooOldPlatformAreLeftOutWithTheTeamsThatHoldOne() {
        // No .NET here, a Node.js without npm, and a Java older than the Rumble's.
        val found = SessionPlatforms.check(checker(Tools(java = "openjdk version \"21.0.5\"", dotnet = null, npm = null)), versions, bots, dir)

        assertEquals(listOf(BotPlatform.JVM, BotPlatform.CSHARP, BotPlatform.TYPESCRIPT), found.unavailable.map { it.platform })
        assertEquals(listOf(PlatformReason.TOO_OLD, PlatformReason.MISSING, PlatformReason.NPM_MISSING), found.unavailable.map { it.reason })
        assertEquals(PlatformCheck(BotPlatform.JVM, PlatformReason.TOO_OLD, "25", "21.0"), found.unavailable.first())
        assertEquals(setOf("Walls 1.0", "Sharp 1.0", "Node 1.0", "SharpTeam 1.0", "MixedTeam 1.0"), found.leftOut)

        // The status keeps each platform once, with its reason and versions, for the screen.
        SessionTracker(SessionFolder(dir.resolve("s")).claim()!!, RumbleEnvironments.TEST, null, listOf("1v1"), null).use { tracker ->
            val event = kotlinx.serialization.json.buildJsonObject {
                put("event", kotlinx.serialization.json.JsonPrimitive("platform-unavailable"))
                put("platform", kotlinx.serialization.json.JsonPrimitive("C#")); put("reason", kotlinx.serialization.json.JsonPrimitive("TOO_OLD"))
                put("needed", kotlinx.serialization.json.JsonPrimitive("10")); put("found", kotlinx.serialization.json.JsonPrimitive("8"))
            }
            tracker.record(event)
            tracker.record(event)
            assertEquals(listOf(SessionPlatform("C#", "TOO_OLD", "10", "8")), tracker.status.platforms)
        }
        assertEquals("Java", ContributeTexts.platformName("JVM"))
        assertEquals("rumble.contribute.platform.too_old", ContributeTexts.forPlatform("TOO_OLD"))
    }

    @Test
    @Tag("RCL-166")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL166_UnitNegative_withEveryPlatformReadyOrTheListUnreadableNothingIsLeftOutAndPythonIsLeftToItsOwnSetup() {
        val ready = SessionPlatforms.check(checker(Tools()), versions, bots, dir)
        assertEquals(emptyList<PlatformCheck>(), ready.unavailable)
        assertEquals(emptySet<String>(), ready.leftOut)

        // Without the Rumble's list a tool passes when it is there, as on the Download bots screen.
        val unknown = SessionPlatforms.check(checker(Tools(java = "openjdk version \"11.0.2\"")), null, bots, dir)
        assertEquals(emptySet<String>(), unknown.leftOut)

        // Python is never asked here: the session sets it up itself.
        val tools = Tools()
        SessionPlatforms.check(checker(tools), versions, bots, dir)
        assertFalse(tools.asked.any { it.startsWith("python") })
        assertTrue(tools.asked.contains("java"))
        assertEquals("rumble.contribute.platform.other", ContributeTexts.forPlatform("NO_VENV"))
    }
}
