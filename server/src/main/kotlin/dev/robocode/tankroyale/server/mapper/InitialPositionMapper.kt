package dev.robocode.tankroyale.server.mapper

import dev.robocode.tankroyale.server.model.InitialPosition

object InitialPositionMapper {
    fun map(initialPosition: dev.robocode.tankroyale.schema.InitialPosition?): InitialPosition? {
        initialPosition?.apply {
            return InitialPosition(x, y, direction)
        }
        return null
    }
}
