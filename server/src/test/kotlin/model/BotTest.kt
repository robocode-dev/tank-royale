package model

import dev.robocode.tankroyale.server.model.Bot
import dev.robocode.tankroyale.server.model.BotId
import dev.robocode.tankroyale.server.model.Point
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe


class BotTest : StringSpec({

    "x and y coordinate must match the position coordinates" {
        val bot = Bot(
            sessionId = null,
            position = Point(1.2, 9.8),
            id = BotId(0), direction = 0.0, gunDirection = 0.0, radarDirection = 0.0
        )

        bot.x shouldBe bot.position.x
        bot.y shouldBe bot.position.y
    }
})