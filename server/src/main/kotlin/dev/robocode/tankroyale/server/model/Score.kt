package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model.ParticipantId

/**
 * Score record for keeping track of the scores for a specific participant.
 */
data class Score(
    /** Participant id */
    val participantId: ParticipantId,

    /** Bullet damage score */
    val bulletDamageScore: Double = 0.0,

    /** Bullet kill bonus (accumulated from killed opponents) */
    val bulletKillBonus: Double = 0.0,

    /** Ram damage score */
    val ramDamageScore: Double = 0.0,

    /** Ram kill bonus (accumulated from killed opponents) */
    val ramKillBonus: Double = 0.0,

    /** Survival score (whenever another participant is defeated) */
    val survivalScore: Double = 0.0,

    /** Last survivor bonus (the last survivor) */
    val lastSurvivorBonus: Double = 0.0,

    /** Number of 1st places */
    var firstPlaces: Int = 0,

    /** Number of 2nd places */
    var secondPlaces: Int = 0,

    /** Number of 3rd places */
    var thirdPlaces: Int = 0,

    /** Rank */
    var rank: Int = 0,
) {
    /** The total score */
    val totalScore: Double
        get() = bulletDamageScore + bulletKillBonus +
                ramDamageScore + ramKillBonus +
                survivalScore + lastSurvivorBonus

    /** Adds another score record to this record */
    operator fun plus(score: Score) = Score(
        participantId,
        bulletDamageScore + score.bulletDamageScore,
        bulletKillBonus + score.bulletKillBonus,
        ramDamageScore + score.ramDamageScore,
        ramKillBonus + score.ramKillBonus,
        survivalScore + score.survivalScore,
        lastSurvivorBonus + score.lastSurvivorBonus,
        firstPlaces + score.firstPlaces,
        secondPlaces + score.secondPlaces,
        thirdPlaces + score.thirdPlaces,
    )
}