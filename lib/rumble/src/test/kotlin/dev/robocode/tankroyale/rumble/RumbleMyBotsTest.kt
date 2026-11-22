package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.IOException
import java.util.concurrent.Executor

/** My bots without a window and without GitHub: the rows, the sent pull requests and the screen's behaviour. */
class RumbleMyBotsTest {

    private val repo = "https://github.com/robocode-dev/rumble-bots-test"

    private fun bot(name: String, version: String, owner: String = "alice", status: String = "active", platform: String = "JVM") =
        CatalogBot(name, version, platform, "bots/java/$name", "sha256:x", owner, listOf("A"), "2026-11-01", status)

    private fun catalog(vararg bots: CatalogBot) = Catalog(1, "2026-11-22", "c0ffee", bots.toList())

    private fun pull(number: Int, name: String, version: String, open: Boolean = true, merged: Boolean = false) =
        SentPullRequest(number, name, version, open, merged, "https://github.com/robocode-dev/rumble-bots-test/pull/$number")

    private fun rows(catalog: Catalog, pulls: List<SentPullRequest>, progress: BotProgress = BotProgress.WaitingForModerator) =
        MyBots.rows(setOf("Alice"), catalog, pulls, repo) { progress }

    @Test
    @Tag("RCL-147")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL147_UnitPositive_oneRowPerBotOfTheAccountFromItsCatalogEntriesAndSentPullRequestsWithTheNewestVersion() {
        val rows = rows(
            catalog(bot("Orbit", "1.0.0", platform = "Python"), bot("Scout", "1.0.2", status = "superseded"), bot("Scout", "1.0.3")),
            listOf(pull(7, "Retcon", "0.2.0"), pull(3, "Scout", "1.0.3", open = false, merged = true)),
        )
        assertEquals(listOf("Orbit", "Retcon", "Scout"), rows.map { it.name })
        assertEquals(listOf("1.0.0", "0.2.0", "1.0.3"), rows.map { it.version })
        assertEquals(listOf("Python", null, "JVM"), rows.map { it.language })
        assertEquals("$repo/tree/c0ffee/bots/java/Orbit", rows[0].page)
        assertEquals("https://github.com/robocode-dev/rumble-bots-test/pull/7", rows[1].page)

        // The catalog names the owner by its owner id, which may list several accounts of the same person.
        val records = listOf(OwnerRecord("alice", listOf("alice", "alice-alt"), listOf("Orbit")), OwnerRecord("bob", listOf("bob"), listOf("Walls")))
        assertEquals(setOf("Alice-Alt", "alice"), MyBots.ownersOf("Alice-Alt", records))
        val viaAlt = MyBots.rows(MyBots.ownersOf("Alice-Alt", records), catalog(bot("Orbit", "1.0.0"), bot("Walls", "1.0.0", owner = "bob")), emptyList(), repo) { BotProgress.Accepted }
        assertEquals(listOf("Orbit"), viaAlt.map { it.name })

        // The newest catalog entry is the one added last, whatever the catalog's order.
        val old = bot("Old", "0.2.0", status = "retired").copy(addedAt = "2026-11-05")
        val older = bot("Old", "0.1.0", status = "retired").copy(addedAt = "2026-10-01", platform = "Python")
        assertEquals("0.2.0", rows(catalog(old, older), emptyList()).single().version)

        // The program's own pull requests are read by their title and branch, whatever the name and version hold.
        assertEquals("Raven-2" to "3", MyBots.nameAndVersion("submit/Raven-2-3", "Add Raven-2 3"))
        assertEquals("Bot" to "1.0-beta", MyBots.nameAndVersion("submit/Bot-1.0-beta-2", "Update Bot to 1.0-beta"))
        assertEquals("Bot" to "v2", MyBots.nameAndVersion("submit/Bot-v2", "Add Bot v2"))
    }

    @Test
    @Tag("RCL-147")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL147_UnitNegative_botsOfAnotherAccountAndBranchesNotSentByTheProgramAreNotListed() {
        assertEquals(emptyList<MyBotRow>(), rows(catalog(bot("Walls", "1.0.0", owner = "bob")), emptyList()))
        val body = """[
            {"number":1,"state":"open","title":"Update Orbit to 1.0.1","html_url":"https://github.com/robocode-dev/rumble-bots-test/pull/1","user":{"login":"alice"},"head":{"ref":"submit/Orbit-1.0.1"}},
            {"number":2,"state":"open","title":"Update Walls to 1.0.1","html_url":"https://github.com/robocode-dev/rumble-bots-test/pull/2","user":{"login":"bob"},"head":{"ref":"submit/Walls-1.0.1"}},
            {"number":3,"state":"open","title":"Fix the readme","html_url":"https://github.com/robocode-dev/rumble-bots-test/pull/3","user":{"login":"alice"},"head":{"ref":"fix-readme"}},
            {"number":4,"state":"closed","title":"Add My-Bot 2.0.0","merged_at":"2026-11-02T10:00:00Z","html_url":"https://evil.example/pull/4","user":{"login":"Alice"},"head":{"ref":"submit/My-Bot-2.0.0-2"}},
            {"number":5,"state":"open","title":"Add Orbit 9.9.9","html_url":"https://github.com/robocode-dev/rumble-bots-test/pull/5","user":{"login":"alice"},"head":{"ref":"submit/Other-1.0.0"}}
        ]"""
        val sent = MyBots.sentPullRequests(body, "alice", "robocode-dev/rumble-bots-test")
        assertEquals(listOf(1, 4), sent.map { it.number })
        assertEquals(SentPullRequest(4, "My-Bot", "2.0.0", open = false, merged = true, url = "https://github.com/robocode-dev/rumble-bots-test/pull/4"), sent[1])
        assertNull(MyBots.nameAndVersion("submit/NoVersion", "Add NoVersion"))
        assertNull(MyBots.nameAndVersion("submit/Bot-1.0.0-x", "Add Bot 1.0.0"), "a suffix that is not a number")
        assertNull(MyBots.nameAndVersion("submit/Bot-1.0.0", "Add Other 1.0.0"), "a title that does not match its branch")
        assertEquals(0, MyBots.pullRequestCount("{not a list"))
        val refused = try {
            MyBots.sentPullRequests("{not a list", "alice", "robocode-dev/rumble-bots-test")
            null
        } catch (e: IOException) {
            e
        }
        assertInstanceOf(IOException::class.java, refused, "an answer that is not a list is a failure, not an empty list")
    }

    @Test
    @Tag("RCL-148")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL148_UnitPositive_anOpenPullRequestShowsItsStateBeforeTheVersionInTheRumble() {
        val update = rows(catalog(bot("Scout", "1.0.3")), listOf(pull(9, "Scout", "1.0.4")), BotProgress.SentBack("No launcher")).single()
        assertEquals("1.0.4", update.version)
        assertEquals(MyBotStanding.Progress(BotProgress.SentBack("No launcher")), update.standing)
        assertEquals("1.0.3", update.inRumble)
        val inRumble = rows(catalog(bot("Scout", "1.0.3")), listOf(pull(3, "Scout", "1.0.3", open = false, merged = true))).single()
        assertEquals(MyBotStanding.Progress(BotProgress.InCatalog("1.0.3")), inRumble.standing)
        assertNull(inRumble.inRumble)
        val merged = rows(catalog(bot("Scout", "1.0.3")), listOf(pull(9, "Scout", "1.0.4", open = false, merged = true))).single()
        assertEquals(MyBotStanding.Progress(BotProgress.Merged), merged.standing)
        assertEquals("1.0.3", merged.inRumble)
        val retired = rows(catalog(bot("Old", "0.1.0", status = "retired")), emptyList()).single()
        assertEquals(MyBotStanding.NotActive("retired"), retired.standing)
        val closed = rows(catalog(), listOf(pull(5, "Gone", "1.0.0", open = false))).single()
        assertEquals(MyBotStanding.Progress(BotProgress.Closed), closed.standing)
        assertEquals("rumble.mybots.sent_back" to listOf("No launcher"), MyBotsTexts.forStanding(update.standing))
        assertEquals("rumble.mybots.status.retired" to emptyList<String>(), MyBotsTexts.forStanding(retired.standing))
        assertEquals("rumble.mybots.not_active" to listOf("paused"), MyBotsTexts.forStanding(MyBotStanding.NotActive("paused")))
        // Retired bots and closed submissions are hidden unless asked for; everything else always shows.
        val all = listOf(update, inRumble, merged, retired, closed)
        assertEquals(listOf(update, inRumble, merged), MyBots.shown(all, showInactive = false))
        assertEquals(all, MyBots.shown(all, showInactive = true))
        val disqualified = rows(catalog(bot("Cheat", "1.0.0", status = "disqualified")), emptyList()).single()
        assertEquals(MyBotStanding.NotActive("disqualified"), disqualified.standing)
        assertEquals(listOf(disqualified), MyBots.shown(listOf(disqualified), showInactive = false), "a disqualified bot is never hidden")
        val waiting = rows(catalog(bot("Scout", "1.0.3")), listOf(pull(8, "Scout", "1.0.4"), pull(12, "Scout", "1.0.5", open = false))).single()
        assertEquals("1.0.4", waiting.version, "an older open pull request is shown before a newer closed one")
    }

    @Test
    @Tag("RCL-148")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL148_UnitNegative_aReadingThatFailedIsSaidAndNeverShownAsAnEmptyListAndALateOneIsThrownAway() {
        val now = Executor { it.run() }
        val shown = mutableListOf<MyBotsState>()
        MyBotsController({ MyBotsReading.Failed("GitHub is limiting requests. Try again later.") }, now, now) { shown += it }.start()
        assertEquals(listOf<MyBotsState>(MyBotsState.Failed("GitHub is limiting requests. Try again later.")), shown)
        shown.clear()
        MyBotsController({ throw IllegalStateException("boom") }, now, now) { shown += it }.start()
        assertInstanceOf(MyBotsState.Failed::class.java, shown.single())
        shown.clear()
        MyBotsController({ MyBotsReading.SignInNeeded("Sign in first.") }, now, now) { shown += it }.start()
        assertEquals(listOf<MyBotsState>(MyBotsState.SignInNeeded("Sign in first.")), shown)

        val later = mutableListOf<Runnable>()
        shown.clear()
        val controller = MyBotsController({ MyBotsReading.Read("alice", emptyList()) }, now, { later += it }) { shown += it }
        controller.start()
        controller.close()
        later.forEach { it.run() }
        assertTrue(shown.isEmpty(), "a reading after closing is thrown away")
        assertEquals(MyBotsState.Reading, controller.state)
    }
}
