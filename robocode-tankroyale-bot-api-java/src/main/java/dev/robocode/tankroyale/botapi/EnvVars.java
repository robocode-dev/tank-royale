package dev.robocode.tankroyale.botapi;

import lombok.val;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Class for reading environment variables.
 */
final class EnvVars {

  // Hide constructor
  private EnvVars() {}

  private static final String SERVER_URI = "ROBOCODE_SERVER_URI";

  private static final String BOT_NAME = "BOT_NAME";
  private static final String BOT_VERSION = "BOT_VERSION";
  private static final String BOT_AUTHOR = "BOT_AUTHOR";
  private static final String BOT_DESCRIPTION = "BOT_DESCRIPTION";
  private static final String BOT_COUNTRY_CODE = "BOT_COUNTRY_CODE";
  private static final String BOT_GAME_TYPES = "BOT_GAME_TYPES";
  private static final String BOT_PROG_LANG = "BOT_PROG_LANG";

  private static final String NO_ENV_VALUE = "No value for environment variable: ";

  /** Bot Info */
  public static BotInfo getBotInfo() {
    if (isNullOrEmpty(getBotName())) {
      throw new BotException(NO_ENV_VALUE + BOT_NAME);
    }
    if (isNullOrEmpty(getBotVersion())) {
      throw new BotException(NO_ENV_VALUE + BOT_VERSION);
    }
    if (isNullOrEmpty(getBotAuthor())) {
      throw new BotException(NO_ENV_VALUE + BOT_AUTHOR);
    }
    if (isNullOrEmpty(getBotGameTypes())) {
      throw new BotException(NO_ENV_VALUE + BOT_GAME_TYPES);
    }
    return BotInfo.builder()
        .name(getBotName())
        .version((getBotVersion()))
        .author(getBotAuthor())
        .description(getBotDescription())
        .countryCode(getBotCountryCode())
        .gameTypes(getBotGameTypes())
        .programmingLang(getBotProgrammingLang())
        .build();
  }

  /** Server URI */
  public static String getServerUri() {
    return System.getenv(SERVER_URI);
  }

  /** Bot name */
  public static String getBotName() {
    return System.getenv(BOT_NAME);
  }

  /** Bot version */
  public static String getBotVersion() {
    return System.getenv(BOT_VERSION);
  }

  /** Bot author */
  public static String getBotAuthor() {
    return System.getenv(BOT_AUTHOR);
  }

  /** Bot description */
  public static String getBotDescription() {
    return System.getenv(BOT_DESCRIPTION);
  }

  /** Bot country code */
  public static String getBotCountryCode() {
    return System.getenv(BOT_COUNTRY_CODE);
  }

  /** List of game types, which the bot supports */
  public static Collection<String> getBotGameTypes() {
    val gameTypes = System.getenv(BOT_GAME_TYPES);
    if (gameTypes == null || gameTypes.trim().length() == 0) {
      return Collections.emptyList();
    }
    return Arrays.asList(gameTypes.split("\\s*,\\s*"));
  }

  /** Language used for programming the bot */
  public static String getBotProgrammingLang() {
    return System.getenv(BOT_PROG_LANG);
  }

  private static boolean isNullOrEmpty(String s) {
    return s == null || s.trim().isEmpty();
  }

  private static boolean isNullOrEmpty(Collection<?> c) {
    return c == null || c.isEmpty();
  }
}
