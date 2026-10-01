package dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.schema.Participant
import dev.robocode.tankroyale.server.model.Score
import java.util.*

object ResultsView {

    fun getResults(botScores: Collection<Score>, participants: Collection<Participant>): Collection<Score> {

        // Team ids and bot ids are separate id spaces, so a row is identified by its kind as well as its id
        data class Participant(val id: Int, val name: String, val isTeam: Boolean)

        val rows = mutableMapOf<Participant, Score>()

        participants.forEach { participant ->
            botScores.find { s -> s.participantId.botId.value == participant.id }?.let { botScore ->
                if (participant.teamId != null) {
                    val team = Participant(participant.teamId, participant.teamName, isTeam = true)
                    val accumulatedTeamScore = rows[team]
                    rows[team] = if (accumulatedTeamScore == null) {
                        botScore
                    } else {
                        accumulatedTeamScore + botScore
                    }
                } else {
                    rows[Participant(participant.id, participant.name, isTeam = false)] = botScore
                }
            }
        }
        // Sort by score descending, then by id ascending as tiebreaker to ensure stable ordering.
        // The remaining tiebreakers make the comparator consistent with equals, as a TreeMap silently
        // drops a row that compares equal to another row.
        val sortedRows = TreeMap<Participant, Score>(
            compareByDescending<Participant> { rows[it]!!.totalScore }
                .thenBy { it.id }
                .thenBy { it.isTeam }
                .thenBy { it.name }
        )
        sortedRows.putAll(rows)

        // Apply competition ranking (1224 style) to aggregated scores
        return RankDecorator.updateRanks(sortedRows.values.toList())
    }
}
