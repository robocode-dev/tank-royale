package dev.robocode.tankroyale.botapi.events;

/** Default event priorities. The lower value, the higher priority. */
public final class DefaultEventPriority {
    static final int ON_WON_ROUND = 10;
    static final int ON_SKIPPED_TURN = 20;
    static final int ON_TICK = 30;
    static final int ON_CONDITION = 40;
//  static final int ON_TEAM_MESSAGE = 50; // Reserved for future
    static final int ON_BOT_DEATH = 60;
    static final int ON_BULLET_FIRED = 70;
    static final int ON_BULLET_HIT_WALL = 80;
    static final int ON_BULLET_HIT_BULLET = 90;
    static final int ON_BULLET_HIT = 100;
    static final int ON_HIT_BY_BULLET = 110;
    static final int ON_HIT_WALL = 120;
    static final int ON_HIT_BOT = 130;
    static final int ON_SCANNED_BOT = 140;
    static final int ON_DEATH = 150;
}
