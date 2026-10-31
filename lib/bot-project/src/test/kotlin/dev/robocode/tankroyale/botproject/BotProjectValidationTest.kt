package dev.robocode.tankroyale.botproject

import dev.robocode.tankroyale.botproject.Fixtures.codes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path

class BotProjectValidationTest {

    private fun edit(file: Path, from: String, to: String) {
        val text = Files.readString(file)
        assertTrue(text.contains(from), "$file does not contain $from")
        Files.writeString(file, text.replace(from, to))
    }

    private fun assertFixed(report: ValidationReport, code: String) {
        val finding = report.findings.firstOrNull { it.code == code }
        assertTrue(finding != null, "Expected $code in ${codes(report)}")
        assertTrue(finding!!.file.isNotEmpty() && finding.message.isNotEmpty() && finding.fix.isNotEmpty(), finding.toString())
    }

    // RBC-014

    @Test
    @Tag("RBC-014")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC014_UnitPositive_validBotsInEveryLanguageAndAValidTeamPass(@TempDir parent: Path) {
        Fixtures.languages.forEach { language ->
            val report = BotValidator.validate(Fixtures.bot(parent.resolve(language), language))
            assertEquals(emptyList<String>(), codes(report), language)
        }
        assertEquals(emptyList<String>(), codes(BotValidator.validate(Fixtures.team(parent))))
    }

    @Test
    @Tag("RBC-014")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC014_UnitNegative_eachMissingMismatchedOrDisallowedMetadataValueIsReported(@TempDir parent: Path) {
        fun java(name: String = "MyBot") = Fixtures.bot(parent.resolve(name + System.nanoTime()), "java", name)

        val noLicense = java().also { edit(it.resolve("MyBot.json"), "\"license\": \"MIT\",", "") }
        assertFixed(BotValidator.validate(noLicense), "MISSING_FIELD")

        // The bot repository's own negative case (rumble-bots e9ce8a5, tests/test_validate_bot.py, test_invalid_license_is_rejected).
        val badLicense = java().also { edit(it.resolve("MyBot.json"), "\"MIT\"", "\"Proprietary\"") }
        assertFixed(BotValidator.validate(badLicense), "LICENSE_NOT_ALLOWED")

        val renamed = java().also { edit(it.resolve("MyBot.json"), "\"name\": \"MyBot\"", "\"name\": \"Other\"") }
        assertFixed(BotValidator.validate(renamed), "NAME_MISMATCH")

        val emptyAuthor = java().also { edit(it.resolve("MyBot.json"), "[\"A. Author\"]", "[\"\"]") }
        assertFixed(BotValidator.validate(emptyAuthor), "AUTHORS_INVALID")

        val noPlatform = java().also { edit(it.resolve("MyBot.json"), ", \"platform\": \"JVM\"", "") }
        assertFixed(BotValidator.validate(noPlatform), "PLATFORM_MISSING")

        val unknownPlatform = java().also { edit(it.resolve("MyBot.json"), "\"JVM\"", "\"Cobol\"") }
        assertFixed(BotValidator.validate(unknownPlatform), "PLATFORM_UNKNOWN")

        val oneMember = Fixtures.team(parent.resolve("t1")).also { edit(it.resolve("MyTeam.json"), ", \"MyBot 1.0.0\"", "") }
        assertFixed(BotValidator.validate(oneMember), "TEAM_MEMBERS_INVALID")

        // The bot repository's own negative case (rumble-bots e9ce8a5, test_RBC004_IntegrationNegative_a_team_directory_may_not_carry_sources).
        val teamWithSource = Fixtures.team(parent.resolve("t2")).also { Fixtures.write(it.resolve("src/Extra.java"), "class Extra") }
        val report = BotValidator.validate(teamWithSource)
        assertFixed(report, "TEAM_HAS_EXTRA_FILES")
        assertEquals("src/Extra.java", report.findings.single { it.code == "TEAM_HAS_EXTRA_FILES" }.file)
    }

    // RBC-015

    @Test
    @Tag("RBC-015")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC015_UnitPositive_launchersAndOfficialApiSourcePassWithoutAnExecutableBitCheck(@TempDir parent: Path) {
        Fixtures.languages.forEach { language ->
            val folder = Fixtures.bot(parent.resolve(language), language)
            val report = BotValidator.validate(folder)

            assertEquals(emptyList<String>(), codes(report), language)
            assertTrue(report.notes.any { it.contains("MyBot.sh") && it.contains("executable") }, report.notes.toString())
        }
    }

    @Test
    @Tag("RBC-015")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC015_UnitNegative_aMissingLauncherSourceOrApiReferenceIsReported(@TempDir parent: Path) {
        val noCmd = Fixtures.bot(parent.resolve("a"), "python").also { Files.delete(it.resolve("MyBot.cmd")) }
        assertFixed(BotValidator.validate(noCmd), "MISSING_LAUNCHER")

        val noSh = Fixtures.bot(parent.resolve("b"), "java").also { Files.delete(it.resolve("MyBot.sh")) }
        val noShReport = BotValidator.validate(noSh)
        assertFixed(noShReport, "MISSING_LAUNCHER")
        assertTrue(noShReport.notes.none { it.contains("executable") })

        val noSource = Fixtures.bot(parent.resolve("c"), "csharp").also { Files.delete(it.resolve("src/MyBot.cs")) }
        assertFixed(BotValidator.validate(noSource), "NO_SOURCE")

        val noApi = Fixtures.bot(parent.resolve("d"), "java").also { Fixtures.write(it.resolve("src/MyBot.java"), "class MyBot {}") }
        assertFixed(BotValidator.validate(noApi), "NO_API_REFERENCE")
    }

    // RBC-016

    @Test
    @Tag("RBC-016")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC016_UnitPositive_cacheFoldersAreNotReported(@TempDir parent: Path) {
        val folder = Fixtures.bot(parent, "python").also {
            Fixtures.write(it.resolve("src/__pycache__/MyBot.cpython-313.pyc"), "x")
        }

        assertEquals(emptyList<String>(), codes(BotValidator.validate(folder)))
    }

    @Test
    @Tag("RBC-016")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC016_UnitNegative_generatedFoldersBinariesAndRestrictedConstructsAreReported(@TempDir parent: Path) {
        val generated = Fixtures.bot(parent.resolve("a"), "typescript").also { Fixtures.write(it.resolve("node_modules/x/index.js"), "x") }
        val generatedReport = BotValidator.validate(generated)
        assertFixed(generatedReport, "GENERATED_DIRECTORY")
        assertEquals("node_modules", generatedReport.findings.first { it.code == "GENERATED_DIRECTORY" }.file)

        val binary = Fixtures.bot(parent.resolve("b"), "java").also { Fixtures.write(it.resolve("lib/Helper.JAR"), "x") }
        val binaryReport = BotValidator.validate(binary)
        assertFixed(binaryReport, "FORBIDDEN_FILE")
        assertEquals("lib/Helper.JAR", binaryReport.findings.first { it.code == "FORBIDDEN_FILE" }.file)

        val restricted = Fixtures.bot(parent.resolve("c"), "python").also {
            Fixtures.write(it.resolve("src/MyBot.py"), "from robocode_tank_royale.bot_api import Bot\nimport subprocess\n")
        }
        val restrictedReport = BotValidator.validate(restricted)
        assertFixed(restrictedReport, "RESTRICTED_CONSTRUCT")
        assertTrue(restrictedReport.findings.first { it.code == "RESTRICTED_CONSTRUCT" }.message.contains("subprocess"))
    }

    // RBC-017

    @Test
    @Tag("RBC-017")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC017_UnitPositive_aTypescriptBotWithOnlyTheApiAndItsRuntimePeerPasses(@TempDir parent: Path) {
        assertEquals(emptyList<String>(), codes(BotValidator.validate(Fixtures.bot(parent, "typescript"))))
    }

    @Test
    @Tag("RBC-017")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC017_UnitNegative_unrelatedPathOrMismatchedDependenciesAndMissingPeersAreReported(@TempDir parent: Path) {
        fun ts(name: String, manifest: String? = null, lock: String? = null): Path =
            Fixtures.bot(parent.resolve(name), "typescript").also {
                manifest?.let { text -> Fixtures.write(it.resolve("package.json"), text) }
                lock?.let { text -> Fixtures.write(it.resolve("package-lock.json"), text) }
            }

        val unrelated = ts("a", manifest = Fixtures.packageJson(extraDependency = ", \"left-pad\": \"1.0.0\""))
        assertFixed(BotValidator.validate(unrelated), "TS_UNRELATED_DEPENDENCY")

        val path = ts("b", manifest = Fixtures.packageJson(wsVersion = "file:../ws"))
        assertFixed(BotValidator.validate(path), "TS_NOT_REGISTRY_VERSION")

        val mismatch = ts("c", lock = Fixtures.packageLock(wsVersion = "8.17.0"))
        assertFixed(BotValidator.validate(mismatch), "TS_LOCK_MISMATCH")

        val noPeer = ts("d", lock = Fixtures.packageLock(withWsPeer = false))
        assertFixed(BotValidator.validate(noPeer), "TS_PEER_MISSING")

        val otherRegistry = ts("e", lock = Fixtures.packageLock(wsResolved = "https://evil.example/ws-8.18.0.tgz"))
        assertFixed(BotValidator.validate(otherRegistry), "TS_LOCK_REGISTRY")

        val noManifest = ts("f").also { Files.delete(it.resolve("package.json")) }
        assertFixed(BotValidator.validate(noManifest), "TS_PACKAGE_UNREADABLE")
    }

    // RBC-018

    @Test
    @Tag("RBC-018")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC018_UnitPositive_aFolderWithoutProblemsHasAnEmptyList(@TempDir parent: Path) {
        val report = BotValidator.validate(Fixtures.bot(parent, "java"))

        assertTrue(report.passes)
        assertEquals(emptyList<Finding>(), report.findings)
    }

    @Test
    @Tag("RBC-018")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC018_UnitNegative_everyProblemIsReportedAtOnceWithCodeFileAndFix(@TempDir parent: Path) {
        val folder = Fixtures.bot(parent, "java").also {
            edit(it.resolve("MyBot.json"), "\"MIT\"", "\"Proprietary\"")
            Files.delete(it.resolve("MyBot.cmd"))
            Fixtures.write(it.resolve("Helper.exe"), "x")
        }

        val report = BotValidator.validate(folder)

        assertEquals(listOf("LICENSE_NOT_ALLOWED", "MISSING_LAUNCHER", "FORBIDDEN_FILE"), codes(report))
        report.findings.forEach { assertTrue(it.code.isNotEmpty() && it.file.isNotEmpty() && it.fix.isNotEmpty(), it.toString()) }
        assertEquals(false, report.passes)
    }
}
