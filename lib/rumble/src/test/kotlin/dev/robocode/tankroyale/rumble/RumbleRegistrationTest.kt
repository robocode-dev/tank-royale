package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class RumbleRegistrationTest {

    private val env = RumbleEnvironments.TEST
    private val dataRepo = "/repos/robocode-dev/rumble-data-test"

    private class Run(val registration: Registration, val clock: MutableClock, val sleeps: MutableList<Long>, val store: RegistrationStore)

    private fun run(github: FakeRumbleGitHub, base: Path): Run {
        val clock = MutableClock(START)
        val sleeps = mutableListOf<Long>()
        val store = RegistrationStore(env, base)
        val registration = Registration(env, github, store, clock, sleep = { millis -> sleeps += millis; clock.advanceMillis(millis) })
        return Run(registration, clock, sleeps, store)
    }

    private fun methodsAndPaths(github: FakeRumbleGitHub) = github.requests.map { it.first + " " + it.second }

    // RCL-036

    @Test
    @Tag("RCL-036")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL036_UnitPositive_anAlreadyRegisteredAccountGetsItsClientIdWithoutAnIssue(@TempDir base: Path) {
        val github = FakeRumbleGitHub().apply { registeredClientId = "alice-0a1b2c3d" }
        val first = run(github, base)

        val result = first.registration.register(tokens())

        assertEquals(RegistrationResult.Registered("alice", "alice-0a1b2c3d", alreadyRegistered = true), result)
        assertEquals(listOf("GET /user", "GET $dataRepo/contents/clients/alice.json"), methodsAndPaths(github))
        assertEquals("alice-0a1b2c3d", first.store.load("alice")!!.clientId)

        val second = FakeRumbleGitHub()
        val cached = run(second, base).registration.register(tokens())
        assertEquals(RegistrationResult.Registered("alice", "alice-0a1b2c3d", alreadyRegistered = true), cached)
        assertEquals(listOf("GET /user"), methodsAndPaths(second))
    }

    @Test
    @Tag("RCL-036")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL036_UnitNegative_aRegistrationFileForAnotherAccountOrWithoutAClientIdIsNotTrusted(@TempDir base: Path) {
        val other = FakeRumbleGitHub().apply { contentsBody = FakeRumbleGitHub.fileJson("mallory", "mallory-1") }
        assertTrue(run(other, base).registration.register(tokens()) is RegistrationResult.Failed)

        val empty = FakeRumbleGitHub().apply {
            contentsBody = """{"encoding":"base64","content":"${java.util.Base64.getEncoder().encodeToString("""{"account":"alice","clientIds":[]}""".toByteArray())}"}"""
        }
        assertTrue(run(empty, base).registration.register(tokens()) is RegistrationResult.Failed)

        val garbage = FakeRumbleGitHub().apply { contentsBody = """{"content":"%%%not base64%%%"}""" }
        assertTrue(run(garbage, base).registration.register(tokens()) is RegistrationResult.Failed)
        assertEquals(null, RegistrationStore(env, base).load("alice"))
    }

    // RCL-037

    @Test
    @Tag("RCL-037")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL037_UnitPositive_aNewAccountOpensOneIssueAndWaitsForTheRegistrationFile(@TempDir base: Path) {
        val github = FakeRumbleGitHub().apply { workflowRegistersAfterFileChecks = 4 }
        val run = run(github, base)

        val result = run.registration.register(tokens())

        assertEquals(RegistrationResult.Registered("alice", "alice-1a2b3c4d", alreadyRegistered = false), result)
        assertEquals(1, github.requests.count { it.first == "POST" })
        assertTrue(github.postedBodies.single().contains("\"title\":\"[register] alice\""))
        assertEquals(listOf(5000L, 5000L), run.sleeps)
        assertEquals("alice-1a2b3c4d", run.store.load("alice")!!.clientId)
    }

    @Test
    @Tag("RCL-037")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL037_UnitPositive_anOpenRegistrationIssueFromTheSameAccountIsReusedInsteadOfOpeningAnother(@TempDir base: Path) {
        val github = FakeRumbleGitHub().apply {
            openIssues += FakeRumbleGitHub.issueJson(5, "[register] alice")
            workflowRegistersAfterFileChecks = 3
        }

        val result = run(github, base).registration.register(tokens())

        assertEquals(false, (result as RegistrationResult.Registered).alreadyRegistered)
        assertEquals(0, github.requests.count { it.first == "POST" })
        assertTrue(github.requests.any { it.second.contains("/issues/5/comments") })
    }

    @Test
    @Tag("RCL-037")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL037_UnitNegative_otherIssuesAndPullRequestsDoNotCountAsARegistrationIssue(@TempDir base: Path) {
        val github = FakeRumbleGitHub().apply {
            openIssues += FakeRumbleGitHub.issueJson(3, "Question about ranking")
            openIssues += FakeRumbleGitHub.issueJson(4, "[register] alice", pullRequest = true)
            workflowRegistersAfterFileChecks = 2
        }

        val result = run(github, base).registration.register(tokens())

        assertInstanceOf(RegistrationResult.Registered::class.java, result)
        assertEquals(1, github.requests.count { it.first == "POST" })
    }

    // RCL-038

    @Test
    @Tag("RCL-038")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL038_UnitPositive_aWaitThatEndsWithoutRegistrationIsNotFinishedAndALaterTryContinuesWithoutAnotherIssue(@TempDir base: Path) {
        val github = FakeRumbleGitHub()
        val first = run(github, base)

        val result = first.registration.register(tokens())

        assertEquals(RegistrationResult.NotFinished("https://github.com/robocode-dev/rumble-data-test/issues/7"), result)
        assertEquals(24, first.sleeps.size)
        assertEquals(1, github.requests.count { it.first == "POST" })

        github.openIssues += FakeRumbleGitHub.issueJson(7, "[register] alice")
        github.workflowRegistersAfterFileChecks = 1
        val later = run(github, base).registration.register(tokens())

        // The workflow finished after the first attempt gave up, so the file is there and nothing new is opened.
        assertEquals(true, (later as RegistrationResult.Registered).alreadyRegistered)
        assertEquals(1, github.requests.count { it.first == "POST" })
    }

    @Test
    @Tag("RCL-038")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL038_UnitNegative_aRefusalReturnsTheWorkflowsReasonInOneShortLine(@TempDir base: Path) {
        val github = FakeRumbleGitHub().apply {
            comments = """[{"user":{"login":"github-actions[bot]"},"body":"Registration refused: account `alice` is banned\nsecond line"}]"""
        }
        val refused = run(github, base).registration.register(tokens())
        assertEquals(RegistrationResult.Refused("account `alice` is banned"), refused)
        assertEquals(null, RegistrationStore(env, base).load("alice"))

        val long = FakeRumbleGitHub().apply {
            comments = """[{"user":{"login":"github-actions[bot]"},"body":"Registration refused: ${"x".repeat(500)}"}]"""
        }
        assertEquals(200, (run(long, base).registration.register(tokens()) as RegistrationResult.Refused).reason.length)

        val bare = FakeRumbleGitHub().apply { comments = """[{"user":{"login":"github-actions[bot]"},"body":"Registration refused:"}]""" }
        assertTrue((run(bare, base).registration.register(tokens()) as RegistrationResult.Refused).reason.isNotEmpty())
    }

    @Test
    @Tag("RCL-038")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL038_UnitNegative_aRejectedTokenMeansSignInAgainAndOtherProblemsAreFailuresInPlainWords(@TempDir base: Path) {
        val rejected = FakeRumbleGitHub().apply { tokenAccepted = false }
        assertTrue((run(rejected, base).registration.register(tokens()) as RegistrationResult.SignInNeeded).message.contains("Sign in again"))

        val offline = FakeRumbleGitHub().apply { offline = true }
        assertTrue((run(offline, base).registration.register(tokens()) as RegistrationResult.Failed).message.contains("cannot be reached"))

        val notInstalled = FakeRumbleGitHub().apply { createStatus = 404 }
        assertTrue((run(notInstalled, base).registration.register(tokens()) as RegistrationResult.Failed).message.contains("installed"))

        val limited = FakeRumbleGitHub().apply { contentsStatus = 403 }
        assertTrue((run(limited, base).registration.register(tokens()) as RegistrationResult.Failed).message.contains("limiting"))

        val broken = FakeRumbleGitHub().apply { userStatus = 500 }
        assertTrue((run(broken, base).registration.register(tokens()) as RegistrationResult.Failed).message.contains("HTTP 500"))
    }

    // RCL-039

    @Test
    @Tag("RCL-039")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL039_UnitPositive_theSavedRegistrationIsAPlainFileWithAccountClientIdEnvironmentAndTime(@TempDir base: Path) {
        val store = RegistrationStore(env, base)

        assertTrue(store.save(RegistrationRecord("Alice", "alice-1a2b3c4d", "test", START)))

        val loaded = store.load("alice")
        assertNotNull(loaded)
        assertEquals("alice-1a2b3c4d", loaded!!.clientId)
        assertEquals(START, loaded.savedAt)
        val text = java.nio.file.Files.readString(env.stateRoot(base).resolve("registration.json"))
        listOf("alice-1a2b3c4d", "\"environment\":\"test\"", "\"account\":\"Alice\"", START.toString()).forEach { assertTrue(text.contains(it), it) }
    }

    @Test
    @Tag("RCL-039")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL039_UnitNegative_aRegistrationForAnotherAccountEnvironmentOrADamagedFileIsIgnoredAndSigningOutRemovesIt(@TempDir base: Path) {
        val store = RegistrationStore(env, base)
        store.save(RegistrationRecord("alice", "alice-1", "test", START))
        val file = env.stateRoot(base).resolve("registration.json")

        assertEquals(null, store.load("bob"))

        val publicFolder = RumbleEnvironments.PUBLIC.stateRoot(base)
        java.nio.file.Files.createDirectories(publicFolder)
        java.nio.file.Files.copy(file, publicFolder.resolve("registration.json"))
        assertEquals(null, RegistrationStore(RumbleEnvironments.PUBLIC, base).load("alice"))

        java.nio.file.Files.writeString(file, "{ damaged")
        assertEquals(null, store.load("alice"))
        java.nio.file.Files.writeString(file, """{"schemaVersion":2,"account":"alice","clientId":"x","environment":"test","savedAt":"$START"}""")
        assertEquals(null, store.load("alice"))

        store.save(RegistrationRecord("alice", "alice-1", "test", START))
        store.forget()
        assertFalse(java.nio.file.Files.exists(file))
        assertEquals(null, store.load("alice"))
    }

    @Test
    @Tag("RCL-039")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL039_UnitNegative_signingOutRemovesTheSavedSignInAndTheSavedRegistration(@TempDir base: Path) {
        val registrationStore = RegistrationStore(env, base)
        val signInStore = SignInStore(env, base, SignInIdentity("alice", "/home/alice", "Linux"))
        val saved = SavedSignIn(signInStore, GitHubSignIn(env, FakeTransport(), MutableClock(START)), registrationStore)
        signInStore.save("ghr_X", null)
        registrationStore.save(RegistrationRecord("alice", "alice-1", "test", START))

        saved.forget()

        assertEquals(LoadResult.Missing, signInStore.load())
        assertEquals(null, registrationStore.load("alice"))
    }
}
