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
    fun testTML004_PerformancePositive_fiveBotTeamSustains30TpsWithoutSkippedTurns() {
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
                    if (tick.turnNumber in 2..62) ticks += System.nanoTime()
                }
                val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(35)
                while (ticks.size < 61 && System.nanoTime() < deadline) Thread.sleep(100)
                handle.stop()
            }
            assertEquals(61, ticks.size, "Battle did not reach the measured turns")
            val elapsedSeconds = (ticks.last() - ticks.first()).toDouble() / 1_000_000_000.0
            val measuredTps = 60.0 / elapsedSeconds
            val intents = runner.intentDiagnostics!!.getIntentsForBot("TeamMessageLoadBot")
            val messageCount = intents.sumOf { it.intent.teamMessages?.size ?: 0 }
            val encodedBytes = intents.sumOf { capture ->
                capture.intent.teamMessages?.sumOf { it.message?.toByteArray(Charsets.UTF_8)?.size ?: 0 } ?: 0
            }
            println("Team-message load: TPS=$measuredTps, messages=$messageCount, payloadBytes=$encodedBytes")
            assertTrue(measuredTps >= 30.0, "Messaging load fell below 30 TPS: $measuredTps")
            val metrics = Files.list(memberDir).use { paths ->
                paths.filter { it.fileName.toString().startsWith("team-message-load-") }.collect(Collectors.toList())
            }
            assertEquals(5, metrics.size, "Expected metrics from all five team members")
            metrics.forEach { path ->
                val fields = Files.readString(path).trim().split(",").map(String::toInt)
                assertTrue(fields[0] >= 62)
                assertEquals(0, fields[2], "Message order failed for $path")
                assertEquals(0, fields[3], "Skipped turns recorded by $path")
                assertTrue(fields[1] >= 4 * 64 * 60, "Missing team messages for $path: ${fields[1]}")
            }
        }
    }
}
