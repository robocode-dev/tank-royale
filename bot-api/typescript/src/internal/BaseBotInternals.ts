import { BotInfo } from "../BotInfo.js";
import { BulletState } from "../BulletState.js";
import { Color } from "../graphics/Color.js";
import { IGraphics } from "../graphics/IGraphics.js";
import { SvgGraphics } from "../graphics/SvgGraphics.js";
import { BotEvent } from "../events/BotEvent.js";
import { Condition } from "../events/Condition.js";
import { EventQueue } from "../events/EventQueue.js";
import { EventPriorities } from "../events/EventPriorities.js";
import { EventInterruption } from "../events/EventInterruption.js";
import { BotEventHandlers } from "../events/BotEventHandlers.js";
import { TickEvent } from "../events/TickEvent.js";
import { RoundStartedEvent as RoundStartedEventClass } from "../events/RoundStartedEvent.js";
import { BulletFiredEvent } from "../events/BulletFiredEvent.js";
import { SkippedTurnEvent } from "../events/SkippedTurnEvent.js";
import { ConnectedEvent } from "../events/ConnectedEvent.js";
import { DisconnectedEvent } from "../events/DisconnectedEvent.js";
import { ConnectionErrorEvent } from "../events/ConnectionErrorEvent.js";
import { GameStartedEvent } from "../events/GameStartedEvent.js";
import { GameEndedEvent } from "../events/GameEndedEvent.js";
import { RoundEndedEvent } from "../events/RoundEndedEvent.js";
import { ResultsMapper } from "../mapper/ResultsMapper.js";
import { InternalEventHandlers } from "./InternalEventHandlers.js";
import { BotStoppedException } from "./BotStoppedException.js";
import { SAB_SLOT_STOP, SAB_SLOT_TURN, SAB_LENGTH } from "./SharedBufferLayout.js";
import { WebSocketHandler } from "../WebSocketHandler.js";
import { EnvVars } from "../EnvVars.js";
import { ColorUtil } from "../util/ColorUtil.js";
import { MathUtil } from "../util/MathUtil.js";
import { detectRuntime } from "../runtime/index.js";
import { GameSetup } from "../GameSetup.js";
import { GameSetupMapper } from "../mapper/GameSetupMapper.js";
import { InitialPosition } from "../InitialPosition.js";
import { EventMapper } from "../mapper/EventMapper.js";
import { toJson } from "../json/JsonUtil.js";
import type { BotIntent as SchemaBotIntent } from "../protocol/schema.js";
import { MessageType } from "../protocol/MessageType.js";
import type { IBaseBot } from "../IBaseBot.js";
import type { IBot } from "../IBot.js";

/** Callback interface for stop/resume lifecycle events */
export interface IStopResumeListener {
  onStop(): void;
  onResume(): void;
}

const DEFAULT_SERVER_URL = "ws://localhost:7654";
const MAX_SPEED = 8;
const MAX_TURN_RATE = 10;
const MAX_GUN_TURN_RATE = 20;
const MAX_RADAR_TURN_RATE = 45;
const DECELERATION = -2;

/**
 * Full internal implementation for BaseBot.
 * Manages WebSocket connection, event dispatching, bot intent, and worker thread synchronization.
 */
export class BaseBotInternals {
  readonly botInfo: BotInfo;
  readonly serverUrl: string;
  readonly serverSecret: string | undefined;
  readonly eventQueue: EventQueue;
  readonly botEventHandlers: BotEventHandlers;
  readonly eventPriorities: EventPriorities;
  readonly eventInterruption: EventInterruption;
  readonly internalEventHandlers: InternalEventHandlers;

  private readonly baseBot: IBaseBot;
  private readonly envVars: EnvVars;
  private wsHandler: WebSocketHandler | null = null;

  // Game state
  private myId = 0;
  private variant = "Tank Royale";
  private version = "";
  private gameSetup: GameSetup | null = null;
  private tickEvent: TickEvent | null = null;
  private tickStartTime = 0;
  private teammateIds: Set<number> = new Set();

  // Bot intent (mutable, sent each turn)
  private intent: SchemaBotIntent = { type: MessageType.BotIntent };

  // Rate/speed limits
  private maxSpeed = MAX_SPEED;
  private maxTurnRate = MAX_TURN_RATE;
  private maxGunTurnRate = MAX_GUN_TURN_RATE;
  private maxRadarTurnRate = MAX_RADAR_TURN_RATE;

  // Stop/resume saved state
  private isStopped = false;
  private stopResumeListener: IStopResumeListener | null = null;
  private savedTargetSpeed: number | null = null;
  private savedTurnRate: number | null = null;
  private savedGunTurnRate: number | null = null;
  private savedRadarTurnRate: number | null = null;

  // Thread/worker state
  private running = false;
  private worker: Worker | null = null;
  private sharedBuffer: SharedArrayBuffer | null = null;
  private sharedView: Int32Array | null = null;
  private eventHandlingDisabledTurn = 0;
  private lastExecuteTurnNumber = -1;
  private movementResetPending = false;

  // Graphics
  private svgGraphics: SvgGraphics | null = null;

  constructor(baseBot: IBaseBot, botInfo: BotInfo, serverUrl: string | null, serverSecret: string | undefined) {
    this.baseBot = baseBot;
    this.botInfo = botInfo;
    this.serverUrl = serverUrl ?? DEFAULT_SERVER_URL;
    this.serverSecret = serverSecret;
    this.envVars = new EnvVars(detectRuntime());
    this.eventPriorities = new EventPriorities();
    this.eventInterruption = new EventInterruption();
    this.eventQueue = new EventQueue(this.eventPriorities, this.eventInterruption);
    this.botEventHandlers = new BotEventHandlers();
    this.internalEventHandlers = new InternalEventHandlers();
    this.subscribeToEvents();
  }

  private subscribeToEvents(): void {
    this.internalEventHandlers.onRoundStarted.subscribe((e) => this.onRoundStarted(e), 100);
    this.internalEventHandlers.onNextTurn.subscribe((e) => this.onNextTurn(e.tickEvent), 100);
    this.internalEventHandlers.onBulletFired.subscribe(() => {
      this.intent.firepower = 0; // stop firing continuously
    }, 100);
  }

  private onRoundStarted(_e: InstanceType<typeof RoundStartedEventClass>): void {
    this.eventQueue.clear();
    this.isStopped = false;
    this.eventHandlingDisabledTurn = 0;
    this.lastExecuteTurnNumber = -1;
    this.movementResetPending = true;
  }

  private onNextTurn(_e: TickEvent): void {
    // Signal the worker thread that a new turn has arrived
    if (this.sharedView != null) {
      Atomics.add(this.sharedView, SAB_SLOT_TURN, 1);
      Atomics.notify(this.sharedView, SAB_SLOT_TURN);
    }
  }

  // ---------------------------------------------------------------------------
  // start() / connect()
  // ---------------------------------------------------------------------------

  start(): void {
    this.connect();
    // Block until WebSocket closes (Node.js: use Atomics.wait on a close latch)
    const closeLatch = new Int32Array(new SharedArrayBuffer(4));
    this._closeLatch = closeLatch;
    Atomics.wait(closeLatch, 0, 0); // blocks until set to non-zero
  }

  /** @internal used by WebSocket close handler */
  _closeLatch: Int32Array | null = null;

  private connect(): void {
    const adapter = detectRuntime();
    this.wsHandler = new WebSocketHandler(
      adapter,
      this.serverUrl,
      this.serverSecret,
      this.botInfo,
      this.envVars,
      {
        onConnected: () => this.handleConnected(),
        onDisconnected: (remote, code, reason) => this.handleDisconnected(remote, code, reason),
        onConnectionError: (err) => this.handleConnectionError(err),
        onServerHandshake: () => { /* handshake reply handled inside WebSocketHandler */ },
        onGameStarted: (msg) => this.handleGameStarted(msg),
        onGameEnded: (msg) => this.handleGameEnded(msg),
        onGameAborted: () => this.handleGameAborted(),
        onRoundStarted: (msg) => this.handleRoundStarted(msg),
        onRoundEnded: (msg) => this.handleRoundEnded(msg),
        onTick: (msg) => this.handleTick(msg),
        onSkippedTurn: (turnNumber) => this.handleSkippedTurn(turnNumber),
      },
    );
    this.wsHandler.connect();
  }

  // ---------------------------------------------------------------------------
  // WebSocket event handlers
  // ---------------------------------------------------------------------------

  private handleConnected(): void {
    const e = new ConnectedEvent(this.serverUrl);
    this.botEventHandlers.onConnected.publish(e);
  }

  private handleDisconnected(remote: boolean, code?: number, reason?: string): void {
    const e = new DisconnectedEvent(this.serverUrl, remote, code, reason);
    this.botEventHandlers.onDisconnected.publish(e);
    this.internalEventHandlers.onDisconnected.publish(e);
    this.stopThread();
    // Unblock start()
    if (this._closeLatch != null) {
      Atomics.store(this._closeLatch, 0, 1);
      Atomics.notify(this._closeLatch, 0);
    }
  }

  private handleConnectionError(err: unknown): void {
    const e = new ConnectionErrorEvent(this.serverUrl, err instanceof Error ? err : new Error(String(err)));
    this.botEventHandlers.onConnectionError.publish(e);
  }

  private handleGameStarted(msg: import("../protocol/schema.js").GameStartedEventForBot): void {
    this.myId = msg.myId;
    this.gameSetup = GameSetupMapper.map(msg.gameSetup);
    this.teammateIds = new Set(msg.teammateIds ?? []);
    const initialPosition = new InitialPosition(msg.startX ?? null, msg.startY ?? null, msg.startDirection ?? null);
    const e = new GameStartedEvent(msg.myId, initialPosition, this.gameSetup);
    this.botEventHandlers.onGameStarted.publish(e);
  }

  private handleGameEnded(msg: import("../protocol/schema.js").GameEndedEventForBot): void {
    this.stopThread();
    const results = ResultsMapper.map(msg.results);
    const e = new GameEndedEvent(msg.numberOfRounds, results);
    this.botEventHandlers.onGameEnded.publish(e);
    this.internalEventHandlers.onGameEnded.publish(e);
  }

  private handleGameAborted(): void {
    this.stopThread();
    this.internalEventHandlers.fireGameAborted();
  }

  private handleRoundStarted(msg: import("../protocol/schema.js").RoundStartedEvent): void {
    const e = new RoundStartedEventClass(msg.roundNumber);
    this.botEventHandlers.onRoundStarted.publish(e);
    this.internalEventHandlers.onRoundStarted.publish(e);
  }

  private handleRoundEnded(msg: import("../protocol/schema.js").RoundEndedEventForBot): void {
    this.stopThread();
    const results = ResultsMapper.map(msg.results);
    const e = new RoundEndedEvent(msg.roundNumber, msg.turnNumber, results);
    this.botEventHandlers.onRoundEnded.publish(e);
    this.internalEventHandlers.onRoundEnded.publish(e);
  }

  private handleTick(msg: import("../protocol/schema.js").TickEventForBot): void {
    this.tickStartTime = Date.now();
    const tick = EventMapper.map(msg, this.myId);
    this.tickEvent = tick;
    this.addEventsFromTick(tick);
    this.enableEventHandling(true);
    this.dispatchEvents(tick.turnNumber);
    this.internalEventHandlers.fireEvent(tick);
  }

  private handleSkippedTurn(turnNumber: number): void {
    const e = new SkippedTurnEvent(turnNumber);
    this.addEvent(e);
  }

  // ---------------------------------------------------------------------------
  // execute() / sendIntent() / waitForNextTurn()
  // ---------------------------------------------------------------------------

  execute(): void {
    if (this.tickEvent == null) {
      this.sendIntent();
      return;
    }
    const turnNumber = this.tickEvent.turnNumber;
    if (turnNumber !== this.lastExecuteTurnNumber) {
      this.lastExecuteTurnNumber = turnNumber;
      this.sendIntent();
      if (this.movementResetPending) {
        this.resetMovement();
        this.movementResetPending = false;
      }
    }
    this.waitForNextTurn(turnNumber);
  }

  private sendIntent(): void {
    if (this.wsHandler == null) return;
    const intent = { ...this.intent };
    this.wsHandler.sendBotIntent(intent as import("../protocol/schema.js").BotIntent);
    this.intent.teamMessages = null;
  }

  private waitForNextTurn(turnNumber: number): void {
    this.stopRogueThread();
    if (this.sharedView == null) return;
    // Spin-wait: block until turn signal changes or stop flag is set
    while (
      this.running &&
      this.tickEvent != null &&
      this.tickEvent.turnNumber === turnNumber
    ) {
      const result = Atomics.wait(this.sharedView, SAB_SLOT_TURN, Atomics.load(this.sharedView, SAB_SLOT_TURN), 100);
      if (result === "ok" || result === "not-equal") break;
      // "timed-out" — loop again to check running flag
    }
    this.stopRogueThread();
  }

  private stopRogueThread(): void {
    if (this.sharedView != null && Atomics.load(this.sharedView, SAB_SLOT_STOP) === 1) {
      throw new BotStoppedException();
    }
  }

  private resetMovement(): void {
    this.intent.turnRate = null;
    this.intent.gunTurnRate = null;
    this.intent.radarTurnRate = null;
    this.intent.targetSpeed = null;
    this.intent.firepower = null;
  }

  // ---------------------------------------------------------------------------
  // Worker thread management
  // ---------------------------------------------------------------------------

  startThread(bot: IBot): void {
    this.enableEventHandling(true);
    this.sharedBuffer = new SharedArrayBuffer(SAB_LENGTH * 4);
    this.sharedView = new Int32Array(this.sharedBuffer);
    Atomics.store(this.sharedView, SAB_SLOT_STOP, 0);
    Atomics.store(this.sharedView, SAB_SLOT_TURN, 0);

    // In Node.js, use worker_threads; in browser, use Worker
    const runtime = detectRuntime();
    try {
      // Try Node.js worker_threads
      const { Worker: NodeWorker } = require("worker_threads");
      const workerCode = `
        const { workerData, parentPort } = require('worker_threads');
        const { sharedBuffer } = workerData;
        const view = new Int32Array(sharedBuffer);
        parentPort.on('message', (msg) => {
          if (msg.type === 'start') {
            // The actual bot run loop is driven from the main thread via postMessage
            // Worker just signals readiness
            parentPort.postMessage({ type: 'ready' });
          }
        });
      `;
      // We run the bot loop on the main thread for simplicity in Node.js
      // (true worker_threads blocking is complex; use async loop instead)
      this.runBotLoop(bot);
    } catch {
      // Browser: use Web Worker
      this.runBotLoop(bot);
    }
  }

  private runBotLoop(bot: IBot): void {
    this.setRunning(true);
    // Run asynchronously to not block the main thread
    Promise.resolve().then(async () => {
      try {
        bot.run();
      } catch (e) {
        if (!(e instanceof BotStoppedException)) {
          // ignore
        }
      }
      this.dispatchFinalTurnEvents();
      while (this.running) {
        try {
          bot.go();
        } catch (e) {
          if (e instanceof BotStoppedException) break;
        }
      }
      this.dispatchFinalTurnEvents();
    });
  }

  stopThread(): void {
    if (!this.running) return;
    this.setRunning(false);
    this.enableEventHandling(false);
    if (this.sharedView != null) {
      Atomics.store(this.sharedView, SAB_SLOT_STOP, 1);
      Atomics.notify(this.sharedView, SAB_SLOT_TURN);
    }
    this.worker = null;
  }

  enableEventHandling(enable: boolean): void {
    if (enable) {
      this.eventHandlingDisabledTurn = 0;
    } else if (this.tickEvent != null) {
      this.eventHandlingDisabledTurn = this.tickEvent.turnNumber;
    }
  }

  getEventHandlingDisabled(): boolean {
    if (this.eventHandlingDisabledTurn === 0) return false;
    if (this.tickEvent == null) return false;
    return this.eventHandlingDisabledTurn < this.tickEvent.turnNumber - 1;
  }

  setStopResumeListener(listener: IStopResumeListener): void {
    this.stopResumeListener = listener;
  }

  // ---------------------------------------------------------------------------
  // Event management
  // ---------------------------------------------------------------------------

  addEventsFromTick(tick: TickEvent): void {
    this.eventQueue.addEventsFromTick(tick);
  }

  addEvent(event: BotEvent): void {
    this.eventQueue.addEvent(event);
  }

  dispatchEvents(turnNumber: number): void {
    this.eventQueue.dispatchEvents(turnNumber, this.botEventHandlers);
  }

  dispatchFinalTurnEvents(): void {
    if (this.tickEvent != null) {
      this.dispatchEvents(this.tickEvent.turnNumber);
    }
  }

  // ---------------------------------------------------------------------------
  // State accessors
  // ---------------------------------------------------------------------------

  getMyId(): number { return this.myId; }

  getVariant(): string {
    return this.wsHandler?.getServerHandshake()?.variant ?? this.variant;
  }

  getVersion(): string {
    return this.wsHandler?.getServerHandshake()?.version ?? this.version;
  }

  getGameType(): string { return this.gameSetup?.gameType ?? ""; }
  getArenaWidth(): number { return this.gameSetup?.arenaWidth ?? 0; }
  getArenaHeight(): number { return this.gameSetup?.arenaHeight ?? 0; }
  getNumberOfRounds(): number { return this.gameSetup?.numberOfRounds ?? 0; }
  getGunCoolingRate(): number { return this.gameSetup?.gunCoolingRate ?? 0; }
  getMaxInactivityTurns(): number { return this.gameSetup?.maxInactivityTurns ?? 0; }
  getTurnTimeout(): number { return this.gameSetup?.turnTimeout ?? 0; }

  getTimeLeft(): number {
    if (this.tickEvent == null) return 0;
    const elapsed = Date.now() - this.tickStartTime;
    return Math.max(0, this.getTurnTimeout() - elapsed);
  }

  getRoundNumber(): number { return this.tickEvent?.roundNumber ?? 0; }
  getTurnNumber(): number { return this.tickEvent?.turnNumber ?? 0; }
  getEnemyCount(): number { return this.tickEvent?.botState.enemyCount ?? 0; }
  getEnergy(): number { return this.tickEvent?.botState.energy ?? 0; }
  isDisabled(): boolean { return this.tickEvent != null && this.getEnergy() === 0; }
  getX(): number { return this.tickEvent?.botState.x ?? 0; }
  getY(): number { return this.tickEvent?.botState.y ?? 0; }
  getDirection(): number { return this.tickEvent?.botState.direction ?? 0; }
  getGunDirection(): number { return this.tickEvent?.botState.gunDirection ?? 0; }
  getRadarDirection(): number { return this.tickEvent?.botState.radarDirection ?? 0; }
  getSpeed(): number { return this.tickEvent?.botState.speed ?? 0; }
  getGunHeat(): number { return this.tickEvent?.botState.gunHeat ?? 0; }
  getBulletStates(): ReadonlySet<BulletState> { return new Set(this.tickEvent?.bulletStates ?? []); }
  getEvents(): readonly BotEvent[] { return this.eventQueue.getEvents(); }
  clearEvents(): void { this.eventQueue.clear(); }

  // ---------------------------------------------------------------------------
  // Rate / speed management
  // ---------------------------------------------------------------------------

  getTurnRate(): number { return this.intent.turnRate ?? 0; }
  setTurnRate(turnRate: number): void {
    this.intent.turnRate = MathUtil.clamp(turnRate, -this.maxTurnRate, this.maxTurnRate);
  }
  getMaxTurnRate(): number { return this.maxTurnRate; }
  setMaxTurnRate(maxTurnRate: number): void { this.maxTurnRate = maxTurnRate; }

  getGunTurnRate(): number { return this.intent.gunTurnRate ?? 0; }
  setGunTurnRate(gunTurnRate: number): void {
    this.intent.gunTurnRate = MathUtil.clamp(gunTurnRate, -this.maxGunTurnRate, this.maxGunTurnRate);
  }
  getMaxGunTurnRate(): number { return this.maxGunTurnRate; }
  setMaxGunTurnRate(maxGunTurnRate: number): void { this.maxGunTurnRate = maxGunTurnRate; }

  getRadarTurnRate(): number { return this.intent.radarTurnRate ?? 0; }
  setRadarTurnRate(radarTurnRate: number): void {
    this.intent.radarTurnRate = MathUtil.clamp(radarTurnRate, -this.maxRadarTurnRate, this.maxRadarTurnRate);
  }
  getMaxRadarTurnRate(): number { return this.maxRadarTurnRate; }
  setMaxRadarTurnRate(maxRadarTurnRate: number): void { this.maxRadarTurnRate = maxRadarTurnRate; }

  getTargetSpeed(): number { return this.intent.targetSpeed ?? 0; }
  setTargetSpeed(targetSpeed: number): void {
    this.intent.targetSpeed = MathUtil.clamp(targetSpeed, -this.maxSpeed, this.maxSpeed);
  }
  getMaxSpeed(): number { return this.maxSpeed; }
  setMaxSpeed(maxSpeed: number): void { this.maxSpeed = maxSpeed; }

  getNewTargetSpeed(speed: number, distance: number): number {
    if (distance < 0) return -this.getNewTargetSpeed(-speed, -distance);
    const targetSpeed = distance === 0 ? 0 : (distance > 0 ? this.maxSpeed : -this.maxSpeed);
    if (speed >= 0) {
      const maxSpeedForDist = this.getMaxSpeedForDistance(distance);
      return MathUtil.clamp(targetSpeed, speed + DECELERATION, Math.min(maxSpeedForDist, speed + 1));
    } else {
      return MathUtil.clamp(targetSpeed, speed - 1, speed - DECELERATION);
    }
  }

  private getMaxSpeedForDistance(distance: number): number {
    const absDecel = Math.abs(DECELERATION);
    const sqrt = Math.sqrt(absDecel * (absDecel + 2 * distance) + 1) - 1;
    return Math.min(this.maxSpeed, sqrt / 2 + absDecel / 2);
  }

  getDistanceTraveledUntilStop(speed: number): number {
    let distance = 0;
    while (Math.abs(speed) > 0.001) {
      speed = this.getNewTargetSpeed(speed, 0);
      distance += speed;
    }
    return Math.abs(distance);
  }

  // ---------------------------------------------------------------------------
  // Fire
  // ---------------------------------------------------------------------------

  setFire(firepower: number): boolean {
    if ((this.tickEvent?.botState.gunHeat ?? 0) > 0 || firepower < 0.1 || firepower > 3) return false;
    this.intent.firepower = firepower;
    return true;
  }
  getFirepower(): number { return this.intent.firepower ?? 0; }

  // ---------------------------------------------------------------------------
  // Adjustment flags
  // ---------------------------------------------------------------------------

  setAdjustGunForBodyTurn(adjust: boolean): void { this.intent.adjustGunForBodyTurn = adjust; }
  isAdjustGunForBodyTurn(): boolean { return this.intent.adjustGunForBodyTurn ?? false; }
  setAdjustRadarForBodyTurn(adjust: boolean): void { this.intent.adjustRadarForBodyTurn = adjust; }
  isAdjustRadarForBodyTurn(): boolean { return this.intent.adjustRadarForBodyTurn ?? false; }
  setAdjustRadarForGunTurn(adjust: boolean): void { this.intent.adjustRadarForGunTurn = adjust; }
  isAdjustRadarForGunTurn(): boolean { return this.intent.adjustRadarForGunTurn ?? false; }

  // ---------------------------------------------------------------------------
  // Stop / resume
  // ---------------------------------------------------------------------------

  setStop(overwrite?: boolean): void {
    if (!this.isStopped || overwrite) {
      this.isStopped = true;
      this.savedTargetSpeed = this.intent.targetSpeed ?? null;
      this.savedTurnRate = this.intent.turnRate ?? null;
      this.savedGunTurnRate = this.intent.gunTurnRate ?? null;
      this.savedRadarTurnRate = this.intent.radarTurnRate ?? null;
      this.intent.targetSpeed = 0;
      this.intent.turnRate = 0;
      this.intent.gunTurnRate = 0;
      this.intent.radarTurnRate = 0;
      this.stopResumeListener?.onStop();
    }
  }

  setResume(): void {
    if (this.isStopped) {
      this.isStopped = false;
      this.intent.targetSpeed = this.savedTargetSpeed;
      this.intent.turnRate = this.savedTurnRate;
      this.intent.gunTurnRate = this.savedGunTurnRate;
      this.intent.radarTurnRate = this.savedRadarTurnRate;
      this.stopResumeListener?.onResume();
    }
  }

  isStopped_(): boolean { return this.isStopped; }

  // ---------------------------------------------------------------------------
  // Scan / fire assist / interruptible
  // ---------------------------------------------------------------------------

  setRescan(): void { this.intent.rescan = true; }
  setFireAssist(enable: boolean): void { this.intent.fireAssist = enable; }
  setInterruptible(interruptible: boolean): void {
    this.eventQueue.setCurrentEventInterruptible(interruptible);
  }

  // ---------------------------------------------------------------------------
  // Custom events
  // ---------------------------------------------------------------------------

  addCustomEvent(condition: Condition): boolean {
    this.eventQueue.addCondition(condition);
    return true;
  }
  removeCustomEvent(condition: Condition): boolean {
    this.eventQueue.removeCondition(condition);
    return true;
  }

  // ---------------------------------------------------------------------------
  // Team
  // ---------------------------------------------------------------------------

  getTeammateIds(): ReadonlySet<number> { return this.teammateIds; }
  isTeammate(botId: number): boolean { return this.teammateIds.has(botId); }

  broadcastTeamMessage(message: unknown): void {
    this.sendTeamMessage(undefined, message);
  }

  sendTeamMessage(teammateId: number | undefined, message: unknown): void {
    const json = toJson(message);
    if (json.length > 32768) return; // TEAM_MESSAGE_MAX_SIZE
    if (!this.intent.teamMessages) this.intent.teamMessages = [];
    if (this.intent.teamMessages.length >= 10) return; // MAX_NUMBER_OF_TEAM_MESSAGES_PER_TURN
    this.intent.teamMessages.push({
      message: json,
      messageType: typeof message === "object" && message !== null ? message.constructor.name : "string",
      receiverId: teammateId ?? null,
    });
  }

  // ---------------------------------------------------------------------------
  // Colors
  // ---------------------------------------------------------------------------

  getBodyColor(): Color | null { return this.intent.bodyColor ? ColorUtil.fromHexColor(this.intent.bodyColor) : null; }
  setBodyColor(color: Color | null): void { this.intent.bodyColor = color ? "#" + ColorUtil.toHex(color) : null; }
  getTurretColor(): Color | null { return this.intent.turretColor ? ColorUtil.fromHexColor(this.intent.turretColor) : null; }
  setTurretColor(color: Color | null): void { this.intent.turretColor = color ? "#" + ColorUtil.toHex(color) : null; }
  getRadarColor(): Color | null { return this.intent.radarColor ? ColorUtil.fromHexColor(this.intent.radarColor) : null; }
  setRadarColor(color: Color | null): void { this.intent.radarColor = color ? "#" + ColorUtil.toHex(color) : null; }
  getBulletColor(): Color | null { return this.intent.bulletColor ? ColorUtil.fromHexColor(this.intent.bulletColor) : null; }
  setBulletColor(color: Color | null): void { this.intent.bulletColor = color ? "#" + ColorUtil.toHex(color) : null; }
  getScanColor(): Color | null { return this.intent.scanColor ? ColorUtil.fromHexColor(this.intent.scanColor) : null; }
  setScanColor(color: Color | null): void { this.intent.scanColor = color ? "#" + ColorUtil.toHex(color) : null; }
  getTracksColor(): Color | null { return this.intent.tracksColor ? ColorUtil.fromHexColor(this.intent.tracksColor) : null; }
  setTracksColor(color: Color | null): void { this.intent.tracksColor = color ? "#" + ColorUtil.toHex(color) : null; }
  getGunColor(): Color | null { return this.intent.gunColor ? ColorUtil.fromHexColor(this.intent.gunColor) : null; }
  setGunColor(color: Color | null): void { this.intent.gunColor = color ? "#" + ColorUtil.toHex(color) : null; }

  // ---------------------------------------------------------------------------
  // Running state
  // ---------------------------------------------------------------------------

  isRunning(): boolean { return this.running; }
  setRunning(running: boolean): void { this.running = running; }

  // ---------------------------------------------------------------------------
  // Graphics
  // ---------------------------------------------------------------------------

  isDebuggingEnabled(): boolean { return this.tickEvent?.botState.isDebuggingEnabled ?? false; }

  getGraphics(): IGraphics {
    if (this.svgGraphics == null) {
      this.svgGraphics = new SvgGraphics();
    }
    return this.svgGraphics;
  }

  // ---------------------------------------------------------------------------
  // Event priority
  // ---------------------------------------------------------------------------

  getEventPriority(eventType: string): number {
    return this.eventPriorities.getPriority(eventType);
  }
  setEventPriority(eventType: string, priority: number): void {
    this.eventPriorities.setPriority(eventType, priority);
  }
}
