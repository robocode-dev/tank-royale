package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.Message
import dev.robocode.tankroyale.schema.TickEventForObserver
import dev.robocode.tankroyale.server.mapper.BotsToBotsWithIdMapper.map
import dev.robocode.tankroyale.server.mapper.BulletsToBulletStatesMapper.map
import dev.robocode.tankroyale.server.model.Round
import dev.robocode.tankroyale.server.model.Turn

object TurnToTickEventForObserverMapper {
    fun map(round: Round, turn: Turn): TickEventForObserver {
        val tick = TickEventForObserver()
        tick.apply {
            `$type` = Message.`$type`.TICK_EVENT_FOR_OBSERVER
            roundNumber = round.roundNumber
            turnNumber = turn.turnNumber
            botStates = map(turn.bots)
            bulletStates = map(turn.bullets)
            events = EventsToEventsMapper.map(turn.observerEvents)
        }
        return tick
    }
}