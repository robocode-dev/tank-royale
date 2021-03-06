package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import test_utils.MockedServer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import static test_utils.EnvironmentVariables.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SetEnvironmentVariable(key = SERVER_URL, value = "ws://localhost:" + MockedServer.PORT)
@SetEnvironmentVariable(key = BOT_NAME, value = "TestBot")
@SetEnvironmentVariable(key = BOT_VERSION, value = "1.0")
@SetEnvironmentVariable(key = BOT_AUTHORS, value = "Author1, Author2")
@SetEnvironmentVariable(key = BOT_GAME_TYPES, value = "classic, melee")
@SetEnvironmentVariable(key = BOT_DESCRIPTION, value = "Short description")
@SetEnvironmentVariable(key = BOT_HOMEPAGE, value = "https://somewhere.net/MyBot")
@SetEnvironmentVariable(key = BOT_COUNTRY_CODES, value = "gb, US")
@SetEnvironmentVariable(key = BOT_PLATFORM, value = "JVM 18")
@SetEnvironmentVariable(key = BOT_PROG_LANG, value = "Java 18")
class BaseBotConstructorTest {

    MockedServer server;

    @BeforeEach
    void setUp() {
        server = new MockedServer();
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop();
    }

    @Test
    void givenEmptyConstructor_whenAllRequiredBotEnvVarsAreSet_thenBotIsCreated() {
        new TestBot();
        // passed when this point is reached
    }

    @Test
    @ClearEnvironmentVariable(key = SERVER_URL)
    void givenEmptyConstructor_whenServerUrlEnvVarIsMissing_thenBotIsCreated() {
        new TestBot();
        // passed when this point is reached
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_NAME)
    void givenEmptyConstructor_whenBotNameEnvVarIsMissing_thenBotExceptionIsThrown() {
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(exceptionContainsEnvVarName(botException, BOT_NAME)).isTrue();
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_VERSION)
    void givenEmptyConstructor_whenBotVersionEnvVarIsMissing_thenBotExceptionIsThrown() {
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(exceptionContainsEnvVarName(botException, BOT_VERSION)).isTrue();
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_AUTHORS)
    void givenEmptyConstructor_whenBotAuthorEnvVarIsMissing_thenBotExceptionIsThrown() {
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(exceptionContainsEnvVarName(botException, BOT_AUTHORS)).isTrue();
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_GAME_TYPES)
    void givenEmptyConstructor_whenBotGameTypesEnvVarIsMissing_thenBotExceptionIsThrown() {
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(exceptionContainsEnvVarName(botException, BOT_GAME_TYPES)).isTrue();
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
        assertThat(botHandshake.getCountryCodes().stream().map(String::toLowerCase).collect(Collectors.toList()))
                .containsAll(Arrays.stream(env.get(BOT_COUNTRY_CODES).split(",")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()));
        assertThat(botHandshake.getDescription()).isEqualTo(env.get(BOT_DESCRIPTION));
        assertThat(botHandshake.getHomepage()).isEqualTo(env.get(BOT_HOMEPAGE));
        assertThat(botHandshake.getPlatform()).isEqualTo(env.get(BOT_PLATFORM));
        assertThat(botHandshake.getProgrammingLang()).isEqualTo(env.get(BOT_PROG_LANG));
    }

    @Test
    @ClearEnvironmentVariable(key = SERVER_URL)
    @ClearEnvironmentVariable(key = BOT_NAME)
    @ClearEnvironmentVariable(key = BOT_VERSION)
    @ClearEnvironmentVariable(key = BOT_AUTHORS)
    @ClearEnvironmentVariable(key = BOT_GAME_TYPES)
    @ClearEnvironmentVariable(key = BOT_DESCRIPTION)
    @ClearEnvironmentVariable(key = BOT_HOMEPAGE)
    @ClearEnvironmentVariable(key = BOT_COUNTRY_CODES)
    @ClearEnvironmentVariable(key = BOT_PLATFORM)
    @ClearEnvironmentVariable(key = BOT_PROG_LANG)
    void givenBotInfoConstructor_whenBotInfoAndServerUrlAndServerSecretAreValid_thenBotIsCreated() {
        new TestBot(createBotInfo());
        // passed when this point is reached
    }


    @Test
    void givenServerUrlConstructor_whenServerUrlIsValid_thenBotMustConnectToServer() throws URISyntaxException {
        var bot = new TestBot(null, new URI("ws://localhost:" + MockedServer.PORT));
        startBotFromThread(bot);
        assertThat(server.awaitBotHandshake(1000)).isTrue();
    }

    @Test
    void givenServerUrlConstructor_whenServerUrlIsInvalidValid_thenBotCannotConnectToServer() throws URISyntaxException {
        var bot = new TestBot(null, new URI("ws://localhost:" + (MockedServer.PORT + 1)));
        startBotFromThread(bot);
        assertThat(server.awaitConnection(1000)).isFalse();
    }

    @Test
    void givenServerSecretConstructor_whenServerSecretIsProvided_thenReturnedBotHandshakeMustProvideThisSecret() throws URISyntaxException {
        var secret = UUID.randomUUID().toString();
        var bot = new TestBot(null, new URI("ws://localhost:" + MockedServer.PORT), secret);
        startBotFromThread(bot);
        assertThat(server.awaitBotHandshake(1000)).isTrue();
        var botHandshake = server.getBotHandshake();
        assertThat(botHandshake.getSecret()).isEqualTo(secret);
    }

    private static void startBotFromThread() {
        new Thread(() -> new TestBot().start()).start();
    }

    private static void startBotFromThread(IBaseBot bot) {
        new Thread(bot::start).start();
    }

    private boolean exceptionContainsEnvVarName(BotException botException, String envVarName) {
        return botException.getMessage().toUpperCase(Locale.ROOT).contains(envVarName);
    }

    private static BotInfo createBotInfo() {
        return new BotInfo(
                "TestBot",
                "1.0",
                List.of("Author1", "Author2"),
                "description",
                "https://testbot.robocode.dev",
                List.of("gb", "us"),
                List.of("classic", "melee", "1v1"),
                "JVM",
                "Java 18",
                InitialPosition.fromString("10, 20, 30")
        );
    }

    static class TestBot extends BaseBot {

        TestBot() {
            super();
        }

        TestBot(BotInfo botInfo) {
            super(botInfo);
        }

        TestBot(BotInfo botInfo, URI serverUrl) {
            super(botInfo, serverUrl);
        }

        TestBot(BotInfo botInfo, URI serverUrl, String serverSecret) {
            super(botInfo, serverUrl, serverSecret);
        }
    }
}
