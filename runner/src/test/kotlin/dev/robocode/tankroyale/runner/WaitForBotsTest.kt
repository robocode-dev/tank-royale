package dev.robocode.tankroyale.runner

import dev.robocode.tankroyale.client.model.BotAddress
import dev.robocode.tankroyale.client.model.BotInfo
import dev.robocode.tankroyale.client.model.BotListUpdate
import dev.robocode.tankroyale.runner.internal.ServerConnection
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.Duration

/**
 * Unit tests for [BattleRunner.waitForBots] — identity-aware bot matching (task 4.4).
 *
 * Tests fire [ServerConnection.onBotListUpdate] events directly without a real server connection.
 */
class WaitForBotsTest {

    // -------------------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------------------

    private fun runner(timeoutMs: Long = 5_000L): BattleRunner =
        BattleRunner.create {
            embeddedServer()
            botConnectTimeout(Duration.ofMillis(timeoutMs))
        }

    private fun conn(): ServerConnection = ServerConnection("ws://localhost:9999", "secret")

    private fun botInfo(name: String, version: String, host: String, port: Int = 0): BotInfo =
        BotInfo(
            name = name,
            version = version,
            authors = emptyList(),
            countryCodes = emptyList(),
            gameTypes = emptySet(),
            host = host,
            port = port,
        )

    private fun emitBots(conn: ServerConnection, vararg bots: BotInfo) {
        conn.onBotListUpdate(BotListUpdate(bots.toSet()))
    }

    // -------------------------------------------------------------------------------------
    // 4.4 — waitForBots tests
    // -------------------------------------------------------------------------------------

    @Test
    fun `matching identities emitted via BotListUpdate returns correct BotAddress set`() {
        val conn = conn()
        val identities = listOf(
            BotIdentity("Alpha", "1.0"),
            BotIdentity("Beta", "2.0"),
        )

        val alphaInfo = botInfo("Alpha", "1.0", "localhost", 7001)
        val betaInfo  = botInfo("Beta",  "2.0", "localhost", 7002)

        // Emit matching bots on a background thread after a short delay
        Thread {
            Thread.sleep(50)
            emitBots(conn, alphaInfo, betaInfo)
        }.start()

        runner().use { r ->
            val result = r.waitForBots(conn, emptySet(), identities)
            assertThat(result).containsExactlyInAnyOrder(
                BotAddress("localhost", 7001),
                BotAddress("localhost", 7002),
            )
        }
    }

    @Test
    fun `partial updates complete only when all identities matched`() {
        val conn = conn()
        val identities = listOf(
            BotIdentity("Alpha", "1.0"),
            BotIdentity("Beta", "2.0"),
        )

        val alphaInfo = botInfo("Alpha", "1.0", "localhost", 7001)
        val betaInfo  = botInfo("Beta",  "2.0", "localhost", 7002)

        Thread {
            Thread.sleep(50)
            // First update: only Alpha
            emitBots(conn, alphaInfo)
            Thread.sleep(50)
            // Second update: both Alpha and Beta
            emitBots(conn, alphaInfo, betaInfo)
        }.start()

        runner().use { r ->
            val result = r.waitForBots(conn, emptySet(), identities)
            assertThat(result).containsExactlyInAnyOrder(
                BotAddress("localhost", 7001),
                BotAddress("localhost", 7002),
            )
        }
    }

    @Test
    fun `timeout throws BattleException with identity-aware message`() {
        val conn = conn()
        val identities = listOf(
            BotIdentity("Alpha", "1.0"),
            BotIdentity("Beta", "2.0"),
        )

        val alphaInfo = botInfo("Alpha", "1.0", "localhost", 7001)

        Thread {
            Thread.sleep(20)
            // Only Alpha connects — Beta never arrives
            emitBots(conn, alphaInfo)
        }.start()

        runner(timeoutMs = 200L).use { r ->
            assertThatThrownBy {
                r.waitForBots(conn, emptySet(), identities)
            }.isInstanceOf(BattleException::class.java)
                .hasMessageContaining("Bot connect timeout")
                .hasMessageContaining("connected 1 of 2")
                .hasMessageContaining("Beta 2.0")
        }
    }

    @Test
    fun `pre-existing bots are excluded from matching`() {
        val conn = conn()
        val identities = listOf(BotIdentity("Alpha", "1.0"))

        val preExisting = botInfo("Alpha", "1.0", "localhost", 7000)
        val newBot      = botInfo("Alpha", "1.0", "localhost", 7001)

        val preExistingAddresses = setOf(BotAddress("localhost", 7000))

        Thread {
            Thread.sleep(50)
            // Both pre-existing and new bot appear in the update
            emitBots(conn, preExisting, newBot)
        }.start()

        runner().use { r ->
            val result = r.waitForBots(conn, preExistingAddresses, identities)
            assertThat(result).containsExactly(BotAddress("localhost", 7001))
            assertThat(result).doesNotContain(BotAddress("localhost", 7000))
        }
    }
}
