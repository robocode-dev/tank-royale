package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import test_utils.MockedServer;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static test_utils.EnvironmentVariables.*;

@Tag("LEGACY")
@ExtendWith(SystemStubsExtension.class)
class BaseBotConstructorTypeParsingTest extends AbstractBotTest {

    static class TestBot extends BaseBot {
        TestBot() { super(); }
    }

    @SystemStub
    final EnvironmentVariables envVars = new EnvironmentVariables(
            SERVER_URL, "ws://localhost:" + MockedServer.PORT,
            BOT_NAME, "TypeParseBot",
            BOT_VERSION, "1.0",
            BOT_AUTHORS, "Alice, Bob"
    );

    @Test
    @DisplayName("TR-API-BOT-001d Type parsing/normalization: ints/bools parsed consistently; trimming/whitespace handling")
    @Tag("BOT")
    @Tag("TR-API-BOT-001d")
    void test_TR_API_BOT_001d_type_parsing_normalization() throws InterruptedException {
        // Arrange: TEAM_ID and BOT_INITIAL_POS with extra whitespace
        envVars.set(BOT_NAME, "TypeParseBot");
        envVars.set(BOT_VERSION, "1.0");
        envVars.set(BOT_AUTHORS, "Alice, Bob");
        envVars.set(SERVER_URL, "ws://localhost:" + MockedServer.PORT);
        envVars.set("TEAM_ID", "  42  ");
        envVars.set(BOT_INITIAL_POS, "  10, 20, 30  ");

        // Act
        var bot = new TestBot();
        startAsync(bot);
        assertThat(server.awaitConnection(5000)).isTrue();
        awaitBotHandshake();
        var handshake = server.getBotHandshake();

        // Assert TEAM_ID parsed as integer and InitialPosition mapped (non-null)
        assertThat(handshake).isNotNull();
        assertThat(handshake.getTeamId()).isEqualTo(42);
        assertThat(handshake.getInitialPosition()).isNotNull();
    }
}
