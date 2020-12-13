package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BulletState
import dev.robocode.tankroyale.server.math.normalAbsoluteDegrees
import dev.robocode.tankroyale.server.model.Bullet

object BulletToBulletStateMapper {
    fun map(bullet: Bullet): BulletState {
        val bulletState = BulletState()
        bullet.apply {
            val (x, y) = calcPosition()
            bulletState.ownerId = botId.value
            bulletState.bulletId = bulletId.value
            bulletState.direction = normalAbsoluteDegrees(direction)
            bulletState.power = power
            bulletState.speed = speed
            bulletState.x = x
            bulletState.y = y
            bulletState.color = color
        }
        return bulletState
    }
}