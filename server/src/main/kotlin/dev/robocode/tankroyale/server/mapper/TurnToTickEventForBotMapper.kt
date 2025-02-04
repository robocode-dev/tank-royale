package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.game.Message
import dev.robocode.tankroyale.schema.game.TickEventForBot
import dev.robocode.tankroyale.server.mapper.BotToBotStateMapper.map
import dev.robocode.tankroyale.server.mapper.BulletsToBulletStatesMapper.map
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.ITurn

object TurnToTickEventForBotMapper {
    fun map(roundNumber: Int, turn: ITurn, botId: BotId, enemyCount: Int): TickEventForBot? {
        val bot = turn.getBot(botId) ?: return null
        return TickEventForBot().apply {
            type = Message.Type.TICK_EVENT_FOR_BOT
            this.roundNumber = roundNumber
            turnNumber = turn.turnNumber
            botState = map(bot, enemyCount)
            bulletStates = map(turn.bullets.filter { it.botId == bot.id }.toSet())
            events = EventsMapper.map(turn.getEvents(botId))
        }
    }
}