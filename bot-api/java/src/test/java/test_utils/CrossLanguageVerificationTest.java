package test_utils;

import dev.robocode.tankroyale.botapi.BaseBot;
import dev.robocode.tankroyale.botapi.BotInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CrossLanguageVerificationTest {

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
    void verifyStateSynchronizationIdentical() throws InterruptedException {
        var bot = new TestBot();
        new Thread(bot::start).start();

        // 1. Verify AwaitBotReady
        assertThat(server.awaitBotReady(30000)).as("awaitBotReady should succeed").isTrue();

        // Give the bot a chance to be ready for the next tick
        Thread.sleep(500);

        // 2. Verify initial state (based on MockedServer defaults)
        assertThat(bot.getEnergy()).isEqualTo(MockedServer.BOT_ENERGY);
        assertThat(bot.getSpeed()).isEqualTo(MockedServer.BOT_SPEED);
        assertThat(bot.getDirection()).isEqualTo(MockedServer.BOT_DIRECTION);
        assertThat(bot.getGunDirection()).isEqualTo(MockedServer.BOT_GUN_DIRECTION);
        assertThat(bot.getRadarDirection()).isEqualTo(MockedServer.BOT_RADAR_DIRECTION);

        // 3. Update all states via setBotStateAndAwaitTick
        double newEnergy = 42.0;
        double newGunHeat = 1.5;
        double newSpeed = 6.5;
        double newDirection = 180.0;
        double newGunDirection = 90.0;
        double newRadarDirection = 270.0;

        boolean success = server.setBotStateAndAwaitTick(
                newEnergy, newGunHeat, newSpeed,
                newDirection, newGunDirection, newRadarDirection
        );

        assertThat(success).as("setBotStateAndAwaitTick should succeed").isTrue();

        // 4. Verify bot reflects new state
        assertThat(bot.getEnergy()).isEqualTo(newEnergy);
        assertThat(bot.getGunHeat()).isEqualTo(newGunHeat);
        assertThat(bot.getSpeed()).isEqualTo(newSpeed);
        assertThat(bot.getDirection()).isEqualTo(newDirection);
        assertThat(bot.getGunDirection()).isEqualTo(newGunDirection);
        assertThat(bot.getRadarDirection()).isEqualTo(newRadarDirection);

        // 5. Verify Turn Number increment
        int currentTurn = bot.getTurnNumber();
        server.setBotStateAndAwaitTick(null, null, null, null, null, null);
        assertThat(bot.getTurnNumber()).isEqualTo(currentTurn + 1);

        // 6. Manual Turn Number setting
        server.setTurnNumber(500);
        server.setBotStateAndAwaitTick(null, null, null, null, null, null);
        assertThat(bot.getTurnNumber()).isEqualTo(500);
    }

    private static class TestBot extends dev.robocode.tankroyale.botapi.Bot {
        TestBot() {
            super(BotInfo.builder()
                    .setName("VerificationBot")
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

        @Override
        public void run() {
            while (isRunning()) {
                setFire(1.0);
                go();
            }
        }
    }
}
