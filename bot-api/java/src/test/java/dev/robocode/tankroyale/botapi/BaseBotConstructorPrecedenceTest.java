package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import test_utils.MockedServer;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static test_utils.EnvironmentVariables.*;

/**
 * TR-API-BOT-001c Precedence tests for Java: explicit args > Java System properties > ENV.
 */
import org.junit.jupiter.api.Disabled;

@ExtendWith(SystemStubsExtension.class)
@Disabled("Superseded by precedence test integrated into BaseBotConstructorTest; disabling to avoid server conflicts")
class BaseBotConstructorPrecedenceTest extends AbstractBotTest {

    static class TestBot extends BaseBot {
        TestBot() { super(); }
        TestBot(BotInfo botInfo, URI serverUrl) { super(botInfo, serverUrl); }
    }

    @SystemStub
    final EnvironmentVariables envVars = new EnvironmentVariables(
            SERVER_URL, "ws://127.0.0.1:65535", // bogus
            BOT_NAME, "TestBot",
            BOT_VERSION, "1.0",
            BOT_AUTHORS, "A,B"
    );

    @AfterEach
    void tearDown() {
        // Clear any system properties possibly set by a test
        System.clearProperty("SERVER_URL");
    }

    @Test
    void test_TR_API_BOT_001c_system_property_over_env_for_server_url() throws Exception {
        // Arrange: ENV points to a bogus port; system property points to the mocked server
        System.setProperty("SERVER_URL", "ws://127.0.0.1:" + MockedServer.PORT);

        // Act
        var bot = new TestBot();
        startAsync(bot);

        // Assert: should connect using system property URL (not ENV)
        assertThat(server.awaitConnection(10000)).isTrue();
    }

}
