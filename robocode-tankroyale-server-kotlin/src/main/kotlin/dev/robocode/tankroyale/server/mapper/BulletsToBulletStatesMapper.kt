package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.schema.BulletState
import dev.robocode.tankroyale.server.model.Bullet
import java.util.ArrayList

object BulletsToBulletStatesMapper {
    fun map(bullets: Set<Bullet>): List<BulletState> {
        val bulletStates: MutableList<BulletState> = ArrayList()
        bullets.forEach { bulletStates.add(BulletToBulletStateMapper.map(it)) }
        return bulletStates
    }
}