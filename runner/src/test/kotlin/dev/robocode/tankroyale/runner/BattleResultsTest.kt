package dev.robocode.tankroyale.runner

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Unit tests for [BattleResults] and [BotResult] data classes.
 */
class BattleResultsTest {

    @Test
    fun `BotResult exposes all fields`() {
        val r = BotResult(
            id = 1, name = "TestBot", version = "2.0", isTeam = false,
            rank = 1, totalScore = 310, survival = 50, lastSurvivorBonus = 10,
            bulletDamage = 200, bulletKillBonus = 30, ramDamage = 15, ramKillBonus = 5,
            firstPlaces = 5, secondPlaces = 3, thirdPlaces = 2
        )
        assertThat(r.id).isEqualTo(1)
        assertThat(r.name).isEqualTo("TestBot")
        assertThat(r.version).isEqualTo("2.0")
        assertThat(r.isTeam).isFalse()
        assertThat(r.rank).isEqualTo(1)
        assertThat(r.totalScore).isEqualTo(310)
        assertThat(r.survival).isEqualTo(50)
        assertThat(r.lastSurvivorBonus).isEqualTo(10)
        assertThat(r.bulletDamage).isEqualTo(200)
        assertThat(r.bulletKillBonus).isEqualTo(30)
        assertThat(r.ramDamage).isEqualTo(15)
        assertThat(r.ramKillBonus).isEqualTo(5)
        assertThat(r.firstPlaces).isEqualTo(5)
        assertThat(r.secondPlaces).isEqualTo(3)
        assertThat(r.thirdPlaces).isEqualTo(2)
    }

    @Test
    fun `BotResult equality works`() {
        val a = BotResult(1, "Bot", "1.0", false, 1, 100, 50, 10, 20, 5, 10, 5, 3, 2, 1)
        val b = BotResult(1, "Bot", "1.0", false, 1, 100, 50, 10, 20, 5, 10, 5, 3, 2, 1)
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun `BotResult with different values are not equal`() {
        val a = BotResult(1, "Bot", "1.0", false, 1, 100, 50, 10, 20, 5, 10, 5, 3, 2, 1)
        val b = BotResult(2, "Bot", "1.0", false, 1, 100, 50, 10, 20, 5, 10, 5, 3, 2, 1)
        assertThat(a).isNotEqualTo(b)
    }

    @Test
    fun `BattleResults contains rounds and results`() {
        val bot1 = BotResult(1, "Winner", "1.0", false, 1, 200, 50, 10, 100, 20, 15, 5, 5, 0, 0)
        val bot2 = BotResult(2, "Loser", "1.0", false, 2, 100, 20, 0, 50, 10, 15, 5, 0, 5, 0)
        val results = BattleResults(numberOfRounds = 10, results = listOf(bot1, bot2))

        assertThat(results.numberOfRounds).isEqualTo(10)
        assertThat(results.results).hasSize(2)
        assertThat(results.results[0].name).isEqualTo("Winner")
        assertThat(results.results[1].name).isEqualTo("Loser")
    }

    @Test
    fun `BattleResults with empty results list`() {
        val results = BattleResults(numberOfRounds = 0, results = emptyList())
        assertThat(results.numberOfRounds).isEqualTo(0)
        assertThat(results.results).isEmpty()
    }

    @Test
    fun `BotResult isTeam can be true`() {
        val r = BotResult(1, "TeamBot", "1.0", true, 1, 100, 50, 10, 20, 5, 10, 5, 3, 2, 1)
        assertThat(r.isTeam).isTrue()
    }
}
