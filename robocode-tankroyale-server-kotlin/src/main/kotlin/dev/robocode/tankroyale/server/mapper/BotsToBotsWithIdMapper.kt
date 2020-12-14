package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BotStateWithId
import dev.robocode.tankroyale.server.model.Bot

object BotsToBotsWithIdMapper {
    fun map(bots: Set<Bot>): List<BotStateWithId> {
        val botStates = mutableListOf<BotStateWithId>()
        bots.forEach { botStates += BotToBotStateWithIdMapper.map(it) }
        return botStates
    }
}