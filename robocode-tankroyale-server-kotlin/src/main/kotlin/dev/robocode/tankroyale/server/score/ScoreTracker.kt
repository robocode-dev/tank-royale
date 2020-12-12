package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.Score
import dev.robocode.tankroyale.server.rules.*
import java.lang.IllegalStateException
import kotlin.collections.HashMap
import kotlin.collections.HashSet

/** Score utility class used for keeping track of the score for an individual bot in a game. */
class ScoreTracker(botIds: Set<BotId>) {

    /** Set of bot identifiers  */
    private val botIds: Set<BotId> = HashSet(botIds)

    /** Map from bot identifier to a bot record  */
    private val scoreRecords: MutableMap<BotId, ScoreRecord> = HashMap()

    /** Set of identifiers of bots alive  */
    private val botsAliveIds: MutableSet<BotId> = HashSet(botIds)

    /** 1st places  */
    private val place1st: MutableMap<BotId, Int> = HashMap()

    /** 2nd places  */
    private val place2nd: MutableMap<BotId, Int> = HashMap()

    /** 3rd places  */
    private val place3rd: MutableMap<BotId, Int> = HashMap()

    init {
        initializeDamageAndSurvivals()
    }

    /** Prepare for new round. */
    fun prepareRound() {
        botsAliveIds += botIds
    }

    /**
     * Returns the current results ordered with highest total scores first.
     * @return a list of scores.
     */
    val results: List<Score>
        get() {
            for (i in botScores.indices) {
                val score = botScores[i]
                val botId = score.botId
                score.firstPlaces = place1st[botId] ?: 0
                score.secondPlaces = place2nd[botId] ?: 0
                score.thirdPlaces = place3rd[botId] ?: 0
            }
            return botScores
        }

    /** Calculates 1st, 2nd, and 3rd places. */
    fun calculatePlacements() {
        val scores: List<Score> = botScores
        var count: Int
        if (scores.isNotEmpty()) {
            val (botId) = scores[0]
            count = place1st[botId] ?: 0
            place1st[botId] = ++count
        }
        if (scores.size >= 2) {
            val (botId) = scores[1]
            count = place2nd[botId] ?: 0
            place2nd[botId] = ++count
        }
        if (scores.size >= 3) {
            val (botId) = scores[2]
            count = place3rd[botId] ?: 0
            place3rd[botId] = ++count
        }
    }

    /**
     * Returns the current bot scores ordered with highest total scores first.
     * @return a list of bot scores.
     */
    private val botScores: MutableList<Score>
        get() {
            val scores: MutableList<Score> = ArrayList()
            botIds.forEach { botId -> scores + getScore(botId) }
            scores.sortByDescending { it.totalScore }
            return scores
        }

    /** Initializes the map containing the BotRecord record for each bot. */
    private fun initializeDamageAndSurvivals() {
        botIds.forEach { scoreRecords[it] = ScoreRecord() }
    }

    /**
     * Returns the score record for a specific bot.
     * @param botId is the identifier of the bot.
     * @return a score record.
     */
    private fun getScore(botId: BotId): Score {
        val damageRecord = scoreRecords[botId] ?: throw IllegalStateException("No score record for botId: $botId")
        damageRecord.apply {
            val score = Score(
                botId = botId,
                survival = survivalCount * SCORE_PER_SURVIVAL,
                lastSurvivorBonus = lastSurvivorCount * BONUS_PER_LAST_SURVIVOR,
                bulletDamage = totalBulletDamage * SCORE_PER_BULLET_DAMAGE,
                ramDamage = totalRamDamage * SCORE_PER_RAM_DAMAGE,
            )
            for (enemyId in bulletKillEnemyIds) {
                val totalDamage = getBulletDamage(enemyId) + getRamDamage(enemyId)
                score.bulletKillBonus += totalDamage * BONUS_PER_BULLET_KILL
            }
            for (enemyId in ramKillEnemyIds) {
                val totalDamage = getBulletDamage(enemyId) + getRamDamage(enemyId)
                score.ramKillBonus += totalDamage * BONUS_PER_RAM_KILL
            }
            return score
        }
    }

    /**
     * Registers a bullet hit.
     * @param botId is the identifier of the bot that hit another bot.
     * @param victimBotId is the identifier of the victim bot that got hit by the bullet.
     * @param damage is the damage that the victim bot receives.
     * @param kill is a flag specifying, if the bot got killed by this bullet.
     */
    fun registerBulletHit(botId: BotId, victimBotId: BotId, damage: Double, kill: Boolean) {
        val damageRecord = scoreRecords[botId] ?: throw IllegalStateException("No score record for botId: $botId")
        damageRecord.apply {
            addBulletDamage(victimBotId, damage)
            if (kill) {
                addBulletKillEnemyId(victimBotId)
            }
        }
    }

    /**
     * Registers a ram hit.
     * @param botId is the identifier of the bot that rammed another bot.
     * @param victimBotId is the identifier of the victim bot that got rammed.
     * @param kill is a flag specifying, if the bot got killed by the ramming.
     */
    fun registerRamHit(botId: BotId, victimBotId: BotId, kill: Boolean) {
        val damageRecord = scoreRecords[botId] ?: throw IllegalStateException("No score record for botId: $botId")
        damageRecord.apply {
            addRamDamage(victimBotId)
            if (kill) {
                addRamKillEnemyId(victimBotId)
            }
        }
    }

    /**
     * Register a bot death.
     * @param botId is the identifier of the bot that died.
     */
    fun registerBotDeath(botId: BotId) {
        botsAliveIds.apply {
            remove(botId)
            forEach { scoreRecords[it]?.incrementSurvivalCount() }
            if (size == 1) {
                val survivorId = botsAliveIds.first()
                val deadCount = scoreRecords.size - botsAliveIds.size
                scoreRecords[survivorId]?.addLastSurvivorCount(deadCount)
            }
        }
    }
}