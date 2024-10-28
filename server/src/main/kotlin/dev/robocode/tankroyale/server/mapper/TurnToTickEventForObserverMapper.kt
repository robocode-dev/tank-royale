package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.game.Message
import dev.robocode.tankroyale.schema.game.Participant
import dev.robocode.tankroyale.schema.game.TickEventForObserver
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.ITurn
import java.util.Collections.unmodifiableList

object TurnToTickEventForObserverMapper {
    fun map(
        roundNumber: Int,
        turn: ITurn,
        participantsMap: Map<BotId, Participant>,
        enemyCountMap: Map<BotId, Int /* enemyCount */>
    ): TickEventForObserver {
        val tick = TickEventForObserver()
        tick.apply {
            type = Message.Type.TICK_EVENT_FOR_OBSERVER
            this.roundNumber = roundNumber
            turnNumber = turn.turnNumber
            botStates = unmodifiableList(BotsToBotsWithIdMapper.map(turn.bots, participantsMap, enemyCountMap))
            bulletStates = unmodifiableList(BulletsToBulletStatesMapper.map(turn.bullets))
            events = unmodifiableList(EventsMapper.map(turn.observerEvents))
        }
        return tick
    }
}