package dev.robocode.tankroyale.botapi.events;

/**
 * Default event priorities values. The higher value, the higher event priority. So the {@link DeathEvent} has the
 * highest priority ({@value DEATH}), and {@link WonRoundEvent} has the lowest priority ({@value WON_ROUND})
 */
public final class DefaultEventPriority {
    /**
     * Event priority for the {@link DeathEvent}
     */
    public static final int DEATH = 150;
    /**
     * Event priority for the {@link ScannedBotEvent}
     */
    public static final int SCANNED_BOT = 140;
    /**
     * Event priority for the {@link HitBotEvent}
     */
    public static final int HIT_BOT = 130;
    /**
     * Event priority for the {@link HitWallEvent}
     */
    public static final int HIT_WALL = 120;
    /**
     * Event priority for the {@link HitByBulletEvent}
     */
    public static final int HIT_BY_BULLET = 110;
    /**
     * Event priority for the {@link BulletFiredEvent}
     */
    public static final int BULLET_FIRED = 100;
    /**
     * Event priority for the {@link BulletHitBotEvent}
     */
    public static final int BULLET_HIT_BOT = 90;
    /**
     * Event priority for the {@link BulletHitBulletEvent}
     */
    public static final int BULLET_HIT_BULLET = 80;
    /**
     * Event priority for the {@link BulletHitWallEvent}
     */
    public static final int BULLET_HIT_WALL = 70;
    /**
     * Event priority for the {@link BotDeathEvent}
     */
    public static final int BOT_DEATH = 60;

//    public static final int TEAM_MESSAGE = 50; // Reserved for future

    /**
     * Event priority for the {@link CustomEvent}
     */
    public static final int CUSTOM = 40;
    /**
     * Event priority for the {@link TickEvent}
     */
    public static final int TICK = 30;
    /**
     * Event priority for the {@link SkippedTurnEvent}
     */
    public static final int SKIPPED_TURN = 20;
    /**
     * Event priority for the {@link WonRoundEvent}
     */
    public static final int WON_ROUND = 10;
}
