package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.IOException
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.util.concurrent.Executor

/** The Contribute battles screen's behavior without a window, against a fake session folder, catalog, program and process. */
class RumbleContributeScreenTest {

    private val ownPid = 100L
    private val jar: Path = Path.of("rumble.jar")
    private val bots = listOf("Alpha 1.0", "Beta 1.0", "Gamma 1.0")
    private val plan = FetchPlan.Needed("1.4.0", "robocode-tankroyale-rumble-cli-1.4.0.jar", 12L * 1024 * 1024, "a".repeat(64), "https://example/rumble.jar")

    private fun status(pid: Long = 555, owner: Long? = ownPid, state: String = "running", ended: SessionEnded? = null) = SessionStatus(
        environment = "test", pid = pid, startedAt = "2026-11-18T10:00:00Z", owner = owner?.let { SessionOwner(it, "2026-11-18T09:00:00Z") },
        state = state, formats = listOf("1v1"), battleLimit = 3, kept = 2, updatedAt = "2026-11-18T10:01:00Z", ended = ended,
    )

    private open class FakePort(override val ownPid: Long) : ContributePort {
        var look: SessionLook = SessionLook.None
        var bots: List<String>? = listOf("Alpha 1.0", "Beta 1.0", "Gamma 1.0")
        var plan: FetchPlan = FetchPlan.NotNeeded(ProgramStatus.Ready(Path.of("rumble.jar"), "1.4.0", false))
        var fetched: ProgramFetch = ProgramFetch.Fetched(Path.of("rumble.jar"), "1.4.0")
        var alive = true
        var startFails = false
        val fetches = mutableListOf<FetchPlan.Needed>()
        val starts = mutableListOf<List<String>>()
        val asked = mutableListOf<SessionControlRequest>()

        override fun look() = look
        override fun activeBots(): List<String> = bots ?: throw IOException("offline")
        override fun planProgram() = plan
        override fun fetchProgram(plan: FetchPlan.Needed): ProgramFetch = fetched.also { fetches += plan }
        override fun start(jar: Path, arguments: List<String>): Long {
            if (startFails) throw IOException("no java")
            starts += arguments
            return 555
        }
        override fun isAlive(pid: Long) = alive
        override fun errors() = "Error: the jar is broken"
        override fun ask(request: SessionControlRequest): Boolean = (look is SessionLook.Running).also { if (it) asked += request }
    }

    /** The screen with work done at once, and every change counted as shown on the screen's own queue. */
    private inner class Screen(val port: FakePort = FakePort(ownPid)) {
        var now: Instant = Instant.parse("2026-11-18T10:00:00Z")
        val shown = mutableListOf<ContributeState>()
        val done = mutableListOf<Pair<SessionStatus, Boolean>>()
        var inPost = false
        var outsidePost = 0
        private val post = Executor { task ->
            val was = inPost
            inPost = true
            try { task.run() } finally { inPost = was }
        }
        val controller = ContributeController(
            port, RumbleEnvironments.TEST, Executor { it.run() }, post,
            onState = { if (!inPost) outsidePost++; shown += it },
            onDone = { status, unexpected -> done += status to unexpected },
            clock = { now }, startTimeout = Duration.ofSeconds(60),
        ).apply { start() }

        val state get() = controller.state
    }

    // RCL-135

    @Test
    @Tag("RCL-135")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL135_UnitPositive_theProgramIsFetchedOnlyAfterThePersonAgreesAndThenTheSessionStarts() {
        val screen = Screen().apply { port.plan = plan }

        screen.controller.startSession(keepOnClose = false)

        val asking = assertInstanceOf(ContributeState.AskingForProgram::class.java, screen.state)
        assertEquals(plan, asking.plan)
        assertTrue(screen.port.fetches.isEmpty() && screen.port.starts.isEmpty(), "nothing is fetched or started before the person agrees")

        screen.controller.fetchProgram()

        assertEquals(listOf(plan), screen.port.fetches)
        assertEquals(1, screen.port.starts.size)
        assertInstanceOf(ContributeState.Starting::class.java, screen.state)
    }

    @Test
    @Tag("RCL-135")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL135_UnitNegative_aDeclinedOrFailedFetchStartsNothingAndSaysWhy() {
        val declined = Screen().apply { port.plan = plan }
        declined.controller.startSession(keepOnClose = false)
        declined.controller.declineProgram()
        assertInstanceOf(ContributeState.Choosing::class.java, declined.state)
        assertTrue(declined.port.fetches.isEmpty() && declined.port.starts.isEmpty())

        val mismatch = Screen().apply { port.plan = plan; port.fetched = ProgramFetch.Failed(ProgramUnavailable(ProgramProblem.HASH_MISMATCH, "differs")) }
        mismatch.controller.startSession(keepOnClose = false)
        mismatch.controller.fetchProgram()
        val failed = assertInstanceOf(ContributeState.Choosing::class.java, mismatch.state)
        assertEquals(ContributeProblem.PROGRAM_UNAVAILABLE, failed.problem)
        assertEquals(ProgramProblem.HASH_MISMATCH, failed.program)
        assertTrue(mismatch.port.starts.isEmpty())

        val none = Screen().apply { port.plan = FetchPlan.Unavailable(ProgramUnavailable(ProgramProblem.NO_RELEASE, "none")) }
        none.controller.startSession(keepOnClose = false)
        assertEquals(ProgramProblem.NO_RELEASE, (none.state as ContributeState.Choosing).program)
        assertTrue(none.port.fetches.isEmpty() && none.port.starts.isEmpty())
    }

    // RCL-136

    @Test
    @Tag("RCL-136")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL136_UnitPositive_theChoicesStartASessionThatIsFollowedAndControlledThroughTheSessionFolder() {
        val screen = Screen()
        screen.controller.setMode(ContributeMode.ONLY)
        screen.controller.setBot("Beta 1.0", true)
        screen.controller.setFormat("melee", false)
        screen.controller.setFormat("twinduel", false)
        screen.controller.setBattles(3)

        screen.controller.startSession(keepOnClose = false)

        assertEquals(
            listOf("contribute", "-e", "test", "--mode", "only", "--bot", "Beta 1.0", "--format", "1v1", "--battles", "3", "--owner-pid", "$ownPid"),
            screen.port.starts.single(),
        )
        assertInstanceOf(ContributeState.Starting::class.java, screen.state)
        screen.port.look = SessionLook.Running(status())
        screen.controller.refresh()
        val running = assertInstanceOf(ContributeState.Running::class.java, screen.state)
        assertTrue(running.ownedHere)

        for ((act, request) in listOf<Pair<() -> Unit, SessionControlRequest>>(
            screen.controller::pause to SessionControlRequest.PAUSE, screen.controller::resume to SessionControlRequest.RESUME,
            screen.controller::stop to SessionControlRequest.STOP, screen.controller::stopNow to SessionControlRequest.STOP_NOW,
        )) {
            act()
            assertEquals(request, (screen.state as ContributeState.Running).asked)
        }
        assertEquals(listOf(SessionControlRequest.PAUSE, SessionControlRequest.RESUME, SessionControlRequest.STOP, SessionControlRequest.STOP_NOW), screen.port.asked)
        assertEquals(0, screen.outsidePost, "every change reaches the screen through its queue")

        // Kept running after Tank Royale closes: no owner; keep running: no battle count.
        val free = Screen()
        free.controller.setBattles(null)
        free.controller.startSession(keepOnClose = true)
        val arguments = free.port.starts.single()
        assertFalse("--owner-pid" in arguments)
        assertFalse("--battles" in arguments)
        assertFalse("--bot" in arguments, "all bots name none")
    }

    @Test
    @Tag("RCL-136")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL136_UnitNegative_nothingStartsWithoutUsableChoicesAndAFailedStartSaysWhy() {
        val screen = Screen()
        screen.controller.setMode(ContributeMode.ONLY)
        screen.controller.startSession(keepOnClose = false)
        screen.controller.setMode(ContributeMode.ALL)
        ContributeChoices.RANKED_FORMATS.forEach { screen.controller.setFormat(it, false) }
        screen.controller.startSession(keepOnClose = false)
        assertTrue(screen.port.starts.isEmpty(), "only chosen bots without any, or no format, starts nothing")

        val offline = Screen(FakePort(ownPid).apply { bots = null })
        val choosing = offline.state as ContributeState.Choosing
        assertNull(choosing.bots)
        assertEquals(ContributeProblem.CATALOG_UNREADABLE, choosing.problem)
        offline.controller.startSession(keepOnClose = false)
        assertTrue(offline.port.starts.isEmpty())
        offline.port.bots = bots
        offline.controller.readAgain()
        assertEquals(bots, (offline.state as ContributeState.Choosing).bots)

        // A catalog that cannot be understood ends the reading too, instead of leaving the screen looking.
        val broken = Screen(object : FakePort(ownPid) {
            override fun activeBots(): List<String> = throw IllegalArgumentException("not JSON")
        })
        broken.controller.readAgain()
        assertEquals(ContributeProblem.CATALOG_UNREADABLE, (broken.state as ContributeState.Choosing).problem)

        // A program that ends before it takes the session folder, or that cannot be started at all.
        val died = Screen()
        died.controller.startSession(keepOnClose = false)
        died.port.alive = false
        died.controller.refresh()
        val failed = died.state as ContributeState.Choosing
        assertEquals(ContributeProblem.START_FAILED, failed.problem)
        assertTrue(failed.details.contains("broken"))

        val slow = Screen()
        slow.controller.startSession(keepOnClose = false)
        slow.now = slow.now.plusSeconds(61)
        slow.controller.refresh()
        assertEquals(ContributeProblem.START_FAILED, (slow.state as ContributeState.Choosing).problem)

        val noJava = Screen().apply { port.startFails = true }
        noJava.controller.startSession(keepOnClose = false)
        assertEquals(ContributeProblem.START_FAILED, (noJava.state as ContributeState.Choosing).problem)

        // Also right after the program was fetched, a start that fails is a start that failed, not a missing program.
        val fetchedThenFailed = Screen().apply { port.plan = plan; port.startFails = true }
        fetchedThenFailed.controller.startSession(keepOnClose = false)
        fetchedThenFailed.controller.fetchProgram()
        assertEquals(ContributeProblem.START_FAILED, (fetchedThenFailed.state as ContributeState.Choosing).problem)
    }

    // RCL-137

    @Test
    @Tag("RCL-137")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL137_UnitPositive_aRunningSessionIsFoundAgainAndItsEndIsShownWithANotice() {
        val port = FakePort(ownPid).apply { look = SessionLook.Running(status(owner = 7)) }
        val screen = Screen(port)

        val running = assertInstanceOf(ContributeState.Running::class.java, screen.state)
        assertFalse(running.ownedHere, "a session started elsewhere is shown, not owned")

        val ended = status(ended = SessionEnded("finished", "2026-11-18T11:00:00Z", "The session ran its battles."))
        port.look = SessionLook.Ended(ended)
        screen.controller.refresh()
        assertEquals(ended, (screen.state as ContributeState.Choosing).last)
        assertEquals(listOf(ended to false), screen.done)
        screen.controller.refresh()
        assertEquals(1, screen.done.size, "the notice is given once")

        // A session that ended while the program was closed is shown as a summary, without a notice of its own here.
        // An ending the person asked for says all there is in its own text; a failure also shows the program's message.
        assertTrue(listOf("finished", "stopped", "cancelled").all { ContributeTexts.ordinary(it) })
        assertFalse(listOf("gave-up", "worker-unavailable", "failed").any { ContributeTexts.ordinary(it) })

        val later = Screen(FakePort(ownPid).apply { look = SessionLook.Ended(ended) })
        assertEquals(ended, (later.state as ContributeState.Choosing).last)
        assertTrue(later.done.isEmpty())
    }

    @Test
    @Tag("RCL-137")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL137_UnitNegative_aSessionThatEndedUnexpectedlyIsShownSoAndOneStartedElsewhereIsFound() {
        val port = FakePort(ownPid).apply { look = SessionLook.Running(status()) }
        val screen = Screen(port)
        val crashed = status()
        port.look = SessionLook.EndedUnexpectedly(crashed)
        screen.controller.refresh()
        val choosing = screen.state as ContributeState.Choosing
        assertTrue(choosing.lastUnexpected)
        assertEquals(listOf(crashed to true), screen.done)

        port.look = SessionLook.Running(status(pid = 777, owner = null))
        screen.controller.refresh()
        assertEquals(777, (screen.state as ContributeState.Running).status!!.pid)
    }

    // RCL-138

    @Test
    @Tag("RCL-138")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL138_UnitPositive_closingWhileAnOwnedSessionRunsAsksOrFollowsTheKeptAnswer() {
        val screen = Screen(FakePort(ownPid).apply { look = SessionLook.Running(status()) })

        assertEquals(CloseAction.ASK, screen.controller.closeAction(CloseChoice.ASK))
        assertEquals(CloseAction.RELEASE, screen.controller.closeAction(CloseChoice.KEEP))
        assertEquals(CloseAction.LET_STOP, screen.controller.closeAction(CloseChoice.STOP))

        // Closing while the session it owns is still starting is asked about the same way.
        val starting = Screen()
        starting.controller.startSession(keepOnClose = false)
        assertInstanceOf(ContributeState.Starting::class.java, starting.state)
        assertEquals(CloseAction.ASK, starting.controller.closeAction(CloseChoice.ASK))
    }

    @Test
    @Tag("RCL-138")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL138_UnitNegative_closingAsksNothingWithoutASessionThisProgramOwns() {
        val none = Screen()
        assertEquals(CloseAction.NOTHING, none.controller.closeAction(CloseChoice.ASK))

        val elsewhere = Screen(FakePort(ownPid).apply { look = SessionLook.Running(status(owner = 7)) })
        assertEquals(CloseAction.NOTHING, elsewhere.controller.closeAction(CloseChoice.ASK))

        val released = Screen(FakePort(ownPid).apply { look = SessionLook.Running(status()) })
        released.controller.release()
        assertEquals(listOf(SessionControlRequest.RELEASE), released.port.asked)
        // Only the session's own status says it took the release; until then closing still asks.
        assertEquals(CloseAction.ASK, released.controller.closeAction(CloseChoice.ASK))
        released.port.look = SessionLook.Running(status(owner = null))
        released.controller.refresh()
        assertEquals(CloseAction.NOTHING, released.controller.closeAction(CloseChoice.ASK))

        // A request that could not be asked is not shown as asked.
        val stuck = Screen(object : FakePort(ownPid) {
            override fun ask(request: SessionControlRequest): Boolean = throw IOException("held")
        }.apply { look = SessionLook.Running(status()) })
        stuck.controller.release()
        assertNull((stuck.state as ContributeState.Running).asked)
        assertEquals(CloseAction.ASK, stuck.controller.closeAction(CloseChoice.ASK))

        val free = Screen()
        free.controller.startSession(keepOnClose = true)
        assertEquals(CloseAction.NOTHING, free.controller.closeAction(CloseChoice.ASK), "a session started without an owner is not asked about")
    }

    // RCI-055 and RCI-057: the session folder itself

    @Test
    @Tag("RCI-055")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI055_UnitPositive_aSessionStartingWhileTheFolderIsLookedAtStillTakesItAndAWriteNeverEndsIt(@TempDir dir: Path) {
        val folder = SessionFolder(dir.resolve("session"))
        val other = folder.claim()!!
        // Another program holds the lock for a moment, as a look does; the starting session waits it out.
        Thread { Thread.sleep(150); other.close() }.start()
        val mine = folder.claim()
        assertTrue(mine != null, "the lock held for a moment does not make the session give up")

        assertTrue(mine!!.write(status()))
        mine.close()

        // The next session starts without the last one's status, keeps a request asked once it holds the lock, and drops
        // one left from before; requests are taken in the order they were asked.
        val requests = folder.folder.resolve("requests")
        java.nio.file.Files.writeString(requests.resolve("000000000000001-000000001-old.stop"), "")
        val next = folder.claim()!!
        assertEquals(SessionLook.Running(null), folder.look(), "the last session's status is not shown as the new one's")
        assertTrue(folder.ask(SessionControlRequest.PAUSE))
        assertTrue(folder.ask(SessionControlRequest.RESUME))
        assertTrue(folder.ask(SessionControlRequest.RELEASE))
        assertEquals(listOf(SessionControlRequest.PAUSE, SessionControlRequest.RESUME, SessionControlRequest.RELEASE), next.take())
        next.close()
        assertFalse(mine.write(status()), "nothing is written after the session let go of the folder")
        assertTrue(mine.take().isEmpty(), "and no request is taken from a later session")
    }

    @Test
    @Tag("RCI-057")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCI057_UnitPositive_anOwnerWhoseStartTimeIsNotToldIsKnownByItsProcessId() {
        assertTrue(SessionOwner(ProcessHandle.current().pid(), "").isAlive())
        assertFalse(SessionOwner(Long.MAX_VALUE - 1, "").isAlive())
    }
}
