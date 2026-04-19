import { describe, it, expect, vi } from "vitest";
import { WebSocketHandler, WebSocketHandlerCallbacks } from "../src/WebSocketHandler.js";
import { BotHandshakeFactory } from "../src/BotHandshakeFactory.js";
import { BotInfo } from "../src/BotInfo.js";
import { EnvVars } from "../src/EnvVars.js";
import { RuntimeAdapter } from "../src/runtime/RuntimeAdapter.js";
import { WebSocketLike } from "../src/runtime/WebSocketLike.js";
import { MessageType } from "../src/protocol/MessageType.js";
import type {
  ServerHandshake,
  GameStartedEventForBot,
  TickEventForBot,
} from "../src/protocol/schema.js";


// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

function makeBotInfo(): BotInfo {
  return new BotInfo("TestBot", "1.0", ["Author"], null, null, [], [], null, null, null);
}

function makeEnvVars(overrides: Record<string, string> = {}): EnvVars {
  const adapter: RuntimeAdapter = {
    createWebSocket: vi.fn(),
    getEnvVar: (name: string) => overrides[name],
    exit: vi.fn(),
  };
  return new EnvVars(adapter);
}

/** Creates a mock WebSocketLike and captures the handlers set on it. */
function makeMockWs() {
  const sent: string[] = [];
  const ws: WebSocketLike = {
    onopen: null,
    onclose: null,
    onerror: null,
    onmessage: null,
    send: (data: string) => sent.push(data),
    close: vi.fn(),
    readyState: 1,
  };
  return { ws, sent };
}

function makeAdapter(ws: WebSocketLike): RuntimeAdapter {
  return {
    createWebSocket: vi.fn().mockReturnValue(ws),
    getEnvVar: vi.fn().mockReturnValue(undefined),
    exit: vi.fn(),
  };
}

function makeHandler(
  ws: WebSocketLike,
  callbacks: WebSocketHandlerCallbacks = {},
  botInfo?: BotInfo,
  envVars?: EnvVars,
): WebSocketHandler {
  const adapter = makeAdapter(ws);
  return new WebSocketHandler(
    adapter,
    "ws://localhost:7654",
    "secret",
    botInfo ?? makeBotInfo(),
    envVars ?? makeEnvVars(),
    callbacks,
  );
}

function simulateMessage(ws: WebSocketLike, msg: object): void {
  ws.onmessage?.({ data: JSON.stringify(msg) });
}

// ---------------------------------------------------------------------------
// connect() / disconnect()
// ---------------------------------------------------------------------------

describe("WebSocketHandler.connect()", () => {
  it("calls createWebSocket with the server URL", () => {
    const { ws } = makeMockWs();
    const adapter = makeAdapter(ws);
    const handler = new WebSocketHandler(adapter, "ws://localhost:7654", "secret", makeBotInfo(), makeEnvVars(), {});
    handler.connect();
    expect(adapter.createWebSocket).toHaveBeenCalledWith("ws://localhost:7654");
  });

  it("fires onConnected callback when socket opens", () => {
    const { ws } = makeMockWs();
    const onConnected = vi.fn();
    const handler = makeHandler(ws, { onConnected });
    handler.connect();
    ws.onopen?.(undefined);
    expect(onConnected).toHaveBeenCalledOnce();
  });

  it("fires onDisconnected callback when socket closes", () => {
    const { ws } = makeMockWs();
    const onDisconnected = vi.fn();
    const handler = makeHandler(ws, { onDisconnected });
    handler.connect();
    ws.onclose?.({ code: 1000, reason: "normal" });
    expect(onDisconnected).toHaveBeenCalledWith(true, 1000, "normal");
  });

  it("fires onConnectionError callback on socket error", () => {
    const { ws } = makeMockWs();
    const onConnectionError = vi.fn();
    const handler = makeHandler(ws, { onConnectionError });
    handler.connect();
    ws.onerror?.({ message: "err" });
    expect(onConnectionError).toHaveBeenCalledOnce();
  });
});

describe("WebSocketHandler.disconnect()", () => {
  it("calls close() on the socket", () => {
    const { ws } = makeMockWs();
    const handler = makeHandler(ws);
    handler.connect();
    handler.disconnect();
    expect(ws.close).toHaveBeenCalledOnce();
  });
});

// ---------------------------------------------------------------------------
// Message routing — ServerHandshake (4.3, 4.4)
// ---------------------------------------------------------------------------

describe("WebSocketHandler — ServerHandshake routing", () => {
  it("fires onServerHandshake callback", () => {
    const { ws } = makeMockWs();
    const onServerHandshake = vi.fn();
    const handler = makeHandler(ws, { onServerHandshake });
    handler.connect();
    const msg: ServerHandshake = {
      type: MessageType.ServerHandshake,
      sessionId: "sess-1",
      name: "Tank Royale",
      variant: "Tank Royale",
      version: "0.38.0",
      gameTypes: ["melee"],
    };
    simulateMessage(ws, msg);
    expect(onServerHandshake).toHaveBeenCalledWith(expect.objectContaining({ sessionId: "sess-1" }));
  });

  it("stores the server handshake", () => {
    const { ws } = makeMockWs();
    const handler = makeHandler(ws);
    handler.connect();
    const msg: ServerHandshake = {
      type: MessageType.ServerHandshake,
      sessionId: "sess-2",
      name: "Tank Royale",
      variant: "Tank Royale",
      version: "0.38.0",
      gameTypes: ["melee"],
    };
    simulateMessage(ws, msg);
    expect(handler.getServerHandshake()?.sessionId).toBe("sess-2");
  });
});

// ---------------------------------------------------------------------------
// Message routing — GameStarted (4.5)
// ---------------------------------------------------------------------------

describe("WebSocketHandler — GameStarted routing", () => {
  const gameStartedMsg: GameStartedEventForBot = {
    type: MessageType.GameStartedEventForBot,
    myId: 42,
    teammateIds: [],
    gameSetup: {
      gameType: "melee",
      arenaWidth: 800,
      arenaHeight: 600,
      numberOfRounds: 10,
      gunCoolingRate: 0.1,
      maxInactivityTurns: 450,
      turnTimeout: 30000,
      readyTimeout: 1000000,
    },
  };

  it("fires onGameStarted callback", () => {
    const { ws } = makeMockWs();
    const onGameStarted = vi.fn();
    const handler = makeHandler(ws, { onGameStarted });
    handler.connect();
    simulateMessage(ws, gameStartedMsg);
    expect(onGameStarted).toHaveBeenCalledWith(expect.objectContaining({ myId: 42 }));
  });
});

// ---------------------------------------------------------------------------
// Message routing — Tick (4.6)
// ---------------------------------------------------------------------------

describe("WebSocketHandler — Tick routing", () => {
  it("fires onTick callback", () => {
    const { ws } = makeMockWs();
    const onTick = vi.fn();
    const handler = makeHandler(ws, { onTick });
    handler.connect();
    const msg: TickEventForBot = {
      type: MessageType.TickEventForBot,
      turnNumber: 5,
      roundNumber: 1,
      botState: {
        isDroid: false,
        energy: 100,
        x: 50,
        y: 50,
        direction: 0,
        gunDirection: 0,
        radarDirection: 0,
        radarSweep: 0,
        speed: 0,
        turnRate: 0,
        gunTurnRate: 0,
        radarTurnRate: 0,
        gunHeat: 0,
        enemyCount: 0,
        isDebuggingEnabled: false,
      },
      bulletStates: [],
      events: [],
    };
    simulateMessage(ws, msg);
    expect(onTick).toHaveBeenCalledWith(expect.objectContaining({ turnNumber: 5, roundNumber: 1 }));
  });
});

// ---------------------------------------------------------------------------
// Message routing — RoundStarted / RoundEnded / GameEnded / GameAborted / SkippedTurn (4.7)
// ---------------------------------------------------------------------------

describe("WebSocketHandler — round/game/skipped routing", () => {
  it("fires onGameAborted", () => {
    const { ws } = makeMockWs();
    const onGameAborted = vi.fn();
    const handler = makeHandler(ws, { onGameAborted });
    handler.connect();
    simulateMessage(ws, { type: MessageType.GameAbortedEvent });
    expect(onGameAborted).toHaveBeenCalledOnce();
  });
});

// ---------------------------------------------------------------------------
// sendBotIntent (4.8)
// ---------------------------------------------------------------------------

describe("WebSocketHandler.sendBotIntent()", () => {
  it("sends JSON with BotIntent type", () => {
    const { ws, sent } = makeMockWs();
    const handler = makeHandler(ws);
    handler.connect();
    handler.sendBotIntent({ type: MessageType.BotIntent, turnRate: 5, firepower: 1 });
    expect(sent).toHaveLength(1);
    const parsed = JSON.parse(sent[0]) as { type: string; turnRate: number };
    expect(parsed.type).toBe(MessageType.BotIntent);
    expect(parsed.turnRate).toBe(5);
  });
});

// ---------------------------------------------------------------------------
// BotHandshakeFactory tests (task 5.2)
// ---------------------------------------------------------------------------

describe("BotHandshakeFactory.create()", () => {
  it("sets isDroid=true when specified", () => {
    const botInfo = makeBotInfo();
    const handshake = BotHandshakeFactory.create("s", botInfo, true, undefined, makeEnvVars());
    expect(handshake.isDroid).toBe(true);
  });

  it("omits optional fields when BotInfo has no values", () => {
    const botInfo = new BotInfo("Bot", "1.0", ["A"], null, null, [], [], null, null, null);
    const handshake = BotHandshakeFactory.create("s", botInfo, false, undefined, makeEnvVars());
    expect(handshake.description).toBeUndefined();
    expect(handshake.homepage).toBeUndefined();
    expect(handshake.countryCodes).toBeUndefined();
    expect(handshake.gameTypes).toBeUndefined();
    expect(handshake.platform).toBeUndefined();
    expect(handshake.programmingLang).toBeUndefined();
    expect(handshake.initialPosition).toBeUndefined();
    expect(handshake.teamId).toBeUndefined();
    expect(handshake.teamName).toBeUndefined();
    expect(handshake.teamVersion).toBeUndefined();
  });

  it("populates optional fields when BotInfo has values", () => {
    const botInfo = new BotInfo(
      "Bot", "1.0", ["A"],
      "A description", "https://example.com",
      ["GB", "US"], ["melee"],
      "Node.js 20", "TypeScript 5",
      null,
    );
    const handshake = BotHandshakeFactory.create("s", botInfo, false, undefined, makeEnvVars());
    expect(handshake.description).toBe("A description");
    expect(handshake.homepage).toBe("https://example.com");
    expect(handshake.countryCodes).toEqual(["GB", "US"]);
    expect(handshake.gameTypes).toEqual(["melee"]);
    expect(handshake.platform).toBe("Node.js 20");
    expect(handshake.programmingLang).toBe("TypeScript 5");
  });

  it("reads team fields from EnvVars", () => {
    const envVars = makeEnvVars({ TEAM_ID: "7", TEAM_NAME: "Alpha", TEAM_VERSION: "1.0" });
    const handshake = BotHandshakeFactory.create("s", makeBotInfo(), false, undefined, envVars);
    expect(handshake.teamId).toBe(7);
    expect(handshake.teamName).toBe("Alpha");
    expect(handshake.teamVersion).toBe("1.0");
  });

  it("debuggerAttached is false when ROBOCODE_DEBUG is not set", () => {
    const envVars = makeEnvVars();
    const handshake = BotHandshakeFactory.create("s", makeBotInfo(), false, undefined, envVars);
    expect(handshake.debuggerAttached).toBe(false);
  });

  it("debuggerAttached is true when ROBOCODE_DEBUG=true", () => {
    const envVars = makeEnvVars({ ROBOCODE_DEBUG: "true" });
    const handshake = BotHandshakeFactory.create("s", makeBotInfo(), false, undefined, envVars);
    expect(handshake.debuggerAttached).toBe(true);
  });

  it("debuggerAttached is false when ROBOCODE_DEBUG=false", () => {
    const envVars = makeEnvVars({ ROBOCODE_DEBUG: "false" });
    const handshake = BotHandshakeFactory.create("s", makeBotInfo(), false, undefined, envVars);
    expect(handshake.debuggerAttached).toBe(false);
  });
});
