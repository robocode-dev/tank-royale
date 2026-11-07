package dev.robocode.tankroyale.rumble

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/** The first three steps of the Submit or update bot wizard, against real bot folders and the real library. */
class RumbleSubmitScreenTest {

    private val direct = Executor { it.run() }

    private class Screen(dir: Path, port: SubmitPort? = null, background: Executor, post: Executor) {
        val staging: Path = dir.resolve("state").resolve("staging")
        val states = mutableListOf<SubmitState>()
        val controller = SubmitController(port ?: LibrarySubmitPort(staging), FakeSendPort(), background, post, ManualScheduler()) { states += it }
        val state get() = controller.state
    }

    private fun screen(dir: Path) = Screen(dir, null, direct, direct)

    /** A bot folder that passes the checks, below [parent]. */
    private fun bot(parent: Path, name: String = "MyBot", license: String? = "MIT"): Path {
        val folder = parent.resolve(name)
        val licenseLine = license?.let { "\"license\": \"$it\"," }.orEmpty()
        write(folder.resolve("$name.json"), "{ \"name\": \"$name\", \"version\": \"1.0.0\", \"authors\": [\"A. Author\"], $licenseLine \"description\": \"A test bot\", \"platform\": \"JVM\" }")
        write(folder.resolve("$name.sh"), "#!/bin/sh\n")
        write(folder.resolve("$name.cmd"), "@echo off\r\n")
        write(folder.resolve("src/$name.java"), "import dev.robocode.tankroyale.botapi.Bot;\nclass $name\n")
        return folder
    }

    private fun write(file: Path, text: String) {
        Files.createDirectories(file.parent)
        Files.writeString(file, text)
    }

    /** What is in a folder, by relative path, size and content, so any change shows. */
    private fun tree(root: Path): Map<String, String> =
        if (!Files.exists(root)) emptyMap()
        else Files.walk(root).use { s ->
            s.filter { Files.isRegularFile(it) }.collect(java.util.stream.Collectors.toList())
                .associate { root.relativize(it).toString() to Files.readString(it) }
        }

    // RCL-058

    @Test
    @Tag("RCL-058")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL058_UnitPositive_choosingABotOrATeamShowsWhatItIsAndChangesNothing(@TempDir dir: Path) {
        val folder = bot(dir.resolve("work"))
        val team = dir.resolve("work").resolve("MyTeam")
        write(team.resolve("MyTeam.json"), "{ \"name\": \"MyTeam\", \"version\": \"1.0.0\", \"authors\": [\"A\"], \"license\": \"MIT\", \"description\": \"d\", \"platform\": \"JVM\", \"teamMembers\": [\"MyBot 1.0.0\"] }")
        val before = tree(dir)
        val screen = screen(dir)

        screen.controller.choose(folder)
        assertEquals(SubmitState.Choose(folder, FoundBot("MyBot", "1.0.0", "java", false)), screen.state)
        assertEquals(listOf(SubmitState.Working(folder, false), screen.state), screen.states, "reading only, no check")

        screen.controller.choose(team)
        assertEquals(true, (screen.state as SubmitState.Choose).found?.isTeam)
        assertEquals(before, tree(dir), "choosing changes nothing on disk")
        assertFalse(Files.exists(screen.staging))
    }

    @Test
    @Tag("RCL-058")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL058_UnitNegative_aFolderThatIsNoBotShowsAPlainProblemCode(@TempDir dir: Path) {
        val screen = screen(dir)
        val noConfig = Files.createDirectories(dir.resolve("work").resolve("Empty"))
        val file = dir.resolve("work").resolve("file.txt").also { write(it, "x") }
        val broken = dir.resolve("work").resolve("Broken").also { write(it.resolve("Broken.json"), "{ not json") }

        fun code(folder: Path): String? {
            screen.controller.choose(folder)
            return (screen.state as SubmitState.Choose).problem?.code
        }

        assertEquals("MISSING_CONFIG", code(noConfig))
        assertEquals("NOT_A_FOLDER", code(file))
        assertEquals("INVALID_CONFIG", code(broken))
        assertEquals(null, (screen.state as SubmitState.Choose).found)
    }

    // RCL-059

    @Test
    @Tag("RCL-059")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL059_UnitNegative_everyProblemIsListedAtOnceWithItsCodeFileMessageAndFix(@TempDir dir: Path) {
        val folder = bot(dir.resolve("work"), license = null)
        Files.delete(folder.resolve("MyBot.sh"))
        val screen = screen(dir)
        screen.controller.choose(folder)

        screen.controller.check()

        val problems = screen.state as SubmitState.Problems
        assertTrue(problems.findings.size >= 2, problems.findings.toString())
        problems.findings.forEach { f -> listOf(f.code, f.file, f.message, f.fix).forEach { assertTrue(it.isNotBlank()) } }
        assertTrue(problems.findings.any { it.code == "MISSING_LAUNCHER" })
    }

    @Test
    @Tag("RCL-059")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL059_UnitPositive_aFixedFolderPassesWhenCheckedAgain(@TempDir dir: Path) {
        val folder = bot(dir.resolve("work"))
        Files.delete(folder.resolve("MyBot.sh"))
        val screen = screen(dir)
        screen.controller.choose(folder)
        screen.controller.check()
        assertInstanceOf(SubmitState.Problems::class.java, screen.state)

        write(folder.resolve("MyBot.sh"), "#!/bin/sh\n")
        screen.controller.check()

        assertEquals(SubmitState.Checked(folder, FoundBot("MyBot", "1.0.0", "java", false), screen.state.let { (it as SubmitState.Checked).notes }), screen.state)
    }

    // RCL-060

    @Test
    @Tag("RCL-060")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL060_UnitPositive_reviewStagesACopyShowsExactlyWhatWillBePublishedAndLeavesTheOriginalAlone(@TempDir dir: Path) {
        val folder = bot(dir.resolve("work"))
        val before = tree(folder)
        val screen = screen(dir)
        screen.controller.choose(folder)
        screen.controller.check()

        screen.controller.review()

        val review = (screen.state as SubmitState.Review).review
        assertEquals("MyBot", review.name)
        assertEquals("1.0.0", review.version)
        assertEquals("java", review.language)
        assertEquals("MIT", review.license)
        assertEquals(setOf("MyBot.json", "MyBot.sh", "MyBot.cmd", "src/MyBot.java"), review.files.toSet())
        assertEquals(before, tree(folder), "the original is byte for byte as it was")
        assertTrue(Files.isDirectory(screen.staging), "the copy is in the state folder")
    }

    @Test
    @Tag("RCL-060")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL060_UnitNegative_aCopyTheLibraryRejectsReturnsThePersonToTheProblems(@TempDir dir: Path) {
        val folder = bot(dir.resolve("work"))
        val screen = screen(dir)
        screen.controller.choose(folder)
        screen.controller.check()
        Files.delete(folder.resolve("MyBot.cmd")) // fixed too early, broken again before the review

        screen.controller.review()

        val problems = screen.state as SubmitState.Problems
        assertTrue(problems.findings.any { it.code == "MISSING_LAUNCHER" }, problems.findings.toString())
    }

    // RCL-061

    @Test
    @Tag("RCL-061")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL061_UnitPositive_thePersonCanGoOnOnlyWithBothConfirmationsAndNothingIsSent(@TempDir dir: Path) {
        val folder = bot(dir.resolve("work"))
        val before = tree(dir)
        val screen = screen(dir)
        screen.controller.choose(folder)
        screen.controller.check()
        screen.controller.review()
        val review = screen.state as SubmitState.Review
        assertFalse(review.publicConfirmed || review.authorConfirmed, "nothing ticks them")
        assertFalse(review.canContinue)

        screen.controller.next()
        assertInstanceOf(SubmitState.Review::class.java, screen.state)
        screen.controller.setPublicConfirmed(true)
        screen.controller.next()
        assertInstanceOf(SubmitState.Review::class.java, screen.state, "one box is not enough")
        screen.controller.setPublicConfirmed(false)
        screen.controller.setAuthorConfirmed(true)
        screen.controller.next()
        assertInstanceOf(SubmitState.Review::class.java, screen.state, "the other box alone is not enough either")
        screen.controller.setPublicConfirmed(true)
        screen.controller.next()

        assertInstanceOf(SubmitState.SendReady::class.java, screen.state, "going on leads to the copy check, which a ready account passes")
        assertEquals(tree(dir).filterKeys { !it.startsWith("state") }, before.filterKeys { !it.startsWith("state") }, "nothing is written beyond the staged copy")
    }

    @Test
    @Tag("RCL-061")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL061_UnitNegative_goingBackAndReturningStartsWithBothConfirmationsUnticked(@TempDir dir: Path) {
        val screen = screen(dir)
        screen.controller.choose(bot(dir.resolve("work")))
        screen.controller.check()
        screen.controller.review()
        screen.controller.setPublicConfirmed(true)
        screen.controller.setAuthorConfirmed(true)
        assertTrue((screen.state as SubmitState.Review).canContinue)

        screen.controller.back()
        assertInstanceOf(SubmitState.Checked::class.java, screen.state)
        screen.controller.review()

        val again = screen.state as SubmitState.Review
        assertFalse(again.publicConfirmed || again.authorConfirmed)
        screen.controller.back()
        screen.controller.back()
        assertInstanceOf(SubmitState.Choose::class.java, screen.state)
        // A tick that arrives when the review is not shown is ignored.
        screen.controller.setPublicConfirmed(true)
        assertInstanceOf(SubmitState.Choose::class.java, screen.state)
    }

    // RCL-062

    @Test
    @Tag("RCL-062")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL062_UnitPositive_everyCallReturnsAtOnceWhileTheDiskWorkBlocksAndResultsComeThroughTheScreensQueue(@TempDir dir: Path) {
        val gate = CountDownLatch(1)
        val real = LibrarySubmitPort(dir.resolve("state").resolve("staging"))
        val blocking = object : SubmitPort {
            override fun inspect(folder: Path): SubmitInspected { gate.await(10, TimeUnit.SECONDS); return real.inspect(folder) }
            override fun check(folder: Path): SubmitChecked { gate.await(10, TimeUnit.SECONDS); return real.check(folder) }
            override fun stage(folder: Path): SubmitStaged { gate.await(10, TimeUnit.SECONDS); return real.stage(folder) }
        }
        val background = Executors.newFixedThreadPool(4)
        val queue = LinkedBlockingQueue<Runnable>()
        val seenOn = mutableSetOf<Thread>()
        val caller = Thread.currentThread()
        val folder = bot(dir.resolve("work"))
        val controller = SubmitController(blocking, FakeSendPort(), background, { queue.add(it) }, ManualScheduler()) { seenOn += Thread.currentThread() }
        try {
            val started = System.nanoTime()
            controller.choose(folder)
            controller.check()
            controller.review()
            controller.back()
            controller.close()
            assertTrue(System.nanoTime() - started < TimeUnit.SECONDS.toNanos(2), "the calls did not wait for the disk")

            while (true) (queue.poll() ?: break).run()
            gate.countDown()
            Thread.sleep(300)
            while (true) (queue.poll() ?: break).run()
            assertTrue(seenOn.isNotEmpty())
            assertEquals(setOf(caller), seenOn, "states are shown on the queue's thread only")
            assertFalse(controller.state is SubmitState.Review, "results that came after closing were thrown away")
        } finally {
            gate.countDown()
            background.shutdownNow()
        }
    }
}
