package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.Message
import dev.robocode.tankroyale.schema.TickEventForObserver
import dev.robocode.tankroyale.server.mapper.BotsToBotsWithIdMapper.map
import dev.robocode.tankroyale.server.mapper.BulletsToBulletStatesMapper.map
import dev.robocode.tankroyale.server.model.ITurn
import java.util.concurrent.CopyOnWriteArrayList

object TurnToTickEventForObserverMapper {
    fun map(roundNumber: Int, turn: ITurn): TickEventForObserver {
        val tick = TickEventForObserver()
        tick.apply {
            type = Message.Type.TICK_EVENT_FOR_OBSERVER
            this.roundNumber = roundNumber
            turnNumber = turn.turnNumber
            botStates = CopyOnWriteArrayList(map(turn.bots))
            bulletStates = CopyOnWriteArrayList(map(turn.bullets))
            events = CopyOnWriteArrayList(EventsToEventsMapper.map(turn.observerEvents))
        }
        return tick
    }
}