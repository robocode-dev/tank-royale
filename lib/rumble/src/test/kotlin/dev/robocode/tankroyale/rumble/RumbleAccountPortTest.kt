package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

/** The account screen's library, made of the real sign-in and registration against a fake GitHub. */
class RumbleAccountPortTest {

    private val env = RumbleEnvironments.TEST
    private val pending = answer("error" to "authorization_pending")

    private class World(dir: Path, vararg auth: Any) {
        val clock = MutableClock(START)
        val transport = FakeTransport(*auth)
        val github = FakeRumbleGitHub().apply { registeredClientId = "alice-1a2b3c4d" }
        val store = SignInStore(RumbleEnvironments.TEST, dir)
        val flow = SignInFlow(RumbleEnvironments.TEST, GitHubSignIn(RumbleEnvironments.TEST, transport, clock), store, github, clock, sleep = { clock.advanceMillis(it) })
        val port = LibraryAccountPort(RumbleEnvironments.TEST, flow, github, clock, sleep = { clock.advanceMillis(it) })
    }

    // RCL-054

    @Test
    @Tag("RCL-054")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL054_UnitPositive_theWholeSignInRegistrationAndSignOutRunThroughTheScreensLibrary(@TempDir dir: Path) {
        val world = World(dir, deviceCodeAnswer(), pending, tokenAnswer("access-1", "refresh-1"), tokenAnswer("access-2", "refresh-2"), tokenAnswer("access-3", "refresh-3"))
        val port = world.port

        assertEquals(AccountOpened.SignedOut(), port.open())

        val begun = port.begin() as AccountBegun.Code
        assertEquals("F94F-59CF", begun.code)
        assertEquals("https://github.com/login/device", begun.address)
        assertEquals(5, begun.retryAfterSeconds)

        assertInstanceOf(AccountChecked.Waiting::class.java, port.check(), "GitHub's pace is kept, so nothing is asked yet")
        world.clock.advanceMillis(6_000)
        assertInstanceOf(AccountChecked.Waiting::class.java, port.check())
        world.clock.advanceMillis(6_000)
        assertEquals(AccountChecked.SignedIn("alice"), port.check())

        assertEquals(AccountRegistration.Registered("alice-1a2b3c4d"), port.register())
        // What was saved is what the command line reads: the same file, the same account.
        val saved = SignInStore(env, dir).load() as LoadResult.Found
        assertEquals("alice", saved.account)
        assertEquals(AccountOpened.SignedIn("alice"), World(dir, tokenAnswer("access-4", "refresh-4")).port.open().let { it })

        assertTrue(port.signOut())
        assertEquals(AccountOpened.SignedOut(), World(dir).port.open())
    }

    @Test
    @Tag("RCL-054")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL054_UnitNegative_eachFailureIsThePlainMessageOfTheLibraryAndCancellingForgetsTheCodeOnly(@TempDir dir: Path) {
        val none = LibraryAccountPort(RumbleEnvironments.PUBLIC, SignInFlow(RumbleEnvironments.PUBLIC, GitHubSignIn(RumbleEnvironments.PUBLIC, FakeTransport()), SignInStore(RumbleEnvironments.PUBLIC, dir), FakeRumbleGitHub()), FakeRumbleGitHub())
        assertEquals(AccountOpened.Unavailable(RumbleEnvironments.PUBLIC.signInNotAvailable()), none.open())
        assertEquals(AccountBegun.Unavailable(RumbleEnvironments.PUBLIC.signInNotAvailable()), none.begin())

        val world = World(dir, deviceCodeAnswer(), deviceCodeAnswer(deviceCode = "device-code-2", userCode = "AAAA-BBBB"), answer("error" to "expired_token"))
        assertEquals("F94F-59CF", (world.port.begin() as AccountBegun.Code).code)
        world.port.cancel()
        assertEquals(AccountChecked.NoWaitingCode, world.port.check(), "the cancelled code is forgotten")
        assertEquals("AAAA-BBBB", (world.port.begin() as AccountBegun.Code).code, "a new code is asked for")

        world.clock.advanceMillis(6_000)
        assertEquals(AccountChecked.Expired, world.port.check())

        val offline = World(dir.resolve("other"), deviceCodeAnswer())
        offline.github.offline = true
        val registered = offline.port.register()
        assertInstanceOf(AccountRegistration.SignInNeeded::class.java, registered, "with nothing saved the person must sign in")
        assertFalse(registered.toString().contains("access"))
    }
}
