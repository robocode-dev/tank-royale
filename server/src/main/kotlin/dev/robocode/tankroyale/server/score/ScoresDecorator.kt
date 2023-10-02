package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.server.model.Score

object ScoresDecorator {

    fun updateRanks(orderedScores: List<Score>) {
        var lastTotalScore = 0.0
        var rank = 1

        // Update ranks

        for (row in orderedScores.indices) {
            val score = orderedScores[row]
            val totalScore = score.totalScore
            if (totalScore != lastTotalScore) {
                rank = row + 1
                lastTotalScore = totalScore
            }
            score.rank = rank
        }
    }

    fun increment1st2ndAnd3rdPlaces(scores: Collection<Score>) {
        scores.filter { it.rank == 1 }.toList().forEach { it.firstPlaces += 1 }
        scores.filter { it.rank == 2 }.toList().forEach { it.secondPlaces += 1 }
        scores.filter { it.rank == 3 }.toList().forEach { it.thirdPlaces += 1 }
    }
}