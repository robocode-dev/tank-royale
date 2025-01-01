package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.rules.RAM_DAMAGE

/** Bot record that tracks damage and survival of a bot, and can calculate score. */
class ScoreAndDamage {

    private val bulletDamage = mutableMapOf<ParticipantId, Double>()
    private val ramHits = mutableMapOf<ParticipantId, Int>()
    private val bulletKillEnemyIds = mutableSetOf<ParticipantId>()
    private val ramKillEnemyIds = mutableSetOf<ParticipantId>()

    /** The survival count, which is the number of rounds where the bot has survived. */
    var survivalCount: Int = 0
        private set

    /** The last survivor count, which is the number of bots that was killed, before this bot became the last survivor. */
    var lastSurvivorCount: Int = 0
        private set

    /** Clears all registered scores and damages when a new round is started. */
    fun clear() {
        bulletDamage.clear()
        ramHits.clear()
        bulletKillEnemyIds.clear()
        ramKillEnemyIds.clear()
        survivalCount = 0
        lastSurvivorCount = 0
    }

    /** The total bullet damage dealt by this bot to other bots. */
    fun getTotalBulletDamage() = bulletDamage.keys.sumOf { getBulletDamage(it) }

    /** The total ram damage dealt by this bot to other bots. */
    fun getTotalRamDamage() = ramHits.keys.sumOf { getRamHits(it) } * RAM_DAMAGE

    /** Returns the bullet kill enemy ids. */
    fun getBulletKillEnemyIds(): Set<ParticipantId> = bulletKillEnemyIds

    /** Returns the ram kill enemy ids. */
    fun getRamKillEnemyIds(): Set<ParticipantId> = ramKillEnemyIds

    /**
     * Returns the bullet damage dealt by this bot to specific bot.
     * @param enemyId is the identifier of the specific enemy bot
     * @return the bullet damage dealt to a specific bot.
     */
    private fun getBulletDamage(enemyId: ParticipantId): Double = bulletDamage[enemyId] ?: 0.0

    /**
     * Returns the number of times ram damage has been dealt by this bot to specific bot.
     * @param enemyId is the identifier of the specific enemy bot
     * @return the ram count for a specific bot.
     */
    private fun getRamHits(enemyId: ParticipantId): Int = ramHits[enemyId] ?: 0

    /**
     * Returns the total damage dealt to a specific bot.
     * @param enemyId is the identifier of the specific enemy bot
     * @return the total damage dealt to the bot.
     */
    fun getTotalDamage(enemyId: ParticipantId): Double = getBulletDamage(enemyId) + getRamHits(enemyId) * RAM_DAMAGE

    /**
     * Adds bullet damage to a specific enemy bot.
     * @param enemyId is the identifier of the specific enemy bot
     * @param damage is the amount of damage that the enemy bot has received
     */
    fun addBulletDamage(enemyId: ParticipantId, damage: Double) {
        bulletDamage[enemyId] = getBulletDamage(enemyId) + damage
    }

    /**
     * Increment the number of ram hits to a specific bot or team.
     * @param id is the identifier of the bot/team.
     */
    fun incrementRamHit(id: ParticipantId) {
        ramHits[id] = getRamHits(id) + 1
    }

    /**
     * Increment the survival count, meaning that this bot has survived an additional round.
     */
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
    fun addBulletKillEnemyId(enemyId: ParticipantId) {
        bulletKillEnemyIds += enemyId
    }

    /**
     * Adds the identifier of an enemy bot to the set over bots killed by ramming by this bot.
     * @param enemyId is the identifier of the enemy bot that was killed by this bot
     */
    fun addRamKillEnemyId(enemyId: ParticipantId) {
        ramKillEnemyIds += enemyId
    }
}