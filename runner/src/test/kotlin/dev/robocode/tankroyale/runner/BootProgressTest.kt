package dev.robocode.tankroyale.runner

import dev.robocode.tankroyale.client.model.BotInfo
import dev.robocode.tankroyale.client.model.BotListUpdate
import dev.robocode.tankroyale.common.event.Event
import dev.robocode.tankroyale.runner.internal.ServerConnection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Unit tests for [BootProgress] data class and [BattleHandle.onBootProgress] event (task 5.3).
 */
class BootProgressTest {

    // -------------------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------------------

    private fun runner(timeoutMs: Long = 5_000L): BattleRunner =
        BattleRunner.create {
            embeddedServer()
            botConnectTimeout(Duration.ofMillis(timeoutMs))
        }

    private fun conn(): ServerConnection = ServerConnection("ws://localhost:9999", "secret")

    private fun botInfo(name: String, version: String, authors: String, host: String, port: Int = 0): BotInfo =
        BotInfo(
            name = name,
            version = version,
            authors = listOf(authors),
            countryCodes = emptyList(),
            gameTypes = emptySet(),
            host = host,
            port = port,
        )

    private fun emitBots(conn: ServerConnection, vararg bots: BotInfo) {
        conn.onBotListUpdate(BotListUpdate(bots.toSet()))
    }

    // -------------------------------------------------------------------------------------
    // 5.3 — BootProgress unit tests
    // -------------------------------------------------------------------------------------

    @Test
    fun `totalExpected and totalConnected computed correctly`() {
        val progress = BootProgress(
            expected = mapOf(BotIdentity("Alpha", "1.0", "Author") to 2, BotIdentity("Beta", "2.0", "Author") to 3),
            connected = mapOf(BotIdentity("Alpha", "1.0", "Author") to 1),
            pending = mapOf(BotIdentity("Alpha", "1.0", "Author") to 1, BotIdentity("Beta", "2.0", "Author") to 3),
            elapsedMs = 100L,
            timeoutMs = 5_000L,
        )

        assertThat(progress.totalExpected).isEqualTo(5)
        assertThat(progress.totalConnected).isEqualTo(1)
    }

    @Test
    fun `pending equals expected minus connected clamped to zero`() {
        val alpha = BotIdentity("Alpha", "1.0", "Author")
        val beta = BotIdentity("Beta", "2.0", "Author")

        val expected = mapOf(alpha to 2, beta to 1)
        val connected = mapOf(alpha to 1)
        // pending should be: alpha→1, beta→1
        val pending = expected.mapValues { (id, count) ->
            (count - (connected[id] ?: 0)).coerceAtLeast(0)
        }.filter { it.value > 0 }

        assertThat(pending[alpha]).isEqualTo(1)
        assertThat(pending[beta]).isEqualTo(1)

        // If connected exceeds expected, pending should be 0 (clamped)
        val overConnected = mapOf(alpha to 3)
        val pendingClamped = expected.mapValues { (id, count) ->
            (count - (overConnected[id] ?: 0)).coerceAtLeast(0)
        }.filter { it.value > 0 }

        assertThat(pendingClamped[alpha]).isNull() // filtered out (value = 0)
    }

    @Test
    fun `progress event fires on each BotListUpdate`() {
        val conn = conn()
        val identities = listOf(
            BotIdentity("Alpha", "1.0", "Author"),
            BotIdentity("Beta", "2.0", "Author"),
        )

        val alphaInfo = botInfo("Alpha", "1.0", "Author", "localhost", 7001)
        val betaInfo  = botInfo("Beta",  "2.0", "Author", "localhost", 7002)

        val progressEvents = CopyOnWriteArrayList<BootProgress>()

        Thread {
            Thread.sleep(50)
            emitBots(conn, alphaInfo)          // partial update
            Thread.sleep(50)
            emitBots(conn, alphaInfo, betaInfo) // complete update
        }.start()

        val progressEvent = Event<BootProgress>()
        progressEvent.on(this) { progress -> progressEvents.add(progress) }

        runner().use { r ->
            r.waitForBots(conn, emptySet(), identities, identities.size, progressEvent)
        }

        // At least 2 progress events fired (one per BotListUpdate)
        assertThat(progressEvents).hasSizeGreaterThanOrEqualTo(2)

        // First event: only Alpha connected
        val first = progressEvents.first()
        assertThat(first.totalConnected).isEqualTo(1)
        assertThat(first.totalExpected).isEqualTo(2)

        // Last event: both connected
        val last = progressEvents.last()
        assertThat(last.totalConnected).isEqualTo(2)
        assertThat(last.totalExpected).isEqualTo(2)
        assertThat(last.pending).isEmpty()
    }
}
