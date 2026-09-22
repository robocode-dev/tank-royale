package model

import dev.robocode.tankroyale.server.event.TeamMessageEvent
import dev.robocode.tankroyale.server.mapper.TurnToTickEventForBotMapper
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.Bot
import dev.robocode.tankroyale.server.model.MutableTurn
import dev.robocode.tankroyale.server.model.Point
import io.kotest.core.Tag
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly

class MutableTurnTest : FunSpec({
    tags(Tag("Unit"))

    test("Unit: private team messages retain insertion order and duplicates") {
        val recipient = BotId(2)
        val sender = BotId(1)
        val first = TeamMessageEvent(7, "same", "String", sender)
        val second = TeamMessageEvent(7, "same", "String", sender)
        val third = TeamMessageEvent(7, "last", "String", sender)
        val turn = MutableTurn(7)
        turn.bots += Bot(recipient, sessionId = null, position = Point(0.0, 0.0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0)

        turn.addPrivateBotEvent(recipient, first)
        turn.addPrivateBotEvent(recipient, second)
        turn.addPrivateBotEvent(recipient, third)

        turn.getEvents(recipient) shouldContainExactly listOf(first, second, third)
        turn.toTurn().getEvents(recipient) shouldContainExactly listOf(first, second, third)

        val tick = requireNotNull(TurnToTickEventForBotMapper.map(1, turn.toTurn(), recipient, 0))
        tick.events.map { (it as dev.robocode.tankroyale.schema.TeamMessageEvent).message }
            .shouldContainExactly("same", "same", "last")
    }
})
