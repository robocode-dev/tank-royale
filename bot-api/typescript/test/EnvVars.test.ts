import { describe, it, expect } from "vitest";
import { EnvVars } from "../src/EnvVars.js";
import { BotException } from "../src/BotException.js";
import { RuntimeAdapter } from "../src/runtime/RuntimeAdapter.js";
import { WebSocketLike } from "../src/runtime/WebSocketLike.js";

/** Creates a mock RuntimeAdapter backed by a plain object map. */
function makeAdapter(env: Record<string, string | undefined>): RuntimeAdapter {
  return {
    getEnvVar: (name: string) => env[name],
    createWebSocket: (_url: string): WebSocketLike => { throw new Error("not implemented"); },
    exit: (_code: number) => {},
  };
}

// ---------------------------------------------------------------------------
// Individual getters
// ---------------------------------------------------------------------------

describe("EnvVars — individual getters", () => {
  it("getServerUrl returns the env value", () => {
    const ev = new EnvVars(makeAdapter({ SERVER_URL: "ws://localhost:7654" }));
    expect(ev.getServerUrl()).toBe("ws://localhost:7654");
  });

  it("getServerUrl returns undefined when missing", () => {
    const ev = new EnvVars(makeAdapter({}));
    expect(ev.getServerUrl()).toBeUndefined();
  });

  it("getServerSecret returns the env value", () => {
    const ev = new EnvVars(makeAdapter({ SERVER_SECRET: "s3cr3t" }));
    expect(ev.getServerSecret()).toBe("s3cr3t");
  });

  it("getBotName returns the env value", () => {
    const ev = new EnvVars(makeAdapter({ BOT_NAME: "MyBot" }));
    expect(ev.getBotName()).toBe("MyBot");
  });

  it("getBotVersion returns the env value", () => {
    const ev = new EnvVars(makeAdapter({ BOT_VERSION: "2.0" }));
    expect(ev.getBotVersion()).toBe("2.0");
  });

  it("getBotDescription returns null when missing", () => {
    const ev = new EnvVars(makeAdapter({}));
    expect(ev.getBotDescription()).toBeNull();
  });

  it("getBotHomepage returns null when missing", () => {
    const ev = new EnvVars(makeAdapter({}));
    expect(ev.getBotHomepage()).toBeNull();
  });

  it("getBotPlatform returns null when missing", () => {
    const ev = new EnvVars(makeAdapter({}));
    expect(ev.getBotPlatform()).toBeNull();
  });

  it("getBotProgrammingLang returns null when missing", () => {
    const ev = new EnvVars(makeAdapter({}));
    expect(ev.getBotProgrammingLang()).toBeNull();
  });

  it("isBotBooted returns false when BOT_BOOTED is absent", () => {
    const ev = new EnvVars(makeAdapter({}));
    expect(ev.isBotBooted()).toBe(false);
  });

  it("isBotBooted returns true when BOT_BOOTED is set", () => {
    const ev = new EnvVars(makeAdapter({ BOT_BOOTED: "1" }));
    expect(ev.isBotBooted()).toBe(true);
  });
});

// ---------------------------------------------------------------------------
// Comma-separated list parsing (Task 3.3)
// ---------------------------------------------------------------------------

describe("EnvVars — comma-separated list parsing", () => {
  it("getBotAuthors splits on comma", () => {
    const ev = new EnvVars(makeAdapter({ BOT_AUTHORS: "Alice,Bob,Carol" }));
    expect(ev.getBotAuthors()).toEqual(["Alice", "Bob", "Carol"]);
  });

  it("getBotAuthors trims whitespace around commas", () => {
    const ev = new EnvVars(makeAdapter({ BOT_AUTHORS: "Alice , Bob , Carol" }));
    expect(ev.getBotAuthors()).toEqual(["Alice", "Bob", "Carol"]);
  });

  it("getBotAuthors returns empty array when missing", () => {
    const ev = new EnvVars(makeAdapter({}));
    expect(ev.getBotAuthors()).toEqual([]);
  });

  it("getBotAuthors returns empty array for blank value", () => {
    const ev = new EnvVars(makeAdapter({ BOT_AUTHORS: "   " }));
    expect(ev.getBotAuthors()).toEqual([]);
  });

  it("getBotCountryCodes splits on comma", () => {
    const ev = new EnvVars(makeAdapter({ BOT_COUNTRY_CODES: "US,GB,DE" }));
    expect(ev.getBotCountryCodes()).toEqual(["US", "GB", "DE"]);
  });

  it("getBotGameTypes splits on comma", () => {
    const ev = new EnvVars(makeAdapter({ BOT_GAME_TYPES: "classic , melee" }));
    expect(ev.getBotGameTypes()).toEqual(["classic", "melee"]);
  });
});

// ---------------------------------------------------------------------------
// getTeamId (Task 3.4)
// ---------------------------------------------------------------------------

describe("EnvVars — getTeamId", () => {
  it("returns null when TEAM_ID is absent", () => {
    const ev = new EnvVars(makeAdapter({}));
    expect(ev.getTeamId()).toBeNull();
  });

  it("returns null when TEAM_ID is blank", () => {
    const ev = new EnvVars(makeAdapter({ TEAM_ID: "  " }));
    expect(ev.getTeamId()).toBeNull();
  });

  it("parses integer value", () => {
    const ev = new EnvVars(makeAdapter({ TEAM_ID: "42" }));
    expect(ev.getTeamId()).toBe(42);
  });

  it("trims whitespace before parsing", () => {
    const ev = new EnvVars(makeAdapter({ TEAM_ID: "  7  " }));
    expect(ev.getTeamId()).toBe(7);
  });
});

// ---------------------------------------------------------------------------
// getBotInitialPosition
// ---------------------------------------------------------------------------

describe("EnvVars — getBotInitialPosition", () => {
  it("returns null when BOT_INITIAL_POS is absent", () => {
    const ev = new EnvVars(makeAdapter({}));
    expect(ev.getBotInitialPosition()).toBeNull();
  });

  it("parses x,y,direction", () => {
    const ev = new EnvVars(makeAdapter({ BOT_INITIAL_POS: "100,200,90" }));
    const pos = ev.getBotInitialPosition();
    expect(pos?.x).toBe(100);
    expect(pos?.y).toBe(200);
    expect(pos?.direction).toBe(90);
  });
});

// ---------------------------------------------------------------------------
// getBotInfo (Task 3.2)
// ---------------------------------------------------------------------------

describe("EnvVars — getBotInfo", () => {
  const fullEnv: Record<string, string> = {
    BOT_NAME: "MyBot",
    BOT_VERSION: "1.0",
    BOT_AUTHORS: "Alice,Bob",
    BOT_DESCRIPTION: "A test bot",
    BOT_HOMEPAGE: "https://example.com",
    BOT_COUNTRY_CODES: "US,GB",
    BOT_GAME_TYPES: "classic",
    BOT_PLATFORM: "Node.js",
    BOT_PROG_LANG: "TypeScript",
  };

  it("constructs BotInfo from full env", () => {
    const ev = new EnvVars(makeAdapter(fullEnv));
    const info = ev.getBotInfo();
    expect(info.name).toBe("MyBot");
    expect(info.version).toBe("1.0");
    expect(info.authors).toEqual(["Alice", "Bob"]);
    expect(info.description).toBe("A test bot");
    expect(info.homepage).toBe("https://example.com");
    expect(info.countryCodes).toEqual(["US", "GB"]);
    expect(info.gameTypes).toEqual(["classic"]);
    expect(info.platform).toBe("Node.js");
    expect(info.programmingLang).toBe("TypeScript");
  });

  it("throws BotException when BOT_NAME is missing", () => {
    const env = { ...fullEnv };
    delete (env as Record<string, string | undefined>)["BOT_NAME"];
    const ev = new EnvVars(makeAdapter(env));
    expect(() => ev.getBotInfo()).toThrow(BotException);
    expect(() => ev.getBotInfo()).toThrow("Missing environment variable: BOT_NAME");
  });

  it("throws BotException when BOT_NAME is blank", () => {
    const ev = new EnvVars(makeAdapter({ ...fullEnv, BOT_NAME: "  " }));
    expect(() => ev.getBotInfo()).toThrow(BotException);
  });

  it("throws BotException when BOT_VERSION is missing", () => {
    const env = { ...fullEnv };
    delete (env as Record<string, string | undefined>)["BOT_VERSION"];
    const ev = new EnvVars(makeAdapter(env));
    expect(() => ev.getBotInfo()).toThrow(BotException);
    expect(() => ev.getBotInfo()).toThrow("Missing environment variable: BOT_VERSION");
  });

  it("throws BotException when BOT_AUTHORS is missing", () => {
    const env = { ...fullEnv };
    delete (env as Record<string, string | undefined>)["BOT_AUTHORS"];
    const ev = new EnvVars(makeAdapter(env));
    expect(() => ev.getBotInfo()).toThrow(BotException);
    expect(() => ev.getBotInfo()).toThrow("Missing environment variable: BOT_AUTHORS");
  });

  it("throws BotException when BOT_AUTHORS is blank", () => {
    const ev = new EnvVars(makeAdapter({ ...fullEnv, BOT_AUTHORS: "  " }));
    expect(() => ev.getBotInfo()).toThrow(BotException);
  });
});
