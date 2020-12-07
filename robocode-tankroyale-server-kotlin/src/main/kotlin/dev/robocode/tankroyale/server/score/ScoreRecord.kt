package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.rules.RAM_DAMAGE
import java.util.Collections.unmodifiableSet
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/** Bot record that tracks damage and survival of a bot, and can calculate score. */
internal class ScoreRecord {

    private val bulletDamage: MutableMap<BotId, Double> = HashMap()
    private val ramDamage: MutableMap<BotId, Double> = HashMap()
    private val _bulletKillEnemyIds: MutableSet<BotId> = HashSet()
    private val _ramKillEnemyIds: MutableSet<BotId> = HashSet()

    /** The survival count, which is the number of rounds where the bot has survived. */
    var survivalCount = 0
        private set

    /** The last survivor count, which is the number of bots that was killed, before this bot became the last survivor. */
    var lastSurvivorCount = 0
        private set

    /** The total bullet damage dealt by this bot to other bots. */
    val totalBulletDamage: Double
        get() {
            var sum = 0.0
            bulletDamage.keys.forEach { sum += getBulletDamage(it) }
            return sum
        }

    /** The total ram damage dealt by this bot to other bots. */
    val totalRamDamage: Double
        get() {
            var sum = 0.0
            ramDamage.keys.forEach { sum += getRamDamage(it) }
            return sum
        }

    /** The set of identifiers of all enemy bot that this bot has killed by bullets. */
    val bulletKillEnemyIds: Set<BotId>
        get() = unmodifiableSet(_bulletKillEnemyIds)

    /** The set of identifiers of all enemy bot that this bot has killed by ramming. */
    val ramKillEnemyIds: Set<BotId>
        get() = unmodifiableSet(_ramKillEnemyIds)

    /**
     * Returns the bullet damage dealt by this bot to specific bot.
     * @param enemyId  is the enemy bot to retrieve the damage for.
     * @return the bullet damage dealt to a specific bot.
     */
    fun getBulletDamage(enemyId: BotId): Double = bulletDamage[enemyId] ?: 0.0

    /**
     * Returns the ram damage dealt by this bot to specific bot.
     * @param enemyId is the enemy bot to retrieve the damage for.
     * @return the ram damage dealt to a specific bot.
     */
    fun getRamDamage(enemyId: BotId): Double = ramDamage[enemyId] ?: 0.0

    /**
     * Adds bullet damage to a specific enemy bot.
     * @param enemyId is the identifier of the enemy bot
     * @param damage is the amount of damage that the enemy bot has received
     */
    fun addBulletDamage(enemyId: BotId, damage: Double) {
        bulletDamage[enemyId] = getBulletDamage(enemyId) + damage
    }

    /**
     * Adds ram damage to a specific enemy bot.
     * @param enemyId is the identifier of the enemy bot
     */
    fun addRamDamage(enemyId: BotId) {
        ramDamage[enemyId] = getRamDamage(enemyId) + RAM_DAMAGE
    }

    /** Increment the survival count, meaning that this bot has survived an additional round. */
    fun incrementSurvivalCount() {
        survivalCount++
    }

    /**
     * Add number of dead enemies to the last survivor count, which only counts, if this bot becomes the last survivor.
     * @param numberOfDeadEnemies is the number of dead bots that must be added to the last survivor count.
     */
    fun addLastSurvivorCount(numberOfDeadEnemies: Int) {
        lastSurvivorCount += numberOfDeadEnemies
    }

    /**
     * Adds the identifier of an enemy bot to the set over bots killed by a bullet from this bot.
     * @param enemyId is the identifier of the enemy bot that was killed by this bot
     */
    fun addBulletKillEnemyId(enemyId: BotId) {
        bulletKillEnemyIds + enemyId
    }

    /**
     * Adds the identifier of an enemy bot to the set over bots killed by ramming by this bot.
     * @param enemyId is the identifier of the enemy bot that was killed by this bot
     */
    fun addRamKillEnemyId(enemyId: BotId) {
        ramKillEnemyIds + enemyId
    }
}