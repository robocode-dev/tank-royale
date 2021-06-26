package model.factory

import dev.robocode.tankroyale.server.model.*

class BulletFactory {
    companion object {

        fun createBullet(
            id: BulletId = BulletId(1),
            botId: BotId = BotId(2),
            power: Double = 1.3,
            direction: Double = 123.456,
            color: Color? = Color(0x112233),
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

        fun createIBullet(
            id: BulletId = BulletId(1),
            botId: BotId = BotId(2),
            power: Double = 1.3,
            direction: Double = 123.456,
            color: Color? = Color(0x112233),
            startPosition: Point = Point(221.34, 643.23),
            tick: Int = 27
        ) = object: IBullet {
            override val power = power
            override val id = id
            override val botId = botId
            override val direction = direction
            override val color: Color? = color
            override val startPosition = startPosition
            override val tick: Int = tick
        }
    }
}