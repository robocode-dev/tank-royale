package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TR-API-BOT-003 Start/Stop/Pause/Resume tests.
 * Verifies correct state flags, idempotence, and event firing order.
 */
@Tag("BOT")
@Tag("TR-API-BOT-003")
class StartStopPauseResumeTest extends AbstractBotTest {

    // ========== setStop() tests ==========

    @Test
    @DisplayName("TR-API-BOT-003a setStop() sets isStopped flag to true")
    @Tag("TR-API-BOT-003a")
    void test_TR_API_BOT_003a_setStop_sets_isStopped_flag() {
        // Arrange
        var bot = startAndAwaitGameStarted();

        // Act & Assert
        var stoppedBefore = bot.isStopped();
        bot.setStop();
        var stoppedAfter = bot.isStopped();

        assertThat(stoppedBefore)
                .as("isStopped() should be false before setStop()")
                .isFalse();
        assertThat(stoppedAfter)
                .as("isStopped() should be true after setStop()")
                .isTrue();
    }

    @Test
    @DisplayName("TR-API-BOT-003b setStop() is idempotent - calling twice has same effect")
    @Tag("TR-API-BOT-003b")
    void test_TR_API_BOT_003b_setStop_is_idempotent() {
        // Arrange
        var bot = startAndAwaitGameStarted();

        // Act
        bot.setStop();
        var stoppedAfterFirstStop = bot.isStopped();
        bot.setStop(); // call again - should be idempotent
        var stoppedAfterSecondStop = bot.isStopped();

        // Assert
        assertThat(stoppedAfterFirstStop)
                .as("isStopped() should be true after first setStop()")
                .isTrue();
        assertThat(stoppedAfterSecondStop)
                .as("isStopped() should still be true after second setStop()")
                .isTrue();
    }

    @Test
    @DisplayName("TR-API-BOT-003c setStop() zeroes movement rates in intent")
    @Tag("TR-API-BOT-003c")
    void test_TR_API_BOT_003c_setStop_zeroes_movement() {
        // Arrange
        var bot = startAndAwaitGameStarted();

        // Act - Set some movement values
        bot.setTurnRate(5.0);
        bot.setTargetSpeed(8.0);

        // Stop should set them to zero
        bot.setStop();

        // Assert - after setStop, the turn rate should be 0
        assertThat(bot.getTurnRate())
                .as("Turn rate should be 0 after setStop()")
                .isEqualTo(0.0);
    }

    // ========== setResume() tests ==========

    @Test
    @DisplayName("TR-API-BOT-003d setResume() sets isStopped flag to false")
    @Tag("TR-API-BOT-003d")
    void test_TR_API_BOT_003d_setResume_clears_isStopped_flag() {
        // Arrange
        var bot = startAndAwaitGameStarted();

        // Act
        bot.setStop();
        var stoppedAfterStop = bot.isStopped();
        bot.setResume();
        var stoppedAfterResume = bot.isStopped();

        // Assert
        assertThat(stoppedAfterStop)
                .as("isStopped() should be true after setStop()")
                .isTrue();
        assertThat(stoppedAfterResume)
                .as("isStopped() should be false after setResume()")
                .isFalse();
    }

    @Test
    @DisplayName("TR-API-BOT-003e setResume() has no effect when not stopped")
    @Tag("TR-API-BOT-003e")
    void test_TR_API_BOT_003e_setResume_no_effect_when_not_stopped() {
        // Arrange
        var bot = startAndAwaitGameStarted();

        // Act - Not stopped initially
        var stoppedBeforeResume = bot.isStopped();
        bot.setResume(); // should have no effect when not stopped
        var stoppedAfterResume = bot.isStopped();

        // Assert
        assertThat(stoppedBeforeResume)
                .as("isStopped() should be false before setResume()")
                .isFalse();
        assertThat(stoppedAfterResume)
                .as("isStopped() should still be false after setResume() when not stopped")
                .isFalse();
    }

    @Test
    @DisplayName("TR-API-BOT-003f setResume() restores saved movement values")
    @Tag("TR-API-BOT-003f")
    void test_TR_API_BOT_003f_setResume_restores_movement() {
        // Arrange
        final double expectedTurnRate = 5.0;
        var bot = startAndAwaitGameStarted();

        // Act - Set movement values
        bot.setTurnRate(expectedTurnRate);

        // Stop saves values and zeroes them
        bot.setStop();
        var turnRateAfterStop = bot.getTurnRate();

        // Resume should restore values
        bot.setResume();
        var turnRateAfterResume = bot.getTurnRate();

        // Assert
        assertThat(turnRateAfterStop)
                .as("Turn rate should be 0 after setStop()")
                .isEqualTo(0.0);
        assertThat(turnRateAfterResume)
                .as("Turn rate should be restored after setResume()")
                .isEqualTo(expectedTurnRate);
    }

    // ========== setStop(boolean overwrite) tests ==========

    @Test
    @DisplayName("TR-API-BOT-003g setStop(false) does not overwrite saved values")
    @Tag("TR-API-BOT-003g")
    void test_TR_API_BOT_003g_setStop_false_does_not_overwrite() {
        // Arrange
        final double firstTurnRate = 5.0;
        final double secondTurnRate = 3.0;
        var bot = startAndAwaitGameStarted();

        // Act - Set first movement value and stop
        bot.setTurnRate(firstTurnRate);
        bot.setStop();

        // Set different value and stop again without overwrite
        bot.setTurnRate(secondTurnRate);
        bot.setStop(false); // should NOT overwrite saved values

        // Resume should restore FIRST values (not second)
        bot.setResume();
        var turnRateAfterResume = bot.getTurnRate();

        // Assert
        assertThat(turnRateAfterResume)
                .as("Turn rate should be first saved value (not overwritten)")
                .isEqualTo(firstTurnRate);
    }

    @Test
    @DisplayName("TR-API-BOT-003h setStop(true) overwrites saved values")
    @Tag("TR-API-BOT-003h")
    void test_TR_API_BOT_003h_setStop_true_overwrites() {
        // Arrange
        final double firstTurnRate = 5.0;
        final double secondTurnRate = 3.0;
        var bot = startAndAwaitGameStarted();

        // Act - Set first movement value and stop
        bot.setTurnRate(firstTurnRate);
        bot.setStop();

        // Set different value and stop again WITH overwrite
        bot.setTurnRate(secondTurnRate);
        bot.setStop(true); // SHOULD overwrite saved values

        // Resume should restore SECOND values (overwritten)
        bot.setResume();
        var turnRateAfterResume = bot.getTurnRate();

        // Assert
        assertThat(turnRateAfterResume)
                .as("Turn rate should be second saved value (overwritten)")
                .isEqualTo(secondTurnRate);
    }

    // ========== start() tests ==========

    @Test
    @DisplayName("TR-API-BOT-003i start() connects to server")
    @Tag("TR-API-BOT-003i")
    void test_TR_API_BOT_003i_start_connects_to_server() {
        // Arrange & Act
        start();

        // Assert - server receives connection
        boolean connected = server.awaitConnection(2000);
        assertThat(connected)
                .as("Bot should connect to server within timeout")
                .isTrue();
    }

    @Test
    @DisplayName("TR-API-BOT-003j start() triggers handshake")
    @Tag("TR-API-BOT-003j")
    void test_TR_API_BOT_003j_start_triggers_handshake() {
        // Arrange & Act
        start();

        // Assert - handshake received
        boolean handshakeReceived = server.awaitBotHandshake(2000);
        assertThat(handshakeReceived)
                .as("Bot handshake should be received after start()")
                .isTrue();
    }

    @Test
    @DisplayName("TR-API-BOT-003k start() triggers game started event")
    @Tag("TR-API-BOT-003k")
    void test_TR_API_BOT_003k_start_triggers_game_started() {
        // Arrange & Act
        var bot = start();
        awaitBotHandshake();

        // Assert - game started event received
        boolean gameStarted = server.awaitGameStarted(2000);
        assertThat(gameStarted)
                .as("Game should start after bot connects")
                .isTrue();
    }

    @Test
    @DisplayName("TR-API-BOT-003l Bot can access game setup after game started")
    @Tag("TR-API-BOT-003l")
    void test_TR_API_BOT_003l_bot_can_access_game_setup() {
        // Arrange & Act
        var bot = startAndAwaitGameStarted();

        // Assert - bot can access game setup
        assertThat(bot.getArenaWidth())
                .as("Bot should know arena width")
                .isGreaterThan(0);
        assertThat(bot.getArenaHeight())
                .as("Bot should know arena height")
                .isGreaterThan(0);
        assertThat(bot.getGameType())
                .as("Bot should know game type")
                .isNotNull();
    }

    @Test
    @DisplayName("TR-API-BOT-003m Bot isStopped is false initially")
    @Tag("TR-API-BOT-003m")
    void test_TR_API_BOT_003m_bot_not_stopped_initially() {
        // Arrange & Act
        var bot = startAndAwaitGameStarted();

        // Assert
        assertThat(bot.isStopped())
                .as("Bot should not be stopped initially")
                .isFalse();
    }
}
