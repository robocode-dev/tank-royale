package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BulletState
import dev.robocode.tankroyale.server.math.normalAbsoluteDegrees
import dev.robocode.tankroyale.server.model.IBullet

object BulletToBulletStateMapper {
    fun map(bullet: IBullet): BulletState {
        return BulletState().apply {
            ownerId = bullet.botId.id
            bulletId = bullet.bulletId.value
            direction = normalAbsoluteDegrees(bullet.direction)
            power = bullet.power
            speed = bullet.speed()
            x = bullet.position().x
            y = bullet.position().y
            color = bullet.color?.value
        }
    }
}