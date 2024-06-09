package dev.robocode.tankroyale.botapi.events;

/**
 * Default event priorities values. The higher value, the higher event priority. So the {@link WonRoundEvent} has the
 * highest priority ({@value WON_ROUND}), and {@link DeathEvent} has the lowest priority ({@value DEATH}).
 */
public final class DefaultEventPriority {

    // Hide constructor to prevent instantiation
    private DefaultEventPriority() {
    }

    /**
     * Event priority for the {@link WonRoundEvent}
     */
    public static final int WON_ROUND = 150;
    /**
     * Event priority for the {@link SkippedTurnEvent}
     */
    public static final int SKIPPED_TURN = 140;
    /**
     * Event priority for the {@link TickEvent}
     */
    public static final int TICK = 130;
    /**
     * Event priority for the {@link CustomEvent}
     */
    public static final int CUSTOM = 120;
    /**
     * Event priority for the {@link TeamMessageEvent}.
     */
    public static final int TEAM_MESSAGE = 110;
    /**
     * Event priority for the {@link BotDeathEvent}
     */
    public static final int BOT_DEATH = 100;
    /**
     * Event priority for the {@link BulletHitWallEvent}
     */
    public static final int BULLET_HIT_WALL = 90;
    /**
     * Event priority for the {@link BulletHitBulletEvent}
     */
    public static final int BULLET_HIT_BULLET = 80;
    /**
     * Event priority for the {@link BulletHitBotEvent}
     */
    public static final int BULLET_HIT_BOT = 70;
    /**
     * Event priority for the {@link BulletFiredEvent}
     */
    public static final int BULLET_FIRED = 60;
    /**
     * Event priority for the {@link HitByBulletEvent}
     */
    public static final int HIT_BY_BULLET = 50;
    /**
     * Event priority for the {@link HitWallEvent}
     */
    public static final int HIT_WALL = 40;
    /**
     * Event priority for the {@link HitBotEvent}
     */
    public static final int HIT_BOT = 30;
    /**
     * Event priority for the {@link ScannedBotEvent}
     */
    public static final int SCANNED_BOT = 20;
    /**
     * Event priority for the {@link DeathEvent}
     */
    public static final int DEATH = 10;
}