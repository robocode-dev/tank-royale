package dev.robocode.tankroyale.rumble

import dev.robocode.tankroyale.botproject.NotIncluded
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path

class RumbleBotSubmissionTest {

    private val env = RumbleEnvironments.TEST
    private val folder = "bots/java/Scout"

    private fun world(configure: FakeBotGitHub.() -> Unit = {}): FakeBotGitHub {
        val github = FakeBotGitHub()
        github.seedUpstream(
            mapOf(
                "README.md" to ("100644" to "rumble".toByteArray()),
                "bots/java/Other/Other.json" to ("100644" to "{}".toByteArray()),
            )
        )
        github.configure()
        return github
    }

    private fun send(github: FakeBotGitHub, bot: dev.robocode.tankroyale.botproject.StagedSubmission, certified: Boolean = true) =
        BotSubmission(env, github).submit(tokens(), bot, certified)

    private fun submitted(result: SubmissionResult) = assertInstanceOf(SubmissionResult.Submitted::class.java, result)

    // RBC-025

    @Test
    @Tag("RBC-025")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC025_UnitPositive_anAccountWithACopyAndTheAppInstalledIsReadyWithoutChangingAnything() {
        listOf(FakeBotGitHub.Install.ALL, FakeBotGitHub.Install.SELECTED_WITH_COPY).forEach { install ->
            val github = world { this.install = install }

            val readiness = BotSubmission(env, github).checkReadiness(tokens())

            assertEquals(SubmissionReadiness.Ready("alice", "alice/rumble-bots-test"), readiness)
            assertTrue(github.writes.isEmpty())
        }
    }

    @Test
    @Tag("RBC-025")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC025_UnitNegative_theAnswerSaysWhichStepIsMissingAndSendingDoesNothingUntilItIsDone(@TempDir dir: Path) {
        val noCopy = world { copy = FakeBotGitHub.Copy.NONE }
        assertEquals(
            SubmissionReadiness.NoCopy("alice", "https://github.com/robocode-dev/rumble-bots-test/fork"),
            BotSubmission(env, noCopy).checkReadiness(tokens()),
        )

        listOf(FakeBotGitHub.Copy.PLAIN_REPOSITORY, FakeBotGitHub.Copy.FORK_OF_SOMETHING_ELSE).forEach { kind ->
            val other = world { copy = kind }
            assertEquals(
                SubmissionReadiness.NotRumblesRepository("alice", "https://github.com/alice/rumble-bots-test"),
                BotSubmission(env, other).checkReadiness(tokens()),
            )
        }

        listOf(FakeBotGitHub.Install.NONE, FakeBotGitHub.Install.SELECTED_WITHOUT_COPY).forEach { install ->
            val missing = world { this.install = install }
            assertEquals(
                SubmissionReadiness.AppNotInstalled("alice", "https://github.com/apps/tank-royale-rumble-contrib-test/installations/new"),
                BotSubmission(env, missing).checkReadiness(tokens()),
            )
        }

        assertTrue(BotSubmission(RumbleEnvironments.PUBLIC, world()).checkReadiness(tokens()) is SubmissionReadiness.Failed)

        val unready = world { copy = FakeBotGitHub.Copy.NONE }
        val result = send(unready, stagedBot(dir))
        assertInstanceOf(SubmissionResult.SetupNeeded::class.java, result)
        assertTrue(unready.writes.isEmpty())
    }

    // RBC-026

    @Test
    @Tag("RBC-026")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC026_UnitPositive_oneCommitFromTheRumblesHeadHoldsTheStagedFilesWithTheLauncherExecutableAndTheOldFolderReplaced(@TempDir dir: Path) {
        val bot = stagedBot(dir)
        val github = world()

        val result = submitted(send(github, bot))

        assertEquals(SubmissionOutcome.OPENED, result.outcome)
        assertEquals("submit/Scout-1.0.3", result.branch)
        val committed = github.filesOn(result.branch, folder)
        assertEquals(
            mapOf(
                "$folder/Scout.json" to FakeBotGitHub.Entry("100644", FakeBotGitHub.blobId("{}\n".toByteArray())),
                "$folder/Scout.sh" to FakeBotGitHub.Entry("100755", FakeBotGitHub.blobId("#!/bin/sh\n".toByteArray())),
                "$folder/Scout.java" to FakeBotGitHub.Entry("100644", FakeBotGitHub.blobId("class Scout {}\n".toByteArray())),
            ),
            committed,
        )
        assertEquals(listOf(github.upstreamMain()), github.parentsOf(github.refs.getValue(github.fk).getValue(result.branch)))
        assertEquals(1, github.filesOn(result.branch, "bots/java/Other").size)
    }

    @Test
    @Tag("RBC-026")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC026_UnitPositive_anUpdateRemovesTheFilesTheAuthorRemovedAndTouchesNothingElse(@TempDir dir: Path) {
        val bot = stagedBot(dir)
        val github = world()
        github.seedUpstream(
            mapOf(
                "README.md" to ("100644" to "rumble".toByteArray()),
                "bots/java/Other/Other.json" to ("100644" to "{}".toByteArray()),
                "$folder/Scout.json" to ("100644" to "old".toByteArray()),
                "$folder/Scout.sh" to ("100755" to "old".toByteArray()),
                "$folder/Old.java" to ("100644" to "removed by the author".toByteArray()),
            )
        )

        val result = submitted(send(github, bot))

        val committed = github.filesOn(result.branch, folder)
        assertEquals(setOf("$folder/Scout.json", "$folder/Scout.sh", "$folder/Scout.java"), committed.keys)
        assertEquals("Update Scout to 1.0.3", github.pulls.single().title)
        assertEquals(1, github.filesOn(result.branch, "bots/java/Other").size)
    }

    @Test
    @Tag("RBC-026")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC026_UnitNegative_aFileGitHubStoresDifferentlyStopsTheSendingBeforeAnyBranchOrPullRequestExists(@TempDir dir: Path) {
        listOf<FakeBotGitHub.() -> Unit>({ corruptBlobIds = true }, { dropModes = true }).forEach { fault ->
            val github = world(fault)

            val result = send(github, stagedBot(dir))

            assertInstanceOf(SubmissionResult.Failed::class.java, result)
            assertFalse(github.refs.getValue(github.fk).keys.any { it.startsWith("submit/") })
            assertTrue(github.writes.none { it.path.endsWith("/git/refs") || it.path.endsWith("/pulls") })
            assertTrue(github.pulls.isEmpty())
        }
    }

    // RBC-027

    @Test
    @Tag("RBC-027")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC027_UnitPositive_theTextStatesTheFactsAndNeverNamesFilesThatWereLeftOut(@TempDir dir: Path) {
        val bot = stagedBot(dir, notIncluded = listOf(NotIncluded("private-diary-notes", 2, "not a bot file"), NotIncluded("my-secret-plans.txt", 1, "unrelated")))
        val github = world()

        val result = submitted(send(github, bot))

        val pull = github.pulls.single()
        assertEquals("Add Scout 1.0.3", pull.title)
        assertEquals("alice", pull.owner)
        assertTrue(pull.body.startsWith("## Bot submission: `Scout` `1.0.3`"))
        listOf(
            "confirmed that they can publish this source", "- Folder: `bots/java/Scout`", "- License: `MIT`", "- Files: 3",
            "- Content hash: `${bot.treeHash}`", "1 file converted to Unix line endings", "`Scout.sh` set as executable",
            "Not checked by the program: running the bot",
        ).forEach { assertTrue(pull.body.contains(it), "Missing: $it") }
        listOf("private-diary-notes", "my-secret-plans").forEach { secret ->
            assertFalse(pull.title.contains(secret) || pull.body.contains(secret) || result.branch.contains(secret))
            assertTrue(github.requests.none { it.path.contains(secret) || it.body.orEmpty().contains(secret) })
        }
        val posted = github.requests.last { it.method == "POST" && it.path.endsWith("/pulls") }.body!!
        assertTrue(posted.contains("\"head\":\"alice:submit/Scout-1.0.3\""))
        assertTrue(posted.contains("\"base\":\"main\""))
    }

    @Test
    @Tag("RBC-027")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC027_UnitNegative_aNameOrVersionOutsideTheSafeCharactersIsRefusedBeforeAnythingIsSent(@TempDir dir: Path) {
        listOf("../x", "a b", "a`b", "x..y", "-start", ".hidden", "name.lock", "@mention", "").forEach { bad ->
            val github = world()
            val byName = stagedBot(dir.resolve("n"), name = "Scout").let { good ->
                dev.robocode.tankroyale.botproject.StagedSubmission(
                    bad, good.version, good.language, good.license, good.authors, false, good.stagingFolder, good.botFolder, good.files, good.notIncluded, good.treeHash,
                )
            }
            val byVersion = stagedBot(dir.resolve("v"), version = bad.ifEmpty { null })

            listOf(byName, byVersion).forEach { bot ->
                assertInstanceOf(SubmissionResult.Refused::class.java, send(github, bot), "Accepted '$bad'")
            }
            assertTrue(github.requests.isEmpty())
        }
        val github = world()
        assertInstanceOf(SubmissionResult.Refused::class.java, send(github, stagedBot(dir.resolve("none"), version = null)))
        assertTrue(github.requests.isEmpty())
    }

    @Test
    @Tag("RBC-027")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC027_UnitNegative_aLicenseTextCannotBreakOutOfItsCodeSpan(@TempDir dir: Path) {
        val github = world()

        send(github, stagedBot(dir, license = "MIT`\n\n@everyone [x](http://evil)"))

        val body = github.pulls.single().body
        val licenseLine = body.lines().single { it.startsWith("- License:") }
        assertEquals(2, licenseLine.count { it == '`' })
        assertTrue(body.lines().none { it.startsWith("@everyone") })
    }

    // RBC-028

    @Test
    @Tag("RBC-028")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC028_UnitNegative_withoutTheAuthorsConfirmationNothingIsSent(@TempDir dir: Path) {
        val github = world()

        val result = send(github, stagedBot(dir), certified = false)

        assertTrue(result is SubmissionResult.Refused)
        assertTrue(github.requests.isEmpty())
    }

    // RBC-029

    @Test
    @Tag("RBC-029")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC029_UnitPositive_sendingTheSameFilesAgainReturnsTheOpenPullRequestUnchanged(@TempDir dir: Path) {
        val bot = stagedBot(dir)
        val github = world()
        val first = submitted(send(github, bot))
        val writesBefore = github.writes.size

        val second = submitted(send(github, bot))

        assertEquals(SubmissionOutcome.UNCHANGED, second.outcome)
        assertEquals(first.pullRequestUrl, second.pullRequestUrl)
        assertEquals(writesBefore, github.writes.size)
        assertEquals(1, github.pulls.size)
    }

    @Test
    @Tag("RBC-029")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC029_UnitPositive_otherFilesGetOneNewCommitOnTheSameBranchAndNoSecondPullRequest(@TempDir dir: Path) {
        val github = world()
        val first = submitted(send(github, stagedBot(dir.resolve("one"))))
        val firstHead = github.refs.getValue(github.fk).getValue(first.branch)
        val commitsBefore = github.commitCount()
        val fixed = stagedBot(
            dir.resolve("two"), version = "1.0.4",
            files = mapOf("Scout.json" to "{}\n", "Scout.sh" to "#!/bin/sh\n", "Scout.java" to "class Scout { int fixed; }\n"),
        )

        val second = submitted(send(github, fixed))

        assertEquals(SubmissionOutcome.UPDATED, second.outcome)
        assertEquals(first.branch, second.branch)
        assertEquals(first.pullRequestNumber, second.pullRequestNumber)
        assertEquals(1, github.pulls.size)
        assertEquals(commitsBefore + 1, github.commitCount())
        val newHead = github.refs.getValue(github.fk).getValue(first.branch)
        assertEquals(listOf(firstHead), github.parentsOf(newHead))
        assertEquals(FakeBotGitHub.blobId("class Scout { int fixed; }\n".toByteArray()), github.filesOn(first.branch, folder).getValue("$folder/Scout.java").sha)
        assertEquals("Add Scout 1.0.4", github.pulls.single().title)
        assertTrue(github.pulls.single().body.contains("`1.0.4`"))
        assertTrue(github.writes.none { it.path.endsWith("/pulls") && it !== github.writes.first { w -> w.path.endsWith("/pulls") } })
    }

    @Test
    @Tag("RBC-029")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC029_UnitPositive_aBranchLeftByAnInterruptedSendingIsUsedWithoutANewCommit(@TempDir dir: Path) {
        val bot = stagedBot(dir)
        val github = world { pullStatus = 422 }
        assertInstanceOf(SubmissionResult.Failed::class.java, send(github, bot))
        val commitsBefore = github.commitCount()
        github.pullStatus = 201

        val result = submitted(send(github, bot))

        assertEquals(SubmissionOutcome.RESUMED, result.outcome)
        assertEquals("submit/Scout-1.0.3", result.branch)
        assertEquals(commitsBefore, github.commitCount())
        assertEquals(1, github.pulls.size)
    }

    @Test
    @Tag("RBC-029")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC029_UnitPositive_aNameUsedByAClosedPullRequestIsFollowedByANumberAndTheOldBranchStays(@TempDir dir: Path) {
        val github = world()
        val first = submitted(send(github, stagedBot(dir.resolve("one"))))
        val oldHead = github.refs.getValue(github.fk).getValue(first.branch)
        github.pulls.single().state = "closed"
        val again = stagedBot(
            dir.resolve("two"),
            files = mapOf("Scout.json" to "{}\n", "Scout.sh" to "#!/bin/sh\n", "Scout.java" to "class Scout { int second; }\n"),
        )

        val second = submitted(send(github, again))

        assertEquals(SubmissionOutcome.OPENED, second.outcome)
        assertEquals("submit/Scout-1.0.3-2", second.branch)
        assertEquals(2, github.pulls.size)
        assertEquals(oldHead, github.refs.getValue(github.fk).getValue(first.branch))
        assertNotEquals(first.pullRequestNumber, second.pullRequestNumber)
    }

    @Test
    @Tag("RBC-029")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC029_UnitNegative_anotherBotWhoseNameStartsWithThisOnesIsNotMistakenForItAndNothingIsForced(@TempDir dir: Path) {
        val github = world()
        github.addPull(1, "open", "submit/Scout-Master-2.0", listOf("bots/java/Scout-Master/Scout-Master.json"))

        val result = submitted(send(github, stagedBot(dir)))

        assertEquals(SubmissionOutcome.OPENED, result.outcome)
        assertEquals(2, github.pulls.size)
        assertEquals("title", github.pulls.first().title)

        val update = stagedBot(
            dir.resolve("two"), version = "1.0.4",
            files = mapOf("Scout.json" to "{}\n", "Scout.sh" to "#!/bin/sh\n", "Scout.java" to "class Scout { int more; }\n"),
        )
        submitted(send(github, update))
        github.writes.filter { it.method == "PATCH" && it.path.contains("/git/refs/") }.forEach { assertTrue(it.body!!.contains("\"force\":false")) }
        assertTrue(github.writes.none { it.body.orEmpty().contains("\"force\":true") })
    }

    // RBC-030

    private fun failures(dir: Path): List<Pair<String, SubmissionResult>> {
        fun run(configure: FakeBotGitHub.() -> Unit, label: String): Pair<String, SubmissionResult> {
            val github = world(configure)
            val result = send(github, stagedBot(dir.resolve(label)))
            assertTrue(github.pulls.isEmpty(), "$label opened a pull request")
            return label to result
        }
        return listOf(
            run({ tokenAccepted = false }, "refused"),
            run({ offline = true }, "offline"),
            run({ rateLimited = true }, "limited"),
            run({ treeStatus = 422 }, "tree"),
            run({ pullStatus = 422 }, "pull"),
        )
    }

    @Test
    @Tag("RBC-030")
    @Tag("Unit")
    @Tag("Positive")
    fun testRBC030_UnitPositive_everyFailureSaysWhatHappenedInPlainWordsAndNoPullRequestIsReported(@TempDir dir: Path) {
        val results = failures(dir).toMap()

        assertTrue((results.getValue("refused") as SubmissionResult.SignInNeeded).message.contains("Sign in again"))
        assertTrue((results.getValue("offline") as SubmissionResult.Failed).message.contains("cannot be reached"))
        assertTrue((results.getValue("limited") as SubmissionResult.Failed).message.contains("limiting"))
        assertTrue((results.getValue("tree") as SubmissionResult.Failed).message.contains("update it on GitHub"))
        assertTrue(results.getValue("pull") is SubmissionResult.Failed)
        assertTrue(results.values.none { it is SubmissionResult.Submitted })
    }

    @Test
    @Tag("RBC-030")
    @Tag("Unit")
    @Tag("Negative")
    fun testRBC030_UnitNegative_theTokenAppearsInNoAddressBodyResultOrConsoleOutput(@TempDir dir: Path) {
        val out = ByteArrayOutputStream()
        val err = ByteArrayOutputStream()
        val originalOut = System.out
        val originalErr = System.err
        val seen = mutableListOf<String>()
        try {
            System.setOut(PrintStream(out, true))
            System.setErr(PrintStream(err, true))
            failures(dir).forEach { seen += it.second.toString() }
            val github = world()
            seen += send(github, stagedBot(dir.resolve("ok"))).toString()
            seen += BotSubmission(env, github).checkReadiness(tokens()).toString()
            github.requests.forEach { request ->
                seen += request.path
                seen += request.body.orEmpty()
                assertEquals(SECRET_TOKEN, request.token)
            }
        } finally {
            System.setOut(originalOut)
            System.setErr(originalErr)
        }

        assertTrue(seen.size > 10)
        seen.forEach { assertFalse(it.contains(SECRET_TOKEN)) }
        assertEquals("", out.toString())
        assertEquals("", err.toString())
        assertTrue(Files.exists(dir))
    }

    @Test
    @Tag("RCL-097")
    @Tag("Unit")
    @Tag("Positive")
    fun testRCL097_UnitPositive_aCopyThatLacksAChangeToTheRumblesWorkflowsIsAskedToBeSyncedAndOtherwiseGoesOn() {
        val behind = world { upstreamAhead = listOf(".github/workflows/validate.yml", "templates/README.md") }
        assertEquals(SubmissionReadiness.CopyBehind("alice", "https://github.com/alice/rumble-bots-test"), BotSubmission(env, behind).checkReadiness(tokens()))
        assertTrue(behind.writes.isEmpty(), "nothing is changed on GitHub")
        assertTrue(RumbleLinks.isGitHubPage("https://github.com/alice/rumble-bots-test"))

        behind.upstreamAhead = emptyList()
        assertEquals(SubmissionReadiness.Ready("alice", "alice/rumble-bots-test"), BotSubmission(env, behind).checkReadiness(tokens()), "synced, it goes on")

        listOf(listOf("bots/java/Other/Other.json"), null, List(300) { "bots/x/$it" } + ".github/workflows/validate.yml").forEach { ahead ->
            val github = world { upstreamAhead = ahead }
            assertEquals(SubmissionReadiness.Ready("alice", "alice/rumble-bots-test"), BotSubmission(env, github).checkReadiness(tokens()),
                "a copy that lacks only other bots, or that the comparison cannot judge, goes on at once: $ahead")
        }
    }

    @Test
    @Tag("RCL-097")
    @Tag("Unit")
    @Tag("Negative")
    fun testRCL097_UnitNegative_aRefusedBranchAsksForASyncedCopyAndEveryOtherRefusalNamesItsStepAndGitHubsMessage(@TempDir dir: Path) {
        val refused = world { refStatus = 403 }
        val result = send(refused, stagedBot(dir))
        assertEquals(SubmissionResult.SetupNeeded(SubmissionReadiness.CopyBehind("alice", "https://github.com/alice/rumble-bots-test")), result)
        assertTrue(refused.pulls.isEmpty(), "no pull request is opened")

        val tree = world { treeStatus = 403 }
        val failed = assertInstanceOf(SubmissionResult.Failed::class.java, send(tree, stagedBot(dir.resolve("b"))))
        assertTrue(failed.message.contains("writing the files to your copy") && failed.message.contains("GitHub said \"x\""), failed.message)
        assertFalse(failed.message.contains("installed"), "a refusal no longer points at the App installation")

        val limited = world { rateLimited = true }
        val readiness = assertInstanceOf(SubmissionReadiness.Failed::class.java, BotSubmission(env, limited).checkReadiness(tokens()))
        assertTrue(readiness.message.contains("limiting requests"), readiness.message)
    }
}
