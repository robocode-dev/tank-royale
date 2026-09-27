package dev.robocode.tankroyale.runner

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

/** Runs a five-bot team at the trial count against the matched local server and Bot API. */
@Tag("integration")
class TeamMessageLoadIntegrationTest {
    @Test
    @Tag("TML-004")
    @Tag("Performance")
    @Tag("Positive")
    @Timeout(value = 90, unit = TimeUnit.SECONDS)
    fun testTML004_PerformancePositive_fiveBotTeamDeliversMessagesWithoutSkippedTurnsAfterWarmup() {
        val botDir = Path.of(checkNotNull(System.getProperty("testBots.java.dir")))
        val sampleBotDir = Path.of(checkNotNull(System.getProperty("sampleBots.java.dir")))
        val memberDir = botDir.resolve("TeamMessageLoadBot")
        Files.list(memberDir).use { paths ->
            paths.filter { it.fileName.toString().startsWith("team-message-load-") }.forEach(Files::deleteIfExists)
        }
        val ticks = CopyOnWriteArrayList<Long>()
        BattleRunner.create {
            embeddedServer()
            enableIntentDiagnostics()
            enableTurnTimingDiagnostics(tps = 30)
        }.use { runner ->
            runner.startBattleAsync(
                BattleSetup.classic {
                    numberOfRounds = 1
                    minNumberOfParticipants = 2
                    maxNumberOfParticipants = 10
                    defaultTurnsPerSecond = 30
                },
                listOf(BotEntry.of(botDir.resolve("TeamMessageLoadTeam")), BotEntry.of(sampleBotDir.resolve("Walls")))
            ).use { handle ->
                val owner = Any()
                handle.onTickEvent.on(owner) { tick ->
                    if (tick.turnNumber in 11..71) ticks += System.nanoTime()
                }
                val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(35)
                while (ticks.size < 61 && System.nanoTime() < deadline) Thread.sleep(100)
                handle.stop()
            }
            assertEquals(61, ticks.size, "Battle did not reach the measured turns")
            val elapsedSeconds = (ticks.last() - ticks.first()).toDouble() / 1_000_000_000.0
            val measuredTps = 60.0 / elapsedSeconds
            val intents = runner.intentDiagnostics!!.getIntentsForBot("TeamMessageLoadBot")
            val packetCount = intents.sumOf { it.intent.teamMessages?.size ?: 0 }
            val encodedBytes = intents.sumOf { capture ->
                capture.intent.teamMessages?.sumOf { it.message?.toByteArray(Charsets.UTF_8)?.size ?: 0 } ?: 0
            }
            println("Team-message load: TPS=$measuredTps, packets=$packetCount, payloadBytes=$encodedBytes")
            // TPS is diagnostic; host scheduling can vary slightly around the configured 30 TPS target.
            assertEquals(5 * 60, packetCount, "Unexpected number of outgoing batch packets")
            val metrics = Files.list(memberDir).use { paths ->
                paths.filter { it.fileName.toString().startsWith("team-message-load-") }.collect(Collectors.toList())
            }
            assertEquals(5, metrics.size, "Expected metrics from all five team members")
            metrics.forEach { path ->
                val fields = Files.readString(path).trim().split(",", limit = 4)
                assertTrue(fields[0].toInt() >= 72)
                assertEquals(0, fields[2].toInt(), "Message order failed for $path")
                val skippedTurns = fields[3].split('|').filter { it.isNotBlank() && it != "-" }.map(String::toInt)
                val measuredSkippedTurns = skippedTurns.filter { it in 11..71 }
                println("Team-message load skipped turns for ${path.fileName}: $skippedTurns")
                assertEquals(emptyList<Int>(), measuredSkippedTurns, "Skipped turns during measured turns 11–71 for $path")
                assertEquals(4 * 64 * 60, fields[1].toInt(), "Unexpected number of received team messages for $path")
            }
        }
    }
}
