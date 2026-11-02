package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream

class RumbleSubmissionProgressTest {

    private val env = RumbleEnvironments.TEST
    private val up = "/repos/robocode-dev/rumble-bots-test"
    private val sha = "f86a91911dba70599b849f62bf474f5f933283a7"

    /** Plays the bot repository's pull request, the commit status and the reviews the moderation workflow wrote. */
    private class Fake : GitHubApi {
        val requests = mutableListOf<Triple<String, String, String?>>()
        var pullState = "open"
        var merged = false
        var status: String? = null // state|description, null for no moderation status
        var reviews = "[]"
        var pullStatus = 200
        var tokenAccepted = true
        var offline = false

        override fun get(path: String, token: String?): ApiResponse {
            requests += Triple("GET", path, token)
            if (offline) throw IOException("offline")
            if (!tokenAccepted) return ApiResponse(401, "{}")
            return when {
                path.endsWith("/pulls/5") -> if (pullStatus != 200) ApiResponse(pullStatus, "{}") else
                    ApiResponse(200, """{"state":"$pullState","merged":$merged,"head":{"sha":"$SHA"}}""")
                path.endsWith("/commits/$SHA/status") -> {
                    val parts = status?.split("|", limit = 2)
                    val list = if (parts == null) "" else """{"context":"bot-moderation","state":"${parts[0]}","description":"${parts[1]}"}"""
                    ApiResponse(200, """{"state":"x","statuses":[$list]}""")
                }
                path.contains("/pulls/5/reviews") -> ApiResponse(200, reviews)
                else -> ApiResponse(404, "{}")
            }
        }

        override fun postJson(path: String, token: String, json: String): ApiResponse = error("progress must not write")
        override fun patchJson(path: String, token: String, json: String): ApiResponse = error("progress must not write")
    }

    private fun catalog(vararg versions: String) = Catalog.parse(
        """{ "schemaVersion": 1, "generatedAt": "2026-11-01T10:00:00Z", "commit": "05c4ecfd6c67ec33e10afa85aa0bc9b81df20416",
             "bots": [${versions.joinToString(",") { """{ "name": "Scout", "version": "$it", "platform": "java", "path": "bots/java/Scout",
               "sourceHash": "sha256:x", "owner": "bob", "authors": ["B"], "addedAt": "2026-10-01", "status": "active" }""" }}] }"""
    )

    private fun read(fake: Fake, catalog: Catalog? = null) = SubmissionProgress(env, fake).read(tokens(), 5, "Scout", "1.0.3", catalog)

    // RBC-035

    @Test
    @Tag("RBC-035")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC035_UnitPositive_anOpenPullRequestIsReportedInOnePlainState() {
        assertEquals(BotProgress.WaitingForChecks, read(Fake()))
        assertEquals(BotProgress.BeingChecked(), read(Fake().apply { status = "pending|Base changed during moderation" }))
        assertEquals(BotProgress.WaitingForModerator, read(Fake().apply { status = "pending|Human moderator review required" }))
        assertEquals(BotProgress.Accepted, read(Fake().apply { status = "success|Source and completed battle passed" }))

        val sentBack = read(
            Fake().apply {
                status = "failure|Bot validation or battle failed"
                reviews = """[{"state":"CHANGES_REQUESTED","commit_id":"$SHA","body":"The bot cannot be accepted: the bot did not start within 20 seconds.\nMore detail follows."}]"""
            }
        )
        assertEquals(BotProgress.SentBack("the bot did not start within 20 seconds."), sentBack)

        val noReview = read(Fake().apply { status = "failure|Bot validation or battle failed"; reviews = """[{"state":"CHANGES_REQUESTED","commit_id":"old","body":"x"}]""" })
        assertEquals(BotProgress.SentBack("Bot validation or battle failed"), noReview)
    }

    @Test
    @Tag("RBC-035")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC035_UnitNegative_readingChangesNothingAndTheTokenAppearsNowhereButTheAuthorizationValue() {
        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        val originalOut = System.out
        val originalErr = System.err
        val seen = mutableListOf<String>()
        val faked = listOf(
            Fake().apply { status = "success|Source and completed battle passed" },
            Fake().apply { tokenAccepted = false },
            Fake().apply { offline = true },
            Fake().apply { pullStatus = 404 },
            Fake().apply { pullStatus = 500 },
        )
        try {
            System.setOut(PrintStream(out, true))
            System.setErr(PrintStream(err, true))
            faked.forEach { fake ->
                seen += read(fake).toString()
                fake.requests.forEach {
                    assertEquals("GET", it.first)
                    assertEquals(SECRET_TOKEN, it.third)
                    seen += it.second
                }
            }
        } finally {
            System.setOut(originalOut)
            System.setErr(originalErr)
        }

        seen.forEach { assertFalse(it.contains(SECRET_TOKEN)) }
        assertEquals("", out.toString())
        assertEquals("", err.toString())
        assertInstanceOf(BotProgress.SignInNeeded::class.java, read(Fake().apply { tokenAccepted = false }))
        assertTrue((read(Fake().apply { offline = true }) as BotProgress.Failed).message.contains("cannot be reached"))
        assertInstanceOf(BotProgress.Failed::class.java, read(Fake().apply { pullStatus = 404 }))
    }

    // RBC-036

    @Test
    @Tag("RBC-036")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC036_UnitPositive_aMergedPullRequestWaitsForTheCatalogAndThenIsInIt() {
        val mergedPull = Fake().apply { pullState = "closed"; merged = true }

        assertEquals(BotProgress.Merged, read(mergedPull))
        assertEquals(BotProgress.Merged, read(mergedPull, catalog("1.0.2")))
        assertEquals(BotProgress.InCatalog("1.0.3"), read(mergedPull, catalog("1.0.2", "1.0.3")))
        assertEquals(BotProgress.Closed, read(Fake().apply { pullState = "closed"; merged = false }, catalog("1.0.3")))
    }

    @Test
    @Tag("RBC-036")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC036_UnitNegative_statusWordingTheProgramDoesNotKnowIsNotGuessed() {
        val unknown = read(Fake().apply { status = "pending|Moderators are on holiday" }) as BotProgress.BeingChecked
        assertTrue(unknown.note!!.contains("not understood"))

        val strange = read(Fake().apply { status = "mystery|whatever" }) as BotProgress.BeingChecked
        assertTrue(strange.note!!.contains("not understood"))
    }

    private companion object {
        const val SHA = "f86a91911dba70599b849f62bf474f5f933283a7"
    }
}
