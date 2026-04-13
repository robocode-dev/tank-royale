/** Registry of event priorities. Higher number = higher priority. */
export class EventPriorities {
  private readonly priorities: Map<string, number>;

  private static readonly DEFAULTS: ReadonlyMap<string, number> = new Map([
    ["WonRoundEvent", 150],
    ["SkippedTurnEvent", 140],
    ["TickEvent", 130],
    ["CustomEvent", 120],
    ["TeamMessageEvent", 110],
    ["BotDeathEvent", 100],
    ["BulletHitWallEvent", 90],
    ["BulletHitBulletEvent", 80],
    ["BulletHitBotEvent", 70],
    ["BulletFiredEvent", 60],
    ["HitByBulletEvent", 50],
    ["HitWallEvent", 40],
    ["HitBotEvent", 30],
    ["ScannedBotEvent", 20],
    ["DeathEvent", 10],
  ]);

  constructor() {
    this.priorities = new Map(EventPriorities.DEFAULTS);
  }

  getPriority(eventType: string): number {
    const p = this.priorities.get(eventType);
    return p !== undefined ? p : 1;
  }

  setPriority(eventType: string, priority: number): void {
    this.priorities.set(eventType, priority);
  }
}
