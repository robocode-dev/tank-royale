package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class RumbleSessionPlannerTest {

    private fun bot(name: String, version: String = "1.0", status: String = "active", members: List<String> = emptyList()) = CatalogBot(
        name = name, version = version, platform = "java", path = "bots/java/$name", sourceHash = "sha256:$name", owner = "owner",
        authors = listOf("owner"), addedAt = "2026-01-01", status = status, teamMembers = members,
    )

    private fun individuals(count: Int, prefix: String = "Bot") = (1..count).map { bot("$prefix$it") }

    private val counts = mapOf("1v1" to 2, "melee" to 4, "twinduel" to 4)

    private fun request(
        entries: List<CatalogBot>,
        selected: Set<String> = emptySet(),
        mode: SelectionMode = SelectionMode.ALL,
        formats: List<String> = listOf("1v1"),
        limit: Int? = null,
        advice: Map<String, List<List<String>>> = emptyMap(),
        seed: Long = 1,
    ) = SessionRequest(entries, selected, mode, formats, counts, limit, advice, seed)

    private fun battle(step: SessionStep) = assertInstanceOf(SessionStep.Battle::class.java, step)

    private fun waiting(step: SessionStep) = assertInstanceOf(SessionStep.Waiting::class.java, step)

    /** Runs [times] battles, recording each in the history, and returns them. */
    private fun run(request: SessionRequest, times: Int): List<SessionStep.Battle> {
        var history = SessionHistory()
        return (1..times).map {
            val b = battle(SessionPlanner.next(request, history))
            history = history.after(b)
            b
        }
    }

    // RCL-102

    @Test
    @Tag("Positive")
    fun testRCL102_UnitPositive_allBotsIgnoresTheSelectionAndNeverUsesAnEntryThatIsNotActive() {
        val entries = individuals(5) + bot("Gone", status = "retired") + bot("Bad", status = "disqualified")
        val request = request(entries, selected = setOf("Bot1 1.0"), mode = SelectionMode.ALL)

        val battles = run(request, 40)

        assertTrue(battles.all { b -> b.participants.none { it.startsWith("Gone") || it.startsWith("Bad") } })
        assertTrue(battles.any { "Bot1 1.0" !in it.participants }, "the selection must not decide the choice")
        assertTrue(battles.flatMap { it.participants }.toSet().size == 5)
    }

    @Test
    @Tag("Negative")
    fun testRCL102_UnitNegative_noGameFormatGivesAWaitAndNoBattle() {
        val step = SessionPlanner.next(request(individuals(5), formats = emptyList()), SessionHistory())

        assertEquals(WaitReason.NO_FORMAT, waiting(step).reason)
    }

    // RCL-103

    @Test
    @Tag("Positive")
    fun testRCL103_UnitPositive_preferTakesASelectedEntryWhenAValidBattleHoldsOne() {
        val request = request(individuals(6), selected = setOf("Bot2 1.0"), mode = SelectionMode.PREFER)

        val battles = run(request, 20)

        assertTrue(battles.all { "Bot2 1.0" in it.participants })
    }

    @Test
    @Tag("Negative")
    fun testRCL103_UnitNegative_preferFallsBackToAnyValidBattleWhenNoSelectedEntryIsUsable() {
        val entries = individuals(4) + bot("Gone", status = "retired")
        val notActive = request(entries, selected = setOf("Gone 1.0"), mode = SelectionMode.PREFER)
        val nothing = request(entries, selected = emptySet(), mode = SelectionMode.PREFER)
        // A selected team in a format that no valid team battle exists for: the one-versus-one format still has a battle.
        val tooFew = request(
            individuals(3) + bot("Team A", members = listOf("Bot1 1.0", "Bot2 1.0")),
            selected = setOf("Team A 1.0"), mode = SelectionMode.PREFER, formats = listOf("twinduel", "1v1"),
        )

        assertEquals(2, battle(SessionPlanner.next(notActive, SessionHistory())).participants.size)
        assertEquals(2, battle(SessionPlanner.next(nothing, SessionHistory())).participants.size)
        val fallback = battle(SessionPlanner.next(tooFew, SessionHistory()))
        assertEquals("1v1", fallback.gameType)
    }

    // RCL-104

    @Test
    @Tag("Positive")
    fun testRCL104_UnitPositive_onlySelectedBotsHoldsASelectedEntryInEveryBattle() {
        val request = request(individuals(6), selected = setOf("Bot3 1.0", "Bot4 1.0"), mode = SelectionMode.ONLY)

        val battles = run(request, 20)

        assertTrue(battles.all { b -> b.participants.any { it == "Bot3 1.0" || it == "Bot4 1.0" } })
    }

    @Test
    @Tag("Negative")
    fun testRCL104_UnitNegative_onlySelectedBotsWaitsWithAReasonAndNeverUsesOtherEntries() {
        val some = individuals(3)
        val nothing = SessionPlanner.next(request(some, mode = SelectionMode.ONLY), SessionHistory())
        val notActive = SessionPlanner.next(
            request(some + bot("Gone", status = "retired"), selected = setOf("Gone 1.0"), mode = SelectionMode.ONLY), SessionHistory(),
        )
        // The only entry that is selected cannot form a battle alone, and the catalog has just that one entry.
        val alone = SessionPlanner.next(request(listOf(bot("Lonely")), selected = setOf("Lonely 1.0"), mode = SelectionMode.ONLY), SessionHistory())
        // The catalog has enough entries, but none of them is a team, so the selected team has no battle of its own.
        val teamOnly = SessionPlanner.next(
            request(individuals(5) + bot("Team A", members = listOf("Bot1 1.0", "Bot9 1.0")), selected = setOf("Team A 1.0"), mode = SelectionMode.ONLY, formats = listOf("twinduel")),
            SessionHistory(),
        )

        assertEquals(WaitReason.NOTHING_SELECTED, waiting(nothing).reason)
        assertEquals(WaitReason.SELECTED_NOT_ACTIVE, waiting(notActive).reason)
        assertEquals(listOf("Gone 1.0"), waiting(notActive).values)
        assertEquals(WaitReason.NOT_ENOUGH_ENTRIES, waiting(alone).reason)
        assertEquals(listOf("1v1"), waiting(alone).values)
        assertEquals(WaitReason.NOT_ENOUGH_ENTRIES, waiting(teamOnly).reason, "one team alone cannot make a TwinDuel battle in the whole catalog either")
    }

    @Test
    @Tag("Negative")
    fun testRCL104_UnitNegative_aWaitThatOnlyTheSelectionCausedIsToldApartFromOneTheWholeCatalogCaused() {
        // Team A shares a member with every other team, but Team C and Team E could still meet each other.
        val entries = individuals(5) + listOf(
            bot("Team A", members = listOf("Bot1 1.0", "Bot2 1.0")),
            bot("Team C", members = listOf("Bot1 1.0", "Bot3 1.0")),
            bot("Team E", members = listOf("Bot2 1.0", "Bot4 1.0")),
            bot("Team D", members = listOf("Bot1 1.0", "Bot4 1.0")),
        )

        val onlySelection = SessionPlanner.next(
            request(entries, selected = setOf("Team A 1.0"), mode = SelectionMode.ONLY, formats = listOf("twinduel")), SessionHistory(),
        )
        val wholeCatalogToo = SessionPlanner.next(
            request(individuals(5) + entries.filter { it.name == "Team A" }, selected = setOf("Team A 1.0"), mode = SelectionMode.ONLY, formats = listOf("twinduel")),
            SessionHistory(),
        )

        assertEquals(WaitReason.NO_VALID_BATTLE_WITH_SELECTED, waiting(onlySelection).reason)
        assertEquals(listOf("twinduel"), waiting(onlySelection).values)
        assertEquals(WaitReason.NOT_ENOUGH_ENTRIES, waiting(wholeCatalogToo).reason)
    }

    // RCL-105

    @Test
    @Tag("Positive")
    fun testRCL105_UnitPositive_selectedEntriesFormatsAndOpponentsTakeTurns() {
        val request = request(
            individuals(7), selected = setOf("Bot1 1.0", "Bot2 1.0"), mode = SelectionMode.ONLY, formats = listOf("1v1", "melee"),
        )

        val battles = run(request, 40)

        val ones = battles.count { it.gameType == "1v1" }
        assertTrue(kotlin.math.abs(ones - (battles.size - ones)) <= 1, "the formats take turns")
        val withBot1 = battles.count { "Bot1 1.0" in it.participants }
        val withBot2 = battles.count { "Bot2 1.0" in it.participants }
        assertTrue(withBot1 > 0 && withBot2 > 0)
        assertTrue(kotlin.math.abs(withBot1 - withBot2) <= 4, "the selected entries take turns: $withBot1 and $withBot2")
        val opponents = battles.flatMap { it.participants }.filter { it != "Bot1 1.0" && it != "Bot2 1.0" }.toSet()
        assertEquals((3..7).map { "Bot$it 1.0" }.toSet(), opponents, "every eligible opponent was used")
    }

    @Test
    @Tag("Negative")
    fun testRCL105_UnitNegative_theSameRequestAndHistoryGiveTheSameBattleAndAnotherSeedMayGiveAnother() {
        val entries = individuals(12)
        val one = request(entries, seed = 1)
        val sameAgain = request(entries, seed = 1)

        assertEquals(SessionPlanner.next(one, SessionHistory()), SessionPlanner.next(sameAgain, SessionHistory()))
        val others = (2L..30L).map { SessionPlanner.next(request(entries, seed = it), SessionHistory()) }.toSet()
        assertTrue(others.size > 1, "another seed may give another battle")
        assertNotEquals(setOf(SessionPlanner.next(one, SessionHistory())), others)
    }

    @Test
    @Tag("Positive")
    fun testRCL105_UnitPositive_threeSelectedEntriesShareTheBattlesCloselyForEverySeedAndInTwinDuelToo() {
        val selected = setOf("Bot1 1.0", "Bot2 1.0", "Bot3 1.0")
        for (seed in 1L..5L) {
            val battles = run(request(individuals(9), selected = selected, mode = SelectionMode.ONLY, seed = seed), 60)
            val counts = selected.map { s -> battles.count { s in it.participants } }
            assertTrue(counts.max() - counts.min() <= 6, "seed $seed gave $counts")
        }
        val teams = listOf(
            bot("Team A", members = listOf("Bot1 1.0", "Bot2 1.0")), bot("Team B", members = listOf("Bot3 1.0", "Bot4 1.0")),
            bot("Team C", members = listOf("Bot5 1.0", "Bot6 1.0")), bot("Team D", members = listOf("Bot7 1.0", "Bot8 1.0")),
        )
        val twin = run(request(individuals(8) + teams, selected = setOf("Team A 1.0", "Team B 1.0"), mode = SelectionMode.PREFER, formats = listOf("twinduel")), 40)
        val a = twin.count { "Team A 1.0" in it.participants }
        val b = twin.count { "Team B 1.0" in it.participants }
        assertTrue(kotlin.math.abs(a - b) <= 6 && a > 0 && b > 0, "TwinDuel selected teams took $a and $b")
    }

    // RCL-106

    @Test
    @Tag("Positive")
    fun testRCL106_UnitPositive_aBattleCountEndsTheSessionAndAdviceThatIsUsableIsFollowed() {
        val request = request(individuals(6), limit = 3, advice = mapOf("1v1" to listOf(listOf("Bot5 1.0", "Bot6 1.0"))))
        var history = SessionHistory()

        val first = battle(SessionPlanner.next(request, history))
        history = history.after(first)
        history = history.after(battle(SessionPlanner.next(request, history)))
        history = history.after(battle(SessionPlanner.next(request, history)))

        assertEquals(setOf("Bot5 1.0", "Bot6 1.0"), first.participants.toSet())
        assertEquals(SessionStep.Finished, SessionPlanner.next(request, history))
    }

    @Test
    @Tag("Negative")
    fun testRCL106_UnitNegative_noCountNeverEndsAndMissingOrUnusableAdviceBlocksNothing() {
        val entries = individuals(4)
        val missing = request(entries)
        val empty = request(entries, advice = mapOf("1v1" to emptyList()))
        val unusable = request(
            entries, advice = mapOf("1v1" to listOf(listOf("Ghost 1.0", "Bot1 1.0"), listOf("Bot1 1.0", "Bot1 1.0"), emptyList())),
        )
        val history = SessionHistory(battlesRun = 100_000)

        for (r in listOf(missing, empty, unusable)) {
            val b = battle(SessionPlanner.next(r, history))
            assertEquals(2, b.participants.toSet().size)
            assertTrue(b.participants.all { it in entries.map { e -> "${e.name} ${e.version}" } })
        }
        assertFalse(SessionPlanner.next(missing, history) is SessionStep.Finished)
    }

    @Test
    @Tag("Negative")
    fun testRCL106_UnitNegative_adviceNamingTwinDuelTeamsThatShareAMemberOrTooManyTeamsBlocksNoValidBattle() {
        val entries = individuals(6) + listOf(
            bot("Team A", members = listOf("Bot1 1.0", "Bot2 1.0")),
            bot("Team B", members = listOf("Bot1 1.0", "Bot3 1.0")),
            bot("Team C", members = listOf("Bot4 1.0", "Bot5 1.0")),
        )
        val clashing = mapOf("twinduel" to listOf(listOf("Team A 1.0", "Team B 1.0"), listOf("Team A 1.0", "Team B 1.0", "Team C 1.0")))

        val all = battle(SessionPlanner.next(request(entries, formats = listOf("twinduel"), advice = clashing), SessionHistory()))
        val only = battle(SessionPlanner.next(request(entries, selected = setOf("Team A 1.0"), mode = SelectionMode.ONLY, formats = listOf("twinduel"), advice = clashing), SessionHistory()))

        for (b in listOf(all, only)) {
            assertEquals(2, b.participants.size)
            val members = b.participants.flatMap { p -> entries.first { "${it.name} ${it.version}" == p }.teamMembers }
            assertEquals(4, members.toSet().size, "the teams of ${b.participants} must not share a bot")
        }
        assertTrue("Team A 1.0" in only.participants)
    }

    // RCL-107

    @Test
    @Tag("Positive")
    fun testRCL107_UnitPositive_aTwinDuelBattleHoldsTheSelectedTeamAndAnotherTeamWithDisjointMembers() {
        val entries = individuals(8) + listOf(
            bot("Team A", members = listOf("Bot1 1.0", "Bot2 1.0")),
            bot("Team B", members = listOf("Bot1 1.0", "Bot3 1.0")),
            bot("Team C", members = listOf("Bot4 1.0", "Bot5 1.0")),
            bot("Team D", members = listOf("Bot6 1.0", "Bot7 1.0")),
        )
        val request = request(entries, selected = setOf("Team A 1.0"), mode = SelectionMode.ONLY, formats = listOf("twinduel"))

        val battles = run(request, 20)

        for (b in battles) {
            assertEquals(2, b.participants.size)
            assertTrue("Team A 1.0" in b.participants)
            assertFalse("Team B 1.0" in b.participants, "Team B shares Bot1 with Team A")
            assertEquals(4, b.participants.flatMap { p -> entries.first { "${it.name} ${it.version}" == p }.teamMembers }.toSet().size)
        }
    }

    @Test
    @Tag("Negative")
    fun testRCL107_UnitNegative_aTeamWhoseMemberIsNotActiveIsNeverUsedAndASelectedTeamWithNoPartnerGivesNoBattle() {
        val entries = individuals(5) + bot("Gone", status = "retired") + listOf(
            bot("Team A", members = listOf("Bot1 1.0", "Bot2 1.0")),
            bot("Team B", members = listOf("Bot3 1.0", "Gone 1.0")),
            bot("Team C", members = listOf("Bot4 1.0", "Bot5 1.0")),
        )
        val used = run(request(entries, formats = listOf("twinduel")), 15).flatMap { it.participants }.toSet()
        assertFalse("Team B 1.0" in used)
        assertEquals(setOf("Team A 1.0", "Team C 1.0"), used)

        val noPartner = request(
            individuals(3) + listOf(bot("Team A", members = listOf("Bot1 1.0", "Bot2 1.0")), bot("Team B", members = listOf("Bot2 1.0", "Bot3 1.0"))),
            selected = setOf("Team A 1.0"), mode = SelectionMode.ONLY, formats = listOf("twinduel"),
        )
        assertEquals(WaitReason.NOT_ENOUGH_ENTRIES, waiting(SessionPlanner.next(noPartner, SessionHistory())).reason)
    }
}
