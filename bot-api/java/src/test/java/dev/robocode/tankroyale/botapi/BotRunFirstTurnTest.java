package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.events.SkippedTurnEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import test_utils.MockedServer;

import java.util.concurrent.atomic.AtomicBoolean;

import static dev.robocode.tankroyale.botapi.Constants.MAX_RADAR_TURN_RATE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression tests for issue #202: first-turn skip caused by bot thread starting during turn 1.
 *
 * <p>The pre-warm fix starts the bot thread at {@code round-started} (before any tick).
 * The thread blocks in {@code waitUntilFirstTickArrived()} until the server sends tick 1,
 * after which {@code run()} is called with valid bot state already available.
 *
 * <h2>How 004a proves the bug</h2>
 * The server sends {@code ROUND_STARTED} but holds tick 1 back until the test releases it.
 * With the pre-warm fix the bot thread is already alive (isRunning=true) at that moment.
 * Without the fix the thread has not started yet (isRunning=false), so the assertion fails.
 */
@Tag("TCK")
@Tag("TR-API-TCK-004")
@DisplayName("TR-API-TCK-004 First-turn state availability (regression: issue #202)")
class BotRunFirstTurnTest extends AbstractBotTest {

    /**
     * Spins its radar every turn. Mirrors {@code RadarSpinBot} in {@code bot-api/tests/bots/java/}.
     * The inner class is used here so the MockedServer URL can be injected automatically.
     */
    private static class RadarSpinBot extends Bot {

        final AtomicBoolean skippedFirstTurn = new AtomicBoolean(false);

        RadarSpinBot() {
            super(botInfo, MockedServer.getServerUrl());
        }

        @Override
        public void run() {
            while (isRunning()) {
                setTurnRadarLeft(MAX_RADAR_TURN_RATE);
                go();
            }
        }

        @Override
        public void onSkippedTurn(SkippedTurnEvent event) {
            if (event.getTurnNumber() == 1) {
                skippedFirstTurn.set(true);
            }
        }
    }

    /**
     * TR-API-TCK-004a — proves the pre-warm fix.
     *
     * <p>The server holds tick 1 after sending ROUND_STARTED. With the pre-warm fix the bot
     * thread must already be alive (isRunning=true) before tick 1 arrives. Without the fix the
     * thread is not started at round-started, so isRunning stays false and the assertion fails.
     */
    @Test
    @Tag("TCK")
    @Tag("TR-API-TCK-004")
    @DisplayName("TR-API-TCK-004a bot thread is pre-warmed before tick 1 (proves issue #202 fix)")
    void test_TR_API_TCK_004a_bot_thread_pre_warmed_before_tick_1() throws InterruptedException {
        server.holdTick(); // hold tick 1 — server will wait after ROUND_STARTED
        var bot = new RadarSpinBot();
        startAsync(bot);

        // Poll until the bot thread becomes running (pre-warm) or the deadline expires.
        // The generous 1500 ms window covers: handshake → game-started → bot-ready →
        // round-started processing → thread start. Without the pre-warm fix this never
        // becomes true (thread only starts after tick 1 is received).
        long deadline = System.currentTimeMillis() + 1500;
        while (!bot.baseBotInternals.isRunning() && System.currentTimeMillis() < deadline) {
            Thread.sleep(5);
        }

        assertThat(bot.baseBotInternals.isRunning())
                .as("bot thread must be pre-warmed at round-started, not at tick 1 (regression: issue #202)")
                .isTrue();

        server.releaseTick(); // unblock server so bot can finish the turn
        server.continueBotIntent();
        awaitBotIntent();
    }

    /**
     * TR-API-TCK-004b — correctness: intent from run() contains the radar turn rate.
     *
     * <p>The pre-warm fix sends an initial empty intent before run() executes
     * (to prevent turn-1 skip). The intent carrying values set in run() is the
     * second one the server receives.
     */
    @Test
    @Tag("TCK")
    @Tag("TR-API-TCK-004")
    @DisplayName("TR-API-TCK-004b first intent contains radar turn rate set in run()")
    void test_TR_API_TCK_004b_first_intent_contains_radar_turn_rate() {
        var bot = new RadarSpinBot();
        startAsync(bot);
        awaitGameStarted(bot);
        awaitTick(bot);

        // Drain the pre-warm initial intent (empty default sent to prevent turn-1 skip)
        server.continueBotIntent();
        awaitBotIntent();

        // Now capture the intent from run() which carries the radar turn rate
        server.resetBotIntentLatch();
        server.continueBotIntent();
        awaitBotIntent();

        assertThat(server.getBotIntent().getRadarTurnRate())
                .as("intent from run() must include the radar turn rate (regression: issue #202)")
                .isEqualTo((double) MAX_RADAR_TURN_RATE);
    }
}
