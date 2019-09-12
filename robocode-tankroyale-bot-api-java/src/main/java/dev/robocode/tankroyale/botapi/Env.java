package dev.robocode.tankroyale.botapi;

import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class Env {

  public static void main(String[] args) {

    val list = new ArrayList<String>(Arrays.asList(" ".split("\\s*,\\s*")));
    if (list.size() == 1 && list.get(0).trim().length() == 0) {
      list.remove(0);
    }

    for (String s : list) {
      System.out.println('"' + s + '"');
    }
  }

  private Env() {}

  static final String SERVER_URI = "ROBOCODE_SERVER_URI";

  private static final String BOT_NAME = "BOT_NAME";
  private static final String BOT_VERSION = "BOT_VERSION";
  private static final String BOT_AUTHOR = "BOT_AUTHOR";
  private static final String BOT_DESCRIPTION = "BOT_DESCRIPTION";
  private static final String BOT_COUNTRY_CODE = "BOT_COUNTRY_CODE";
  private static final String BOT_GAME_TYPES = "BOT_GAME_TYPES";
  private static final String BOT_PROG_LANG = "BOT_PROG_LANG";

  private static final String NO_ENV_VALUE = "No value for environment variable: ";

  static BotInfo getBotInfo() {
    if (getBotName() == null) {
      throw new BotException(NO_ENV_VALUE + BOT_NAME);
    }
    if (getBotVersion() == null) {
      throw new BotException(NO_ENV_VALUE + BOT_VERSION);
    }
    if (getBotAuthor() == null) {
      throw new BotException(NO_ENV_VALUE + BOT_AUTHOR);
    }
    if (getBotGameTypes().isEmpty()) {
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

  static String getServerUri() {
    return System.getenv(SERVER_URI);
  }

  private static String getBotName() {
    return System.getenv(BOT_NAME);
  }

  private static String getBotVersion() {
    return System.getenv(BOT_VERSION);
  }

  private static String getBotAuthor() {
    return System.getenv(BOT_AUTHOR);
  }

  private static String getBotDescription() {
    return System.getenv(BOT_DESCRIPTION);
  }

  private static String getBotCountryCode() {
    return System.getenv(BOT_COUNTRY_CODE);
  }

  private static List<String> getBotGameTypes() {
    val gameTypes = System.getenv(BOT_GAME_TYPES);
    if (gameTypes == null || gameTypes.trim().length() == 0) {
      return Collections.emptyList();
    }
    return Arrays.asList(gameTypes.split("\\s*,\\s*"));
  }

  private static String getBotProgrammingLang() {
    return System.getenv(BOT_PROG_LANG);
  }
}
