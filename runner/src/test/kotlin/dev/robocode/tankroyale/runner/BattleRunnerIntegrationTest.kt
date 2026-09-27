package dev.robocode.tankroyale.runner

import dev.robocode.tankroyale.common.rules.CURRENT_BEHAVIOR_VERSION
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.*
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.Timeout
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.zip.GZIPInputStream
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.util.Collections
import java.nio.charset.StandardCharsets
import kotlin.io.path.exists
import kotlinx.serialization.json.JsonPrimitive

/**
 * Integration tests that run real battles against the embedded server with sample bots.
 *
 * Each test creates its own [BattleRunner] to avoid inter-test state leaking (e.g. stale bot
 * connections). Embedded server/booter JARs are filesystem resources during testing, so the
 * [ServerManager]/[BooterManager] cleanup correctly leaves them intact for reuse.
 *
 * Run via `./gradlew :runner:integrationTest` — excluded from the default `test` task.
 */
@Tag("integration")
class BattleRunnerIntegrationTest {

    @TempDir
    lateinit var tempDir: Path

    companion object {
        private val sampleBotsDir: Path by lazy {
            val dir = System.getProperty("sampleBots.java.dir")
                ?: error("System property 'sampleBots.java.dir' not set — run via :runner:integrationTest")
            Path.of(dir)
        }

        private val csharpBotsDir: Path by lazy {
            val dir = System.getProperty("sampleBots.csharp.dir")
                ?: error("System property 'sampleBots.csharp.dir' not set — run via :runner:integrationTest")
            Path.of(dir)
        }

        private val testBotsJavaDir: Path by lazy {
            val dir = System.getProperty("testBots.java.dir")
                ?: error("System property 'testBots.java.dir' not set — run via :runner:integrationTest")
            Path.of(dir)
        }

        private val testBotsCsharpDir: Path by lazy {
            val dir = System.getProperty("testBots.csharp.dir")
                ?: error("System property 'testBots.csharp.dir' not set — run via :runner:integrationTest")
            Path.of(dir)
        }

        private val testBotsTypescriptDir: Path by lazy {
            val dir = System.getProperty("testBots.typescript.dir")
                ?: error("System property 'testBots.typescript.dir' not set — run via :runner:integrationTest")
            Path.of(dir)
        }

        private fun botDir(name: String): Path {
            val dir = sampleBotsDir.resolve(name)
            check(dir.exists()) { "Sample bot not found: $dir" }
            return dir
        }

        private fun csharpBotDir(name: String): Path {
            val dir = csharpBotsDir.resolve(name)
            check(dir.exists()) { "C# sample bot not found: $dir" }
            return dir
        }

        private fun testBotDir(name: String): Path {
            val dir = testBotsJavaDir.resolve(name)
            check(dir.exists()) { "Test bot not found: $dir" }
            return dir
        }

        private fun testBotCsharpDir(name: String): Path {
            val dir = testBotsCsharpDir.resolve(name)
            check(dir.exists()) { "C# test bot not found: $dir" }
            return dir
        }

        private fun testBotTsDir(name: String): Path {
            val dir = testBotsTypescriptDir.resolve(name)
            check(dir.exists()) { "TypeScript test bot not found: $dir" }
            return dir
        }
    }

    // -------------------------------------------------------------------------------------
    // 10.2 — Run a real battle with sample bots
    // -------------------------------------------------------------------------------------

    @Test
    @Tag("BR-049")
    @Tag("Integration")
    @Tag("Positive")
    fun testBR049_IntegrationPositive_acceptsMatchingBehaviorVersionBeforeBotBoot() {
        BattleRunner.create {
            embeddedServer()
            requireBehaviorVersion(CURRENT_BEHAVIOR_VERSION)
        }.use { runner ->
            val results = runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
            )

            assertThat(results.results).hasSize(2)
        }
    }

    @Test
    @Tag("BR-049")
    @Tag("Integration")
    @Tag("Negative")
    fun testBR049_IntegrationNegative_rejectsMismatchedBehaviorVersionBeforeBotBoot() {
        BattleRunner.create {
            embeddedServer()
            requireBehaviorVersion(CURRENT_BEHAVIOR_VERSION + 1)
        }.use { runner ->
            assertThatThrownBy {
                runner.runBattle(
                    setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                    bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
                )
            }.isInstanceOf(BattleException::class.java)
                .hasMessageContaining("does not match required version")

            assertThat(runner.connection!!.latestBotList.get())
                .describedAs("No bot may connect when the behavior-version precondition fails")
                .isEmpty()
        }
    }

    @Tag("Unit")
    @Test
    fun `runBattle with two sample bots returns valid results`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            val results = runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
            )
            assertThat(results.numberOfRounds).isEqualTo(1)
            assertThat(results.results).hasSize(2)

            results.results.forEach { bot ->
                assertThat(bot.name).isNotBlank()
                assertThat(bot.version).isNotBlank()
                assertThat(bot.rank).isIn(1, 2)
                assertThat(bot.totalScore).isGreaterThanOrEqualTo(0)
                assertThat(bot.survival).isGreaterThanOrEqualTo(0)
                assertThat(bot.bulletDamage).isGreaterThanOrEqualTo(0)
                assertThat(bot.ramDamage).isGreaterThanOrEqualTo(0)
            }

            assertThat(results.results.map { it.rank }.sorted()).containsExactly(1, 2)
        }
    }

    @Tag("Unit")
    @Test
    fun `runBattle with config-less Java bot succeeds`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            val results = runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(
                    BotEntry.of(testBotDir("ConfigLessJavaBot")),
                    BotEntry.of(botDir("SpinBot"))
                )
            )
            assertThat(results.results).hasSize(2)
            assertThat(results.results.map { it.name }).contains("ConfigLessJavaBot")
        }
    }

    @Tag("Unit")
    @Test
    fun `runBattle with bot missing required properties fails with BotException`() {
        BattleRunner.create {
            embeddedServer()
            botConnectTimeout(Duration.ofSeconds(10))
        }.use { runner ->
            assertThatThrownBy {
                runner.runBattle(
                    setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                    bots = listOf(
                        BotEntry.of(testBotDir("MissingPropertiesJavaBot")),
                        BotEntry.of(botDir("SpinBot"))
                    )
                )
            }.isInstanceOf(BattleException::class.java)
                .hasMessageContaining("Bot connect timeout") // Booter might exit, but runner sees timeout
        }
    }

    @Tag("Unit")
    @Test
    fun `runBattle with multiple rounds produces results with scores`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            val results = runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 3 },
                bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("Target")))
            )
            assertThat(results.numberOfRounds).isEqualTo(3)
            assertThat(results.results).hasSize(2)

            val winner = results.results.first { it.rank == 1 }
            assertThat(winner.totalScore).isGreaterThan(0)
        }
    }

    // -------------------------------------------------------------------------------------
    // 10.3 — Server reuse across battles
    // -------------------------------------------------------------------------------------

    @Tag("Unit")
    @Test
    fun `server is reused across battles`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            val results1 = runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
            )
            assertThat(results1.results).hasSize(2)

            // Allow previous bots to fully disconnect before starting the next battle
            Thread.sleep(2000)

            val results2 = runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("Target")))
            )
            assertThat(results2.results).hasSize(2)
        }
    }

    // -------------------------------------------------------------------------------------
    // 10.3 — External server mode
    // -------------------------------------------------------------------------------------

    @Tag("Unit")
    @Test
    fun `external server mode throws for unreachable server`() {
        BattleRunner.create { externalServer("ws://localhost:1") }.use { extRunner ->
            assertThatThrownBy {
                extRunner.runBattle(
                    setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                    bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
                )
            }.isInstanceOf(BattleException::class.java)
        }
    }

    // -------------------------------------------------------------------------------------
    // 10.4 — Error scenarios
    // -------------------------------------------------------------------------------------

    @Tag("Unit")
    @Test
    fun `runBattle with too few bots throws BattleException`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            assertThatThrownBy {
                runner.runBattle(
                    setup = BattleSetup.oneVsOne(),
                    bots = listOf(BotEntry.of(botDir("Walls")))
                )
            }.isInstanceOf(BattleException::class.java)
                .hasMessageContaining("at least 2 bots")
        }
    }

    @Tag("Unit")
    @Test
    fun `runBattle with too many bots for 1v1 throws BattleException`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            assertThatThrownBy {
                runner.runBattle(
                    setup = BattleSetup.oneVsOne(),
                    bots = listOf(
                        BotEntry.of(botDir("Walls")),
                        BotEntry.of(botDir("SpinBot")),
                        BotEntry.of(botDir("Target"))
                    )
                )
            }.isInstanceOf(BattleException::class.java)
                .hasMessageContaining("At most 2 bots")
        }
    }

    @Tag("Unit")
    @Test
    fun `runBattle after close throws`() {
        val disposableRunner = BattleRunner.create { externalServer("ws://localhost:1") }
        disposableRunner.close()

        assertThatThrownBy {
            disposableRunner.runBattle(
                setup = BattleSetup.classic(),
                bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
            )
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("closed")
    }

    @Tag("Unit")
    @Test
    fun `runBattle with invalid bot directory throws`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            val fakeBot = tempDir.resolve("FakeBot")
            fakeBot.toFile().mkdirs()

            assertThatThrownBy {
                runner.runBattle(
                    setup = BattleSetup.oneVsOne(),
                    bots = listOf(BotEntry.of(fakeBot), BotEntry.of(botDir("Walls")))
                )
            }.isInstanceOf(BattleException::class.java)
                .hasMessageContaining("configuration file")
        }
    }

    // -------------------------------------------------------------------------------------
    // 10.2 — Async battle (BattleHandle)
    // -------------------------------------------------------------------------------------

    @Tag("Unit")
    @Test
    fun `startBattleAsync returns handle with events and results`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            val handle = runner.startBattleAsync(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
            )
            handle.use {
                val results = it.awaitResults()
                assertThat(results.numberOfRounds).isEqualTo(1)
                assertThat(results.results).hasSize(2)
            }
        }
    }

    // -------------------------------------------------------------------------------------
    // 10.5 — Battle recording
    // -------------------------------------------------------------------------------------

    @Tag("Unit")
    @Test
    fun `recording produces valid gzip ND-JSON file`() {
        val recordingDir = tempDir.resolve("recordings")
        recordingDir.toFile().mkdirs()

        BattleRunner.create {
            embeddedServer()
            enableRecording(recordingDir)
        }.use { runner ->
            runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
            )
        }

        // Find the .battle.gz file
        val recordings = recordingDir.toFile().listFiles { _, name -> name.endsWith(".battle.gz") }
        assertThat(recordings).isNotNull().isNotEmpty()

        val recordingFile = recordings!!.first()
        assertThat(recordingFile.length()).isGreaterThan(0)

        // Verify it's valid GZIP and contains ND-JSON lines
        val lines = GZIPInputStream(recordingFile.inputStream()).use { gzis ->
            BufferedReader(InputStreamReader(gzis)).readLines()
        }
        assertThat(lines).isNotEmpty()

        lines.forEach { line ->
            assertThat(line).contains("\"type\"")
        }

        // Should contain at least GameStartedEventForObserver and GameEndedEventForObserver
        val types = lines.map { line ->
            val match = Regex("\"type\"\\s*:\\s*\"([^\"]+)\"").find(line)
            match?.groupValues?.get(1)
        }.filterNotNull().toSet()

        assertThat(types).contains("GameStartedEventForObserver")
        assertThat(types).contains("GameEndedEventForObserver")
    }

    // -------------------------------------------------------------------------------------
    // 10.6 — Intent diagnostics
    // -------------------------------------------------------------------------------------

    @Tag("Unit")
    @Test
    fun `intent diagnostics captures bot intents`() {
        BattleRunner.create {
            embeddedServer()
            enableIntentDiagnostics()
        }.use { runner ->
            runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
            )

            val store = runner.intentDiagnostics
            assertThat(store).isNotNull()

            // Both bots should have captured intents
            assertThat(store!!.botNames()).hasSize(2)
            assertThat(store.size).isGreaterThan(0)

            // Each bot should have at least one intent
            store.botNames().forEach { botName ->
                val intents = store.getIntentsForBot(botName)
                assertThat(intents).isNotEmpty()

                intents.forEach { intent ->
                    assertThat(intent.roundNumber).isGreaterThanOrEqualTo(1)
                    assertThat(intent.turnNumber).isGreaterThanOrEqualTo(1)
                    assertThat(intent.botName).isEqualTo(botName)
                }
            }
        }
    }

    @Tag("integration")
    @Tag("slow")
    @Tag("PRO-006")
    @Tag("Integration")
    @Tag("Positive")
    @Test
    @Timeout(120)
    fun testPRO006_IntegrationPositive_fiveBotsDeliverOrdered128ItemBatchesWithoutSkippedTurns() {
        runFiveBotTeamMessageTrial(
            teamName = "TeamMessageBatchStressTeam",
            botName = "TeamMessageBatchStress",
            expectedReceivedItemsPerBot = 4 * 60 * 128,
            outboundTeamMessageBytes = estimateBatchArrayBytes(128),
            itemsPerBatch = 128
        )
    }

    @Tag("integration")
    @Tag("slow")
    @Tag("PRO-006")
    @Tag("Integration")
    @Tag("Positive")
    @Test
    @Timeout(120)
    fun testPRO006_IntegrationPositive_fiveBotNoMessageControlHasNoSkippedTurns() {
        runFiveBotTeamMessageTrial(
            teamName = "TeamMessageBatchControlTeam",
            botName = "TeamMessageBatchControl",
            expectedReceivedItemsPerBot = 0,
            outboundTeamMessageBytes = 0,
            itemsPerBatch = 128
        )
    }

    @Tag("integration")
    @Tag("slow")
    @Tag("PRO-006")
    @Tag("Integration")
    @Tag("Positive")
    @Test
    @Timeout(120)
    fun testPRO006_IntegrationPositive_fiveBotsDeliverOrdered64ItemBatchesWithoutSkippedTurns() {
        runFiveBotTeamMessageTrial(
            teamName = "TeamMessageBatchStress64Team",
            botName = "TeamMessageBatchStress64",
            expectedReceivedItemsPerBot = 4 * 60 * 64,
            outboundTeamMessageBytes = estimateBatchArrayBytes(64),
            itemsPerBatch = 64
        )
    }

    @Tag("integration")
    @Tag("slow")
    @Tag("PRO-006")
    @Tag("Integration")
    @Tag("Positive")
    @Test
    @Timeout(120)
    fun testPRO006_IntegrationPositive_fiveBot64ItemNoMessageControlHasNoSkippedTurns() {
        runFiveBotTeamMessageTrial(
            teamName = "TeamMessageBatchControl64Team",
            botName = "TeamMessageBatchControl64",
            expectedReceivedItemsPerBot = 0,
            outboundTeamMessageBytes = 0,
            itemsPerBatch = 64
        )
    }

    @Tag("integration")
    @Tag("slow")
    @Tag("PRO-006")
    @Tag("Integration")
    @Tag("Positive")
    @Test
    @Timeout(120)
    fun testPRO006_IntegrationPositive_fiveBotsDeliverOrdered32ItemBatchesWithoutSkippedTurns() {
        runFiveBotTeamMessageTrial(
            teamName = "TeamMessageBatchStress32Team",
            botName = "TeamMessageBatchStress32",
            expectedReceivedItemsPerBot = 4 * 1000 * 32,
            outboundTeamMessageBytes = estimateBatchArrayBytes(32, 11..1010),
            itemsPerBatch = 32,
            measuredSendTurns = 1000,
            paceServerAtTps = true,
            usePassiveOpponents = true,
            recordExactSkippedTurnNumbers = true
        )
    }

    @Tag("integration")
    @Tag("slow")
    @Tag("PRO-006")
    @Tag("Integration")
    @Tag("Positive")
    @Test
    @Timeout(120)
    fun testPRO006_IntegrationPositive_fiveBot32ItemNoMessageControlHasNoSkippedTurns() {
        runFiveBotTeamMessageTrial(
            teamName = "TeamMessageBatchControl32Team",
            botName = "TeamMessageBatchControl32",
            expectedReceivedItemsPerBot = 0,
            outboundTeamMessageBytes = 0,
            itemsPerBatch = 32,
            measuredSendTurns = 1000,
            paceServerAtTps = true,
            usePassiveOpponents = true,
            recordExactSkippedTurnNumbers = true
        )
    }

    private fun runFiveBotTeamMessageTrial(
        teamName: String,
        botName: String,
        expectedReceivedItemsPerBot: Int,
        outboundTeamMessageBytes: Long,
        itemsPerBatch: Int,
        measuredSendTurns: Int = 60,
        paceServerAtTps: Boolean = false,
        usePassiveOpponents: Boolean = false,
        recordExactSkippedTurnNumbers: Boolean = false
    ) {
        val measuredFromTurn = 11 // Turns 1–10 are warm-up; measure from the first post-warm-up tick.
        val lastMeasuredSendTurn = measuredFromTurn + measuredSendTurns - 1
        val measuredThroughTurn = lastMeasuredSendTurn + 1
        val finalStateTurn = ((lastMeasuredSendTurn + 1 + 4) / 5) * 5 + 1
        val completionTurn = lastMeasuredSendTurn + 12
        val firstTickTurn = AtomicReference<Int?>()
        val startedAtNanos = AtomicReference<Long?>()
        val finishedAtNanos = AtomicReference<Long?>()
        val finalStates = AtomicReference<List<dev.robocode.tankroyale.client.model.BotState>?>()
        val completed = CountDownLatch(1)
        val owner = Any()
        val observedSkippedTurnNumbersByBot = mutableMapOf<Int, MutableSet<Int>>()
        val opponentNames = if (usePassiveOpponents) {
            listOf("TeamMessageBatchControl", "TeamMessageBatchControl64")
        } else {
            listOf("Walls", "SpinBot")
        }
        val rootLogger = Logger.getLogger("")
        val savedRootLogLevel = rootLogger.level
        val timingLogHandler = CapturingHandler()
        if (paceServerAtTps) {
            rootLogger.level = Level.ALL
            rootLogger.addHandler(timingLogHandler)
        }

        try {
            BattleRunner.create {
                embeddedServer()
                if (paceServerAtTps) enableTurnTimingDiagnostics(tps = 30)
            }.use { runner ->
            val handle = runner.startBattleAsync(
                BattleSetup.custom {
                    numberOfRounds = 1
                    minNumberOfParticipants = 7
                    maxNumberOfParticipants = 7
                    maxInactivityTurns = 120
                    turnTimeoutMicros = 30_000
                    defaultTurnsPerSecond = 30
                },
                listOf(BotEntry.of(botDir(teamName))) + opponentNames.map { BotEntry.of(botDir(it)) }
            )
            handle.onTickEvent.on(owner) { tick ->
                if (firstTickTurn.get() == null && tick.turnNumber >= measuredFromTurn) {
                    firstTickTurn.set(tick.turnNumber)
                    startedAtNanos.set(System.nanoTime())
                }
                if (tick.turnNumber >= measuredThroughTurn && finishedAtNanos.get() == null) {
                    finishedAtNanos.set(System.nanoTime())
                }
                if (tick.turnNumber >= finalStateTurn && finalStates.get() == null) {
                    finalStates.set(tick.botStates.toList())
                }
                if (recordExactSkippedTurnNumbers) {
                    val bitmapTurn = ((tick.turnNumber - 1) / 5) * 5
                    tick.botStates.filter { it.name == botName }.forEach { state ->
                        if (state.turretColor == null || state.gunColor == null || state.bulletColor == null) {
                            return@forEach
                        }
                        val skipMask = decodeRgb(state.turretColor).toLong() or
                            (decodeRgb(state.gunColor).toLong() shl 24) or
                            ((decodeRgb(state.bulletColor).toLong() and 0xfff) shl 48)
                        val observed = observedSkippedTurnNumbersByBot.getOrPut(state.id) { mutableSetOf() }
                        for (bit in 0 until 60) {
                            if ((skipMask and (1L shl bit)) == 0L) continue
                            val firstTurnForBit = measuredFromTurn + bit
                            val latestTurnForBit = firstTurnForBit +
                                Math.floorDiv(bitmapTurn - firstTurnForBit, 60) * 60
                            if (latestTurnForBit in measuredFromTurn..lastMeasuredSendTurn &&
                                latestTurnForBit >= bitmapTurn - 59
                            ) {
                                observed += latestTurnForBit
                            }
                        }
                    }
                }
                if (tick.turnNumber >= completionTurn && completed.count > 0) {
                    completed.countDown()
                }
            }

            assertThat(completed.await(90, TimeUnit.SECONDS))
                .describedAs("the five-bot $botName workload must reach turn $completionTurn")
                .isTrue()
            assertThat(firstTickTurn.get())
                .describedAs("the measured interval must start at the first post-warm-up tick")
                .isEqualTo(measuredFromTurn)
            handle.onTickEvent.off(owner)
            handle.stop()
            if (paceServerAtTps) runner.close()
            val states = finalStates.get()!!.filter { it.name == botName }
            assertThat(states).hasSize(5)
            println(
                "TEAM_MESSAGE_TRIAL_STATE workload=$botName " + states.joinToString { state ->
                    "id=${state.id},body=${state.bodyColor},tracks=${state.tracksColor}," +
                        "turret=${state.turretColor},radar=${state.radarColor}," +
                        "scan=${state.scanColor},gun=${state.gunColor},bullet=${state.bulletColor}"
                }
            )
            val minTimeLeftMicros = mutableListOf<Int>()
            val averageTimeLeftMicros = mutableListOf<Int>()
            val maxMessageWorkMicros = mutableListOf<Int>()
            val maxTeamMessageHandlerMicros = mutableListOf<Int>()
            val exactSkippedTurnNumbersByBot = mutableMapOf<Int, List<Int>>()
            val skipMaskMatchesEventCountByBot = mutableMapOf<Int, Boolean>()
            val receivedItemsByBot = mutableMapOf<Int, Int>()
            val skippedTurnCountByBot = mutableMapOf<Int, Int>()
            val protocolErrorsByBot = mutableMapOf<Int, Int>()
            val contentErrorsByBot = mutableMapOf<Int, Int>()
            states.forEach { state ->
                val body = decodeRgb(state.bodyColor)
                val tracks = decodeRgb(state.tracksColor)
                val isWideCountWorkload = botName == "TeamMessageBatchStress32"
                val received = if (isWideCountWorkload) body else (body ushr 8) and 0xffff
                val protocolErrors = if (isWideCountWorkload) (tracks ushr 16) and 0xff else body and 0xff
                val skippedTurns = (tracks ushr 8) and 0xff
                val radar = decodeRgb(state.radarColor)
                val contentErrors = (radar ushr 16) and 0xff
                val averageLeftMicros = radar and 0xffff
                val minLeftMicros = decodeRgb(state.scanColor)
                receivedItemsByBot[state.id] = received
                skippedTurnCountByBot[state.id] = skippedTurns
                protocolErrorsByBot[state.id] = protocolErrors
                contentErrorsByBot[state.id] = contentErrors
                if (recordExactSkippedTurnNumbers) {
                    val skippedTurnNumbers = observedSkippedTurnNumbersByBot[state.id].orEmpty().sorted()
                    exactSkippedTurnNumbersByBot[state.id] = skippedTurnNumbers
                    skipMaskMatchesEventCountByBot[state.id] = skippedTurnNumbers.size == skippedTurns
                } else {
                    maxMessageWorkMicros.add(decodeRgb(state.gunColor))
                    maxTeamMessageHandlerMicros.add(decodeRgb(state.bulletColor))
                }
                minTimeLeftMicros.add(minLeftMicros)
                averageTimeLeftMicros.add(averageLeftMicros)
            }

            val elapsedSeconds = (finishedAtNanos.get()!! - startedAtNanos.get()!!) / 1_000_000_000.0
            val observerCallbackTps = measuredSendTurns / elapsedSeconds
            val measuredTps = if (paceServerAtTps) {
                val teamBotIds = states.map { it.id }.toSet()
                val dispatchTimestampPattern = Regex(
                    "TURN_TIMING source=server event=tick-dispatch botId=(\\d+) round=1 turn=(\\d+) nanos=(\\d+)"
                )
                val dispatchTimestampsByTurn = timingLogHandler.messages.mapNotNull { message ->
                    dispatchTimestampPattern.find(message)?.let { match ->
                        Triple(match.groupValues[1].toInt(), match.groupValues[2].toInt(), match.groupValues[3].toLong())
                    }
                }.filter { (botId, turn, _) ->
                    botId in teamBotIds && turn in setOf(measuredFromTurn, measuredThroughTurn)
                }.groupBy({ it.second }, { it.third })
                val firstTurnTimestamps = dispatchTimestampsByTurn[measuredFromTurn].orEmpty()
                val lastTurnTimestamps = dispatchTimestampsByTurn[measuredThroughTurn].orEmpty()
                assertThat(firstTurnTimestamps).hasSize(states.size)
                assertThat(lastTurnTimestamps).hasSize(states.size)
                val elapsedNanos = lastTurnTimestamps.average() - firstTurnTimestamps.average()
                measuredSendTurns * 1_000_000_000.0 / elapsedNanos
            } else {
                observerCallbackTps
            }
            println(
                "TEAM_MESSAGE_TRIAL workload=$botName bots=5 turns=$measuredSendTurns itemsPerBatch=$itemsPerBatch tps=$measuredTps " +
                    "observerCallbackTps=$observerCallbackTps " +
                    "outboundTeamMessagesBytes=$outboundTeamMessageBytes " +
                    "estimatedTeamPayloadFanoutBytes=${outboundTeamMessageBytes * 4} " +
                    "receivedPerBot=$expectedReceivedItemsPerBot " +
                    "exactSkippedTurnNumbersByBot=$exactSkippedTurnNumbersByBot " +
                    "receivedItemsByBot=$receivedItemsByBot skippedTurnCountByBot=$skippedTurnCountByBot " +
                    "protocolErrorsByBot=$protocolErrorsByBot contentErrorsByBot=$contentErrorsByBot " +
                    "minTimeLeftMicros=${minTimeLeftMicros.minOrNull()} " +
                    "averageTimeLeftMicros=${averageTimeLeftMicros.average().toLong()} " +
                    "maxMessageWorkMicros=${maxMessageWorkMicros.maxOrNull() ?: "not-recorded"} " +
                    "maxTeamMessageHandlerMicros=${maxTeamMessageHandlerMicros.maxOrNull() ?: "not-recorded"}"
            )
            // Effective TPS is diagnostic; the pass criteria are complete, error-free ordered delivery and zero skipped turns.
            receivedItemsByBot.forEach { (botId, received) ->
                assertThat(received)
                    .describedAs("ordered logical messages received by $botName $botId; errors=${protocolErrorsByBot[botId]} skipped=${skippedTurnCountByBot[botId]}")
                    .isEqualTo(expectedReceivedItemsPerBot)
            }
            skippedTurnCountByBot.forEach { (botId, skippedTurns) ->
                assertThat(skippedTurns)
                    .describedAs("skipped turns recorded by $botName $botId")
                    .isZero()
            }
            protocolErrorsByBot.forEach { (botId, protocolErrors) ->
                assertThat(protocolErrors)
                    .describedAs("message and protocol errors recorded by $botName $botId")
                    .isZero()
            }
            contentErrorsByBot.forEach { (botId, contentErrors) ->
                assertThat(contentErrors)
                    .describedAs("message content errors recorded by $botName $botId")
                    .isZero()
            }
            skipMaskMatchesEventCountByBot.forEach { (botId, matches) ->
                assertThat(matches)
                    .describedAs("skip-turn bitmap matches the skipped-turn event count for $botName $botId")
                    .isTrue()
            }
            exactSkippedTurnNumbersByBot.forEach { (botId, skippedTurns) ->
                assertThat(skippedTurns)
                    .describedAs("exact skipped-turn numbers reported by $botName $botId")
                    .isEmpty()
            }
            if (expectedReceivedItemsPerBot == 0 && !recordExactSkippedTurnNumbers) {
                assertThat(maxTeamMessageHandlerMicros).containsOnly(0)
            }
            }
        } finally {
            if (paceServerAtTps) {
                rootLogger.removeHandler(timingLogHandler)
                rootLogger.level = savedRootLogLevel
            }
        }
    }

    private fun decodeRgb(color: String?): Int = requireNotNull(color).removePrefix("#").take(6).toInt(16)

    private fun estimateBatchArrayBytes(itemsPerBatch: Int, turns: IntRange = 1..60): Long {
        fun encodedString(value: String) = JsonPrimitive(value).toString()
        var totalBytes = 0L
        for (botId in 1..5) {
            for (turn in turns) {
                val entries = (0 until itemsPerBatch).joinToString(",") { item ->
                    "{\"messageType\":\"java.lang.String\",\"message\":" +
                        encodedString(encodedString("$botId:$turn:$item")) + "}"
                }
                val batchPayload = "{\"messages\":[$entries]}"
                val packet = "[{\"message\":" + encodedString(batchPayload) +
                    ",\"messageType\":\"team-message-batch-v1\"}]"
                totalBytes += packet.toByteArray(StandardCharsets.UTF_8).size
            }
        }
        return totalBytes
    }

    // -------------------------------------------------------------------------------------
    // WonRoundEvent delivery verification — 10-round battle
    // -------------------------------------------------------------------------------------

    @Tag("Unit")
    @Test
    fun `firstPlaces sum equals number of rounds in 10-round battle`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            val results = runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 10 },
                bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
            )
            assertThat(results.numberOfRounds).isEqualTo(10)

            val totalFirstPlaces = results.results.sumOf { it.firstPlaces }
            assertThat(totalFirstPlaces)
                .describedAs("Each of the 10 rounds must have exactly one winner: sum of firstPlaces should equal 10")
                .isEqualTo(10)
        }
    }

    @Tag("Unit")
    @Test
    fun `WonRoundCounterJava bot receives one WonRoundEvent per round it wins`() {
        val countFile = Path.of(System.getProperty("java.io.tmpdir"), "won_round_java.txt")
        countFile.toFile().delete()

        BattleRunner.create { embeddedServer() }.use { runner ->
            val results = runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 10 },
                bots = listOf(
                    BotEntry.of(testBotDir("WonRoundCounterJava")),
                    BotEntry.of(botDir("SpinBot"))
                )
            )

            val serverFirstPlaces = results.results
                .first { it.name == "WonRoundCounterJava" }
                .firstPlaces

            val botSideCount = if (countFile.exists())
                countFile.toFile().readText().trim().toIntOrNull() ?: 0
            else 0

            assertThat(botSideCount)
                .describedAs(
                    "WonRoundCounterJava should receive exactly as many WonRoundEvents " +
                    "as the server recorded first-place finishes (got $botSideCount, server says $serverFirstPlaces)"
                )
                .isEqualTo(serverFirstPlaces)
        }
    }

    @Tag("Unit")
    @Test
    fun `WonRoundCounterCSharp bot receives one WonRoundEvent per round it wins`() {
        val countFile = Path.of(System.getProperty("java.io.tmpdir"), "won_round_csharp.txt")
        countFile.toFile().delete()

        BattleRunner.create { embeddedServer() }.use { runner ->
            val results = runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 10 },
                bots = listOf(
                    BotEntry.of(testBotCsharpDir("WonRoundCounterCSharp")),
                    BotEntry.of(botDir("SpinBot"))
                )
            )

            val serverFirstPlaces = results.results
                .first { it.name == "WonRoundCounterCSharp" }
                .firstPlaces

            val botSideCount = if (countFile.exists())
                countFile.toFile().readText().trim().toIntOrNull() ?: 0
            else 0

            assertThat(botSideCount)
                .describedAs(
                    "WonRoundCounterCSharp should receive exactly as many WonRoundEvents " +
                    "as the server recorded first-place finishes (got $botSideCount, server says $serverFirstPlaces)"
                )
                .isEqualTo(serverFirstPlaces)
        }
    }

    @Tag("Unit")
    @Test
    fun `WonRoundCounterTs bot receives one WonRoundEvent per round it wins`() {
        val countFile = Path.of(System.getProperty("java.io.tmpdir"), "won_round_ts.txt")
        countFile.toFile().delete()

        // Use a longer connect timeout: npm install may run on the first test invocation.
        BattleRunner.create {
            embeddedServer()
            botConnectTimeout(java.time.Duration.ofSeconds(120))
        }.use { runner ->
            val results = runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 10 },
                bots = listOf(
                    BotEntry.of(testBotTsDir("WonRoundCounterTs")),
                    BotEntry.of(botDir("SpinBot"))
                )
            )

            val serverFirstPlaces = results.results
                .first { it.name == "WonRoundCounterTs" }
                .firstPlaces

            val botSideCount = if (countFile.exists())
                countFile.toFile().readText().trim().toIntOrNull() ?: 0
            else 0

            assertThat(botSideCount)
                .describedAs(
                    "WonRoundCounterTs should receive exactly as many WonRoundEvents " +
                    "as the server recorded first-place finishes (got $botSideCount, server says $serverFirstPlaces)"
                )
                .isEqualTo(serverFirstPlaces)
        }
    }

    // -------------------------------------------------------------------------------------
    // captureServerOutput logging behavior — integration (real processes, real JUL capture)
    // -------------------------------------------------------------------------------------

    @Tag("Unit")
    @Test
    fun `server and booter output is logged by default`() {
        val handler = CapturingHandler()
        val rootLogger = Logger.getLogger("")
        val savedLevel = rootLogger.level
        rootLogger.level = Level.ALL
        rootLogger.addHandler(handler)

        try {
            BattleRunner.create { embeddedServer() }.use { runner ->
                runner.runBattle(
                    setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                    bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
                )
            }
            assertThat(handler.messages)
                .describedAs("Expected [SERVER] prefixed lines when captureServerOutput is enabled (default)")
                .anyMatch { it.startsWith("[SERVER]") }
            assertThat(handler.messages)
                .describedAs("Expected [BOOTER] prefixed lines when captureServerOutput is enabled (default)")
                .anyMatch { it.startsWith("[BOOTER]") }
        } finally {
            rootLogger.removeHandler(handler)
            rootLogger.level = savedLevel
        }
    }

    @Tag("Unit")
    @Test
    fun `suppressServerOutput produces no SERVER or BOOTER prefixed log lines`() {
        val handler = CapturingHandler()
        val rootLogger = Logger.getLogger("")
        val savedLevel = rootLogger.level
        rootLogger.level = Level.ALL
        rootLogger.addHandler(handler)

        try {
            BattleRunner.create { embeddedServer(); suppressServerOutput() }.use { runner ->
                runner.runBattle(
                    setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                    bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
                )
            }
            assertThat(handler.messages)
                .describedAs("Expected no [SERVER] prefixed lines when suppressServerOutput() is set")
                .noneMatch { it.startsWith("[SERVER]") }
            assertThat(handler.messages)
                .describedAs("Expected no [BOOTER] prefixed lines when suppressServerOutput() is set")
                .noneMatch { it.startsWith("[BOOTER]") }
        } finally {
            rootLogger.removeHandler(handler)
            rootLogger.level = savedLevel
        }
    }

    // -------------------------------------------------------------------------------------
    // Identity matching — successive battles reset matcher state
    // -------------------------------------------------------------------------------------

    @Tag("Unit")
    @Test
    fun `successive battles with different bot compositions both succeed`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            // Battle 1: Walls vs SpinBot
            val results1 = runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
            )
            assertThat(results1.results).hasSize(2)
            assertThat(results1.results.map { it.name }.toSet())
                .containsExactlyInAnyOrder("Walls", "SpinBot")

            Thread.sleep(2000)

            // Battle 2: completely different bots — matcher state must not leak
            val results2 = runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(BotEntry.of(botDir("Target")), BotEntry.of(botDir("Crazy")))
            )
            assertThat(results2.results).hasSize(2)
            assertThat(results2.results.map { it.name }.toSet())
                .containsExactlyInAnyOrder("Target", "Crazy")
        }
    }

    // -------------------------------------------------------------------------------------
    // Identity matching — timeout error message contains pending identities
    // -------------------------------------------------------------------------------------

    @Tag("Unit")
    @Test
    fun `bot connect timeout produces identity-aware error message`() {
        // Create a valid bot directory whose bot will never connect (booter is not started for it)
        val ghostDir = tempDir.resolve("GhostBot")
        ghostDir.toFile().mkdirs()
        ghostDir.resolve("GhostBot.json").toFile().writeText(
            """{"name":"GhostBot","version":"0.1","authors":"test","gameTypes":["1v1","classic"]}"""
        )

        BattleRunner.create {
            embeddedServer()
            botConnectTimeout(Duration.ofSeconds(3))
        }.use { runner ->
            assertThatThrownBy {
                runner.runBattle(
                    setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                    bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(ghostDir))
                )
            }.isInstanceOf(BattleException::class.java)
                .hasMessageContaining("Bot connect timeout")
                .hasMessageContaining("GhostBot 0.1")
        }
    }

    // -------------------------------------------------------------------------------------
    // 10.7 — Debug mode and breakpoint mode
    // -------------------------------------------------------------------------------------

    @Tag("Unit")
    @Test
    fun `serverFeatures advertises debugMode and breakpointMode`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            runner.startBattleAsync(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
            ).use { handle ->
                assertThat(handle.serverFeatures).isNotNull()
                assertThat(handle.serverFeatures?.debugMode).isTrue()
                assertThat(handle.serverFeatures?.breakpointMode).isTrue()
                handle.awaitResults()
            }
        }
    }

    @Tag("Unit")
    @Test
    fun `enableDebugMode pauses after each turn with debug_step pauseCause`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            runner.startBattleAsync(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
            ).use { handle ->
                val owner = Any()
                val firstPauseLatch = CountDownLatch(1)
                val receivedCauses = mutableListOf<String?>()

                handle.onGamePaused.on(owner) { event ->
                    synchronized(receivedCauses) { receivedCauses.add(event.pauseCause) }
                    firstPauseLatch.countDown()
                }

                handle.onGameStarted.on(owner) { handle.enableDebugMode() }

                assertThat(firstPauseLatch.await(30, TimeUnit.SECONDS))
                    .describedAs("Expected at least one debug_step pause within 30 seconds")
                    .isTrue()

                synchronized(receivedCauses) {
                    assertThat(receivedCauses).isNotEmpty()
                    assertThat(receivedCauses).allMatch { it == "debug_step" }
                }

                handle.resume()
                handle.awaitResults()
            }
        }
    }

    @Tag("Unit")
    @Test
    fun `disableDebugMode exits debug mode and battle completes normally`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            runner.startBattleAsync(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
            ).use { handle ->
                val owner = Any()
                val pausedLatch = CountDownLatch(1)

                handle.onGamePaused.on(owner) { pausedLatch.countDown() }
                handle.onGameStarted.on(owner) { handle.enableDebugMode() }

                assertThat(pausedLatch.await(30, TimeUnit.SECONDS))
                    .describedAs("Expected debug_step pause before disableDebugMode()")
                    .isTrue()

                handle.disableDebugMode()

                val results = handle.awaitResults()
                assertThat(results.results).hasSize(2)
            }
        }
    }

    // -------------------------------------------------------------------------------------
    // WonRound cross-language tests
    // -------------------------------------------------------------------------------------

    @Tag("Unit")
    @Test
    fun `combined Java and CSharp WonRoundEvents sum to number of rounds`() {
        val javaCountFile  = Path.of(System.getProperty("java.io.tmpdir"), "won_round_java.txt")
        val csharpCountFile = Path.of(System.getProperty("java.io.tmpdir"), "won_round_csharp.txt")
        javaCountFile.toFile().delete()
        csharpCountFile.toFile().delete()

        BattleRunner.create { embeddedServer() }.use { runner ->
            val results = runner.runBattle(
                setup = BattleSetup.oneVsOne { numberOfRounds = 10 },
                bots = listOf(
                    BotEntry.of(testBotDir("WonRoundCounterJava")),
                    BotEntry.of(testBotCsharpDir("WonRoundCounterCSharp"))
                )
            )

            assertThat(results.numberOfRounds).isEqualTo(10)

            val javaCount  = javaCountFile.toFile().takeIf { it.exists() }?.readText()?.trim()?.toIntOrNull() ?: 0
            val csharpCount = csharpCountFile.toFile().takeIf { it.exists() }?.readText()?.trim()?.toIntOrNull() ?: 0
            val total = javaCount + csharpCount

            assertThat(total)
                .describedAs(
                    "Across a 10-round battle, exactly 10 WonRoundEvents must be delivered in total " +
                    "(Java got $javaCount, C# got $csharpCount, total = $total)"
                )
                .isEqualTo(10)
        }
    }

    // -------------------------------------------------------------------------------------
    // Breakpoint disconnect regression (issue #206) — slow soak, excluded from normal CI
    // -------------------------------------------------------------------------------------

    /**
     * Verifies that a bot paused at a debugger breakpoint does **not** disconnect from the
     * server within 3 minutes (well past the 60-second WebSocket connection-lost timeout).
     *
     * The bot used here ([BreakpointStallBot]) simulates a frozen JVM:
     *   - It connects and completes the handshake, but never sends [BotIntent] messages.
     *   - It deliberately does **not** respond to WebSocket ping frames, so the server's
     *     connection-lost detection would normally disconnect it after ~60 seconds.
     *
     * With the fix (GameServer disables connection-lost detection on breakpoint pause),
     * the bot must remain connected for the full 3-minute soak period.
     *
     * Tagged [Tag("slow")] — excluded from the default `integrationTest` Gradle task.
     * Run explicitly with `./gradlew :runner:slowIntegrationTest`.
    @Tag("Unit")
     */
    @Tag("Unit")
    @Test
    @Tag("slow")
    @Timeout(value = 6, unit = TimeUnit.MINUTES)
    fun `bot in breakpoint pause stays connected for 3 minutes (issue 206 regression)`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            runner.startBattleAsync(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(
                    BotEntry.of(testBotDir("BreakpointStallBot")),
                    BotEntry.of(botDir("SpinBot"))
                )
            ).use { handle ->
                val owner = Any()
                val breakpointPauseLatch = CountDownLatch(1)
                val botDisconnected = AtomicBoolean(false)

                // Count bots present when the game starts; any BotListUpdate with fewer bots
                // after the breakpoint pause means BreakpointStallBot was disconnected.
                var botCountAtStart = 0
                handle.onGameStarted.on(owner) { event ->
                    botCountAtStart = event.participants.size
                    val stallBotId = event.participants.first { it.name == "BreakpointStallBot" }.id
                    handle.setBotPolicy(stallBotId, breakpointEnabled = true)
                }

                // Track bot disconnections that happen AFTER the breakpoint pause has been confirmed.
                var watchingForDisconnect = false
                handle.onBotListUpdate.on(owner) { update ->
                    if (watchingForDisconnect && update.bots.size < botCountAtStart) {
                        botDisconnected.set(true)
                    }
                }

                handle.onGamePaused.on(owner) { event ->
                    if (event.pauseCause == "breakpoint" && !watchingForDisconnect) {
                        watchingForDisconnect = true
                        breakpointPauseLatch.countDown()
                    }
                }

                // Wait up to 60 s for the game to pause at the breakpoint.
                assertThat(breakpointPauseLatch.await(60, TimeUnit.SECONDS))
                    .describedAs("Game must pause at breakpoint within 60 seconds")
                    .isTrue()

                // Soak for 3 minutes — longer than the 60-second connection-lost timeout.
                // Without the fix the server would disconnect the bot after ~60–80 s.
                Thread.sleep(TimeUnit.MINUTES.toMillis(3))

                assertThat(botDisconnected.get())
                    .describedAs(
                        "BreakpointStallBot must NOT be disconnected by the server during a 3-minute " +
                        "breakpoint pause (regression: issue #206 — connection-lost detection must be " +
                        "disabled while the game is paused for a breakpoint)"
                    )
                    .isFalse()

                // Clean up: stop the battle so the runner can shut down cleanly.
                handle.stop()
            }
        }
    }

    /**
     * Negative counterpart to the positive test above.
     *
     * Verifies that [BreakpointStallBot] **is** disconnected by the server when breakpoint
     * mode is NOT enabled — confirming that the connection-lost detection is only suppressed
     * during a breakpoint pause, not globally.
     *
     * Without breakpoint mode the server keeps its default 60-second connection-lost timeout.
     * [BreakpointStallBot] never sends pong responses, so the server closes its connection
     * after ~120 seconds (one 60-second cycle to send the ping, another to detect no pong).
     *
     * Tagged [Tag("slow")] — excluded from the default `integrationTest` Gradle task.
     * Run explicitly with `./gradlew :runner:slowIntegrationTest`.
    @Tag("Unit")
     */
    @Tag("Unit")
    @Test
    @Tag("slow")
    @Timeout(value = 6, unit = TimeUnit.MINUTES)
    fun `bot without breakpoint mode is disconnected by connection-lost detection (issue 206 negative)`() {
        BattleRunner.create { embeddedServer() }.use { runner ->
            runner.startBattleAsync(
                setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                bots = listOf(
                    BotEntry.of(testBotDir("BreakpointStallBot")),
                    BotEntry.of(botDir("SpinBot"))
                )
            ).use { handle ->
                val owner = Any()
                val botDisconnectLatch = CountDownLatch(1)

                var botCountAtStart = 0
                var watchingForDisconnect = false

                handle.onGameStarted.on(owner) { event ->
                    botCountAtStart = event.participants.size
                    // Breakpoint mode deliberately NOT enabled — fix must not suppress the timeout.
                    watchingForDisconnect = true
                }

                handle.onBotListUpdate.on(owner) { update ->
                    if (watchingForDisconnect && update.bots.size < botCountAtStart) {
                        botDisconnectLatch.countDown()
                    }
                }

                // Wait up to 4 minutes: server pings at ~60 s, closes at ~120 s if no pong.
                // The bot never sends pongs, so it must be disconnected well within this window.
                assertThat(botDisconnectLatch.await(4, TimeUnit.MINUTES))
                    .describedAs(
                        "BreakpointStallBot must be disconnected by connection-lost detection within " +
                        "4 minutes when breakpoint mode is not active (fix must not suppress the " +
                        "timeout globally — only during a breakpoint pause)"
                    )
                    .isTrue()

                handle.stop()
            }
        }
    }

    // -------------------------------------------------------------------------------------
    // Bot color regression — verifies Corners bot colors reach observer tick data
    // -------------------------------------------------------------------------------------

    @Tag("Unit")
    @Test
    fun `Corners bot colors appear in tick event bot states and intent diagnostics`() {
        // Corners sets: body=RED(#ff0000), turret=BLACK(#000000), radar=YELLOW(#ffff00),
        //               bullet=GREEN(#00ff00), scan=GREEN(#00ff00)
        val collectedColors = Collections.synchronizedList(mutableListOf<Map<String, String?>>())

        BattleRunner.create {
            embeddedServer()
            enableIntentDiagnostics()
        }.use { runner ->
            runner.startBattleAsync(
                setup = BattleSetup.classic { numberOfRounds = 1 },
                bots = listOf(BotEntry.of(botDir("Corners")), BotEntry.of(botDir("SpinBot")))
            ).use { handle ->
                val owner = Any()
                handle.onTickEvent.on(owner) { tick ->
                    tick.botStates.forEach { state ->
                        val map = mapOf(
                            "bodyColor"   to state.bodyColor,
                            "turretColor" to state.turretColor,
                            "radarColor"  to state.radarColor,
                            "bulletColor" to state.bulletColor,
                            "scanColor"   to state.scanColor,
                        )
                        if (map.values.any { it != null }) collectedColors.add(map)
                    }
                }
                handle.awaitResults()
            }

            // --- Observer tick assertion: at least one tick must carry Corners' RED body color ---
            val cornersRedTick = collectedColors.firstOrNull {
                it["bodyColor"]?.equals("#ff0000", ignoreCase = true) == true
            }
            assertThat(cornersRedTick)
                .describedAs(
                    "No tick with bodyColor=#ff0000 found. " +
                    "Distinct bodyColors seen: ${collectedColors.map { it["bodyColor"] }.toSet()}"
                )
                .isNotNull()

            // --- Intent diagnostics: Corners' intents must carry bodyColor ---
            val store = runner.intentDiagnostics!!
            val cornersIntents = store.getIntentsForBot("Corners")
            assertThat(cornersIntents).isNotEmpty()

            val firstColorIntent = cornersIntents.firstOrNull { it.intent.bodyColor != null }
            assertThat(firstColorIntent)
                .describedAs("Corners never sent a bodyColor in any BotIntent — color not serialized")
                .isNotNull()
            assertThat(firstColorIntent!!.intent.bodyColor)
                .describedAs("Expected Corners bodyColor #ff0000 in BotIntent")
                .isEqualToIgnoringCase("#ff0000")
        }
    }

    @Tag("Unit")
    @Test
    @Timeout(180)
    fun `CSharp repeated connected restarts preserve turn 1 run state and debug graphics`() {
        BattleRunner.create {
            embeddedServer()
            enableIntentDiagnostics()
        }.use { runner ->
            val botEntries = listOf(
                BotEntry.of(csharpBotDir("PaintingBot")),
                BotEntry.of(csharpBotDir("Target"))
            )
            val setup = BattleSetup.oneVsOne {
                numberOfRounds = 50
                defaultTurnsPerSecond = 5
            }

            val handle = runner.startBattleAsync(setup, botEntries)
            val conn = runner.connection!!
            val store = runner.intentDiagnostics!!
            val botAddresses = conn.latestBotList.get().map { it.botAddress }.toList()

            val game1TickLatch = CountDownLatch(1)
            val policyActiveLatch = CountDownLatch(1)
            val paintingBotId = AtomicReference<Int?>()
            val primeOwner = Any()
            conn.onTickEvent.on(primeOwner) { tick ->
                if (paintingBotId.get() == null) {
                    tick.botStates.firstOrNull { it.name == "Painting Bot" }?.id?.let { id ->
                        paintingBotId.set(id)
                        game1TickLatch.countDown()
                    }
                }
                val id = paintingBotId.get()
                if (id != null && tick.botStates.firstOrNull { it.id == id }?.isDebuggingEnabled == true) {
                    policyActiveLatch.countDown()
                }
            }
            assertThat(game1TickLatch.await(30, TimeUnit.SECONDS))
                .describedAs("Game 1 must produce a PaintingBot tick so the debug policy can be enabled")
                .isTrue()
            handle.setBotPolicy(paintingBotId.get()!!, debuggingEnabled = true)
            assertThat(policyActiveLatch.await(30, TimeUnit.SECONDS))
                .describedAs("Game 1 must observe debug graphics enabled before restarting")
                .isTrue()
            conn.onTickEvent.off(primeOwner)

            val initialStopLatch = CountDownLatch(1)
            val initialStopOwner = Any()
            conn.onGameAborted.once(initialStopOwner) { initialStopLatch.countDown() }
            conn.onGameEnded.once(initialStopOwner) { initialStopLatch.countDown() }
            handle.stop()
            assertThat(initialStopLatch.await(30, TimeUnit.SECONDS))
                .describedAs("Initial game must stop before repeated restarts begin")
                .isTrue()

            for (cycle in 2..6) {
                store.clear()

                val startedLatch = CountDownLatch(1)
                val firstTickLatch = CountDownLatch(1)
                val stopLatch = CountDownLatch(1)
                val cyclePaintingBotId = AtomicReference<Int?>()
                val debugOnTurn1 = AtomicBoolean(false)

                val startOwner = Any()
                conn.onGameStarted.on(startOwner) { event ->
                    cyclePaintingBotId.set(event.participants.firstOrNull { it.name == "Painting Bot" }?.id)
                    handle.setBotPolicy(cyclePaintingBotId.get()!!, debuggingEnabled = true)
                    startedLatch.countDown()
                }

                val tickOwner = Any()
                conn.onTickEvent.on(tickOwner) { tick ->
                    val id = cyclePaintingBotId.get() ?: return@on
                    val botState = tick.botStates.firstOrNull { it.id == id } ?: return@on
                    if (firstTickLatch.count > 0L) {
                        debugOnTurn1.set(botState.isDebuggingEnabled)
                        firstTickLatch.countDown()
                    }
                }

                val stopOwner = Any()
                conn.onGameAborted.once(stopOwner) { stopLatch.countDown() }
                conn.onGameEnded.once(stopOwner) { stopLatch.countDown() }

                conn.startBattle(BattleRunner.toClientGameSetup(setup), botAddresses)

                assertThat(startedLatch.await(30, TimeUnit.SECONDS))
                    .describedAs("Restart cycle $cycle must produce GameStarted")
                    .isTrue()
                assertThat(firstTickLatch.await(30, TimeUnit.SECONDS))
                    .describedAs("Restart cycle $cycle must produce a first tick")
                    .isTrue()

                Thread.sleep(1200)

                val paintingTurn1 = store.getIntentsForBot("Painting Bot")
                    .firstOrNull { it.roundNumber == 1 && it.turnNumber == 1 }
                val targetTurn1 = store.getIntentsForBot("Target")
                    .firstOrNull { it.roundNumber == 1 && it.turnNumber == 1 }

                assertThat(debugOnTurn1.get())
                    .describedAs("C# restart cycle $cycle must keep debug graphics enabled on turn 1")
                    .isTrue()
                assertThat(paintingTurn1)
                    .describedAs("C# restart cycle $cycle must capture PaintingBot turn-1 intent")
                    .isNotNull()
                assertThat(paintingTurn1!!.intent.targetSpeed)
                    .describedAs(
                        "PaintingBot.Run() starts with Forward(100), so cycle $cycle must send targetSpeed=1.0 " +
                            "on turn 1. Regression: turn-1 events were dispatched before Run(), letting " +
                            "OnScannedBot() call Go() before movement was initialized."
                    )
                    .isEqualTo(1.0)
                assertThat(targetTurn1)
                    .describedAs("C# restart cycle $cycle must capture Target turn-1 intent")
                    .isNotNull()
                assertThat(targetTurn1!!.intent.bodyColor)
                    .describedAs(
                        "Target.Run() sets BodyColor white before the first turn. Regression: turn-1 events were " +
                            "dispatched before Run(), producing a default turn-1 intent with no color."
                    )
                    .isEqualToIgnoringCase("#ffffff")

                conn.onGameStarted.off(startOwner)
                conn.onTickEvent.off(tickOwner)

                handle.stop()
                assertThat(stopLatch.await(30, TimeUnit.SECONDS))
                    .describedAs("Restart cycle $cycle must stop cleanly before the next restart")
                    .isTrue()
            }

            handle.close()
        }
    }
}

/**
 * JUL [Handler] that accumulates all published log messages in memory.
 * Thread-safe: the reader thread (ServerManager/BooterManager) writes concurrently with the
 * test thread reading [messages].
 */
private class CapturingHandler : Handler() {
    private val _messages = mutableListOf<String>()
    val messages: List<String> get() = synchronized(_messages) { _messages.toList() }

    init { level = Level.ALL }

    override fun publish(record: LogRecord) {
        record.message?.let { synchronized(_messages) { _messages.add(it) } }
    }

    override fun flush() {}
    override fun close() {}
}
