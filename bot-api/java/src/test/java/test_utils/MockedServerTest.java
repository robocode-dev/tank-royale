package test_utils;

import dev.robocode.tankroyale.botapi.BaseBot;
import dev.robocode.tankroyale.botapi.BotInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MockedServerTest {

    private MockedServer server;

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
    void awaitBotReady_shouldSucceed() {
        var bot = new TestBot();
        new Thread(bot::start).start();

        boolean ready = server.awaitBotReady(30000);
        assertThat(ready).as("Bot should be ready").isTrue();
    }

    @Test
    void setBotStateAndAwaitTick_shouldUpdateStateAndReturnTrue() {
        var bot = new TestBot();
        new Thread(bot::start).start();

        assertThat(server.awaitBotReady(30000)).as("Bot should be ready").isTrue();

        double newEnergy = 50.0;
        double newSpeed = 4.0;

        boolean success = server.setBotStateAndAwaitTick(newEnergy, null, newSpeed, null, null, null);

        assertThat(success).as("setBotStateAndAwaitTick should succeed").isTrue();
        assertThat(bot.getEnergy()).isEqualTo(newEnergy);
        assertThat(bot.getSpeed()).isEqualTo(newSpeed);
    }

    private static class TestBot extends BaseBot {
        TestBot() {
            super(BotInfo.builder()
                    .setName("TestBot")
                    .setVersion("1.0")
                    .addAuthor("Author")
                    .setDescription("Description")
                    .setHomepage("https://test.com")
                    .addCountryCode("us")
                    .addGameType("classic")
                    .setPlatform("JVM")
                    .setProgrammingLang("Java")
                    .build(), MockedServer.getServerUrl());
        }
    }
}
