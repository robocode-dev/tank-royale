package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.model.Score

class AccumulatedScoreCalculator {

    private val accumulatedScores: ArrayList<Score> = ArrayList()

    fun addScores(scores: List<Score>) {
        val newScoreMap = scores.associateBy { it.participantId }

        val merged = if (accumulatedScores.isEmpty()) {
            scores
        } else {
            accumulatedScores.map { existing ->
                val incoming = newScoreMap[existing.participantId]
                if (incoming != null) existing + incoming else existing
            }
        }

        val reranked = RankDecorator.updateRanks(merged)
        accumulatedScores.clear()
        accumulatedScores.addAll(reranked)
    }

    fun getScores(): List<Score> = accumulatedScores
}