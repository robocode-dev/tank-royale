import { IBaseBot } from "./IBaseBot.js";
import { BotInfo } from "./BotInfo.js";
import { BulletState } from "./BulletState.js";
import { Color } from "./graphics/Color.js";
import { IGraphics } from "./graphics/IGraphics.js";
import { BotEvent } from "./events/BotEvent.js";
import { Condition } from "./events/Condition.js";
import { ConnectedEvent } from "./events/ConnectedEvent.js";
import { DisconnectedEvent } from "./events/DisconnectedEvent.js";
import { ConnectionErrorEvent } from "./events/ConnectionErrorEvent.js";
import { GameStartedEvent } from "./events/GameStartedEvent.js";
import { GameEndedEvent } from "./events/GameEndedEvent.js";
import { RoundStartedEvent } from "./events/RoundStartedEvent.js";
import { RoundEndedEvent } from "./events/RoundEndedEvent.js";
import { TickEvent } from "./events/TickEvent.js";
import { BotDeathEvent } from "./events/BotDeathEvent.js";
import { DeathEvent } from "./events/DeathEvent.js";
import { HitBotEvent } from "./events/HitBotEvent.js";
import { HitWallEvent } from "./events/HitWallEvent.js";
import { BulletFiredEvent } from "./events/BulletFiredEvent.js";
import { HitByBulletEvent } from "./events/HitByBulletEvent.js";
import { BulletHitBotEvent } from "./events/BulletHitBotEvent.js";
import { BulletHitBulletEvent } from "./events/BulletHitBulletEvent.js";
import { BulletHitWallEvent } from "./events/BulletHitWallEvent.js";
import { ScannedBotEvent } from "./events/ScannedBotEvent.js";
import { SkippedTurnEvent } from "./events/SkippedTurnEvent.js";
import { WonRoundEvent } from "./events/WonRoundEvent.js";
import { CustomEvent } from "./events/CustomEvent.js";
import { TeamMessageEvent } from "./events/TeamMessageEvent.js";
import { BaseBotInternals } from "./internal/BaseBotInternals.js";
import { EnvVars } from "./EnvVars.js";
import { detectRuntime } from "./runtime/index.js";
import { Constants } from "./Constants.js";
import { MathUtil } from "./util/MathUtil.js";

/**
 * Abstract base class for bots implementing the IBaseBot interface.
 * Provides all state accessors, rate setters, color management, and math utilities.
 * Subclasses should override the event handler methods to implement bot behavior.
 */
export abstract class BaseBot implements IBaseBot {
  readonly TEAM_MESSAGE_MAX_SIZE = Constants.TEAM_MESSAGE_MAX_SIZE;
  readonly MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN = Constants.MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN;

  /** @internal */
  readonly _internals: BaseBotInternals;

  /**
   * Creates a new BaseBot with auto-detected configuration from environment variables.
   */
  constructor();
  /**
   * Creates a new BaseBot with the given bot info.
   * @param botInfo the bot info.
   */
  constructor(botInfo: BotInfo);
  /**
   * Creates a new BaseBot with the given bot info and server URL.
   * @param botInfo the bot info.
   * @param serverUrl the server URL.
   */
  constructor(botInfo: BotInfo, serverUrl: string);
  /**
   * Creates a new BaseBot with the given bot info, server URL, and server secret.
   * @param botInfo the bot info.
   * @param serverUrl the server URL.
   * @param serverSecret the server secret.
   */
  constructor(botInfo: BotInfo, serverUrl: string, serverSecret: string);
  constructor(botInfo?: BotInfo, serverUrl?: string, serverSecret?: string) {
    const info = botInfo ?? BaseBot._loadBotInfo(new.target.name);
    this._internals = new BaseBotInternals(
      this,
      info,
      serverUrl ?? null,
      serverSecret,
    );
  }

  private static _loadBotInfo(className: string): BotInfo {
    const runtime = detectRuntime();
    // Try <ClassName>.json in the current directory first (matches Python/Java behaviour)
    const text = runtime.readFile(`./${className}.json`);
    if (text) {
      try {
        return BotInfo.fromJson(text);
      } catch {
        // Fall through to env vars
      }
    }
    return new EnvVars(runtime).getBotInfo();
  }

  start(): void {
    this._internals.start();
  }

  go(): void {
    const currentTick = this._internals.getCurrentTickOrNull();
    const capturedTurnNumber = currentTick?.turnNumber ?? -1;
    if (currentTick != null) {
      this._internals.dispatchEvents(capturedTurnNumber);
    }
    // Pass captured turn number to execute() so it uses the same tick we dispatched events for.
    // Without this, execute() re-reads the live tickEvent which may have been updated by the
    // message handler between dispatchEvents() and execute(), causing skipped turns.
    this._internals.execute(capturedTurnNumber);
  }

  getMyId(): number { return this._internals.getMyId(); }
  getVariant(): string { return this._internals.getVariant(); }
  getVersion(): string { return this._internals.getVersion(); }
  getGameType(): string { return this._internals.getGameType(); }
  getArenaWidth(): number { return this._internals.getArenaWidth(); }
  getArenaHeight(): number { return this._internals.getArenaHeight(); }
  getNumberOfRounds(): number { return this._internals.getNumberOfRounds(); }
  getGunCoolingRate(): number { return this._internals.getGunCoolingRate(); }
  getMaxInactivityTurns(): number { return this._internals.getMaxInactivityTurns(); }
  getTurnTimeout(): number { return this._internals.getTurnTimeout(); }
  getTimeLeft(): number { return this._internals.getTimeLeft(); }
  getRoundNumber(): number { return this._internals.getRoundNumber(); }
  getTurnNumber(): number { return this._internals.getTurnNumber(); }
  getEnemyCount(): number { return this._internals.getEnemyCount(); }
  getEnergy(): number { return this._internals.getEnergy(); }
  isDisabled(): boolean { return this._internals.isDisabled(); }
  getX(): number { return this._internals.getX(); }
  getY(): number { return this._internals.getY(); }
  getDirection(): number { return this._internals.getDirection(); }
  getGunDirection(): number { return this._internals.getGunDirection(); }
  getRadarDirection(): number { return this._internals.getRadarDirection(); }
  getSpeed(): number { return this._internals.getSpeed(); }
  getGunHeat(): number { return this._internals.getGunHeat(); }
  getBulletStates(): ReadonlySet<BulletState> { return this._internals.getBulletStates(); }
  getEvents(): BotEvent[] { return [...this._internals.getEvents()]; }
  clearEvents(): void { this._internals.clearEvents(); }

  getTurnRate(): number { return this._internals.getTurnRate(); }
  setTurnRate(turnRate: number): void { this._internals.setTurnRate(turnRate); }
  getMaxTurnRate(): number { return this._internals.getMaxTurnRate(); }
  setMaxTurnRate(maxTurnRate: number): void { this._internals.setMaxTurnRate(maxTurnRate); }
  getGunTurnRate(): number { return this._internals.getGunTurnRate(); }
  setGunTurnRate(gunTurnRate: number): void { this._internals.setGunTurnRate(gunTurnRate); }
  getMaxGunTurnRate(): number { return this._internals.getMaxGunTurnRate(); }
  setMaxGunTurnRate(maxGunTurnRate: number): void { this._internals.setMaxGunTurnRate(maxGunTurnRate); }
  getRadarTurnRate(): number { return this._internals.getRadarTurnRate(); }
  setRadarTurnRate(radarTurnRate: number): void { this._internals.setRadarTurnRate(radarTurnRate); }
  getMaxRadarTurnRate(): number { return this._internals.getMaxRadarTurnRate(); }
  setMaxRadarTurnRate(maxRadarTurnRate: number): void { this._internals.setMaxRadarTurnRate(maxRadarTurnRate); }
  getTargetSpeed(): number { return this._internals.getTargetSpeed(); }
  setTargetSpeed(targetSpeed: number): void { this._internals.setTargetSpeed(targetSpeed); }
  getMaxSpeed(): number { return this._internals.getMaxSpeed(); }
  setMaxSpeed(maxSpeed: number): void { this._internals.setMaxSpeed(maxSpeed); }

  setFire(firepower: number): boolean { return this._internals.setFire(firepower); }
  getFirepower(): number { return this._internals.getFirepower(); }
  setRescan(): void { this._internals.setRescan(); }
  setFireAssist(enable: boolean): void { this._internals.setFireAssist(enable); }
  setInterruptible(interruptible: boolean): void { this._internals.setInterruptible(interruptible); }

  setAdjustGunForBodyTurn(adjust: boolean): void { this._internals.setAdjustGunForBodyTurn(adjust); }
  isAdjustGunForBodyTurn(): boolean { return this._internals.isAdjustGunForBodyTurn(); }
  setAdjustRadarForBodyTurn(adjust: boolean): void { this._internals.setAdjustRadarForBodyTurn(adjust); }
  isAdjustRadarForBodyTurn(): boolean { return this._internals.isAdjustRadarForBodyTurn(); }
  setAdjustRadarForGunTurn(adjust: boolean): void { this._internals.setAdjustRadarForGunTurn(adjust); }
  isAdjustRadarForGunTurn(): boolean { return this._internals.isAdjustRadarForGunTurn(); }

  addCustomEvent(condition: Condition): boolean { return this._internals.addCustomEvent(condition); }
  removeCustomEvent(condition: Condition): boolean { return this._internals.removeCustomEvent(condition); }

  setStop(): void;
  setStop(overwrite: boolean): void;
  setStop(overwrite?: boolean): void { this._internals.setStop(overwrite); }
  setResume(): void { this._internals.setResume(); }
  isStopped(): boolean { return this._internals.isStopped_(); }

  getTeammateIds(): ReadonlySet<number> { return this._internals.getTeammateIds(); }
  isTeammate(botId: number): boolean { return this._internals.isTeammate(botId); }
  broadcastTeamMessage(message: unknown): void { this._internals.broadcastTeamMessage(message); }
  sendTeamMessage(teammateId: number, message: unknown): void { this._internals.sendTeamMessage(teammateId, message); }

  getBodyColor(): Color | null { return this._internals.getBodyColor(); }
  setBodyColor(color: Color | null): void { this._internals.setBodyColor(color); }
  getTurretColor(): Color | null { return this._internals.getTurretColor(); }
  setTurretColor(color: Color | null): void { this._internals.setTurretColor(color); }
  getRadarColor(): Color | null { return this._internals.getRadarColor(); }
  setRadarColor(color: Color | null): void { this._internals.setRadarColor(color); }
  getBulletColor(): Color | null { return this._internals.getBulletColor(); }
  setBulletColor(color: Color | null): void { this._internals.setBulletColor(color); }
  getScanColor(): Color | null { return this._internals.getScanColor(); }
  setScanColor(color: Color | null): void { this._internals.setScanColor(color); }
  getTracksColor(): Color | null { return this._internals.getTracksColor(); }
  setTracksColor(color: Color | null): void { this._internals.setTracksColor(color); }
  getGunColor(): Color | null { return this._internals.getGunColor(); }
  setGunColor(color: Color | null): void { this._internals.setGunColor(color); }

  calcMaxTurnRate(speed: number): number {
    return Constants.MAX_TURN_RATE - 0.75 * Math.abs(MathUtil.clamp(speed, -Constants.MAX_SPEED, Constants.MAX_SPEED));
  }

  calcBulletSpeed(firepower: number): number {
    return 20 - 3 * MathUtil.clamp(firepower, Constants.MIN_FIREPOWER, Constants.MAX_FIREPOWER);
  }

  calcGunHeat(firepower: number): number {
    return 1 + MathUtil.clamp(firepower, Constants.MIN_FIREPOWER, Constants.MAX_FIREPOWER) / 5;
  }

  getEventPriority(eventType: string): number { return this._internals.getEventPriority(eventType); }
  setEventPriority(eventType: string, priority: number): void { this._internals.setEventPriority(eventType, priority); }

  isDebuggingEnabled(): boolean { return this._internals.isDebuggingEnabled(); }
  getGraphics(): IGraphics { return this._internals.getGraphics(); }

  // ---------------------------------------------------------------------------
  // Overridable event handler methods (no-op by default)
  // ---------------------------------------------------------------------------

  onConnected(_event: ConnectedEvent): void { /* override to handle */ }
  onDisconnected(_event: DisconnectedEvent): void { /* override to handle */ }
  onConnectionError(_event: ConnectionErrorEvent): void { /* override to handle */ }
  onGameStarted(_event: GameStartedEvent): void { /* override to handle */ }
  onGameEnded(_event: GameEndedEvent): void { /* override to handle */ }
  onRoundStarted(_event: RoundStartedEvent): void { /* override to handle */ }
  onRoundEnded(_event: RoundEndedEvent): void { /* override to handle */ }
  onTick(_event: TickEvent): void { /* override to handle */ }
  onBotDeath(_event: BotDeathEvent): void { /* override to handle */ }
  onDeath(_event: DeathEvent): void { /* override to handle */ }
  onHitBot(_event: HitBotEvent): void { /* override to handle */ }
  onHitWall(_event: HitWallEvent): void { /* override to handle */ }
  onBulletFired(_event: BulletFiredEvent): void { /* override to handle */ }
  onHitByBullet(_event: HitByBulletEvent): void { /* override to handle */ }
  onBulletHitBot(_event: BulletHitBotEvent): void { /* override to handle */ }
  onBulletHitBullet(_event: BulletHitBulletEvent): void { /* override to handle */ }
  onBulletHitWall(_event: BulletHitWallEvent): void { /* override to handle */ }
  onScannedBot(_event: ScannedBotEvent): void { /* override to handle */ }
  onSkippedTurn(_event: SkippedTurnEvent): void { /* override to handle */ }
  onWonRound(_event: WonRoundEvent): void { /* override to handle */ }
  onCustomEvent(_event: CustomEvent): void { /* override to handle */ }
  onTeamMessage(_event: TeamMessageEvent): void { /* override to handle */ }

  // ---------------------------------------------------------------------------
  // Calculation methods
  // ---------------------------------------------------------------------------

  calcBearing(direction: number): number {
    return this.normalizeRelativeAngle(direction - this.getDirection());
  }

  calcGunBearing(direction: number): number {
    return this.normalizeRelativeAngle(direction - this.getGunDirection());
  }

  calcRadarBearing(direction: number): number {
    return this.normalizeRelativeAngle(direction - this.getRadarDirection());
  }

  directionTo(x: number, y: number): number {
    return this.normalizeAbsoluteAngle(
      (Math.atan2(y - this.getY(), x - this.getX()) * 180) / Math.PI,
    );
  }

  bearingTo(x: number, y: number): number {
    return this.normalizeRelativeAngle(this.directionTo(x, y) - this.getDirection());
  }

  gunBearingTo(x: number, y: number): number {
    return this.normalizeRelativeAngle(this.directionTo(x, y) - this.getGunDirection());
  }

  radarBearingTo(x: number, y: number): number {
    return this.normalizeRelativeAngle(this.directionTo(x, y) - this.getRadarDirection());
  }

  distanceTo(x: number, y: number): number {
    const dx = x - this.getX();
    const dy = y - this.getY();
    return Math.sqrt(dx * dx + dy * dy);
  }

  normalizeAbsoluteAngle(angle: number): number {
    return ((angle % 360) + 360) % 360;
  }

  normalizeRelativeAngle(angle: number): number {
    let a = ((angle % 360) + 360) % 360;
    if (a > 180) a -= 360;
    return a;
  }

  calcDeltaAngle(targetAngle: number, sourceAngle: number): number {
    let angle = targetAngle - sourceAngle;
    angle += angle > 180 ? -360 : angle < -180 ? 360 : 0;
    return angle;
  }
}
