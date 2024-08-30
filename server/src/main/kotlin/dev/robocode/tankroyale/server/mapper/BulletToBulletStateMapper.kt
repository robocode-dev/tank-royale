package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.game.BulletState
import dev.robocode.tankroyale.server.model.normalizeAbsoluteDegrees
import dev.robocode.tankroyale.server.model.IBullet

object BulletToBulletStateMapper {
    fun map(bullet: IBullet): BulletState {
        return BulletState().apply {
            ownerId = bullet.botId.value
            bulletId = bullet.id.value
            direction = normalizeAbsoluteDegrees(bullet.direction)
            power = bullet.power
            x = bullet.position().x
            y = bullet.position().y
            color = bullet.color?.value
        }
    }
}