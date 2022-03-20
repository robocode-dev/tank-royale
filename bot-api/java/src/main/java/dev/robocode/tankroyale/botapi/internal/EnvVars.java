package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.BotException;
import dev.robocode.tankroyale.botapi.BotInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for reading environment variables.
 */
final class EnvVars {

    /**
     * Name of environment variable for server URI.
     */
    static final String SERVER_URL = "SERVER_URL";
    /**
     * Name of environment variable for server URI.
     */
    static final String SERVER_SECRET = "SERVER_SECRET";
    /**
     * Name of environment variable for bot name.
     */
    static final String BOT_NAME = "BOT_NAME";
    /**
     * Name of environment variable for bot version.
     */
    static final String BOT_VERSION = "BOT_VERSION";
    /**
     * Name of environment variable for bot author(s).
     */
    static final String BOT_AUTHORS = "BOT_AUTHORS";
    /**
     * Name of environment variable for bot description.
     */
    static final String BOT_DESCRIPTION = "BOT_DESCRIPTION";
    /**
     * Name of environment variable for bot homepage URL.
     */
    static final String BOT_HOMEPAGE = "BOT_HOMEPAGE";
    /**
     * Name of environment variable for bot country code(s).
     */
    static final String BOT_COUNTRY_CODES = "BOT_COUNTRY_CODES";
    /**
     * Name of environment variable for bot game type(s).
     */
    static final String BOT_GAME_TYPES = "BOT_GAME_TYPES";
    /**
     * Name of environment variable for bot platform.
     */
    static final String BOT_PLATFORM = "BOT_PLATFORM";
    /**
     * Name of environment variable for bot programming language.
     */
    static final String BOT_PROGRAMMING_LANG = "BOT_PROG_LANG";

    private static final String NO_ENV_VALUE = "No value for environment variable: ";

    // Hide constructor
    EnvVars() {
    }

    /**
     * Bot Info
     */
    static BotInfo getBotInfo() {
        if (isNullOrEmpty(getBotName())) {
            throw new BotException(NO_ENV_VALUE + BOT_NAME);
        }
        if (isNullOrEmpty(getBotVersion())) {
            throw new BotException(NO_ENV_VALUE + BOT_VERSION);
        }
        if (isNullOrEmpty(getBotAuthors())) {
            throw new BotException(NO_ENV_VALUE + BOT_AUTHORS);
        }
        if (isNullOrEmpty(getBotGameTypes())) {
            throw new BotException(NO_ENV_VALUE + BOT_GAME_TYPES);
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
                getBotProgrammingLang());
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
     * List of game type(s), which the bot supports
     */
    static List<String> getBotGameTypes() {
        return propertyAsList(BOT_GAME_TYPES);
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
        return System.getenv(BOT_PROGRAMMING_LANG);
    }

    private static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static boolean isNullOrEmpty(Collection<?> c) {
        return c == null || c.isEmpty();
    }

    private static List<String> propertyAsList(String propertyName) {
        String value = System.getenv(propertyName);
        if (value == null || value.trim().length() == 0) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split("\\s*,\\s*"));
    }
}
