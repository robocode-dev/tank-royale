package dev.robocode.tankroyale.rumble.cli

import dev.robocode.tankroyale.botproject.TreeHash
import dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION
import dev.robocode.tankroyale.rumble.ApiResponse
import dev.robocode.tankroyale.rumble.AuthResponse
import dev.robocode.tankroyale.rumble.CatalogSource
import dev.robocode.tankroyale.rumble.GitHubApi
import dev.robocode.tankroyale.rumble.GitHubAuthTransport
import dev.robocode.tankroyale.rumble.RankedJournal
import dev.robocode.tankroyale.rumble.RecordState
import dev.robocode.tankroyale.rumble.RegistrationRecord
import dev.robocode.tankroyale.rumble.ResultDataSource
import dev.robocode.tankroyale.rumble.RumbleEnvironments
import dev.robocode.tankroyale.rumble.SentBatches
import dev.robocode.tankroyale.rumble.SessionControlRequest
import dev.robocode.tankroyale.rumble.SessionFolder
import dev.robocode.tankroyale.rumble.SessionLook
import dev.robocode.tankroyale.rumble.SessionOwner
import dev.robocode.tankroyale.rumble.SessionStatus
import dev.robocode.tankroyale.rumble.SignInStore
import dev.robocode.tankroyale.rumble.WorkerCommand
import dev.robocode.tankroyale.rumble.WorkerLimits
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.PrintStream
import java.nio.file.Files
import java.nio.file.Path
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.Base64
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class RumbleCliContributeTest {

    private val env = RumbleEnvironments.TEST
    private val commit = "c".repeat(40)
    private val data = "/repos/robocode-dev/rumble-data-test"
    private val java = File(System.getProperty("java.home"), "bin/java").absolutePath

    private fun files(name: String): Map<String, ByteArray> = mapOf(
        "$name.json" to "{ \"name\": \"$name\", \"version\": \"1.0\" }".toByteArray(),
        "$name.sh" to "#!/bin/sh\n".toByteArray(),
    )

    private val names = listOf("Alpha", "Beta", "Gamma")

    /** The catalog, with a Python bot "Snake" when [python] is true. */
    private fun catalogText(python: Boolean) = if (!python) catalogText else catalogText.replace(
        "\"status\": \"retired\" } ] }",
        "\"status\": \"retired\" },\n{ \"name\": \"Snake\", \"version\": \"1.0\", \"platform\": \"Python\", \"path\": \"bots/python/Snake\", " +
            "\"sourceHash\": \"${TreeHash.of(files("Snake"))}\", \"owner\": \"owner\", \"authors\": [\"Author\"], \"addedAt\": \"2026-01-01\", \"status\": \"active\" } ] }",
    )

    private val catalogText = """
        { "schemaVersion": 1, "generatedAt": "2026-11-16T10:00:00Z", "commit": "$commit", "bots": [
        ${names.joinToString(",\n") { """{ "name": "$it", "version": "1.0", "platform": "JVM", "path": "bots/java/$it", "sourceHash": "${TreeHash.of(files(it))}",
            "owner": "owner", "authors": ["Author"], "addedAt": "2026-01-01", "status": "active" }""" }},
        { "name": "Old", "version": "1.0", "platform": "JVM", "path": "bots/java/Old", "sourceHash": "sha256:${"0".repeat(64)}",
            "owner": "owner", "authors": ["Author"], "addedAt": "2026-01-01", "status": "retired" } ] }
    """.trimIndent()

    private inner class Catalog(var readable: Boolean = true, var python: Boolean = false) : CatalogSource {
        override fun catalogText(commit: String): String = if (readable) catalogText(python) else throw IOException("The catalog could not be fetched.")
        override fun fileText(commit: String, path: String): String? = null
        override fun archive(commit: String): InputStream {
            val bytes = ByteArrayOutputStream()
            ZipOutputStream(bytes).use { zip ->
                for (name in names) for ((path, content) in files(name)) {
                    zip.putNextEntry(ZipEntry("rumble-bots-test-$commit/bots/java/$name/$path"))
                    zip.write(content)
                    zip.closeEntry()
                }
                for ((path, content) in files("Snake")) {
                    zip.putNextEntry(ZipEntry("rumble-bots-test-$commit/bots/python/Snake/$path"))
                    zip.write(content)
                    zip.closeEntry()
                }
            }
            return ByteArrayInputStream(bytes.toByteArray())
        }
    }

    private class Data(var behavior: Int = CURRENT_BEHAVIOR_VERSION, var readable: Boolean = true) : ResultDataSource {
        override fun fileText(path: String): String? {
            if (!readable) throw IOException("The pin could not be fetched.")
            if (path != "engine.json") return null
            return """{ "schemaVersion": 1, "behaviorVersion": $behavior, "gameTypes": {
                "1v1": { "rounds": 3, "battlefield": [800, 600], "participants": 2 },
                "twinduel": { "rounds": 3, "battlefield": [800, 800], "participants": 4 },
                "melee": { "rounds": 3, "battlefield": [1000, 1000], "participants": 10 } } }"""
        }
    }

    /** GitHub's API: the account, its registration, the bot repository's head, and the result issues with receipts. */
    private inner class Api(var registered: Boolean = true) : GitHubApi {
        var registrationStatus = 200
        var registrationReads = 0
        var commentReads = 0
        val issues = mutableListOf<JsonObject>()
        val titles = mutableListOf<String>()
        var receipts = true
        var unclearPosts = 0

        override fun get(path: String, token: String?): ApiResponse = when {
            path == "/user" -> ApiResponse(200, """{"login":"alice"}""")
            path == "/repos/robocode-dev/rumble-bots-test/commits/HEAD" -> ApiResponse(200, """{"sha":"$commit"}""")
            path.startsWith("$data/contents/clients/") ->
                if (registrationReads++ < 0) error("never") else if (registrationStatus != 200) ApiResponse(registrationStatus, "{}")
                else if (!registered) ApiResponse(404, "{}")
                else ApiResponse(200, """{"content":${JsonPrimitive(Base64.getEncoder().encodeToString("""{"schemaVersion":1,"account":"alice","clientIds":["alice-1a2b3c4d"]}""".toByteArray()))}}""")
            path.startsWith("$data/issues/") && path.contains("/comments") -> {
                val number = path.removePrefix("$data/issues/").substringBefore('/').toInt()
                commentReads++
                if (!receipts) ApiResponse(200, "[]")
                else ApiResponse(200, "[" + battleIds(number).let { ids ->
                    """{ "user": { "login": "github-actions[bot]" }, "body": ${JsonPrimitive("Processed result batch:\n\n```text\n" + ids.joinToString("\n") { "$it: accepted" } + "\n```\n")} }"""
                } + "]")
            }
            else -> ApiResponse(404, "{}")
        }

        override fun postJson(path: String, token: String, json: String): ApiResponse {
            assertEquals("$data/issues", path)
            val issue = Json.parseToJsonElement(json).jsonObject
            titles += issue["title"]!!.jsonPrimitive.content
            if (unclearPosts > 0) {
                unclearPosts--
                return ApiResponse(502, "{}")
            }
            issues += issue
            return ApiResponse(201, """{"number":${issues.size},"html_url":"x"}""")
        }

        override fun patchJson(path: String, token: String, json: String) = ApiResponse(404, "{}")

        fun battleIds(issue: Int): List<String> {
            val body = issues[issue - 1]["body"]!!.jsonPrimitive.content
            val envelope = Json.parseToJsonElement(body.substringAfter("```json\n").substringBefore("\n```")).jsonObject
            return envelope["results"]!!.jsonArray.map { it.jsonObject["battleId"]!!.jsonPrimitive.content }
        }
    }

    private class Auth : GitHubAuthTransport {
        override fun postForm(url: String, form: Map<String, String>) =
            AuthResponse(200, mapOf("access_token" to "access-1", "refresh_token" to "refresh-1", "expires_in" to "28800"))
    }

    private class Now(var now: Instant = Instant.parse("2026-11-16T10:00:00Z")) : Clock() {
        override fun getZone(): ZoneId = ZoneId.of("UTC")
        override fun withZone(zone: ZoneId?): Clock = this
        override fun instant(): Instant = now
    }

    private class Run(val exit: Int, val out: String) {
        val lines: List<JsonObject> get() = out.lines().filter { it.isNotBlank() }.map { Json.parseToJsonElement(it).jsonObject }
        val events: List<String> get() = lines.mapNotNull { it["event"]?.jsonPrimitive?.content }
        val last: JsonObject get() = lines.last()
        val outcome: String get() = last["outcome"]!!.jsonPrimitive.content
    }

    /** Plays the contributor's Python and pip, as the session asks them. */
    private class FakePython(var version: String? = "3 14") : dev.robocode.tankroyale.rumble.ToolRunner {
        var installs = 0

        /** Called at the first install, as a person pressing Ctrl+C while pip runs; pip then ends without installing. */
        var duringInstall: (() -> Boolean)? = null
        override fun run(command: List<String>, timeout: Duration): dev.robocode.tankroyale.rumble.ToolOutput? {
            if (command.getOrNull(1) == "-c") return version?.let { dev.robocode.tankroyale.rumble.ToolOutput(0, "$it /usr/bin/python3\n") }
            if (duringInstall?.invoke() == true) return dev.robocode.tankroyale.rumble.ToolOutput(1, "KeyboardInterrupt")
            installs++
            val target = Path.of(command[command.indexOf("--target") + 1])
            Files.createDirectories(target.resolve("robocode_tank_royale"))
            Files.writeString(target.resolve("robocode_tank_royale/__init__.py"), "# api $installs\n")
            return dev.robocode.tankroyale.rumble.ToolOutput(0, "ok")
        }
    }

    private inner class World(val state: Path) {
        val python = FakePython()
        val api = Api()
        val catalog = Catalog()
        val pin = Data()
        var mode = "ok"
        var workerReady = true
        var interrupt: (() -> Unit)? = null
        var onInterruptInstalled: (() -> Unit) -> Unit = {}

        fun signIn(account: String? = "alice", register: Boolean = true) {
            val store = SignInStore(env, state)
            store.beginSignIn("refresh-0", null, signInId = "s1")
            if (account != null) store.saveAccount("s1", account)
            if (account != null && register) store.registrationFor("s1").save(RegistrationRecord(account, "alice-1a2b3c4d", env.name, Instant.parse("2026-11-01T10:00:00Z")))
        }

        fun services() = Services(
            makeAuth = { Auth() }, makeApi = { api }, clock = Now(), sleep = {},
            catalog = { catalog }, resultData = { pin },
            worker = {
                if (!workerReady) throw IOException("This program does not carry the battle worker.")
                WorkerCommand { listOf(java, "-cp", System.getProperty("java.class.path"), "dev.robocode.tankroyale.rumble.cli.CliFakeWorkerKt", mode) }
            },
            workerLimits = WorkerLimits(totalTime = Duration.ofSeconds(60)),
            interrupts = { action -> interrupt = action; onInterruptInstalled(action); AutoCloseable { interrupt = null } },
            seed = { 1L },
            tools = python, runtimeList = { dev.robocode.tankroyale.rumble.RuntimeListSource { "java=25\ndotnet=10\npython=3.14\nnode=24\n" } },
            pythonBotApi = dev.robocode.tankroyale.rumble.PythonBotApiWheel {
                val bytes = ByteArrayOutputStream()
                ZipOutputStream(bytes).use { zip ->
                    zip.putNextEntry(ZipEntry("robocode_tank_royale-1.4.0.dist-info/METADATA"))
                    zip.write("Name: robocode-tank-royale\nVersion: 1.4.0\nRequires-Dist: websockets>=15\nRequires-Dist: pycountry>=24\n\n".toByteArray())
                    zip.closeEntry()
                }
                ByteArrayInputStream(bytes.toByteArray())
            },
        )

        fun run(vararg args: String, json: Boolean = true): Run {
            val out = ByteArrayOutputStream()
            val printOut = PrintStream(out, true, Charsets.UTF_8)
            val io = Io(printOut, PrintStream(ByteArrayOutputStream(), true, Charsets.UTF_8), { state })
            val all = arrayOf("contribute", *args, "-e", "test") + if (json) arrayOf("--json") else emptyArray()
            val code = RumbleCli.run(all, io, services())
            return Run(code, out.toString(Charsets.UTF_8))
        }

        fun journal() = RankedJournal.open(env, env.stateRoot(state).resolve("ranked/journal.jsonl"))
    }

    // RCI-048

    @Test
    @Tag("Positive")
    fun testRCI048_UnitPositive_aSessionStartsWhenEverythingIsReady(@TempDir dir: Path) {
        val world = World(dir).apply { signIn() }

        val run = world.run("--battles", "1", "--format", "1v1")

        assertEquals(ExitCodes.DONE, run.exit, run.out)
        assertEquals("started", run.events.first())
        val started = run.lines.first()
        assertEquals("alice", started["account"]!!.jsonPrimitive.content)
        assertEquals(commit, started["catalogCommit"]!!.jsonPrimitive.content)
        assertEquals("finished", run.outcome)

        // A registration read from the Rumble is remembered, so the next session needs no request for it.
        val elsewhere = World(dir.resolve("elsewhere")).apply { signIn(register = false) }
        elsewhere.run("--battles", "1", "--format", "1v1")
        elsewhere.run("--battles", "1", "--format", "1v1")
        assertEquals(1, elsewhere.api.registrationReads)
    }

    @Test
    @Tag("Negative")
    fun testRCI048_UnitNegative_anythingNotReadyEndsTheCommandBeforeAnyBattleWithItsExitCodeAndReason(@TempDir dir: Path) {
        fun check(world: World, exit: Int, outcome: String, next: String? = null) {
            val run = world.run("--battles", "1", "--format", "1v1")
            assertEquals(exit, run.exit, run.out)
            assertEquals(outcome, run.outcome, run.out)
            assertFalse(run.events.contains("battle-started"))
            assertEquals(0, world.api.issues.size)
            next?.let { assertEquals("rumble $it -e test", run.last["next"]!!.jsonObject["command"]!!.jsonPrimitive.content) }
        }

        check(World(dir.resolve("none")), ExitCodes.SIGN_IN_NEEDED, "no-sign-in", "login")
        check(World(dir.resolve("unregistered")).apply { signIn(register = false); api.registered = false }, ExitCodes.SIGN_IN_NEEDED, "not-registered", "register")
        check(World(dir.resolve("catalog")).apply { signIn(); catalog.readable = false }, ExitCodes.FAILED, "catalog-unreadable")
        check(World(dir.resolve("pin-behind")).apply { signIn(); pin.behavior = CURRENT_BEHAVIOR_VERSION - 1 }, ExitCodes.REFUSED, "pin-refused")
        val pinAnswer = World(dir.resolve("pin-answer")).apply { signIn(); pin.behavior = CURRENT_BEHAVIOR_VERSION - 1 }.run("--battles", "1").last
        assertEquals("BEHAVIOR_VERSION_DIFFERS", pinAnswer["reason"]!!.jsonPrimitive.content)
        assertEquals(listOf("${CURRENT_BEHAVIOR_VERSION - 1}", "$CURRENT_BEHAVIOR_VERSION"), pinAnswer["values"]!!.jsonArray.map { it.jsonPrimitive.content })
        check(World(dir.resolve("pin-ahead")).apply { signIn(); pin.behavior = CURRENT_BEHAVIOR_VERSION + 1 }, ExitCodes.REFUSED, "pin-refused")
        check(World(dir.resolve("pin-unreadable")).apply { signIn(); pin.readable = false }, ExitCodes.FAILED, "pin-refused")
        check(World(dir.resolve("worker")).apply { signIn(); workerReady = false }, ExitCodes.FAILED, "worker-unavailable")
        // A registration that cannot be read now is not taken for a missing one.
        check(World(dir.resolve("registration-busy")).apply { signIn(register = false); api.registrationStatus = 503 }, ExitCodes.FAILED, "registration-unreadable")
        // A sign-in GitHub refuses while the registration is read is a sign-in problem, and the refused sign-in is forgotten.
        val refused = World(dir.resolve("registration-refused")).apply { signIn(register = false); api.registrationStatus = 401 }
        check(refused, ExitCodes.SIGN_IN_NEEDED, "sign-in-refused", "login")
        assertFalse(SignInStore(env, refused.state).load() is dev.robocode.tankroyale.rumble.LoadResult.Found, "the refused sign-in is forgotten")

        val ahead = World(dir.resolve("pin-ahead-text")).apply { signIn(); pin.behavior = CURRENT_BEHAVIOR_VERSION + 1 }.run("--battles", "1", json = false)
        assertTrue(ahead.out.contains("Update Tank Royale to take part."), ahead.out)
        val behind = World(dir.resolve("pin-behind-text")).apply { signIn(); pin.behavior = CURRENT_BEHAVIOR_VERSION - 1 }.run("--battles", "1", json = false)
        assertTrue(behind.out.contains("has not moved to this version yet"), behind.out)
    }

    // RCI-049

    @Test
    @Tag("Positive")
    fun testRCI049_UnitPositive_theSessionRunsTheChosenBotsAndFormatsAndSendsWhatItKeeps(@TempDir dir: Path) {
        val world = World(dir).apply { signIn() }

        val run = world.run("--battles", "2", "--format", "1v1", "--bot", "Gamma 1.0", "--mode", "only")

        assertEquals(ExitCodes.DONE, run.exit, run.out)
        val battles = run.lines.filter { it["event"]?.jsonPrimitive?.content == "battle-started" }
        assertEquals(2, battles.size)
        assertTrue(battles.all { b -> b["gameType"]!!.jsonPrimitive.content == "1v1" && b["participants"]!!.jsonArray.any { it.jsonPrimitive.content == "Gamma 1.0" } })
        val records = world.journal().entries().map { it.record }
        assertEquals(2, records.size)
        assertTrue(records.all { it.client.id == "alice-1a2b3c4d" && it.rounds == 3 && it.engine.behaviorVersion == CURRENT_BEHAVIOR_VERSION && it.botsRepoCommit == commit })
        assertEquals(records.map { it.battleId }.toSet(), world.api.issues.indices.flatMap { world.api.battleIds(it + 1) }.toSet(), "everything kept was sent")
        assertEquals(2, run.last["accepted"]!!.jsonPrimitive.content.toInt(), "the receipts were read at the end")
    }

    @Test
    @Tag("Negative")
    fun testRCI049_UnitNegative_anUnknownBotOrAModeWithoutBotsIsBadInput(@TempDir dir: Path) {
        val world = World(dir).apply { signIn() }

        val unknown = world.run("--bot", "Nobody 9.9", "--battles", "1")
        val noBots = world.run("--mode", "only", "--battles", "1")

        assertEquals(ExitCodes.REFUSED, unknown.exit)
        assertEquals("bad-input", unknown.outcome)
        assertEquals(ExitCodes.REFUSED, noBots.exit)
        assertEquals("bad-input", noBots.outcome)
        val inactive = world.run("--mode", "only", "--bot", "Old 1.0", "--battles", "1")
        assertEquals(ExitCodes.REFUSED, inactive.exit)
        assertTrue(inactive.last["message"]!!.jsonPrimitive.content.contains("not active"), inactive.out)
        assertEquals(0, world.api.issues.size)

        // Choices with which no battle can be run end the session instead of waiting for ever.
        val noTeams = world.run("--format", "twinduel", "--battles", "1")
        assertEquals(ExitCodes.FAILED, noTeams.exit, noTeams.out)
        assertEquals("nothing-to-run", noTeams.outcome)
        assertTrue(noTeams.events.contains("waiting"))
    }

    // RCI-050

    @Test
    @Tag("Positive")
    fun testRCI050_UnitPositive_startingAgainKeepsWhatWasKeptAndSendsAHandedOverBatchAgain(@TempDir dir: Path) {
        val world = World(dir).apply { signIn() }
        world.api.receipts = false
        world.run("--battles", "1", "--format", "1v1")
        val first = world.journal().entries().single().record.battleId
        // The first result was sent and its receipt had not come when the session ended.
        assertEquals(1, SentBatches.open(env.stateRoot(dir).resolve("ranked/sent-batches.jsonl")).open().size)

        world.api.receipts = true
        val again = world.run("--battles", "1", "--format", "1v1")

        val entries = world.journal().entries()
        assertEquals(2, entries.size, "the earlier result is kept and one more was run")
        assertTrue(entries.any { it.record.battleId == first })
        assertEquals(1, again.lines.count { it["event"]?.jsonPrimitive?.content == "battle-kept" }, "the kept battle is not run again")
        assertTrue(entries.all { it.state == RecordState.ACCEPTED }, entries.map { it.state }.toString())
    }

    @Test
    @Tag("Negative")
    fun testRCI050_UnitNegative_aBatchWhoseSendHadNoClearAnswerIsSentAgainUnderItsOwnIdentity(@TempDir dir: Path) {
        val world = World(dir).apply { signIn() }
        world.api.unclearPosts = 1
        val first = world.run("--battles", "1", "--format", "1v1")
        assertTrue(first.events.contains("not-sent"))
        assertEquals(RecordState.HANDED_OVER, world.journal().entries().single().state)

        world.run("--battles", "1", "--format", "1v1")

        assertEquals(world.api.titles[0], world.api.titles[1], "the batch is sent again under its own identity")
        assertTrue(world.journal().entries().all { it.state == RecordState.ACCEPTED })
    }

    // RCI-051

    @Test
    @Tag("Positive")
    fun testRCI051_UnitPositive_theFirstCtrlCFinishesTheBattleKeepsItAndStops(@TempDir dir: Path) {
        val world = World(dir).apply { signIn(); mode = "slow" }
        world.onInterruptInstalled = { action -> Thread { Thread.sleep(500); action() }.apply { isDaemon = true; start() } }

        val run = world.run("--format", "1v1")

        assertEquals(ExitCodes.DONE, run.exit, run.out)
        assertEquals("stopped", run.outcome)
        assertTrue(run.events.contains("stopping"))
        assertEquals(1, run.events.count { it == "battle-kept" })
        assertEquals(1, world.api.issues.size, "what waited was sent before it ended")
    }

    @Test
    @Tag("Negative")
    fun testRCI051_UnitNegative_aSecondCtrlCEndsTheBattleAtOnceAndKeepsNothingOfIt(@TempDir dir: Path) {
        val world = World(dir).apply { signIn(); mode = "hang" }
        world.onInterruptInstalled = { action -> Thread { Thread.sleep(1_500); action(); Thread.sleep(300); action() }.apply { isDaemon = true; start() } }
        val started = System.nanoTime()

        val run = world.run("--format", "1v1")

        assertEquals(ExitCodes.NOT_FINISHED, run.exit, run.out)
        assertEquals("cancelled", run.outcome)
        assertTrue(run.events.containsAll(listOf("stopping", "cancelling")))
        assertEquals(0, world.journal().entries().size)
        assertEquals(0, world.api.commentReads, "after ending at once nothing is sent or read over the network")
        assertTrue(Duration.ofNanos(System.nanoTime() - started).seconds < 40, "it did not wait for the worker's time limit")
    }

    // RCI-052

    @Test
    @Tag("Positive")
    fun testRCI052_UnitPositive_eventsAreJsonLinesAndTheFinalAnswerCountsTheResults(@TempDir dir: Path) {
        val world = World(dir).apply { signIn() }

        val run = world.run("--battles", "1", "--format", "1v1")
        val text = World(dir.resolve("text")).apply { signIn() }.run("--battles", "1", "--format", "1v1", json = false)

        assertEquals(listOf("started", "battle-started", "battle-kept"), run.events.take(3))
        assertTrue(run.events.contains("batch-sent"))
        assertTrue(run.events.contains("result-accepted"))
        val last = run.last
        assertEquals("contribute", last["command"]!!.jsonPrimitive.content)
        assertEquals(1, last["keptThisSession"]!!.jsonPrimitive.content.toInt())
        assertEquals(listOf("waiting", "sent", "accepted", "rejected", "setAside").map { last[it]!!.jsonPrimitive.content.toInt() }, listOf(0, 0, 1, 0, 0))
        assertTrue(text.out.lines().any { it.startsWith("Battle 1v1: ") }, text.out)
        assertTrue(text.out.contains("Kept this session: 1."), text.out)
        assertFalse(text.out.contains("{"), "text form has no JSON")
    }

    @Test
    @Tag("Negative")
    fun testRCI052_UnitNegative_aSessionThatGivesUpEndsWithExitCodeOneAndItsReasons(@TempDir dir: Path) {
        val world = World(dir).apply { signIn(); mode = "fail" }

        val run = world.run("--battles", "1", "--format", "1v1")

        assertEquals(ExitCodes.FAILED, run.exit, run.out)
        assertEquals("gave-up", run.outcome)
        assertEquals(5, run.lines.count { it["event"]?.jsonPrimitive?.content == "battle-not-kept" && it["reason"]!!.jsonPrimitive.content == "WORKER_FAILED" })
        assertEquals(0, world.api.issues.size)
    }

    // RCI-053

    @Test
    @Tag("Positive")
    fun testRCI053_IntegrationPositive_oneRealSessionInTheTestRumbleKeepsAndSendsAResultThatIsAccepted() {
        org.junit.jupiter.api.Assumptions.assumeTrue(
            System.getProperty("rumble.live.send") == "true",
            "Set -Drumble.live.send=true, with a saved sign-in and registration for the test Rumble, to run one real session",
        )
        val out = ByteArrayOutputStream()
        val io = Io(PrintStream(out, true, Charsets.UTF_8), PrintStream(ByteArrayOutputStream(), true, Charsets.UTF_8))

        val code = RumbleCli.run(arrayOf("contribute", "-e", "test", "--battles", "1", "--format", "1v1", "--json"), io)

        val run = Run(code, out.toString(Charsets.UTF_8))
        println(run.out)
        assertEquals(ExitCodes.DONE, run.exit, run.out)
        assertEquals("finished", run.outcome)
        assertTrue(run.events.contains("batch-sent"), run.out)
        // The receiving workflow answers within minutes; the next session, or a later look, reads the receipt.
        val issue = run.lines.first { it["event"]?.jsonPrimitive?.content == "batch-sent" }["issue"]!!.jsonPrimitive.content.toInt()
        val api = dev.robocode.tankroyale.rumble.HttpGitHubApi()
        val deadline = Instant.now().plus(Duration.ofMinutes(10))
        var receipt = ""
        while (Instant.now().isBefore(deadline) && !receipt.contains("Processed result batch")) {
            Thread.sleep(15_000)
            receipt = api.get("$data/issues/$issue/comments", null).body
        }
        assertTrue(receipt.contains(": accepted"), receipt)
    }

    // RCI-054

    @Test
    @Tag("Positive")
    fun testRCI054_UnitPositive_aSessionSetsUpPythonForItsPythonBotsAndSaysSo(@TempDir dir: Path) {
        val world = World(dir).apply { signIn(); catalog.python = true }

        val run = world.run("--battles", "1", "--format", "1v1", "--bot", "Snake 1.0", "--mode", "only")

        assertEquals(ExitCodes.DONE, run.exit, run.out)
        assertTrue(run.events.containsAll(listOf("python-setting-up", "python-ready")), run.events.toString())
        assertEquals("3.14", run.lines.first { it["event"]?.jsonPrimitive?.content == "python-ready" }["version"]!!.jsonPrimitive.content)
        val battle = run.lines.first { it["event"]?.jsonPrimitive?.content == "battle-started" }
        assertTrue(battle["participants"]!!.jsonArray.any { it.jsonPrimitive.content == "Snake 1.0" })
        assertEquals(2, world.python.installs, "the Bot API and what it needs, once for the session")
        val ranked = env.stateRoot(dir).resolve("ranked")
        val sessions = ranked.resolve(dev.robocode.tankroyale.rumble.SessionPython.SESSIONS_FOLDER)
        assertEquals(0L, Files.list(sessions).use { it.count() }, "the session's Python folder is removed at the end")
        // The program carries its own Python Bot API.
        assertTrue(carriedPythonBotApi() != null, "the command line carries the Python Bot API")
    }

    @Test
    @Tag("Negative")
    fun testRCI054_UnitNegative_withoutAUsablePythonThePythonBotsAreLeftOutAndTheOthersRun(@TempDir dir: Path) {
        val world = World(dir).apply { signIn(); catalog.python = true; python.version = "3 12" }

        val run = world.run("--battles", "1", "--format", "1v1")
        val only = World(dir.resolve("only")).apply { signIn(); catalog.python = true; python.version = null }.run("--battles", "1", "--format", "1v1", "--bot", "Snake 1.0", "--mode", "only")

        assertEquals(ExitCodes.DONE, run.exit, run.out)
        val unavailable = run.lines.first { it["event"]?.jsonPrimitive?.content == "python-unavailable" }
        assertEquals("TOO_OLD", unavailable["reason"]!!.jsonPrimitive.content)
        assertEquals("3.14", unavailable["needed"]!!.jsonPrimitive.content)
        assertEquals("3.12", unavailable["found"]!!.jsonPrimitive.content)
        assertTrue(run.lines.filter { it["event"]?.jsonPrimitive?.content == "battle-started" }.none { b -> b["participants"]!!.jsonArray.any { it.jsonPrimitive.content == "Snake 1.0" } })
        assertEquals(0, world.python.installs)
        assertEquals("nothing-to-run", only.outcome, "only Python bots chosen and no Python: nothing can run")
        // Only a Java bot chosen in `only` mode: its opponent may still be a Python bot, so Python is set up.
        val javaChosen = World(dir.resolve("java-chosen")).apply { signIn(); catalog.python = true }
        val chosen = javaChosen.run("--battles", "1", "--format", "1v1", "--bot", "Alpha 1.0", "--mode", "only")
        assertEquals(2, javaChosen.python.installs)
        assertTrue(chosen.events.contains("python-ready"), chosen.events.toString())
        // A catalog without Python bots asks nothing of Python.
        val javaOnly = World(dir.resolve("java")).apply { signIn() }
        javaOnly.run("--battles", "1", "--format", "1v1")
        assertEquals(0, javaOnly.python.installs)
    }

    // RCL-134

    @Test
    @Tag("Positive")
    fun testRCL134_IntegrationPositive_oneRealSessionRunsAPythonCatalogBotAndItsResultIsAccepted() {
        org.junit.jupiter.api.Assumptions.assumeTrue(
            System.getProperty("rumble.live.send") == "true",
            "Set -Drumble.live.send=true, with a saved sign-in and registration for the test Rumble and Python, to run one real session with a Python bot",
        )
        val out = ByteArrayOutputStream()
        val io = Io(PrintStream(out, true, Charsets.UTF_8), PrintStream(ByteArrayOutputStream(), true, Charsets.UTF_8))

        val code = RumbleCli.run(arrayOf("contribute", "-e", "test", "--battles", "1", "--format", "1v1", "--mode", "only", "--bot", "Orbit 1.0.0", "--json"), io)

        val run = Run(code, out.toString(Charsets.UTF_8))
        println(run.out)
        assertEquals(ExitCodes.DONE, run.exit, run.out)
        assertTrue(run.events.contains("python-ready"), run.out)
        assertTrue(run.events.contains("battle-kept"), run.out)
        val issue = run.lines.first { it["event"]?.jsonPrimitive?.content == "batch-sent" }["issue"]!!.jsonPrimitive.content.toInt()
        val api = dev.robocode.tankroyale.rumble.HttpGitHubApi()
        val deadline = Instant.now().plus(Duration.ofMinutes(10))
        var receipt = ""
        while (Instant.now().isBefore(deadline) && !receipt.contains("Processed result batch")) {
            Thread.sleep(15_000)
            receipt = api.get("$data/issues/$issue/comments", null).body
        }
        assertTrue(receipt.contains(": accepted"), receipt)
    }

    @Test
    @Tag("Negative")
    fun testRCI054_UnitNegative_aCtrlCWhilePythonIsSetUpIsAStopAndNotAnInstallFailure(@TempDir dir: Path) {
        val world = World(dir).apply { signIn(); catalog.python = true }
        world.python.duringInstall = { world.interrupt?.invoke(); true }

        val run = world.run("--battles", "1", "--format", "1v1", "--bot", "Snake 1.0", "--mode", "only")

        assertTrue(run.events.contains("python-stopped"), run.events.toString())
        assertTrue(run.events.none { it == "python-unavailable" }, "not reported as an install failure")
        assertEquals("stopped", run.outcome, run.out)
    }

    @Test
    @Tag("Positive")
    fun testRCI054_UnitPositive_onlyABattleWithAPythonBotGetsACopyAndItIsNotInTheBotsTemporaryFolder(@TempDir dir: Path) {
        val fake = FakePython()
        val wheel = World(dir).services().pythonBotApi
        val session = dev.robocode.tankroyale.rumble.SessionPython(fake, dir.resolve("state"), wheel, "3.14")
        session.setUp()
        fun bot(name: String, platform: String) = dev.robocode.tankroyale.rumble.CatalogBot(name, "1.0", platform, "bots/x/$name", "sha256:a", "o", listOf("A"), "2026-01-01", "active")
        fun folders(name: String) = dev.robocode.tankroyale.rumble.WorkerFolders(
            Files.createDirectories(dir.resolve("$name/work")), Files.createDirectories(dir.resolve("$name/temp")),
            Files.createDirectories(dir.resolve("$name/home")), Files.createDirectories(dir.resolve("$name/battle")),
        )

        val java = folders("java")
        val python = folders("python")
        val none = pythonBattleEnvironment(session, listOf(bot("Walls", "JVM"), bot("Fire", "JVM")), java)
        val some = pythonBattleEnvironment(session, listOf(bot("Walls", "JVM"), bot("Snake", "Python")), python)
        val withoutPython = pythonBattleEnvironment(null, listOf(bot("Snake", "Python")), folders("off"))

        assertEquals(emptyMap<String, String>(), none, "a battle without a Python bot gets nothing")
        assertFalse(Files.exists(java.battle.resolve(dev.robocode.tankroyale.rumble.SessionPython.COPY_FOLDER)))
        assertEquals(python.battle.resolve(dev.robocode.tankroyale.rumble.SessionPython.COPY_FOLDER).toString(), some["PYTHONPATH"])
        assertFalse(Files.exists(python.temp.resolve(dev.robocode.tankroyale.rumble.SessionPython.COPY_FOLDER)), "not in the bots' temporary folder")
        assertEquals(emptyMap<String, String>(), withoutPython, "without a usable Python nothing is given")
        session.close()
    }

    @Test
    @Tag("Negative")
    fun testRCI054_UnitNegative_whenABattlesCopyCanNoLongerBeMadeThePythonBotsAreLeftOutForTheRestOfTheSession(@TempDir dir: Path) {
        val wheel = World(dir).services().pythonBotApi
        val session = dev.robocode.tankroyale.rumble.SessionPython(FakePython(), dir.resolve("state"), wheel, "3.14").apply { setUp() }
        val told = mutableListOf<Pair<dev.robocode.tankroyale.rumble.SessionPythonProblem, String>>()
        val holder = SessionPythonHolder { reason, detail -> told += reason to detail }.apply { python = session }
        fun bot(name: String, platform: String) = dev.robocode.tankroyale.rumble.CatalogBot(name, "1.0", platform, "bots/x/$name", "sha256:a", "o", listOf("A"), "2026-01-01", "active")
        fun folders(name: String) = dev.robocode.tankroyale.rumble.WorkerFolders(
            Files.createDirectories(dir.resolve("$name/work")), Files.createDirectories(dir.resolve("$name/temp")),
            Files.createDirectories(dir.resolve("$name/home")), dir.resolve("$name/battle-not-made"),
        )
        // A battle folder that cannot be written to, as with a full disk: its file is in the way.
        val blocked = folders("blocked")
        Files.writeString(blocked.battle, "in the way")

        org.junit.jupiter.api.Assertions.assertThrows(IOException::class.java) { holder.environment(listOf(bot("Snake", "Python")), blocked) }

        assertEquals(dev.robocode.tankroyale.rumble.SessionPythonProblem.BATTLE_COPY_FAILED, told.single().first)
        assertEquals(null, holder.python, "the session's Python is dropped")
        assertEquals(emptyMap<String, String>(), holder.environment(listOf(bot("Snake", "Python")), folders("next")), "later battles get nothing")
        holder.close()
    }

    // RCI-055, RCI-056 and RCI-057: the session folder

    private fun folderOf(world: World) = SessionFolder.of(env, world.state)

    /** The event words of the session's log so far; a line still being written is left out. */
    private fun logged(world: World): List<String> = folderOf(world).events().mapNotNull { line ->
        try {
            Json.parseToJsonElement(line).jsonObject["event"]?.jsonPrimitive?.content
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Plays a person or a program at the session folder while the session runs: each step waits until its event word is
     * in the session's log, then acts. The steps start when the session can be interrupted, so its folder is taken.
     */
    private fun World.drive(vararg steps: Pair<String, () -> Unit>) {
        onInterruptInstalled = {
            Thread {
                for ((event, act) in steps) {
                    val until = System.nanoTime() + Duration.ofSeconds(60).toNanos()
                    while (event !in logged(this) && System.nanoTime() < until) Thread.sleep(50)
                    act()
                }
            }.apply { isDaemon = true; start() }
        }
    }

    /** A process that runs until it is ended, to own a session. */
    private fun ownerProcess(): Process =
        ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), "dev.robocode.tankroyale.rumble.cli.CliFakeWorkerKt", "hang").start()

    @Test
    @Tag("Positive")
    fun testRCI055_UnitPositive_aRunningSessionKeepsItsLockStatusAndLogInTheSessionFolder(@TempDir dir: Path) {
        val world = World(dir).apply { signIn(); mode = "slow" }
        val folder = folderOf(world)
        var seen: SessionLook? = null
        world.drive("battle-started" to { seen = folder.look() })

        val run = world.run("--battles", "1", "--format", "1v1")

        assertEquals(ExitCodes.DONE, run.exit, run.out)
        val running = seen as SessionLook.Running
        val status = running.status!!
        assertEquals(ProcessHandle.current().pid(), status.pid)
        assertEquals("test", status.environment)
        assertEquals("running", status.state)
        assertEquals("1v1", status.battle!!.gameType)
        assertEquals(listOf("1v1"), status.formats)
        assertEquals(1, status.battleLimit)

        val ended = folder.look() as SessionLook.Ended
        assertEquals("finished", ended.status.ended!!.outcome)
        assertEquals(1, ended.status.kept)
        assertEquals(1, ended.status.results.accepted)
        assertEquals(run.events, logged(world), "every event reported is in the session's log")
        folder.claim()!!.close() // the lock was let go
    }

    @Test
    @Tag("Negative")
    fun testRCI055_UnitNegative_aSecondSessionIsRefusedAndASessionThatEndsBeforeItsFirstBattleSaysWhy(@TempDir dir: Path) {
        val world = World(dir).apply { signIn() }
        val folder = folderOf(world)
        val other = folder.claim()!!
        val otherStatus = SessionStatus(environment = "test", pid = 4242, startedAt = "2026-11-18T10:00:00Z", state = "running", updatedAt = "2026-11-18T10:00:00Z")
        other.write(otherStatus)

        val second = world.run("--battles", "1", "--format", "1v1")

        assertEquals(ExitCodes.REFUSED, second.exit, second.out)
        assertEquals("session-running", second.outcome)
        assertEquals(4242, second.last["pid"]!!.jsonPrimitive.content.toInt())
        assertFalse(second.events.contains("started"))
        assertEquals(otherStatus, (folder.look() as SessionLook.Running).status, "the running session is left as it was")
        other.close()

        // A session that ends before its first battle still records why, and lets go of the lock.
        val unsigned = World(dir.resolve("unsigned"))
        unsigned.run("--battles", "1")
        assertEquals("no-sign-in", ((folderOf(unsigned).look() as SessionLook.Ended).status.ended!!).outcome)
        val unknown = World(dir.resolve("unknown")).apply { signIn() }
        unknown.run("--bot", "Nobody 9.9", "--battles", "1")
        assertEquals("bad-input", ((folderOf(unknown).look() as SessionLook.Ended).status.ended!!).outcome)
        folderOf(unknown).claim()!!.close()
    }

    @Test
    @Tag("Positive")
    fun testRCI056_UnitPositive_requestsPauseResumeAndStopARunningSession(@TempDir dir: Path) {
        val world = World(dir).apply { signIn(); mode = "slow" }
        val folder = folderOf(world)
        world.drive(
            "battle-started" to { folder.ask(SessionControlRequest.PAUSE) },
            "paused" to { folder.ask(SessionControlRequest.RESUME) },
            "resumed" to { folder.ask(SessionControlRequest.STOP) },
        )

        val run = world.run("--format", "1v1")

        assertEquals(ExitCodes.DONE, run.exit, run.out)
        assertEquals("stopped", run.outcome)
        val order = listOf("pausing", "paused", "resumed", "stopping").map { run.events.indexOf(it) }
        assertTrue(order.all { it >= 0 } && order == order.sorted(), run.out)
        assertEquals(2, run.events.count { it == "battle-kept" })
        assertEquals("stopped", (folder.look() as SessionLook.Ended).status.ended!!.outcome)
    }

    @Test
    @Tag("Positive")
    fun testRCI056_UnitPositive_aPauseLiftedBeforeItTookHoldLeavesTheSessionRunning(@TempDir dir: Path) {
        val world = World(dir).apply { signIn(); mode = "slow" }
        val folder = folderOf(world)
        var afterResume: SessionStatus? = null
        world.drive(
            "battle-started" to { folder.ask(SessionControlRequest.PAUSE); folder.ask(SessionControlRequest.PAUSE) },
            "pausing" to { folder.ask(SessionControlRequest.RESUME) },
            "resuming" to { afterResume = (folder.look() as SessionLook.Running).status },
        )

        val run = world.run("--battles", "2", "--format", "1v1")

        assertEquals("finished", run.outcome, run.out)
        assertEquals(1, run.events.count { it == "pausing" }, "a second pause while pausing changes nothing")
        assertTrue(afterResume!!.state != "pausing", "the status does not stay pausing")
        assertEquals("running", (folder.look() as SessionLook.Ended).status.state)
    }

    @Test
    @Tag("Negative")
    fun testRCI056_UnitNegative_stopNowKeepsNothingAndEachRequestIsActedOnOnce(@TempDir dir: Path) {
        val world = World(dir).apply { signIn(); mode = "hang" }
        val folder = folderOf(world)
        world.drive(
            "battle-started" to {
                folder.ask(SessionControlRequest.STOP_NOW)
                folder.ask(SessionControlRequest.STOP_NOW)
                folder.ask(SessionControlRequest.STOP)
                Files.writeString(folder.folder.resolve("requests/0-x.unknown"), "")
            },
        )

        val run = world.run("--format", "1v1")

        assertEquals(ExitCodes.NOT_FINISHED, run.exit, run.out)
        assertEquals("cancelled", run.outcome)
        assertEquals(1, run.events.count { it == "cancelling" })
        assertFalse(run.events.contains("stopping"), "a stop after stop now changes nothing")
        assertEquals(0, world.journal().entries().size)

        // A request left from an earlier session is not acted on by the next one.
        val next = World(dir.resolve("next")).apply { signIn() }
        val requests = folderOf(next).folder.resolve("requests")
        Files.createDirectories(requests)
        Files.writeString(requests.resolve("1-a.stop-now"), "")
        val clean = next.run("--battles", "1", "--format", "1v1")
        assertEquals("finished", clean.outcome, clean.out)
        assertFalse(clean.events.contains("cancelling"))
    }

    @Test
    @Tag("Positive")
    fun testRCI057_UnitPositive_aSessionStopsAfterItsBattleWhenItsOwnerIsGoneAndRunsOnWhenReleased(@TempDir dir: Path) {
        val owner = ownerProcess()
        try {
            val world = World(dir).apply { signIn(); mode = "slow" }
            var seen: SessionStatus? = null
            world.drive("battle-started" to { seen = (folderOf(world).look() as SessionLook.Running).status; owner.destroyForcibly() })

            val run = world.run("--format", "1v1", "--owner-pid", owner.pid().toString())

            assertEquals(ExitCodes.DONE, run.exit, run.out)
            assertEquals("stopped", run.outcome)
            assertEquals(owner.pid(), seen!!.owner!!.pid)
            assertTrue(run.events.indexOf("owner-gone") in 0 until run.events.indexOf("stopping"), run.out)
            assertTrue(run.events.count { it == "battle-kept" } >= 1, "the running battle was kept")
        } finally {
            owner.destroyForcibly()
        }

        val released = ownerProcess()
        try {
            val world = World(dir.resolve("released")).apply { signIn(); mode = "slow" }
            val folder = folderOf(world)
            world.drive(
                "battle-started" to { folder.ask(SessionControlRequest.RELEASE) },
                "released" to { released.destroyForcibly() },
            )

            val run = world.run("--battles", "2", "--format", "1v1", "--owner-pid", released.pid().toString())

            assertEquals("finished", run.outcome, run.out)
            assertEquals(2, run.events.count { it == "battle-kept" })
            assertFalse(run.events.contains("owner-gone"))
            assertEquals(null, (folder.look() as SessionLook.Ended).status.owner)
        } finally {
            released.destroyForcibly()
        }
    }

    @Test
    @Tag("Negative")
    fun testRCI057_UnitNegative_anOwnerThatDoesNotRunIsBadInputAndAReusedProcessIdIsNotTheOwner(@TempDir dir: Path) {
        val gone = ProcessBuilder(java, "-version").redirectErrorStream(true).start()
        gone.inputStream.readAllBytes()
        gone.waitFor()
        val world = World(dir).apply { signIn() }

        val run = world.run("--battles", "1", "--owner-pid", gone.pid().toString())

        assertEquals(ExitCodes.REFUSED, run.exit, run.out)
        assertEquals("bad-input", run.outcome)
        assertEquals(SessionLook.None, folderOf(world).look(), "no session was started")

        val me = SessionOwner.of(ProcessHandle.current().pid())!!
        assertTrue(me.isAlive())
        val reused = me.copy(startedAt = Instant.parse(me.startedAt).minusSeconds(3_600).toString())
        assertFalse(reused.isAlive(), "a process with the same ID that started at another time is not the owner")
    }
}
