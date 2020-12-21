package dev.robocode.tankroyale.server.model

data class MutableBullet(
    /** Id of the bot that fired this bullet */
    override val botId: BotId,

    /** Id of the bullet */
    override val bulletId: BulletId,

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
    fun toBullet() = Bullet(botId, bulletId, power, direction, color, startPosition, tick)

    /** Increment the tick used for moving the bullet when calculating its position. */
    fun incrementTick() { tick++ }

    override fun hashCode(): Int {
        return bulletId.value
    }

    override fun equals(other: Any?): Boolean {
        return other is IBullet && other.bulletId == bulletId
    }
}