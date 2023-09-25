package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model.BotOrTeamId
import dev.robocode.tankroyale.server.model.Score
import dev.robocode.tankroyale.server.rules.*

/** Score utility class used for keeping track of the score for an individual bot in a game. */
class ScoreTracker(private val botOrTeamIds: List<BotOrTeamId>) {

    /** Map from bot identifier to a bot record  */
    private val scoreAndDamages = mutableMapOf<BotOrTeamId, ScoreAndDamage>()

    /** Set of identifiers of bots alive */
    private val teamsAlive = mutableSetOf<BotOrTeamId>()

    /** 1st places */
    private val firstPlaces = mutableMapOf<BotOrTeamId, Int>()

    /** 2nd places  */
    private val secondPlaces = mutableMapOf<BotOrTeamId, Int>()

    /** 3rd places  */
    private val thirdPlaces = mutableMapOf<BotOrTeamId, Int>()

    init {
        botOrTeamIds.forEach { scoreAndDamages[it] = ScoreAndDamage() }
    }

    /** Current bot scores ordered with higher total scores first. */
    fun getBotScores(): Collection<Score> = botOrTeamIds.map { getScore(it) }

    /** Prepare for new round. */
    fun prepareRound() {
        teamsAlive.apply {
            clear()
            addAll(botOrTeamIds)
        }
    }

    /**
     * Returns the score for a specific bot.
     * @param botOrTeamId is the identifier of the team or bot.
     * @return a score record.
     */
    private fun getScore(botOrTeamId: BotOrTeamId): Score {
        getScoreAndDamage(botOrTeamId).apply {
            val score = Score(
                botOrTeamId = botOrTeamId,
                survival = survivalCount * SCORE_PER_SURVIVAL,
                lastSurvivorBonus = lastSurvivorCount * BONUS_PER_LAST_SURVIVOR,
                bulletDamage = getTotalBulletDamage() * SCORE_PER_BULLET_DAMAGE,
                ramDamage = getTotalRamDamage() * SCORE_PER_RAM_DAMAGE,
            )
            val totalDamage = getBulletKillEnemyIds().sumOf { getBulletDamage(it) + getRamDamage(it) }

            score.bulletKillBonus += totalDamage * BONUS_PER_BULLET_KILL
            score.ramKillBonus += totalDamage * BONUS_PER_RAM_KILL

            score.firstPlaces = firstPlaces[botOrTeamId] ?: 0
            score.secondPlaces = secondPlaces[botOrTeamId] ?: 0
            score.thirdPlaces = thirdPlaces[botOrTeamId] ?: 0

            return score
        }
    }

    /**
     * Registers a bullet hit.
     * @param botOrTeamId is the identifier of the bot that hit another bot.
     * @param victimBotId is the identifier of the victim bot that got hit by the bullet.
     * @param damage is the damage that the victim bot receives.
     * @param kill is a flag specifying, if the bot got killed by this bullet.
     */
    fun registerBulletHit(botOrTeamId: BotOrTeamId, victimBotId: BotOrTeamId, damage: Double, kill: Boolean) {
        getScoreAndDamage(botOrTeamId).apply {
            addBulletDamage(victimBotId, damage)
            if (kill) {
                addBulletKillEnemyId(victimBotId)
            }
        }
    }

    /**
     * Registers a ram hit.
     * @param botOrTeamId is the identifier of the bot that rammed another bot.
     * @param victimBotId is the identifier of the victim bot that got rammed.
     * @param kill is a flag specifying, if the bot got killed by the ramming.
     */
    fun registerRamHit(botOrTeamId: BotOrTeamId, victimBotId: BotOrTeamId, kill: Boolean) {
        getScoreAndDamage(botOrTeamId).apply {
            addRamDamage(victimBotId)
            if (kill) {
                addRamKillEnemyId(victimBotId)
            }
        }
    }

    /**
     * Register a bot death.
     * @param botOrTeamId is the identifier of the bot (and team)
     */
    fun registerBotDeath(botOrTeamId: BotOrTeamId) {
        teamsAlive.remove(botOrTeamId) // remove dead bot before counting the score!

        teamsAlive.distinctBy { it.id }.apply {

            forEach { scoreAndDamages[it]?.incrementSurvivalCount() }

            when (size) {
                0 -> increment1stPlaces(botOrTeamId)

                1 -> {
                    increment2ndPlaces(botOrTeamId)

                    val deadCount = scoreAndDamages.size - 1
                    scoreAndDamages[first()]?.addLastSurvivorCount(deadCount) // first() is the only one left
                }

                2 -> {
                    if (!teamsAlive.distinctBy { it.id }.map { it.id }.contains(botOrTeamId.id)) {
                        increment3rdPlaces(botOrTeamId)
                    }
                }
            }
        }
    }

    /**
     * Increment the number of 1st places for a bot.
     * @param botOrTeamId is the identifier of the team or bot that earned a 1st place.
     */
    fun increment1stPlaces(botOrTeamId: BotOrTeamId) {
        val count = firstPlaces[botOrTeamId] ?: 0
        firstPlaces[botOrTeamId] = count + 1
    }

    /**
     * Increment the number of 2nd places for a bot.
     * @param botOrTeamId is the identifier of the team or bot that earned a 2nd place.
     */
    fun increment2ndPlaces(botOrTeamId: BotOrTeamId) {
        val count = secondPlaces[botOrTeamId] ?: 0
        secondPlaces[botOrTeamId] = count + 1
    }

    /**
     * Increment the number of 3rd places for a bot.
     * @param botOrTeamId is the identifier of the team or bot that earned a 3rd place.
     */
    fun increment3rdPlaces(botOrTeamId: BotOrTeamId) {
        val count = thirdPlaces[botOrTeamId] ?: 0
        thirdPlaces[botOrTeamId] = count + 1
    }

    private fun getScoreAndDamage(botOrTeamId: BotOrTeamId): ScoreAndDamage =
        (scoreAndDamages[botOrTeamId] ?: throw IllegalStateException("No score record for teamOrBotId: $botOrTeamId)"))
}