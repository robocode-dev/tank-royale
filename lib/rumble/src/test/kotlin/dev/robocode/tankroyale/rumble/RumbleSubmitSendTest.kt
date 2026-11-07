package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.StagedSubmission
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

/** The last three steps of the Submit or update bot wizard: the copy check, sending and following, against fakes of GitHub. */
class RumbleSubmitSendTest {

    private val direct = Executor { it.run() }

    private class Screen(val dir: Path, val send: SendPort = FakeSendPort(), background: Executor, post: Executor) {
        val clock = ManualScheduler()
        val states = mutableListOf<SubmitState>()
        val staging: Path = dir.resolve("state").resolve("staging")
        val controller = SubmitController(LibrarySubmitPort(staging), send, background, post, clock) { states += it }
        val fake get() = send as FakeSendPort
        val state get() = controller.state
    }

    private fun screen(dir: Path, fake: FakeSendPort = FakeSendPort()) = Screen(dir, fake, direct, direct)

    private fun bot(parent: Path, name: String = "MyBot"): Path {
        val folder = parent.resolve(name)
        write(folder.resolve("$name.json"), "{ \"name\": \"$name\", \"version\": \"1.0.0\", \"authors\": [\"A. Author\"], \"license\": \"MIT\", \"description\": \"A test bot\", \"platform\": \"JVM\" }")
        write(folder.resolve("$name.sh"), "#!/bin/sh\n")
        write(folder.resolve("$name.cmd"), "@echo off\r\n")
        write(folder.resolve("src/$name.java"), "import dev.robocode.tankroyale.botapi.Bot;\nclass $name\n")
        return folder
    }

    private fun write(file: Path, text: String) {
        Files.createDirectories(file.parent)
        Files.writeString(file, text)
    }

    private fun tree(root: Path): Map<String, String> =
        if (!Files.exists(root)) emptyMap()
        else Files.walk(root).use { s ->
            s.filter { Files.isRegularFile(it) }.collect(java.util.stream.Collectors.toList()).associate { root.relativize(it).toString() to Files.readString(it) }
        }

    /** Brings the screen to the review of a real bot folder, with the confirmations still unticked. */
    private fun Screen.toReview(): Path {
        val folder = bot(dir.resolve("work"))
        controller.choose(folder)
        controller.check()
        controller.review()
        assertInstanceOf(SubmitState.Review::class.java, state)
        return folder
    }

    private fun Screen.tickBoth() {
        controller.setPublicConfirmed(true)
        controller.setAuthorConfirmed(true)
    }

    private fun Screen.toSendReady(): SubmitState.SendReady {
        toReview()
        tickBoth()
        controller.next()
        return state as SubmitState.SendReady
    }

    // RCL-066

    @Test
    @Tag("RCL-066")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL066_UnitPositive_aReadyAccountGoesStraightOnAndAMissingCopyOrAppIsWaitedFor(@TempDir dir: Path) {
        val ready = screen(dir.resolve("a"))
        ready.toSendReady()
        assertEquals(1, ready.fake.readinessCalls.size)
        assertEquals(emptyList<ManualScheduler.Task>(), ready.clock.open)

        val none = screen(dir.resolve("b"), FakeSendPort().apply { readiness = SubmissionReadiness.NoCopy("alice", "https://github.com/robocode-dev/rumble-bots-test/fork") })
        none.toReview(); none.tickBoth(); none.controller.next()
        assertEquals(SubmitState.Copy(none.states.filterIsInstance<SubmitState.Review>().last().folder, none.states.filterIsInstance<SubmitState.Review>().last().review, none.fake.readiness), none.state)
        assertEquals(listOf(5L), none.clock.open.map { it.seconds }, "it looks again by itself, calmly")

        none.fake.readiness = SubmissionReadiness.AppNotInstalled("alice", "https://github.com/apps/rumble/installations/new")
        none.clock.fire()
        assertEquals(none.fake.readiness, (none.state as SubmitState.Copy).readiness)
        assertEquals(listOf(5L), none.clock.open.map { it.seconds })

        none.fake.readiness = SubmissionReadiness.Ready("alice", "alice/rumble-bots-test")
        none.clock.fire()
        assertInstanceOf(SubmitState.SendReady::class.java, none.state, "both found, so it goes on by itself")
        assertEquals(emptyList<ManualScheduler.Task>(), none.clock.open, "and stops looking")
    }

    @Test
    @Tag("RCL-066")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL066_UnitNegative_otherAnswersAreShownOnceWithoutLookingAgainAndNothingIsMadeOrInstalled(@TempDir dir: Path) {
        listOf(
            SubmissionReadiness.NotRumblesRepository("alice", "https://github.com/alice/rumble-bots-test"),
            SubmissionReadiness.SignInNeeded("The sign-in is not accepted."),
            SubmissionReadiness.Failed("GitHub cannot be reached."),
        ).forEachIndexed { index, answer ->
            val screen = screen(dir.resolve("n$index"), FakeSendPort().apply { readiness = answer })
            screen.toReview(); screen.tickBoth(); screen.controller.next()

            assertEquals(answer, (screen.state as SubmitState.Copy).readiness)
            assertEquals(emptyList<ManualScheduler.Task>(), screen.clock.open, "no repeated look for $answer")
            assertEquals(emptyList<Any>(), screen.fake.sent, "nothing is sent while the account is not ready")
            assertEquals(emptyList<Any>(), screen.fake.prepared_for)
        }
        // The button looks again at once, which a failed look needs.
        val failed = screen(dir.resolve("again"), FakeSendPort().apply { readiness = SubmissionReadiness.Failed("x") })
        failed.toReview(); failed.tickBoth(); failed.controller.next()
        failed.fake.readiness = SubmissionReadiness.Ready("alice", "alice/rumble-bots-test")
        failed.controller.checkNow()
        assertInstanceOf(SubmitState.SendReady::class.java, failed.state)
    }

    @Test
    @Tag("RCL-066")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL066_UnitNegative_onlyGitHubsOwnPagesCanBeOpened() {
        assertTrue(RumbleLinks.isGitHubPage("https://github.com/robocode-dev/rumble-bots-test/fork"))
        assertTrue(RumbleLinks.isGitHubPage("https://github.com/apps/rumble/installations/new"))
        listOf("http://github.com/x", "https://github.com.evil.example/x", "https://evil.example/https://github.com/", "file:///C:/x", "javascript:alert(1)", "").forEach {
            assertFalse(RumbleLinks.isGitHubPage(it), it)
        }
    }

    // RCL-067

    private class BlockingSend(val inner: FakeSendPort = FakeSendPort()) : SendPort {
        val release = CountDownLatch(1)
        val entered = CountDownLatch(1)
        val threads = mutableListOf<Thread>()
        override fun readiness(): SubmissionReadiness {
            threads += Thread.currentThread(); entered.countDown(); release.await(); return inner.readiness()
        }
        override fun prepare(submission: StagedSubmission, account: String) = inner.prepare(submission, account)
        override fun send(submission: StagedSubmission, authorCertifiesLicense: Boolean) = inner.send(submission, authorCertifiesLicense)
        override fun progress(number: Int, name: String, version: String) = inner.progress(number, name, version)
    }

    @Test
    @Tag("RCL-067")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL067_UnitPositive_aBlockedLookReturnsAtOnceRunsAwayFromTheScreenAndItsResultIsShownInOrder(@TempDir dir: Path) {
        val blocking = BlockingSend()
        val background = Executors.newSingleThreadExecutor { Thread(it, "wizard-background").apply { isDaemon = true } }
        val queue = LinkedBlockingQueue<Runnable>()
        val screen = Screen(dir, blocking, background, { queue.add(it) })
        fun drain(count: Int) = repeat(count) { queue.poll(5, TimeUnit.SECONDS)!!.run() }
        try {
            // Reach the review with the real library: each step's work comes back through the queue.
            val folder = bot(dir.resolve("work"))
            screen.controller.choose(folder); drain(2)
            screen.controller.check(); drain(2)
            screen.controller.review(); drain(2)
            screen.controller.setPublicConfirmed(true); drain(1)
            screen.controller.setAuthorConfirmed(true); drain(1)

            val started = System.nanoTime()
            screen.controller.next(); drain(1)
            assertTrue(System.nanoTime() - started < TimeUnit.SECONDS.toNanos(2), "next returns while GitHub is being waited for")
            assertTrue(blocking.entered.await(5, TimeUnit.SECONDS))
            assertEquals(SubmitState.Copy::class, screen.state::class)
            assertEquals(null, (screen.state as SubmitState.Copy).readiness)
            assertEquals("wizard-background", blocking.threads.single().name, "the call to GitHub runs on a thread of its own")

            blocking.release.countDown()
            drain(2) // the readiness, then the preparing
            assertInstanceOf(SubmitState.SendReady::class.java, screen.state)
        } finally {
            blocking.release.countDown()
            background.shutdownNow()
        }
    }

    @Test
    @Tag("RCL-067")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL067_UnitNegative_noLookRunsAfterGoingBackOrClosingAndALateResultIsThrownAway(@TempDir dir: Path) {
        // Going back stops the repeated look.
        val waiting = screen(dir.resolve("a"), FakeSendPort().apply { readiness = SubmissionReadiness.NoCopy("alice", "https://github.com/x/y/fork") })
        waiting.toReview(); waiting.tickBoth(); waiting.controller.next()
        assertEquals(1, waiting.clock.open.size)
        waiting.controller.back()
        assertInstanceOf(SubmitState.Review::class.java, waiting.state)
        assertEquals(emptyList<ManualScheduler.Task>(), waiting.clock.open, "the waiting look was cancelled")
        assertEquals(1, waiting.fake.readinessCalls.size)

        // Closing stops it too.
        val closing = screen(dir.resolve("b"), FakeSendPort().apply { readiness = SubmissionReadiness.NoCopy("alice", "https://github.com/x/y/fork") })
        closing.toReview(); closing.tickBoth(); closing.controller.next()
        closing.controller.close()
        assertEquals(emptyList<ManualScheduler.Task>(), closing.clock.open)

        // A result that arrives after closing is dropped.
        val blocking = BlockingSend()
        val background = Executors.newSingleThreadExecutor { Thread(it).apply { isDaemon = true } }
        val queue = LinkedBlockingQueue<Runnable>()
        val late = Screen(dir.resolve("c"), blocking, background, { queue.add(it) })
        fun drain(count: Int) = repeat(count) { queue.poll(5, TimeUnit.SECONDS)!!.run() }
        try {
            val folder = bot(dir.resolve("c").resolve("work"))
            late.controller.choose(folder); drain(2)
            late.controller.check(); drain(2)
            late.controller.review(); drain(2)
            late.controller.setPublicConfirmed(true); drain(1)
            late.controller.setAuthorConfirmed(true); drain(1)
            late.controller.next(); drain(1)
            assertTrue(blocking.entered.await(5, TimeUnit.SECONDS))
            late.controller.close(); drain(1)
            val before = late.state
            blocking.release.countDown()
            drain(1)
            assertEquals(before, late.state, "the late answer changed nothing")
            assertEquals(emptyList<ManualScheduler.Task>(), late.clock.open)
        } finally {
            blocking.release.countDown()
            background.shutdownNow()
        }
    }

    // RCL-068

    @Test
    @Tag("RCL-068")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL068_UnitPositive_theReviewedCopyIsSentWithTheAuthorsConfirmationAndNeverTheOriginalFolder(@TempDir dir: Path) {
        val screen = screen(dir)
        val folder = screen.toReview()
        val reviewed = (screen.state as SubmitState.Review).review
        val original = tree(folder)
        screen.tickBoth()
        screen.controller.next()
        val ready = screen.state as SubmitState.SendReady
        assertEquals(reviewed, ready.review)

        screen.controller.send()

        val (submission, certified) = screen.fake.sent.single()
        assertTrue(certified, "the author's confirmation is the second box")
        assertEquals(reviewed.files, submission.files.map { it.path }, "exactly the files that were reviewed")
        assertTrue(submission.stagingFolder.startsWith(screen.staging), "the staged copy, not the original")
        assertNotEquals(folder, submission.botFolder)
        assertFalse(submission.botFolder.startsWith(folder))
        assertEquals(original, tree(folder), "the original folder is untouched")
        assertInstanceOf(SubmitState.SendEnded::class.java, screen.state)
    }

    @Test
    @Tag("RCL-068")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL068_UnitNegative_nothingIsSentWithoutBothConfirmationsOrWhenSendingIsNotOffered(@TempDir dir: Path) {
        // One box, the other box, none: the copy is not even looked for, and a press of send does nothing.
        listOf(Pair(true, false), Pair(false, true), Pair(false, false)).forEachIndexed { index, (publicBox, authorBox) ->
            val screen = screen(dir.resolve("o$index"))
            screen.toReview()
            screen.controller.setPublicConfirmed(publicBox)
            screen.controller.setAuthorConfirmed(authorBox)
            screen.controller.next()
            screen.controller.send()
            assertInstanceOf(SubmitState.Review::class.java, screen.state)
            assertEquals(emptyList<Any>(), screen.fake.sent)
            assertEquals(0, screen.fake.readinessCalls.size, "$publicBox $authorBox")
        }
        // Going back clears the author's confirmation, so a send after going back is not possible without ticking again.
        val back = screen(dir.resolve("back"))
        back.toSendReady()
        back.controller.back()
        back.controller.send()
        assertInstanceOf(SubmitState.Review::class.java, back.state)
        assertEquals(emptyList<Any>(), back.fake.sent)
        assertFalse((back.state as SubmitState.Review).canContinue)

        // Exactly this version is already in the Rumble, or the name and version hold other content: sending is not offered.
        listOf(ContentState.ALREADY_IN_RUMBLE, ContentState.DIFFERENT_CONTENT).forEachIndexed { index, content ->
            val fake = FakeSendPort().apply { prepared = SendPrepared.Ready(LauncherState.Same, content, null) }
            val screen = screen(dir.resolve("c$index"), fake)
            val ready = screen.toSendReady()
            assertFalse(ready.canSend)
            screen.controller.send()
            assertEquals(emptyList<Any>(), fake.sent, content.name)
        }
    }

    @Test
    @Tag("RCL-068")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL068_UnitNegative_aSecondPressOfSendOpensNothingNew(@TempDir dir: Path) {
        val screen = screen(dir)
        screen.toSendReady()

        screen.controller.send()
        screen.controller.send()

        assertEquals(1, screen.fake.sent.size, "once it has been sent, send is no longer offered")
        assertInstanceOf(SubmitState.SendEnded::class.java, screen.state)
    }

    // RCL-069

    @Test
    @Tag("RCL-069")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL069_UnitPositive_everyEndingOfASendHasItsOwnStateAndALauncherDifferenceNeverBlocks(@TempDir dir: Path) {
        SubmitTexts.ALL_RESULTS.forEachIndexed { index, result ->
            val fake = FakeSendPort().apply {
                this.result = result
                prepared = SendPrepared.Ready(LauncherState.Differs(listOf("MyBot.sh")), ContentState.NEW, null)
            }
            val screen = screen(dir.resolve("r$index"), fake)
            screen.toSendReady()
            screen.controller.send()

            val ended = screen.state as SubmitState.SendEnded
            assertEquals(result, ended.result)
            assertEquals(LauncherState.Differs(listOf("MyBot.sh")), ended.launchers, "advice only")
            assertEquals(1, fake.sent.size, "the difference did not stop the send")
            assertEquals(if (result is SubmissionResult.Submitted) 1 else 0, fake.progressCalls.size, "only a pull request is followed")
        }
    }

    @Test
    @Tag("RCL-069")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL069_UnitNegative_aFailedOrRefusedSendLeavesTheReviewedCopyAndShowsTheReviewAgainWithBoxesUnticked(@TempDir dir: Path) {
        val fake = FakeSendPort().apply { result = SubmissionResult.Failed("GitHub cannot be reached.") }
        val screen = screen(dir, fake)
        val ready = screen.toSendReady()
        screen.controller.send()
        assertInstanceOf(SubmissionResult.Failed::class.java, (screen.state as SubmitState.SendEnded).result)

        screen.controller.back()
        val review = screen.state as SubmitState.Review
        assertEquals(ready.review, review.review, "the same reviewed copy")
        assertFalse(review.publicConfirmed || review.authorConfirmed)

        // A thrown exception from the library is shown as a failed ending and never escapes.
        val throwing = object : SendPort by FakeSendPort() {
            override fun send(submission: StagedSubmission, authorCertifiesLicense: Boolean): SubmissionResult = throw IllegalStateException("boom")
        }
        val s2 = Screen(dir.resolve("t"), throwing, direct, direct)
        s2.toSendReady()
        s2.controller.send()
        assertEquals(SubmissionResult.Failed(""), (s2.state as SubmitState.SendEnded).result)
    }

    // RCL-070

    @Test
    @Tag("RCL-070")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL070_UnitPositive_everyProgressStateIsShownAndOpenOnesAreReadAgainCalmly(@TempDir dir: Path) {
        SubmitTexts.ALL_PROGRESS.forEachIndexed { index, progress ->
            val fake = FakeSendPort().apply { this.progress = progress }
            val screen = screen(dir.resolve("p$index"), fake)
            screen.toSendReady()
            screen.controller.send()

            assertEquals(progress, (screen.state as SubmitState.SendEnded).progress)
            val again = progress.isOpen || progress == BotProgress.Merged
            assertEquals(if (again) listOf(30L) else emptyList<Long>(), screen.clock.open.map { it.seconds }, progress.toString())
        }
        val fake = FakeSendPort()
        val screen = screen(dir.resolve("loop"), fake)
        screen.toSendReady()
        screen.controller.send()
        fake.progress = BotProgress.WaitingForModerator
        screen.clock.fire()
        assertEquals(BotProgress.WaitingForModerator, (screen.state as SubmitState.SendEnded).progress)
        assertEquals(Triple(7, "MyBot", "1.0.0"), fake.progressCalls.last())
        fake.progress = BotProgress.InCatalog("1.0.0")
        screen.clock.fire()
        assertEquals(BotProgress.InCatalog("1.0.0"), (screen.state as SubmitState.SendEnded).progress)
        assertEquals(emptyList<ManualScheduler.Task>(), screen.clock.open, "nothing more to wait for")
    }

    @Test
    @Tag("RCL-070")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL070_UnitNegative_checkNowReadsAtOnceAndClosingStopsTheReading(@TempDir dir: Path) {
        val fake = FakeSendPort()
        val screen = screen(dir, fake)
        screen.toSendReady()
        screen.controller.send()
        val calls = fake.progressCalls.size

        fake.progress = BotProgress.Accepted
        screen.controller.checkNow()
        assertEquals(calls + 1, fake.progressCalls.size)
        assertEquals(BotProgress.Accepted, (screen.state as SubmitState.SendEnded).progress)
        assertEquals(1, screen.clock.open.size, "one waiting read, not two")

        screen.controller.close()
        assertEquals(emptyList<ManualScheduler.Task>(), screen.clock.open)
        // Checks, review, publication and visibility are separate states, never one word.
        assertEquals(SubmitTexts.ALL_PROGRESS.size, SubmitTexts.ALL_PROGRESS.map { SubmitTexts.forProgress(it) }.toSet().size)
    }

    // RCL-071 and RCL-072: the library behind the last steps, with a real saved sign-in and fakes of what it asks GitHub.

    private class Sending : BotSending {
        var readiness: SubmissionReadiness = SubmissionReadiness.Ready("alice", "alice/rumble-bots-test")
        val submitted = mutableListOf<Boolean>()
        override fun checkReadiness(tokens: GitHubTokens) = readiness
        var refuseFirstLook = false
        val lookTokens = mutableListOf<String>()
        override fun compareLaunchers(tokens: GitHubTokens, submission: StagedSubmission): LauncherState {
            lookTokens += tokens.accessToken
            if (refuseFirstLook && lookTokens.size == 1) throw SignInRefused()
            return LauncherState.Same
        }
        override fun submit(tokens: GitHubTokens, submission: StagedSubmission, authorCertifiesLicense: Boolean): SubmissionResult {
            submitted += authorCertifiesLicense
            return SubmissionResult.Submitted("https://github.com/x/y/pull/9", 9, "submit/MyBot-1.0.0", SubmissionOutcome.OPENED)
        }
    }

    private class Reading(var sent: SentBot? = null, var progress: BotProgress = BotProgress.WaitingForModerator, var findFails: Boolean = false, var readFails: Boolean = false) : BotProgressReading {
        override fun find(tokens: GitHubTokens, account: String, name: String): SentBot? {
            if (findFails) throw java.io.IOException("GitHub cannot be reached.")
            return sent
        }
        override fun read(tokens: GitHubTokens, pullRequestNumber: Int, name: String, version: String, catalog: Catalog?): BotProgress {
            if (readFails) throw java.io.IOException("GitHub cannot be reached.")
            return progress
        }
    }

    private class Catalogs(val text: String?) : CatalogSource {
        override fun catalogText(commit: String): String = text ?: throw CatalogUnreadable("The catalog cannot be read.")
        override fun archive(commit: String) = throw UnsupportedOperationException()
    }

    private class HeadApi : GitHubApi {
        override fun get(path: String, token: String?) = ApiResponse(200, "{\"sha\":\"" + "a".repeat(40) + "\"}")
        override fun postJson(path: String, token: String, json: String) = throw UnsupportedOperationException()
        override fun patchJson(path: String, token: String, json: String) = throw UnsupportedOperationException()
    }

    private fun catalog(vararg bots: Triple<String, String, String>): String =
        bots.joinToString(prefix = "{\"schemaVersion\":1,\"generatedAt\":\"x\",\"commit\":\"c\",\"bots\":[", postfix = "]}") { (name, version, hash) ->
            "{\"name\":\"$name\",\"version\":\"$version\",\"platform\":\"JVM\",\"path\":\"bots/java/$name\",\"sourceHash\":\"$hash\",\"owner\":\"alice\",\"authors\":[\"A\"],\"addedAt\":\"x\",\"status\":\"active\"}"
        }

    private fun submission(dir: Path, hash: String = "sha256:aaa"): StagedSubmission =
        StagedSubmission("MyBot", "1.0.0", "java", "MIT", listOf("A"), false, dir, dir, emptyList(), emptyList(), hash)

    private fun port(dir: Path, sending: Sending = Sending(), reading: Reading = Reading(), catalogText: String? = catalog(), refreshes: Int = 6): LibrarySendPort {
        val env = RumbleEnvironments.TEST
        SignInStore(env, dir).save("refresh-0", Instant.now().plusSeconds(86_400))
        val clock = MutableClock(Instant.now())
        val answers = (1..refreshes).map { tokenAnswer("access-$it", "refresh-$it") }.toTypedArray()
        val flow = SignInFlow(env, GitHubSignIn(env, FakeTransport(*answers), clock), SignInStore(env, dir), FakeRumbleGitHub(), clock)
        return LibrarySendPort(env, flow, HeadApi(), sending, reading, Catalogs(catalogText))
    }

    @Test
    @Tag("RCL-071")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL071_UnitPositive_theChecksumTellsAVersionThatIsAlreadyInTheRumbleFromOneWithOtherContentAndFromANewOne(@TempDir dir: Path) {
        val same = port(dir.resolve("a"), catalogText = catalog(Triple("MyBot", "1.0.0", "sha256:aaa"))).prepare(submission(dir, "sha256:aaa"), "alice")
        assertEquals(ContentState.ALREADY_IN_RUMBLE, (same as SendPrepared.Ready).content)

        val other = port(dir.resolve("b"), catalogText = catalog(Triple("MyBot", "1.0.0", "sha256:bbb"))).prepare(submission(dir, "sha256:aaa"), "alice")
        assertEquals(ContentState.DIFFERENT_CONTENT, (other as SendPrepared.Ready).content)

        val fresh = port(dir.resolve("c"), catalogText = catalog(Triple("MyBot", "0.9.0", "sha256:aaa"), Triple("Other", "1.0.0", "sha256:aaa"))).prepare(submission(dir, "sha256:aaa"), "alice")
        assertEquals(ContentState.NEW, (fresh as SendPrepared.Ready).content, "another version or another bot is not the same")
    }

    @Test
    @Tag("RCL-071")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL071_UnitNegative_anUnreadableCatalogComparesNothingAndNothingIsSavedOnTheComputer(@TempDir dir: Path) {
        val port = port(dir.resolve("state"), catalogText = null)
        fun files(): Set<String> = Files.walk(dir).use { s -> s.filter { Files.isRegularFile(it) }.map { dir.relativize(it).toString() }.collect(java.util.stream.Collectors.toSet()) }
        val before = files()

        val prepared = port.prepare(submission(dir), "alice") as SendPrepared.Ready

        assertEquals(ContentState.NOT_CHECKED, prepared.content)
        // Only the sign-in file and its lock, which renewing the sign-in already uses, may appear: nothing is written to remember a bot or a hash.
        val added = files() - before
        assertTrue(added.all { it.endsWith("signin.lock") || it.endsWith("signin.bin") }, "$added")
        assertEquals(before.filter { !it.endsWith("signin.bin") }.toSet(), (files() - added).filter { !it.endsWith("signin.bin") }.toSet())
    }

    @Test
    @Tag("RCL-072")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL072_UnitPositive_anOpenPullRequestOfTheAccountIsFoundAgainAndShowsWhereItStands(@TempDir dir: Path) {
        val reading = Reading(sent = SentBot(9, "1.0.0"), progress = BotProgress.WaitingForModerator)

        val prepared = port(dir, reading = reading).prepare(submission(dir), "alice") as SendPrepared.Ready

        assertEquals(ExistingPullRequest(9, "1.0.0", BotProgress.WaitingForModerator), prepared.existing)
        // The screen shows it and offers to update it with the new files.
        val fake = FakeSendPort().apply { this.prepared = prepared }
        val screen = screen(dir.resolve("screen"), fake)
        val ready = screen.toSendReady()
        assertEquals(prepared.existing, ready.ready.existing)
        assertTrue(ready.canSend)
        assertEquals(listOf(30L), screen.clock.open.map { it.seconds }, "where it stands is read again calmly")
        fake.progress = BotProgress.Accepted
        screen.controller.checkNow()
        assertEquals(BotProgress.Accepted, (screen.state as SubmitState.SendReady).ready.existing?.progress)
    }

    @Test
    @Tag("RCL-072")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL072_UnitNegative_aClosedOrMergedPullRequestOrNoneLeadsToANewSendAndAFailedLookStopsNothing(@TempDir dir: Path) {
        listOf(BotProgress.Closed, BotProgress.Merged, BotProgress.InCatalog("1.0.0")).forEachIndexed { index, progress ->
            val reading = Reading(sent = SentBot(9, "1.0.0"), progress = progress)
            val prepared = port(dir.resolve("c$index"), reading = reading).prepare(submission(dir), "alice") as SendPrepared.Ready
            assertEquals(null, prepared.existing, progress.toString())
        }
        assertEquals(null, (port(dir.resolve("none"), reading = Reading()).prepare(submission(dir), "alice") as SendPrepared.Ready).existing)
        // A look that fails is advice that failed: the send is still possible.
        val failing = port(dir.resolve("fail"), reading = Reading(findFails = true)).prepare(submission(dir), "alice")
        assertEquals(null, (failing as SendPrepared.Ready).existing)
    }

    @Test
    @Tag("RCL-068")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL068_UnitNegative_theLibraryPassesTheAuthorsConfirmationOnAndNeverMakesOneUp(@TempDir dir: Path) {
        val sending = Sending()
        val port = port(dir, sending)

        port.send(submission(dir), authorCertifiesLicense = false)
        port.send(submission(dir), authorCertifiesLicense = true)

        assertEquals(listOf(false, true), sending.submitted)
    }

    // Found by the review of the change

    /** A send port that lets the test act while a call is running, on the same thread as the screen. */
    private class Hooked(val inner: FakeSendPort = FakeSendPort(), var onSend: () -> Unit = {}, var progressFails: Boolean = false) : SendPort {
        override fun readiness() = inner.readiness()
        override fun prepare(submission: StagedSubmission, account: String) = inner.prepare(submission, account)
        override fun send(submission: StagedSubmission, authorCertifiesLicense: Boolean): SubmissionResult {
            onSend()
            return inner.send(submission, authorCertifiesLicense)
        }
        override fun progress(number: Int, name: String, version: String): BotProgress {
            if (progressFails) throw IllegalStateException("one look failed")
            return inner.progress(number, name, version)
        }
    }

    @Test
    @Tag("RCL-067")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL067_UnitNegative_goingBackDuringASendOrAfterASentPullRequestChangesNothingAndLosesNothing(@TempDir dir: Path) {
        val hooked = Hooked()
        val screen = Screen(dir, hooked, direct, direct)
        hooked.onSend = { screen.controller.back() }
        screen.toSendReady()

        screen.controller.send()

        val ended = screen.state as SubmitState.SendEnded
        assertInstanceOf(SubmissionResult.Submitted::class.java, ended.result, "the result of the send was not thrown away")
        assertEquals(BotProgress.WaitingForChecks, ended.progress)
        assertEquals(1, screen.clock.open.size, "the sent pull request is still followed")
        screen.controller.back()
        assertEquals(1, screen.clock.open.size, "going back from a sent pull request does not stop the following")
        assertInstanceOf(SubmitState.SendEnded::class.java, screen.state)
    }

    @Test
    @Tag("RCL-070")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL070_UnitNegative_oneFailedReadOfAnOpenPullRequestDoesNotEndTheReading(@TempDir dir: Path) {
        val hooked = Hooked(FakeSendPort().apply { prepared = SendPrepared.Ready(LauncherState.Same, ContentState.NEW, ExistingPullRequest(5, "1.0.0", BotProgress.WaitingForChecks)) })
        val screen = Screen(dir, hooked, direct, direct)
        screen.toSendReady()
        assertEquals(1, screen.clock.open.size)

        hooked.progressFails = true
        screen.clock.fire()
        assertEquals(1, screen.clock.open.size, "it looks again after a failed look")

        hooked.progressFails = false
        hooked.inner.progress = BotProgress.Closed
        screen.clock.fire()
        assertEquals(BotProgress.Closed, ((screen.state) as SubmitState.SendReady).ready.existing?.progress)
        assertEquals(emptyList<ManualScheduler.Task>(), screen.clock.open, "and stops when it cannot change any more")
    }

    @Test
    @Tag("RCL-072")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL072_UnitNegative_aFailedReadOfTheOpenPullRequestStopsNothingAndAnExpiredKeptSignInIsRenewedOnce(@TempDir dir: Path) {
        val reading = Reading(sent = SentBot(9, "1.0.0"), readFails = true)
        val prepared = port(dir.resolve("read"), reading = reading).prepare(submission(dir), "alice")
        assertEquals(null, (prepared as SendPrepared.Ready).existing, "the send is still possible")

        val sending = Sending().apply { refuseFirstLook = true }
        val renewed = port(dir.resolve("renew"), sending).prepare(submission(dir), "alice")
        assertInstanceOf(SendPrepared.Ready::class.java, renewed, "the saved sign-in was renewed instead of asking the person")
        assertEquals(2, sending.lookTokens.size)
        assertNotEquals(sending.lookTokens[0], sending.lookTokens[1])
    }
}
