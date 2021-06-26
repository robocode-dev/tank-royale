package dev.robocode.tankroyale.server.model

/** Convenient class to wrap a bullet to cache the current and next position of the bullet. */
data class BulletLine(
    /** The bullet of the bullet line */
    val bullet: IBullet // bullet need to be a copy/snapshot!
) {
    /** Start position of the bullet line */
    private val start: Point by lazy { bullet.position() }

    /** End position of the bullet line */
    val end: Point by lazy { bullet.nextPosition() }

    /** The current bullet line */
    val line by lazy { Line(start, end) }
}