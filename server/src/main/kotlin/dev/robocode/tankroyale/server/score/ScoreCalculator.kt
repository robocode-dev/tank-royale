package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.model.ParticipantId
import dev.robocode.tankroyale.server.model.Score
import dev.robocode.tankroyale.server.score.ScoreTracker

class ScoreCalculator(private val participantIds: Set<ParticipantId>, private val scoreTracker: ScoreTracker) {

    /**
     * Returns an ordered list containing the current scores, ranks, and placements for all participants.
     * The higher _total_ scores are listed before lower _total_ scores.
     */
    fun getScores(): List<Score> {
        val scores = participantIds.map { scoreTracker.calculateScore(it) }.sortedByDescending { it.totalScore }
        RankDecorator.updateRanks(scores)
        increment1st2ndAnd3rdPlaces(scores)
        return scores
    }

    private fun increment1st2ndAnd3rdPlaces(scores: Collection<Score>) {
        scores.forEach { score ->
            when (score.rank) {
                1 -> score.firstPlaces++
                2 -> score.secondPlaces++
                3 -> score.thirdPlaces++
            }
        }
    }
}