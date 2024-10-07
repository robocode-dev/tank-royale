package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.model.Score

object RankDecorator {

    fun updateRanks(scores: List<Score>) {
        var lastTotalScore: Double? = null
        var rank = 0

        // Update ranks

        for (row in scores.sortedByDescending { it.totalScore }.indices) {
            val score = scores[row]
            val totalScore = score.totalScore
            if (totalScore != lastTotalScore) {
                rank = row + 1
            }
            lastTotalScore = totalScore
            score.rank = rank
        }
    }
}