package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.StagedSubmission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path

class RumbleSubmissionPrecheckTest {

    private fun entry(name: String, version: String, hash: String, status: String = "active") = """
        { "name": "$name", "version": "$version", "platform": "java", "path": "bots/java/$name",
          "sourceHash": "$hash", "owner": "bob", "authors": ["Bob"], "addedAt": "2026-10-01", "status": "$status" }
    """.trimIndent()

    private fun catalog(vararg entries: String) = Catalog.parse(
        """{ "schemaVersion": 1, "generatedAt": "2026-11-01T10:00:00Z", "commit": "05c4ecfd6c67ec33e10afa85aa0bc9b81df20416",
             "bots": [${entries.joinToString(",")}] }"""
    )

    private val owners = """
        { "schemaVersion": 1, "owners": [
          { "ownerId": "bob", "accounts": ["bob", "bobby"], "bots": ["Walls", "Scout"], "activeSlots": 2 } ] }
    """.trimIndent()

    private val bans = """
        { "schemaVersion": 1, "bannedOwners": [ { "account": "mallory", "reason": "x" } ],
          "disqualifiedBots": [ { "bot": "Cheater", "reason": "x" } ] }
    """.trimIndent()

    private val rules get() = RumbleRules.parse(owners, bans)

    private fun staged(name: String = "Scout", version: String? = "1.0.3", hash: String = "sha256:mine"): StagedSubmission {
        val folder = Path.of("unused")
        return StagedSubmission(name, version, "java", "MIT", listOf("A"), false, folder, folder, emptyList(), emptyList(), hash)
    }

    private fun codes(report: PrecheckReport) = report.problems.map { it.code }

    // RBC-031

    @Test
    @Tag("RBC-031")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC031_UnitPositive_ownersBannedAccountsAndDisqualifiedBotsAreRead() {
        val read = rules

        assertEquals(setOf("mallory"), read.bannedAccounts)
        assertEquals(setOf("Cheater"), read.disqualifiedBots)
        assertEquals("bob", read.ownerOf("Walls")!!.ownerId)
        assertEquals(listOf("bob", "bobby"), read.ownerOf("Scout")!!.accounts)
        assertNull(read.ownerOf("Unclaimed"))
        assertTrue(RumbleRules.parse(null, bans).owners.isEmpty())
    }

    @Test
    @Tag("RBC-031")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC031_UnitNegative_aMissingOrMalformedListOrUnknownVersionIsReportedInPlainWords() {
        assertThrows(CatalogUnreadable::class.java) { RumbleRules.parse(owners, "not json") }
        assertThrows(CatalogUnreadable::class.java) { RumbleRules.parse("{ broken", bans) }
        val newer = assertThrows(CatalogUnreadable::class.java) { RumbleRules.parse(owners, bans.replace("\"schemaVersion\": 1", "\"schemaVersion\": 2")) }
        assertTrue(newer.message!!.contains("Update the program"), newer.message)

        val noBans = object : CatalogSource {
            override fun catalogText(commit: String) = ""
            override fun archive(commit: String) = throw UnsupportedOperationException()
            override fun fileText(commit: String, path: String): String? = if (path == RumbleRules.OWNERS_PATH) owners else null
        }
        val missing = assertThrows(CatalogUnreadable::class.java) { RumbleRules.read(noBans, "05c4ecf") }
        assertTrue(missing.message!!.contains("ban list"), missing.message)
    }

    // RBC-032

    @Test
    @Tag("RBC-032")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC032_UnitPositive_eachRefusalTheRumbleWouldMakeIsAProblemWithAPlainFix() {
        val published = catalog(entry("Scout", "1.0.3", "sha256:theirs"), entry("Walls", "2.0.0", "sha256:mine"))

        val banned = SubmissionPrecheck.check(staged("Fresh"), "mallory", published, rules)
        assertEquals(listOf(SubmissionPrecheck.ACCOUNT_BANNED), codes(banned))

        val disqualified = SubmissionPrecheck.check(staged("Cheater"), "carol", published, rules)
        assertEquals(listOf(SubmissionPrecheck.BOT_DISQUALIFIED), codes(disqualified))

        val owned = SubmissionPrecheck.check(staged("Walls", "3.0.0"), "carol", published, rules)
        assertEquals(listOf(SubmissionPrecheck.OWNED_BY_ANOTHER), codes(owned))
        assertTrue(owned.problems.single().message.contains("bob"))

        val sameVersion = SubmissionPrecheck.check(staged("Scout", "1.0.3", "sha256:mine"), "bob", published, rules)
        assertEquals(listOf(SubmissionPrecheck.SAME_VERSION_DIFFERENT_SOURCE), codes(sameVersion))

        val identical = SubmissionPrecheck.check(staged("Walls", "2.0.0", "sha256:mine"), "bob", published, rules)
        assertEquals(listOf(SubmissionPrecheck.ALREADY_PUBLISHED), codes(identical))

        listOf(banned, disqualified, owned, sameVersion, identical).forEach { report ->
            assertTrue(report.hasProblems)
            report.problems.forEach { assertTrue(it.message.isNotBlank() && it.fix.isNotBlank()) }
        }
    }

    @Test
    @Tag("RBC-032")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC032_UnitNegative_aNewNameAndAnOwnersNewVersionHaveNoProblem() {
        val published = catalog(entry("Scout", "1.0.3", "sha256:theirs"))

        val brandNew = SubmissionPrecheck.check(staged("Fresh", "1.0.0"), "carol", published, rules)
        val update = SubmissionPrecheck.check(staged("Scout", "1.0.4", "sha256:newer"), "bobby", published, rules)
        val retired = SubmissionPrecheck.check(staged("Scout", "1.0.3"), "bob", catalog(entry("Scout", "1.0.3", "sha256:old", "superseded")), rules)

        listOf(brandNew, update, retired).forEach {
            assertFalse(it.hasProblems, it.problems.toString())
            assertTrue(it.warnings.isEmpty())
        }
    }

    // RBC-033

    @Test
    @Tag("RBC-033")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC033_UnitPositive_aLowerVersionIsAWarningAndNeverAProblem() {
        val published = catalog(entry("Scout", "1.10.0", "sha256:theirs"))

        val lower = SubmissionPrecheck.check(staged("Scout", "1.9.0"), "bob", published, rules)
        val shorter = SubmissionPrecheck.check(staged("Scout", "1.0"), "bob", published, rules)

        listOf(lower, shorter).forEach {
            assertFalse(it.hasProblems)
            assertEquals(listOf(SubmissionPrecheck.LOWER_VERSION), it.warnings.map { w -> w.code })
            assertTrue(it.warnings.single().message.contains("replace the newer"))
        }
    }

    @Test
    @Tag("RBC-033")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC033_UnitNegative_higherEqualAndNonNumericVersionsGiveNoWarning() {
        val published = catalog(entry("Scout", "1.10.0", "sha256:theirs"))

        listOf("1.10.1", "2.0.0", "1.10.0.0").forEach {
            assertTrue(SubmissionPrecheck.check(staged("Scout", it), "bob", published, rules).warnings.isEmpty(), it)
        }
        val named = catalog(entry("Scout", "2.0-beta", "sha256:theirs"))
        listOf("1.0.0", "2.0-alpha", "1.0-rc1").forEach {
            assertTrue(SubmissionPrecheck.check(staged("Scout", it), "bob", named, rules).warnings.isEmpty(), it)
        }
        assertTrue(SubmissionPrecheck.check(staged("Scout", null), "bob", published, rules).warnings.isEmpty())
    }

    // RBC-034

    @Test
    @Tag("RBC-034")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC034_UnitNegative_theSendingCodeNeverUsesThePrecheckOrTheRules() {
        val sourceRoot = Path.of("src", "main", "kotlin", "dev", "robocode", "tankroyale", "rumble")
        val sending = Files.readString(sourceRoot.resolve("BotSubmission.kt"))

        listOf("SubmissionPrecheck", "RumbleRules", "PrecheckReport", "CatalogSource", "Catalog.parse").forEach {
            assertFalse(sending.contains(it), "BotSubmission.kt refers to $it")
        }
    }

    // RBC-037

    @Test
    @Tag("RBC-037")
    @Tag("Integration")
    @Tag("Positive")
    fun testRBC037_IntegrationPositive_everyActivePublicBotPassesAsItsOwnerAndIsRefusedAsAnotherAccount() {
        assumeTrue(System.getProperty("rumble.live") == "true", "Set -Drumble.live=true to read the public Rumble")
        val live = GitHubCatalogSource(RumbleEnvironments.PUBLIC)
        val head = java.net.URI.create("https://raw.githubusercontent.com/robocode-dev/rumble-bots/main/bots/index.json").toURL().readText()
        val current = Catalog.parse(live.catalogText(Catalog.parse(head).commit))
        val liveRules = RumbleRules.read(live, current.commit)

        val problems = current.activeBots.flatMap { bot ->
            val submission = StagedSubmission(
                bot.name, bot.version, bot.platform, "MIT", bot.authors, bot.teamMembers.isNotEmpty(),
                Path.of("unused"), Path.of("unused"), emptyList(), emptyList(), bot.sourceHash,
            )
            val owner = liveRules.ownerOf(bot.name)
            val account = owner?.accounts?.firstOrNull()
            val asOwner = if (account == null) listOf("${bot.name}: no owner record")
            else SubmissionPrecheck.check(submission, account, current, liveRules).problems.map { it.code }
                .filter { it != SubmissionPrecheck.ALREADY_PUBLISHED }.map { "${bot.name}: as owner $it" }
            val asOther = SubmissionPrecheck.check(submission, "an-unrelated-account", current, liveRules).problems.map { it.code }
            asOwner + if (SubmissionPrecheck.OWNED_BY_ANOTHER in asOther) emptyList() else listOf("${bot.name}: not refused as another account")
        }
        assertEquals(emptyList<String>(), problems)
    }
}
