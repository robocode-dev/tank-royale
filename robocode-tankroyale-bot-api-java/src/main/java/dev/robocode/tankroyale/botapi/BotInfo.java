package dev.robocode.tankroyale.botapi;

import com.neovisionaries.i18n.CountryCode;

import java.util.*;

/** Required information about the bot. */
@SuppressWarnings("unused")
public final class BotInfo {

  /** Name, e.g. "MyBot" (required field) */
  private String name;

  /** Version, e.g. "1.0" (required field) */
  private String version;

  /** Author, e.g. "John Doe (johndoe@somewhere.io)" (required field) */
  private String author;

  /** Short description of the bot, preferable a one-liner */
  private String description;

  /**
   * Country code defined by ISO 3166-1 alpha-2: https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2.
   * If no country code is provided, the locale of the system is being used instead.
   */
  private String countryCode;

  /**
   * Game types accepted by the bot, e.g. "melee", "1v1". The game types defines which game types
   * the bot is able to participate in. See {@link GameType} for using predefined game type.
   */
  private Set<String> gameTypes;

  /** Programming language used for developing the bot, e.g. "Java" or "C#" */
  private String programmingLang;

  public BotInfo(
      final String name,
      final String version,
      final String author,
      final String description,
      final String countryCode,
      final Collection<String> gameTypes,
      final String programmingLang) {

    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Name cannot be null, empty or blank");
    }
    if (version == null || version.trim().isEmpty()) {
      throw new IllegalArgumentException("Version cannot be null, empty or blank");
    }
    if (author == null || author.trim().isEmpty()) {
      throw new IllegalArgumentException("Author cannot be null, empty or blank");
    }
    if (gameTypes == null || gameTypes.isEmpty()) {
      throw new IllegalArgumentException("Game types cannot be null, empty or blank");
    }
    CountryCode code = null;
    if (countryCode != null) {
      // Get country code from input parameter
      code = CountryCode.getByCodeIgnoreCase(countryCode);
    }
    if (code == null) {
      // Get local country code
      code = CountryCode.getByLocale(Locale.getDefault());
    }

    // Remove null, empty or blank game types
    Set<String> trimmedGameTypes = new HashSet<>();
    gameTypes
        .iterator()
        .forEachRemaining(
            gameType -> {
              if (gameType != null && !gameType.trim().isEmpty()) {
                trimmedGameTypes.add(gameType.trim());
              }
            });

    if (trimmedGameTypes.size() == 0) {
      throw new IllegalArgumentException("Game types does not contain any game types");
    }

    this.name = name;
    this.version = version;
    this.author = author;
    this.description = description;
    this.countryCode = (code == null) ? null : code.getAlpha2();
    this.gameTypes = trimmedGameTypes;
    this.programmingLang = programmingLang;
  }

  /** Returns the name, e.g. "MyBot" (required field) */
  public String getName() {
    return name;
  }

  /** Returns the version, e.g. "1.0" (required field) */
  public String getVersion() {
    return version;
  }

  /** Returns the author, e.g. "John Doe (johndoe@somewhere.io)" (required field) */
  public String getAuthor() {
    return author;
  }

  /** Returns a short description of the bot, preferable a one-liner */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the country code defined by ISO 3166-1 alpha-2: https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2.
   * If no country code is provided, the locale of the system is being used instead.
   */
  public String getCountryCode() {
    return countryCode;
  }

  /**
   * Returns the game types accepted by the bot, e.g. "melee", "1v1". The game types defines which game types
   * the bot is able to participate in. See {@link GameType} for using predefined game type.
   */
  public Set<String> getGameTypes() {
    return gameTypes;
  }

  /** Returns the Programming language used for developing the bot, e.g. "Java" or "C#" */
  public String getProgrammingLang() {
    return programmingLang;
  }
}
