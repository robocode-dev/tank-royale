package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BotStateWithId
import dev.robocode.tankroyale.server.model.IBot

object BotsToBotsWithIdMapper {
    fun map(bots: Set<IBot>): List<BotStateWithId> {
        val botStates = mutableListOf<BotStateWithId>()
        bots.forEach { botStates += BotToBotStateWithIdMapper.map(it) }
        return botStates
    }
}