package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.BotException;
import dev.robocode.tankroyale.botapi.BotInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/** Utility class for reading environment variables. */
final class EnvVars {

  // Hide constructor
  EnvVars() {}

  /** Name of environment variable for server URI. */
  static final String SERVER_URL = "ROBOCODE_SERVER_URL";

  /** Name of environment variable for bot name. */
  static final String BOT_NAME = "BOT_NAME";

  /** Name of environment variable for bot version. */
  static final String BOT_VERSION = "BOT_VERSION";

  /** Name of environment variable for bot author. */
  static final String BOT_AUTHOR = "BOT_AUTHOR";

  /** Name of environment variable for bot description. */
  static final String BOT_DESCRIPTION = "BOT_DESCRIPTION";

  /** Name of environment variable for bot URL. */
  static final String BOT_URL = "BOT_URL";

  /** Name of environment variable for bot country code. */
  static final String BOT_COUNTRY_CODE = "BOT_COUNTRY_CODE";

  /** Name of environment variable for bot game types. */
  static final String BOT_GAME_TYPES = "BOT_GAME_TYPES";

  /** Name of environment variable for bot platform. */
  static final String BOT_PLATFORM = "BOT_PLATFORM";

  /** Name of environment variable for bot programming language. */
  static final String BOT_PROG_LANG = "BOT_PROG_LANG";

  private static final String NO_ENV_VALUE = "No value for environment variable: ";

  /** Bot Info */
  static BotInfo getBotInfo() {
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
    return new BotInfo(
        getBotName(),
        getBotVersion(),
        getBotAuthor(),
        getBotDescription(),
        getBotUrl(),
        getBotCountryCode(),
        getBotGameTypes(),
        getBotPlatform(),
        getBotProgrammingLang());
  }

  /** Server URI */
  static String getServerUrl() {
    return System.getenv(SERVER_URL);
  }

  /** Bot name */
  static String getBotName() {
    return System.getenv(BOT_NAME);
  }

  /** Bot version */
  static String getBotVersion() {
    return System.getenv(BOT_VERSION);
  }

  /** Bot author */
  static String getBotAuthor() {
    return System.getenv(BOT_AUTHOR);
  }

  /** Bot description */
  static String getBotDescription() {
    return System.getenv(BOT_DESCRIPTION);
  }

  /** Bot URL */
  static String getBotUrl() {
    return System.getenv(BOT_URL);
  }

  /** Bot country code */
  static String getBotCountryCode() {
    return System.getenv(BOT_COUNTRY_CODE);
  }

  /** List of game types, which the bot supports */
  static Collection<String> getBotGameTypes() {
    String gameTypes = System.getenv(BOT_GAME_TYPES);
    if (gameTypes == null || gameTypes.trim().length() == 0) {
      return Collections.emptyList();
    }
    return Arrays.asList(gameTypes.split("\\s*,\\s*"));
  }

  /** Platform used for running the bot */
  static String getBotPlatform() {
    return System.getenv(BOT_PLATFORM);
  }

  /** Language used for programming the bot */
  static String getBotProgrammingLang() {
    return System.getenv(BOT_PROG_LANG);
  }

  private static boolean isNullOrEmpty(String s) {
    return s == null || s.trim().isEmpty();
  }

  private static boolean isNullOrEmpty(Collection<?> c) {
    return c == null || c.isEmpty();
  }
}
