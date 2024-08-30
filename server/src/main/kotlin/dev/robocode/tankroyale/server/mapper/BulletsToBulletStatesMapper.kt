package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.game.BulletState
import dev.robocode.tankroyale.server.model.IBullet

object BulletsToBulletStatesMapper {
    fun map(bullets: Set<IBullet>): List<BulletState> {
        val bulletStates = mutableListOf<BulletState>()
        bullets.forEach { bulletStates += BulletToBulletStateMapper.map(it) }
        return bulletStates
    }
}