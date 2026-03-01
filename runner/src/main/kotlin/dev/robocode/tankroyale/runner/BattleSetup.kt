package dev.robocode.tankroyale.runner

import dev.robocode.tankroyale.common.rules.GAME_TYPE_PRESETS
import dev.robocode.tankroyale.common.rules.GameType
import dev.robocode.tankroyale.common.rules.GameTypePreset
import java.util.function.Consumer

/**
 * Immutable battle configuration derived from a game type preset with optional parameter overrides.
 *
 * **Kotlin:**
 * ```kotlin
 * val setup = BattleSetup.classic()
 * val setup = BattleSetup.classic { numberOfRounds = 5 }
 * val setup = BattleSetup.melee { numberOfRounds = 3 }
 * val setup = BattleSetup.custom { arenaWidth = 1200; arenaHeight = 900 }
 * ```
 *
 * **Java:**
 * ```java
 * var setup = BattleSetup.classic();
 * var setup = BattleSetup.classic(s -> s.setNumberOfRounds(5));
 * var setup = BattleSetup.melee(s -> s.setNumberOfRounds(3));
 * var setup = BattleSetup.custom(s -> { s.setArenaWidth(1200); s.setArenaHeight(900); });
 * ```
 *
 * @property gameType identifies which game type this configuration is for
 * @property arenaWidth arena width in pixels
 * @property arenaHeight arena height in pixels
 * @property minNumberOfParticipants minimum number of bots required to start the battle
 * @property maxNumberOfParticipants maximum number of bots allowed, or null for no upper limit
 * @property numberOfRounds number of rounds to play
 * @property gunCoolingRate rate at which gun heat decreases per turn
 * @property maxInactivityTurns maximum consecutive turns allowed without bot activity
 * @property turnTimeoutMicros per-turn deadline for bots to submit their intent, in microseconds
 * @property readyTimeoutMicros deadline for bots to signal ready at round start, in microseconds
 */
data class BattleSetup(
    val gameType: GameType,
    val arenaWidth: Int,
    val arenaHeight: Int,
    val minNumberOfParticipants: Int,
    val maxNumberOfParticipants: Int?,
    val numberOfRounds: Int,
    val gunCoolingRate: Double,
    val maxInactivityTurns: Int,
    val turnTimeoutMicros: Int,
    val readyTimeoutMicros: Int,
) {
    /**
     * Builder for creating a [BattleSetup] from a preset with optional field overrides.
     * All fields are pre-populated with preset defaults and may be freely overridden.
     */
    class Builder internal constructor(preset: GameTypePreset) {
        /** Game type (read-only — determined by the factory function used to create this builder). */
        val gameType: GameType = preset.gameType

        /** Arena width in pixels. */
        var arenaWidth: Int = preset.arenaWidth

        /** Arena height in pixels. */
        var arenaHeight: Int = preset.arenaHeight

        /** Minimum number of bots required to start the battle. */
        var minNumberOfParticipants: Int = preset.minNumberOfParticipants

        /** Maximum number of bots allowed, or null for no upper limit. */
        var maxNumberOfParticipants: Int? = preset.maxNumberOfParticipants

        /** Number of rounds to play. */
        var numberOfRounds: Int = preset.numberOfRounds

        /** Rate at which gun heat decreases per turn. */
        var gunCoolingRate: Double = preset.gunCoolingRate

        /** Maximum consecutive turns allowed without bot activity before the bot is penalized. */
        var maxInactivityTurns: Int = preset.maxInactivityTurns

        /** Per-turn deadline for bots to submit their intent, in microseconds. */
        var turnTimeoutMicros: Int = preset.turnTimeoutMicros

        /** Deadline for bots to signal ready at round start, in microseconds. */
        var readyTimeoutMicros: Int = preset.readyTimeoutMicros

        internal fun build(): BattleSetup = BattleSetup(
            gameType = gameType,
            arenaWidth = arenaWidth,
            arenaHeight = arenaHeight,
            minNumberOfParticipants = minNumberOfParticipants,
            maxNumberOfParticipants = maxNumberOfParticipants,
            numberOfRounds = numberOfRounds,
            gunCoolingRate = gunCoolingRate,
            maxInactivityTurns = maxInactivityTurns,
            turnTimeoutMicros = turnTimeoutMicros,
            readyTimeoutMicros = readyTimeoutMicros,
        )
    }

    companion object {
        private fun forPreset(gameType: GameType, block: Builder.() -> Unit): BattleSetup {
            val preset = checkNotNull(GAME_TYPE_PRESETS[gameType]) {
                "No preset found for game type: $gameType"
            }
            return Builder(preset).apply(block).build()
        }

        private fun forPreset(gameType: GameType, configurer: Consumer<Builder>): BattleSetup {
            val preset = checkNotNull(GAME_TYPE_PRESETS[gameType]) {
                "No preset found for game type: $gameType"
            }
            return Builder(preset).also { configurer.accept(it) }.build()
        }

        private fun forPreset(gameType: GameType): BattleSetup {
            val preset = checkNotNull(GAME_TYPE_PRESETS[gameType]) {
                "No preset found for game type: $gameType"
            }
            return Builder(preset).build()
        }

        /**
         * Creates a Classic game setup (800×600 arena, standard rules) with Kotlin DSL overrides.
         *
         * @param block DSL block to override preset values
         */
        @JvmSynthetic
        fun classic(block: Builder.() -> Unit): BattleSetup =
            forPreset(GameType.CLASSIC, block)

        /** Creates a Classic game setup (800×600 arena, standard rules) with default preset values. */
        @JvmStatic
        fun classic(): BattleSetup = forPreset(GameType.CLASSIC)

        /**
         * Creates a Classic game setup (800×600 arena, standard rules) with optional Java consumer overrides.
         *
         * @param configurer consumer that overrides preset values
         */
        @JvmStatic
        fun classic(configurer: Consumer<Builder>): BattleSetup =
            forPreset(GameType.CLASSIC, configurer)

        /**
         * Creates a Melee game setup (1000×1000 arena, 10+ participants) with Kotlin DSL overrides.
         *
         * @param block DSL block to override preset values
         */
        @JvmSynthetic
        fun melee(block: Builder.() -> Unit): BattleSetup =
            forPreset(GameType.MELEE, block)

        /** Creates a Melee game setup (1000×1000 arena, 10+ participants) with default preset values. */
        @JvmStatic
        fun melee(): BattleSetup = forPreset(GameType.MELEE)

        /**
         * Creates a Melee game setup (1000×1000 arena, 10+ participants) with optional Java consumer overrides.
         *
         * @param configurer consumer that overrides preset values
         */
        @JvmStatic
        fun melee(configurer: Consumer<Builder>): BattleSetup =
            forPreset(GameType.MELEE, configurer)

        /**
         * Creates a 1v1 game setup (800×600 arena, exactly 2 participants) with Kotlin DSL overrides.
         *
         * @param block DSL block to override preset values
         */
        @JvmSynthetic
        fun oneVsOne(block: Builder.() -> Unit): BattleSetup =
            forPreset(GameType.ONE_VS_ONE, block)

        /** Creates a 1v1 game setup (800×600 arena, exactly 2 participants) with default preset values. */
        @JvmStatic
        fun oneVsOne(): BattleSetup = forPreset(GameType.ONE_VS_ONE)

        /**
         * Creates a 1v1 game setup (800×600 arena, exactly 2 participants) with optional Java consumer overrides.
         *
         * @param configurer consumer that overrides preset values
         */
        @JvmStatic
        fun oneVsOne(configurer: Consumer<Builder>): BattleSetup =
            forPreset(GameType.ONE_VS_ONE, configurer)

        /**
         * Creates a Custom game setup where all parameters are configurable, with Kotlin DSL overrides.
         *
         * @param block DSL block to override preset values
         */
        @JvmSynthetic
        fun custom(block: Builder.() -> Unit): BattleSetup =
            forPreset(GameType.CUSTOM, block)

        /** Creates a Custom game setup where all parameters are configurable, with default preset values. */
        @JvmStatic
        fun custom(): BattleSetup = forPreset(GameType.CUSTOM)

        /**
         * Creates a Custom game setup where all parameters are configurable, with optional Java consumer overrides.
         *
         * @param configurer consumer that overrides preset values
         */
        @JvmStatic
        fun custom(configurer: Consumer<Builder>): BattleSetup =
            forPreset(GameType.CUSTOM, configurer)
    }
}
