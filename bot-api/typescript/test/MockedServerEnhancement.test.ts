/**
 * MockedServer Enhancement Tests — TypeScript port of Java/Python/.NET Phase 1 tests.
 *
 * These tests verify that MockedServer.awaitBotReady() and
 * MockedServer.setBotStateAndAwaitTick() work correctly by simulating the bot
 * handshake/ready/intent protocol using a raw WebSocket client.
 *
 * Note: The TypeScript Bot API uses Worker threads (ADR-0028). In-process vitest
 * tests cannot easily spawn workers re-using the same entry point, so we drive
 * the protocol with a minimal WS client rather than a full Bot instance — the
 * same behaviour that Java/Python/.NET tests exercise through their respective
 * Bot classes.
 */
import { describe, it, expect, beforeEach, afterEach } from "vitest";
import { WebSocket } from "ws";
import {
  MockedServer,
  SESSION_ID,
  MY_ID,
  GAME_TYPE,
  ARENA_WIDTH,
  ARENA_HEIGHT,
  NUMBER_OF_ROUNDS,
  GUN_COOLING_RATE,
  MAX_INACTIVITY_TURNS,
  TURN_TIMEOUT,
  READY_TIMEOUT,
  BOT_ENERGY,
  BOT_GUN_HEAT,
} from "./test_utils/MockedServer.js";
import { MessageType } from "../src/protocol/MessageType.js";
import type {
  ServerHandshake,
  GameStartedEventForBot,
  BotHandshake,
  BotReady,
  BotIntent,
} from "../src/protocol/schema.js";

describe("LEGACY", () => {

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

/**
 * Minimal bot client that speaks the bot protocol against a real MockedServer.
 * Handles the full startup sequence and sends a BotIntent after each tick.
 */
class MinimalBotClient {
  private ws: WebSocket;
  private latestEnergy = 0;
  private latestGunHeat = 0;

  constructor(serverUrl: string) {
    this.ws = new WebSocket(serverUrl);
  }

  /** Connect and drive the full handshake → BotReady sequence. Returns a promise that resolves when connected and the first tick has been processed. */
  start(): void {
    this.ws.on("message", (data: Buffer | string) => {
      const json = data.toString();
      const msg = JSON.parse(json) as { type: string };
      this.handleMessage(msg);
    });
  }

  private handleMessage(msg: { type: string }): void {
    switch (msg.type as MessageType) {
      case MessageType.ServerHandshake: {
        const sh = msg as unknown as ServerHandshake;
        const handshake: BotHandshake = {
          type: MessageType.BotHandshake,
          sessionId: sh.sessionId,
          name: "TestBot",
          version: "1.0",
          authors: ["TestAuthor"],
        };
        this.ws.send(JSON.stringify(handshake));
        break;
      }
      case MessageType.GameStartedEventForBot: {
        const ready: BotReady = { type: MessageType.BotReady };
        this.ws.send(JSON.stringify(ready));
        break;
      }
      case MessageType.RoundStartedEvent:
        // nothing to do
        break;
      case MessageType.TickEventForBot: {
        const tick = msg as unknown as { botState: { energy: number; gunHeat: number } };
        this.latestEnergy = tick.botState.energy;
        this.latestGunHeat = tick.botState.gunHeat;
        // Send BotIntent to allow the server to proceed
        const intent: BotIntent = { type: MessageType.BotIntent };
        this.ws.send(JSON.stringify(intent));
        break;
      }
    }
  }

  getEnergy(): number { return this.latestEnergy; }
  getGunHeat(): number { return this.latestGunHeat; }

  close(): void {
    this.ws.close();
  }
}

/** Returns a promise that resolves when the WebSocket client is open. */
function waitForOpen(ws: WebSocket): Promise<void> {
  return new Promise((resolve, reject) => {
    if (ws.readyState === WebSocket.OPEN) {
      resolve();
    } else {
      ws.once("open", resolve);
      ws.once("error", reject);
    }
  });
}

/** Poll until predicate returns true or timeout expires. */
function poll(predicate: () => boolean, timeoutMs = 2000, intervalMs = 10): Promise<boolean> {
  return new Promise((resolve) => {
    const deadline = Date.now() + timeoutMs;
    const check = () => {
      if (predicate()) {
        resolve(true);
      } else if (Date.now() >= deadline) {
        resolve(false);
      } else {
        setTimeout(check, intervalMs);
      }
    };
    check();
  });
}

// ---------------------------------------------------------------------------
// Tests
// ---------------------------------------------------------------------------

describe("MockedServer Enhancement Tests", () => {
  let server: MockedServer;
  let client: MinimalBotClient;

  beforeEach(async () => {
    server = new MockedServer();
    await server.start();
    client = new MinimalBotClient(server.serverUrl);
    client.start();
  });

  afterEach(async () => {
    client.close();
    await server.stop();
  });

  it("awaitBotReady() should succeed when bot is ready", async () => {
    const ready = await server.awaitBotReady(2000);
    expect(ready).toBe(true);
  });

  it("setBotStateAndAwaitTick() should update state and await next tick", async () => {
    // Wait for bot to complete startup sequence
    const ready = await server.awaitBotReady(2000);
    expect(ready).toBe(true);

    // Poll until the client has processed the first tick with the initial energy
    const initialEnergyReceived = await poll(() => client.getEnergy() === BOT_ENERGY);
    expect(initialEnergyReceived)
      .toBe(true);

    // Update state on the server
    const newEnergy = 50.0;
    const newGunHeat = 1.5;
    const success = await server.setBotStateAndAwaitTick(newEnergy, newGunHeat);

    expect(success).toBe(true);

    // Server state is updated immediately
    expect(server.getEnergy()).toBe(newEnergy);
    expect(server.getGunHeat()).toBe(newGunHeat);

    // Poll until the client has received and processed the updated tick
    const updatedEnergyReceived = await poll(() => client.getEnergy() === newEnergy);
    expect(updatedEnergyReceived)
      .toBe(true);

    const updatedGunHeatReceived = await poll(() => client.getGunHeat() === newGunHeat);
    expect(updatedGunHeatReceived)
      .toBe(true);
  });
});
});
