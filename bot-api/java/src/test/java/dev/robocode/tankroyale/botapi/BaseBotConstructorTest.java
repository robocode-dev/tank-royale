package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import test_utils.BotInfoBuilder;
import test_utils.CountryCodeUtil;
import test_utils.MockedServer;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static test_utils.CountryCodeUtil.getLocalCountryCode;
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
        assertThat(botHandshake.getDescription()).isEqualTo(env.get(BOT_DESCRIPTION));
        assertThat(botHandshake.getHomepage()).isEqualTo(env.get(BOT_HOMEPAGE));
        assertThat(botHandshake.getPlatform()).isEqualTo(env.get(BOT_PLATFORM));
        assertThat(botHandshake.getProgrammingLang()).isEqualTo(env.get(BOT_PROG_LANG));

        assertThat(botHandshake.getCountryCodes().stream().map(String::toLowerCase).collect(Collectors.toList()))
                .containsAll(singletonList(getLocalCountryCode().toLowerCase()));
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

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void givenBotInfoConstructor_whenNameIsNullEmptyOrBlank_thenThrowIllegalArgumentException(String name) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setName(name);
        var exception = assertThrows(IllegalArgumentException.class, () -> new TestBot(builder.build()));
        assertThat(exception.getMessage()).containsIgnoringCase("name cannot be null, empty or blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void givenBotInfoConstructor_whenVersionIsNullEmptyOrBlank_thenThrowIllegalArgumentException(String version) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setVersion(version);
        var exception = assertThrows(IllegalArgumentException.class, () -> new TestBot(builder.build()));
        assertThat(exception.getMessage()).containsIgnoringCase("version cannot be null, empty or blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("invalidListOfStrings")
    void givenBotInfoConstructor_whenAuthorsIsInvalid_thenThrowIllegalArgumentException(List<String> authors) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setAuthors(authors);
        var exception = assertThrows(IllegalArgumentException.class, () -> new TestBot(builder.build()));
        assertThat(exception.getMessage()).containsIgnoringCase("authors cannot be null or empty or contain blanks");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void givenBotInfoConstructor_whenDescriptionIsNullEmptyOrBlank_thenBotIsCreated(String description) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setDescription(description);
        new TestBot(builder.build());
        // passed when this point is reached
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void givenBotInfoConstructor_whenHomepageIsInvalid_thenBotIsCreated(String homepage) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setHomepage(homepage);
        new TestBot(builder.build());
        // passed when this point is reached
    }

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("invalidListOfStrings")
    void givenBotInfoConstructor_whenGameTypesIsInvalid_thenBotIsCreated(List<String> gameTypes) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setGameTypes(gameTypes);
        var exception = assertThrows(IllegalArgumentException.class, () -> new TestBot(builder.build()));
        assertThat(exception.getMessage()).containsIgnoringCase("game types cannot be null or empty or contain blanks");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void givenBotInfoConstructor_whenPlatformIsInvalid_thenBotIsCreated(String platform) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setPlatform(platform);
        new TestBot(builder.build());
        // passed when this point is reached
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void givenBotInfoConstructor_whenProgrammingLangIsInvalid_thenBotIsCreated(String programmingLang) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setProgrammingLang(programmingLang);
        new TestBot(builder.build());
        // passed when this point is reached
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void givenBotInfoConstructor_whenInitialPositionIsInvalid_thenBotIsCreated(String initialPosition) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setInitialPosition(InitialPosition.fromString(initialPosition));
        new TestBot(builder.build());
        // passed when this point is reached
    }

    private static void startBotFromThread() {
        new Thread(() -> new TestBot().start()).start();
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

    private static Stream<List<String>> invalidListOfStrings() {
        return Stream.of(
                List.of(),
                List.of(""),
                List.of("\t"),
                List.of(" \n"),
                List.of(" ", "")
        );
    }

    static class TestBot extends BaseBot {

        TestBot() {
            super();
        }

        TestBot(BotInfo botInfo) {
            super(botInfo);
        }
    }
}
