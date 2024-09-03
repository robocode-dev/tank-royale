package model.factory

import dev.robocode.tankroyale.server.model.*

class BulletFactory {
    companion object {

        fun createBullet(
            id: BulletId = BulletId(1),
            botId: BotId = BotId(2),
            power: Double = 1.3,
            direction: Double = 123.456,
            color: Color? = Color.from("#112233"),
            startPosition: Point = Point(221.34, 643.23),
            tick: Int = 27
        ) = Bullet(
            id = id,
            botId = botId,
            power = power,
            direction = direction,
            color = color,
            startPosition = startPosition,
            tick = tick
        )
    }
}