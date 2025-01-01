package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.score

import dev.robocode.tankroyale.schema.game.Participant
import dev.robocode.tankroyale.server.model.Score
import java.util.*

object ResultsView {

    fun getResults(botScores: Collection<Score>, participants: Collection<Participant>): Collection<Score> {

        data class Participant(private val id: Int, private val name: String)

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
        val sortedRows = TreeMap<Participant, Score> { p1, p2 -> rows[p2]!!.totalScore.compareTo(rows[p1]!!.totalScore) }
        sortedRows.putAll(rows)
        return sortedRows.values
    }
}