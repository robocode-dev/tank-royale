package dev.robocode.tankroyale.botapi.events;

/**
 * Event occurring when a bot has collided with another bot.
 */
@SuppressWarnings("unused")
public final class HitBotEvent extends BotEvent {

    // id of the other bot that your bot has collided with.
    private final int victimId;

    // Remaining energy level of the victim bot.
    private final double energy;

    // X coordinate of victim bot.
    private final double x;

    // Y coordinate of victim bot.
    private final double y;

    // Flag specifying, if the victim bot got rammed.
    private final boolean isRammed;

    /**
     * Initializes a new instance of the BotHitBotEvent class.
     *
     * @param turnNumber is the turn number where the bot hit another bot.
     * @param victimId   is the id of the other bot that your bot has collided with.
     * @param energy     is the remaining energy level of the victim bot.
     * @param x          is the X coordinate of victim bot.
     * @param y          is the Y coordinate of victim bot.
     * @param isRammed   is the flag specifying, if the victim bot got rammed.
     */
    public HitBotEvent(int turnNumber, int victimId, double energy, double x, double y, boolean isRammed) {
        super(turnNumber);
        this.victimId = victimId;
        this.energy = energy;
        this.x = x;
        this.y = y;
        this.isRammed = isRammed;
    }

    /**
     * Returns the id of the other bot that your bot has collided with.
     *
     * @return The id of the other bot that your bot has collided with.
     */
    public int getVictimId() {
        return victimId;
    }

    /**
     * Returns the remaining energy level of the victim bot.
     *
     * @return The remaining energy level of the victim bot.
     */
    public double getEnergy() {
        return energy;
    }

    /**
     * Returns the X coordinate of victim bot.
     *
     * @return The X coordinate of victim bot.
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the Y coordinate of victim bot.
     *
     * @return The Y coordinate of victim bot.
     */
    public double getY() {
        return y;
    }

    /**
     * Checks if the other bot got rammed by your bot.
     *
     * @return {@code true} if the other bot got rammed; {@code false} otherwise.
     */
    public boolean isRammed() {
        return isRammed;
    }
}
