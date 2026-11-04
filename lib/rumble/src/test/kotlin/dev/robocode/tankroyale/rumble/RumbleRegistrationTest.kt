package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertNotEquals
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

    private class Run(val registration: Registration, val clock: MutableClock, val sleeps: MutableList<Long>, val store: InMemoryRegistrations)

    private val memories = HashMap<Path, InMemoryRegistrations>()

    private fun memory(base: Path) = memories.getOrPut(base) { InMemoryRegistrations() }

    private fun run(github: FakeRumbleGitHub, base: Path): Run {
        val clock = MutableClock(START)
        val sleeps = mutableListOf<Long>()
        val store = memory(base)
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
        assertEquals(null, memory(base).load("alice"))
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
        assertEquals(RegistrationResult.Refused("account `alice` is banned", account = "alice"), refused)
        assertEquals(null, memory(base).load("alice"))

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

    // RCL-042

    private val alice = SignInIdentity("alice", "/home/alice", "Linux")

    private fun signedIn(base: Path, environment: RumbleEnvironment = env): Pair<SignInStore, String> {
        val store = SignInStore(environment, base, alice)
        assertEquals(SaveResult.Saved, store.beginSignIn("ghr_X", null))
        return store to (store.load() as LoadResult.Found).signInId
    }

    @Test
    @Tag("RCL-042")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL042_UnitPositive_theRegistrationIsSavedInTheSignInsOneFileAndARenewalKeepsIt(@TempDir base: Path) {
        val (store, id) = signedIn(base)

        assertTrue(store.registrationFor(id).save(RegistrationRecord("Alice", "alice-1a2b3c4d", "test", START)))
        assertEquals(SaveResult.Saved, store.save("ghr_RENEWED", null))

        val loaded = SignInStore(env, base, alice).load() as LoadResult.Found
        assertEquals("ghr_RENEWED", loaded.refreshToken)
        assertEquals(id, loaded.signInId)
        assertEquals("Alice", loaded.account)
        assertEquals("alice-1a2b3c4d", loaded.registration!!.clientId)
        assertEquals("test", loaded.registration!!.environment)
        assertEquals(START, loaded.registration!!.savedAt)
        assertEquals("alice-1a2b3c4d", store.registrationFor(id).load("alice")!!.clientId)
        assertEquals(listOf("signin.bin", "signin.lock"), env.stateRoot(base).toFile().list()!!.sorted())
    }

    @Test
    @Tag("RCL-042")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL042_UnitNegative_aRegistrationIsIgnoredForAnotherAccountAndEnvironmentAndAlwaysFollowsItsSignIn(@TempDir base: Path) {
        val (store, id) = signedIn(base)
        val memory = store.registrationFor(id)
        memory.save(RegistrationRecord("alice", "alice-1", "test", START))

        assertEquals(null, memory.load("bob"))

        val publicFile = RumbleEnvironments.PUBLIC.stateRoot(base).resolve("signin.bin")
        java.nio.file.Files.createDirectories(publicFile.parent)
        java.nio.file.Files.copy(env.stateRoot(base).resolve("signin.bin"), publicFile)
        assertInstanceOf(LoadResult.Unusable::class.java, SignInStore(RumbleEnvironments.PUBLIC, base, alice).load())

        assertEquals(SaveResult.Saved, store.beginSignIn("ghr_NEW", null))
        val fresh = store.load() as LoadResult.Found
        assertNotEquals(id, fresh.signInId)
        assertEquals(null, fresh.registration)
        assertEquals(null, fresh.account)
        assertFalse(memory.save(RegistrationRecord("alice", "alice-1", "test", START)), "too late for a later sign-in")
        assertFalse(store.saveAccount(id, "alice"), "too late for a later sign-in")
        assertEquals(null, (store.load() as LoadResult.Found).account)

        val freshMemory = store.registrationFor(fresh.signInId)
        freshMemory.save(RegistrationRecord("alice", "alice-1", "test", START))
        SavedSignIn(store, GitHubSignIn(env, FakeTransport(), MutableClock(START))).forget()
        assertEquals(LoadResult.Missing, store.load())
        assertEquals(null, freshMemory.load("alice"))
        assertFalse(freshMemory.save(RegistrationRecord("alice", "alice-1", "test", START)), "too late after signing out")
    }

    @Test
    @Tag("RCL-042")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL042_UnitNegative_aNameThatChangedOnGitHubLeavesTheRegistrationBehind(@TempDir base: Path) {
        val (store, id) = signedIn(base)
        store.registrationFor(id).save(RegistrationRecord("alice", "alice-1", "test", START))

        assertTrue(store.saveAccount(id, "ALICE"))
        assertEquals("alice-1", (store.load() as LoadResult.Found).registration!!.clientId, "the same name in other letters")

        assertTrue(store.saveAccount(id, "alice-renamed"))
        val loaded = store.load() as LoadResult.Found
        assertEquals("alice-renamed", loaded.account)
        assertEquals(null, loaded.registration)
        assertEquals(null, store.registrationFor(id).load("alice-renamed"))
    }

    @Test
    @Tag("RCL-042")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL042_UnitNegative_whatCannotBeSavedAnswersFalseAndThrowsNothing(@TempDir base: Path) {
        val notAFolder = java.nio.file.Files.writeString(base.resolve("a-file"), "not a folder")
        val store = SignInStore(env, notAFolder, alice)

        assertFalse(store.saveAccount("id", "alice"))
        assertFalse(store.registrationFor("id").save(RegistrationRecord("alice", "alice-1", "test", START)))
    }

    @Test
    @Tag("RCL-041")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL041_UnitNegative_aFileThatCannotBeReadJustNowIsLeftAsItIs(@TempDir base: Path) {
        val trusting = object : FileProtection {
            override fun createOwnerOnly(file: Path) {
                java.nio.file.Files.createFile(file)
            }

            override fun isOwnerOnly(file: Path) = true
        }
        val store = SignInStore(env, base, alice, trusting)
        val file = env.stateRoot(base).resolve("signin.bin")
        java.nio.file.Files.createDirectories(file.resolve("cannot-be-read-as-a-file"))

        assertInstanceOf(SaveResult.NotSaved::class.java, store.save("ghr_NEW", null))
        assertTrue((store.saveWaiting(WaitingSignIn("A-B", "https://github.com/login/device", START, 5, START, "secret")) as SaveResult.NotSaved).reason.contains("left as it is"))
        assertTrue(java.nio.file.Files.isDirectory(file.resolve("cannot-be-read-as-a-file")))
    }

    @Test
    @Tag("RCI-026")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCI026_UnitNegative_anAnswerThatIsNoListOpensNoNewIssue(@TempDir base: Path) {
        val comments = FakeRumbleGitHub().apply {
            closedIssues = mutableListOf(FakeRumbleGitHub.issueJson(5, "[register] alice"))
            this.comments = "not json"
        }
        assertInstanceOf(RegistrationResult.Failed::class.java, run(comments, base).registration.register(tokens()))
        assertTrue(comments.postedBodies.isEmpty())
    }
}
