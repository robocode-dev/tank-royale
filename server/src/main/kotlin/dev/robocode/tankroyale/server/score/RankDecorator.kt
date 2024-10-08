package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.model.Score

// Uses Shared Placement System where two participants sharing the same score gets the same rank
object RankDecorator {

    fun updateRanks(scores: List<Score>) {
        var lastTotalScore = 0.0
        var lastRank = 0

        scores.sortedByDescending { it.totalScore }.forEachIndexed { index, score ->
            if (score.totalScore != lastTotalScore) {
                lastRank = index + 1
            }
            score.rank = lastRank
            lastTotalScore = score.totalScore
        }
    }
}