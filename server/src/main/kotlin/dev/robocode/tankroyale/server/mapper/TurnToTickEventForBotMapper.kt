package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.Message
import dev.robocode.tankroyale.schema.TickEventForBot
import dev.robocode.tankroyale.server.mapper.BotToBotStateMapper.map
import dev.robocode.tankroyale.server.mapper.BulletsToBulletStatesMapper.map
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.ITurn

object TurnToTickEventForBotMapper {
    fun map(roundNumber: Int, turn: ITurn, botId: BotId): TickEventForBot? {
        val bot = turn.getBot(botId) ?: return null
        val tick = TickEventForBot()
        tick.apply {
            type = Message.Type.TICK_EVENT_FOR_BOT
            this.roundNumber = roundNumber
            turnNumber = turn.turnNumber
            enemyCount = turn.bots.size - 1
            botState = map(bot)
            bulletStates = map(turn.bullets)
            events = EventsToEventsMapper.map(turn.getEvents(botId))
        }
        return tick
    }
}