package dev.robocode.tankroyale.botapi.events;

/**
 * Default event priorities. The lower value, the higher event priority.
 */
public final class DefaultEventPriority {
    static final int ON_SCANNED_BOT = 10;
    static final int ON_HIT_BOT = 20;
    static final int ON_HIT_WALL = 30;
    static final int ON_HIT_BY_BULLET = 40;
    static final int ON_BULLET_HIT = 50;
    static final int ON_BULLET_HIT_BULLET = 60;
    static final int ON_BULLET_HIT_WALL = 70;
    static final int ON_BULLET_FIRED = 80;
    static final int ON_BOT_DEATH = 90;
//    static final int ON_TEAM_MESSAGE = 100; // Reserved for future
    static final int ON_CONDITION = 110;
    static final int ON_TICK = 120;
    static final int ON_SKIPPED_TURN = 130;
    static final int ON_WON_ROUND = 140;
    static final int ON_DEATH = 150;
}
