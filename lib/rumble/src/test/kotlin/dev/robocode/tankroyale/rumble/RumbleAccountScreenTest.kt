package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

class RumbleAccountScreenTest {

    private val expires = Instant.parse("2026-11-05T12:00:00Z")
    private val token = "ghu_FAKEACCESSTOKEN0123456789"

    /** A library that answers what the test says. The token exists only here, so it can be shown to be absent. */
    private class Port : AccountPort {
        var opened: AccountOpened = AccountOpened.SignedOut()
        var begun: AccountBegun = AccountBegun.Code("F94F-59CF", "https://github.com/login/device", Instant.parse("2026-11-05T12:00:00Z"), 5)
        val checks = ArrayDeque<AccountChecked>()
        var registration: AccountRegistration = AccountRegistration.Registered("alice-1234")
        var removed = true
        var gate: CountDownLatch? = null
        val calls: MutableList<String> = java.util.Collections.synchronizedList(mutableListOf<String>())

        private fun blocked() { gate?.await(10, TimeUnit.SECONDS) }

        override fun open(): AccountOpened { calls += "open"; blocked(); return opened }
        override fun begin(): AccountBegun { calls += "begin"; blocked(); return begun }
        override fun check(): AccountChecked { calls += "check"; blocked(); return checks.removeFirstOrNull() ?: AccountChecked.NoWaitingCode }
        override fun register(): AccountRegistration { calls += "register"; blocked(); return registration }
        override fun cancel() { calls += "cancel" }
        override fun signOut(): Boolean { calls += "signOut"; blocked(); return removed }
    }

    /** Time moved by hand: tasks run only when the test says so. */
    private class Clock : AccountScheduler {
        class Task(val seconds: Long, val task: () -> Unit) : AutoCloseable {
            var closed = false
            override fun close() { closed = true }
        }

        val tasks = mutableListOf<Task>()
        override fun after(seconds: Long, task: () -> Unit): AutoCloseable = Task(seconds, task).also { tasks += it }
        val open: List<Task> get() = tasks.filter { !it.closed }
        fun fire() { val next = open.first(); next.closed = true; next.task() }
    }

    private class Screen(val port: Port = Port(), val clock: Clock = Clock(), via: AccountPort = port) {
        val states = mutableListOf<AccountState>()
        val direct = Executor { it.run() }
        val controller = AccountController(via, direct, direct, clock) { states += it }
        val state get() = controller.state
    }

    // RCL-051

    @Test
    @Tag("RCL-051")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL051_UnitPositive_openingShowsSignedInSignedOutAndWhyWhenThereIsAReason() {
        val signedIn = Screen().apply { port.opened = AccountOpened.SignedIn("alice"); controller.open() }
        assertEquals(AccountState.SignedIn("alice", AccountRegistration.Registered("alice-1234")), signedIn.state)
        assertEquals(listOf(AccountState.Starting, AccountState.SignedIn("alice", AccountRegistration.Registering)), signedIn.states.take(2))

        assertEquals(AccountState.SignedOut(), Screen().apply { controller.open() }.state)

        val unusable = Screen().apply { port.opened = AccountOpened.SignedOut(AccountNotice.SAVED_UNUSABLE, "The saved sign-in could not be used."); controller.open() }
        assertEquals(AccountState.SignedOut(AccountNotice.SAVED_UNUSABLE, "The saved sign-in could not be used."), unusable.state)
    }

    @Test
    @Tag("RCL-051")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL051_UnitNegative_aLibraryThatFailsOrIsUnavailableIsShownWithItsPlainMessageAndNoTokenAppears() {
        val failed = Screen().apply { port.opened = AccountOpened.Failed("GitHub could not be reached."); controller.open() }
        assertEquals(AccountState.SignedOut(AccountNotice.FAILED, "GitHub could not be reached."), failed.state)

        val none = Screen().apply { port.opened = AccountOpened.Unavailable("Signing in is not available for the Public Beta Rumble yet."); controller.open() }
        assertEquals(AccountState.Unavailable("Signing in is not available for the Public Beta Rumble yet."), none.state)

        val broken = Port()
        val throwing = Screen(broken, via = object : AccountPort by broken { override fun open(): AccountOpened = throw IllegalStateException(token) })
        throwing.controller.open()
        assertEquals(AccountState.SignedOut(AccountNotice.FAILED, null), throwing.state, "a failure with nothing to say has no detail, and the screen says it in its own words")

        listOf(failed, none, throwing).forEach { screen -> assertFalse(screen.states.joinToString().contains(token)) }
    }

    // RCL-052

    @Test
    @Tag("RCL-052")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL052_UnitPositive_signingInShowsTheCodeWaitsAtGitHubsPaceThenRegistersAndShowsTheClientId() {
        val screen = Screen().apply { controller.open() }

        screen.controller.signIn()
        assertEquals(AccountState.WaitingForCode("F94F-59CF", "https://github.com/login/device", expires), screen.state)
        assertEquals(listOf(5L), screen.clock.open.map { it.seconds }, "the first check waits the pace GitHub asked for")
        assertEquals(0, screen.port.calls.count { it == "check" }, "GitHub is not asked before that")

        screen.port.checks += AccountChecked.Waiting("F94F-59CF", "https://github.com/login/device", expires, 10)
        screen.clock.fire()
        assertEquals(listOf(10L), screen.clock.open.map { it.seconds })
        assertInstanceOf(AccountState.WaitingForCode::class.java, screen.state)

        screen.port.checks += AccountChecked.SignedIn("alice")
        screen.clock.fire()
        assertEquals(AccountState.SignedIn("alice", AccountRegistration.Registered("alice-1234")), screen.state)
        assertEquals(emptyList<Clock.Task>(), screen.clock.open, "nothing more is checked")
        assertEquals(listOf("open", "begin", "check", "check", "register"), screen.port.calls.toList())
    }

    @Test
    @Tag("RCL-052")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL052_UnitNegative_everyWayTheSignInCanEndShowsWhatHappened() {
        fun ends(check: AccountChecked): AccountState {
            val screen = Screen().apply { controller.open(); controller.signIn() }
            screen.port.checks += check
            screen.clock.fire()
            return screen.state
        }
        assertEquals(AccountState.SignedOut(AccountNotice.CODE_EXPIRED), ends(AccountChecked.Expired))
        assertEquals(AccountState.SignedOut(AccountNotice.DENIED), ends(AccountChecked.Denied))
        assertEquals(AccountState.SignedOut(AccountNotice.NOT_SAVED, "This computer cannot keep it."), ends(AccountChecked.NotSaved("This computer cannot keep it.")))
        assertEquals(AccountState.SignedOut(AccountNotice.FAILED, "GitHub could not be reached."), ends(AccountChecked.Failed("GitHub could not be reached.", canRetry = false)))
        assertEquals(AccountState.Unavailable("No App."), ends(AccountChecked.Unavailable("No App.")))
        assertEquals(AccountState.SignedOut(), ends(AccountChecked.NoWaitingCode))

        val blocked = Screen().apply { port.begun = AccountBegun.NotPrivate("This computer cannot keep your sign-in private."); controller.open(); controller.signIn() }
        assertEquals(AccountState.SignedOut(AccountNotice.NOT_PRIVATE, "This computer cannot keep your sign-in private."), blocked.state)
        val failed = Screen().apply { port.begun = AccountBegun.Failed("GitHub could not be reached."); controller.open(); controller.signIn() }
        assertEquals(AccountState.SignedOut(AccountNotice.FAILED, "GitHub could not be reached."), failed.state)
        val already = Screen().apply { port.begun = AccountBegun.AlreadySignedIn("bob"); controller.open(); controller.signIn() }
        assertEquals(AccountState.SignedIn("bob", AccountRegistration.Registered("alice-1234")), already.state)
    }

    @Test
    @Tag("RCL-052")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL052_UnitNegative_aCheckThatFailsButCanWorkIsTriedAgainAndARefusedRegistrationLeavesThePersonSignedIn() {
        val screen = Screen().apply { controller.open(); controller.signIn() }
        screen.port.checks += AccountChecked.Failed("GitHub could not be reached.", canRetry = true, retryAfterSeconds = 15)
        screen.clock.fire()
        assertInstanceOf(AccountState.WaitingForCode::class.java, screen.state, "still waiting")
        assertEquals(listOf(15L), screen.clock.open.map { it.seconds })

        screen.port.registration = AccountRegistration.Refused("The account is banned.")
        screen.port.checks += AccountChecked.SignedIn("alice")
        screen.clock.fire()
        assertEquals(AccountState.SignedIn("alice", AccountRegistration.Refused("The account is banned.")), screen.state)

        listOf(AccountRegistration.NotFinished, AccountRegistration.Failed("It failed.")).forEach { result ->
            val other = Screen().apply { port.registration = result; controller.open(); controller.signIn() }
            other.port.checks += AccountChecked.SignedIn("alice")
            other.clock.fire()
            assertEquals(AccountState.SignedIn("alice", result), other.state)
        }

        val revoked = Screen().apply { port.registration = AccountRegistration.SignInNeeded("GitHub did not accept the sign-in."); controller.open(); controller.signIn() }
        revoked.port.checks += AccountChecked.SignedIn("alice")
        revoked.clock.fire()
        assertEquals(AccountState.SignedOut(AccountNotice.REVOKED, "GitHub did not accept the sign-in."), revoked.state)
    }

    // RCL-053

    @Test
    @Tag("RCL-053")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL053_UnitPositive_cancelForgetsTheCodeCloseKeepsItAndSignOutRemovesWhatIsSaved() {
        val cancelled = Screen().apply { controller.open(); controller.signIn() }
        cancelled.controller.cancel()
        assertEquals(AccountState.SignedOut(AccountNotice.CANCELLED), cancelled.state)
        assertTrue("cancel" in cancelled.port.calls)
        assertEquals(emptyList<Clock.Task>(), cancelled.clock.open, "no more checks")

        val closed = Screen().apply { controller.open(); controller.signIn() }
        closed.controller.close()
        assertEquals(AccountState.SignedOut(), closed.state)
        assertFalse("cancel" in closed.port.calls, "the waiting code is kept")
        assertEquals(emptyList<Clock.Task>(), closed.clock.open)

        val out = Screen().apply { port.opened = AccountOpened.SignedIn("alice"); controller.open(); controller.signOut() }
        assertEquals(AccountState.SignedOut(), out.state)
        assertTrue("signOut" in out.port.calls)
    }

    @Test
    @Tag("RCL-053")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL053_UnitNegative_aSignOutThatCouldNotRemoveEverythingSaysSoAndAResultAfterCancellingIsThrownAway() {
        val failed = Screen().apply { port.removed = false; port.opened = AccountOpened.SignedIn("alice"); controller.open(); controller.signOut() }
        assertEquals(AccountState.SignedOut(AccountNotice.SIGN_OUT_FAILED), failed.state)

        // A check that was on its way when the person cancelled must not bring the sign-in back.
        val late = Screen().apply { controller.open(); controller.signIn() }
        late.port.checks += AccountChecked.SignedIn("alice")
        val waiting = late.clock.open.first()
        late.controller.cancel()
        waiting.task()
        assertEquals(AccountState.SignedOut(AccountNotice.CANCELLED), late.state)
        assertEquals(listOf("open", "begin", "cancel"), late.port.calls.toList())
    }

    @Test
    @Tag("RCL-053")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL053_UnitPositive_everyCallReturnsAtOnceWhileTheLibraryBlocksAndResultsComeThroughTheScreensQueue() {
        val port = Port().apply { gate = CountDownLatch(1) }
        val background: ExecutorService = Executors.newFixedThreadPool(4)
        val queue = LinkedBlockingQueue<Runnable>()
        val states = mutableListOf<AccountState>()
        val callerThread = Thread.currentThread()
        val seenOn = mutableSetOf<Thread>()
        val controller = AccountController(port, background, { queue.add(it) }, Clock()) { state -> seenOn += Thread.currentThread(); states += state }
        try {
            val started = System.nanoTime()
            controller.open()
            controller.signIn()
            controller.cancel()
            controller.close()
            controller.signOut()
            assertTrue(System.nanoTime() - started < TimeUnit.SECONDS.toNanos(2), "the calls did not wait for the library")

            // The screen's own queue is drained here, by the test thread, as the event thread would.
            drain(queue)
            assertEquals(setOf(callerThread), seenOn, "states are shown on the queue's thread only")
            port.gate!!.countDown()
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10)
            while (System.nanoTime() < deadline && controller.state != AccountState.SignedOut()) {
                Thread.sleep(10)
                drain(queue)
            }
            assertEquals(AccountState.SignedOut(), controller.state)
            assertEquals(setOf(callerThread), seenOn)
        } finally {
            port.gate?.countDown()
            background.shutdownNow()
        }
    }

    private fun drain(queue: LinkedBlockingQueue<Runnable>) {
        while (true) (queue.poll() ?: return).run()
    }
}
