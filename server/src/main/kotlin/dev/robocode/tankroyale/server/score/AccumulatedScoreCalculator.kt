package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.model.Score

class AccumulatedScoreCalculator {

    private val accumulatedScores: ArrayList<Score> = ArrayList()

    fun addScores(scores: List<Score>) {
        if (accumulatedScores.isEmpty()) {
            accumulatedScores.addAll(scores)
        } else {
            val scoreMap = accumulatedScores.associateBy { it.participantId }
            scores.forEach { score ->
                scoreMap[score.participantId]?.accumulate(score)
            }

            // Rank needs to be recalculated; replace entries with newly ranked copies
            val reranked = RankDecorator.updateRanks(accumulatedScores)
            accumulatedScores.clear()
            accumulatedScores.addAll(reranked)
        }
    }

    fun getScores(): List<Score> = accumulatedScores
}