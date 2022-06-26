package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import test_utils.MockedServer;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static test_utils.Await.await;

@ExtendWith(SystemStubsExtension.class)
class BaseBotConstructorTest {

    static final String SERVER_URL = "SERVER_URL";
    static final String BOT_NAME = "BOT_NAME";
    static final String BOT_VERSION = "BOT_VERSION";
    static final String BOT_AUTHORS = "BOT_AUTHORS";
    static final String BOT_GAME_TYPES = "BOT_GAME_TYPES";
    static final String BOT_DESCRIPTION = "BOT_DESCRIPTION";
    static final String BOT_HOMEPAGE = "BOT_HOMEPAGE";
    static final String BOT_COUNTRY_CODES = "BOT_COUNTRY_CODES";
    static final String BOT_PLATFORM = "BOT_PLATFORM";
    static final String BOT_PROG_LANG = "BOT_PROG_LANG";

    MockedServer server;

    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables()
            .set(SERVER_URL, "ws://localhost:" + MockedServer.PORT)
            .set(BOT_NAME, "MyBot")
            .set(BOT_VERSION, "1.0")
            .set(BOT_AUTHORS, "Author1, Author2")
            .set(BOT_GAME_TYPES, "classic, melee")
            .set(BOT_DESCRIPTION, "Short description")
            .set(BOT_HOMEPAGE, "https://somewhere.net/MyBot")
            .set(BOT_COUNTRY_CODES, "uk, us")
            .set(BOT_PLATFORM, ".Net 5.0")
            .set(BOT_PROG_LANG, "C# 8.0");

    @BeforeEach
    void setup() {
        server = new MockedServer();
    }

    @AfterEach
    void teardown() {
        server.close();
    }

    @Test
    void givenEmptyConstructor_whenAllRequiredBotEnvVarsAreSet_thenBotIsCreatedSuccessfully() {
        new TestBot();
        // passed when this point is reached
    }

    @Test
    void givenEmptyConstructor_whenServerUrlEnvVarIsMissing_thenBotIsCreatedSuccessfully() {
        environmentVariables.set(SERVER_URL, null);
        new TestBot();
    }

    @Test
    void givenEmptyConstructor_whenBotNameEnvVarIsMissing_thenBotExceptionIsThrown() {
        environmentVariables.set(BOT_NAME, null);
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(botException.getMessage().toUpperCase(Locale.ROOT)).contains(BOT_NAME);
    }

    @Test
    void givenEmptyConstructor_whenBotVersionEnvVarIsMissing_thenBotExceptionIsThrown() {
        environmentVariables.set(BOT_VERSION, null);
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(botException.getMessage().toUpperCase(Locale.ROOT)).contains(BOT_VERSION);
    }

    @Test
    void givenEmptyConstructor_whenBotAuthorEnvVarIsMissing_thenBotExceptionIsThrown() {
        environmentVariables.set(BOT_AUTHORS, null);
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(botException.getMessage().toUpperCase(Locale.ROOT)).contains(BOT_AUTHORS);
    }

    @Test
    void givenEmptyConstructor_whenBotGameTypesEnvVarIsMissing_thenBotExceptionIsThrown() {
        environmentVariables.set(BOT_GAME_TYPES, null);
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(botException.getMessage().toUpperCase(Locale.ROOT)).contains(BOT_GAME_TYPES);
    }

    @Test
    void givenEmptyConstructor_whenAllRequiredBotEnvVarsAreSetAndStartingBot_thenBotMustConnectToServer() {
        startBotFromThread();
        await(() -> server.isConnected(), 1000);
        assertThat(server.isConnected()).isTrue();
    }

    @Test
    void givenEmptyConstructor_whenServerUrlEnvVarIsMissingAndStartingBot_thenBotCannotConnect() {
        environmentVariables.set(SERVER_URL, null);
        startBotFromThread();
        await(() -> server.isConnected(), 1000);
        assertThat(server.isConnected()).isFalse();
    }

    @Test
    void givenEmptyConstructor_whenAllRequiredBotEnvVarsAreSetAndStartingBot_thenBotHandshakeMustBeCorrect() {
        startBotFromThread();
        await(() -> server.getBotHandshake() != null, 1000);

        var botHandshake = server.getBotHandshake();
        var env = environmentVariables.getVariables();

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
