package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class RumbleSavedSignInTest {

    private val test = RumbleEnvironments.TEST
    private val alice = SignInIdentity("alice", "/home/alice", "Linux")

    /** GitHub as it behaves: every refresh token works once and is replaced by a new one at each renewal. */
    private class SingleUseGitHub(first: String) : GitHubAuthTransport {
        private var valid = first
        private var counter = 0
        val renewals = AtomicInteger()
        val refusals = AtomicInteger()

        @Synchronized
        override fun postForm(url: String, form: Map<String, String>): AuthResponse {
            if (form["refresh_token"] != valid) {
                refusals.incrementAndGet()
                return answer("error" to "bad_refresh_token")
            }
            renewals.incrementAndGet()
            counter++
            valid = "refresh-$counter"
            return tokenAnswer(access = "access-$counter", refresh = valid)
        }

        @Synchronized
        fun revokeEverything() {
            valid = "revoked"
        }
    }

    private fun program(base: Path, transport: GitHubAuthTransport) =
        SavedSignIn(SignInStore(test, base, alice), GitHubSignIn(test, transport, MutableClock(START)))

    private fun firstSignIn(base: Path, transport: GitHubAuthTransport): SavedSignIn {
        val program = program(base, transport)
        assertEquals(SaveResult.Saved, program.remember(GitHubTokens("access-0", "refresh-0", null, null)))
        return program
    }

    @Test
    @Tag("RCL-035")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL035_UnitPositive_twoProgramsRenewOneAfterTheOtherAndBothStaySignedIn(@TempDir base: Path) {
        val github = SingleUseGitHub("refresh-0")
        val a = firstSignIn(base, github)
        val b = program(base, github)

        val renewals = listOf(a.renew(), b.renew(), a.renew(), b.renew(), b.renew(), a.renew())

        renewals.forEach { assertInstanceOf(SavedRenewal.Renewed::class.java, it) }
        assertEquals(6, github.renewals.get())
        assertEquals(0, github.refusals.get())
        assertEquals("refresh-6", (SignInStore(test, base, alice).load() as LoadResult.Found).refreshToken)
    }

    @Test
    @Tag("RCL-035")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL035_UnitPositive_programsRenewingAtTheSameTimeTakeTurnsAndNoneIsRefused(@TempDir base: Path) {
        val github = SingleUseGitHub("refresh-0")
        val programs = listOf(firstSignIn(base, github), program(base, github), program(base, github))
        val pool = Executors.newFixedThreadPool(programs.size)
        val start = CountDownLatch(1)
        val failures = AtomicInteger()

        val done = programs.map { program ->
            pool.submit {
                start.await()
                repeat(15) { if (program.renew() !is SavedRenewal.Renewed) failures.incrementAndGet() }
            }
        }
        start.countDown()
        done.forEach { it.get(60, TimeUnit.SECONDS) }
        pool.shutdown()

        assertEquals(0, failures.get())
        assertEquals(0, github.refusals.get())
        assertEquals(45, github.renewals.get())
    }

    @Test
    @Tag("RCL-035")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL035_UnitNegative_anOutdatedRefreshTokenIsRefusedByGitHubWhichIsWhyTheFileIsReadAgain(@TempDir base: Path) {
        val github = SingleUseGitHub("refresh-0")
        val a = firstSignIn(base, github)
        val b = program(base, github)
        a.renew()
        b.renew()

        // A program that kept the refresh token it saw earlier, instead of reading the file, is turned away.
        val stale = GitHubSignIn(test, github, MutableClock(START)).refresh(GitHubTokens("", "refresh-0", null, null))

        assertInstanceOf(RefreshResult.Refused::class.java, stale)
        assertInstanceOf(SavedRenewal.Renewed::class.java, a.renew())
    }

    @Test
    @Tag("RCL-035")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL035_UnitNegative_aRefusalRemovesTheSavedSignInAndAsksForANewOne(@TempDir base: Path) {
        val github = SingleUseGitHub("refresh-0")
        val a = firstSignIn(base, github)
        github.revokeEverything()

        val result = a.renew()

        assertTrue((result as SavedRenewal.SignInNeeded).message.contains("Sign in again"))
        assertEquals(LoadResult.Missing, SignInStore(test, base, alice).load())
        assertTrue(a.renew() is SavedRenewal.SignInNeeded)
    }

    @Test
    @Tag("RCL-035")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL035_UnitNegative_noSavedOrAnUnusableSignInNeverContactsGitHub(@TempDir base: Path) {
        val transport = FakeTransport()
        val none = program(base, transport)

        assertTrue(none.renew() is SavedRenewal.SignInNeeded)
        SignInStore(test, base, alice).save("ghr_X", null)
        val other = SavedSignIn(SignInStore(test, base, SignInIdentity("bob", "/home/bob", "Linux")), GitHubSignIn(test, transport))
        assertTrue(other.renew() is SavedRenewal.SignInNeeded)
        assertEquals(0, transport.requests.size)
    }

    @Test
    @Tag("RCL-035")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL035_UnitNegative_aFailedRenewalKeepsTheSavedSignInForANewTry(@TempDir base: Path) {
        val a = program(base, FakeTransport(java.io.IOException("offline")))
        a.remember(GitHubTokens("access-0", "refresh-0", null, null))

        assertTrue(a.renew() is SavedRenewal.Failed)
        assertEquals("refresh-0", (SignInStore(test, base, alice).load() as LoadResult.Found).refreshToken)
    }
}
