package dev.robocode.tankroyale.botapi;

import java.awt.Color;

/**
 * Represents the state of a bullet that has been fired by a bot.
 */
@SuppressWarnings("unused")
public final class BulletState {

    /**
     * Unique id of the bullet.
     */
    private final int bulletId;

    /**
     * Id of the bot that fired the bullet.
     */
    private final int ownerId;

    /**
     * Bullet firepower level.
     */
    private final double power;

    /**
     * X coordinate.
     */
    private final double x;

    /**
     * Y coordinate.
     */
    private final double y;

    /**
     * Direction in degrees.
     */
    private final double direction;

    /**
     * Color of the bullet.
     */
    private final Color color;

    /**
     * Initializes a new instance of the BulletState class.
     *
     * @param bulletId  is the unique id of the bullet.
     * @param ownerId   is the id of the bot that fired the bullet.
     * @param power     is the bullet firepower level.
     * @param x         is the X coordinate of the bullet.
     * @param y         is the Y coordinate of the bullet.
     * @param direction is the direction in degrees.
     * @param color     is the color of the bullet.
     */
    public BulletState(int bulletId, int ownerId, double power, double x, double y, double direction, Color color) {
        this.bulletId = bulletId;
        this.ownerId = ownerId;
        this.power = power;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.color = color;
    }

    /**
     * Returns the unique id of the bullet.
     *
     * @return The unique id of the bullet.
     */
    public int getBulletId() {
        return bulletId;
    }

    /**
     * Returns the id of the owner bot that fired the bullet.
     *
     * @return The id of the owner bot that fired the bullet.
     */
    public int getOwnerId() {
        return ownerId;
    }

    /**
     * Returns the bullet firepower level.
     *
     * @return The bullet firepower level.
     */
    public double getPower() {
        return power;
    }

    /**
     * Returns the X coordinate of the bullet.
     *
     * @return The X coordinate of the bullet.
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the Y coordinate of the bullet.
     *
     * @return The Y coordinate of the bullet.
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the direction of the bullet in degrees.
     *
     * @return The direction of the bullet in degrees.
     */
    public double getDirection() {
        return direction;
    }

    /**
     * Returns the speed of the bullet measured in units per turn.
     *
     * @return The speed of the bullet measured in units per turn.
     */
    public double getSpeed() {
        return 20 - 3 * power;
    }

    /**
     * Returns the color of the bullet.
     *
     * @return The color of the bullet.
     */
    public Color getColor() {
        return color;
    }
}
