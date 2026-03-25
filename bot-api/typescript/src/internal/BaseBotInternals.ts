import { BotInfo } from "../BotInfo.js";
import { BulletState } from "../BulletState.js";
import { Color } from "../graphics/Color.js";
import { IGraphics } from "../graphics/IGraphics.js";
import { BotEvent } from "../events/BotEvent.js";
import { Condition } from "../events/Condition.js";
import { EventQueue } from "../events/EventQueue.js";
import { EventPriorities } from "../events/EventPriorities.js";
import { EventInterruption } from "../events/EventInterruption.js";
import { BotEventHandlers } from "../events/BotEventHandlers.js";

/**
 * Internal implementation class for BaseBot.
 * This class holds the internal state and logic for the bot.
 * Full implementation is covered in task 4.
 */
export class BaseBotInternals {
  readonly botInfo: BotInfo;
  readonly serverUrl: URL | null;
  readonly serverSecret: string | null;
  readonly eventQueue: EventQueue;
  readonly botEventHandlers: BotEventHandlers;
  readonly eventPriorities: EventPriorities;
  readonly eventInterruption: EventInterruption;

  private myId = 0;
  private variant = "Tank Royale";
  private version = "";
  private gameType = "";
  private arenaWidth = 0;
  private arenaHeight = 0;
  private numberOfRounds = 0;
  private gunCoolingRate = 0;
  private maxInactivityTurns = 0;
  private turnTimeout = 0;
  private timeLeft = 0;
  private roundNumber = 0;
  private turnNumber = 0;
  private enemyCount = 0;
  private energy = 0;
  private disabled = false;
  private x = 0;
  private y = 0;
  private direction = 0;
  private gunDirection = 0;
  private radarDirection = 0;
  private speed = 0;
  private gunHeat = 0;
  private bulletStates: Set<BulletState> = new Set();

  private turnRate = 0;
  private maxTurnRate = 10;
  private gunTurnRate = 0;
  private maxGunTurnRate = 20;
  private radarTurnRate = 0;
  private maxRadarTurnRate = 45;
  private targetSpeed = 0;
  private maxSpeed = 8;
  private firepower = 0;

  private adjustGunForBodyTurn = false;
  private adjustRadarForBodyTurn = false;
  private adjustRadarForGunTurn = false;

  private stopped = false;
  private teammateIds: Set<number> = new Set();

  private bodyColor: Color | null = null;
  private turretColor: Color | null = null;
  private radarColor: Color | null = null;
  private bulletColor: Color | null = null;
  private scanColor: Color | null = null;
  private tracksColor: Color | null = null;
  private gunColor: Color | null = null;

  private running = false;
  private debuggingEnabled = false;

  constructor(_baseBot: object, botInfo: BotInfo, serverUrl: URL | null, serverSecret: string | null) {
    this.botInfo = botInfo;
    this.serverUrl = serverUrl;
    this.serverSecret = serverSecret;
    this.eventPriorities = new EventPriorities();
    this.eventInterruption = new EventInterruption();
    this.eventQueue = new EventQueue(this.eventPriorities, this.eventInterruption);
    this.botEventHandlers = new BotEventHandlers();
  }

  start(): void {
    // Full implementation in task 4
    throw new Error("BaseBotInternals.start() not yet implemented (task 4)");
  }

  execute(): void {
    // Full implementation in task 4
    throw new Error("BaseBotInternals.execute() not yet implemented (task 4)");
  }

  dispatchEvents(turnNumber: number): void {
    this.eventQueue.dispatchEvents(turnNumber, this.botEventHandlers);
  }

  getMyId(): number { return this.myId; }
  getVariant(): string { return this.variant; }
  getVersion(): string { return this.version; }
  getGameType(): string { return this.gameType; }
  getArenaWidth(): number { return this.arenaWidth; }
  getArenaHeight(): number { return this.arenaHeight; }
  getNumberOfRounds(): number { return this.numberOfRounds; }
  getGunCoolingRate(): number { return this.gunCoolingRate; }
  getMaxInactivityTurns(): number { return this.maxInactivityTurns; }
  getTurnTimeout(): number { return this.turnTimeout; }
  getTimeLeft(): number { return this.timeLeft; }
  getRoundNumber(): number { return this.roundNumber; }
  getTurnNumber(): number { return this.turnNumber; }
  getEnemyCount(): number { return this.enemyCount; }
  getEnergy(): number { return this.energy; }
  isDisabled(): boolean { return this.disabled; }
  getX(): number { return this.x; }
  getY(): number { return this.y; }
  getDirection(): number { return this.direction; }
  getGunDirection(): number { return this.gunDirection; }
  getRadarDirection(): number { return this.radarDirection; }
  getSpeed(): number { return this.speed; }
  getGunHeat(): number { return this.gunHeat; }
  getBulletStates(): ReadonlySet<BulletState> { return this.bulletStates; }
  getEvents(): readonly BotEvent[] { return this.eventQueue.getEvents(); }
  clearEvents(): void { this.eventQueue.clear(); }

  getTurnRate(): number { return this.turnRate; }
  setTurnRate(turnRate: number): void { this.turnRate = turnRate; }
  getMaxTurnRate(): number { return this.maxTurnRate; }
  setMaxTurnRate(maxTurnRate: number): void { this.maxTurnRate = maxTurnRate; }
  getGunTurnRate(): number { return this.gunTurnRate; }
  setGunTurnRate(gunTurnRate: number): void { this.gunTurnRate = gunTurnRate; }
  getMaxGunTurnRate(): number { return this.maxGunTurnRate; }
  setMaxGunTurnRate(maxGunTurnRate: number): void { this.maxGunTurnRate = maxGunTurnRate; }
  getRadarTurnRate(): number { return this.radarTurnRate; }
  setRadarTurnRate(radarTurnRate: number): void { this.radarTurnRate = radarTurnRate; }
  getMaxRadarTurnRate(): number { return this.maxRadarTurnRate; }
  setMaxRadarTurnRate(maxRadarTurnRate: number): void { this.maxRadarTurnRate = maxRadarTurnRate; }
  getTargetSpeed(): number { return this.targetSpeed; }
  setTargetSpeed(targetSpeed: number): void { this.targetSpeed = targetSpeed; }
  getMaxSpeed(): number { return this.maxSpeed; }
  setMaxSpeed(maxSpeed: number): void { this.maxSpeed = maxSpeed; }

  setFire(firepower: number): boolean {
    if (this.gunHeat > 0 || firepower < 0.1 || firepower > 3) return false;
    this.firepower = firepower;
    return true;
  }
  getFirepower(): number { return this.firepower; }
  setRescan(): void { /* full impl in task 4 */ }
  setFireAssist(_enable: boolean): void { /* full impl in task 4 */ }
  setInterruptible(interruptible: boolean): void {
    this.eventQueue.setCurrentEventInterruptible(interruptible);
  }

  setAdjustGunForBodyTurn(adjust: boolean): void { this.adjustGunForBodyTurn = adjust; }
  isAdjustGunForBodyTurn(): boolean { return this.adjustGunForBodyTurn; }
  setAdjustRadarForBodyTurn(adjust: boolean): void { this.adjustRadarForBodyTurn = adjust; }
  isAdjustRadarForBodyTurn(): boolean { return this.adjustRadarForBodyTurn; }
  setAdjustRadarForGunTurn(adjust: boolean): void { this.adjustRadarForGunTurn = adjust; }
  isAdjustRadarForGunTurn(): boolean { return this.adjustRadarForGunTurn; }

  addCustomEvent(condition: Condition): boolean {
    this.eventQueue.addCondition(condition);
    return true;
  }
  removeCustomEvent(condition: Condition): boolean {
    this.eventQueue.removeCondition(condition);
    return true;
  }

  setStop(_overwrite?: boolean): void { this.stopped = true; }
  setResume(): void { this.stopped = false; }
  isStopped(): boolean { return this.stopped; }

  getTeammateIds(): ReadonlySet<number> { return this.teammateIds; }
  isTeammate(botId: number): boolean { return this.teammateIds.has(botId); }
  broadcastTeamMessage(_message: unknown): void { /* full impl in task 4 */ }
  sendTeamMessage(_teammateId: number, _message: unknown): void { /* full impl in task 4 */ }

  getBodyColor(): Color | null { return this.bodyColor; }
  setBodyColor(color: Color | null): void { this.bodyColor = color; }
  getTurretColor(): Color | null { return this.turretColor; }
  setTurretColor(color: Color | null): void { this.turretColor = color; }
  getRadarColor(): Color | null { return this.radarColor; }
  setRadarColor(color: Color | null): void { this.radarColor = color; }
  getBulletColor(): Color | null { return this.bulletColor; }
  setBulletColor(color: Color | null): void { this.bulletColor = color; }
  getScanColor(): Color | null { return this.scanColor; }
  setScanColor(color: Color | null): void { this.scanColor = color; }
  getTracksColor(): Color | null { return this.tracksColor; }
  setTracksColor(color: Color | null): void { this.tracksColor = color; }
  getGunColor(): Color | null { return this.gunColor; }
  setGunColor(color: Color | null): void { this.gunColor = color; }

  isRunning(): boolean { return this.running; }
  setRunning(running: boolean): void { this.running = running; }

  isDebuggingEnabled(): boolean { return this.debuggingEnabled; }

  getGraphics(): IGraphics {
    throw new Error("BaseBotInternals.getGraphics() not yet implemented (task 4)");
  }

  getEventPriority(eventType: string): number {
    return this.eventPriorities.getPriority(eventType);
  }
  setEventPriority(eventType: string, priority: number): void {
    this.eventPriorities.setPriority(eventType, priority);
  }
}
