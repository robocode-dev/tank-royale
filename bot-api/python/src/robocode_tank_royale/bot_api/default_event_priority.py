class DefaultEventPriority:
    """
    Default event priorities values. The higher value, the higher event priority. So the WonRoundEvent has the
    highest priority (150), and DeathEvent has the lowest priority (10).
    """

    WON_ROUND = 150
    """Event priority for the WonRoundEvent"""
    SKIPPED_TURN = 140
    """Event priority for the SkippedTurnEvent"""
    TICK = 130
    """Event priority for the TickEvent"""
    CUSTOM = 120
    """Event priority for the CustomEvent"""
    TEAM_MESSAGE = 110
    """Event priority for the TeamMessageEvent"""
    BOT_DEATH = 100
    """Event priority for the BotDeathEvent"""
    BULLET_HIT_WALL = 90
    """Event priority for the BulletHitWallEvent"""
    BULLET_HIT_BULLET = 80
    """Event priority for the BulletHitBulletEvent"""
    BULLET_HIT_BOT = 70
    """Event priority for the BulletHitBotEvent"""
    BULLET_FIRED = 60
    """Event priority for the BulletFiredEvent"""
    HIT_BY_BULLET = 50
    """Event priority for the HitByBulletEvent"""
    HIT_WALL = 40
    """Event priority for the HitWallEvent"""
    HIT_BOT = 30
    """Event priority for the HitBotEvent"""
    SCANNED_BOT = 20
    """Event priority for the ScannedBotEvent"""
    DEATH = 10
    """Event priority for the DeathEvent"""
