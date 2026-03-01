package dev.robocode.tankroyale.runner

import dev.robocode.tankroyale.common.rules.GameType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

/**
 * Unit tests for [BattleSetup] — preset defaults, builder overrides, and all game type factories.
 */
class BattleSetupTest {

    // -------------------------------------------------------------------------------------
    // Preset defaults
    // -------------------------------------------------------------------------------------

    @Test
    fun `classic preset has expected defaults`() {
        val setup = BattleSetup.classic()
        assertThat(setup.gameType).isEqualTo(GameType.CLASSIC)
        assertThat(setup.arenaWidth).isEqualTo(800)
        assertThat(setup.arenaHeight).isEqualTo(600)
        assertThat(setup.minNumberOfParticipants).isEqualTo(2)
        assertThat(setup.maxNumberOfParticipants).isNull()
        assertThat(setup.numberOfRounds).isEqualTo(10)
        assertThat(setup.gunCoolingRate).isEqualTo(0.1)
        assertThat(setup.maxInactivityTurns).isEqualTo(450)
        assertThat(setup.turnTimeoutMicros).isEqualTo(30_000)
        assertThat(setup.readyTimeoutMicros).isEqualTo(1_000_000)
    }

    @Test
    fun `melee preset has expected defaults`() {
        val setup = BattleSetup.melee()
        assertThat(setup.gameType).isEqualTo(GameType.MELEE)
        assertThat(setup.arenaWidth).isEqualTo(1000)
        assertThat(setup.arenaHeight).isEqualTo(1000)
        assertThat(setup.minNumberOfParticipants).isEqualTo(10)
        assertThat(setup.maxNumberOfParticipants).isNull()
        assertThat(setup.numberOfRounds).isEqualTo(10)
    }

    @Test
    fun `oneVsOne preset has expected defaults`() {
        val setup = BattleSetup.oneVsOne()
        assertThat(setup.gameType).isEqualTo(GameType.ONE_VS_ONE)
        assertThat(setup.arenaWidth).isEqualTo(800)
        assertThat(setup.arenaHeight).isEqualTo(600)
        assertThat(setup.minNumberOfParticipants).isEqualTo(2)
        assertThat(setup.maxNumberOfParticipants).isEqualTo(2)
        assertThat(setup.numberOfRounds).isEqualTo(10)
    }

    @Test
    fun `custom preset has expected defaults`() {
        val setup = BattleSetup.custom()
        assertThat(setup.gameType).isEqualTo(GameType.CUSTOM)
        assertThat(setup.arenaWidth).isEqualTo(800)
        assertThat(setup.arenaHeight).isEqualTo(600)
        assertThat(setup.minNumberOfParticipants).isEqualTo(2)
        assertThat(setup.maxNumberOfParticipants).isNull()
        assertThat(setup.numberOfRounds).isEqualTo(10)
        assertThat(setup.gunCoolingRate).isEqualTo(0.1)
        assertThat(setup.maxInactivityTurns).isEqualTo(450)
        assertThat(setup.turnTimeoutMicros).isEqualTo(30_000)
        assertThat(setup.readyTimeoutMicros).isEqualTo(1_000_000)
    }

    // -------------------------------------------------------------------------------------
    // Builder overrides
    // -------------------------------------------------------------------------------------

    @Test
    fun `classic preset allows overriding numberOfRounds`() {
        val setup = BattleSetup.classic { numberOfRounds = 25 }
        assertThat(setup.gameType).isEqualTo(GameType.CLASSIC)
        assertThat(setup.numberOfRounds).isEqualTo(25)
        // non-overridden fields retain preset defaults
        assertThat(setup.arenaWidth).isEqualTo(800)
        assertThat(setup.arenaHeight).isEqualTo(600)
    }

    @Test
    fun `custom preset allows overriding all fields`() {
        val setup = BattleSetup.custom {
            arenaWidth = 1200
            arenaHeight = 900
            minNumberOfParticipants = 4
            maxNumberOfParticipants = 8
            numberOfRounds = 50
            gunCoolingRate = 0.2
            maxInactivityTurns = 300
            turnTimeoutMicros = 50_000
            readyTimeoutMicros = 500_000
        }
        assertThat(setup.arenaWidth).isEqualTo(1200)
        assertThat(setup.arenaHeight).isEqualTo(900)
        assertThat(setup.minNumberOfParticipants).isEqualTo(4)
        assertThat(setup.maxNumberOfParticipants).isEqualTo(8)
        assertThat(setup.numberOfRounds).isEqualTo(50)
        assertThat(setup.gunCoolingRate).isEqualTo(0.2)
        assertThat(setup.maxInactivityTurns).isEqualTo(300)
        assertThat(setup.turnTimeoutMicros).isEqualTo(50_000)
        assertThat(setup.readyTimeoutMicros).isEqualTo(500_000)
    }

    @Test
    fun `melee preset allows overriding numberOfRounds`() {
        val setup = BattleSetup.melee { numberOfRounds = 3 }
        assertThat(setup.numberOfRounds).isEqualTo(3)
        // non-overridden fields retain preset defaults
        assertThat(setup.arenaWidth).isEqualTo(1000)
        assertThat(setup.minNumberOfParticipants).isEqualTo(10)
    }

    @Test
    fun `oneVsOne preset allows overriding numberOfRounds`() {
        val setup = BattleSetup.oneVsOne { numberOfRounds = 50 }
        assertThat(setup.numberOfRounds).isEqualTo(50)
        assertThat(setup.maxNumberOfParticipants).isEqualTo(2)
    }

    // -------------------------------------------------------------------------------------
    // Java Consumer overloads
    // -------------------------------------------------------------------------------------

    @Test
    fun `classic Consumer overload works`() {
        val setup = BattleSetup.classic { numberOfRounds = 7 }
        assertThat(setup.gameType).isEqualTo(GameType.CLASSIC)
        assertThat(setup.numberOfRounds).isEqualTo(7)
    }

    @Test
    fun `melee Consumer overload works`() {
        val setup = BattleSetup.melee { maxInactivityTurns = 200 }
        assertThat(setup.gameType).isEqualTo(GameType.MELEE)
        assertThat(setup.maxInactivityTurns).isEqualTo(200)
    }

    @Test
    fun `oneVsOne Consumer overload works`() {
        val setup = BattleSetup.oneVsOne { readyTimeoutMicros = 2_000_000 }
        assertThat(setup.gameType).isEqualTo(GameType.ONE_VS_ONE)
        assertThat(setup.readyTimeoutMicros).isEqualTo(2_000_000)
    }

    @Test
    fun `custom Consumer overload works`() {
        val setup = BattleSetup.custom { arenaWidth = 1500 }
        assertThat(setup.gameType).isEqualTo(GameType.CUSTOM)
        assertThat(setup.arenaWidth).isEqualTo(1500)
    }

    // -------------------------------------------------------------------------------------
    // Builder gameType is read-only
    // -------------------------------------------------------------------------------------

    @Test
    fun `builder exposes gameType as read-only`() {
        BattleSetup.classic {
            assertThat(gameType).isEqualTo(GameType.CLASSIC)
        }
        BattleSetup.melee {
            assertThat(gameType).isEqualTo(GameType.MELEE)
        }
        BattleSetup.oneVsOne {
            assertThat(gameType).isEqualTo(GameType.ONE_VS_ONE)
        }
        BattleSetup.custom {
            assertThat(gameType).isEqualTo(GameType.CUSTOM)
        }
    }

    // -------------------------------------------------------------------------------------
    // Data class equality
    // -------------------------------------------------------------------------------------

    @Test
    fun `BattleSetup with same values are equal`() {
        val a = BattleSetup.classic { numberOfRounds = 5 }
        val b = BattleSetup.classic { numberOfRounds = 5 }
        assertThat(a).isEqualTo(b)
        assertThat(a.hashCode()).isEqualTo(b.hashCode())
    }

    @Test
    fun `BattleSetup with different values are not equal`() {
        val a = BattleSetup.classic { numberOfRounds = 5 }
        val b = BattleSetup.classic { numberOfRounds = 10 }
        assertThat(a).isNotEqualTo(b)
    }
}
