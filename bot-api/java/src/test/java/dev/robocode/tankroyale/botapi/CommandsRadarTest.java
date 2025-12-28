package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.schema.BotIntent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TR-API-CMD-003 Radar/Scan commands")
class CommandsRadarTest extends AbstractBotTest {

    @Test
    @Tag("CMD")
    @Tag("TR-API-CMD-003")
    void test_rescan_intent() {
        // Arrange
        var bot = start();
        awaitBotHandshake();
        awaitGameStarted(bot);

        // Act
        bot.setRescan();
        goAsync(bot);
        awaitBotIntent();

        // Assert
        BotIntent intent = server.getBotIntent();
        assertThat(intent).isNotNull();
        assertThat(intent.getRescan()).isTrue();
    }

    @Test
    @Tag("CMD")
    @Tag("TR-API-CMD-003")
    void test_blocking_rescan() {
        // Arrange
        var bot = new IBotTestBot();
        startAsync(bot);
        awaitBotHandshake();
        awaitGameStarted(bot);

        // Act (rescan is blocking and calls go() internally)
        new Thread(() -> {
            try {
                bot.rescan();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        awaitBotIntent();

        // Assert
        BotIntent intent = server.getBotIntent();
        assertThat(intent).isNotNull();
        assertThat(intent.getRescan()).isTrue();
    }

    @Test
    @Tag("CMD")
    @Tag("TR-API-CMD-003")
    void test_adjust_radar_body() {
        // Arrange
        var bot = start();
        awaitBotHandshake();
        awaitGameStarted(bot);

        // Act
        bot.setAdjustRadarForBodyTurn(true);
        goAsync(bot);
        awaitBotIntent();

        // Assert
        BotIntent intent = server.getBotIntent();
        assertThat(intent).isNotNull();
        assertThat(intent.getAdjustRadarForBodyTurn()).isTrue();

        // Reset for next step
        server.resetBotIntentLatch();

        // Act
        bot.setAdjustRadarForBodyTurn(false);
        goAsync(bot);
        awaitBotIntent();

        // Assert
        intent = server.getBotIntent();
        assertThat(intent.getAdjustRadarForBodyTurn()).isFalse();
    }

    @Test
    @Tag("CMD")
    @Tag("TR-API-CMD-003")
    void test_adjust_radar_gun() {
        // Arrange
        var bot = start();
        awaitBotHandshake();
        awaitGameStarted(bot);

        // Act
        bot.setAdjustRadarForGunTurn(true);
        goAsync(bot);
        awaitBotIntent();

        // Assert
        BotIntent intent = server.getBotIntent();
        assertThat(intent).isNotNull();
        assertThat(intent.getAdjustRadarForGunTurn()).isTrue();
        assertThat(intent.getFireAssist()).isFalse();

        // Reset for next step
        server.resetBotIntentLatch();

        // Act
        bot.setAdjustRadarForGunTurn(false);
        goAsync(bot);
        awaitBotIntent();

        // Assert
        intent = server.getBotIntent();
        assertThat(intent.getAdjustRadarForGunTurn()).isFalse();
        assertThat(intent.getFireAssist()).isTrue();
    }

    private static class IBotTestBot extends Bot {
        IBotTestBot() {
            super(botInfo, test_utils.MockedServer.getServerUrl());
        }

        @Override
        public void run() {
            // Do nothing, we will call rescan from outside
        }
    }
}
