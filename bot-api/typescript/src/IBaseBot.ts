import { Color } from "./graphics/Color.js";
import { IGraphics } from "./graphics/IGraphics.js";
import { BulletState } from "./BulletState.js";
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
/**
 * Interface containing the core API for a bot.
 */
export interface IBaseBot {
  /** The maximum size of a team message in bytes (32 KB). */
  readonly TEAM_MESSAGE_MAX_SIZE: 32768;

  /** The maximum number of team messages that can be sent per turn. */
  readonly MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN: 10;

  /** Starts the bot, connecting to the server and running until the game ends. */
  start(): void;

  /**
   * Commits the current commands (actions), finalizing the current turn for the bot.
   * Must be called once per turn before the turn timeout occurs.
   */
  go(): void;

  /** Unique id of this bot, available when the game has started. */
  getMyId(): number;

  /** The game variant, which is "Tank Royale". */
  getVariant(): string;

  /** Game version, e.g. "1.0.0". */
  getVersion(): string;

  /** Game type, e.g. "melee" or "1v1". First available when the game has started. */
  getGameType(): string;

  /** Width of the arena measured in units. First available when the game has started. */
  getArenaWidth(): number;

  /** Height of the arena measured in units. First available when the game has started. */
  getArenaHeight(): number;

  /** The number of rounds in a battle. First available when the game has started. */
  getNumberOfRounds(): number;

  /** Gun cooling rate. First available when the game has started. */
  getGunCoolingRate(): number;

  /** The maximum number of inactive turns allowed before the bot is zapped. */
  getMaxInactivityTurns(): number;

  /** The turn timeout in microseconds. First available when the game has started. */
  getTurnTimeout(): number;

  /** The number of microseconds left of this turn before the bot will skip the turn. */
  getTimeLeft(): number;

  /** The current round number. */
  getRoundNumber(): number;

  /** The current turn number. */
  getTurnNumber(): number;

  /** The number of enemy bots left in the round. */
  getEnemyCount(): number;

  /** The energy level of the bot. */
  getEnergy(): number;

  /** Whether the bot is disabled, i.e. has no energy. */
  isDisabled(): boolean;

  /** The X coordinate of the bot. */
  getX(): number;

  /** The Y coordinate of the bot. */
  getY(): number;

  /** The driving direction of the bot in degrees. */
  getDirection(): number;

  /** The gun direction of the bot in degrees. */
  getGunDirection(): number;

  /** The radar direction of the bot in degrees. */
  getRadarDirection(): number;

  /** The speed of the bot measured in units per turn. */
  getSpeed(): number;

  /** The gun heat of the bot. The gun cannot fire when the gun heat is > 0. */
  getGunHeat(): number;

  /** The current bullet states. */
  getBulletStates(): ReadonlySet<BulletState>;

  /** The events that occurred in the current turn. */
  getEvents(): BotEvent[];

  /** Clears the events for the current turn. */
  clearEvents(): void;

  /** The current turn rate of the bot in degrees per turn. */
  getTurnRate(): number;

  /** Sets the turn rate of the bot in degrees per turn. */
  setTurnRate(turnRate: number): void;

  /** The maximum turn rate of the bot in degrees per turn. */
  getMaxTurnRate(): number;

  /** Sets the maximum turn rate of the bot in degrees per turn. */
  setMaxTurnRate(maxTurnRate: number): void;

  /** The current gun turn rate in degrees per turn. */
  getGunTurnRate(): number;

  /** Sets the gun turn rate in degrees per turn. */
  setGunTurnRate(gunTurnRate: number): void;

  /** The maximum gun turn rate in degrees per turn. */
  getMaxGunTurnRate(): number;

  /** Sets the maximum gun turn rate in degrees per turn. */
  setMaxGunTurnRate(maxGunTurnRate: number): void;

  /** The current radar turn rate in degrees per turn. */
  getRadarTurnRate(): number;

  /** Sets the radar turn rate in degrees per turn. */
  setRadarTurnRate(radarTurnRate: number): void;

  /** The maximum radar turn rate in degrees per turn. */
  getMaxRadarTurnRate(): number;

  /** Sets the maximum radar turn rate in degrees per turn. */
  setMaxRadarTurnRate(maxRadarTurnRate: number): void;

  /** The target speed in units per turn. */
  getTargetSpeed(): number;

  /** Sets the target speed in units per turn. */
  setTargetSpeed(targetSpeed: number): void;

  /** The maximum speed in units per turn. */
  getMaxSpeed(): number;

  /** Sets the maximum speed in units per turn. */
  setMaxSpeed(maxSpeed: number): void;

  /**
   * Sets the gun to fire with the given firepower. Returns true if the gun will fire, false if not.
   * The gun will not fire if the gun is still hot, or the firepower is out of range.
   */
  setFire(firepower: number): boolean;

  /** The firepower of the last setFire() call. */
  getFirepower(): number;

  /** Sets the bot to rescan with the radar. */
  setRescan(): void;

  /** Enables or disables fire assistance. */
  setFireAssist(enable: boolean): void;

  /** Sets whether the current event handler is interruptible. */
  setInterruptible(interruptible: boolean): void;

  /** Sets whether the gun should adjust for the body turn. */
  setAdjustGunForBodyTurn(adjust: boolean): void;

  /** Returns whether the gun is set to adjust for the body turn. */
  isAdjustGunForBodyTurn(): boolean;

  /** Sets whether the radar should adjust for the body turn. */
  setAdjustRadarForBodyTurn(adjust: boolean): void;

  /** Returns whether the radar is set to adjust for the body turn. */
  isAdjustRadarForBodyTurn(): boolean;

  /** Sets whether the radar should adjust for the gun turn. */
  setAdjustRadarForGunTurn(adjust: boolean): void;

  /** Returns whether the radar is set to adjust for the gun turn. */
  isAdjustRadarForGunTurn(): boolean;

  /** Adds a custom condition that triggers a CustomEvent when met. Returns true if added. */
  addCustomEvent(condition: Condition): boolean;

  /** Removes a custom condition. Returns true if removed. */
  removeCustomEvent(condition: Condition): boolean;

  /** Sets the bot to stop all movement. */
  setStop(): void;

  /** Sets the bot to stop all movement, optionally overwriting a previous stop. */
  setStop(overwrite: boolean): void;

  /** Sets the bot to resume movement after a stop. */
  setResume(): void;

  /** Returns whether the bot is stopped. */
  isStopped(): boolean;

  /** Returns the set of teammate bot IDs. */
  getTeammateIds(): ReadonlySet<number>;

  /** Returns whether the given bot ID is a teammate. */
  isTeammate(botId: number): boolean;

  /** Broadcasts a message to all teammates. */
  broadcastTeamMessage(message: unknown): void;

  /** Sends a message to a specific teammate. */
  sendTeamMessage(teammateId: number, message: unknown): void;

  /** The body color of the bot. */
  getBodyColor(): Color | null;

  /** Sets the body color of the bot. */
  setBodyColor(color: Color | null): void;

  /** The turret color of the bot. */
  getTurretColor(): Color | null;

  /** Sets the turret color of the bot. */
  setTurretColor(color: Color | null): void;

  /** The radar color of the bot. */
  getRadarColor(): Color | null;

  /** Sets the radar color of the bot. */
  setRadarColor(color: Color | null): void;

  /** The bullet color of the bot. */
  getBulletColor(): Color | null;

  /** Sets the bullet color of the bot. */
  setBulletColor(color: Color | null): void;

  /** The scan arc color of the bot. */
  getScanColor(): Color | null;

  /** Sets the scan arc color of the bot. */
  setScanColor(color: Color | null): void;

  /** The tracks color of the bot. */
  getTracksColor(): Color | null;

  /** Sets the tracks color of the bot. */
  setTracksColor(color: Color | null): void;

  /** The gun color of the bot. */
  getGunColor(): Color | null;

  /** Sets the gun color of the bot. */
  setGunColor(color: Color | null): void;

  /** Calculates the maximum turn rate for a given speed. */
  calcMaxTurnRate(speed: number): number;

  /** Calculates the bullet speed for a given firepower. */
  calcBulletSpeed(firepower: number): number;

  /** Calculates the gun heat for a given firepower. */
  calcGunHeat(firepower: number): number;

  /** Gets the event priority for a given event type name. */
  getEventPriority(eventType: string): number;

  /** Sets the event priority for a given event type name. */
  setEventPriority(eventType: string, priority: number): void;

  /** Returns whether debugging is enabled. */
  isDebuggingEnabled(): boolean;

  /** Returns the graphics object for drawing on the canvas. */
  getGraphics(): IGraphics;

  // ---------------------------------------------------------------------------
  // Default event handler methods (no-op by default)
  // ---------------------------------------------------------------------------

  onConnected(event: ConnectedEvent): void;
  onDisconnected(event: DisconnectedEvent): void;
  onConnectionError(event: ConnectionErrorEvent): void;
  onGameStarted(event: GameStartedEvent): void;
  onGameEnded(event: GameEndedEvent): void;
  onRoundStarted(event: RoundStartedEvent): void;
  onRoundEnded(event: RoundEndedEvent): void;
  onTick(event: TickEvent): void;
  onBotDeath(event: BotDeathEvent): void;
  onDeath(event: DeathEvent): void;
  onHitBot(event: HitBotEvent): void;
  onHitWall(event: HitWallEvent): void;
  onBulletFired(event: BulletFiredEvent): void;
  onHitByBullet(event: HitByBulletEvent): void;
  onBulletHitBot(event: BulletHitBotEvent): void;
  onBulletHitBullet(event: BulletHitBulletEvent): void;
  onBulletHitWall(event: BulletHitWallEvent): void;
  onScannedBot(event: ScannedBotEvent): void;
  onSkippedTurn(event: SkippedTurnEvent): void;
  onWonRound(event: WonRoundEvent): void;
  onCustomEvent(event: CustomEvent): void;
  onTeamMessage(event: TeamMessageEvent): void;

  // ---------------------------------------------------------------------------
  // Default calculation methods
  // ---------------------------------------------------------------------------

  /**
   * Calculates the bearing (angle) from the bot's direction to the given direction.
   * @param direction the direction to calculate the bearing to.
   * @returns the bearing in degrees in the range [-180, 180].
   */
  calcBearing(direction: number): number;

  /**
   * Calculates the bearing from the gun's direction to the given direction.
   * @param direction the direction to calculate the bearing to.
   * @returns the bearing in degrees in the range [-180, 180].
   */
  calcGunBearing(direction: number): number;

  /**
   * Calculates the bearing from the radar's direction to the given direction.
   * @param direction the direction to calculate the bearing to.
   * @returns the bearing in degrees in the range [-180, 180].
   */
  calcRadarBearing(direction: number): number;

  /**
   * Calculates the direction from the bot to the given coordinates.
   * @param x the X coordinate.
   * @param y the Y coordinate.
   * @returns the direction in degrees in the range [0, 360).
   */
  directionTo(x: number, y: number): number;

  /**
   * Calculates the bearing from the bot's direction to the given coordinates.
   * @param x the X coordinate.
   * @param y the Y coordinate.
   * @returns the bearing in degrees in the range [-180, 180].
   */
  bearingTo(x: number, y: number): number;

  /**
   * Calculates the bearing from the gun's direction to the given coordinates.
   * @param x the X coordinate.
   * @param y the Y coordinate.
   * @returns the bearing in degrees in the range [-180, 180].
   */
  gunBearingTo(x: number, y: number): number;

  /**
   * Calculates the bearing from the radar's direction to the given coordinates.
   * @param x the X coordinate.
   * @param y the Y coordinate.
   * @returns the bearing in degrees in the range [-180, 180].
   */
  radarBearingTo(x: number, y: number): number;

  /**
   * Calculates the distance from the bot to the given coordinates.
   * @param x the X coordinate.
   * @param y the Y coordinate.
   * @returns the distance in units.
   */
  distanceTo(x: number, y: number): number;

  /**
   * Normalizes an angle to an absolute angle in the range [0, 360).
   * @param angle the angle to normalize.
   * @returns the normalized angle.
   */
  normalizeAbsoluteAngle(angle: number): number;

  /**
   * Normalizes an angle to a relative angle in the range [-180, 180].
   * @param angle the angle to normalize.
   * @returns the normalized angle.
   */
  normalizeRelativeAngle(angle: number): number;

  /**
   * Calculates the delta angle between a target angle and a source angle.
   * @param targetAngle the target angle.
   * @param sourceAngle the source angle.
   * @returns the delta angle in degrees in the range [-180, 180].
   */
  calcDeltaAngle(targetAngle: number, sourceAngle: number): number;
}
