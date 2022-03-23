package dev.robocode.tankroyale.botapi.test_utils;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;

import static dev.robocode.tankroyale.botapi.test_utils.EnvironmentVariablesConstants.*;

public class EnvironmentVariablesBuilder {

    public static EnvironmentVariables createAll() {
        return new EnvironmentVariables()
                .set(SERVER_URL, "ws://localhost:7654")
                .set(BOT_NAME, "MyBot")
                .set(BOT_VERSION, "1.0")
                .set(BOT_AUTHORS, "Author1, Author2")
                .set(BOT_GAME_TYPES, "1v1, classic, melee")
                .set(BOT_DESCRIPTION, "Short description")
                .set(BOT_HOMEPAGE, "https://somewhere.net/MyBot")
                .set(BOT_COUNTRY_CODES, "uk, us")
                .set(BOT_PLATFORM, "JVM")
                .set(BOT_PROG_LANG, "Java 11")
                .set(BOT_INITIAL_POS, "50,50, 90");
    }
}
