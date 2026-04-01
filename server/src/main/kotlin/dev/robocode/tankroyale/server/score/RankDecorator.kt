package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.model.Score

// Uses Shared Placement System where two participants sharing the same score gets the same rank
object RankDecorator {

    /** Returns a new list where each score has its [Score.rank] field set according to the
     *  Shared Placement (1224) system. The input list order is preserved. */
    fun updateRanks(scores: List<Score>): List<Score> {
        var lastTotalScore = 0.0
        var lastRank = 0

        val rankByParticipant = scores
            .sortedByDescending { it.totalScore }
            .mapIndexed { index, score ->
                if (score.totalScore != lastTotalScore) {
                    lastRank = index + 1
                }
                lastTotalScore = score.totalScore
                score.participantId to lastRank
            }
            .toMap()

        return scores.map { it.copy(rank = rankByParticipant[it.participantId] ?: 0) }
    }
}