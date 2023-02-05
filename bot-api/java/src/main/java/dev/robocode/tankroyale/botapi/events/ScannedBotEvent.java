package dev.robocode.tankroyale.botapi.events;

/**
 * Event occurring when a bot has scanned another bot.
 */
@SuppressWarnings("unused")
public final class ScannedBotEvent extends BotEvent {

    // id of the bot did the scanning.
    private final int scannedByBotId;

    // id of the bot that was scanned.
    private final int scannedBotId;

    // Energy level of the scanned bot.
    private final double energy;

    // X coordinate of the scanned bot.
    private final double x;

    // Y coordinate of the scanned bot.
    private final double y;

    // Direction in degrees of the scanned bot.
    private final double direction;

    // Speed measured in units per turn of the scanned bot.
    private final double speed;

    /**
     * Initializes a new instance of the ScannedBotEvent class.
     *
     * @param turnNumber     is the turn number when the bot was scanned.
     * @param scannedByBotId is the id of the bot did the scanning.
     * @param scannedBotId   is the id of the bot that was scanned.
     * @param energy         is the energy level of the scanned bot.
     * @param x              is the X coordinate of the scanned bot.
     * @param y              is the Y coordinate of the scanned bot.
     * @param direction      is the direction in degrees of the scanned bot.
     * @param speed          is the speed measured in units per turn of the scanned bot.
     */
    public ScannedBotEvent(
            int turnNumber,
            int scannedByBotId,
            int scannedBotId,
            double energy,
            double x,
            double y,
            double direction,
            double speed) {
        super(turnNumber);
        this.scannedByBotId = scannedByBotId;
        this.scannedBotId = scannedBotId;
        this.energy = energy;
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.speed = speed;
    }

    /**
     * Returns the id of the bot did the scanning.
     *
     * @return The id of the bot did the scanning.
     */
    public int getScannedByBotId() {
        return scannedByBotId;
    }

    /**
     * Returns the id of the bot that was scanned.
     *
     * @return The id of the bot that was scanned.
     */
    public int getScannedBotId() {
        return scannedBotId;
    }

    /**
     * Returns the energy level of the scanned bot.
     *
     * @return The energy level of the scanned bot.
     */
    public double getEnergy() {
        return energy;
    }

    /**
     * Returns the X coordinate of the scanned bot.
     *
     * @return The X coordinate of the scanned bot.
     */
    public double getX() {
        return x;
    }

    /**
     * Returns the Y coordinate of the scanned bot.
     *
     * @return The Y coordinate of the scanned bot.
     */
    public double getY() {
        return y;
    }

    /**
     * Returns the direction in degrees of the scanned bot.
     *
     * @return The direction in degrees of the scanned bot.
     */
    public double getDirection() {
        return direction;
    }

    /**
     * Returns the Speed measured in units per turn of the scanned bot.
     *
     * @return The Speed measured in units per turn of the scanned bot.
     */
    public double getSpeed() {
        return speed;
    }
}
