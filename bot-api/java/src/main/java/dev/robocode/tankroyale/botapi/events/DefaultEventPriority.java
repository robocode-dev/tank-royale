package dev.robocode.tankroyale.botapi.events;

/**
 * Default event priorities. The higher value, the higher event priority.
 */
public final class DefaultEventPriority {
    static final int ON_TICK = 150;
    static final int ON_WON_ROUND = 140;
    static final int ON_SKIPPED_TURN = 130;
    static final int ON_CONDITION = 120;
//    static final int ON_TEAM_MESSAGE = 110; // Reserved for future
    static final int ON_BOT_DEATH = 100;
    static final int ON_BULLET_FIRED = 90;
    static final int ON_BULLET_HIT_WALL = 80;
    static final int ON_BULLET_HIT_BULLET = 70;
    static final int ON_BULLET_HIT = 60;
    static final int ON_HIT_BY_BULLET = 50;
    static final int ON_HIT_WALL = 40;
    static final int ON_HIT_BOT = 30;
    static final int ON_SCANNED_BOT = 20;
    static final int ON_DEATH = 10;
}
