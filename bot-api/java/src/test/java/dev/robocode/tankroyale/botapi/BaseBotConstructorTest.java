package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import test_utils.MockedServer;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

import static test_utils.EnvironmentVariables.*;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SetEnvironmentVariable(key = SERVER_URL, value = "ws://localhost:" + MockedServer.PORT)
@SetEnvironmentVariable(key = BOT_NAME, value = "TestBot")
@SetEnvironmentVariable(key = BOT_VERSION, value = "1.0")
@SetEnvironmentVariable(key = BOT_AUTHORS, value = "Author 1, Author 2")
@SetEnvironmentVariable(key = BOT_GAME_TYPES, value = "classic, 1v1, melee")
@SetEnvironmentVariable(key = BOT_DESCRIPTION, value = "Short description")
@SetEnvironmentVariable(key = BOT_HOMEPAGE, value = "https://testbot.robocode.dev")
@SetEnvironmentVariable(key = BOT_COUNTRY_CODES, value = "gb, US")
@SetEnvironmentVariable(key = BOT_PLATFORM, value = "JVM 19")
@SetEnvironmentVariable(key = BOT_PROG_LANG, value = "Java 19")
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

    @Test
    void givenAllRequiredEnvVarsSet_whenCallingDefaultConstructor_thenBotIsCreated() {
        new TestBot();
        // passed when this point is reached
    }

    @Test
    @ClearEnvironmentVariable(key = SERVER_URL)
    void givenMissingServerUrlEnvVar_whenCallingDefaultConstructor_thenBotIsCreated() {
        new TestBot();
        // passed when this point is reached
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_NAME)
    void givenMissingBotNameEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo() {
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(exceptionContainsEnvVarName(botException, BOT_NAME)).isTrue();
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_VERSION)
    void givenMissingBotVersionEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo() {
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(exceptionContainsEnvVarName(botException, BOT_VERSION)).isTrue();
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_AUTHORS)
    void givenMissingBotAuthorsEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo() {
        var botException = assertThrows(BotException.class, TestBot::new);
        assertThat(exceptionContainsEnvVarName(botException, BOT_AUTHORS)).isTrue();
    }

    @Test
    void givenAllRequiredEnvVarsSet_callingDefaultConstructorFromThread_thenBotIsCreatedAndConnectingToServer() {
        startAndAwaitHandshake();
    }

    @Test
    @ClearEnvironmentVariable(key = SERVER_URL)
    void givenMissingServerUrlEnvVar_callingDefaultConstructorFromThread_thenBotIsCreatedButNotConnectingToServer() {
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
    void givenNoEnvVarsSet_callingDefaultConstructorWithBotInfoFromThread_thenBotHandshakeMustBeCorrect() {
        new TestBot(botInfo);
        // passed when this point is reached
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
}
