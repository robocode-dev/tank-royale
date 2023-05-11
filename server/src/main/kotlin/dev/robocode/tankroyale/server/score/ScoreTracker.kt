package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.Score
import dev.robocode.tankroyale.server.model.TeamId
import dev.robocode.tankroyale.server.rules.*

/** Score utility class used for keeping track of the score for an individual bot in a game. */
class ScoreTracker(private val botAndTeamIds: Map<BotId, TeamId?>) {

    /** Map from bot identifier to a bot record  */
    private val scoreAndDamages = mutableMapOf<Int, ScoreAndDamage>()

    /** Set of identifiers of bots alive */
    private val teamsAliveIds = mutableSetOf<Int>()

    /** 1st places  */
    private val firstPlaces = mutableMapOf<Int, Int>()

    /** 2nd places  */
    private val secondPlaces = mutableMapOf<Int, Int>()

    /** 3rd places  */
    private val thirdPlaces = mutableMapOf<Int, Int>()

    init {
        botAndTeamIds.forEach { (botId, teamId) -> scoreAndDamages[toScoreId(botId, teamId)] = ScoreAndDamage() }
    }

    /** Current bot scores ordered with higher total scores first. */
    fun getBotScores(): Collection<Score> = botAndTeamIds.map { (botId, teamId) -> getScore(botId, teamId) }

    /** Prepare for new round. */
    fun prepareRound() {
        teamsAliveIds.apply {
            clear()
            addAll(botAndTeamIds.map { (botId, teamId) -> toScoreId(botId, teamId) })
        }
    }

    private fun toScoreId(botId: BotId, teamId: TeamId?): Int = teamId?.value ?: -botId.value

    /**
     * Returns the score for a specific bot.
     * @param botId is the identifier of the bot.
     * @return a score record.
     */
    private fun getScore(botId: BotId, teamId: TeamId?): Score {
        getScoreAndDamage(botId, teamId).apply {
            val score = Score(
                id = botId.value,
                survival = survivalCount * SCORE_PER_SURVIVAL,
                lastSurvivorBonus = lastSurvivorCount * BONUS_PER_LAST_SURVIVOR,
                bulletDamage = getTotalBulletDamage() * SCORE_PER_BULLET_DAMAGE,
                ramDamage = getTotalRamDamage() * SCORE_PER_RAM_DAMAGE,
            )

            val totalDamage = getBulletKillEnemyIds().sumOf { getBulletDamage(it) + getRamDamage(it) }

            score.bulletKillBonus += totalDamage * BONUS_PER_BULLET_KILL
            score.ramKillBonus += totalDamage * BONUS_PER_RAM_KILL

            val id = toScoreId(botId, teamId)

            score.firstPlaces = firstPlaces[id] ?: 0
            score.secondPlaces = secondPlaces[id] ?: 0
            score.thirdPlaces = thirdPlaces[id] ?: 0

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
    fun registerBulletHit(botId: BotId, teamId: TeamId?, victimBotId: BotId, damage: Double, kill: Boolean) {
        getScoreAndDamage(botId, teamId).apply {
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
    fun registerRamHit(botId: BotId, teamId: TeamId?, victimBotId: BotId, kill: Boolean) {
        getScoreAndDamage(botId, teamId).apply {
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
    fun registerBotDeath(botId: BotId, teamId: TeamId?) {
        teamsAliveIds.apply {
            remove(toScoreId(botId, teamId))

            forEach { scoreAndDamages[it]?.incrementSurvivalCount() }
            if (size == 1) {
                val survivorId = teamsAliveIds.first()
                val deadCount = scoreAndDamages.size - teamsAliveIds.size
                scoreAndDamages[survivorId]?.addLastSurvivorCount(deadCount)
            }
        }
    }

    /**
     * Increment the number of 1st places for a bot.
     * @param botId is the identifier of the bot that earned a 1st place.
     */
    fun increment1stPlaces(botId: BotId, teamId: TeamId?) {
        val id = toScoreId(botId, teamId)
        val count = firstPlaces[id] ?: 0
        firstPlaces[id] = count + 1
    }

    /**
     * Increment the number of 2nd places for a bot.
     * @param botId is the identifier of the bot that earned a 2nd place.
     */
    fun increment2ndPlaces(botId: BotId, teamId: TeamId?) {
        val id = toScoreId(botId, teamId)
        val count = secondPlaces[id] ?: 0
        secondPlaces[id] = count + 1
    }

    /**
     * Increment the number of 3rd places for a bot.
     * @param botId is the identifier of the bot that earned a 3rd place.
     */
    fun increment3rdPlaces(botId: BotId, teamId: TeamId?) {
        val id = toScoreId(botId, teamId)
        val count = thirdPlaces[id] ?: 0
        thirdPlaces[id] = count + 1
    }

    private fun getScoreAndDamage(botId: BotId, teamId: TeamId?): ScoreAndDamage =
        (scoreAndDamages[toScoreId(botId, teamId)]
            ?: throw IllegalStateException(
                "No score record for botId: $botId, teamId: $teamId (scoreId: ${toScoreId(botId, teamId)})"
            ))
}