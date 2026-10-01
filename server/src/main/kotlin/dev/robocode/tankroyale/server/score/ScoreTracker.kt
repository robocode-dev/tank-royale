package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.model.Score
import dev.robocode.tankroyale.server.model.TeamId
import dev.robocode.tankroyale.server.rules.*

/**
 * Utility class used for keeping track of the score for each bot in a game.
 * @param participantIds is the ids of all participant bots and teams.
 */
class ScoreTracker(private val participantIds: Set<ParticipantId>) {

    // Map over the score and damage records per participant
    private val scoreAndDamages = mutableMapOf<ParticipantId, ScoreAndDamage>()

    // Map over alive participants
    private val aliveParticipants = mutableSetOf<ParticipantId>()

    private var lastSurvivorBonusAwarded = false

    init {
        participantIds.forEach { scoreAndDamages[it] = ScoreAndDamage() }

        aliveParticipants.addAll(participantIds)
    }

    /**
     * Clears all scores used when a new round is started.
     */
    fun clear() {
        aliveParticipants.apply { clear(); addAll(participantIds) }
        lastSurvivorBonusAwarded = false
        scoreAndDamages.values.forEach { it.clear() }
    }

    /**
     * Calculates and returns the score for a specific participant.
     * @param participantId is the identifier of the participant.
     * @return a [Score] record.
     */
    fun calculateScore(participantId: ParticipantId): Score {
        val scoreAndDamage = getScoreAndDamage(participantId) ?: return Score(participantId = participantId)
        scoreAndDamage.apply {
            val survivalCount = scoreAndDamage.survivalCount
            val lastSurvivorCount = scoreAndDamage.lastSurvivorCount

            return Score(
                participantId = participantId,
                bulletDamageScore = SCORE_PER_BULLET_DAMAGE * getTotalBulletDamage(),
                bulletKillBonus = BONUS_PER_BULLET_KILL * getBulletKillEnemyIds().sumOf { getTotalDamage(it) },
                ramDamageScore = SCORE_PER_RAM_DAMAGE * getTotalRamDamage(),
                ramKillBonus = BONUS_PER_RAM_KILL * getRamKillEnemyIds().sumOf { getTotalDamage(it) },
                survivalScore = SCORE_PER_SURVIVAL * survivalCount,
                lastSurvivorBonus = BONUS_PER_LAST_SURVIVOR * lastSurvivorCount,
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
        getScoreAndDamage(offenderId)?.apply {
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
        getScoreAndDamage(offenderId)?.apply {
            incrementRamHit(victimId)
            if (kill) {
                addRamKillEnemyId(victimId)
            }
        }
    }

    /**
     * Registers newly defeated bots and awards survival and last-survivor scores.
     * @param victimIds is the set of bots reported as defeated on this turn.
     */
    fun registerDeaths(victimIds: Set<ParticipantId>) {
        val newlyDefeated = aliveParticipants.intersect(victimIds)
        if (newlyDefeated.isEmpty()) return

        // All deaths from this turn are already dead when Classic awards survival points.
        aliveParticipants.removeAll(newlyDefeated)
        newlyDefeated.forEach { defeated ->
            aliveParticipants
                .filter { survivor -> survivor.scoringGroup() != defeated.scoringGroup() }
                .forEach { survivor -> scoreAndDamages[survivor]?.incrementSurvivalCount() }
        }

        awardLastSurvivorBonusIfNeeded()
    }

    private fun awardLastSurvivorBonusIfNeeded() {
        if (lastSurvivorBonusAwarded || aliveParticipants.isEmpty()) return

        val remainingGroups = aliveParticipants.map { it.scoringGroup() }.toSet()
        if (remainingGroups.size != 1) return

        val winningGroup = remainingGroups.single()
        val opponentCount = participantIds.count { it.scoringGroup() != winningGroup }
        lastSurvivorBonusAwarded = true
        aliveParticipants.forEach { scoreAndDamages[it]?.addLastSurvivorCount(opponentCount) }
    }

    private fun getScoreAndDamage(participantId: ParticipantId): ScoreAndDamage? =
        scoreAndDamages[participantId]}

private sealed interface ScoringGroup {
    data class Team(val teamId: TeamId) : ScoringGroup
    data class UnteamedBot(val botId: BotId) : ScoringGroup
}

private fun ParticipantId.scoringGroup(): ScoringGroup =
    teamId?.let { ScoringGroup.Team(it) } ?: ScoringGroup.UnteamedBot(botId)
