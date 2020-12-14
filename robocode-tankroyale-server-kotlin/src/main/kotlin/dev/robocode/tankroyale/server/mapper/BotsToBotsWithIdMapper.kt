package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BotStateWithId
import dev.robocode.tankroyale.server.model.Bot
import java.util.ArrayList
import java.util.concurrent.CopyOnWriteArrayList

object BotsToBotsWithIdMapper {
    fun map(bots: Set<Bot>): List<BotStateWithId> {
        val botStates: MutableList<BotStateWithId> = CopyOnWriteArrayList()
        bots.forEach { botStates += BotToBotStateWithIdMapper.map(it) }
        return botStates
    }
}