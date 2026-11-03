package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.rumble.ApiResponse
import dev.robocode.tankroyale.rumble.AuthResponse
import dev.robocode.tankroyale.rumble.GitHubApi
import dev.robocode.tankroyale.rumble.GitHubAuthTransport
import dev.robocode.tankroyale.rumble.GitHubSignIn
import dev.robocode.tankroyale.rumble.RegistrationRecord
import dev.robocode.tankroyale.rumble.RegistrationStore
import dev.robocode.tankroyale.rumble.RumbleEnvironments
import dev.robocode.tankroyale.rumble.SaveResult
import dev.robocode.tankroyale.rumble.SavedAccountStore
import dev.robocode.tankroyale.rumble.SignInStore
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.ArrayDeque
import java.util.Base64

class RumbleCliSignInTest {

    private val deviceSecret = "DEVICE-SECRET-1234567890"
    private val accessToken = "ghu_FAKEACCESSTOKEN0123456789"
    private val refreshToken = "ghr_FAKEREFRESHTOKEN0123456789"
    private val address = "https://github.com/login/device"
    private val userCode = "WDJB-MJHT"

    private class MovableClock(var now: Instant = Instant.parse("2026-11-03T10:00:00Z")) : Clock() {
        override fun getZone(): ZoneId = ZoneId.of("UTC")
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = now
    }

    /** Plays GitHub's sign-in pages. */
    private inner class FakeAuth : GitHubAuthTransport {
        val codeRequests = mutableListOf<Map<String, String>>()
        val checks = mutableListOf<Map<String, String>>()
        val renewals = mutableListOf<Map<String, String>>()
        val answers = ArrayDeque<Map<String, String>>()
        var renewal: Map<String, String> = tokens()
        var offline = false

        fun tokens(refresh: Boolean = true) = buildMap {
            put("access_token", accessToken)
            if (refresh) put("refresh_token", refreshToken)
        }

        override fun postForm(url: String, form: Map<String, String>): AuthResponse {
            if (offline) throw IOException("offline")
            return when {
                url == GitHubSignIn.DEVICE_CODE_URL -> {
                    codeRequests += form
                    AuthResponse(200, mapOf("device_code" to deviceSecret, "user_code" to userCode, "verification_uri" to address, "expires_in" to "900", "interval" to "5"))
                }
                form["grant_type"] == "refresh_token" -> {
                    renewals += form
                    AuthResponse(200, renewal)
                }
                else -> {
                    checks += form
                    AuthResponse(200, answers.poll() ?: mapOf("error" to "authorization_pending"))
                }
            }
        }
    }

    /** Plays GitHub's API and the Rumble result repository. */
    private class FakeApi(var login: String = "alice") : GitHubApi {
        val requests = mutableListOf<String>()
        val created = mutableListOf<String>()
        var userStatus = 200
        var clientId: String? = null
        var registerOnCreate = false
        var openIssues = "[]"
        var closedIssues = "[]"
        var comments = "[]"

        override fun get(path: String, token: String?): ApiResponse {
            requests += path
            val data = "/repos/robocode-dev/rumble-data-test"
            return when {
                path == "/user" -> ApiResponse(userStatus, """{"login":"$login"}""")
                path == "$data/contents/clients/$login.json" -> clientId?.let { ApiResponse(200, file(login, it)) } ?: ApiResponse(404, "{}")
                path.startsWith("$data/issues?") && path.contains("state=closed") -> ApiResponse(200, closedIssues)
                path.startsWith("$data/issues?") -> ApiResponse(200, openIssues)
                path.startsWith("$data/issues/") -> ApiResponse(200, comments)
                else -> ApiResponse(404, "{}")
            }
        }

        override fun postJson(path: String, token: String, json: String): ApiResponse {
            created += path
            if (registerOnCreate) clientId = "$login-1a2b3c4d"
            return ApiResponse(201, """{"number":9,"html_url":"$ISSUE_URL"}""")
        }

        override fun patchJson(path: String, token: String, json: String) = ApiResponse(404, "{}")

        private fun file(account: String, id: String): String {
            val text = """{"schemaVersion":1,"account":"$account","clientIds":["$id"]}"""
            return """{"content":${JsonPrimitive(Base64.getMimeEncoder().encodeToString(text.toByteArray()))}}"""
        }
    }

    private class Run(val exit: Int, val out: String, val err: String) {
        val json: JsonObject get() = Json.parseToJsonElement(out.trim()).jsonObject
        val outcome: String get() = json["outcome"]!!.jsonPrimitive.content
        val next: String? get() = json["next"]?.jsonObject?.get("command")?.jsonPrimitive?.content
    }

    private inner class World(val state: Path, val redaction: Redaction? = null, val env: Map<String, String> = emptyMap()) {
        val clock = MovableClock()
        val auth = FakeAuth()
        val api = FakeApi()
        var duringSleep: () -> Unit = {}
        var sleeps = 0

        fun services() = Services(
            { auth }, { api }, clock,
            { millis -> sleeps++; clock.now = clock.now.plusMillis(millis); duringSleep() },
            registrationPollSeconds = 1, registrationWaitSeconds = 2, readEnv = { env[it] },
        )

        fun run(vararg args: String): Run {
            val out = ByteArrayOutputStream()
            val err = ByteArrayOutputStream()
            val printOut = PrintStream(out, true, Charsets.UTF_8)
            val printErr = PrintStream(err, true, Charsets.UTF_8)
            val io = if (redaction == null) Io(printOut, printErr, { state }) else Io(printOut, printErr, { state }, redaction)
            val code = RumbleCli.run(arrayOf(*args), io, services())
            return Run(code, out.toString(Charsets.UTF_8), err.toString(Charsets.UTF_8))
        }

        /** Runs [args] for the test environment as JSON. */
        fun test(vararg args: String) = run(*args, "-e", "test", "--json")

        fun advance(seconds: Long) {
            clock.now = clock.now.plusSeconds(seconds)
        }

        fun approveNextCheck(refresh: Boolean = true) {
            auth.answers.add(auth.tokens(refresh))
        }

        fun signIn(): Run {
            test("login")
            advance(5)
            approveNextCheck()
            return test("login", "--finish")
        }

        fun store() = SignInStore(RumbleEnvironments.TEST, state)
        fun folder(): Path = RumbleEnvironments.TEST.stateRoot(state)
    }

    private val record = Instant.parse("2026-11-01T10:00:00Z")

    // RCI-014

    @Test
    @Tag("RCI-014")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI014_UnitPositive_theJsonOptionChangesTheFormOfAnAnswerAndNotWhatTheCommandDoes(@TempDir dir: Path) {
        val asJson = World(dir.resolve("a"))
        val asText = World(dir.resolve("b"))

        val json = asJson.run("login", "-e", "test", "--json")
        val text = asText.run("login", "-e", "test")

        assertEquals(1, json.out.trim().lines().size)
        assertEquals(json.exit, text.exit)
        assertTrue(text.out.contains(userCode) && json.out.contains(userCode))
        assertTrue(text.out.contains(address) && json.out.contains(address))
        assertEquals(asJson.store().loadWaiting()?.userCode, asText.store().loadWaiting()?.userCode)
        assertEquals(1, asJson.auth.codeRequests.size)
        assertEquals(1, asText.auth.codeRequests.size)
        assertEquals("", text.err)
        assertEquals("", json.err)
    }

    @Test
    @Tag("RCI-014")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI014_UnitNegative_aFailureNoCommandExpectedStillAnswersInOneDocumentAndAPathFullOfQuotesStaysValidJson(@TempDir dir: Path) {
        val blocker = dir.resolve("a-file").also { Files.writeString(it, "not a folder") }
        val world = World(dir)

        val failed = world.run("register", "-e", "test", "--json", "--state-folder", blocker.toString())
        assertEquals(ExitCodes.FAILED, failed.exit, failed.out + failed.err)
        assertEquals("failed", failed.outcome)
        assertEquals("", failed.err)

        val badPath = world.run("status", "--json", "--state-folder", "bad\u0000path")
        assertEquals(ExitCodes.REFUSED, badPath.exit)
        assertEquals("bad-input", badPath.outcome)

        listOf("C:\\foo\"bar", "/home/x\"y").forEach { word ->
            val answer = world.run(word, "--json")
            assertEquals(ExitCodes.REFUSED, answer.exit)
            assertEquals("bad-input", answer.outcome)
            assertFalse(answer.json["message"]!!.jsonPrimitive.content.contains("foo"), answer.out)
        }
    }

    // RCI-015

    @Test
    @Tag("RCI-015")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI015_UnitPositive_aMissingSignInIsThreeAndNotFinishedIsFourWithAnOutcomeWord(@TempDir dir: Path) {
        val world = World(dir)

        val needed = world.test("register")
        assertEquals(ExitCodes.SIGN_IN_NEEDED, needed.exit)
        assertEquals(3, needed.exit)
        assertEquals("no-sign-in", needed.outcome)

        world.test("login")
        val waiting = world.test("login", "--finish")
        assertEquals(ExitCodes.NOT_FINISHED, waiting.exit)
        assertEquals(4, waiting.exit)
        assertEquals("waiting", waiting.outcome)
    }

    // RCI-016

    @Test
    @Tag("RCI-016")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI016_UnitPositive_statusGivesTheAccountTheWaitingCodeAndWhatToDoNextWithoutContactingAnyone(@TempDir dir: Path) {
        val world = World(dir)

        val first = world.test("status")
        assertEquals("rumble login -e test", first.next)
        assertFalse(first.json["ready"]!!.jsonPrimitive.boolean)

        world.test("login")
        val waiting = world.test("status")
        assertEquals(userCode, waiting.json["waitingCode"]!!.jsonObject["userCode"]!!.jsonPrimitive.content)
        assertEquals("rumble login --finish -e test", waiting.next)

        world.advance(5)
        world.approveNextCheck()
        world.test("login", "--finish")
        val signedIn = world.test("status")
        assertEquals("alice", signedIn.json["signIn"]!!.jsonObject["account"]!!.jsonPrimitive.content)
        assertFalse(signedIn.json["signIn"]!!.jsonObject["checkedWithGitHub"]!!.jsonPrimitive.boolean)
        assertEquals("rumble register -e test", signedIn.next)

        RegistrationStore(RumbleEnvironments.TEST, dir).save(RegistrationRecord("alice", "alice-1", "test", record))
        val ready = world.test("status")
        assertTrue(ready.json["ready"]!!.jsonPrimitive.boolean)
        assertNull(ready.next)
        assertTrue(world.run("status", "-e", "test").out.contains("not checked with GitHub"))
    }

    @Test
    @Tag("RCI-016")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI016_UnitNegative_aRegistrationOfAnotherAccountIsNotShownAndStatusMakesNoRequestAndWritesNothing(@TempDir dir: Path) {
        val world = World(dir)
        RegistrationStore(RumbleEnvironments.TEST, dir).save(RegistrationRecord("bob", "bob-1", "test", record))
        assertEquals(SaveResult.Saved, world.store().save(refreshToken, null))
        SavedAccountStore(RumbleEnvironments.TEST, dir).save("alice")
        val before = world.folder().toFile().listFiles()!!.map { it.name to it.lastModified() }.toSet()

        val status = world.test("status")

        assertFalse(status.json["registration"]!!.jsonObject["saved"]!!.jsonPrimitive.boolean)
        assertFalse(status.out.contains("bob"))
        assertEquals("rumble register -e test", status.next)
        assertTrue(world.api.requests.isEmpty())
        assertTrue(world.auth.renewals.isEmpty() && world.auth.checks.isEmpty())
        assertEquals(before, world.folder().toFile().listFiles()!!.map { it.name to it.lastModified() }.toSet())
    }

    // RCI-017

    @Test
    @Tag("RCI-017")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI017_UnitPositive_webAddressesAndAnAccountNamedLikeTheUserStayWholeInAnAnswer(@TempDir dir: Path) {
        val world = World(dir, Redaction("C:\\Users\\alice", "alice"))
        world.api.clientId = null

        val started = world.test("login")
        assertEquals(address, started.json["verificationUri"]!!.jsonPrimitive.content)
        assertTrue(world.run("login", "-e", "test").out.contains(address))

        world.advance(5)
        world.approveNextCheck()
        val signedIn = world.test("login", "--finish")
        assertEquals("alice", signedIn.json["account"]!!.jsonPrimitive.content)
        assertTrue(signedIn.json["message"]!!.jsonPrimitive.content.contains("alice"))

        val pending = world.test("register")
        assertEquals(ISSUE_URL, pending.json["issueUrl"]!!.jsonPrimitive.content)
    }

    @Test
    @Tag("RCI-017")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI017_UnitNegative_noAnswerOfTheSignInCommandsHoldsATokenOrADeviceCodeAndAPathIsRemovedEvenWithAKeptName(@TempDir dir: Path) {
        val world = World(dir)
        val answers = mutableListOf(world.test("login"), world.run("login", "-e", "test"))
        world.advance(5)
        world.approveNextCheck()
        answers += world.test("login", "--finish")
        answers += world.test("status")
        answers += world.test("register")
        answers += world.test("logout")
        answers.forEach { answer ->
            val text = answer.out + answer.err
            listOf(deviceSecret, accessToken, refreshToken).forEach { assertFalse(text.contains(it), text) }
        }
        listOf("signin.bin", "signin-waiting.bin").map { world.folder().resolve(it) }.filter { Files.exists(it) }.forEach { file ->
            val bytes = String(Files.readAllBytes(file), Charsets.ISO_8859_1)
            listOf(deviceSecret, refreshToken).forEach { assertFalse(bytes.contains(it)) }
        }

        val redaction = Redaction("C:\\Users\\alice", "alice")
        val cleaned = redaction.clean("""alice at https://github.com/x and D:\alice\data and another alice""", setOf("https://github.com/x"))
        assertEquals("<user> at https://github.com/x and <path> and another <user>", cleaned)
        assertEquals("""<path>""", redaction.clean("""C:/alice/x""", setOf("alice")))
    }

    // RCI-019

    @Test
    @Tag("RCI-019")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI019_UnitPositive_loginGivesTheCodeAtOnceAndASecondLoginGivesTheSameCode(@TempDir dir: Path) {
        val world = World(dir)

        val first = world.test("login")

        assertEquals(0, first.exit)
        assertEquals("started", first.outcome)
        assertEquals(userCode, first.json["userCode"]!!.jsonPrimitive.content)
        assertEquals(address, first.json["verificationUri"]!!.jsonPrimitive.content)
        assertEquals(Instant.parse("2026-11-03T10:15:00Z"), Instant.parse(first.json["expiresAt"]!!.jsonPrimitive.content))
        assertEquals("rumble login --finish -e test", first.next)
        assertFalse(first.out.contains(deviceSecret))

        val second = world.test("login")
        assertEquals(userCode, second.json["userCode"]!!.jsonPrimitive.content)
        assertTrue(second.json["reused"]!!.jsonPrimitive.boolean)
        assertEquals(1, world.auth.codeRequests.size)
    }

    @Test
    @Tag("RCI-019")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI019_UnitNegative_noAppIsRefusedFirstAWorkingSignInGivesNoCodeAndARevokedOneGivesANewCode(@TempDir dir: Path) {
        val world = World(dir)
        val noApp = world.run("login", "--json")
        assertEquals(ExitCodes.REFUSED, noApp.exit)
        assertEquals("no-sign-in-app", noApp.outcome)
        assertTrue(world.auth.codeRequests.isEmpty())
        assertFalse(Files.exists(RumbleEnvironments.PUBLIC.stateRoot(dir)))

        world.signIn()
        world.auth.codeRequests.clear()
        val already = world.test("login")
        assertEquals("already-signed-in", already.outcome)
        assertEquals("alice", already.json["account"]!!.jsonPrimitive.content)
        assertTrue(world.auth.codeRequests.isEmpty())

        world.auth.renewal = mapOf("error" to "bad_refresh_token")
        val revoked = world.test("login")
        assertEquals("started", revoked.outcome)
        assertEquals(1, world.auth.codeRequests.size)
    }

    // RCI-020

    @Test
    @Tag("RCI-020")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI020_UnitPositive_finishChecksOnceAtThePaceAndAWaitEndsWithinTheTimeAsked(@TempDir dir: Path) {
        val world = World(dir)
        world.test("login")

        val tooSoon = world.test("login", "--finish")
        assertEquals(ExitCodes.NOT_FINISHED, tooSoon.exit)
        assertEquals("waiting", tooSoon.outcome)
        assertEquals(5, tooSoon.json["retryAfterSeconds"]!!.jsonPrimitive.long)
        assertTrue(world.auth.checks.isEmpty(), "within the pace GitHub is not contacted")

        world.advance(5)
        assertEquals("waiting", world.test("login", "--finish").outcome)
        assertEquals(1, world.auth.checks.size)

        val start = world.clock.now
        val waited = world.test("login", "--finish", "--wait", "7")
        assertEquals("waiting", waited.outcome)
        assertEquals(2, world.auth.checks.size, "one check after five seconds, and the next would be too late")
        assertFalse(world.clock.now.isAfter(start.plusSeconds(7)))

        world.approveNextCheck()
        assertEquals("signed-in", world.test("login", "--finish", "--wait", "20").outcome)
    }

    @Test
    @Tag("RCI-020")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI020_UnitNegative_aSlowerPaceIsKeptAndExpiredDeniedAndMissingCodesAreExitThree(@TempDir dir: Path) {
        val world = World(dir)
        world.test("login")
        world.advance(5)
        world.auth.answers.add(mapOf("error" to "slow_down", "interval" to "30"))
        assertEquals(30, world.test("login", "--finish").json["retryAfterSeconds"]!!.jsonPrimitive.long)
        world.advance(29)
        assertEquals("waiting", world.test("login", "--finish").outcome)
        assertEquals(1, world.auth.checks.size, "the slower pace holds between runs")

        world.advance(900)
        val expired = world.test("login", "--finish")
        assertEquals(ExitCodes.SIGN_IN_NEEDED, expired.exit)
        assertEquals("code-expired", expired.outcome)
        assertEquals("rumble login -e test", expired.next)
        assertNull(world.store().loadWaiting())

        val none = world.test("login", "--finish")
        assertEquals(ExitCodes.SIGN_IN_NEEDED, none.exit)
        assertEquals("no-waiting-code", none.outcome)

        world.test("login")
        world.advance(5)
        world.auth.answers.add(mapOf("error" to "access_denied"))
        val denied = world.test("login", "--finish")
        assertEquals(ExitCodes.SIGN_IN_NEEDED, denied.exit)
        assertEquals("code-denied", denied.outcome)
        assertNull(world.store().loadWaiting())
    }

    // RCI-021

    @Test
    @Tag("RCI-021")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI021_UnitPositive_anApprovedSignInIsSavedWithItsAccountAndAnotherAccountsRegistrationIsRemoved(@TempDir dir: Path) {
        val world = World(dir)
        val registrations = RegistrationStore(RumbleEnvironments.TEST, dir)
        registrations.save(RegistrationRecord("bob", "bob-1", "test", record))

        val signedIn = world.signIn()

        assertEquals(0, signedIn.exit)
        assertEquals("signed-in", signedIn.outcome)
        assertEquals("alice", signedIn.json["account"]!!.jsonPrimitive.content)
        assertEquals("rumble register -e test", signedIn.next)
        assertEquals("alice", SavedAccountStore(RumbleEnvironments.TEST, dir).load())
        assertNull(registrations.loadAny())
        assertNull(world.store().loadWaiting())
    }

    @Test
    @Tag("RCI-021")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI021_UnitNegative_aSignInNotSavedSendsTheCallerBackAndAnUnknownAccountIsFilledInLater(@TempDir dir: Path) {
        val noRefresh = World(dir.resolve("a"))
        noRefresh.test("login")
        noRefresh.advance(5)
        noRefresh.approveNextCheck(refresh = false)
        val notSaved = noRefresh.test("login", "--finish")
        assertEquals(ExitCodes.FAILED, notSaved.exit)
        assertEquals("not-saved", notSaved.outcome)
        assertEquals("rumble login -e test", notSaved.next)
        assertNull(noRefresh.store().loadWaiting(), "the code is used up")

        val noName = World(dir.resolve("b"))
        noName.api.userStatus = 500
        val signedIn = noName.signIn()
        assertEquals("signed-in", signedIn.outcome)
        assertNull(signedIn.json["account"]?.jsonPrimitive?.takeIf { it.isString }?.content)
        assertEquals("rumble login -e test", noName.test("status").next, "the name is still missing")

        noName.api.userStatus = 200
        assertEquals("alice", noName.test("login").json["account"]!!.jsonPrimitive.content)
        assertEquals("rumble register -e test", noName.test("status").next)
    }

    @Test
    @Tag("RCI-021")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI021_UnitNegative_aNewSignInIsNeverTakenForTheAccountBefore(@TempDir dir: Path) {
        val world = World(dir)
        world.signIn()
        RegistrationStore(RumbleEnvironments.TEST, dir).save(RegistrationRecord("alice", "alice-1", "test", record))

        world.auth.renewal = mapOf("error" to "bad_refresh_token")
        assertEquals("started", world.test("login").outcome, "the sign-in was revoked")
        assertNull(world.test("status").json["signIn"]!!.jsonObject["account"], "a name left without a sign-in says nothing")

        world.api.login = "bob"
        world.api.userStatus = 500
        world.advance(5)
        world.approveNextCheck()
        val signedIn = world.test("login", "--finish")
        assertEquals("signed-in", signedIn.outcome)
        assertFalse(signedIn.out.contains("alice"), signedIn.out)

        val status = world.test("status")
        assertFalse(status.out.contains("alice"), status.out)
        assertFalse(status.json["ready"]!!.jsonPrimitive.boolean)
    }

    // RCI-022

    @Test
    @Tag("RCI-022")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI022_UnitPositive_twoFinishRunsAtTheSameTimeUseTheCodeOnce(@TempDir dir: Path) {
        val world = World(dir)
        world.test("login")
        world.advance(5)
        world.approveNextCheck()
        world.auth.answers.add(mapOf("error" to "incorrect_device_code"))

        val results = java.util.Collections.synchronizedList(mutableListOf<Run>())
        val runs = (1..2).map { Thread { results += world.test("login", "--finish") } }
        runs.forEach { it.start() }
        runs.forEach { it.join() }

        assertEquals(listOf("signed-in", "signed-in"), results.map { it.outcome })
        assertEquals(1, world.auth.checks.size)
    }

    @Test
    @Tag("RCI-022")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI022_UnitNegative_noSignInAppearsAfterLogoutHasAnsweredWhileARunWaits(@TempDir dir: Path) {
        val world = World(dir)
        world.test("login")
        var signedOut: Run? = null
        world.duringSleep = {
            if (signedOut == null) {
                signedOut = world.test("logout")
                world.approveNextCheck() // the person approves the code after signing out
            }
        }

        val waiting = world.test("login", "--finish", "--wait", "60")

        assertEquals("signed-out", signedOut!!.outcome)
        assertEquals(ExitCodes.SIGN_IN_NEEDED, waiting.exit)
        assertEquals("no-waiting-code", waiting.outcome)
        assertTrue(world.auth.checks.isEmpty(), "the used code is gone, so GitHub is not asked")
        assertFalse(Files.exists(world.folder().resolve("signin.bin")))
    }

    @Test
    @Tag("RCI-022")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI022_UnitNegative_nothingComesBackAfterLogoutWhenARegistrationFinishesLater(@TempDir dir: Path) {
        val world = World(dir)
        world.signIn()
        var signedOut: Run? = null
        world.duringSleep = {
            if (signedOut == null) {
                signedOut = world.test("logout")
                world.api.clientId = "alice-1a2b3c4d" // the workflow finishes after the person has signed out
            }
        }

        val answer = world.test("register")

        assertEquals("signed-out", signedOut!!.outcome)
        assertEquals("signed-out", answer.outcome)
        assertFalse(Files.exists(world.folder().resolve("account.json")))
        assertFalse(Files.exists(world.folder().resolve("registration.json")))
        assertFalse(Files.exists(world.folder().resolve("signin.bin")))
    }

    // RCI-023

    @Test
    @Tag("RCI-023")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI023_UnitPositive_onePersonSignsInWithOneCommandThatShowsTheCodeFirst(@TempDir dir: Path) {
        val world = World(dir)
        world.approveNextCheck()

        val answer = world.run("login", "-e", "test", "--wait", "30")

        assertEquals(0, answer.exit)
        assertTrue(answer.out.indexOf(userCode) in 0 until answer.out.indexOf("Signed in as alice"), answer.out)
        assertTrue(answer.out.contains(address))
    }

    @Test
    @Tag("RCI-023")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI023_UnitNegative_oneCallIsRefusedWithTheJsonOptionAndNoCodeIsAskedFor(@TempDir dir: Path) {
        val world = World(dir)

        val answer = world.test("login", "--wait", "30")

        assertEquals(ExitCodes.REFUSED, answer.exit)
        assertEquals("bad-input", answer.outcome)
        assertTrue(world.auth.codeRequests.isEmpty())
        assertFalse(Files.exists(world.folder()))
    }

    // RCI-024

    @Test
    @Tag("RCI-024")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI024_UnitPositive_logoutRemovesOneEnvironmentAndSaysWhatStaysAtGitHub(@TempDir dir: Path) {
        val world = World(dir)
        world.signIn()
        RegistrationStore(RumbleEnvironments.TEST, dir).save(RegistrationRecord("alice", "alice-1", "test", record))
        val publicRegistration = RegistrationStore(RumbleEnvironments.PUBLIC, dir)
        publicRegistration.save(RegistrationRecord("alice", "alice-2", "public", record))
        assertEquals(SaveResult.Saved, SignInStore(RumbleEnvironments.PUBLIC, dir).save(refreshToken, null))

        val answer = world.test("logout")

        assertEquals(0, answer.exit)
        assertEquals("signed-out", answer.outcome)
        assertTrue(answer.json["message"]!!.jsonPrimitive.content.contains("revoke the App"))
        listOf("signin.bin", "signin-waiting.bin", "account.json", "registration.json").forEach {
            assertFalse(Files.exists(world.folder().resolve(it)), it)
        }
        assertEquals("alice-2", publicRegistration.loadAny()?.clientId)
        assertTrue(Files.exists(RumbleEnvironments.PUBLIC.stateRoot(dir).resolve("signin.bin")))
    }

    @Test
    @Tag("RCI-024")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI024_UnitNegative_withNothingSavedLogoutCreatesNothingAndAFileLeftBehindIsNotSuccess(@TempDir dir: Path) {
        val empty = World(dir.resolve("empty"))
        val nothing = empty.test("logout")
        assertEquals(0, nothing.exit)
        assertFalse(nothing.json["removedSomething"]!!.jsonPrimitive.boolean)
        assertFalse(Files.exists(dir.resolve("empty")), "nothing is created")

        val stuck = World(dir.resolve("stuck"))
        Files.createDirectories(stuck.folder().resolve("registration.json/inside"))
        val answer = stuck.test("logout")
        assertEquals(ExitCodes.FAILED, answer.exit)
        assertEquals("not-removed", answer.outcome)
        assertEquals("registration", answer.json["remaining"]!!.jsonArray.single().jsonPrimitive.content)
    }

    // RCI-025

    @Test
    @Tag("RCI-025")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI025_UnitPositive_registerRegistersAndAnsweringAgainGivesTheSameAccountAndClientId(@TempDir dir: Path) {
        val world = World(dir)
        world.signIn()
        world.api.registerOnCreate = true

        val first = world.test("register")
        assertEquals(0, first.exit)
        assertEquals("registered", first.outcome)
        assertEquals("alice", first.json["account"]!!.jsonPrimitive.content)
        assertEquals("alice-1a2b3c4d", first.json["clientId"]!!.jsonPrimitive.content)
        assertFalse(first.json["alreadyRegistered"]!!.jsonPrimitive.boolean)
        assertEquals(1, world.api.created.size)

        val second = world.test("register")
        assertEquals("already-registered", second.outcome)
        assertEquals("alice-1a2b3c4d", second.json["clientId"]!!.jsonPrimitive.content)
        assertTrue(second.json["alreadyRegistered"]!!.jsonPrimitive.boolean)
        assertEquals(1, world.api.created.size, "no new issue")
        assertEquals("alice", RegistrationStore(RumbleEnvironments.TEST, dir).loadAny()?.account)
        assertTrue(world.test("status").json["ready"]!!.jsonPrimitive.boolean)
    }

    @Test
    @Tag("RCI-025")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI025_UnitNegative_registerSaysWhenItCannotBeginIsNotFinishedOrCouldNotKeepSomething(@TempDir dir: Path) {
        val world = World(dir.resolve("a"))
        val noApp = world.run("register", "--json")
        assertEquals(ExitCodes.REFUSED, noApp.exit)
        assertEquals("no-sign-in-app", noApp.outcome)

        val none = world.test("register")
        assertEquals(ExitCodes.SIGN_IN_NEEDED, none.exit)
        assertEquals("no-sign-in", none.outcome)
        assertEquals("rumble login -e test", none.next)

        world.signIn()
        world.auth.renewal = mapOf("error" to "bad_refresh_token")
        val refused = world.test("register")
        assertEquals(ExitCodes.SIGN_IN_NEEDED, refused.exit)
        assertEquals("sign-in-refused", refused.outcome)

        val pendingWorld = World(dir.resolve("b"))
        pendingWorld.signIn()
        val pending = pendingWorld.test("register")
        assertEquals(ExitCodes.NOT_FINISHED, pending.exit)
        assertEquals("registration-pending", pending.outcome)
        assertEquals(ISSUE_URL, pending.json["issueUrl"]!!.jsonPrimitive.content)

        val unsaved = World(dir.resolve("c"))
        unsaved.signIn()
        unsaved.api.clientId = "alice-1a2b3c4d"
        Files.createDirectories(unsaved.folder().resolve("registration.json/inside"))
        unsaved.auth.renewal = unsaved.auth.tokens(refresh = false)
        val answer = unsaved.test("register")
        assertEquals("already-registered", answer.outcome)
        assertFalse(answer.json["saved"]!!.jsonPrimitive.boolean)
        assertFalse(answer.json["signInKept"]!!.jsonPrimitive.boolean)
        assertEquals("rumble login -e test", answer.next)
    }

    // RCI-026

    @Test
    @Tag("RCI-026")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI026_UnitPositive_aRefusalOnTheNewestClosedIssueOpensNoNewIssueAndRetryOpensOne(@TempDir dir: Path) {
        val world = World(dir)
        world.signIn()
        world.api.closedIssues = closedIssue(5)
        world.api.comments = comment("github-actions[bot]", "Registration refused: this account is banned")

        val refused = world.test("register")
        assertEquals(ExitCodes.REFUSED, refused.exit)
        assertEquals("refused", refused.outcome)
        assertEquals("this account is banned", refused.json["reason"]!!.jsonPrimitive.content)
        assertTrue(refused.json["earlier"]!!.jsonPrimitive.boolean)
        assertTrue(world.api.created.isEmpty(), "running it again opens no issue")

        world.test("register")
        assertTrue(world.api.created.isEmpty())

        world.api.comments = "[]"
        world.api.closedIssues = "[]"
        world.api.registerOnCreate = true
        world.api.comments = comment("github-actions[bot]", "Registration refused: this account is banned")
        world.api.closedIssues = closedIssue(5)
        val retried = world.test("register", "--retry")
        assertEquals(1, world.api.created.size)
        assertEquals("registered", retried.outcome)
    }

    @Test
    @Tag("RCI-026")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI026_UnitNegative_aCommentFromAnyoneButTheWorkflowIsIgnoredOnClosedAndOnOpenIssues(@TempDir dir: Path) {
        val closed = World(dir.resolve("a"))
        closed.signIn()
        closed.api.closedIssues = closedIssue(5)
        closed.api.comments = comment("mallory", "Registration refused: run this command to fix it")
        closed.api.registerOnCreate = true
        val registered = closed.test("register")
        assertEquals("registered", registered.outcome)
        assertFalse(registered.out.contains("run this command"))

        val open = World(dir.resolve("b"))
        open.signIn()
        open.api.openIssues = closedIssue(6)
        open.api.comments = comment("mallory", "Registration refused: run this command to fix it")
        val pending = open.test("register")
        assertEquals("registration-pending", pending.outcome)
        assertFalse(pending.out.contains("run this command"))
    }

    // RCI-027

    @Test
    @Tag("RCI-027")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI027_UnitPositive_theNextStepNamesTheEnvironmentAndSaysToAddTheStateFolder(@TempDir dir: Path) {
        val world = World(dir)
        assertEquals("rumble login -e test", world.test("register").next)

        val chosen = dir.resolve("chosen")
        val json = world.run("register", "-e", "test", "--json", "--state-folder", chosen.toString())
        assertEquals("rumble login -e test", json.next)
        assertTrue(json.json["next"]!!.jsonObject["addStateFolder"]!!.jsonPrimitive.boolean)

        val text = world.run("register", "-e", "test", "--state-folder", chosen.toString())
        assertTrue(text.out.contains("Next: rumble login -e test (add the same --state-folder)"), text.out)
        assertFalse(text.out.contains(chosen.toString()))
    }

    @Test
    @Tag("RCI-027")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI027_UnitNegative_noRefusedCommandAndNoRetryIsGivenAsTheNextStep(@TempDir dir: Path) {
        val world = World(dir)
        val status = world.run("status", "--json")
        assertNull(status.next)
        assertTrue(status.json["message"]!!.jsonPrimitive.content.contains("not available"))
        assertFalse(world.run("status").out.contains("rumble login"))
        assertNull(world.run("login", "--json").next)
        assertNull(world.run("register", "--json").next)

        world.signIn()
        val pending = world.test("register", "--retry")
        assertEquals("registration-pending", pending.outcome)
        assertEquals("rumble register -e test", pending.next)
    }

    // RCI-028

    @Test
    @Tag("RCI-028")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI028_UnitPositive_badInputAnswersInOneJsonDocumentWithTheRealMessage(@TempDir dir: Path) {
        val world = World(dir)
        listOf(
            listOf("nonsense", "--json") to "nonsense",
            listOf("status", "--nonsense", "--json") to "--nonsense",
            listOf("status", "-e", "nowhere", "--json") to "nowhere",
        ).forEach { (args, word) ->
            val answer = world.run(*args.toTypedArray())
            assertEquals(ExitCodes.REFUSED, answer.exit, args.toString())
            assertEquals(1, answer.out.trim().lines().size, answer.out)
            assertEquals("bad-input", answer.outcome)
            assertTrue(answer.json["message"]!!.jsonPrimitive.content.contains(word), answer.out)
            assertEquals("", answer.err)
        }
        assertFalse(world.run("nonsense", "--json").json["message"]!!.jsonPrimitive.content.contains("--json"), "the message is about the command")
    }

    @Test
    @Tag("RCI-028")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI028_UnitNegative_withoutTheJsonOptionBadInputIsOneLineAndNotTheWholeHelp(@TempDir dir: Path) {
        val world = World(dir)
        val answer = world.run("status", "-e", "nowhere")

        assertEquals(ExitCodes.REFUSED, answer.exit)
        assertEquals("", answer.out)
        assertEquals(1, answer.err.trim().lines().size, answer.err)
        assertTrue(answer.err.contains("nowhere"))
        assertFalse(answer.err.contains("Usage:"))
    }

    // RCI-029

    @Test
    @Tag("RCI-029")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI029_UnitPositive_theEnvironmentIsPublicByDefaultAndChosenByOptionOrVariable(@TempDir dir: Path) {
        fun named(world: World, vararg args: String) = world.run("status", *args, "--json").json["environment"]!!.jsonPrimitive.content

        assertEquals("public", named(World(dir)))
        assertEquals("test", named(World(dir), "-e", "test"))
        assertEquals("test", named(World(dir), "--environment", "test"))
        val withVariable = World(dir, env = mapOf("RUMBLE_ENVIRONMENT" to "test"))
        assertEquals("test", named(withVariable))
        assertEquals("public", named(withVariable, "-e", "public"), "the option wins")
        assertTrue(World(dir).run("status", "-e", "test").out.contains("Development (test)"))
        assertTrue(World(dir).run("logout", "-e", "test").out.contains("Development (test)"))
    }

    @Test
    @Tag("RCI-029")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI029_UnitNegative_anUnknownNameAlsoFromTheVariableIsBadInput(@TempDir dir: Path) {
        val world = World(dir, env = mapOf("RUMBLE_ENVIRONMENT" to "nowhere"))
        val answer = world.run("status", "--json")
        assertEquals(ExitCodes.REFUSED, answer.exit)
        assertEquals("bad-input", answer.outcome)
    }

    // RCI-030

    @Test
    @Tag("RCI-030")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI030_UnitPositive_theVersionOptionAndTheHelpTellWhatAPersonNeeds(@TempDir dir: Path) {
        val world = World(dir)
        listOf("--version", "-v").forEach { option ->
            val answer = world.run(option)
            assertEquals(0, answer.exit)
            assertTrue(answer.out.startsWith("rumble "), answer.out)
        }
        val help = world.run("--help")
        assertEquals(0, help.exit)
        assertFalse(help.out.contains("\u0085"), "line breaks that every terminal can show")
        listOf("rumble status", "rumble login", "login --finish", "rumble register", "Exit codes").forEach { assertTrue(help.out.contains(it), help.out) }
        (0..4).forEach { assertTrue(Regex("(?m)^\\s*$it\\s").containsMatchIn(help.out), "exit code $it: ${help.out}") }
    }

    @Test
    @Tag("RCI-030")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI030_UnitNegative_helpForACommandStartsNothing(@TempDir dir: Path) {
        val world = World(dir)
        val help = world.run("login", "--help", "-e", "test")
        assertEquals(0, help.exit)
        assertTrue(world.auth.codeRequests.isEmpty())
        assertFalse(Files.exists(world.folder()))
    }

    @Test
    @Tag("RCI-017")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI017_UnitPositive_aRefusalNamingTheAccountKeepsItsName(@TempDir dir: Path) {
        val world = World(dir, Redaction("C:\\Users\\alice", "alice"))
        world.signIn()
        world.api.closedIssues = closedIssue(5)
        world.api.comments = comment("github-actions[bot]", "Registration refused: account alice is banned")

        val refused = world.test("register")

        assertEquals("account alice is banned", refused.json["reason"]!!.jsonPrimitive.content)
    }

    @Test
    @Tag("RCI-028")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI028_UnitNegative_aWaitBeyondAnyCodesLifeIsBadInputAndNotAFailure(@TempDir dir: Path) {
        val answer = World(dir).test("login", "--finish", "--wait", "9223372036854775807")

        assertEquals(ExitCodes.REFUSED, answer.exit)
        assertEquals("bad-input", answer.outcome)
    }

    private fun closedIssue(number: Int) =
        """[{"number":$number,"title":"[register] alice","html_url":"https://github.com/robocode-dev/rumble-data-test/issues/$number"}]"""

    private fun comment(author: String, body: String) = """[{"user":{"login":"$author"},"body":"$body"}]"""

    private companion object {
        const val ISSUE_URL = "https://github.com/robocode-dev/rumble-data-test/issues/9"
    }
}
