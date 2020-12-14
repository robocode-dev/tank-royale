package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BulletState
import dev.robocode.tankroyale.server.model.Bullet

object BulletsToBulletStatesMapper {
    fun map(bullets: Set<Bullet>): List<BulletState> {
        val bulletStates = mutableListOf<BulletState>()
        bullets.forEach { bulletStates += BulletToBulletStateMapper.map(it) }
        return bulletStates
    }
}