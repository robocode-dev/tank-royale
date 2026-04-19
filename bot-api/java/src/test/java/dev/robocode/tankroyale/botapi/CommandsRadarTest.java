package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.schema.BotIntent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test_utils.MockedServer;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for radar commands (TR-API-CMD-003).
 */
@Tag("CMD")
@Tag("TR-API-CMD-003")
@DisplayName("Radar Commands (TR-API-CMD-003)")
class CommandsRadarTest extends AbstractBotTest {

    private static class RadarTestBot extends Bot {
        RadarTestBot() {
            super(botInfo, MockedServer.getServerUrl());
        }
    }

    private RadarTestBot startRadarBot() {
        var bot = new RadarTestBot();
        startAsync(bot);
        // We must wait for the game to start and the first tick (Turn 1).
        awaitGameStarted(bot);
        awaitTick(bot);

        // No need to drain Turn 1 here, we'll do it in awaitExpectedIntent if needed.
        return bot;
    }

    /**
     * Helper to wait for an intent that satisfies a predicate.
     * This handles draining multiple intents if the bot is looping automatically.
     */
    private void awaitExpectedIntent(Predicate<BotIntent> predicate) {
        long start = System.currentTimeMillis();
        int count = 0;
        while (System.currentTimeMillis() - start < 10000) {
            count++;
            server.continueBotIntent();
            if (server.awaitBotIntent(2000)) {
                BotIntent intent = server.getBotIntent();
                System.out.println("Received intent " + count + ": rescan=" + intent.getRescan() 
                    + ", adjRadarBody=" + intent.getAdjustRadarForBodyTurn()
                    + ", adjRadarGun=" + intent.getAdjustRadarForGunTurn());
                if (predicate.test(intent)) {
                    return;
                }
                server.resetBotIntentLatch();
            } else {
                System.out.println("Timed out waiting for intent " + count);
            }
        }
        assertThat(false).as("Timed out waiting for expected intent after " + count + " attempts").isTrue();
    }

    @Test
    @DisplayName("setRescan() sets the rescan flag in the intent")
    void testRescanIntent() {
        var bot = startRadarBot();

        bot.setRescan();
        awaitExpectedIntent(intent -> Boolean.TRUE.equals(intent.getRescan()));
    }

    @Test
    @DisplayName("rescan() blocking call sets the rescan flag in the intent")
    void testBlockingRescan() {
        var bot = startRadarBot();

        // Run rescan in a separate thread because it's blocking
        goAsync(bot::rescan);
        awaitExpectedIntent(intent -> Boolean.TRUE.equals(intent.getRescan()));
    }

    @Test
    @DisplayName("setAdjustRadarForBodyTurn(true) sets the flag in the intent")
    void testAdjustRadarBodyTrue() {
        var bot = startRadarBot();

        bot.setAdjustRadarForBodyTurn(true);
        awaitExpectedIntent(intent -> Boolean.TRUE.equals(intent.getAdjustRadarForBodyTurn()));
    }

    @Test
    @DisplayName("setAdjustRadarForBodyTurn(false) sets the flag in the intent")
    void testAdjustRadarBodyFalse() {
        var bot = startRadarBot();

        // Set to true first, then to false
        bot.setAdjustRadarForBodyTurn(true);
        awaitExpectedIntent(intent -> Boolean.TRUE.equals(intent.getAdjustRadarForBodyTurn()));

        bot.setAdjustRadarForBodyTurn(false);
        awaitExpectedIntent(intent -> Boolean.FALSE.equals(intent.getAdjustRadarForBodyTurn()));
    }

    @Test
    @DisplayName("setAdjustRadarForGunTurn(true) sets the flag in the intent")
    void testAdjustRadarGunTrue() {
        var bot = startRadarBot();

        bot.setAdjustRadarForGunTurn(true);
        awaitExpectedIntent(intent -> Boolean.TRUE.equals(intent.getAdjustRadarForGunTurn()));
    }

    @Test
    @DisplayName("setAdjustRadarForGunTurn(false) sets the flag in the intent")
    void testAdjustRadarGunFalse() {
        var bot = startRadarBot();

        // Set to true first, then to false
        bot.setAdjustRadarForGunTurn(true);
        awaitExpectedIntent(intent -> Boolean.TRUE.equals(intent.getAdjustRadarForGunTurn()));

        bot.setAdjustRadarForGunTurn(false);
        awaitExpectedIntent(intent -> Boolean.FALSE.equals(intent.getAdjustRadarForGunTurn()));
    }
}
