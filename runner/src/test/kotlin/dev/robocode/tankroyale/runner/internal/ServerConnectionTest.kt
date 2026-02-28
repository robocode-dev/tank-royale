package dev.robocode.tankroyale.runner.internal

import dev.robocode.tankroyale.client.model.GameEndedEvent
import dev.robocode.tankroyale.client.model.Results
import dev.robocode.tankroyale.runner.BattleException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

/**
 * Unit tests for [ServerConnection] — configuration, result mapping, and preconditions.
 * These tests do NOT connect to a real server.
 */
class ServerConnectionTest {

    // -------------------------------------------------------------------------------------
    // Initial state
    // -------------------------------------------------------------------------------------

    @Test
    fun `new instance is not connected`() {
        val conn = ServerConnection("ws://localhost:7654", "secret")
        assertThat(conn.isConnected).isFalse()
    }

    @Test
    fun `close on unconnected instance is a no-op`() {
        val conn = ServerConnection("ws://localhost:7654", "secret")
        conn.close()
    }

    @Test
    fun `stopBattle on unconnected instance is a no-op`() {
        val conn = ServerConnection("ws://localhost:7654", "secret")
        // stopBattle() has a soft guard (returns silently if not connected)
        conn.stopBattle()
    }

    @Test
    fun `pauseBattle throws when not connected`() {
        val conn = ServerConnection("ws://localhost:7654", "secret")
        assertThatThrownBy { conn.pauseBattle() }
            .isInstanceOf(BattleException::class.java)
            .hasMessageContaining("Not connected")
    }

    @Test
    fun `resumeBattle throws when not connected`() {
        val conn = ServerConnection("ws://localhost:7654", "secret")
        assertThatThrownBy { conn.resumeBattle() }
            .isInstanceOf(BattleException::class.java)
            .hasMessageContaining("Not connected")
    }

    @Test
    fun `nextTurn throws when not connected`() {
        val conn = ServerConnection("ws://localhost:7654", "secret")
        assertThatThrownBy { conn.nextTurn() }
            .isInstanceOf(BattleException::class.java)
            .hasMessageContaining("Not connected")
    }

    // -------------------------------------------------------------------------------------
    // 6.9 — BattleResults extraction
    // -------------------------------------------------------------------------------------

    @Test
    fun `toBattleResults maps GameEndedEvent correctly`() {
        val event = GameEndedEvent(
            numberOfRounds = 10,
            results = listOf(
                Results(
                    id = 1, name = "BotA", version = "1.0", isTeam = false,
                    rank = 1, survival = 50, lastSurvivorBonus = 10,
                    bulletDamage = 200, bulletKillBonus = 30,
                    ramDamage = 15, ramKillBonus = 5,
                    totalScore = 310, firstPlaces = 8, secondPlaces = 2, thirdPlaces = 0
                ),
                Results(
                    id = 2, name = "BotB", version = "2.0", isTeam = true,
                    rank = 2, survival = 30, lastSurvivorBonus = 0,
                    bulletDamage = 100, bulletKillBonus = 10,
                    ramDamage = 5, ramKillBonus = 0,
                    totalScore = 145, firstPlaces = 2, secondPlaces = 8, thirdPlaces = 0
                ),
            )
        )

        val results = ServerConnection.toBattleResults(event)

        assertThat(results.numberOfRounds).isEqualTo(10)
        assertThat(results.results).hasSize(2)

        val first = results.results[0]
        assertThat(first.id).isEqualTo(1)
        assertThat(first.name).isEqualTo("BotA")
        assertThat(first.version).isEqualTo("1.0")
        assertThat(first.isTeam).isFalse()
        assertThat(first.rank).isEqualTo(1)
        assertThat(first.totalScore).isEqualTo(310)
        assertThat(first.survival).isEqualTo(50)
        assertThat(first.lastSurvivorBonus).isEqualTo(10)
        assertThat(first.bulletDamage).isEqualTo(200)
        assertThat(first.bulletKillBonus).isEqualTo(30)
        assertThat(first.ramDamage).isEqualTo(15)
        assertThat(first.ramKillBonus).isEqualTo(5)
        assertThat(first.firstPlaces).isEqualTo(8)
        assertThat(first.secondPlaces).isEqualTo(2)
        assertThat(first.thirdPlaces).isEqualTo(0)

        val second = results.results[1]
        assertThat(second.isTeam).isTrue()
        assertThat(second.rank).isEqualTo(2)
    }

    @Test
    fun `toBattleResults handles null isTeam as false`() {
        val event = GameEndedEvent(
            numberOfRounds = 1,
            results = listOf(
                Results(
                    id = 1, name = "Bot", version = "1.0", isTeam = null,
                    rank = 1, survival = 0, lastSurvivorBonus = 0,
                    bulletDamage = 0, bulletKillBonus = 0,
                    ramDamage = 0, ramKillBonus = 0,
                    totalScore = 0, firstPlaces = 0, secondPlaces = 0, thirdPlaces = 0
                ),
            )
        )

        val results = ServerConnection.toBattleResults(event)
        assertThat(results.results[0].isTeam).isFalse()
    }
}
