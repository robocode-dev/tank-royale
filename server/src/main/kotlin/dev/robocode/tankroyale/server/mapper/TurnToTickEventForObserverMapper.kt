package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.Message
import dev.robocode.tankroyale.schema.Participant
import dev.robocode.tankroyale.schema.TickEventForObserver
import dev.robocode.tankroyale.server.mapper.BotsToBotsWithIdMapper.map
import dev.robocode.tankroyale.server.mapper.BulletsToBulletStatesMapper.map
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.ITurn
import java.util.Collections.unmodifiableList

object TurnToTickEventForObserverMapper {
    fun map(roundNumber: Int, turn: ITurn, participantsMap: Map<BotId, Participant>): TickEventForObserver {
        val tick = TickEventForObserver()
        tick.apply {
            type = Message.Type.TICK_EVENT_FOR_OBSERVER
            this.roundNumber = roundNumber
            turnNumber = turn.turnNumber
            botStates = unmodifiableList(map(turn.bots, participantsMap))
            bulletStates = unmodifiableList(map(turn.bullets))
            events = unmodifiableList(EventsMapper.map(turn.observerEvents))
        }
        return tick
    }
}