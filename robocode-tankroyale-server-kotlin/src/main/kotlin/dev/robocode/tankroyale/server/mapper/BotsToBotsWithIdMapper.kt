package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BotStateWithId
import dev.robocode.tankroyale.server.model.Bot
import java.util.ArrayList

object BotsToBotsWithIdMapper {
    fun map(bots: Set<Bot>): List<BotStateWithId> {
        val botStates: MutableList<BotStateWithId> = ArrayList()
        bots.forEach { botStates.add(BotToBotStateWithIdMapper.map(it)) }
        return botStates
    }
}