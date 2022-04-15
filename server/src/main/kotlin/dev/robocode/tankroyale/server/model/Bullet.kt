package dev.robocode.tankroyale.server.model

/**
 * An immutable bullet.
 * The [startPosition] and [tick] defines the current position of the bullet.
 * @param id Unique id of the bullet.
 * @param botId Unique id of the bot that fired this bullet.
 * @param power Power of the bullet.
 * @param direction Direction of the bullet in degrees.
 * @param color Color of the bullet. If set to `null`, the default bullet color will be used.
 * @param startPosition Start position where the bullet was fired from.
 * @param tick Tick, which is the number of turns since the bullet was fired.
 */
data class Bullet(
    override val id: BulletId,
    override val botId: BotId,
    override val power: Double,
    override val direction: Double,
    override val color: Color?,
    override val startPosition: Point, // must be immutable as this point is used for calculating future positions
    override val tick: Int = 0,

    ) : IBullet {

    /**
     * Returns a mutable copy of this point.
     * @return a [MutableBullet] instance that is a copy of this bullet.
     */
    fun toMutableBullet() = MutableBullet(id, botId, power, direction, color, startPosition, tick)

    /**
     * Returns a hash code that is the (unique) id of this bullet making this call fast.
     * @return the (unique) id of this bullet.
     * @see [Object.hashCode]
     */
    override fun hashCode(): Int {
        return id.value
    }

    /**
     * Compares this bullet another object by checking if the input object is a [IBullet] instance and share the same
     * bullet id.
     * @param other is any object.
     * @return `true` if the two bullets are equal; `false` otherwise.
     * @see [Object.equals]
     */
    override fun equals(other: Any?): Boolean {
        return other is IBullet && other.id == id
    }
}