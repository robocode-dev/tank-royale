package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.schema.Participant
import dev.robocode.tankroyale.server.model.Score
import java.util.*

object ResultsView {

    fun getResults(botScores: Collection<Score>, participants: Collection<Participant>): Collection<Score> {

        data class Participant(val id: Int, private val name: String)

        val rows = mutableMapOf<Participant, Score>()

        participants.forEach { participant ->
            botScores.find { s -> s.participantId.botId.value == participant.id }?.let { botScore ->
                if (participant.teamId != null) {
                    val team = Participant(participant.teamId, participant.teamName)
                    val accumulatedTeamScore = rows[team]
                    rows[team] = if (accumulatedTeamScore == null) {
                        botScore
                    } else {
                        accumulatedTeamScore + botScore
                    }
                } else {
                    rows[Participant(participant.id, participant.name)] = botScore
                }
            }
        }
        // Sort by score descending, then by id ascending as tiebreaker to ensure stable ordering
        val sortedRows = TreeMap<Participant, Score> { p1, p2 ->
            val scoreComparison = rows[p2]!!.totalScore.compareTo(rows[p1]!!.totalScore)
            if (scoreComparison != 0) scoreComparison else p1.id.compareTo(p2.id)
        }
        sortedRows.putAll(rows)

        // Apply competition ranking (1224 style) to aggregated scores
        RankDecorator.updateRanks(sortedRows.values.toList())

        return sortedRows.values
    }
}
