package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import test_utils.MockedServer;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import static test_utils.EnvironmentVariables.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SetEnvironmentVariable(key = SERVER_URL, value = "ws://localhost:" + MockedServer.PORT)
@SetEnvironmentVariable(key = BOT_NAME, value = "MyBot")
@SetEnvironmentVariable(key = BOT_VERSION, value = "1.0")
@SetEnvironmentVariable(key = BOT_AUTHORS, value = "Author1, Author2")
@SetEnvironmentVariable(key = BOT_GAME_TYPES, value = "classic, melee")
@SetEnvironmentVariable(key = BOT_DESCRIPTION, value = "Short description")
@SetEnvironmentVariable(key = BOT_HOMEPAGE, value = "https://somewhere.net/MyBot")
@SetEnvironmentVariable(key = BOT_COUNTRY_CODES, value = "uk, us")
@SetEnvironmentVariable(key = BOT_PLATFORM, value = "JVM 18")
@SetEnvironmentVariable(key = BOT_PROG_LANG, value = "Java 18")
class BaseBotConstructorTest {

    MockedServer server;

    @BeforeEach
    void setup() {
        server = new MockedServer();
        server.start();
    }

    @AfterEach
    void teardown() {
        server.stop();
    }

    @Test
    void givenEmptyConstructor_whenAllRequiredBotEnvVarsAreSet_thenBotIsCreatedSuccessfully() {
        new TestBot();
        // passed when this point is reached
    }

    @Test
    @ClearEnvironmentVariable(key = SERVER_URL)
    void givenEmptyConstructor_whenServerUrlEnvVarIsMissing_thenBotIsCreatedSuccessfully() {
        new TestBot();
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_NAME)
    void givenEmptyConstructor_whenBotNameEnvVarIsMissing_thenBotExceptionIsThrown() {
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(botException.getMessage().toUpperCase(Locale.ROOT)).contains(BOT_NAME);
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_VERSION)
    void givenEmptyConstructor_whenBotVersionEnvVarIsMissing_thenBotExceptionIsThrown() {
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(botException.getMessage().toUpperCase(Locale.ROOT)).contains(BOT_VERSION);
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_AUTHORS)
    void givenEmptyConstructor_whenBotAuthorEnvVarIsMissing_thenBotExceptionIsThrown() {
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(botException.getMessage().toUpperCase(Locale.ROOT)).contains(BOT_AUTHORS);
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_GAME_TYPES)
    void givenEmptyConstructor_whenBotGameTypesEnvVarIsMissing_thenBotExceptionIsThrown() {
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(botException.getMessage().toUpperCase(Locale.ROOT)).contains(BOT_GAME_TYPES);
    }

    @Test
    void givenEmptyConstructor_whenAllRequiredBotEnvVarsAreSetAndStartingBot_thenBotMustConnectToServer() {
        startBotFromThread();
        assertThat(server.awaitConnection(1000)).isTrue();
    }

    @Test
    @ClearEnvironmentVariable(key = SERVER_URL)
    void givenEmptyConstructor_whenServerUrlEnvVarIsMissingAndStartingBot_thenBotCannotConnect() {
        startBotFromThread();
        assertThat(server.awaitConnection(1000)).isFalse();
    }

    @Test
    void givenEmptyConstructor_whenAllRequiredBotEnvVarsAreSetAndStartingBot_thenBotHandshakeMustBeCorrect() {
        startBotFromThread();
        assertThat(server.awaitBotHandshake(1000)).isTrue();

        var botHandshake = server.getBotHandshake();
        var env = System.getenv();

        assertThat(botHandshake).isNotNull();
        assertThat(botHandshake.getName()).isEqualTo(env.get(BOT_NAME));
        assertThat(botHandshake.getVersion()).isEqualTo(env.get(BOT_VERSION));
        assertThat(botHandshake.getAuthors()).containsAll(Arrays.asList(env.get(BOT_AUTHORS).split("\\s*,\\s*")));
        assertThat(botHandshake.getGameTypes()).containsAll(Arrays.asList(env.get(BOT_GAME_TYPES).split("\\s*,\\s*")));
        assertThat(botHandshake.getDescription()).isEqualTo(env.get(BOT_DESCRIPTION));
        assertThat(botHandshake.getHomepage()).isEqualTo(env.get(BOT_HOMEPAGE));
        assertThat(botHandshake.getCountryCodes().stream().map(String::toLowerCase).collect(Collectors.toList()))
                .containsAll(Arrays.asList(env.get(BOT_COUNTRY_CODES).toLowerCase().split("\\s*,\\s*")));
        assertThat(botHandshake.getPlatform()).isEqualTo(env.get(BOT_PLATFORM));
        assertThat(botHandshake.getProgrammingLang()).isEqualTo(env.get(BOT_PROG_LANG));
    }

    private void startBotFromThread() {
        new Thread(() -> new TestBot().start()).start();
    }

    static class TestBot extends BaseBot {
        public TestBot() {
            super();
        }
    }
}
