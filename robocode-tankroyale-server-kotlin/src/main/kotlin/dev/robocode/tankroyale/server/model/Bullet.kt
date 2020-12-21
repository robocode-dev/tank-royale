package dev.robocode.tankroyale.server.model

data class Bullet(
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
    override val tick: Int = 0,

    ) : IBullet {

    /** Returns a mutable copy of this point */
    fun toMutableBullet() = MutableBullet(botId, bulletId, power, direction, color, startPosition, tick)

    override fun hashCode(): Int {
        return bulletId.value
    }

    override fun equals(other: Any?): Boolean {
        return other is IBullet && other.bulletId == bulletId
    }
}