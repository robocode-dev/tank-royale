package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BulletState
import dev.robocode.tankroyale.server.math.normalAbsoluteDegrees
import dev.robocode.tankroyale.server.model.Bullet

object BulletToBulletStateMapper {
    fun map(bullet: Bullet): BulletState {
        return BulletState().apply {
            ownerId = bullet.botId.value
            bulletId = bullet.bulletId.value
            direction = normalAbsoluteDegrees(bullet.direction)
            power = bullet.power
            speed = bullet.speed
            x = bullet.position().x
            y = bullet.position().y
            color = bullet.color?.value
        }
    }
}