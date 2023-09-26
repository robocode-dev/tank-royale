package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.model.Score
import dev.robocode.tankroyale.server.rules.BONUS_PER_BULLET_KILL
import dev.robocode.tankroyale.server.rules.BONUS_PER_LAST_SURVIVOR
import dev.robocode.tankroyale.server.rules.BONUS_PER_RAM_KILL
import dev.robocode.tankroyale.server.rules.SCORE_PER_SURVIVAL

/**
 * Utility class used for keeping track of the score for an individual bot and/or team in a game.
 * @param participantIds is the ids of all participant bots and teams.
 */
class ScoreTracker(private val participantIds: Set<ParticipantId>) {

    // Map over the score and damage records per participant
    private val scoreAndDamages = mutableMapOf<ParticipantId, ScoreAndDamage>()

    // Map over alive participants
    private val aliveParticipants = mutableSetOf<ParticipantId>()

    // Map over number of 1st places
    private val firstPlaces = mutableMapOf<ParticipantId, Int>()

    // Map over number of 2nd places
    private val secondPlaces = mutableMapOf<ParticipantId, Int>()

    // Map over number of 3rd places
    private val thirdPlaces = mutableMapOf<ParticipantId, Int>()

    init {
        participantIds.forEach { scoreAndDamages[it] = ScoreAndDamage() }

        aliveParticipants.addAll(participantIds)
    }

    /**
     * Returns an ordered list containing the current scores for all participants.
     * The higher _total_ scores are listed before lower _total_ scores.
     */
    fun getScores(): Collection<Score> = participantIds.map { calculateScore(it) }.sortedByDescending { it.totalScore }

    /**
     * Clears all scores used when a new round is started.
     */
    fun clear() {
        aliveParticipants.apply {
            clear()
            addAll(participantIds)
        }
    }

    /**
     * Calculates and returns the score for a specific participant.
     * @param participantId is the identifier of the participant.
     * @return a [Score] record.
     */
    private fun calculateScore(participantId: ParticipantId): Score {
        getScoreAndDamage(participantId).apply {
            return Score(
                participantId = participantId,
                bulletDamage = getTotalBulletDamage(),
                bulletKillBonus = BONUS_PER_BULLET_KILL * getBulletKillEnemyIds().sumOf { getTotalDamage(it) },
                ramDamage = getTotalRamDamage(),
                ramKillBonus = BONUS_PER_RAM_KILL * getRamKillEnemyIds().sumOf { getTotalDamage(it) },
                survival = SCORE_PER_SURVIVAL * survivalCount,
                lastSurvivorBonus = BONUS_PER_LAST_SURVIVOR * lastSurvivorCount,
                firstPlaces = firstPlaces[participantId] ?: 0,
                secondPlaces = secondPlaces[participantId] ?: 0,
                thirdPlaces = thirdPlaces[participantId] ?: 0,
            )
        }
    }

    /**
     * Registers a bullet hit.
     * @param offenderId is the id of the bot hitting a victim by a bullet.
     * @param victimId is the id of the victim bot that got hit by the bullet.
     * @param damage is the damage dealt.
     * @param kill is `true` if the bot got killed by the bullet; `false` otherwise.
     */
    fun registerBulletHit(offenderId: ParticipantId, victimId: ParticipantId, damage: Double, kill: Boolean) {
        getScoreAndDamage(offenderId).apply {
            addBulletDamage(victimId, damage)
            if (kill) {
                addBulletKillEnemyId(victimId)
            }
        }
    }

    /**
     * Registers a ram hit.
     * @param offenderId is the id of the bot ramming a victim.
     * @param victimId is the id of the victim bot that got rammed.
     * @param kill is `true` if the bot got killed by the ramming; `false` otherwise.
     */
    fun registerRamHit(offenderId: ParticipantId, victimId: ParticipantId, kill: Boolean) {
        getScoreAndDamage(offenderId).apply {
            incrementRamHit(victimId)
            if (kill) {
                addRamKillEnemyId(victimId)
            }
        }
    }

    /**
     * Registers a death of a bot.
     * @param victimId is the id of the bot that died.
     */
    fun registerDeath(victimId: ParticipantId) {
        aliveParticipants.remove(victimId) // remove dead bot before counting the score!

        aliveParticipants.distinctBy { it.id }.apply {

            forEach { scoreAndDamages[it]?.incrementSurvivalCount() }

            when (size) {
                0 -> increment1stPlaces(victimId)

                1 -> {
                    increment2ndPlaces(victimId)

                    val deadCount = scoreAndDamages.size - 1
                    scoreAndDamages[first()]?.addLastSurvivorCount(deadCount) // first() is the only one left
                }

                2 -> {
                    if (!aliveParticipants.distinctBy { it.id }.map { it.id }.contains(victimId.id)) {
                        increment3rdPlaces(victimId)
                    }
                }
            }
        }
    }

    /**
     * Increment the number of 1st places for a participant.
     * @param participantId is the id of a participant that earned a 1st place.
     */
    fun increment1stPlaces(participantId: ParticipantId) {
        val count = firstPlaces[participantId] ?: 0
        firstPlaces[participantId] = count + 1
    }

    /**
     * Increment the number of 2nd places for a participant.
     * @param participantId is the id of a participant that earned a 2nd place.
     */
    private fun increment2ndPlaces(participantId: ParticipantId) {
        val count = secondPlaces[participantId] ?: 0
        secondPlaces[participantId] = count + 1
    }

    /**
     * Increment the number of 3rd places for a participant.
     * @param participantId is the id of a participant that earned a 3rd place.
     */
    private fun increment3rdPlaces(participantId: ParticipantId) {
        val count = thirdPlaces[participantId] ?: 0
        thirdPlaces[participantId] = count + 1
    }

    private fun getScoreAndDamage(participantId: ParticipantId): ScoreAndDamage =
        (scoreAndDamages[participantId]
            ?: throw IllegalStateException("No score record for teamOrBotId: $participantId)"))
}