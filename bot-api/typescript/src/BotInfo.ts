import { InitialPosition } from "./InitialPosition.js";

// ISO 3166-1 alpha-2 country codes (uppercase)
const VALID_COUNTRY_CODES = new Set([
  "AF","AX","AL","DZ","AS","AD","AO","AI","AQ","AG","AR","AM","AW","AU","AT","AZ",
  "BS","BH","BD","BB","BY","BE","BZ","BJ","BM","BT","BO","BQ","BA","BW","BV","BR",
  "IO","BN","BG","BF","BI","CV","KH","CM","CA","KY","CF","TD","CL","CN","CX","CC",
  "CO","KM","CG","CD","CK","CR","CI","HR","CU","CW","CY","CZ","DK","DJ","DM","DO",
  "EC","EG","SV","GQ","ER","EE","SZ","ET","FK","FO","FJ","FI","FR","GF","PF","TF",
  "GA","GM","GE","DE","GH","GI","GR","GL","GD","GP","GU","GT","GG","GN","GW","GY",
  "HT","HM","VA","HN","HK","HU","IS","IN","ID","IR","IQ","IE","IM","IL","IT","JM",
  "JP","JE","JO","KZ","KE","KI","KP","KR","KW","KG","LA","LV","LB","LS","LR","LY",
  "LI","LT","LU","MO","MG","MW","MY","MV","ML","MT","MH","MQ","MR","MU","YT","MX",
  "FM","MD","MC","MN","ME","MS","MA","MZ","MM","NA","NR","NP","NL","NC","NZ","NI",
  "NE","NG","NU","NF","MK","MP","NO","OM","PK","PW","PS","PA","PG","PY","PE","PH",
  "PN","PL","PT","PR","QA","RE","RO","RU","RW","BL","SH","KN","LC","MF","PM","VC",
  "WS","SM","ST","SA","SN","RS","SC","SL","SG","SX","SK","SI","SB","SO","ZA","GS",
  "SS","ES","LK","SD","SR","SJ","SE","CH","SY","TW","TJ","TZ","TH","TL","TG","TK",
  "TO","TT","TN","TR","TM","TC","TV","UG","UA","AE","GB","US","UM","UY","UZ","VU",
  "VE","VN","VG","VI","WF","EH","YE","ZM","ZW",
]);

export class BotInfo {
  static readonly MAX_NAME_LENGTH = 30;
  static readonly MAX_VERSION_LENGTH = 20;
  static readonly MAX_AUTHOR_LENGTH = 50;
  static readonly MAX_DESCRIPTION_LENGTH = 250;
  static readonly MAX_HOMEPAGE_LENGTH = 150;
  static readonly MAX_GAME_TYPE_LENGTH = 20;
  static readonly MAX_PLATFORM_LENGTH = 30;
  static readonly MAX_PROGRAMMING_LANG_LENGTH = 30;
  static readonly MAX_NUMBER_OF_AUTHORS = 20;
  static readonly MAX_NUMBER_OF_COUNTRY_CODES = 20;
  static readonly MAX_NUMBER_OF_GAME_TYPES = 10;

  readonly name: string | null;
  readonly version: string | null;
  readonly authors: string[] | null;
  readonly description: string | null;
  readonly homepage: string | null;
  readonly countryCodes: string[];
  readonly gameTypes: string[];
  readonly platform: string | null;
  readonly programmingLang: string | null;
  readonly initialPosition: InitialPosition | null;

  constructor(
    name: string | null | undefined,
    version: string | null | undefined,
    authors: string[] | null | undefined,
    description: string | null,
    homepage: string | null,
    countryCodes: string[],
    gameTypes: string[],
    platform: string | null,
    programmingLang: string | null,
    initialPosition: InitialPosition | null,
  ) {
    this.name = BotInfo.processName(name);
    this.version = BotInfo.processVersion(version);
    this.authors = BotInfo.processAuthors(authors);
    this.description = BotInfo.validateDescription(description);
    this.homepage = BotInfo.validateHomepage(homepage);
    this.countryCodes = BotInfo.processCountryCodes(countryCodes);
    this.gameTypes = BotInfo.validateGameTypes(gameTypes);
    this.platform = BotInfo.validatePlatform(platform);
    this.programmingLang = BotInfo.validateProgrammingLang(programmingLang);
    this.initialPosition = initialPosition ?? null;
  }

  static builder(name: string | null, version: string | null, authors: string[] | null): BotInfoBuilder {
    return new BotInfoBuilder(name, version, authors);
  }

  static fromJson(json: string): BotInfo {
    const d = JSON.parse(json) as Record<string, unknown>;
    return new BotInfo(
      d["name"] as string,
      d["version"] as string,
      (d["authors"] as string[]) ?? [],
      (d["description"] as string | null) ?? null,
      (d["homepage"] as string | null) ?? null,
      (d["countryCodes"] as string[]) ?? [],
      (d["gameTypes"] as string[]) ?? [],
      (d["platform"] as string | null) ?? null,
      (d["programmingLang"] as string | null) ?? null,
      null,
    );
  }

  private static processName(name: string | null | undefined): string | null {
    if (name == null || name.trim() === "") return null;
    if (name.length > BotInfo.MAX_NAME_LENGTH) {
      throw new Error(`'name' length exceeds ${BotInfo.MAX_NAME_LENGTH} characters: ${name.length}`);
    }
    return name.trim();
  }

  private static processVersion(version: string | null | undefined): string | null {
    if (version == null || version.trim() === "") return null;
    if (version.length > BotInfo.MAX_VERSION_LENGTH) {
      throw new Error(`'version' length exceeds ${BotInfo.MAX_VERSION_LENGTH} characters: ${version.length}`);
    }
    return version.trim();
  }

  private static processAuthors(authors: string[] | null | undefined): string[] | null {
    if (authors == null || authors.length === 0) return null;
    const trimmed = authors.map((a) => a?.trim() ?? "").filter((a) => a !== "");
    if (trimmed.length === 0) return null;
    if (trimmed.length > BotInfo.MAX_NUMBER_OF_AUTHORS) {
      throw new Error(`Size of 'authors' exceeds the maximum: ${BotInfo.MAX_NUMBER_OF_AUTHORS}`);
    }
    for (const author of trimmed) {
      if (author.length > BotInfo.MAX_AUTHOR_LENGTH) {
        throw new Error(`'author' length exceeds ${BotInfo.MAX_AUTHOR_LENGTH} characters: ${author.length}`);
      }
    }
    return trimmed;
  }

  private static validateDescription(description: string | null | undefined): string | null {
    if (description == null || description.trim() === "") return null;
    if (description.trim().length > BotInfo.MAX_DESCRIPTION_LENGTH) {
      throw new Error(`'description' length exceeds the maximum of ${BotInfo.MAX_DESCRIPTION_LENGTH} characters`);
    }
    return description.trim();
  }

  private static validateHomepage(homepage: string | null | undefined): string | null {
    if (homepage == null || homepage.trim() === "") return null;
    if (homepage.trim().length > BotInfo.MAX_HOMEPAGE_LENGTH) {
      throw new Error(`'homepage' length exceeds the maximum of ${BotInfo.MAX_HOMEPAGE_LENGTH} characters`);
    }
    return homepage.trim();
  }

  private static processCountryCodes(countryCodeStrings: string[]): string[] {
    const result: string[] = [];
    if (countryCodeStrings != null) {
      for (const code of countryCodeStrings) {
        if (code == null) continue;
        const upper = code.trim().toUpperCase();
        if (VALID_COUNTRY_CODES.has(upper)) {
          result.push(upper);
        }
      }
    }
    if (result.length > BotInfo.MAX_NUMBER_OF_COUNTRY_CODES) {
      throw new Error(`Size of 'countryCodes' exceeds the maximum of ${BotInfo.MAX_NUMBER_OF_COUNTRY_CODES}`);
    }
    return result;
  }

  private static validateGameTypes(gameTypes: string[]): string[] {
    if (gameTypes == null || gameTypes.length === 0 || gameTypes.every((g) => g == null || g.trim() === "")) {
      return [];
    }
    if (gameTypes.length > BotInfo.MAX_NUMBER_OF_GAME_TYPES) {
      throw new Error(`Size of 'gameTypes' exceeds the maximum of ${BotInfo.MAX_NUMBER_OF_GAME_TYPES}`);
    }
    for (const gameType of gameTypes) {
      if (gameType.length > BotInfo.MAX_GAME_TYPE_LENGTH) {
        throw new Error(`'gameTypes' length exceeds the maximum of ${BotInfo.MAX_GAME_TYPE_LENGTH} characters`);
      }
    }
    return gameTypes.filter((g) => g != null && g.trim() !== "");
  }

  private static validatePlatform(platform: string | null | undefined): string | null {
    if (platform == null || platform.trim() === "") return null;
    if (platform.trim().length > BotInfo.MAX_PLATFORM_LENGTH) {
      throw new Error(`'platform' length exceeds the maximum of ${BotInfo.MAX_PLATFORM_LENGTH} characters`);
    }
    return platform.trim();
  }

  private static validateProgrammingLang(programmingLang: string | null | undefined): string | null {
    if (programmingLang == null || programmingLang.trim() === "") return null;
    if (programmingLang.trim().length > BotInfo.MAX_PROGRAMMING_LANG_LENGTH) {
      throw new Error(`'programmingLang' length exceeds the maximum of ${BotInfo.MAX_PROGRAMMING_LANG_LENGTH} characters`);
    }
    return programmingLang.trim();
  }
}

export class BotInfoBuilder {
  private _name: string | null;
  private _version: string | null;
  private _authors: string[] | null;
  private _description: string | null = null;
  private _homepage: string | null = null;
  private _countryCodes: string[] = [];
  private _gameTypes: string[] = [];
  private _platform: string | null = null;
  private _programmingLang: string | null = null;
  private _initialPosition: InitialPosition | null = null;

  constructor(name: string | null, version: string | null, authors: string[] | null) {
    this._name = name;
    this._version = version;
    this._authors = authors;
  }

  setDescription(description: string | null): this {
    this._description = description;
    return this;
  }

  setHomepage(homepage: string | null): this {
    this._homepage = homepage;
    return this;
  }

  setCountryCodes(countryCodes: string[]): this {
    this._countryCodes = countryCodes;
    return this;
  }

  addCountryCode(countryCode: string): this {
    this._countryCodes.push(countryCode);
    return this;
  }

  setGameTypes(gameTypes: string[]): this {
    this._gameTypes = gameTypes;
    return this;
  }

  setPlatform(platform: string | null): this {
    this._platform = platform;
    return this;
  }

  setProgrammingLang(programmingLang: string | null): this {
    this._programmingLang = programmingLang;
    return this;
  }

  setInitialPosition(initialPosition: InitialPosition | null): this {
    this._initialPosition = initialPosition;
    return this;
  }

  build(): BotInfo {
    return new BotInfo(
      this._name,
      this._version,
      this._authors,
      this._description,
      this._homepage,
      this._countryCodes,
      this._gameTypes,
      this._platform,
      this._programmingLang,
      this._initialPosition,
    );
  }
}
