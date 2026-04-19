import { vi, describe, it, expect, beforeEach, afterEach } from "vitest";
import { BaseBot } from "../src/BaseBot.js";
import { MockedServer } from "./test_utils/MockedServer.js";
import * as detectRuntimeModule from "../src/runtime/detectRuntime.js";
import { BotInfo } from "../src/BotInfo.js";
import { WebSocket } from "ws";
import { BotException } from "../src/BotException.js";
import { BaseBotInternals } from "../src/internal/BaseBotInternals.js";

describe("TR-API-BOT-001: Constructor & Lifecycle", () => {
  let server: MockedServer;
  let mockEnv: Record<string, string | undefined> = {};

  beforeEach(async () => {
    // Mock worker_threads to null to force main-thread mode in tests
    vi.spyOn(BaseBotInternals.prototype as any, "_importWorkerThreads").mockResolvedValue(null);

    server = new MockedServer();
    await server.start();
    mockEnv = {
        SERVER_URL: server.serverUrl,
        BOT_NAME: "DefaultBot",
        BOT_VERSION: "1.0",
        BOT_AUTHORS: "Author",
    };
    
    const mockAdapter = {
      getEnvVar: (name: string) => mockEnv[name],
      createWebSocket: (url: string) => new WebSocket(url),
      exit: () => {},
      readFile: (_path: string) => undefined,
    };
    vi.spyOn(detectRuntimeModule, "detectRuntime").mockReturnValue(mockAdapter as any);
  });

  afterEach(async () => {
    vi.restoreAllMocks();
    await server.stop();
  });

  it("TR-API-BOT-001a: Constructor reads env vars and applies defaults", async () => {
    mockEnv["BOT_NAME"] = "EnvBot";
    mockEnv["BOT_AUTHORS"] = "Alice, Bob";
    
    const bot = new BaseBot();
    bot.start();

    await server.awaitBotHandshake(5000);
    const handshake = server.getBotHandshake();
    expect(handshake!.name).toBe("EnvBot");
    expect(handshake!.authors).toEqual(["Alice", "Bob"]);
  });

  it("TR-API-BOT-001c: Explicit args take precedence over env vars", async () => {
    mockEnv["BOT_NAME"] = "EnvBot";
    
    const explicitInfo = new BotInfo("ExplicitBot", "2.0", ["Carol"], null, null, null, ["classic"], null, null);
    const bot = new BaseBot(explicitInfo);
    bot.start();

    await server.awaitBotHandshake(5000);
    const handshake = server.getBotHandshake();
    expect(handshake!.name).toBe("ExplicitBot");
    expect(handshake!.version).toBe("2.0");
  });

  it("TR-API-BOT-001d: Field type parsing (TEAM_ID, INITIAL_POS)", async () => {
    mockEnv["TEAM_ID"] = "  42  ";
    mockEnv["BOT_INITIAL_POS"] = "  10.5, 20.5, 90  ";
    
    const bot = new BaseBot();
    bot.start();

    await server.awaitBotHandshake(5000);
    const handshake = server.getBotHandshake();
    expect(handshake!.teamId).toBe(42);
    expect(handshake!.initialPosition).toEqual({ x: 10.5, y: 20.5, direction: 90 });
  });

  it("TR-API-BOT-001b: Missing required env defers validation to handshake (Negative: missing name)", async () => {
    delete mockEnv["BOT_NAME"];
    
    // In TypeScript, BaseBotInternals.connect() doesn't throw if name is missing,
    // but the server handshake handler will throw a BotException when it receives the server handshake.
    expect(() => new BaseBot()).not.toThrow();
    
    const bot = new BaseBot();
    let caughtError: any = null;
    bot.onConnectionError = (e) => {
        caughtError = e.error;
    };
    
    bot.start();
    
    // We don't await server.awaitBotHandshake(5000) here because the bot should fail 
    // BEFORE sending the handshake due to validation error.
    
    // Wait a bit for the error to be processed (it happens after server sends its handshake)
    await new Promise(resolve => setTimeout(resolve, 500));
    
    expect(caughtError).toBeInstanceOf(BotException);
    expect(caughtError.message).toContain("Required bot property 'name' is missing");
  });
});
