import { BotException } from "./BotException.js";
import { BotInfo } from "./BotInfo.js";
import { InitialPosition } from "./InitialPosition.js";
import { RuntimeAdapter } from "./runtime/RuntimeAdapter.js";

/**
 * Utility class for reading environment variables via a {@link RuntimeAdapter}.
 *
 * Environment variable names match the Java implementation exactly.
 */
export class EnvVars {
  static readonly SERVER_URL = "SERVER_URL";
  static readonly SERVER_SECRET = "SERVER_SECRET";
  static readonly BOT_NAME = "BOT_NAME";
  static readonly BOT_VERSION = "BOT_VERSION";
  static readonly BOT_AUTHORS = "BOT_AUTHORS";
  static readonly BOT_DESCRIPTION = "BOT_DESCRIPTION";
  static readonly BOT_HOMEPAGE = "BOT_HOMEPAGE";
  static readonly BOT_COUNTRY_CODES = "BOT_COUNTRY_CODES";
  static readonly BOT_GAME_TYPES = "BOT_GAME_TYPES";
  static readonly BOT_PLATFORM = "BOT_PLATFORM";
  static readonly BOT_PROG_LANG = "BOT_PROG_LANG";
  static readonly BOT_INITIAL_POS = "BOT_INITIAL_POS";
  static readonly TEAM_ID = "TEAM_ID";
  static readonly TEAM_NAME = "TEAM_NAME";
  static readonly TEAM_VERSION = "TEAM_VERSION";
  static readonly BOT_BOOTED = "BOT_BOOTED";

  private static readonly MISSING_ENV_VALUE = "Missing environment variable: ";

  private readonly adapter: RuntimeAdapter;

  constructor(adapter: RuntimeAdapter) {
    this.adapter = adapter;
  }

  /** Constructs a {@link BotInfo} from environment variables. Throws {@link BotException} if name, version, or authors are missing. */
  getBotInfo(): BotInfo {
    if (this.isBlankStr(this.getBotName())) {
      throw new BotException(EnvVars.MISSING_ENV_VALUE + EnvVars.BOT_NAME);
    }
    if (this.isBlankStr(this.getBotVersion())) {
      throw new BotException(EnvVars.MISSING_ENV_VALUE + EnvVars.BOT_VERSION);
    }
    if (this.isBlankList(this.getBotAuthors())) {
      throw new BotException(EnvVars.MISSING_ENV_VALUE + EnvVars.BOT_AUTHORS);
    }
    return new BotInfo(
      this.getBotName()!,
      this.getBotVersion()!,
      this.getBotAuthors(),
      this.getBotDescription(),
      this.getBotHomepage(),
      this.getBotCountryCodes(),
      this.getBotGameTypes(),
      this.getBotPlatform(),
      this.getBotProgrammingLang(),
      this.getBotInitialPosition(),
    );
  }

  /** Server URL */
  getServerUrl(): string | undefined {
    return this.adapter.getEnvVar(EnvVars.SERVER_URL);
  }

  /** Server secret */
  getServerSecret(): string | undefined {
    return this.adapter.getEnvVar(EnvVars.SERVER_SECRET);
  }

  /** Bot name */
  getBotName(): string | undefined {
    return this.adapter.getEnvVar(EnvVars.BOT_NAME);
  }

  /** Bot version */
  getBotVersion(): string | undefined {
    return this.adapter.getEnvVar(EnvVars.BOT_VERSION);
  }

  /** Bot author(s) — comma-separated list */
  getBotAuthors(): string[] {
    return this.propertyAsList(EnvVars.BOT_AUTHORS);
  }

  /** Bot description */
  getBotDescription(): string | null {
    return this.adapter.getEnvVar(EnvVars.BOT_DESCRIPTION) ?? null;
  }

  /** Bot homepage URL */
  getBotHomepage(): string | null {
    return this.adapter.getEnvVar(EnvVars.BOT_HOMEPAGE) ?? null;
  }

  /** Bot country code(s) — comma-separated list */
  getBotCountryCodes(): string[] {
    return this.propertyAsList(EnvVars.BOT_COUNTRY_CODES);
  }

  /** Set of game type(s) the bot supports — comma-separated list */
  getBotGameTypes(): string[] {
    return this.propertyAsList(EnvVars.BOT_GAME_TYPES);
  }

  /** Platform used for running the bot */
  getBotPlatform(): string | null {
    return this.adapter.getEnvVar(EnvVars.BOT_PLATFORM) ?? null;
  }

  /** Language used for programming the bot */
  getBotProgrammingLang(): string | null {
    return this.adapter.getEnvVar(EnvVars.BOT_PROG_LANG) ?? null;
  }

  /** Initial starting position used for debugging */
  getBotInitialPosition(): InitialPosition | null {
    return InitialPosition.fromString(this.adapter.getEnvVar(EnvVars.BOT_INITIAL_POS));
  }

  /** Bot team id — integer or null */
  getTeamId(): number | null {
    const value = this.adapter.getEnvVar(EnvVars.TEAM_ID);
    if (value === undefined) return null;
    const trimmed = value.trim();
    if (trimmed === "") return null;
    return parseInt(trimmed, 10);
  }

  /** Bot team name */
  getTeamName(): string | undefined {
    return this.adapter.getEnvVar(EnvVars.TEAM_NAME);
  }

  /** Bot team version */
  getTeamVersion(): string | undefined {
    return this.adapter.getEnvVar(EnvVars.TEAM_VERSION);
  }

  /** Returns true if the bot is being booted (env var is set). */
  isBotBooted(): boolean {
    return this.adapter.getEnvVar(EnvVars.BOT_BOOTED) !== undefined;
  }

  private isBlankStr(s: string | undefined | null): boolean {
    return s === undefined || s === null || s.trim() === "";
  }

  private isBlankList(list: string[]): boolean {
    return list.length === 0;
  }

  /** Splits a comma-separated env var value into a trimmed string array. */
  private propertyAsList(name: string): string[] {
    const value = this.adapter.getEnvVar(name);
    if (value === undefined || value.trim() === "") return [];
    return value.split(/\s*,\s*/);
  }
}
