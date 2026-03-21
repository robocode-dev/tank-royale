package dev.robocode.tankroyale.runner

import dev.robocode.tankroyale.client.model.GameEndedEvent
import dev.robocode.tankroyale.client.model.Results
import dev.robocode.tankroyale.common.rules.GameType
import dev.robocode.tankroyale.runner.internal.ServerConnection
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Unit tests for [BattleRunner] orchestration logic — configuration, conversion,
 * validation, and lifecycle. These tests do NOT connect to a real server.
 */
class BattleRunnerTest {

    private var runner: BattleRunner? = null

    @AfterEach
    fun cleanup() {
        runner?.close()
    }

    // -------------------------------------------------------------------------------------
    // GameSetup conversion (8.1)
    // -------------------------------------------------------------------------------------

    @Test
    fun `toClientGameSetup converts classic preset correctly`() {
        val setup = BattleSetup.classic()
        val gs = BattleRunner.toClientGameSetup(setup)

        assertThat(gs.gameType).isEqualTo("classic")
        assertThat(gs.arenaWidth).isEqualTo(setup.arenaWidth)
        assertThat(gs.arenaHeight).isEqualTo(setup.arenaHeight)
        assertThat(gs.minNumberOfParticipants).isEqualTo(setup.minNumberOfParticipants)
        assertThat(gs.maxNumberOfParticipants).isEqualTo(setup.maxNumberOfParticipants)
        assertThat(gs.numberOfRounds).isEqualTo(setup.numberOfRounds)
        assertThat(gs.gunCoolingRate).isEqualTo(setup.gunCoolingRate)
        assertThat(gs.maxInactivityTurns).isEqualTo(setup.maxInactivityTurns)
        assertThat(gs.turnTimeout).isEqualTo(setup.turnTimeoutMicros)
        assertThat(gs.readyTimeout).isEqualTo(setup.readyTimeoutMicros)
        assertThat(gs.defaultTurnsPerSecond).isEqualTo(-1)
    }

    @Test
    fun `toClientGameSetup converts 1v1 preset correctly`() {
        val setup = BattleSetup.oneVsOne { numberOfRounds = 3 }
        val gs = BattleRunner.toClientGameSetup(setup)

        assertThat(gs.gameType).isEqualTo("1v1")
        assertThat(gs.numberOfRounds).isEqualTo(3)
        assertThat(gs.minNumberOfParticipants).isEqualTo(2)
        assertThat(gs.maxNumberOfParticipants).isEqualTo(2)
    }

    @Test
    fun `toClientGameSetup converts melee preset correctly`() {
        val setup = BattleSetup.melee()
        val gs = BattleRunner.toClientGameSetup(setup)

        assertThat(gs.gameType).isEqualTo("melee")
    }

    @Test
    fun `toClientGameSetup converts custom preset correctly`() {
        val setup = BattleSetup.custom { arenaWidth = 1200; arenaHeight = 900 }
        val gs = BattleRunner.toClientGameSetup(setup)

        assertThat(gs.gameType).isEqualTo("custom")
        assertThat(gs.arenaWidth).isEqualTo(1200)
        assertThat(gs.arenaHeight).isEqualTo(900)
    }

    @Test
    fun `toClientGameSetup sets all locked fields to false`() {
        val setup = BattleSetup.classic()
        val gs = BattleRunner.toClientGameSetup(setup)

        assertThat(gs.isArenaWidthLocked).isFalse()
        assertThat(gs.isArenaHeightLocked).isFalse()
        assertThat(gs.isMinNumberOfParticipantsLocked).isFalse()
        assertThat(gs.isMaxNumberOfParticipantsLocked).isFalse()
        assertThat(gs.isNumberOfRoundsLocked).isFalse()
        assertThat(gs.isGunCoolingRateLocked).isFalse()
        assertThat(gs.isMaxInactivityTurnsLocked).isFalse()
        assertThat(gs.isTurnTimeoutLocked).isFalse()
        assertThat(gs.isReadyTimeoutLocked).isFalse()
    }

    // -------------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------------
    // captureServerOutput config (Builder)
    // -------------------------------------------------------------------------------------

    @Test
    fun `default config has captureServerOutput true`() {
        runner = BattleRunner.create { embeddedServer() }
        assertThat(runner!!.config.captureServerOutput).isTrue()
    }

    @Test
    fun `suppressServerOutput sets captureServerOutput to false`() {
        runner = BattleRunner.create { embeddedServer(); suppressServerOutput() }
        assertThat(runner!!.config.captureServerOutput).isFalse()
    }

    @Test
    fun `suppressServerOutput is chainable with other builder calls`() {
        runner = BattleRunner.create { embeddedServer().suppressServerOutput().enableIntentDiagnostics() }
        assertThat(runner!!.config.captureServerOutput).isFalse()
        assertThat(runner!!.config.intentDiagnosticsEnabled).isTrue()
    }

    @Test
    fun `create with no arguments defaults captureServerOutput to true`() {
        runner = BattleRunner.create()
        assertThat(runner!!.config.captureServerOutput).isTrue()
    }

    // -------------------------------------------------------------------------------------
    // Lifecycle (8.4)
    // -------------------------------------------------------------------------------------

    @Test
    fun `close is idempotent`() {
        runner = BattleRunner.create { embeddedServer() }
        runner!!.close()
        runner!!.close() // should not throw
        runner = null // already closed
    }

    @Test
    fun `startBattleAsync throws after close`() {
        runner = BattleRunner.create { embeddedServer() }
        runner!!.close()

        assertThatThrownBy {
            runner!!.startBattleAsync(BattleSetup.classic(), emptyList())
        }.isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("closed")

        runner = null // already closed
    }

    // -------------------------------------------------------------------------------------
    // BattleHandle — awaitResults (8.2)
    // -------------------------------------------------------------------------------------

    @Test
    fun `BattleHandle awaitResults returns when game ends`() {
        val conn = ServerConnection("ws://localhost:9999", "secret")
        val closeLatch = CountDownLatch(1)

        val handle = BattleHandle(conn) { closeLatch.countDown() }

        // Simulate GameEndedEvent
        val event = GameEndedEvent(
            numberOfRounds = 5,
            results = listOf(
                Results(
                    id = 1, name = "Bot", version = "1.0", isTeam = false,
                    rank = 1, survival = 50, lastSurvivorBonus = 10,
                    bulletDamage = 200, bulletKillBonus = 30,
                    ramDamage = 15, ramKillBonus = 5,
                    totalScore = 310, firstPlaces = 5, secondPlaces = 0, thirdPlaces = 0
                ),
            )
        )

        // Fire event on a background thread
        Thread {
            Thread.sleep(100)
            conn.onGameEnded(event)
        }.start()

        val results = handle.awaitResults()
        assertThat(results.numberOfRounds).isEqualTo(5)
        assertThat(results.results).hasSize(1)
        assertThat(results.results[0].name).isEqualTo("Bot")

        handle.close()
        assertThat(closeLatch.await(1, TimeUnit.SECONDS)).isTrue()
    }

    @Test
    fun `BattleHandle awaitResults throws on game abort`() {
        val conn = ServerConnection("ws://localhost:9999", "secret")
        val handle = BattleHandle(conn) {}

        // Fire abort on a background thread
        Thread {
            Thread.sleep(100)
            conn.onGameAborted(dev.robocode.tankroyale.client.model.GameAbortedEvent)
        }.start()

        assertThatThrownBy { handle.awaitResults() }
            .isInstanceOf(BattleException::class.java)
            .hasMessageContaining("aborted")

        handle.close()
    }

    @Test
    fun `BattleHandle close unsubscribes event handlers`() {
        val conn = ServerConnection("ws://localhost:9999", "secret")
        val handle = BattleHandle(conn) {}

        handle.close()

        // Fire event after close — should not affect handle
        conn.onGameEnded(
            GameEndedEvent(
                numberOfRounds = 1,
                results = emptyList()
            )
        )
        // No assertion needed — just verifying no exceptions
    }
}
