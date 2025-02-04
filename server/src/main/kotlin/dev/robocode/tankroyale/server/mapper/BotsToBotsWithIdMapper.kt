package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.game.BotStateWithId
import dev.robocode.tankroyale.schema.game.Participant
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.IBot

object BotsToBotsWithIdMapper {
    fun map(
        bots: Set<IBot>,
        participantsMap: Map<BotId, Participant>,
        enemyCountMap: Map<BotId, Int /* enemyCount */>,
        debugGraphicsEnableMap: Map<BotId, Boolean /* isDebugGraphicsEnabled */>
    ): List<BotStateWithId> {
        val botStates = mutableListOf<BotStateWithId>()
        bots.forEach { bot ->
            participantsMap[bot.id]?.let { participant ->
                botStates += BotToBotStateWithIdMapper.map(
                    bot,
                    participant.sessionId,
                    enemyCountMap[bot.id] ?: 0,
                    debugGraphicsEnableMap[bot.id] ?: false
                )
            }
        }
        return botStates
    }
}