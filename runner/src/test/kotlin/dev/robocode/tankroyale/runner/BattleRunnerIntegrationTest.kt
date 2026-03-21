package dev.robocode.tankroyale.runner

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.*
import org.junit.jupiter.api.io.TempDir
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path
import java.time.Duration
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import java.util.zip.GZIPInputStream
import kotlin.io.path.exists

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
    }

    // -------------------------------------------------------------------------------------
    // 10.2 — Run a real battle with sample bots
    // -------------------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------------------
    // WonRoundEvent delivery verification — 10-round battle
    // -------------------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------------------
    // captureServerOutput logging behavior — integration (real processes, real JUL capture)
    // -------------------------------------------------------------------------------------

    @Test
    fun `server and booter output is logged with SERVER and BOOTER prefixes when enableServerOutput is set`() {
        val handler = CapturingHandler()
        val rootLogger = Logger.getLogger("")
        val savedLevel = rootLogger.level
        rootLogger.level = Level.ALL
        rootLogger.addHandler(handler)

        try {
            BattleRunner.create { embeddedServer(); enableServerOutput() }.use { runner ->
                runner.runBattle(
                    setup = BattleSetup.oneVsOne { numberOfRounds = 1 },
                    bots = listOf(BotEntry.of(botDir("Walls")), BotEntry.of(botDir("SpinBot")))
                )
            }
            assertThat(handler.messages)
                .describedAs("Expected [SERVER] prefixed lines when captureServerOutput is enabled")
                .anyMatch { it.startsWith("[SERVER]") }
            assertThat(handler.messages)
                .describedAs("Expected [BOOTER] prefixed lines when captureServerOutput is enabled")
                .anyMatch { it.startsWith("[BOOTER]") }
        } finally {
            rootLogger.removeHandler(handler)
            rootLogger.level = savedLevel
        }
    }

    @Test
    fun `server and booter output is not logged by default`() {
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
                .describedAs("Expected no [SERVER] prefixed lines by default (captureServerOutput is disabled)")
                .noneMatch { it.startsWith("[SERVER]") }
            assertThat(handler.messages)
                .describedAs("Expected no [BOOTER] prefixed lines by default (captureServerOutput is disabled)")
                .noneMatch { it.startsWith("[BOOTER]") }
        } finally {
            rootLogger.removeHandler(handler)
            rootLogger.level = savedLevel
        }
    }

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

    @Test
    fun `bot connect timeout produces identity-aware error message`() {
        // Create a valid bot directory whose bot will never connect (booter is not started for it)
        val ghostDir = tempDir.resolve("GhostBot")
        ghostDir.toFile().mkdirs()
        ghostDir.resolve("GhostBot.json").toFile().writeText(
            """{"name":"GhostBot","version":"0.1","authors":["test"],"gameTypes":["1v1","classic"]}"""
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
    // WonRound cross-language tests
    // -------------------------------------------------------------------------------------

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
