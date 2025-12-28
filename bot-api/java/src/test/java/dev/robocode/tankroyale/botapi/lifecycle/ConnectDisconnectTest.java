package dev.robocode.tankroyale.botapi.lifecycle;

import dev.robocode.tankroyale.botapi.BaseBot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.botapi.InitialPosition;
import dev.robocode.tankroyale.schema.BotHandshake;
import dev.robocode.tankroyale.schema.Message;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test_utils.MockedServer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TR-API-BOT-002 Connect/Disconnect tests.
 * Verifies that bot opens and closes connection correctly, and handshake messages are valid per schema.
 */
@Tag("BOT")
@Tag("TR-API-BOT-002")
class ConnectDisconnectTest {

    private MockedServer server;

    private static final BotInfo botInfo = BotInfo.builder()
            .setName("TestBot")
            .setVersion("1.0")
            .addAuthor("Author 1")
            .addAuthor("Author 2")
            .setDescription("Short description")
            .setHomepage("https://testbot.robocode.dev")
            .addCountryCode("gb")
            .addCountryCode("us")
            .addGameType("classic")
            .addGameType("melee")
            .addGameType("1v1")
            .setPlatform("JVM 19")
            .setProgrammingLang("Java 19")
            .setInitialPosition(InitialPosition.fromString("10, 20, 30"))
            .build();

    private static class TestBot extends BaseBot {
        TestBot() {
            super(botInfo, MockedServer.getServerUrl());
        }
    }

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
    @DisplayName("TR-API-BOT-002a Bot connects to server successfully")
    @Tag("TR-API-BOT-002a")
    void test_TR_API_BOT_002a_bot_connects_to_server() {
        // Arrange
        var bot = new TestBot();

        // Act - start bot in separate thread
        var thread = new Thread(bot::start);
        thread.start();

        // Assert - server receives connection
        boolean connected = server.awaitConnection(2000);
        assertThat(connected)
                .as("Bot should connect to server within timeout")
                .isTrue();
    }

    @Test
    @DisplayName("TR-API-BOT-002b Bot sends valid BotHandshake message")
    @Tag("TR-API-BOT-002b")
    void test_TR_API_BOT_002b_bot_sends_valid_handshake() {
        // Arrange
        var bot = new TestBot();

        // Act - start bot and wait for handshake
        var thread = new Thread(bot::start);
        thread.start();

        boolean handshakeReceived = server.awaitBotHandshake(2000);

        // Assert - handshake was received
        assertThat(handshakeReceived)
                .as("Server should receive BotHandshake within timeout")
                .isTrue();

        // Assert - handshake message type is correct
        BotHandshake handshake = server.getBotHandshake();
        assertThat(handshake).isNotNull();
        assertThat(handshake.getType())
                .as("Handshake type should be BOT_HANDSHAKE")
                .isEqualTo(Message.Type.BOT_HANDSHAKE);
    }

    @Test
    @DisplayName("TR-API-BOT-002c BotHandshake contains correct bot info fields")
    @Tag("TR-API-BOT-002c")
    void test_TR_API_BOT_002c_handshake_contains_bot_info() {
        // Arrange
        var bot = new TestBot();

        // Act - start bot and wait for handshake
        var thread = new Thread(bot::start);
        thread.start();
        server.awaitBotHandshake(2000);

        // Assert - handshake fields match BotInfo
        BotHandshake handshake = server.getBotHandshake();
        assertThat(handshake).isNotNull();

        assertThat(handshake.getName())
                .as("Handshake name should match BotInfo")
                .isEqualTo(botInfo.getName());

        assertThat(handshake.getVersion())
                .as("Handshake version should match BotInfo")
                .isEqualTo(botInfo.getVersion());

        assertThat(handshake.getAuthors())
                .as("Handshake authors should match BotInfo")
                .containsExactlyElementsOf(botInfo.getAuthors());

        assertThat(handshake.getDescription())
                .as("Handshake description should match BotInfo")
                .isEqualTo(botInfo.getDescription());

        assertThat(handshake.getHomepage())
                .as("Handshake homepage should match BotInfo")
                .isEqualTo(botInfo.getHomepage());

        assertThat(handshake.getCountryCodes())
                .as("Handshake country codes should match BotInfo")
                .containsExactlyInAnyOrderElementsOf(botInfo.getCountryCodes());

        assertThat(handshake.getGameTypes())
                .as("Handshake game types should match BotInfo")
                .containsExactlyInAnyOrderElementsOf(botInfo.getGameTypes());

        assertThat(handshake.getPlatform())
                .as("Handshake platform should match BotInfo")
                .isEqualTo(botInfo.getPlatform());

        assertThat(handshake.getProgrammingLang())
                .as("Handshake programming language should match BotInfo")
                .isEqualTo(botInfo.getProgrammingLang());
    }

    @Test
    @DisplayName("TR-API-BOT-002d BotHandshake contains session ID from server")
    @Tag("TR-API-BOT-002d")
    void test_TR_API_BOT_002d_handshake_contains_session_id() {
        // Arrange
        var bot = new TestBot();

        // Act - start bot and wait for handshake
        var thread = new Thread(bot::start);
        thread.start();
        server.awaitBotHandshake(2000);

        // Assert - handshake contains session ID from server
        BotHandshake handshake = server.getBotHandshake();
        assertThat(handshake).isNotNull();
        assertThat(handshake.getSessionId())
                .as("Handshake should contain session ID from server")
                .isEqualTo(MockedServer.SESSION_ID);
    }

    @Test
    @DisplayName("TR-API-BOT-002e BotHandshake contains initial position when specified")
    @Tag("TR-API-BOT-002e")
    void test_TR_API_BOT_002e_handshake_contains_initial_position() {
        // Arrange
        var bot = new TestBot();

        // Act - start bot and wait for handshake
        var thread = new Thread(bot::start);
        thread.start();
        server.awaitBotHandshake(2000);

        // Assert - handshake contains initial position
        BotHandshake handshake = server.getBotHandshake();
        assertThat(handshake).isNotNull();
        assertThat(handshake.getInitialPosition())
                .as("Handshake should contain initial position")
                .isNotNull();

        var initialPos = handshake.getInitialPosition();
        assertThat(initialPos.getX())
                .as("Initial position X should match")
                .isEqualTo(10.0);
        assertThat(initialPos.getY())
                .as("Initial position Y should match")
                .isEqualTo(20.0);
        assertThat(initialPos.getDirection())
                .as("Initial position direction should match")
                .isEqualTo(30.0);
    }

    @Test
    @DisplayName("TR-API-BOT-002f Bot is not a droid by default")
    @Tag("TR-API-BOT-002f")
    void test_TR_API_BOT_002f_bot_is_not_droid_by_default() {
        // Arrange
        var bot = new TestBot();

        // Act - start bot and wait for handshake
        var thread = new Thread(bot::start);
        thread.start();
        server.awaitBotHandshake(2000);

        // Assert - isDroid is false for regular bot
        BotHandshake handshake = server.getBotHandshake();
        assertThat(handshake).isNotNull();
        assertThat(handshake.getIsDroid())
                .as("Regular bot should not be marked as droid")
                .isFalse();
    }
}
