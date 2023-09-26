package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.rules.SCORE_PER_BULLET_DAMAGE
import dev.robocode.tankroyale.server.rules.SCORE_PER_RAM_DAMAGE

/**
 * Score record for keeping track of the scores for a specific participant.
 */
data class Score(
    /** Participant id */
    val participantId: ParticipantId,

    /** Survival score gained whenever another participant is defeated */
    val survival: Double = 0.0,

    /** Last survivor score for the last survivor in a round */
    val lastSurvivorBonus: Double = 0.0,

    /** Bullet damage dealt */
    val bulletDamage: Double = 0.0,

    /** Bullet kill bonus */
    val bulletKillBonus: Double = 0.0,

    /** Ram damage dealt */
    val ramDamage: Double = 0.0,

    /** Ram kill bonus */
    val ramKillBonus: Double = 0.0,

    /** Number of 1st places */
    val firstPlaces: Int = 0,

    /** Number of 2nd places */
    val secondPlaces: Int = 0,

    /** Number of 3rd places */
    val thirdPlaces: Int = 0,
) {
    /** The total score */
    val totalScore: Double
        get() = SCORE_PER_BULLET_DAMAGE * bulletDamage + bulletKillBonus +
                SCORE_PER_RAM_DAMAGE * ramDamage + ramKillBonus +
                survival + lastSurvivorBonus

    /** Adds another score record to this record */
    operator fun plus(score: Score) = Score(
        participantId,
        survival + score.survival,
        lastSurvivorBonus + score.lastSurvivorBonus,
        bulletDamage + score.bulletDamage,
        bulletKillBonus + score.bulletKillBonus,
        ramDamage + score.ramDamage,
        ramKillBonus + score.ramKillBonus,
        firstPlaces + score.firstPlaces,
        secondPlaces + score.secondPlaces,
        thirdPlaces + score.thirdPlaces
    )
}