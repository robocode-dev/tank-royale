package dev.robocode.tankroyale.botapi;

import com.neovisionaries.i18n.CountryCode;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Required information about the bot. */
@Value
@Builder
public final class BotInfo {

  /** Name, e.g. "MyBot" (required field) */
  @NonNull String name;

  /** Version, e.g. "1.0" (required field) */
  @NonNull String version;

  /** Author, e.g. "John Doe (johndoe@somewhere.io)" (required field) */
  @NonNull String author;

  /** Short description of the bot, preferable a one-liner */
  String description;

  /**
   * Country code defined by ISO 3166-1 alpha-2: https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2.
   * If no country code is provided, the locale of the system is being used instead.
   */
  String countryCode;

  /**
   * Game types accepted by the bot, e.g. Arrays.asList("melee", "1v1"). The game types defines
   * which game types the bot is able to participate in. See {@link GameType} for using predefined
   * game type.
   */
  @NonNull List<String> gameTypes;

  /** Programming language used for developing the bot, e.g. "Java" or "C#" */
  String programmingLang;

  public BotInfo(
      final String name,
      final String version,
      final String author,
      final String description,
      final String countryCode,
      final List<String> gameTypes,
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

    CountryCode code = null;
    if (countryCode != null) {
      code = CountryCode.getByCodeIgnoreCase(countryCode);
    }
    if (code == null) {
      code = CountryCode.getByLocale(Locale.getDefault());
    }

    if (gameTypes == null || gameTypes.isEmpty()) {
      throw new IllegalArgumentException("Game types cannot be null, empty or blank");
    }
    // Remove null, empty or blank game types
    val trimmedGameTypes = new ArrayList<String>();
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
}
