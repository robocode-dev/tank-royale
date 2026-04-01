package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.model.Score

// Uses Shared Placement System where two participants sharing the same score gets the same rank
object RankDecorator {

    /** Returns a new list where each score has its [Score.rank] field set according to the
     *  Shared Placement (1224) system. The input list order is preserved. */
    fun updateRanks(scores: List<Score>): List<Score> {
        val rankByParticipant = scores
            .sortedByDescending { it.totalScore }
            .foldIndexed(emptyMap<ParticipantId, Int>() to (0.0 to 0)) { index, (map, lastState), score ->
                val (lastScore, lastRank) = lastState
                val rank = if (score.totalScore != lastScore) index + 1 else lastRank
                (map + (score.participantId to rank)) to (score.totalScore to rank)
            }
            .first

        return scores.map { it.copy(rank = rankByParticipant[it.participantId] ?: 0) }
    }
}