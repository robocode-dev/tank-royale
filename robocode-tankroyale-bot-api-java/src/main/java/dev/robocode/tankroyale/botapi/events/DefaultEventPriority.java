package dev.robocode.tankroyale.botapi.events;

/** Default event priorities. The lower value, the higher event priority. */
public final class DefaultEventPriority {
    static final int ON_ROUND_STARTED = 10;
    static final int ON_ROUND_ENDED = 20;
    static final int ON_SCANNED_BOT = 30;
    static final int ON_HIT_BOT = 40;
    static final int ON_HIT_WALL = 50;
    static final int ON_HIT_BY_BULLET = 60;
    static final int ON_BULLET_HIT = 70;
    static final int ON_BULLET_HIT_BULLET = 80;
    static final int ON_BULLET_HIT_WALL = 90;
    static final int ON_BULLET_FIRED = 100;
    static final int ON_BOT_DEATH = 110;
//  static final int ON_TEAM_MESSAGE = 120; // Reserved for future
    static final int ON_CONDITION = 130;
    static final int ON_TICK = 140;
    static final int ON_SKIPPED_TURN = 150;
    static final int ON_WON_ROUND = 160;
    static final int ON_DEATH = 170;
}
