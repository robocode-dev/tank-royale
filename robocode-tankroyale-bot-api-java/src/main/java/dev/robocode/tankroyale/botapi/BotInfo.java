package dev.robocode.tankroyale.botapi;

import com.neovisionaries.i18n.CountryCode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/** Bot info contains the properties of a bot. */
@SuppressWarnings("unused")
public final class BotInfo {

  private final String name; // required
  private final String version; // required
  private final String author; // required
  private final String description; // optional
  private final String url; // optional
  private final String countryCode; // optional
  private final Set<String> gameTypes; // required
  private final String platform; // optional
  private final String programmingLang; // optional

  /**
   * Initializes a new instance of the BotInfo class.
   *
   * @param name is the name of the bot (required).
   * @param version is the version of the bot (required).
   * @param author is the author of the bot (required).
   * @param description is a short description of the bot (optional).
   * @param url is the URL to a web page for the bot (optional).
   * @param countryCode is the country code for the bot (optional).
   * @param gameTypes is the game types that this bot can handle (required).
   * @param platform is the platform used for running the bot (optional).
   * @param programmingLang is the programming language used for developing the bot (optional).
   */
  public BotInfo(
      final String name,
      final String version,
      final String author,
      final String description,
      final String url,
      final String countryCode,
      final Collection<String> gameTypes,
      final String platform,
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
    this.url = url;
    this.countryCode = (code == null) ? null : code.getAlpha2();
    this.gameTypes = trimmedGameTypes;
    this.platform = platform;
    this.programmingLang = programmingLang;
  }

  /**
   * Reads the bot info from a file.
   *
   * @param fileName is the filename of the file containing bot properties.
   * @return A BotInfo instance containing the bot properties read from the file.
   * @throws IOException if an error occurs when reading the file.
   */
  public static BotInfo fromFile(String fileName) throws IOException {
    File file = new File(BotInfo.class.getClassLoader().getResource(fileName).getFile());
    try (FileInputStream fis = new FileInputStream(file)) {
      Properties prop = new Properties();
      prop.load(fis);
      return new BotInfo(
          prop.getProperty("name"),
          prop.getProperty("version"),
          prop.getProperty("author"),
          prop.getProperty("description"),
          prop.getProperty("url"),
          prop.getProperty("countryCode"),
          Arrays.asList(prop.getProperty("gameTypes").split("\\s*,\\s*")),
          prop.getProperty("platform"),
          prop.getProperty("programmingLang"));
    }
  }

  /**
   * Returns the name, e.g., "MyBot". This field must always be provided with the bot info.
   *
   * @return The name of the bot.
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the version, e.g., "1.0". This field must always be provided with the bot info.
   *
   * @return The version of the bot.
   */
  public String getVersion() {
    return version;
  }

  /**
   * Returns the author, e.g., "John Doe (johndoe@somewhere.io)". This field must always be provided
   * with the bot info.
   *
   * @return The author of the bot.
   */
  public String getAuthor() {
    return author;
  }

  /**
   * Returns a short description of the bot, preferably a one-liner. This field is optional.
   *
   * @return a short description of the bot.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Returns the URL of a web page for the bot. This field is optional.
   *
   * @return The URL of a web page for the bot.
   */
  public String getUrl() {
    return url;
  }

  /**
   * Returns the country code defined by ISO 3166-1 alpha-2, e.g. "us":
   * https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2. This field is optional. If no country code is
   * provided, the locale of the system is being used instead.
   *
   * @return The country code for the bot.
   */
  public String getCountryCode() {
    return countryCode;
  }

  /**
   * Returns the game types accepted by the bot, e.g., "melee", "1v1". This field must always be
   * provided with the bot info. The game types define which game types the bot can participate in.
   * See {@link GameType} for using predefined game type.
   *
   * @return The game types that this bot can handle.
   */
  public Set<String> getGameTypes() {
    return gameTypes;
  }

  /**
   * Returns the platform used for running the bot, e.g., "Java Runtime Environment" or ".Net Core".
   * This field is optional.
   *
   * @return The platform used for running the bot.
   */
  public String getPlatform() {
    return platform;
  }

  /**
   * Returns the programming language used for developing the bot, e.g., "Java" or "C#". This field
   * is optional.
   *
   * @return The programming language used for developing the bot.
   */
  public String getProgrammingLang() {
    return programmingLang;
  }
}
