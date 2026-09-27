import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { BaseBot } from "../src/BaseBot.js";
import { Bot } from "../src/Bot.js";
import { MockedServer } from "./test_utils/MockedServer.js";
import { BotInfo } from "../src/BotInfo.js";
import { BaseBotInternals } from "../src/internal/BaseBotInternals.js";
import { MessageType } from "../src/protocol/MessageType.js";
import { TickEvent } from "../src/events/TickEvent.js";
import { BotException } from "../src/BotException.js";

describe("Unit: TR-API-TCK: Protocol Conformance", () => {
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

  describe("PRO-012", () => {
    it("Integration Positive: TR-API-TCK-023 bot name lookup returns own and teammate names", async () => {
      server.setGameStartedNames(
        [2],
        [
          { botId: 1, name: "legacy.TeamBot 1.2 (1)" },
          { botId: 2, name: "legacy.TeamBot 1.2 (2)" },
        ],
      );
      const bot = new BaseBot(info, server.serverUrl);
      bot.start();

      await server.awaitBotHandshake(5000);
      server.sendGameStarted(1, ["classic"], [2]);
      expect(await server.awaitBotReady(5000)).toBe(true);

      expect(bot.getBotName(1)).toBe("legacy.TeamBot 1.2 (1)");
      expect(bot.getBotName(2)).toBe("legacy.TeamBot 1.2 (2)");
    });

  });

  describe("PRO-012a", () => {
    it("Integration Negative: TR-API-TCK-023 bot name lookup throws before start and returns null for unmapped IDs", async () => {
      const bot = new BaseBot(info, server.serverUrl);
      expect(() => bot.getBotName(1)).toThrow(BotException);
      bot.start();

      await server.awaitBotHandshake(5000);
      server.sendGameStarted();
      expect(await server.awaitBotReady(5000)).toBe(true);

      expect(bot.getBotName(2)).toBeNull();
      expect(bot.getBotName(99)).toBeNull();
    });
  });

  describe("PRO-011", () => {
    it("Integration Positive: TR-API-TCK-024 bot handshake preserves a long team member name", async () => {
      const teamMemberName = "legacy.package.TeamRobotNameThatExceedsThirtyCharacters";
      const infoWithTeamName = new BotInfo(
        "TCKBot", "1.0", ["Author"], null, null, [], ["classic"], null, null, null, teamMemberName,
      );
      const bot = new BaseBot(infoWithTeamName, server.serverUrl);
      bot.start();

      await server.awaitBotHandshake(5000);

      expect(server.getBotHandshake()?.teamMemberName).toBe(teamMemberName);
    });

  });

  describe("PRO-011a", () => {
    it("Integration Negative: TR-API-TCK-024 bot handshake omits an unset team member name", async () => {
      const bot = new BaseBot(info, server.serverUrl);
      bot.start();

      await server.awaitBotHandshake(5000);

      expect(server.getBotHandshake()?.teamMemberName).toBeUndefined();
      expect(JSON.parse(server.getBotHandshakeJson()!)).not.toHaveProperty("teamMemberName");
    });
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

  it("TR-API-TCK-012: SkippedTurnEvent fires with correct turnNumber", async () => {
    let capturedTurn: number | null = null;

    const bot = new BaseBot(info, server.serverUrl);
    bot.onSkippedTurn = (e) => { capturedTurn = e.turnNumber; };
    bot.onTick = () => { bot.go(); };

    // Inject SkippedTurnEvent(1) into tick 1's events payload before the bot starts
    server.addEvent({ type: MessageType.SkippedTurnEvent, turnNumber: 1 });

    bot.start();

    await server.awaitBotHandshake(5000);
    // Tick 1 (containing SkippedTurnEvent) is auto-sent on BotReady.
    // awaitBotIntent waits for go() → sendIntent to confirm tick 1 was fully processed.
    await server.awaitBotIntent(5000);

    expect(capturedTurn).toBe(1);
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
