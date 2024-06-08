package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import test_utils.MockedServer;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static test_utils.EnvironmentVariables.*;

@ExtendWith(SystemStubsExtension.class)
class BaseBotConstructorTest extends AbstractBotTest {

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

    @SystemStub
    final static EnvironmentVariables envVars = new EnvironmentVariables(
            SERVER_URL, "ws://localhost:" + MockedServer.PORT,
            BOT_NAME, "TestBot",
            BOT_VERSION, "1.0",
            BOT_AUTHORS, "Author 1, Author 2",
            BOT_GAME_TYPES, "classic, 1v1, melee",
            BOT_DESCRIPTION, "Short description",
            BOT_HOMEPAGE, "https://testbot.robocode.dev",
            BOT_COUNTRY_CODES, "gb, US",
            BOT_PLATFORM, "JVM 19",
            BOT_PROG_LANG, "Java 19"
    );

    @Test
    void givenAllRequiredEnvVarsSet_whenCallingDefaultConstructor_thenBotIsCreated() {
        new TestBot();
        assertTrue(true);
    }

    @Test
    void givenMissingBotNameEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo() {
        var botName = envVars.getVariables().get(BOT_NAME);

        envVars.set(BOT_NAME, null);
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(exceptionContainsEnvVarName(botException, BOT_NAME)).isTrue();

        envVars.set(BOT_NAME, botName); // restore
    }

    @Test
    void givenMissingBotVersionEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo() {
        var version = envVars.getVariables().get(BOT_VERSION);

        envVars.set(BOT_VERSION, null);
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(exceptionContainsEnvVarName(botException, BOT_VERSION)).isTrue();

        envVars.set(BOT_VERSION, version); // restore
    }

    @Test
    void givenMissingBotAuthorsEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo() {
        var authors = envVars.getVariables().get(BOT_AUTHORS);

        envVars.set(BOT_AUTHORS, null);
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(exceptionContainsEnvVarName(botException, BOT_AUTHORS)).isTrue();

        envVars.set(BOT_AUTHORS, authors); // restore
    }

    @Test
    void givenAllRequiredEnvVarsSet_callingDefaultConstructorFromThread_thenBotIsCreatedAndConnectingToServer() {
        startAndAwaitHandshake();
        assertTrue(true);
    }

    @Test
    void givenMissingServerUrlEnvVar_callingDefaultConstructorFromThread_thenBotIsCreatedButNotConnectingToServer() {
        envVars.set(SERVER_URL, null);
        var bot = new TestBot();
        startAsync(bot);
        assertThat(server.awaitConnection(1000)).isFalse();
    }

    @Test
    void givenAllRequiredEnvVarsSet_callingDefaultConstructorFromThread_thenBotHandshakeMustBeCorrect() {
        startAndAwaitHandshake();
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
    void givenNoEnvVarsSet_callingDefaultConstructorWithBotInfoFromThread_thenBotHandshakeMustBeCorrect() {
        new TestBot(botInfo);
        assertTrue(true);
    }

    @Test
    void givenServerUrlWithValidPortAsParameter_whenCallingConstructor_thenBotIsConnectingToServer() throws URISyntaxException {
        var bot = new TestBot(null, new URI("ws://localhost:" + MockedServer.PORT)); // valid port
        startAsync(bot);
        assertThat(server.awaitConnection(1000)).isTrue();
    }

    @Test
    void givenServerUrlWithInvalidPortAsParameter_whenCallingConstructor_thenBotIsNotConnectingToServer() throws URISyntaxException {
        var bot = new TestBot(null, new URI("ws://localhost:" + (MockedServer.PORT + 1))); // invalid port
        startAsync(bot);
        assertThat(server.awaitConnection(1000)).isFalse();
    }

    @Test
    void givenServerSecretConstructor_whenCallingConstructor_thenReturnedBotHandshakeContainsSecret() throws URISyntaxException {
        var secret = UUID.randomUUID().toString();
        var bot = new TestBot(null, new URI("ws://localhost:" + MockedServer.PORT), secret);
        startAsync(bot);
        awaitBotHandshake();
        var botHandshake = server.getBotHandshake();
        assertThat(botHandshake.getSecret()).isEqualTo(secret);
    }

    @ParameterizedTest
    @ValueSource(strings = {"file", "dict", "ftp", "gopher"})
    void givenUnknownScheme_whenCallingConstructor_thenThrowException(String scheme) throws Exception {
        var bot = new TestBot(null, new URI(scheme + "://localhost:" + MockedServer.PORT));
        try {
            startAsync(bot).join();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(BotException.class);
            assertThat(e.getMessage()).startsWith("Wrong scheme used with server URL");
        }
    }
}
