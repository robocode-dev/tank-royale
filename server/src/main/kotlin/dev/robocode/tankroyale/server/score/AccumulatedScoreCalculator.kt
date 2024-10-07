package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.model.Score

class AccumulatedScoreCalculator {

    private val accumulatedScores: ArrayList<Score> = ArrayList()

    fun addScores(scores: List<Score>) {
        if (accumulatedScores.isEmpty()) {
            accumulatedScores.addAll(scores)
        } else {
            val scoreMap = accumulatedScores.associateBy { it.participantId }
            scores.forEach { score ->
                val accScore = scoreMap[score.participantId]
                accScore?.let { it += score }
            }

            // Rank needs to be recalculated
            RankDecorator.updateRanks(accumulatedScores)
        }
    }

    fun getScores(): List<Score> = accumulatedScores
}