/**
 * Default event priority values. The higher value, the higher event priority. So WonRoundEvent has the
 * highest priority (150), and DeathEvent has the lowest priority (10).
 */
export const DefaultEventPriority = {
  /** Event priority for the WonRoundEvent */
  WON_ROUND: 150,

  /** Event priority for the SkippedTurnEvent */
  SKIPPED_TURN: 140,

  /** Event priority for the TickEvent */
  TICK: 130,

  /** Event priority for the CustomEvent */
  CUSTOM: 120,

  /** Event priority for the TeamMessageEvent */
  TEAM_MESSAGE: 110,

  /** Event priority for the BotDeathEvent */
  BOT_DEATH: 100,

  /** Event priority for the BulletHitWallEvent */
  BULLET_HIT_WALL: 90,

  /** Event priority for the BulletHitBulletEvent */
  BULLET_HIT_BULLET: 80,

  /** Event priority for the BulletHitBotEvent */
  BULLET_HIT_BOT: 70,

  /** Event priority for the BulletFiredEvent */
  BULLET_FIRED: 60,

  /** Event priority for the HitByBulletEvent */
  HIT_BY_BULLET: 50,

  /** Event priority for the HitWallEvent */
  HIT_WALL: 40,

  /** Event priority for the HitBotEvent */
  HIT_BOT: 30,

  /** Event priority for the ScannedBotEvent */
  SCANNED_BOT: 20,

  /** Event priority for the DeathEvent */
  DEATH: 10,
} as const;
