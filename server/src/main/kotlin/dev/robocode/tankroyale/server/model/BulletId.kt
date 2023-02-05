package dev.robocode.tankroyale.server.model

/**
 * BulletId contains the id of a bullet. It is a inline class used to make it easy to differ between an Int and a
 * BulletId.
 * @param value id value of the bullet.
 */
@JvmInline
value class BulletId(val value: Int)