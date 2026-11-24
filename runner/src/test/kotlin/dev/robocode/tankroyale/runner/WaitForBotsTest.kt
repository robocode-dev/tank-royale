package dev.robocode.tankroyale.runner

import dev.robocode.tankroyale.client.model.BotAddress
import dev.robocode.tankroyale.client.model.BotInfo
import dev.robocode.tankroyale.client.model.BotListUpdate
import dev.robocode.tankroyale.runner.internal.ServerConnection
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Tag
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
    // 4.4 — waitForBots tests
    // -------------------------------------------------------------------------------------

    @Tag("Unit")
    @Test
    fun `matching identities emitted via BotListUpdate returns correct BotAddress set`() {
        val conn = conn()
        val identities = listOf(
            BotIdentity("Alpha", "1.0", "Author"),
            BotIdentity("Beta", "2.0", "Author"),
        )

        val alphaInfo = botInfo("Alpha", "1.0", "Author", "localhost", 7001)
        val betaInfo  = botInfo("Beta",  "2.0", "Author", "localhost", 7002)

        // Emit matching bots on a background thread after a short delay
        Thread {
            Thread.sleep(50)
            emitBots(conn, alphaInfo, betaInfo)
        }.start()

        runner().use { r ->
            val result = r.waitForBots(conn, emptySet(), identities, 2)
            assertThat(result).containsExactlyInAnyOrder(
                BotAddress("localhost", 7001),
                BotAddress("localhost", 7002),
            )
        }
    }

    @Tag("Unit")
    @Test
    fun `partial updates complete only when all identities matched`() {
        val conn = conn()
        val identities = listOf(
            BotIdentity("Alpha", "1.0", "Author"),
            BotIdentity("Beta", "2.0", "Author"),
        )

        val alphaInfo = botInfo("Alpha", "1.0", "Author", "localhost", 7001)
        val betaInfo  = botInfo("Beta",  "2.0", "Author", "localhost", 7002)

        Thread {
            Thread.sleep(50)
            // First update: only Alpha
            emitBots(conn, alphaInfo)
            Thread.sleep(50)
            // Second update: both Alpha and Beta
            emitBots(conn, alphaInfo, betaInfo)
        }.start()

        runner().use { r ->
            val result = r.waitForBots(conn, emptySet(), identities, 2)
            assertThat(result).containsExactlyInAnyOrder(
                BotAddress("localhost", 7001),
                BotAddress("localhost", 7002),
            )
        }
    }

    @Tag("Unit")
    @Test
    fun `timeout throws BattleException with identity-aware message`() {
        val conn = conn()
        val identities = listOf(
            BotIdentity("Alpha", "1.0", "Author"),
            BotIdentity("Beta", "2.0", "Author"),
        )

        val alphaInfo = botInfo("Alpha", "1.0", "Author", "localhost", 7001)

        Thread {
            Thread.sleep(20)
            // Only Alpha connects — Beta never arrives
            emitBots(conn, alphaInfo)
        }.start()

        runner(timeoutMs = 200L).use { r ->
            assertThatThrownBy {
                r.waitForBots(conn, emptySet(), identities, 2)
            }.isInstanceOf(BattleException::class.java)
                .hasMessageContaining("Bot connect timeout")
                .hasMessageContaining("connected 1 of 2")
                .hasMessageContaining("Beta 2.0")
        }
    }

    @Tag("Unit")
    @Test
    fun `pre-existing bots are excluded from matching`() {
        val conn = conn()
        val identities = listOf(BotIdentity("Alpha", "1.0", "Author"))

        val preExisting = botInfo("Alpha", "1.0", "Author", "localhost", 7000)
        val newBot      = botInfo("Alpha", "1.0", "Author", "localhost", 7001)

        val preExistingAddresses = setOf(BotAddress("localhost", 7000))

        Thread {
            Thread.sleep(50)
            // Both pre-existing and new bot appear in the update
            emitBots(conn, preExisting, newBot)
        }.start()

        runner().use { r ->
            val result = r.waitForBots(conn, preExistingAddresses, identities, 1)
            assertThat(result).containsExactly(BotAddress("localhost", 7001))
            assertThat(result).doesNotContain(BotAddress("localhost", 7000))
        }
    }

    // -------------------------------------------------------------------------------------
    // BR-053 — a bot whose process ended before it connected
    // -------------------------------------------------------------------------------------

    private fun botFolder(root: java.nio.file.Path, name: String, version: String): java.nio.file.Path =
        java.nio.file.Files.createDirectories(root.resolve(name)).also {
            java.nio.file.Files.writeString(it.resolve("$name.json"), """{"name":"$name","version":"$version","authors":["Author"]}""")
        }

    @Tag("BR-053")
    @Tag("Unit")
    @Tag("Positive")
    @Test
    fun `a bot whose process ended before it connected ends the wait at once and is named`(@org.junit.jupiter.api.io.TempDir dir: java.nio.file.Path) {
        val conn = conn()
        val alpha = BotIdentity("Alpha", "1.0", "Author")
        val beta = BotIdentity("Beta", "2.0", "Author")
        val betaDir = botFolder(dir, "Beta", "2.0")
        val ended = java.util.concurrent.CopyOnWriteArrayList<String>()

        val started = System.currentTimeMillis()
        runner(timeoutMs = 20_000L).use { r ->
            assertThatThrownBy {
                r.waitForBots(conn, emptySet(), listOf(alpha, beta), 2, onSubscribed = {
                    emitBots(conn, botInfo("Alpha", "1.0", "Author", "localhost", 7001))
                    ended += betaDir.toString()
                }, exitedDirs = { ended })
            }.isInstanceOf(BattleException::class.java)
                .satisfies({ assertThat((it as BattleException).notConnected).containsExactly(beta) })
                .hasMessageContaining("ended before it connected")
        }
        assertThat(System.currentTimeMillis() - started).isLessThan(5_000L)

        // The booter's line for a process that ended by itself names its folder; one it stopped does not.
        val booter = dev.robocode.tankroyale.runner.internal.BooterManager("ws://localhost:1", "s")
        val latch = java.util.concurrent.CountDownLatch(2)
        booter.parseLine("41;${betaDir}", latch)
        booter.parseLine("42;${dir.resolve("Alpha")}", latch)
        booter.parseLine("exited 41", latch)
        booter.parseLine("stopped 42", latch)
        assertThat(booter.exitedDirs).containsExactly(betaDir.toString())
    }

    @Tag("BR-053")
    @Tag("Unit")
    @Tag("Negative")
    @Test
    fun `a bot that runs without connecting is waited for until the limit, and an ended bot that had connected changes nothing`(@org.junit.jupiter.api.io.TempDir dir: java.nio.file.Path) {
        val conn = conn()
        val alpha = BotIdentity("Alpha", "1.0", "Author")
        val beta = BotIdentity("Beta", "2.0", "Author")
        val alphaDir = botFolder(dir, "Alpha", "1.0")

        val started = System.currentTimeMillis()
        runner(timeoutMs = 1_500L).use { r ->
            assertThatThrownBy {
                // Alpha connected and then its process ended; Beta never connects but its process runs.
                r.waitForBots(conn, emptySet(), listOf(alpha, beta), 2, onSubscribed = {
                    emitBots(conn, botInfo("Alpha", "1.0", "Author", "localhost", 7001))
                }, exitedDirs = { listOf(alphaDir.toString()) })
            }.isInstanceOf(BattleException::class.java)
                .satisfies({ assertThat((it as BattleException).notConnected).containsExactly(beta) })
                .hasMessageContaining("connect timeout")
        }
        assertThat(System.currentTimeMillis() - started).isGreaterThanOrEqualTo(1_400L)
    }
}
