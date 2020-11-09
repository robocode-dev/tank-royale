package dev.robocode.tankroyale.botapi.events;

/** Default event priorities. */
public final class EventPriority {
    public static int onWonRound = DefaultEventPriority.ON_WON_ROUND;
    public static int onSkippedTurn = DefaultEventPriority.ON_SKIPPED_TURN;
    public static int onTick = DefaultEventPriority.ON_TICK;
    public static int onCondition = DefaultEventPriority.ON_CONDITION;
//    public static int onTeamMessage = DefaultEventPriority.ON_TEAM_MESSAGE;
    public static int onBotDeath = DefaultEventPriority.ON_BOT_DEATH;
    public static int onBulletFired = DefaultEventPriority.ON_BULLET_FIRED;
    public static int onBulletHitWall = DefaultEventPriority.ON_BULLET_HIT_WALL;
    public static int onBulletHitBullet = DefaultEventPriority.ON_BULLET_HIT_BULLET;
    public static int onBulletHit = DefaultEventPriority.ON_BULLET_HIT;
    public static int onHitByBullet = DefaultEventPriority.ON_HIT_BY_BULLET;
    public static int onHitWall = DefaultEventPriority.ON_HIT_WALL;
    public static int onHitBot = DefaultEventPriority.ON_HIT_BOT;
    public static int onScannedBot = DefaultEventPriority.ON_SCANNED_BOT;
    public static int onDeath = DefaultEventPriority.ON_DEATH;
}
