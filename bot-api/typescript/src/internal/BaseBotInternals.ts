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
import { WonRoundEvent } from "../events/WonRoundEvent.js";
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
import { initNodeRuntime } from "../runtime/NodeRuntimeAdapter.js";
import { GameSetup } from "../GameSetup.js";
import { GameSetupMapper } from "../mapper/GameSetupMapper.js";
import { InitialPosition } from "../InitialPosition.js";
import { EventMapper } from "../mapper/EventMapper.js";
import { toJson } from "../json/JsonUtil.js";
import { IntentValidator } from "./intentValidator.js";
import { Constants } from "../Constants.js";
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

/**
 * Full internal implementation for BaseBot.
 * Manages WebSocket connection, event dispatching, bot intent, and worker thread synchronization.
 *
 * Architecture (ADR-0028):
 * - Main thread: WebSocket only. Forwards all server messages to Worker via postMessage/Atomics.notify.
 * - Worker thread: Runs bot.run() synchronously. Uses Atomics.wait() in go() to block between turns.
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
  private maxSpeed: number = Constants.MAX_SPEED;
  private maxTurnRate: number = Constants.MAX_TURN_RATE;
  private maxGunTurnRate: number = Constants.MAX_GUN_TURN_RATE;
  private maxRadarTurnRate: number = Constants.MAX_RADAR_TURN_RATE;

  // Stop/resume saved state
  private isStopped = false;
  private stopResumeListener: IStopResumeListener | null = null;
  private savedTargetSpeed: number | null = null;
  private savedTurnRate: number | null = null;
  private savedGunTurnRate: number | null = null;
  private savedRadarTurnRate: number | null = null;

  // Thread/worker state
  private running = false;
  private eventHandlingDisabledTurn = 0;
  private lastExecuteTurnNumber = -1;
  private movementResetPending = false;

  // Worker architecture fields
  private workerMode = false;          // true when running inside the Worker thread
  private keepRunningGame = true;      // Worker bootstrap loop exit flag
  private sharedBuffer: SharedArrayBuffer | null = null;
  private sharedView: Int32Array | null = null;

  // Main thread side: port to talk to Worker
  private mainPort: any | null = null;

  // Worker thread side: port to talk to main thread
  private workerPort: any | null = null;
  private workerParentPort: { postMessage(msg: any): void } | null = null;
  private workerReceiveMessageOnPort: ((port: any) => { message: any } | undefined) | null = null;

  // Graphics
  private svgGraphics: SvgGraphics | null = null;

  constructor(baseBot: IBaseBot, botInfo: BotInfo, serverUrl: string | null, serverSecret: string | undefined) {
    this.baseBot = baseBot;
    this.botInfo = botInfo;
    this.envVars = new EnvVars(detectRuntime());
    this.serverUrl = serverUrl ?? this.envVars.getServerUrl() ?? DEFAULT_SERVER_URL;
    this.serverSecret = serverSecret ?? this.envVars.getServerSecret();
    this.eventPriorities = new EventPriorities();
    this.eventInterruption = new EventInterruption();
    this.eventQueue = new EventQueue(this.eventPriorities, this.eventInterruption);
    this.botEventHandlers = new BotEventHandlers();
    this.internalEventHandlers = new InternalEventHandlers();
    this.subscribeToEvents();
  }

  private subscribeToEvents(): void {
    // Internal lifecycle subscriptions
    this.internalEventHandlers.onRoundStarted.subscribe((e) => this.onRoundStarted(e), 100);
    this.internalEventHandlers.onNextTurn.subscribe((e) => this.onNextTurn(e.tickEvent), 100);
    this.internalEventHandlers.onBulletFired.subscribe(() => {
      this.intent.firepower = 0; // stop firing continuously
    }, 100);

    // Wire botEventHandlers to baseBot user-overridable methods.
    // Internal handlers (distanceRemaining reset, stopThread) fire at priority 90 (before user code).
    const beh = this.botEventHandlers;
    const ih = this.internalEventHandlers;
    const bot = this.baseBot;

    beh.onConnected.subscribe((e) => bot.onConnected?.(e));
    beh.onDisconnected.subscribe((e) => bot.onDisconnected?.(e));
    beh.onConnectionError.subscribe((e) => bot.onConnectionError?.(e));
    beh.onGameStarted.subscribe((e) => bot.onGameStarted?.(e));
    beh.onGameEnded.subscribe((e) => bot.onGameEnded?.(e));
    beh.onRoundStarted.subscribe((e) => bot.onRoundStarted?.(e));
    beh.onRoundEnded.subscribe((e) => bot.onRoundEnded?.(e));
    beh.onTick.subscribe((e) => bot.onTick?.(e));
    beh.onBotDeath.subscribe((e) => bot.onBotDeath?.(e));
    beh.onSkippedTurn.subscribe((e) => bot.onSkippedTurn?.(e));
    beh.onWonRound.subscribe((e) => bot.onWonRound?.(e));
    beh.onCustomEvent.subscribe((e) => bot.onCustomEvent?.(e));
    beh.onTeamMessage.subscribe((e) => bot.onTeamMessage?.(e));
    beh.onHitByBullet.subscribe((e) => bot.onHitByBullet?.(e));
    beh.onBulletHitBot.subscribe((e) => bot.onBulletHitBot?.(e));
    beh.onBulletHitBullet.subscribe((e) => bot.onBulletHitBullet?.(e));
    beh.onBulletHitWall.subscribe((e) => bot.onBulletHitWall?.(e));
    beh.onScannedBot.subscribe((e) => bot.onScannedBot?.(e));

    // Events that also fire internal handlers (internal at priority 90, user at default 0)
    beh.onDeath.subscribe((e) => ih.onDeath.publish(e), 90);
    beh.onDeath.subscribe((e) => bot.onDeath?.(e));

    beh.onHitWall.subscribe((e) => ih.onHitWall.publish(e), 90);
    beh.onHitWall.subscribe((e) => bot.onHitWall?.(e));

    beh.onHitBot.subscribe((e) => ih.onHitBot.publish(e), 90);
    beh.onHitBot.subscribe((e) => bot.onHitBot?.(e));

    beh.onBulletFired.subscribe((e) => ih.onBulletFired.publish(e), 90);
    beh.onBulletFired.subscribe((e) => bot.onBulletFired?.(e));
  }

  private onRoundStarted(_e: InstanceType<typeof RoundStartedEventClass>): void {
    this.tickEvent = null;
    this.eventQueue.clear();
    this.isStopped = false;
    this.eventHandlingDisabledTurn = 0;
    this.lastExecuteTurnNumber = -1;
    this.movementResetPending = true;
  }

  private onNextTurn(_e: TickEvent): void {
    // In worker mode: main thread already called Atomics.notify via forwardToWorker.
    // In main thread (no Worker, legacy path): notify the shared view if present.
    if (!this.workerMode && this.sharedView != null) {
      Atomics.add(this.sharedView, SAB_SLOT_TURN, 1);
      Atomics.notify(this.sharedView, SAB_SLOT_TURN);
    }
  }

  // ---------------------------------------------------------------------------
  // start() — detects main vs worker mode and dispatches
  // ---------------------------------------------------------------------------

  start(): void {
    this._startAsync().catch((err) => {
      console.error("[BOT-API] Failed to start bot:", err);
    });
  }

  private async _startAsync(): Promise<void> {
    await initNodeRuntime();
    const wt = await this._importWorkerThreads();
    if (wt != null && wt.workerData?.isWorker === true) {
      // We are inside the spawned Worker
      this.startAsWorker(wt);
    } else {
      // We are on the main thread — spawn a Worker
      this.startAsMain(wt);
    }
  }

  private startAsMain(wt: any | null): void {
    if (wt == null) {
      // No worker_threads available (browser or very old Node) — fall back to legacy connect
      this.connect();
      return;
    }

    // Create shared buffer for synchronization
    this.sharedBuffer = new SharedArrayBuffer(SAB_LENGTH * 4);
    this.sharedView = new Int32Array(this.sharedBuffer);

    // Create a MessageChannel — port1 stays on main, port2 goes to Worker
    const { port1, port2 } = new wt.MessageChannel();
    this.mainPort = port1;

    // Listen on port1 for intent messages from Worker
    port1.on("message", (msg: any) => {
      if (msg.type === "intent" && this.wsHandler != null) {
        this.wsHandler.sendBotIntent(msg.intent);
      }
    });

    // Spawn the Worker (re-running the same script with workerData.isWorker=true)
    const worker = new wt.Worker(process.argv[1], {
      workerData: {
        isWorker: true,
        sharedBuffer: this.sharedBuffer,
        port: port2,
      },
      transferList: [port2],
    });

    worker.on("error", (err: Error) => {
      console.error("[BOT-API] Worker error:", err);
    });

    // Connect WebSocket on main thread
    this.connect();
  }

  private startAsWorker(wt: any): void {
    this.workerMode = true;
    this.sharedBuffer = wt.workerData.sharedBuffer;
    this.sharedView = new Int32Array(this.sharedBuffer!);
    this.workerPort = wt.workerData.port;
    this.workerParentPort = wt.workerData.port;
    this.workerReceiveMessageOnPort = wt.receiveMessageOnPort;

    // The Worker doesn't run WebSocket — it receives forwarded messages from main.
    // Bootstrap loop: wait for game messages and process them.
    this.bootstrapWorker();
  }

  // ---------------------------------------------------------------------------
  // connect() — sets up WebSocket (main thread only)
  // ---------------------------------------------------------------------------

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
  // Main thread message forwarding to Worker
  // ---------------------------------------------------------------------------

  private forwardToWorker(type: string, data?: any): void {
    if (this.mainPort != null) {
      this.mainPort.postMessage({ type, data });
    }
    // Increment turn counter and notify Worker
    if (this.sharedView != null) {
      Atomics.add(this.sharedView, SAB_SLOT_TURN, 1);
      Atomics.notify(this.sharedView, SAB_SLOT_TURN);
    }
  }

  // ---------------------------------------------------------------------------
  // WebSocket event handlers
  // ---------------------------------------------------------------------------

  private handleConnected(): void {
    if (this.workerMode) {
      // Worker mode: process directly (shouldn't happen — connected fires on main)
      const e = new ConnectedEvent(this.serverUrl);
      this.botEventHandlers.onConnected.publish(e);
    } else if (this.mainPort != null) {
      // Forward to Worker
      this.forwardToWorker("connected", { serverUrl: this.serverUrl });
    } else {
      // No Worker, handle directly
      const e = new ConnectedEvent(this.serverUrl);
      this.botEventHandlers.onConnected.publish(e);
    }
  }

  private handleDisconnected(remote: boolean, code?: number, reason?: string): void {
    if (this.mainPort != null) {
      this.forwardToWorker("disconnected", { serverUrl: this.serverUrl, remote, code, reason });
    } else {
      const e = new DisconnectedEvent(this.serverUrl, remote, code, reason);
      this.botEventHandlers.onDisconnected.publish(e);
      this.internalEventHandlers.onDisconnected.publish(e);
    }
    this.stopThread();
  }

  private handleConnectionError(err: unknown): void {
    if (this.mainPort != null) {
      this.forwardToWorker("connectionError", {
        serverUrl: this.serverUrl,
        message: err instanceof Error ? err.message : String(err),
      });
    } else {
      const e = new ConnectionErrorEvent(this.serverUrl, err instanceof Error ? err : new Error(String(err)));
      this.botEventHandlers.onConnectionError.publish(e);
    }
  }

  private handleGameStarted(msg: import("../protocol/schema.js").GameStartedEventForBot): void {
    if (this.mainPort != null) {
      this.forwardToWorker("gameStarted", msg);
    } else {
      this.processGameStarted(msg);
    }
  }

  private handleGameEnded(msg: import("../protocol/schema.js").GameEndedEventForBot): void {
    this.stopThread();
    if (this.mainPort != null) {
      this.forwardToWorker("gameEnded", msg);
    } else {
      this.processGameEnded(msg);
    }
  }

  private handleGameAborted(): void {
    this.stopThread();
    if (this.mainPort != null) {
      this.forwardToWorker("gameAborted");
    } else {
      this.internalEventHandlers.fireGameAborted();
    }
  }

  private handleRoundStarted(msg: import("../protocol/schema.js").RoundStartedEvent): void {
    if (this.mainPort != null) {
      this.forwardToWorker("roundStarted", msg);
    } else {
      this.processRoundStarted(msg);
    }
  }

  private handleRoundEnded(msg: import("../protocol/schema.js").RoundEndedEventForBot): void {
    this.stopThread();
    if (this.mainPort != null) {
      this.forwardToWorker("roundEnded", msg);
    } else {
      this.processRoundEnded(msg);
    }
  }

  private handleTick(msg: import("../protocol/schema.js").TickEventForBot): void {
    if (this.mainPort != null) {
      this.forwardToWorker("tick", msg);
    } else {
      this.processTick(msg);
    }
  }

  private handleSkippedTurn(turnNumber: number): void {
    if (this.mainPort != null) {
      this.forwardToWorker("skippedTurn", { turnNumber });
    } else {
      const e = new SkippedTurnEvent(turnNumber);
      this.addEvent(e);
    }
  }

  // ---------------------------------------------------------------------------
  // Worker message processing
  // ---------------------------------------------------------------------------

  private drainWorkerMessages(): void {
    if (this.workerReceiveMessageOnPort == null || this.workerPort == null) return;
    let msgWrapper: { message: any } | undefined;
    while ((msgWrapper = this.workerReceiveMessageOnPort(this.workerPort)) != null) {
      this.processWorkerMessage(msgWrapper.message);
    }
  }

  private processWorkerMessage(msg: any): void {
    switch (msg.type) {
      case "connected":
        {
          const e = new ConnectedEvent(msg.data.serverUrl);
          this.botEventHandlers.onConnected.publish(e);
        }
        break;
      case "disconnected":
        {
          const e = new DisconnectedEvent(msg.data.serverUrl, msg.data.remote, msg.data.code, msg.data.reason);
          this.botEventHandlers.onDisconnected.publish(e);
          this.internalEventHandlers.onDisconnected.publish(e);
        }
        break;
      case "connectionError":
        {
          const e = new ConnectionErrorEvent(msg.data.serverUrl, new Error(msg.data.message));
          this.botEventHandlers.onConnectionError.publish(e);
        }
        break;
      case "gameStarted":
        this.processGameStarted(msg.data);
        break;
      case "gameEnded":
        this.keepRunningGame = false;
        this.processGameEnded(msg.data);
        break;
      case "gameAborted":
        this.keepRunningGame = false;
        this.internalEventHandlers.fireGameAborted();
        break;
      case "roundStarted":
        this.processRoundStarted(msg.data);
        break;
      case "roundEnded":
        this.processRoundEnded(msg.data);
        break;
      case "tick":
        this.processTick(msg.data);
        break;
      case "skippedTurn":
        {
          const e = new SkippedTurnEvent(msg.data.turnNumber);
          this.addEvent(e);
        }
        break;
    }
  }

  // ---------------------------------------------------------------------------
  // Shared processing logic (used by both main and worker paths)
  // ---------------------------------------------------------------------------

  private processGameStarted(msg: import("../protocol/schema.js").GameStartedEventForBot): void {
    this.myId = msg.myId;
    this.gameSetup = GameSetupMapper.map(msg.gameSetup);
    this.teammateIds = new Set(msg.teammateIds ?? []);
    const initialPosition = new InitialPosition(msg.startX ?? null, msg.startY ?? null, msg.startDirection ?? null);
    const e = new GameStartedEvent(msg.myId, initialPosition, this.gameSetup);
    this.botEventHandlers.onGameStarted.publish(e);
  }

  private processGameEnded(msg: import("../protocol/schema.js").GameEndedEventForBot): void {
    const results = ResultsMapper.map(msg.results);
    const e = new GameEndedEvent(msg.numberOfRounds, results);
    this.botEventHandlers.onGameEnded.publish(e);
    this.internalEventHandlers.onGameEnded.publish(e);
  }

  private processRoundStarted(msg: import("../protocol/schema.js").RoundStartedEvent): void {
    const e = new RoundStartedEventClass(msg.roundNumber);
    this.internalEventHandlers.onRoundStarted.publish(e);
    this.botEventHandlers.onRoundStarted.publish(e);
  }

  private processRoundEnded(msg: import("../protocol/schema.js").RoundEndedEventForBot): void {
    const results = ResultsMapper.map(msg.results);
    const e = new RoundEndedEvent(msg.roundNumber, msg.turnNumber, results);

    // Publish onRoundEnded and stop the bot thread first, so dispatchEvents runs uncontested
    this.botEventHandlers.onRoundEnded.publish(e);
    this.internalEventHandlers.onRoundEnded.publish(e); // triggers stopThread()

    // Flush any queued events from the last tick (e.g. WonRoundEvent) before the next
    // RoundStartedEvent clears the event queue.
    this.dispatchEvents(msg.turnNumber);

    // If the bot won this round (rank == 1), ensure onWonRound is triggered.
    if (results != null && results.rank === 1) {
      this.botEventHandlers.onWonRound.publish(new WonRoundEvent(msg.turnNumber));
    }
  }

  private processTick(msg: import("../protocol/schema.js").TickEventForBot): void {
    this.tickStartTime = Date.now();
    const tick = EventMapper.map(msg, this.myId);
    this.tickEvent = tick;
    this.addEventsFromTick(tick);
    this.enableEventHandling(true);
    // Internal handlers (processTurn, Atomics.notify) fire synchronously.
    this.internalEventHandlers.fireEvent(tick);
    // In the legacy (browser/no-worker) path the worker loop never runs, so we must dispatch
    // bot events — including onTick — directly from the WebSocket callback.
    if (!this.workerMode) {
      this.dispatchEvents(tick.turnNumber);
    }
  }

  // ---------------------------------------------------------------------------
  // Worker bootstrap loop
  // ---------------------------------------------------------------------------

  private bootstrapWorker(): void {
    while (this.keepRunningGame) {
      if (this.sharedView == null) break;
      const curVal = Atomics.load(this.sharedView, SAB_SLOT_TURN);
      const msgWrapper = this.workerReceiveMessageOnPort != null && this.workerPort != null
        ? this.workerReceiveMessageOnPort(this.workerPort)
        : undefined;
      if (msgWrapper != null) {
        this.processWorkerMessage(msgWrapper.message);
      } else {
        Atomics.wait(this.sharedView, SAB_SLOT_TURN, curVal, 10);
      }
    }
  }

  // ---------------------------------------------------------------------------
  // execute() / sendIntentToMain() / waitForNextTurnWorker()
  // ---------------------------------------------------------------------------

  /**
   * @param capturedTurnNumber the turn number captured by go() at the time events were dispatched,
   *                           or -1 if no tick was available
   */
  execute(capturedTurnNumber: number): void {
    if (!this.workerMode) {
      // Main thread (no Worker, legacy path): send intent directly
      if (capturedTurnNumber < 0) {
        this.sendIntentDirect();
        return;
      }
      if (capturedTurnNumber !== this.lastExecuteTurnNumber) {
        this.lastExecuteTurnNumber = capturedTurnNumber;
        this.sendIntentDirect();
        if (this.movementResetPending) {
          this.resetMovement();
          this.movementResetPending = false;
        }
      }
      return;
    }

    // Worker mode: send intent to main thread, then wait for next turn
    if (capturedTurnNumber < 0) {
      this.sendIntentToMain();
      return;
    }
    if (capturedTurnNumber !== this.lastExecuteTurnNumber) {
      this.lastExecuteTurnNumber = capturedTurnNumber;
      this.sendIntentToMain();
      if (this.movementResetPending) {
        this.resetMovement();
        this.movementResetPending = false;
      }
    }
    this.waitForNextTurnWorker(capturedTurnNumber);
  }

  private sendIntentDirect(): void {
    if (this.wsHandler == null) return;
    const intent: Record<string, unknown> = {};
    for (const [k, v] of Object.entries(this.intent)) {
      if (v != null) intent[k] = v;
    }
    this.wsHandler.sendBotIntent(intent as unknown as import("../protocol/schema.js").BotIntent);
    this.intent.teamMessages = null;
  }

  private sendIntentToMain(): void {
    if (this.workerParentPort == null) return;
    const intent: Record<string, unknown> = {};
    for (const [k, v] of Object.entries(this.intent)) {
      if (v != null) intent[k] = v;
    }
    this.workerParentPort.postMessage({ type: "intent", intent });
    this.intent.teamMessages = null;
  }

  private waitForNextTurnWorker(currentTurnNumber: number): void {
    this.stopRogueThread();
    while (this.running && (this.tickEvent?.turnNumber ?? 0) === currentTurnNumber) {
      this.stopRogueThread();
      if (this.sharedView == null) break;
      const curVal = Atomics.load(this.sharedView, SAB_SLOT_TURN);
      Atomics.wait(this.sharedView, SAB_SLOT_TURN, curVal, 500);
      this.drainWorkerMessages();
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
    if (!this.sharedView) {
      this.sharedBuffer = new SharedArrayBuffer(SAB_LENGTH * 4);
      this.sharedView = new Int32Array(this.sharedBuffer);
    }
    Atomics.store(this.sharedView, SAB_SLOT_STOP, 0);
    this.runBotLoop(bot);
  }

  private runBotLoop(bot: IBot): void {
    this.setRunning(true);
    try {
      this.waitUntilFirstTickArrived();
      bot.run();
    } catch (e) {
      if (!(e instanceof BotStoppedException)) {
        // ignore unexpected errors from bot.run()
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
  }

  isWorkerMode(): boolean { return this.workerMode; }

  // Blocks the pre-warmed bot thread until the first tick of the round arrives.
  // The thread is started at round-started (before any tick), so it must wait here
  // before run() can safely read bot state (radar direction, etc.).
  // Only applicable in worker mode — in legacy (non-worker) mode, returns immediately.
  private waitUntilFirstTickArrived(): void {
    if (!this.workerMode || this.sharedView == null) return;
    while (this.tickEvent == null && this.running) {
      const curVal = Atomics.load(this.sharedView, SAB_SLOT_TURN);
      Atomics.wait(this.sharedView, SAB_SLOT_TURN, curVal, 500);
      this.drainWorkerMessages();
      this.stopRogueThread();
    }
  }

  stopThread(): void {
    if (!this.running) return;
    this.setRunning(false);
    this.enableEventHandling(false);
    if (this.sharedView != null) {
      Atomics.store(this.sharedView, SAB_SLOT_STOP, 1);
      Atomics.notify(this.sharedView, SAB_SLOT_TURN);
    }
  }

  enableEventHandling(enable: boolean): void {
    if (enable) {
      this.eventHandlingDisabledTurn = 0;
    } else if (this.tickEvent != null) {
      this.eventHandlingDisabledTurn = this.tickEvent.turnNumber;
    }
  }

  isEventHandlingDisabled(): boolean {
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
  // Worker threads helper
  // ---------------------------------------------------------------------------

  private async _importWorkerThreads(): Promise<any | null> {
    try {
      return await import("node:worker_threads");
    } catch {
      return null;
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

  getCurrentTickOrNull(): TickEvent | null { return this.tickEvent; }
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
    this.intent.turnRate = IntentValidator.validateTurnRate(turnRate, this.maxTurnRate);
  }
  getMaxTurnRate(): number { return this.maxTurnRate; }
  setMaxTurnRate(maxTurnRate: number): void { this.maxTurnRate = IntentValidator.validateMaxTurnRate(maxTurnRate); }

  getGunTurnRate(): number { return this.intent.gunTurnRate ?? 0; }
  setGunTurnRate(gunTurnRate: number): void {
    this.intent.gunTurnRate = IntentValidator.validateGunTurnRate(gunTurnRate, this.maxGunTurnRate);
  }
  getMaxGunTurnRate(): number { return this.maxGunTurnRate; }
  setMaxGunTurnRate(maxGunTurnRate: number): void { this.maxGunTurnRate = IntentValidator.validateMaxGunTurnRate(maxGunTurnRate); }

  getRadarTurnRate(): number { return this.intent.radarTurnRate ?? 0; }
  setRadarTurnRate(radarTurnRate: number): void {
    this.intent.radarTurnRate = IntentValidator.validateRadarTurnRate(radarTurnRate, this.maxRadarTurnRate);
  }
  getMaxRadarTurnRate(): number { return this.maxRadarTurnRate; }
  setMaxRadarTurnRate(maxRadarTurnRate: number): void { this.maxRadarTurnRate = IntentValidator.validateMaxRadarTurnRate(maxRadarTurnRate); }

  getTargetSpeed(): number { return this.intent.targetSpeed ?? 0; }
  setTargetSpeed(targetSpeed: number): void {
    this.intent.targetSpeed = IntentValidator.validateTargetSpeed(targetSpeed, this.maxSpeed);
  }
  getMaxSpeed(): number { return this.maxSpeed; }
  setMaxSpeed(maxSpeed: number): void { this.maxSpeed = IntentValidator.validateMaxSpeed(maxSpeed); }

  getNewTargetSpeed(speed: number, distance: number): number {
    return IntentValidator.getNewTargetSpeed(speed, distance, this.maxSpeed);
  }

  getDistanceTraveledUntilStop(speed: number): number {
    return IntentValidator.getDistanceTraveledUntilStop(speed, this.maxSpeed);
  }

  // ---------------------------------------------------------------------------
  // Fire
  // ---------------------------------------------------------------------------

  setFire(firepower: number): boolean {
    IntentValidator.validateFirepower(firepower);
    if (!IntentValidator.isValidFirepower(firepower) ||
        (this.tickEvent?.botState.gunHeat ?? 0) > 0 ||
        (this.tickEvent?.botState.energy ?? 0) < firepower) return false;
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
    IntentValidator.validateTeamMessageSize(json);

    if (!this.intent.teamMessages) this.intent.teamMessages = [];
    IntentValidator.validateTeamMessage(message, this.intent.teamMessages.length);

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
  setBodyColor(color: Color | null): void { this.intent.bodyColor = IntentValidator.colorToHex(color); }
  getTurretColor(): Color | null { return this.intent.turretColor ? ColorUtil.fromHexColor(this.intent.turretColor) : null; }
  setTurretColor(color: Color | null): void { this.intent.turretColor = IntentValidator.colorToHex(color); }
  getRadarColor(): Color | null { return this.intent.radarColor ? ColorUtil.fromHexColor(this.intent.radarColor) : null; }
  setRadarColor(color: Color | null): void { this.intent.radarColor = IntentValidator.colorToHex(color); }
  getBulletColor(): Color | null { return this.intent.bulletColor ? ColorUtil.fromHexColor(this.intent.bulletColor) : null; }
  setBulletColor(color: Color | null): void { this.intent.bulletColor = IntentValidator.colorToHex(color); }
  getScanColor(): Color | null { return this.intent.scanColor ? ColorUtil.fromHexColor(this.intent.scanColor) : null; }
  setScanColor(color: Color | null): void { this.intent.scanColor = IntentValidator.colorToHex(color); }
  getTracksColor(): Color | null { return this.intent.tracksColor ? ColorUtil.fromHexColor(this.intent.tracksColor) : null; }
  setTracksColor(color: Color | null): void { this.intent.tracksColor = IntentValidator.colorToHex(color); }
  getGunColor(): Color | null { return this.intent.gunColor ? ColorUtil.fromHexColor(this.intent.gunColor) : null; }
  setGunColor(color: Color | null): void { this.intent.gunColor = IntentValidator.colorToHex(color); }

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
