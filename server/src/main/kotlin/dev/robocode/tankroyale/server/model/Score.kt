package dev.robocode.tankroyale.server.model

import dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.model.ParticipantId

/**
 * Score record for keeping track of the scores for a specific participant.
 */
data class Score(
    /** Participant id */
    var participantId: ParticipantId,

    /** Bullet damage score */
    var bulletDamageScore: Double = 0.0,

    /** Bullet kill bonus (accumulated from killed opponents) */
    var bulletKillBonus: Double = 0.0,

    /** Ram damage score */
    var ramDamageScore: Double = 0.0,

    /** Ram kill bonus (accumulated from killed opponents) */
    var ramKillBonus: Double = 0.0,

    /** Survival score (whenever another participant is defeated) */
    var survivalScore: Double = 0.0,

    /** Last survivor bonus (the last survivor) */
    var lastSurvivorBonus: Double = 0.0,

    /** Number of 1st places */
    var firstPlaces: Int = 0,

    /** Number of 2nd places */
    var secondPlaces: Int = 0,

    /** Number of 3rd places */
    var thirdPlaces: Int = 0,

    /** Rank */
    var rank: Int = 0,

) : Comparable<Score> {

    /** The total score */
    val totalScore: Double
        get() = bulletDamageScore + bulletKillBonus +
                ramDamageScore + ramKillBonus +
                survivalScore + lastSurvivorBonus

    override fun compareTo(other: Score): Int = totalScore.compareTo(other.totalScore)

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

    operator fun plusAssign(score: Score) {
        bulletDamageScore += score.bulletDamageScore
        bulletKillBonus += score.bulletKillBonus
        ramDamageScore += score.ramDamageScore
        ramKillBonus += score.ramKillBonus
        survivalScore += score.survivalScore
        lastSurvivorBonus += score.lastSurvivorBonus
        firstPlaces += score.firstPlaces
        secondPlaces += score.secondPlaces
        thirdPlaces += score.thirdPlaces
    }
}