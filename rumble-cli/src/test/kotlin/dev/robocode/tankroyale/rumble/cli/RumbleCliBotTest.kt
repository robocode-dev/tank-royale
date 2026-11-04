package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.botproject.StagedSubmission
import dev.robocode.tankroyale.rumble.ApiResponse
import dev.robocode.tankroyale.rumble.AuthResponse
import dev.robocode.tankroyale.rumble.BotProgress
import dev.robocode.tankroyale.rumble.BotProgressReading
import dev.robocode.tankroyale.rumble.BotSending
import dev.robocode.tankroyale.rumble.CatalogSource
import dev.robocode.tankroyale.rumble.CatalogUnreadable
import dev.robocode.tankroyale.rumble.GitHubApi
import dev.robocode.tankroyale.rumble.GitHubAuthTransport
import dev.robocode.tankroyale.rumble.GitHubTokens
import dev.robocode.tankroyale.rumble.LoadResult
import dev.robocode.tankroyale.rumble.RumbleEnvironments
import dev.robocode.tankroyale.rumble.SaveResult
import dev.robocode.tankroyale.rumble.SentBot
import dev.robocode.tankroyale.rumble.SignInIdentity
import dev.robocode.tankroyale.rumble.SignInRefused
import dev.robocode.tankroyale.rumble.SignInStore
import dev.robocode.tankroyale.rumble.SubmissionOutcome
import dev.robocode.tankroyale.rumble.SubmissionReadiness
import dev.robocode.tankroyale.rumble.SubmissionResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

class RumbleCliBotTest {

    private val refreshToken = "ghr_FAKEREFRESHTOKEN0123456789"
    private val accessToken = "ghu_FAKEACCESSTOKEN0123456789"
    private val head = "a".repeat(40)

    private val catalog = """{"schemaVersion":1,"generatedAt":"2026-11-01T00:00:00Z","commit":"$head","bots":[]}"""
    private val bans = """{"schemaVersion":1,"bannedOwners":[],"disqualifiedBots":[]}"""

    private class RunResult(val exit: Int, val out: String, val err: String) {
        val json: JsonObject get() = Json.parseToJsonElement(out.trim()).jsonObject
        val outcome: String get() = json["outcome"]!!.jsonPrimitive.content
        val next: String? get() = json["next"]?.jsonObject?.get("command")?.jsonPrimitive?.content
    }

    /** GitHub's sign-in pages: every renewal works. */
    private inner class Auth : GitHubAuthTransport {
        var refuse = false
        override fun postForm(url: String, form: Map<String, String>): AuthResponse =
            if (refuse) AuthResponse(200, mapOf("error" to "bad_refresh_token"))
            else AuthResponse(200, mapOf("access_token" to accessToken, "refresh_token" to refreshToken))
    }

    private class Api : GitHubApi {
        val requests = mutableListOf<String>()
        var login = "alice"
        var headStatus = 200
        override fun get(path: String, token: String?): ApiResponse {
            requests += path
            return when {
                path == "/user" -> ApiResponse(200, """{"login":"$login"}""")
                path.endsWith("/commits/HEAD") -> ApiResponse(headStatus, """{"sha":"${"a".repeat(40)}"}""")
                else -> ApiResponse(404, "{}")
            }
        }

        override fun postJson(path: String, token: String, json: String): ApiResponse = ApiResponse(404, "{}")
        override fun patchJson(path: String, token: String, json: String): ApiResponse = ApiResponse(404, "{}")
    }

    private inner class Source : CatalogSource {
        var catalogBody: String? = catalog
        var owners: String? = null
        var bansBody: String = bans
        override fun catalogText(commit: String): String = catalogBody ?: throw CatalogUnreadable("no catalog")
        override fun archive(commit: String): InputStream = ByteArrayInputStream(ByteArray(0))
        override fun fileText(commit: String, path: String): String? = if (path.endsWith("owners.json")) owners else bansBody
    }

    private class Sending : BotSending {
        var readiness: SubmissionReadiness = SubmissionReadiness.Ready("alice", "alice/rumble-bots-test")
        var result: ((StagedSubmission) -> SubmissionResult) = { SubmissionResult.Submitted("https://github.com/x/y/pull/7", 7, "submit/MyBot-1.0.0", SubmissionOutcome.OPENED) }
        val sent = mutableListOf<Pair<StagedSubmission, Boolean>>()
        override fun checkReadiness(tokens: GitHubTokens) = readiness
        override fun submit(tokens: GitHubTokens, submission: StagedSubmission, authorCertifiesLicense: Boolean): SubmissionResult {
            sent += submission to authorCertifiesLicense
            return result(submission)
        }
    }

    private class Progress : BotProgressReading {
        var found: SentBot? = SentBot(7, "1.0.0")
        var findFails: IOException? = null
        var state: BotProgress = BotProgress.WaitingForChecks
        var sawCatalog: Boolean? = null
        override fun find(tokens: GitHubTokens, account: String, name: String): SentBot? {
            findFails?.let { throw it }
            return found
        }

        override fun read(tokens: GitHubTokens, pullRequestNumber: Int, name: String, version: String, catalog: dev.robocode.tankroyale.rumble.Catalog?): BotProgress {
            sawCatalog = catalog != null
            return state
        }
    }

    private inner class World(val state: Path, val redaction: Redaction? = null) {
        val auth = Auth()
        val api = Api()
        val source = Source()
        val sending = Sending()
        val progress = Progress()
        private val services = Services(
            { auth }, { api }, java.time.Clock.systemUTC(), { }, readEnv = { null },
            catalog = { source }, makeSubmission = { _, _ -> sending }, makeProgress = { _, _ -> progress },
        )

        fun run(vararg args: String): RunResult {
            val out = ByteArrayOutputStream()
            val err = ByteArrayOutputStream()
            val printOut = PrintStream(out, true, Charsets.UTF_8)
            val printErr = PrintStream(err, true, Charsets.UTF_8)
            val io = if (redaction == null) Io(printOut, printErr, { state }) else Io(printOut, printErr, { state }, redaction)
            val code = RumbleCli.run(arrayOf(*args), io, services)
            return RunResult(code, out.toString(Charsets.UTF_8), err.toString(Charsets.UTF_8))
        }

        fun test(vararg args: String) = run(*args, "-e", "test", "--json")

        fun signedIn(account: String? = "alice") {
            val store = SignInStore(RumbleEnvironments.TEST, state)
            assertEquals(SaveResult.Saved, store.beginSignIn(refreshToken, null))
            if (account != null) store.saveAccount((store.load() as LoadResult.Found).signInId, account)
        }
    }

    /** A bot folder that passes the checks, below [parent]. */
    private fun bot(parent: Path, name: String = "MyBot", version: String = "1.0.0", license: String? = "MIT"): Path {
        val dir = parent.resolve(name)
        val licenseLine = license?.let { "\"license\": \"$it\"," }.orEmpty()
        write(dir.resolve("$name.json"), """{ "name": "$name", "version": "$version", "authors": ["A. Author"], $licenseLine "description": "A test bot", "platform": "JVM" }""")
        write(dir.resolve("$name.sh"), "#!/bin/sh\n")
        write(dir.resolve("$name.cmd"), "@echo off\r\n")
        write(dir.resolve("src/$name.java"), "import dev.robocode.tankroyale.botapi.Bot;\nclass $name\n")
        return dir
    }

    private fun write(file: Path, text: String) {
        Files.createDirectories(file.parent)
        Files.writeString(file, text)
    }

    private fun tree(root: Path): Map<String, Long> =
        if (!Files.exists(root)) emptyMap()
        else root.toFile().walkTopDown().filter { it.isFile }.associate { it.relativeTo(root.toFile()).path to it.length() + it.lastModified() }

    // RCI-032

    @Test
    @Tag("RCI-032")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI032_UnitPositive_aPassingBotIsExitZeroWithNoSignInNoNetworkAndNothingWritten(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        val folder = bot(dir.resolve("work"))
        val before = tree(dir)

        val json = world.test("bot", "check", folder.toString())
        val text = world.run("bot", "check", folder.toString(), "-e", "test")

        assertEquals(0, json.exit, json.out + json.err)
        assertEquals("passes", json.outcome)
        assertEquals("bot check", json.json["command"]!!.jsonPrimitive.content)
        assertEquals(0, json.json["findings"]!!.jsonArray.size)
        assertEquals(0, text.exit)
        assertTrue(world.api.requests.isEmpty())
        assertEquals(before, tree(dir), "nothing is written")
        assertFalse(Files.exists(dir.resolve("state")))
    }

    @Test
    @Tag("RCI-032")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI032_UnitNegative_everyProblemIsListedWithItsFixAndAFolderThatIsNoBotIsRefused(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        val broken = bot(dir.resolve("work"), "Broken", license = null)
        Files.delete(broken.resolve("Broken.sh"))

        val answer = world.test("bot", "check", broken.toString())
        val findings = answer.json["findings"]!!.jsonArray.map { it.jsonObject }

        assertEquals(2, answer.exit)
        assertEquals("check-failed", answer.outcome)
        assertTrue(findings.size >= 2, answer.out)
        findings.forEach { f -> listOf("code", "file", "message", "fix").forEach { assertTrue(f[it]!!.jsonPrimitive.content.isNotBlank(), it) } }

        val none = world.test("bot", "check", dir.resolve("not-a-bot").toString())
        assertEquals(2, none.exit)
        assertEquals("check-failed", none.outcome)
        assertTrue(world.api.requests.isEmpty())
    }

    // RCI-033

    @Test
    @Tag("RCI-033")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI033_UnitPositive_aDryRunStagesChecksAndStopsBeforeSendingAnythingWithoutAConfirmation(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        world.signedIn()
        val folder = bot(dir.resolve("work"))

        val answer = world.test("bot", "send", folder.toString(), "--dry-run")

        assertEquals(0, answer.exit, answer.out + answer.err)
        assertEquals("dry-run", answer.outcome)
        assertTrue(world.sending.sent.isEmpty(), "nothing is sent")
        assertTrue(answer.json["catalogChecked"]!!.jsonPrimitive.boolean)
        assertEquals("MyBot", answer.json["bot"]!!.jsonObject["name"]!!.jsonPrimitive.content)
        assertTrue(Files.isDirectory(RumbleEnvironments.TEST.stateRoot(dir.resolve("state")).resolve("staging")), "staged in the state folder")
        assertTrue(answer.next!!.contains("--certify-license"))
        assertFalse(answer.out.contains(accessToken) || answer.out.contains(refreshToken))
    }

    @Test
    @Tag("RCI-033")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI033_UnitNegative_aDryRunStopsAtAProblemOfTheBotAndAtAnAccountThatIsNotReady(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        world.signedIn()
        val broken = bot(dir.resolve("work"), "Broken", license = null)

        assertEquals("check-failed", world.test("bot", "send", broken.toString(), "--dry-run").outcome)

        world.sending.readiness = SubmissionReadiness.NoCopy("alice", "https://github.com/robocode-dev/rumble-bots-test/fork")
        val notReady = world.test("bot", "send", bot(dir.resolve("work2")).toString(), "--dry-run")
        assertEquals(2, notReady.exit)
        assertEquals("setup-needed", notReady.outcome)
        assertTrue(world.sending.sent.isEmpty())
    }

    // RCI-034

    @Test
    @Tag("RCI-034")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI034_UnitPositive_withTheCertifyOptionTheBotIsSentAndTheAnswerSaysTheAuthorConfirmed(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        world.signedIn()
        val folder = bot(dir.resolve("work"))

        val answer = world.test("bot", "send", folder.toString(), "--certify-license")

        assertEquals(0, answer.exit, answer.out + answer.err)
        assertTrue(answer.json["licenseCertified"]!!.jsonPrimitive.boolean)
        assertEquals(listOf(true), world.sending.sent.map { it.second })
        val help = world.run("bot", "send", "--help").out
        assertTrue(help.contains("--certify-license") && help.contains("PUBLIC"), help)
    }

    @Test
    @Tag("RCI-034")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI034_UnitNegative_withoutTheOptionNothingIsSentAndNothingAsksForIt(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        world.signedIn()
        val folder = bot(dir.resolve("work"))

        val answer = world.test("bot", "send", folder.toString())

        assertEquals(ExitCodes.REFUSED, answer.exit)
        assertEquals("license-not-certified", answer.outcome)
        assertTrue(world.sending.sent.isEmpty())
        assertTrue(world.api.requests.isEmpty(), "GitHub is not touched")
        assertFalse(answer.json["licenseCertified"]!!.jsonPrimitive.boolean)
        // Nothing else turns it on: not the environment, not an earlier run.
        world.test("bot", "send", folder.toString(), "--certify-license")
        assertEquals("license-not-certified", world.test("bot", "send", folder.toString()).outcome, "not remembered")
        assertEquals(1, world.sending.sent.size)
    }

    // RCI-035

    @Test
    @Tag("RCI-035")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI035_UnitPositive_everyAnswerSaysTheSourceIsPublicAndListsTheFilesInBothForms(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        world.signedIn()
        val folder = bot(dir.resolve("work"))

        listOf(
            arrayOf("--dry-run"), arrayOf(), arrayOf("--certify-license"),
        ).forEach { options ->
            val json = world.test("bot", "send", folder.toString(), *options)
            assertTrue(json.json["sourceIsPublic"]!!.jsonPrimitive.boolean, options.toList().toString())
            assertTrue(json.json["notice"]!!.jsonPrimitive.content.contains("public"))
            val files = json.json["publicFiles"]!!.jsonArray.map { it.jsonPrimitive.content }
            if (json.outcome != "license-not-certified") assertTrue(files.any { it.endsWith("MyBot.json") }, files.toString())
        }
        val text = world.run("bot", "send", folder.toString(), "-e", "test", "--dry-run")
        assertTrue(text.out.contains("PUBLIC") && text.out.contains("MyBot.json"), text.out)
    }

    @Test
    @Tag("RCI-035")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI035_UnitNegative_theNoticeIsAlsoGivenWhenAProblemStopsTheSendAndInTheTestEnvironment(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        world.signedIn()
        val broken = bot(dir.resolve("work"), "Broken", license = null)

        val stopped = world.test("bot", "send", broken.toString(), "--certify-license")

        assertEquals("check-failed", stopped.outcome)
        assertTrue(stopped.json["sourceIsPublic"]!!.jsonPrimitive.boolean)
        assertEquals("test", stopped.json["environment"]!!.jsonPrimitive.content)
    }

    // RCI-036

    @Test
    @Tag("RCI-036")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI036_UnitPositive_eachOutcomeOfSendingIsSaidWithTheAddressAndTheNextStep(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        world.signedIn()
        val folder = bot(dir.resolve("work"))
        SubmissionOutcome.entries.forEach { outcome ->
            world.sending.result = { SubmissionResult.Submitted("https://github.com/robocode-dev/rumble-bots-test/pull/7", 7, "submit/MyBot-1.0.0", outcome) }

            val answer = world.test("bot", "send", folder.toString(), "--certify-license")

            assertEquals(0, answer.exit)
            assertEquals(outcome.name.lowercase(), answer.outcome)
            assertEquals(7, answer.json["pullRequestNumber"]!!.jsonPrimitive.content.toInt())
            assertEquals("https://github.com/robocode-dev/rumble-bots-test/pull/7", answer.json["pullRequestUrl"]!!.jsonPrimitive.content)
            assertEquals("rumble bot status MyBot -e test", answer.next)
        }
    }

    @Test
    @Tag("RCI-036")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI036_UnitNegative_aFailureAndARefusalFromSendingAreAnsweredWithTheirReason(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        world.signedIn()
        val folder = bot(dir.resolve("work"))

        world.sending.result = { SubmissionResult.Failed("GitHub is limiting requests. Try again later.") }
        val failed = world.test("bot", "send", folder.toString(), "--certify-license")
        assertEquals(ExitCodes.FAILED, failed.exit)
        assertEquals("failed", failed.outcome)
        assertTrue(failed.json["message"]!!.jsonPrimitive.content.contains("limiting"))

        world.sending.result = { SubmissionResult.Refused("The bot's name may hold only letters.") }
        assertEquals(ExitCodes.REFUSED, world.test("bot", "send", folder.toString(), "--certify-license").exit)
    }

    // RCI-037

    @Test
    @Tag("RCI-037")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI037_UnitPositive_aMissingSetupIsSaidWithTheAddressAndNothingIsSent(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        world.signedIn()
        val folder = bot(dir.resolve("work"))
        mapOf(
            SubmissionReadiness.NoCopy("alice", "https://github.com/robocode-dev/rumble-bots-test/fork") to "no-copy",
            SubmissionReadiness.AppNotInstalled("alice", "https://github.com/apps/x/installations/new") to "app-not-installed",
            SubmissionReadiness.NotRumblesRepository("alice", "https://github.com/alice/rumble-bots-test") to "not-a-copy",
        ).forEach { (readiness, setup) ->
            world.sending.readiness = readiness

            val answer = world.test("bot", "send", folder.toString(), "--certify-license")

            assertEquals(ExitCodes.REFUSED, answer.exit)
            assertEquals("setup-needed", answer.outcome)
            assertEquals(setup, answer.json["setup"]!!.jsonPrimitive.content)
            assertTrue(answer.json["setupUrl"]!!.jsonPrimitive.content.startsWith("https://github.com/"))
            assertTrue(answer.json["message"]!!.jsonPrimitive.content.contains(answer.json["setupUrl"]!!.jsonPrimitive.content))
        }
        assertTrue(world.sending.sent.isEmpty())
    }

    @Test
    @Tag("RCI-037")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI037_UnitNegative_theRumblesRulesStopASendAndAnUnreadableCatalogDoesNot(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        world.signedIn()
        val folder = bot(dir.resolve("work"))

        world.source.bansBody = """{"schemaVersion":1,"bannedOwners":[{"account":"alice"}],"disqualifiedBots":[]}"""
        val banned = world.test("bot", "send", folder.toString(), "--certify-license")
        assertEquals(ExitCodes.REFUSED, banned.exit)
        assertEquals("precheck-failed", banned.outcome)
        assertEquals("account-banned", banned.json["findings"]!!.jsonArray.first().jsonObject["code"]!!.jsonPrimitive.content)
        assertTrue(world.sending.sent.isEmpty())

        world.source.bansBody = bans
        world.source.owners = """{"schemaVersion":1,"owners":[{"ownerId":"bob","accounts":["bob"],"bots":["MyBot"]}]}"""
        assertEquals("precheck-failed", world.test("bot", "send", folder.toString(), "--certify-license").outcome)

        world.source.owners = null
        world.source.catalogBody = null
        val unreadable = world.test("bot", "send", folder.toString(), "--certify-license")
        assertEquals(0, unreadable.exit)
        assertFalse(unreadable.json["catalogChecked"]!!.jsonPrimitive.boolean)
        assertTrue(unreadable.json["catalogNote"]!!.jsonPrimitive.content.contains("were not checked"))
        assertEquals(1, world.sending.sent.size)
    }

    // RCI-038

    @Test
    @Tag("RCI-038")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI038_UnitPositive_noSavedSignInIsExitThreeWithLoginAsTheNextStep(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        val folder = bot(dir.resolve("work"))

        listOf(
            arrayOf("bot", "send", folder.toString(), "--certify-license"),
            arrayOf("bot", "send", folder.toString(), "--dry-run"),
            arrayOf("bot", "status", "MyBot"),
        ).forEach { args ->
            val answer = world.test(*args)

            assertEquals(ExitCodes.SIGN_IN_NEEDED, answer.exit, args.toList().toString())
            assertEquals("no-sign-in", answer.outcome)
            assertEquals("rumble login -e test", answer.next)
        }
        assertTrue(world.sending.sent.isEmpty())
    }

    @Test
    @Tag("RCI-038")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI038_UnitNegative_aRefusedSignInOrTokenIsRemovedAndAnEnvironmentWithoutAnAppIsRefusedFirst(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        world.signedIn()
        val folder = bot(dir.resolve("work"))

        world.sending.result = { SubmissionResult.SignInNeeded("GitHub did not accept the sign-in. Sign in again.") }
        val refusedToken = world.test("bot", "send", folder.toString(), "--certify-license")
        assertEquals(ExitCodes.SIGN_IN_NEEDED, refusedToken.exit)
        assertEquals("sign-in-refused", refusedToken.outcome)
        assertEquals("no-sign-in", world.test("bot", "status", "MyBot").outcome, "the refused sign-in is gone")

        world.signedIn()
        world.auth.refuse = true
        assertEquals("sign-in-refused", world.test("bot", "send", folder.toString(), "--certify-license").outcome)

        val publicEnvironment = world.run("bot", "send", folder.toString(), "--certify-license", "--json")
        assertEquals(ExitCodes.REFUSED, publicEnvironment.exit)
        assertEquals("no-sign-in-app", publicEnvironment.outcome)
        assertEquals(ExitCodes.REFUSED, world.run("bot", "status", "MyBot", "--json").exit)
    }

    // RCI-039

    @Test
    @Tag("RCI-039")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI039_UnitPositive_eachStateOfAPullRequestHasItsOwnOutcomeWord(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        world.signedIn()
        mapOf(
            BotProgress.WaitingForChecks to "waiting-for-checks",
            BotProgress.BeingChecked() to "being-checked",
            BotProgress.SentBack("The bot cannot start.") to "sent-back",
            BotProgress.WaitingForModerator to "waiting-for-moderator",
            BotProgress.Accepted to "accepted",
            BotProgress.Merged to "merged",
            BotProgress.InCatalog("1.0.0") to "in-catalog",
            BotProgress.Closed to "closed",
        ).forEach { (state, outcome) ->
            world.progress.state = state

            val answer = world.test("bot", "status", "MyBot")

            assertEquals(0, answer.exit, outcome)
            assertEquals(outcome, answer.outcome)
            assertEquals(7, answer.json["pullRequestNumber"]!!.jsonPrimitive.content.toInt())
            assertEquals("bot status", answer.json["command"]!!.jsonPrimitive.content)
        }
        assertEquals("The bot cannot start.", run { world.progress.state = BotProgress.SentBack("The bot cannot start."); world.test("bot", "status", "MyBot").json["reason"]!!.jsonPrimitive.content })
    }

    @Test
    @Tag("RCI-039")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI039_UnitNegative_aBotNeverSentIsRefusedAndAnUnreadableCatalogIsSaidAndAFailureIsExitOne(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        world.signedIn()

        world.progress.found = null
        val never = world.test("bot", "status", "MyBot")
        assertEquals(ExitCodes.REFUSED, never.exit)
        assertEquals("not-sent", never.outcome)
        assertTrue(never.next!!.startsWith("rumble bot send"))

        world.progress.found = SentBot(7, "1.0.0")
        world.source.catalogBody = null
        val noCatalog = world.test("bot", "status", "MyBot")
        assertEquals(0, noCatalog.exit)
        assertEquals("waiting-for-checks", noCatalog.outcome, "the state of the pull request is not hidden")
        assertTrue(noCatalog.json["catalogNote"]!!.jsonPrimitive.content.contains("catalog could not be read"))
        assertFalse(world.progress.sawCatalog!!)

        world.progress.findFails = IOException("GitHub cannot be reached. Try again when you are online.")
        val failed = world.test("bot", "status", "MyBot")
        assertEquals(ExitCodes.FAILED, failed.exit)
        assertEquals("failed", failed.outcome)

        world.progress.findFails = SignInRefused()
        assertEquals(ExitCodes.SIGN_IN_NEEDED, world.test("bot", "status", "MyBot").exit)
    }

    // RCI-042

    @Test
    @Tag("RCI-042")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI042_UnitPositive_theBotCommandsAnswerInBothFormsWithAnOutcomeTheEnvironmentAndARunnableNextStep(@TempDir dir: Path) {
        val world = World(dir.resolve("state"))
        world.signedIn()
        val folder = bot(dir.resolve("work"))

        listOf(
            arrayOf("bot", "check", folder.toString()),
            arrayOf("bot", "send", folder.toString(), "--dry-run"),
            arrayOf("bot", "status", "MyBot"),
        ).forEach { args ->
            val asJson = world.test(*args)
            val asText = world.run(*args, "-e", "test")

            assertEquals(1, asJson.out.trim().lines().size)
            assertEquals(asJson.exit, asText.exit)
            assertEquals("test", asJson.json["environment"]!!.jsonPrimitive.content)
            assertTrue(asJson.json["outcome"]!!.jsonPrimitive.content.isNotBlank())
            assertTrue(asText.out.contains("Development (test)"), asText.out)
            assertEquals("", asJson.err + asText.err)
            assertFalse((asJson.out + asText.out).let { it.contains(accessToken) || it.contains(refreshToken) || Regex("[A-Za-z]:[\\\\/]").containsMatchIn(it) })
        }
        assertEquals(2, world.run("bot", "nonsense", "--json").exit)
        assertEquals("bad-input", world.run("bot", "nonsense", "--json").outcome)
        assertEquals(2, world.run("bot", "check", "--json").exit)
    }

    @Test
    @Tag("RCI-042")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI042_UnitNegative_aUserNamedLikeAProgramWordChangesNothingButTheNameOfABotFromOutside(@TempDir dir: Path) {
        listOf("test", "bot", "send").forEach { user ->
            val world = World(dir.resolve("state-$user"), Redaction("C:\\nowhere", user))
            world.signedIn()
            val folder = bot(dir.resolve("work-$user"), "MyBot")

            val answer = world.test("bot", "send", folder.toString(), "--dry-run")

            assertEquals("dry-run", answer.outcome, user)
            assertEquals("test", answer.json["environment"]!!.jsonPrimitive.content)
            assertEquals("bot send", answer.json["command"]!!.jsonPrimitive.content)
            assertEquals("rumble bot send <folder> --certify-license -e test", answer.next)
        }
        // A bot named like the user has its name removed from the text, which is text from outside.
        val world = World(dir.resolve("state-named"), Redaction("C:\\nowhere", "secretname"))
        world.signedIn()
        val named = bot(dir.resolve("work-named"), "secretname")
        val answer = world.test("bot", "send", named.toString(), "--dry-run")
        assertFalse(answer.out.contains("secretname"), answer.out)
        assertNull(answer.json["bot"]?.jsonObject?.get("name")?.jsonPrimitive?.content?.takeIf { it == "secretname" })
    }
}
