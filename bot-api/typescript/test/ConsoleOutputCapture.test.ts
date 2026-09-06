/**
 * Integration tests wiring ConsoleCapture into BaseBotInternals per IDR-005 (P-005/M-017,
 * TBA-132..TBA-136). Mirrors the direct-private-access pattern used in BotLifecycle.test.ts.
 */
import { describe, it, expect, vi, afterEach } from "vitest";
import { BaseBotInternals } from "../src/internal/BaseBotInternals.js";
import { ConsoleCapture } from "../src/internal/ConsoleCapture.js";
import { BotInfo } from "../src/BotInfo.js";
import { MessageType } from "../src/protocol/MessageType.js";
import type {
  GameStartedEventForBot,
  RoundStartedEvent,
  RoundEndedEventForBot,
  TickEventForBot,
} from "../src/protocol/schema.js";

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

function makeResults() {
  return {
    rank: 1, survival: 0, lastSurvivorBonus: 0, bulletDamage: 0, bulletKillBonus: 0,
    ramDamage: 0, ramKillBonus: 0, totalScore: 0, firstPlaces: 0, secondPlaces: 0, thirdPlaces: 0,
  };
}

function makeTickEvent(turnNumber: number, roundNumber = 1): TickEventForBot {
  return {
    type: MessageType.TickEventForBot,
    turnNumber,
    roundNumber,
    botState: {
      isDroid: false, energy: 100, x: 0, y: 0, direction: 0, gunDirection: 0, radarDirection: 0,
      radarSweep: 0, speed: 0, turnRate: 0, gunTurnRate: 0, radarTurnRate: 0, gunHeat: 0, enemyCount: 1,
      bodyColor: null, turretColor: null, radarColor: null, bulletColor: null, scanColor: null,
      tracksColor: null, gunColor: null, isDebuggingEnabled: false,
    } as unknown as TickEventForBot["botState"],
    bulletStates: [],
    events: [],
  };
}

type Internals = BaseBotInternals & {
  consoleCapture: ConsoleCapture;
  intent: { stdOut?: string | null; stdErr?: string | null };
  wsHandler: { sendBotIntent: (i: unknown) => void } | null;
  handleGameStarted: (m: GameStartedEventForBot) => void;
  handleRoundStarted: (m: RoundStartedEvent) => void;
  handleTick: (m: TickEventForBot) => void;
  handleRoundEnded: (m: RoundEndedEventForBot) => void;
  sendIntentDirect: () => void;
};

function buildInternals() {
  const stub = {} as import("../src/IBaseBot.js").IBaseBot;
  const internals = new BaseBotInternals(stub, makeBotInfo(), "ws://localhost:7654", undefined) as Internals;
  const sentIntents: Record<string, unknown>[] = [];
  internals.wsHandler = { sendBotIntent: (i) => sentIntents.push(i as Record<string, unknown>) };

  internals.handleGameStarted({
    type: MessageType.GameStartedEventForBot,
    myId: 1,
    gameSetup: makeGameSetup(),
    teammateIds: [],
  });
  internals.handleRoundStarted({ type: MessageType.RoundStartedEvent, roundNumber: 1 });

  return { internals, sentIntents };
}

describe("Console output capture wired into BaseBotInternals", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("TBA-132/133: capture drains into the intent sent for the turn, then clears", () => {
    const { internals, sentIntents } = buildInternals();
    vi.spyOn(console, "log").mockImplementation(() => {});
    vi.spyOn(console, "error").mockImplementation(() => {});

    internals.consoleCapture.install();
    console.log("turn one output");
    console.error("turn one problem");
    internals.sendIntentDirect();

    expect(sentIntents).toHaveLength(1);
    expect(sentIntents[0].stdOut).toContain("turn one output");
    expect(sentIntents[0].stdErr).toContain("turn one problem");

    // Nothing new was logged — the next intent must not resend stale output.
    internals.sendIntentDirect();
    expect(sentIntents[1].stdOut).toBeUndefined();
    expect(sentIntents[1].stdErr).toBeUndefined();

    internals.consoleCapture.restore();
  });

  it("TBA-135: round-end residual output rides on the next round's first intent", () => {
    const { internals, sentIntents } = buildInternals();
    vi.spyOn(console, "log").mockImplementation(() => {});

    internals.consoleCapture.install();
    internals.handleTick(makeTickEvent(10));

    // A final-tick handler (e.g. onWonRound) logging after the round's last intent already sent.
    internals.botEventHandlers.onRoundEnded.subscribe(() => {
      console.log("won the round");
    });
    internals.handleRoundEnded({
      type: MessageType.RoundEndedEventForBot,
      roundNumber: 1,
      turnNumber: 10,
      results: makeResults(),
    });

    // Simulate round 2 starting and its first intent going out.
    internals.handleRoundStarted({ type: MessageType.RoundStartedEvent, roundNumber: 2 });
    internals.sendIntentDirect();

    const lastIntent = sentIntents[sentIntents.length - 1];
    expect(lastIntent.stdOut).toContain("won the round");

    internals.consoleCapture.restore();
  });

  it("TBA-134: capture installs on the no-Worker fallback thread, not on a worker-spawning main thread", () => {
    const stub = {} as import("../src/IBaseBot.js").IBaseBot;

    // No worker_threads available (browser or old Node): bot.run() executes on this thread.
    const legacy = new BaseBotInternals(stub, makeBotInfo(), null, undefined) as unknown as {
      startAsMain: (wt: unknown) => void;
      connect: () => void;
      consoleCapture: ConsoleCapture;
    };
    vi.spyOn(legacy, "connect").mockImplementation(() => {});
    legacy.startAsMain(null);
    expect((legacy.consoleCapture as unknown as { installed: boolean }).installed).toBe(true);
    legacy.consoleCapture.restore();

    // A Worker is spawned: bot.run() executes there, not on this main thread.
    const spawning = new BaseBotInternals(stub, makeBotInfo(), null, undefined) as unknown as {
      startAsMain: (wt: unknown) => void;
      connect: () => void;
      consoleCapture: ConsoleCapture;
    };
    vi.spyOn(spawning, "connect").mockImplementation(() => {});
    const fakePort = { on: vi.fn(), postMessage: vi.fn() };
    const fakeWorker = { on: vi.fn() };
    const fakeWt = {
      MessageChannel: vi.fn().mockImplementation(() => ({ port1: fakePort, port2: {} })),
      Worker: vi.fn().mockImplementation(() => fakeWorker),
    };
    spawning.startAsMain(fakeWt);
    expect((spawning.consoleCapture as unknown as { installed: boolean }).installed).toBe(false);
  });

  it("TBA-136: capture is restored when the game ends", () => {
    const { internals } = buildInternals();
    vi.spyOn(console, "log").mockImplementation(() => {});
    const originalLog = console.log;

    internals.consoleCapture.install();
    expect(console.log).not.toBe(originalLog);

    (internals as unknown as { processGameEnded: (m: unknown) => void }).processGameEnded({
      type: MessageType.GameEndedEventForBot,
      numberOfRounds: 10,
      results: makeResults(),
    });

    expect(console.log).toBe(originalLog);
  });
});
