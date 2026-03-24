import { describe, it, expect } from "vitest";
import { BotInfo } from "../src/BotInfo.js";

const NAME = "MyBot";
const VERSION = "1.0";
const AUTHORS = ["John Doe"];

describe("BotInfo constants", () => {
  it("has correct max lengths", () => {
    expect(BotInfo.MAX_NAME_LENGTH).toBe(30);
    expect(BotInfo.MAX_VERSION_LENGTH).toBe(20);
    expect(BotInfo.MAX_AUTHOR_LENGTH).toBe(50);
    expect(BotInfo.MAX_DESCRIPTION_LENGTH).toBe(250);
    expect(BotInfo.MAX_HOMEPAGE_LENGTH).toBe(150);
    expect(BotInfo.MAX_GAME_TYPE_LENGTH).toBe(20);
    expect(BotInfo.MAX_PLATFORM_LENGTH).toBe(30);
    expect(BotInfo.MAX_PROGRAMMING_LANG_LENGTH).toBe(30);
    expect(BotInfo.MAX_NUMBER_OF_AUTHORS).toBe(5);
    expect(BotInfo.MAX_NUMBER_OF_COUNTRY_CODES).toBe(5);
    expect(BotInfo.MAX_NUMBER_OF_GAME_TYPES).toBe(10);
  });
});

describe("BotInfo construction", () => {
  it("creates with required fields", () => {
    const bot = new BotInfo(NAME, VERSION, AUTHORS, null, null, [], [], null, null, null);
    expect(bot.name).toBe(NAME);
    expect(bot.version).toBe(VERSION);
    expect(bot.authors).toEqual(AUTHORS);
  });

  it("builder pattern works", () => {
    const bot = BotInfo.builder(NAME, VERSION, AUTHORS)
      .setDescription("A test bot")
      .setHomepage("https://example.com")
      .setCountryCodes(["US"])
      .setGameTypes(["classic"])
      .setPlatform("Node.js")
      .setProgrammingLang("TypeScript")
      .build();
    expect(bot.name).toBe(NAME);
    expect(bot.description).toBe("A test bot");
    expect(bot.homepage).toBe("https://example.com");
    expect(bot.countryCodes).toEqual(["US"]);
    expect(bot.gameTypes).toEqual(["classic"]);
    expect(bot.platform).toBe("Node.js");
    expect(bot.programmingLang).toBe("TypeScript");
  });
});

describe("BotInfo validation", () => {
  it("throws if name is blank", () => {
    expect(() => new BotInfo("", VERSION, AUTHORS, null, null, [], [], null, null, null)).toThrow("'name'");
    expect(() => new BotInfo("   ", VERSION, AUTHORS, null, null, [], [], null, null, null)).toThrow("'name'");
  });

  it("throws if name exceeds max length", () => {
    const longName = "a".repeat(31);
    expect(() => new BotInfo(longName, VERSION, AUTHORS, null, null, [], [], null, null, null)).toThrow("'name' length");
  });

  it("throws if version is blank", () => {
    expect(() => new BotInfo(NAME, "", AUTHORS, null, null, [], [], null, null, null)).toThrow("'version'");
  });

  it("throws if version exceeds max length", () => {
    const longVersion = "a".repeat(21);
    expect(() => new BotInfo(NAME, longVersion, AUTHORS, null, null, [], [], null, null, null)).toThrow("'version' length");
  });

  it("throws if authors is empty", () => {
    expect(() => new BotInfo(NAME, VERSION, [], null, null, [], [], null, null, null)).toThrow("'authors'");
  });

  it("throws if authors exceeds max count", () => {
    const tooMany = ["a", "b", "c", "d", "e", "f"];
    expect(() => new BotInfo(NAME, VERSION, tooMany, null, null, [], [], null, null, null)).toThrow("'authors'");
  });

  it("throws if author name exceeds max length", () => {
    const longAuthor = "a".repeat(51);
    expect(() => new BotInfo(NAME, VERSION, [longAuthor], null, null, [], [], null, null, null)).toThrow("'author' length");
  });

  it("throws if description exceeds max length", () => {
    const longDesc = "a".repeat(251);
    expect(() => new BotInfo(NAME, VERSION, AUTHORS, longDesc, null, [], [], null, null, null)).toThrow("'description' length");
  });

  it("throws if homepage exceeds max length", () => {
    const longUrl = "a".repeat(151);
    expect(() => new BotInfo(NAME, VERSION, AUTHORS, null, longUrl, [], [], null, null, null)).toThrow("'homepage' length");
  });

  it("throws if country codes exceed max count", () => {
    const codes = ["US", "GB", "DE", "FR", "JP", "AU"];
    expect(() => new BotInfo(NAME, VERSION, AUTHORS, null, null, codes, [], null, null, null)).toThrow("'countryCodes'");
  });

  it("silently drops invalid country codes", () => {
    const bot = new BotInfo(NAME, VERSION, AUTHORS, null, null, ["XX", "US"], [], null, null, null);
    expect(bot.countryCodes).toEqual(["US"]);
  });

  it("normalizes country codes to uppercase", () => {
    const bot = new BotInfo(NAME, VERSION, AUTHORS, null, null, ["us", "gb"], [], null, null, null);
    expect(bot.countryCodes).toEqual(["US", "GB"]);
  });

  it("throws if game types exceed max count", () => {
    const types = ["a","b","c","d","e","f","g","h","i","j","k"];
    expect(() => new BotInfo(NAME, VERSION, AUTHORS, null, null, [], types, null, null, null)).toThrow("'gameTypes'");
  });

  it("throws if game type exceeds max length", () => {
    const longType = "a".repeat(21);
    expect(() => new BotInfo(NAME, VERSION, AUTHORS, null, null, [], [longType], null, null, null)).toThrow("'gameTypes' length");
  });

  it("throws if platform exceeds max length", () => {
    const longPlatform = "a".repeat(31);
    expect(() => new BotInfo(NAME, VERSION, AUTHORS, null, null, [], [], longPlatform, null, null)).toThrow("'platform' length");
  });

  it("throws if programmingLang exceeds max length", () => {
    const longLang = "a".repeat(31);
    expect(() => new BotInfo(NAME, VERSION, AUTHORS, null, null, [], [], null, longLang, null)).toThrow("'programmingLang' length");
  });

  it("trims description, homepage, platform, programmingLang", () => {
    const bot = new BotInfo(NAME, VERSION, AUTHORS, "  desc  ", "  http://x.com  ", [], [], "  Node  ", "  TS  ", null);
    expect(bot.description).toBe("desc");
    expect(bot.homepage).toBe("http://x.com");
    expect(bot.platform).toBe("Node");
    expect(bot.programmingLang).toBe("TS");
  });

  it("returns null for blank optional fields", () => {
    const bot = new BotInfo(NAME, VERSION, AUTHORS, "  ", "  ", [], [], "  ", "  ", null);
    expect(bot.description).toBeNull();
    expect(bot.homepage).toBeNull();
    expect(bot.platform).toBeNull();
    expect(bot.programmingLang).toBeNull();
  });
});
