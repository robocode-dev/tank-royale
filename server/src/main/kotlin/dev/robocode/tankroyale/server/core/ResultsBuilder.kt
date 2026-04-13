package dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.schema.ResultsForBot
import dev.robocode.tankroyale.schema.ResultsForObserver
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.score.ResultsView
import kotlin.math.roundToInt

/** Builds result objects for broadcast at game end and round end. */
class ResultsBuilder(
    private val modelUpdater: () -> ModelUpdater?,
    private val participantRegistry: ParticipantRegistry
) {

    fun buildResultsForBot(botId: BotId): ResultsForBot {
        val results = requireNotNull(modelUpdater()) { "modelUpdater is null" }.getResults()
        val index = results.indexOfFirst { it.participantId.botId == botId }
        check(index >= 0) { "botId was not found in results: $botId" }

        val score = results[index]
        return ResultsForBot().apply {
            this.rank = index + 1
            survival = score.survivalScore.roundToInt()
            lastSurvivorBonus = score.lastSurvivorBonus.roundToInt()
            bulletDamage = score.bulletDamageScore.roundToInt()
            bulletKillBonus = score.bulletKillBonus.toInt()
            ramDamage = score.ramDamageScore.roundToInt()
            ramKillBonus = score.ramKillBonus.roundToInt()
            totalScore = score.totalScore.roundToInt()
            firstPlaces = score.firstPlaces
            secondPlaces = score.secondPlaces
            thirdPlaces = score.thirdPlaces
        }
    }

    fun buildResultsForObservers(): List<ResultsForObserver> {
        val results = mutableListOf<ResultsForObserver>()
        val updater = requireNotNull(modelUpdater()) { "modelUpdater is null" }

        val scores = ResultsView.getResults(updater.getResults(), participantRegistry.participantMap.values).toList()
        scores.forEach { score ->
            participantRegistry.participantMap[score.participantId.botId]?.let { participant ->
                val (id, name, version) =
                    if (participant.teamId == null)
                        Triple(participant.id, participant.name, participant.version)
                    else
                        Triple(participant.teamId, participant.teamName, participant.teamVersion)

                ResultsForObserver().apply {
                    this.id = id
                    this.name = name
                    this.version = version
                    this.rank = score.rank
                    isTeam = participant.teamId != null
                    survival = score.survivalScore.roundToInt()
                    lastSurvivorBonus = score.lastSurvivorBonus.roundToInt()
                    bulletDamage = score.bulletDamageScore.roundToInt()
                    bulletKillBonus = score.bulletKillBonus.toInt()
                    ramDamage = score.ramDamageScore.roundToInt()
                    ramKillBonus = score.ramKillBonus.roundToInt()
                    totalScore = score.totalScore.roundToInt()
                    firstPlaces = score.firstPlaces
                    secondPlaces = score.secondPlaces
                    thirdPlaces = score.thirdPlaces

                    results += this
                }
            }
        }
        return results
    }
}
