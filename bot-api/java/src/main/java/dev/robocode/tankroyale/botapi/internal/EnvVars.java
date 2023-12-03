package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.BotException;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.InitialPosition;

import java.util.*;

/**
 * Utility class for reading environment variables.
 */
final class EnvVars {

    // Hides constructor
    private EnvVars() {
    }

    /**
     * Name of environment variable for server URI.
     */
    private static final String SERVER_URL = "SERVER_URL";
    /**
     * Name of environment variable for server URI.
     */
    private static final String SERVER_SECRET = "SERVER_SECRET";
    /**
     * Name of environment variable for bot name.
     */
    private static final String BOT_NAME = "BOT_NAME";
    /**
     * Name of environment variable for bot version.
     */
    private static final String BOT_VERSION = "BOT_VERSION";
    /**
     * Name of environment variable for bot author(s).
     */
    private static final String BOT_AUTHORS = "BOT_AUTHORS";
    /**
     * Name of environment variable for bot description.
     */
    private static final String BOT_DESCRIPTION = "BOT_DESCRIPTION";
    /**
     * Name of environment variable for bot homepage URL.
     */
    private static final String BOT_HOMEPAGE = "BOT_HOMEPAGE";
    /**
     * Name of environment variable for bot country code(s).
     */
    private static final String BOT_COUNTRY_CODES = "BOT_COUNTRY_CODES";
    /**
     * Name of environment variable for bot game type(s).
     */
    private static final String BOT_GAME_TYPES = "BOT_GAME_TYPES";
    /**
     * Name of environment variable for bot platform.
     */
    private static final String BOT_PLATFORM = "BOT_PLATFORM";
    /**
     * Name of environment variable for bot programming language.
     */
    private static final String BOT_PROG_LANG = "BOT_PROG_LANG";
    /**
     * Name of environment variable for bot initial position.
     */
    private static final String BOT_INITIAL_POS = "BOT_INITIAL_POS";
    /**
     * Name of environment variable that is set if bot team id is provided.
     */
    private static final String TEAM_ID = "TEAM_ID";
    /**
     * Name of environment variable that is set if bot team name is provided.
     */
    private static final String TEAM_NAME = "TEAM_NAME";
    /**
     * Name of environment variable that is set if bot team version is provided.
     */
    private static final String TEAM_VERSION = "TEAM_VERSION";
    /**
     * Name of environment variable that is set if bot is being booted.
     */
    private static final String BOT_BOOTED = "BOT_BOOTED";

    private static final String MISSING_ENV_VALUE = "Missing environment variable: ";

    /**
     * Bot Info
     */
    static BotInfo getBotInfo() {
        if (isBlank(getBotName())) {
            throw new BotException(MISSING_ENV_VALUE + BOT_NAME);
        }
        if (isBlank(getBotVersion())) {
            throw new BotException(MISSING_ENV_VALUE + BOT_VERSION);
        }
        if (isBlank(getBotAuthors())) {
            throw new BotException(MISSING_ENV_VALUE + BOT_AUTHORS);
        }
        return new BotInfo(
                getBotName(),
                getBotVersion(),
                getBotAuthors(),
                getBotDescription(),
                getBotHomepage(),
                getBotCountryCodes(),
                getBotGameTypes(),
                getBotPlatform(),
                getBotProgrammingLang(),
                getBotInitialPosition());
    }

    /**
     * Server URL
     */
    static String getServerUrl() {
        return System.getenv(SERVER_URL);
    }

    /**
     * Server secret
     */
    static String getServerSecret() {
        return System.getenv(SERVER_SECRET);
    }

    /**
     * Bot name
     */
    static String getBotName() {
        return System.getenv(BOT_NAME);
    }

    /**
     * Bot version
     */
    static String getBotVersion() {
        return System.getenv(BOT_VERSION);
    }

    /**
     * Bot author(s)
     */
    static List<String> getBotAuthors() {
        return propertyAsList(BOT_AUTHORS);
    }

    /**
     * Bot description
     */
    static String getBotDescription() {
        return System.getenv(BOT_DESCRIPTION);
    }

    /**
     * Bot homepage URL.
     */
    static String getBotHomepage() {
        return System.getenv(BOT_HOMEPAGE);
    }

    /**
     * Bot country code(s)
     */
    static List<String> getBotCountryCodes() {
        return propertyAsList(BOT_COUNTRY_CODES);
    }

    /**
     * Set of game type(s), which the bot supports
     */
    static Set<String> getBotGameTypes() {
        return new HashSet<>(propertyAsList(BOT_GAME_TYPES));
    }

    /**
     * Platform used for running the bot
     */
    static String getBotPlatform() {
        return System.getenv(BOT_PLATFORM);
    }

    /**
     * Language used for programming the bot
     */
    static String getBotProgrammingLang() {
        return System.getenv(BOT_PROG_LANG);
    }

    /**
     * Initial starting position used for debugging the bot
     */
    static InitialPosition getBotInitialPosition() {
        return InitialPosition.fromString(System.getenv(BOT_INITIAL_POS));
    }

    /**
     * Bot team id
     */
    static Integer getTeamId() {
        String teamId = System.getenv(TEAM_ID);
        return teamId != null ? Integer.parseInt(teamId) : null;
    }

    /**
     * Bot team name
     */
    static String getTeamName() {
        return System.getenv(TEAM_NAME);
    }

    /**
     * Bot team version
     */
    static String getTeamVersion() {
        return System.getenv(TEAM_VERSION);
    }

    /**
     * Checks if bot is being booted.
     */
    public static boolean isBotBooted() {
        return System.getenv(BOT_BOOTED) != null;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static boolean isBlank(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    private static List<String> propertyAsList(String propertyName) {
        String value = System.getenv(propertyName);
        if (value == null || value.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split("\\s*,\\s*"));
    }
}
