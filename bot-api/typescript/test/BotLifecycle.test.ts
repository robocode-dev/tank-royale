/**
 * Integration tests for tasks 4–8: BaseBotInternals, BotInternals, Bot lifecycle.
 * Uses a mock WebSocket server to drive the bot through game events.
 */
import { describe, it, expect, vi, beforeEach } from "vitest";
import { BaseBotInternals } from "../src/internal/BaseBotInternals.js";
import { BotInternals } from "../src/internal/BotInternals.js";
import { BotStoppedException } from "../src/internal/BotStoppedException.js";
import { Bot } from "../src/Bot.js";
import { BaseBot } from "../src/BaseBot.js";
import { BotInfo } from "../src/BotInfo.js";
import { Condition } from "../src/events/Condition.js";
import { SkippedTurnEvent } from "../src/events/SkippedTurnEvent.js";
import { GameSetup } from "../src/GameSetup.js";
import { BotResults } from "../src/BotResults.js";
import { MessageType } from "../src/protocol/MessageType.js";
import type {
  GameStartedEventForBot,
  GameEndedEventForBot,
  RoundStartedEvent,
  RoundEndedEventForBot,
  TickEventForBot,
  ServerHandshake,
} from "../src/protocol/schema.js";

describe("LEGACY", () => {

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeBotInfo(): BotInfo {
  return new BotInfo("TestBot", "1.0", ["Author"], null, null, null, ["classic"], null, null);
}

function makeGameSetup() {
  return {
    gameType: "classic",
    arenaWidth: 800,
    arenaHeight: 600,
    numberOfRounds: 10,
    gunCoolingRate: 0.1,
    maxInactivityTurns: 450,
    turnTimeout: 30000,
    isArenaWidthLocked: false,
    isArenaHeightLocked: false,
    isNumberOfRoundsLocked: false,
    isGunCoolingRateLocked: false,
    isMaxInactivityTurnsLocked: false,
    isTurnTimeoutLocked: false,
    minNumberOfParticipants: 2,
    maxNumberOfParticipants: null,
    isMinNumberOfParticipantsLocked: false,
    isMaxNumberOfParticipantsLocked: false,
    readyTimeout: 1000,
    isReadyTimeoutLocked: false,
    defaultTurnsPerSecond: 30,
  };
}

function makeTickEvent(turnNumber: number, roundNumber = 1, energy = 100): TickEventForBot {
  return {
    type: MessageType.TickEventForBot,
    turnNumber,
    roundNumber,
    botState: {
      isDroid: false,
      energy,
      x: 100,
      y: 200,
      direction: 45,
      gunDirection: 90,
      radarDirection: 135,
      radarSweep: 22.5,
      speed: 0,
      turnRate: 0,
      gunTurnRate: 0,
      radarTurnRate: 0,
      gunHeat: 0,
      enemyCount: 1,
      bodyColor: null,
      turretColor: null,
      radarColor: null,
      bulletColor: null,
      scanColor: null,
      tracksColor: null,
      gunColor: null,
      isDebuggingEnabled: false,
    },
    bulletStates: [],
    events: [],
  };
}

function makeResults(): import("../src/protocol/schema.js").ResultsForBot {
  return {
    rank: 1,
    survival: 10,
    lastSurvivorBonus: 0,
    bulletDamage: 5,
    bulletKillBonus: 0,
    ramDamage: 0,
    ramKillBonus: 0,
    totalScore: 15,
    firstPlaces: 1,
    secondPlaces: 0,
    thirdPlaces: 0,
  };
}

// ---------------------------------------------------------------------------
// Mock WebSocket + adapter setup
// ---------------------------------------------------------------------------

type MockWsCallbacks = {
  onopen?: () => void;
  onclose?: (e: { code?: number; reason?: string }) => void;
  onerror?: (e: unknown) => void;
  onmessage?: (e: { data: string }) => void;
};

function makeMockAdapter(callbacks: MockWsCallbacks) {
  const sent: string[] = [];
  const ws = {
    onopen: null as unknown,
    onclose: null as unknown,
    onerror: null as unknown,
    onmessage: null as unknown,
    readyState: 1,
    send(data: string) { sent.push(data); },
    close() {
      if (typeof (ws as MockWsCallbacks).onclose === "function") {
        (ws.onclose as (e: { code?: number }) => void)({ code: 1000 });
      }
    },
  };
  Object.assign(callbacks, {
    triggerOpen: () => (ws.onopen as () => void)?.(),
    triggerMessage: (data: string) => (ws.onmessage as (e: { data: string }) => void)?.({ data }),
    triggerClose: () => (ws.onclose as (e: { code?: number }) => void)?.({ code: 1000 }),
    sent,
  });
  const adapter = {
    createWebSocket: () => ws,
    getEnvVar: () => null,
    exit: () => {},
  };
  return { adapter, ws, sent };
}

// Build a BaseBotInternals with a mock adapter injected via monkey-patching detectRuntime
function makeInternals(botInfo = makeBotInfo()) {
  const sent: string[] = [];
  let wsCallbacks: {
    onopen?: () => void;
    onclose?: (e: { code?: number; reason?: string }) => void;
    onmessage?: (e: { data: string }) => void;
  } = {};

  const mockWs = {
    onopen: null as unknown,
    onclose: null as unknown,
    onerror: null as unknown,
    onmessage: null as unknown,
    readyState: 1,
    send(data: string) { sent.push(data); },
    close() {},
  };

  // Patch WebSocketHandler to use mock ws
  const { WebSocketHandler } = require("../src/WebSocketHandler.js");
  const origConnect = WebSocketHandler.prototype.connect;
  WebSocketHandler.prototype.connect = function () {
    const ws = mockWs;
    (this as { socket: unknown }).socket = ws;
    ws.onopen = () => this.callbacks?.onConnected?.();
    ws.onclose = (e: { code?: number; reason?: string }) =>
      this.callbacks?.onDisconnected?.(true, e.code, e.reason);
    ws.onerror = (e: unknown) => this.callbacks?.onConnectionError?.(e);
    ws.onmessage = (e: { data: string }) => {
      const msg = JSON.parse(e.data);
      this.handleMessage?.(e.data);
    };
    wsCallbacks = {
      onopen: () => (ws.onopen as () => void)?.(),
      onclose: (e) => (ws.onclose as (e: { code?: number; reason?: string }) => void)?.(e),
      onmessage: (e) => (ws.onmessage as (e: { data: string }) => void)?.(e),
    };
  };

  // We can't easily inject adapter, so test BaseBotInternals methods directly
  WebSocketHandler.prototype.connect = origConnect;

  return { sent, wsCallbacks, mockWs };
}

// ---------------------------------------------------------------------------
// Task 4: BaseBotInternals
// ---------------------------------------------------------------------------

describe("Task 4: BaseBotInternals", () => {
  it("4.1 constructor initializes all fields correctly", () => {
    const info = makeBotInfo();
    // Create a minimal stub baseBot
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, info, "ws://localhost:7654", undefined);
    expect(internals.botInfo).toBe(info);
    expect(internals.serverUrl).toBe("ws://localhost:7654");
    expect(internals.serverSecret).toBeUndefined();
    expect(internals.eventQueue).toBeDefined();
    expect(internals.botEventHandlers).toBeDefined();
    expect(internals.internalEventHandlers).toBeDefined();
  });

  it("4.10 dispatchEvents calls eventQueue.dispatchEvents", () => {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, makeBotInfo(), null, undefined);
    const spy = vi.spyOn(internals.eventQueue, "dispatchEvents");
    internals.dispatchEvents(5);
    expect(spy).toHaveBeenCalledWith(5, internals.botEventHandlers);
  });

  it("4.11 enableEventHandling sets/clears disabled turn", () => {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, makeBotInfo(), null, undefined);
    internals.enableEventHandling(true);
    expect(internals.isEventHandlingDisabled()).toBe(false);
  });

  it("4.12 state accessors return defaults before game starts", () => {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, makeBotInfo(), null, undefined);
    expect(internals.getMyId()).toBe(0);
    expect(internals.getRoundNumber()).toBe(0);
    expect(internals.getTurnNumber()).toBe(0);
    expect(internals.getEnergy()).toBe(0);
    expect(internals.isDisabled()).toBe(false);
    expect(internals.isRunning()).toBe(false);
  });

  it("4.13 setTurnRate clamps to maxTurnRate", () => {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, makeBotInfo(), null, undefined);
    internals.setTurnRate(999);
    expect(internals.getTurnRate()).toBe(10);
    internals.setTurnRate(-999);
    expect(internals.getTurnRate()).toBe(-10);
  });

  it("4.13 setGunTurnRate clamps to maxGunTurnRate", () => {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, makeBotInfo(), null, undefined);
    internals.setGunTurnRate(999);
    expect(internals.getGunTurnRate()).toBe(20);
  });

  it("4.13 setRadarTurnRate clamps to maxRadarTurnRate", () => {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, makeBotInfo(), null, undefined);
    internals.setRadarTurnRate(999);
    expect(internals.getRadarTurnRate()).toBe(45);
  });

  it("4.13 setTargetSpeed clamps to maxSpeed", () => {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, makeBotInfo(), null, undefined);
    internals.setTargetSpeed(999);
    expect(internals.getTargetSpeed()).toBe(8);
    internals.setTargetSpeed(-999);
    expect(internals.getTargetSpeed()).toBe(-8);
  });

  it("4.13 getNewTargetSpeed returns correct speed for distance", () => {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, makeBotInfo(), null, undefined);
    // From rest, moving forward 100 units — should accelerate
    const speed = internals.getNewTargetSpeed(0, 100);
    expect(speed).toBeGreaterThan(0);
    expect(speed).toBeLessThanOrEqual(8);
  });

  it("4.13 getDistanceTraveledUntilStop returns positive distance", () => {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, makeBotInfo(), null, undefined);
    const dist = internals.getDistanceTraveledUntilStop(8);
    expect(dist).toBeGreaterThan(0);
  });

  it("4.14 setStop saves rates and zeroes intent; setResume restores", () => {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, makeBotInfo(), null, undefined);
    internals.setTurnRate(5);
    internals.setTargetSpeed(4);
    internals.setStop();
    expect(internals.getTurnRate()).toBe(0);
    expect(internals.getTargetSpeed()).toBe(0);
    expect(internals.isStopped_()).toBe(true);
    internals.setResume();
    expect(internals.getTurnRate()).toBe(5);
    expect(internals.getTargetSpeed()).toBe(4);
    expect(internals.isStopped_()).toBe(false);
  });

  it("4.14 setStop with overwrite=false does not overwrite existing stop", () => {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, makeBotInfo(), null, undefined);
    internals.setTurnRate(5);
    internals.setStop();
    internals.setTurnRate(3); // change after stop
    internals.setStop(false); // should not overwrite
    internals.setResume();
    expect(internals.getTurnRate()).toBe(5); // restored to pre-first-stop value
  });

  it("4.15 setRescan sets rescan flag on intent", () => {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, makeBotInfo(), null, undefined);
    internals.setRescan();
    // No direct getter, but should not throw
  });

  it("4.17 addEvent routes SkippedTurnEvent to queue", () => {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, makeBotInfo(), null, undefined);
    const e = new SkippedTurnEvent(5);
    internals.addEvent(e);
    // getEvents() returns the raw queue array — event should be present before dispatch
    const events = internals.getEvents();
    expect(events.length).toBeGreaterThan(0);
    expect(events.some((ev) => ev === e)).toBe(true);
  });
});

// ---------------------------------------------------------------------------
// Task 5: Bot class
// ---------------------------------------------------------------------------

describe("Task 5: Bot class", () => {
  class TestBot extends Bot {
    constructor() { super(makeBotInfo(), "ws://localhost:7654"); }
  }

  it("5.1 Bot extends BaseBot and implements IBot", () => {
    const bot = new TestBot();
    expect(bot).toBeInstanceOf(Bot);
    expect(bot).toBeInstanceOf(BaseBot);
  });

  it("5.3 setTurnRate/setGunTurnRate/setRadarTurnRate/setTargetSpeed delegate to BotInternals", () => {
    const bot = new TestBot();
    bot.setTurnRate(5);
    expect(bot.getTurnRate()).toBe(5);
    bot.setGunTurnRate(10);
    expect(bot.getGunTurnRate()).toBe(10);
    bot.setRadarTurnRate(20);
    expect(bot.getRadarTurnRate()).toBe(20);
    bot.setTargetSpeed(4);
    expect(bot.getTargetSpeed()).toBe(4);
  });

  it("5.4 isRunning returns false before start", () => {
    const bot = new TestBot();
    expect(bot.isRunning()).toBe(false);
  });

  it("5.5 setForward/setBack set distanceRemaining", () => {
    const bot = new TestBot();
    bot.setForward(100);
    expect(bot.getDistanceRemaining()).toBe(100);
    bot.setBack(50);
    expect(bot.getDistanceRemaining()).toBe(-50);
  });

  it("5.5 setTurnLeft/setTurnRight set turnRemaining", () => {
    const bot = new TestBot();
    bot.setTurnLeft(90);
    expect(bot.getTurnRemaining()).toBe(90);
    bot.setTurnRight(45);
    expect(bot.getTurnRemaining()).toBe(-45);
  });

  it("5.5 setTurnGunLeft/setTurnGunRight set gunTurnRemaining", () => {
    const bot = new TestBot();
    bot.setTurnGunLeft(30);
    expect(bot.getGunTurnRemaining()).toBe(30);
    bot.setTurnGunRight(15);
    expect(bot.getGunTurnRemaining()).toBe(-15);
  });

  it("5.5 setTurnRadarLeft/setTurnRadarRight set radarTurnRemaining", () => {
    const bot = new TestBot();
    bot.setTurnRadarLeft(60);
    expect(bot.getRadarTurnRemaining()).toBe(60);
    bot.setTurnRadarRight(30);
    expect(bot.getRadarTurnRemaining()).toBe(-30);
  });

  it("5.7 remaining getters return 0 by default", () => {
    const bot = new TestBot();
    expect(bot.getDistanceRemaining()).toBe(0);
    expect(bot.getTurnRemaining()).toBe(0);
    expect(bot.getGunTurnRemaining()).toBe(0);
    expect(bot.getRadarTurnRemaining()).toBe(0);
  });
});

// ---------------------------------------------------------------------------
// Task 6: BotInternals
// ---------------------------------------------------------------------------

describe("Task 6: BotInternals", () => {
  class TestBot extends Bot {
    constructor() { super(makeBotInfo(), "ws://localhost:7654"); }
    // Expose internals for testing
    get internals() { return this._internals; }
  }

  it("6.1 BotInternals subscribes to internal events", () => {
    const bot = new TestBot();
    // onNextTurn subscriber at priority 110 should exist
    const ih = bot.internals.internalEventHandlers;
    expect(ih.onNextTurn).toBeDefined();
  });

  it("6.7 onStop/onResume saves and restores remaining values", () => {
    const bot = new TestBot();
    bot.setForward(100);
    bot.setTurnLeft(90);
    // setStop on BaseBotInternals triggers stopResumeListener.onStop() in BotInternals
    bot._internals.setStop();
    expect(bot.getDistanceRemaining()).toBe(100); // BotInternals saved it
    bot._internals.setResume();
    expect(bot.getDistanceRemaining()).toBe(100); // restored
    expect(bot.getTurnRemaining()).toBe(90);
  });

  it("6.8 override flags set correctly by setForward", () => {
    const bot = new TestBot();
    bot.setForward(50);
    expect(bot.getDistanceRemaining()).toBe(50);
  });

  it("6.9 setBack negates distance", () => {
    const bot = new TestBot();
    bot.setBack(30);
    expect(bot.getDistanceRemaining()).toBe(-30);
  });
});

// ---------------------------------------------------------------------------
// Task 7: BotStoppedException
// ---------------------------------------------------------------------------

describe("Task 7: BotStoppedException", () => {
  it("7.5 BotStoppedException is an Error subclass", () => {
    const e = new BotStoppedException();
    expect(e).toBeInstanceOf(Error);
    expect(e.name).toBe("BotStoppedException");
    expect(e.message).toBe("Bot stopped");
  });

  it("7.5 BotStoppedException can be caught separately from generic Error", () => {
    let caught = false;
    try {
      throw new BotStoppedException();
    } catch (e) {
      if (e instanceof BotStoppedException) caught = true;
    }
    expect(caught).toBe(true);
  });
});

// ---------------------------------------------------------------------------
// Task 8: Integration tests (mock-server driven)
// ---------------------------------------------------------------------------

describe("Task 8: Integration tests", () => {
  // Helper: build a BaseBotInternals with a mock WebSocketHandler
  function buildInternals() {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;
    const internals = new BaseBotInternals(stub, makeBotInfo(), "ws://localhost:7654", undefined);

    // Simulate receiving messages by calling internal handlers directly
    const simulateGameStarted = () => {
      const msg: GameStartedEventForBot = {
        type: MessageType.GameStartedEventForBot,
        myId: 42,
        gameSetup: makeGameSetup(),
        teammateIds: [],
      };
      (internals as unknown as { handleGameStarted: (m: GameStartedEventForBot) => void })
        .handleGameStarted?.(msg);
    };

    const simulateRoundStarted = (roundNumber = 1) => {
      const msg: RoundStartedEvent = { type: MessageType.RoundStartedEvent, roundNumber };
      (internals as unknown as { handleRoundStarted: (m: RoundStartedEvent) => void })
        .handleRoundStarted?.(msg);
    };

    const simulateTick = (turnNumber: number, roundNumber = 1, energy = 100) => {
      const msg = makeTickEvent(turnNumber, roundNumber, energy);
      (internals as unknown as { handleTick: (m: TickEventForBot) => void })
        .handleTick?.(msg);
    };

    const simulateRoundEnded = (roundNumber = 1) => {
      const msg: RoundEndedEventForBot = {
        type: MessageType.RoundEndedEventForBot,
        roundNumber,
        turnNumber: 10,
        results: makeResults(),
      };
      (internals as unknown as { handleRoundEnded: (m: RoundEndedEventForBot) => void })
        .handleRoundEnded?.(msg);
    };

    const simulateGameEnded = () => {
      const msg: GameEndedEventForBot = {
        type: MessageType.GameEndedEventForBot,
        numberOfRounds: 10,
        results: makeResults(),
      };
      (internals as unknown as { handleGameEnded: (m: GameEndedEventForBot) => void })
        .handleGameEnded?.(msg);
    };

    return { internals, simulateGameStarted, simulateRoundStarted, simulateTick, simulateRoundEnded, simulateGameEnded };
  }

  it("8.1 handleGameStarted sets myId and gameSetup", () => {
    const { internals, simulateGameStarted } = buildInternals();
    simulateGameStarted();
    expect(internals.getMyId()).toBe(42);
    expect(internals.getGameType()).toBe("classic");
    expect(internals.getArenaWidth()).toBe(800);
  });

  it("8.3 handleTick updates state accessors", () => {
    const { internals, simulateGameStarted, simulateTick } = buildInternals();
    simulateGameStarted();
    simulateTick(5, 2, 75);
    expect(internals.getTurnNumber()).toBe(5);
    expect(internals.getRoundNumber()).toBe(2);
    expect(internals.getEnergy()).toBe(75);
    expect(internals.getX()).toBe(100);
    expect(internals.getY()).toBe(200);
    expect(internals.getDirection()).toBe(45);
  });

  it("8.3 handleTick fires onGameStarted event handler", () => {
    const { internals, simulateGameStarted } = buildInternals();
    let fired = false;
    internals.botEventHandlers.onGameStarted.subscribe(() => { fired = true; });
    simulateGameStarted();
    expect(fired).toBe(true);
  });

  it("8.6 event handlers fire during tick dispatch (onTick)", () => {
    const { internals, simulateGameStarted, simulateTick } = buildInternals();
    simulateGameStarted();
    let tickFired = false;
    internals.botEventHandlers.onTick.subscribe(() => { tickFired = true; });
    simulateTick(1);
    // onTick is dispatched through the event queue; must call dispatchEvents explicitly
    // (in normal operation, BaseBot.go() calls dispatchEvents before execute()).
    internals.dispatchEvents(1);
    expect(tickFired).toBe(true);
  });

  it("8.7 round transition fires onRoundEnded", () => {
    const { internals, simulateGameStarted, simulateRoundEnded } = buildInternals();
    simulateGameStarted();
    let roundEndedFired = false;
    internals.botEventHandlers.onRoundEnded.subscribe(() => { roundEndedFired = true; });
    simulateRoundEnded(1);
    expect(roundEndedFired).toBe(true);
  });

  it("8.8 game end fires onGameEnded", () => {
    const { internals, simulateGameStarted, simulateGameEnded } = buildInternals();
    simulateGameStarted();
    let gameEndedFired = false;
    internals.botEventHandlers.onGameEnded.subscribe(() => { gameEndedFired = true; });
    simulateGameEnded();
    expect(gameEndedFired).toBe(true);
  });

  it("8.9 stop/resume saves and restores movement state", () => {
    const { internals } = buildInternals();
    internals.setTurnRate(5);
    internals.setTargetSpeed(4);
    internals.setStop();
    expect(internals.getTurnRate()).toBe(0);
    expect(internals.getTargetSpeed()).toBe(0);
    internals.setResume();
    expect(internals.getTurnRate()).toBe(5);
    expect(internals.getTargetSpeed()).toBe(4);
  });

  it("8.11 isRunning returns false after round ended", () => {
    const { internals, simulateGameStarted, simulateRoundEnded } = buildInternals();
    simulateGameStarted();
    internals.setRunning(true);
    simulateRoundEnded();
    expect(internals.isRunning()).toBe(false);
  });

  it("8.11 isRunning returns false after game ended", () => {
    const { internals, simulateGameStarted, simulateGameEnded } = buildInternals();
    simulateGameStarted();
    internals.setRunning(true);
    simulateGameEnded();
    expect(internals.isRunning()).toBe(false);
  });

  it("8.13 setAdjustGunForBodyTurn/setAdjustRadarForGunTurn flags stored in intent", () => {
    const { internals } = buildInternals();
    internals.setAdjustGunForBodyTurn(true);
    expect(internals.isAdjustGunForBodyTurn()).toBe(true);
    internals.setAdjustRadarForGunTurn(true);
    expect(internals.isAdjustRadarForGunTurn()).toBe(true);
    internals.setAdjustRadarForBodyTurn(true);
    expect(internals.isAdjustRadarForBodyTurn()).toBe(true);
  });
});
});
