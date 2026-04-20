import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { BaseBot } from "../src/BaseBot.js";
import { Bot } from "../src/Bot.js";
import { MockedServer } from "./test_utils/MockedServer.js";
import { BotInfo } from "../src/BotInfo.js";
import { BaseBotInternals } from "../src/internal/BaseBotInternals.js";
import { MessageType } from "../src/protocol/MessageType.js";
import { TickEvent } from "../src/events/TickEvent.js";

describe("TR-API-TCK: Protocol Conformance", () => {
  let server: MockedServer;
  const info = new BotInfo("TCKBot", "1.0", ["Author"], null, null, null, ["classic"], null, null);

  beforeEach(async () => {
    // Mock worker_threads to null to force main-thread mode in tests
    vi.spyOn(BaseBotInternals.prototype as any, "_importWorkerThreads").mockResolvedValue(null);

    server = new MockedServer();
    await server.start();
  });

  afterEach(async () => {
    await server.stop();
  });

  it("TR-API-TCK-004: Bot sees first tick state and sends initial intent", async () => {
    const bot = new BaseBot(info, server.serverUrl);
    
    // In legacy main-thread mode (forced by mock), we must manually call go() or 
    // drive the bot via events because there is no worker loop.
    bot.onTick = () => {
        bot.go();
    };
    
    bot.start();
    
    await server.awaitBotHandshake(5000);
    server.sendGameStarted();
    server.sendRoundStarted();
    server.sendTick(1);
    
    await server.awaitBotIntent(5000);
    const intent = server.getBotIntent();
    expect(intent).toBeDefined();
    expect(intent!.type).toBe("BotIntent");
  });

  it("TR-API-TCK-006: Team message delivery", async () => {
      // Create a bot that sends a team message on first tick
      class TeamBot extends BaseBot {
          onTick() {
              this.broadcastTeamMessage({ hello: "team" });
              this.go();
          }
      }
      
      const bot = new TeamBot(info, server.serverUrl);
      bot.start();
      
      await server.awaitBotHandshake(5000);
      server.sendGameStarted(1, ["classic"], [1, 2]); // teammateIds: [1, 2]
      server.sendRoundStarted();
      server.sendTick(1);
      
      await server.awaitBotIntent(5000);
      const intent = server.getBotIntent();
      expect(intent).toBeDefined();
      expect(intent!.teamMessages).toBeDefined();
      expect(intent!.teamMessages!.length).toBe(1);
  });

  it("TR-API-TCK-005: WonRoundEvent delivery", async () => {
    let wonRoundFired = false;
    const bot = new BaseBot(info, server.serverUrl);
    bot.onWonRound = (e) => {
        wonRoundFired = true;
    };
    
    bot.start();
    await server.awaitBotHandshake(5000);
    server.sendGameStarted();
    server.sendRoundStarted();
    
    // Add WonRoundEvent to the next tick (turn 1)
    server.addEvent({
        type: MessageType.WonRoundEvent,
        turnNumber: 1
    });
    
    // Trigger tick 1
    await server.setBotStateAndAwaitTick();
    
    // Wait for event loop
    await new Promise(resolve => setTimeout(resolve, 100));
    
    expect(wonRoundFired).toBe(true);
  });

  it("TR-API-TCK-007: debugGraphics is populated in intent when isDebuggingEnabled=true", async () => {
    class PaintBot extends BaseBot {
      override onTick(_e: TickEvent) {
        const g = this.getGraphics();
        g.fillCircle(100, 200, 20);
        this.go();
      }
    }

    server.setDebuggingEnabled(true);

    const bot = new PaintBot(info, server.serverUrl);
    bot.start();

    await server.awaitBotHandshake(5000);
    server.sendGameStarted();
    server.sendRoundStarted();
    server.sendTick(1);

    await server.awaitBotIntent(5000);
    const intent = server.getBotIntent();
    expect(intent).toBeDefined();
    expect(intent!.debugGraphics).toBeDefined();
    expect(intent!.debugGraphics).not.toBeNull();
    expect(intent!.debugGraphics).toContain("<svg");
    expect(intent!.debugGraphics).toContain("circle");
  });
});
