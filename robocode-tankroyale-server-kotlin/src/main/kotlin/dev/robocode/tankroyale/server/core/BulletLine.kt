package dev.robocode.tankroyale.server.dev.robocode.tankroyale.server.core

import dev.robocode.tankroyale.server.math.Line
import dev.robocode.tankroyale.server.math.Point
import dev.robocode.tankroyale.server.model.Bullet

class BulletLine(val bullet: Bullet) {
    private val start: Point by lazy { bullet.calcPosition() }
    val end: Point by lazy { bullet.calcNextPosition() }
    val line: Line by lazy { Line(start, end) }
}