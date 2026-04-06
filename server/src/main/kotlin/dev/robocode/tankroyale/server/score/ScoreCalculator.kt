package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.model.Score

class ScoreCalculator(private val participantIds: Set<ParticipantId>, private val scoreTracker: ScoreTracker) {

    /**
     * Returns an ordered list containing the current scores, ranks, and placements for all participants.
     * The higher _total_ scores are listed before lower _total_ scores.
     */
    fun getScores(): List<Score> =
        participantIds.map { scoreTracker.calculateScore(it) }
            .sortedByDescending { it.totalScore }
            .let { RankDecorator.updateRanks(it) }
            .let { increment1st2ndAnd3rdPlaces(it) }

    private fun increment1st2ndAnd3rdPlaces(scores: List<Score>): List<Score> =
        scores.map { score ->
            when (score.rank) {
                1 -> score.copy(firstPlaces = score.firstPlaces + 1)
                2 -> score.copy(secondPlaces = score.secondPlaces + 1)
                3 -> score.copy(thirdPlaces = score.thirdPlaces + 1)
                else -> score
            }
        }
}
