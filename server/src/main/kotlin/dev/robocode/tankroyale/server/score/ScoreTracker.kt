package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model.TeamOrBotId
import dev.robocode.tankroyale.server.model.Score
import dev.robocode.tankroyale.server.rules.*

/** Score utility class used for keeping track of the score for an individual bot in a game. */
class ScoreTracker(private val teamOrBotIds: List<TeamOrBotId>) {

    /** Map from bot identifier to a bot record  */
    private val scoreAndDamages = mutableMapOf<TeamOrBotId, ScoreAndDamage>()

    /** Set of identifiers of bots alive */
    private val teamsAlive = mutableSetOf<TeamOrBotId>()

    /** 1st places */
    private val firstPlaces = mutableMapOf<TeamOrBotId, Int>()

    /** 2nd places  */
    private val secondPlaces = mutableMapOf<TeamOrBotId, Int>()

    /** 3rd places  */
    private val thirdPlaces = mutableMapOf<TeamOrBotId, Int>()

    init {
        teamOrBotIds.forEach { scoreAndDamages[it] = ScoreAndDamage() }
    }

    /** Current bot scores ordered with higher total scores first. */
    fun getBotScores(): Collection<Score> = teamOrBotIds.map { getScore(it) }

    /** Prepare for new round. */
    fun prepareRound() {
        teamsAlive.apply {
            clear()
            addAll(teamOrBotIds)
        }
    }

    /**
     * Returns the score for a specific bot.
     * @param teamOrBotId is the identifier of the team or bot.
     * @return a score record.
     */
    private fun getScore(teamOrBotId: TeamOrBotId): Score {
        getScoreAndDamage(teamOrBotId).apply {
            val score = Score(
                teamOrBotId = teamOrBotId,
                survival = survivalCount * SCORE_PER_SURVIVAL,
                lastSurvivorBonus = lastSurvivorCount * BONUS_PER_LAST_SURVIVOR,
                bulletDamage = getTotalBulletDamage() * SCORE_PER_BULLET_DAMAGE,
                ramDamage = getTotalRamDamage() * SCORE_PER_RAM_DAMAGE,
            )
            val totalDamage = getBulletKillEnemyIds().sumOf { getBulletDamage(it) + getRamDamage(it) }

            score.bulletKillBonus += totalDamage * BONUS_PER_BULLET_KILL
            score.ramKillBonus += totalDamage * BONUS_PER_RAM_KILL

            score.firstPlaces = firstPlaces[teamOrBotId] ?: 0
            score.secondPlaces = secondPlaces[teamOrBotId] ?: 0
            score.thirdPlaces = thirdPlaces[teamOrBotId] ?: 0

            return score
        }
    }

    /**
     * Registers a bullet hit.
     * @param teamOrBotId is the identifier of the bot that hit another bot.
     * @param victimBotId is the identifier of the victim bot that got hit by the bullet.
     * @param damage is the damage that the victim bot receives.
     * @param kill is a flag specifying, if the bot got killed by this bullet.
     */
    fun registerBulletHit(teamOrBotId: TeamOrBotId, victimBotId: TeamOrBotId, damage: Double, kill: Boolean) {
        getScoreAndDamage(teamOrBotId).apply {
            addBulletDamage(victimBotId, damage)
            if (kill) {
                addBulletKillEnemyId(victimBotId)
            }
        }
    }

    /**
     * Registers a ram hit.
     * @param teamOrBotId is the identifier of the bot that rammed another bot.
     * @param victimBotId is the identifier of the victim bot that got rammed.
     * @param kill is a flag specifying, if the bot got killed by the ramming.
     */
    fun registerRamHit(teamOrBotId: TeamOrBotId, victimBotId: TeamOrBotId, kill: Boolean) {
        getScoreAndDamage(teamOrBotId).apply {
            addRamDamage(victimBotId)
            if (kill) {
                addRamKillEnemyId(victimBotId)
            }
        }
    }

    /**
     * Register a bot death.
     * @param teamOrBotId is the identifier of the bot (and team)
     */
    fun registerBotDeath(teamOrBotId: TeamOrBotId) {
        teamsAlive.remove(teamOrBotId) // remove dead bot before counting the score!

        teamsAlive.distinctBy { it.id }.apply {

            forEach { scoreAndDamages[it]?.incrementSurvivalCount() }

            when (size) {
                0 -> increment1stPlaces(teamOrBotId)

                1 -> {
                    increment2ndPlaces(teamOrBotId)

                    val deadCount = scoreAndDamages.size - 1
                    scoreAndDamages[first()]?.addLastSurvivorCount(deadCount) // first() is the only one left
                }

                2 -> {
                    val numberOfParticipants = teamOrBotIds.distinctBy { it.id }.count()
                    if (numberOfParticipants > 2) {
                        increment3rdPlaces(teamOrBotId)
                    }
                }
            }
        }
    }

    /**
     * Increment the number of 1st places for a bot.
     * @param teamOrBotId is the identifier of the team or bot that earned a 1st place.
     */
    fun increment1stPlaces(teamOrBotId: TeamOrBotId) {
        val count = firstPlaces[teamOrBotId] ?: 0
        firstPlaces[teamOrBotId] = count + 1
    }

    /**
     * Increment the number of 2nd places for a bot.
     * @param teamOrBotId is the identifier of the team or bot that earned a 2nd place.
     */
    fun increment2ndPlaces(teamOrBotId: TeamOrBotId) {
        val count = secondPlaces[teamOrBotId] ?: 0
        secondPlaces[teamOrBotId] = count + 1
    }

    /**
     * Increment the number of 3rd places for a bot.
     * @param teamOrBotId is the identifier of the team or bot that earned a 3rd place.
     */
    fun increment3rdPlaces(teamOrBotId: TeamOrBotId) {
        val count = thirdPlaces[teamOrBotId] ?: 0
        thirdPlaces[teamOrBotId] = count + 1
    }

    private fun getScoreAndDamage(teamOrBotId: TeamOrBotId): ScoreAndDamage =
        (scoreAndDamages[teamOrBotId] ?: throw IllegalStateException("No score record for teamOrBotId: $teamOrBotId)"))
}