/**
 * MockedServer — TypeScript port of the Java/Python/.NET test utility.
 *
 * Starts a real WebSocket server on a random free port. Drives the bot through
 * the standard handshake/game/round/tick lifecycle and provides await helpers
 * with deterministic state control for integration tests.
 */
import * as http from "node:http";
import { WebSocketServer, WebSocket } from "ws";
import { MessageType } from "../../src/protocol/MessageType.js";
import type {
  ServerHandshake,
  GameStartedEventForBot,
  RoundStartedEvent,
  TickEventForBot,
  BotHandshake,
  BotReady,
  BotIntent,
} from "../../src/protocol/schema.js";

// ---------------------------------------------------------------------------
// Lightweight resettable latch — mirrors Java's volatile CountDownLatch
// ---------------------------------------------------------------------------

class Latch {
  private _resolve!: () => void;
  private _promise: Promise<void>;

  constructor() {
    this._promise = new Promise<void>((res) => {
      this._resolve = res;
    });
  }

  /** Signal the latch (equivalent to countDown()). */
  signal(): void {
    this._resolve();
  }

  /** Wait up to timeoutMs; returns true if signalled, false on timeout. */
  async wait(timeoutMs: number): Promise<boolean> {
    let timerId: ReturnType<typeof setTimeout> | undefined;
    const timeout = new Promise<boolean>((res) => {
      timerId = setTimeout(() => res(false), timeoutMs);
    });
    const latchResolved = this._promise.then(() => {
      clearTimeout(timerId);
      return true;
    });
    return Promise.race([latchResolved, timeout]);
  }
}

// ---------------------------------------------------------------------------
// Static constants (keep in sync with Java MockedServer)
// ---------------------------------------------------------------------------

export const SESSION_ID = "123abc";
export const SERVER_NAME = "MockedServer";
export const SERVER_VERSION = "1.0.0";
export const VARIANT = "Tank Royale";
export const GAME_TYPES = ["melee", "classic", "1v1"];
export const MY_ID = 1;
export const GAME_TYPE = "classic";
export const ARENA_WIDTH = 800;
export const ARENA_HEIGHT = 600;
export const NUMBER_OF_ROUNDS = 10;
export const GUN_COOLING_RATE = 0.1;
export const MAX_INACTIVITY_TURNS = 450;
export const TURN_TIMEOUT = 30_000;
export const READY_TIMEOUT = 1_000_000;

export const BOT_ENEMY_COUNT = 7;
export const BOT_ENERGY = 99.7;
export const BOT_X = 44.5;
export const BOT_Y = 721.34;
export const BOT_DIRECTION = 120.1;
export const BOT_GUN_DIRECTION = 103.45;
export const BOT_RADAR_DIRECTION = 253.3;
export const BOT_RADAR_SWEEP = 13.5;
export const BOT_SPEED = 8.0;
export const BOT_TURN_RATE = 5.1;
export const BOT_GUN_TURN_RATE = 18.9;
export const BOT_RADAR_TURN_RATE = 34.1;
export const BOT_GUN_HEAT = 7.6;

// ---------------------------------------------------------------------------
// MockedServer
// ---------------------------------------------------------------------------

export class MockedServer {
  private readonly port: number;
  readonly serverUrl: string;

  private wss: WebSocketServer | null = null;
  private conn: WebSocket | null = null;

  // Runtime state
  private turnNumber = 1;
  private energy = BOT_ENERGY;
  private gunHeat = BOT_GUN_HEAT;
  private speed = BOT_SPEED;
  private direction = BOT_DIRECTION;
  private gunDirection = BOT_GUN_DIRECTION;
  private radarDirection = BOT_RADAR_DIRECTION;

  private additionalEvents: any[] = [];

  public addEvent(event: any): void {
    this.additionalEvents.push(event);
  }

  // Increments
  private speedIncrement = 0;
  private turnIncrement = 0;
  private gunTurnIncrement = 0;
  private radarTurnIncrement = 0;

  // Optional limits
  private speedMinLimit: number | null = null;
  private speedMaxLimit: number | null = null;
  private directionMinLimit: number | null = null;
  private directionMaxLimit: number | null = null;
  private gunDirectionMinLimit: number | null = null;
  private gunDirectionMaxLimit: number | null = null;
  private radarDirectionMinLimit: number | null = null;
  private radarDirectionMaxLimit: number | null = null;

  // Latches
  private openedLatch = new Latch();
  private botHandshakeLatch = new Latch();
  private gameStartedLatch = new Latch();
  private botIntentLatch = new Latch();
  // volatile: re-created after each intent (mirrors Java's volatile CountDownLatch)
  private tickEventLatch = new Latch();
  private botIntentContinueLatch = new Latch();

  // Captured state
  private botHandshakeData: BotHandshake | null = null;
  private botIntentData: BotIntent | null = null;

  constructor() {
    this.port = MockedServer.findAvailablePort();
    this.serverUrl = `ws://127.0.0.1:${this.port}`;
  }

  // ---------------------------------------------------------------------------
  // Lifecycle
  // ---------------------------------------------------------------------------

  /** Start the server; resolves once listening. */
  async start(): Promise<void> {
    await new Promise<void>((resolve, reject) => {
      this.wss = new WebSocketServer({ port: this.port }, () => resolve());
      this.wss.on("error", reject);
    });

    this.wss!.on("connection", (ws) => {
      this.conn = ws;
      this.openedLatch.signal();

      ws.on("message", (data: Buffer | string) => {
        const json = data.toString();
        const msg = JSON.parse(json) as { type: string };

        switch (msg.type as MessageType) {
          case MessageType.BotHandshake:
            this.botHandshakeData = msg as unknown as BotHandshake;
            this.botHandshakeLatch.signal();
            this.sendGameStarted();
            this.gameStartedLatch.signal();
            break;

          case MessageType.BotReady:
            this.sendRoundStarted();
            this.sendTick(this.turnNumber++);
            this.tickEventLatch.signal();
            break;

          case MessageType.BotIntent:
            if (this.speedMinLimit != null && this.speed < this.speedMinLimit) return;
            if (this.speedMaxLimit != null && this.speed > this.speedMaxLimit) return;
            if (this.directionMinLimit != null && this.direction < this.directionMinLimit) return;
            if (this.directionMaxLimit != null && this.direction > this.directionMaxLimit) return;
            if (this.gunDirectionMinLimit != null && this.gunDirection < this.gunDirectionMinLimit) return;
            if (this.gunDirectionMaxLimit != null && this.gunDirection > this.gunDirectionMaxLimit) return;
            if (this.radarDirectionMinLimit != null && this.radarDirection < this.radarDirectionMinLimit) return;
            if (this.radarDirectionMaxLimit != null && this.radarDirection > this.radarDirectionMaxLimit) return;

            // Gate: wait for the continue latch before sending the next tick
            this.botIntentContinueLatch.wait(5000).then(() => {
              // Re-create continue latch for the next intent (mirrors Java's volatile re-assign)
              this.botIntentContinueLatch = new Latch();

              this.botIntentData = msg as unknown as BotIntent;
              this.botIntentLatch.signal();

              // Apply increments
              this.speed += this.speedIncrement;
              this.direction += this.turnIncrement;
              this.gunDirection += this.gunTurnIncrement;
              this.radarDirection += this.radarTurnIncrement;

              // Apply bot intent changes (firepower → gunHeat / energy)
              const intent = this.botIntentData;
              if (intent != null) {
                const fp = (intent as any).firepower;
                if (fp != null && fp > 0) {
                  this.gunHeat += 1.0 + fp / 5.0;
                  this.energy -= fp;
                }
              }

              this.sendTick(this.turnNumber++);
              this.tickEventLatch.signal();
            });
            break;
        }
      });

      ws.on("error", (err: Error) => {
        console.error("[MockedServer] WebSocket error:", err);
      });

      this.sendServerHandshake();
    });
  }

  /** Stop the server; resolves after it is closed. */
  async stop(): Promise<void> {
    // Signal any waiting botIntentContinueLatch so message handlers can exit
    this.botIntentContinueLatch.signal();

    await new Promise<void>((resolve) => {
      if (this.conn) {
        this.conn.close();
        this.conn = null;
      }
      if (this.wss) {
        this.wss.close(() => resolve());
        this.wss = null;
      } else {
        resolve();
      }
    });
  }

  // ---------------------------------------------------------------------------
  // State setters
  // ---------------------------------------------------------------------------

  setEnergy(energy: number): void { this.energy = energy; }
  setGunHeat(gunHeat: number): void { this.gunHeat = gunHeat; }
  setSpeed(speed: number): void { this.speed = speed; }
  setSpeedIncrement(increment: number): void { this.speedIncrement = increment; }
  setTurnIncrement(increment: number): void { this.turnIncrement = increment; }
  setGunTurnIncrement(increment: number): void { this.gunTurnIncrement = increment; }
  setRadarTurnIncrement(increment: number): void { this.radarTurnIncrement = increment; }
  setSpeedMinLimit(limit: number): void { this.speedMinLimit = limit; }
  setSpeedMaxLimit(limit: number): void { this.speedMaxLimit = limit; }
  setDirectionMinLimit(limit: number): void { this.directionMinLimit = limit; }
  setDirectionMaxLimit(limit: number): void { this.directionMaxLimit = limit; }
  setGunDirectionMinLimit(limit: number): void { this.gunDirectionMinLimit = limit; }
  setGunDirectionMaxLimit(limit: number): void { this.gunDirectionMaxLimit = limit; }
  setRadarDirectionMinLimit(limit: number): void { this.radarDirectionMinLimit = limit; }
  setRadarDirectionMaxLimit(limit: number): void { this.radarDirectionMaxLimit = limit; }

  // ---------------------------------------------------------------------------
  // State accessors
  // ---------------------------------------------------------------------------

  getEnergy(): number { return this.energy; }
  getGunHeat(): number { return this.gunHeat; }
  getBotHandshake(): BotHandshake | null { return this.botHandshakeData; }
  getBotIntent(): BotIntent | null { return this.botIntentData; }

  // ---------------------------------------------------------------------------
  // Enhancement methods (Phase 1)
  // ---------------------------------------------------------------------------

  /**
   * Blocks until the bot has completed the full startup sequence:
   * BotHandshake received → GameStarted sent → first tick sent.
   */
  async awaitBotReady(timeoutMs: number): Promise<boolean> {
    return (
      await this.awaitBotHandshake(timeoutMs) &&
      await this.awaitGameStarted(timeoutMs) &&
      await this.awaitTick(timeoutMs)
    );
  }

  /**
   * Updates the specified state fields, resets the tick latch, releases the
   * intent gate, then waits for the next tick to be sent to the bot.
   * Returns true if the tick arrived within 5 000 ms.
   */
  async setBotStateAndAwaitTick(
    energy?: number | null,
    gunHeat?: number | null,
    speed?: number | null,
    direction?: number | null,
    gunDirection?: number | null,
    radarDirection?: number | null,
  ): Promise<boolean> {
    if (energy != null) this.energy = energy;
    if (gunHeat != null) this.gunHeat = gunHeat;
    if (speed != null) this.speed = speed;
    if (direction != null) this.direction = direction;
    if (gunDirection != null) this.gunDirection = gunDirection;
    if (radarDirection != null) this.radarDirection = radarDirection;

    // Re-create tickEventLatch (mirrors Java's volatile CountDownLatch re-assign)
    this.tickEventLatch = new Latch();

    // Release the botIntentContinueLatch so the pending BotIntent handler can proceed
    this.botIntentContinueLatch.signal();

    return this.awaitTick(5000);
  }

  // ---------------------------------------------------------------------------
  // Existing await helpers
  // ---------------------------------------------------------------------------

  async awaitConnection(timeoutMs: number): Promise<boolean> {
    return this.openedLatch.wait(timeoutMs);
  }

  async awaitBotHandshake(timeoutMs: number): Promise<boolean> {
    return this.botHandshakeLatch.wait(timeoutMs);
  }

  async awaitGameStarted(timeoutMs: number): Promise<boolean> {
    return this.gameStartedLatch.wait(timeoutMs);
  }

  async awaitTick(timeoutMs: number): Promise<boolean> {
    return this.tickEventLatch.wait(timeoutMs);
  }

  async awaitBotIntent(timeoutMs: number): Promise<boolean> {
    this.botIntentContinueLatch.signal();
    return this.botIntentLatch.wait(timeoutMs);
  }

  // ---------------------------------------------------------------------------
  // Message senders
  // ---------------------------------------------------------------------------

  private sendServerHandshake(): void {
    const msg: ServerHandshake = {
      type: MessageType.ServerHandshake,
      sessionId: SESSION_ID,
      name: SERVER_NAME,
      variant: VARIANT,
      version: SERVER_VERSION,
      gameTypes: GAME_TYPES,
    };
    this.send(msg);
  }

  private sendGameStarted(): void {
    const msg: GameStartedEventForBot = {
      type: MessageType.GameStartedEventForBot,
      myId: MY_ID,
      teammateIds: [],
      gameSetup: {
        gameType: GAME_TYPE,
        arenaWidth: ARENA_WIDTH,
        arenaHeight: ARENA_HEIGHT,
        numberOfRounds: NUMBER_OF_ROUNDS,
        gunCoolingRate: GUN_COOLING_RATE,
        maxInactivityTurns: MAX_INACTIVITY_TURNS,
        turnTimeout: TURN_TIMEOUT,
        readyTimeout: READY_TIMEOUT,
      },
    };
    this.send(msg);
  }

  private sendRoundStarted(): void {
    const msg: RoundStartedEvent = {
      type: MessageType.RoundStartedEvent,
      roundNumber: 1,
    };
    this.send(msg);
  }

  private sendTick(turnNumber: number): void {
    const msg: TickEventForBot = {
      type: MessageType.TickEventForBot,
      roundNumber: 1,
      turnNumber,
      botState: {
        isDroid: false,
        energy: this.energy,
        x: BOT_X,
        y: BOT_Y,
        direction: this.direction,
        gunDirection: this.gunDirection,
        radarDirection: this.radarDirection,
        radarSweep: BOT_RADAR_SWEEP,
        speed: this.speed,
        turnRate: BOT_TURN_RATE,
        gunTurnRate: BOT_GUN_TURN_RATE,
        radarTurnRate: BOT_RADAR_TURN_RATE,
        gunHeat: this.gunHeat,
        enemyCount: BOT_ENEMY_COUNT,
        isDebuggingEnabled: false,
      },
      bulletStates: [
        { bulletId: 1, ownerId: 0, power: 0, x: 0, y: 0, direction: 0 },
        { bulletId: 2, ownerId: 0, power: 0, x: 0, y: 0, direction: 0 },
      ],
      events: [
        {
          type: MessageType.ScannedBotEvent,
          turnNumber: 1,
          scannedByBotId: MY_ID,
          scannedBotId: 2,
          energy: 56.9,
          x: 134.56,
          y: 256.7,
          direction: 45.0,
          speed: 9.6,
        },
        ...this.additionalEvents,
      ] as unknown as TickEventForBot["events"],
    };
    this.additionalEvents = [];
    this.send(msg);
  }

  private send(msg: object): void {
    if (this.conn != null && this.conn.readyState === WebSocket.OPEN) {
      this.conn.send(JSON.stringify(msg));
    }
  }

  // ---------------------------------------------------------------------------
  // Port utility
  // ---------------------------------------------------------------------------

  static findAvailablePort(): number {
    const server = http.createServer();
    server.listen(0);
    const address = server.address();
    const port = typeof address === "object" && address !== null ? address.port : 0;
    server.close();
    return port || 7913;
  }
}
