package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.Message
import dev.robocode.tankroyale.schema.TickEventForBot
import dev.robocode.tankroyale.server.mapper.BotToBotStateMapper.map
import dev.robocode.tankroyale.server.mapper.BulletsToBulletStatesMapper.map
import dev.robocode.tankroyale.server.model.Bot
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.Round
import dev.robocode.tankroyale.server.model.Turn

object TurnToTickEventForBotMapper {
    fun map(round: Round, turn: Turn, botId: BotId): TickEventForBot? {
        val bot: Bot = turn.getBot(botId) ?: return null
        val tick = TickEventForBot()
        tick.apply {
            `$type` = Message.`$type`.TICK_EVENT_FOR_BOT
            roundNumber = round.roundNumber
            turnNumber = turn.turnNumber
            enemyCount = turn.bots.size - 1
            botState = map(bot)
            bulletStates = map(turn.bullets)
            events = EventsToEventsMapper.map(turn.getEvents(botId)!!)
        }
        return tick
    }
}