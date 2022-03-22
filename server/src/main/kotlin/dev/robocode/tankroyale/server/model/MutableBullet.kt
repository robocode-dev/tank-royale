package dev.robocode.tankroyale.server.model

/**
 * A mutable bullet, where the tick can be changed.
 * The [startPosition] and [tick] defines the current position of the bullet.
 * @param id Unique id of the bullet.
 * @param botId Unique id of the bot that fired this bullet.
 * @param power Power of the bullet.
 * @param direction Direction of the bullet in degrees.
 * @param color Color of the bullet. If set to `null`, the default bullet color will be used.
 * @param startPosition Start position where the bullet was fired from.
 * @param tick Tick, which is the number of turns since the bullet was fired.
 */
data class MutableBullet(
    /** Id of the bullet */
    override val id: BulletId,

    /** Id of the bot that fired this bullet */
    override val botId: BotId,

    /** Power of the bullet */
    override val power: Double,

    /** Direction of the bullet in degrees */
    override val direction: Double,

    /** Color of the bullet */
    override val color: Color?,

    /** Start position where the bullet was fired from */
    override val startPosition: Point, // must be immutable as this point is used for calculating future positions

    /** Tick, which is the number of turns since the bullet was fired */
    override var tick: Int = 0,

    ) : IBullet {

    /** Returns a immutable copy of this point */
    fun toBullet() = Bullet(id, botId, power, direction, color, startPosition, tick)

    /** Increment the tick used for moving the bullet when calculating its position. */
    fun incrementTick() {
        tick++
    }

    override fun hashCode(): Int {
        return id.value
    }

    override fun equals(other: Any?): Boolean {
        return other is IBullet && other.id == id
    }
}