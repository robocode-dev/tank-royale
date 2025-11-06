package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.schema.BotIntent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static dev.robocode.tankroyale.botapi.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TR-API-CMD-001 Movement commands")
class CommandsMovementTest extends AbstractBotTest {

    @Test
    @Tag("CMD")
    @Tag("TR-API-CMD-001")
    void givenMovementCommandsSet_whenGo_thenIntentContainsClampedValues() {
        // Arrange
        var bot = start();
        awaitBotHandshake();
        awaitGameStarted(bot);
        // Act: set values beyond limits to verify clamping
        bot.setTurnRate(999); // > MAX_TURN_RATE
        bot.setGunTurnRate(-999); // < -MAX_GUN_TURN_RATE
        bot.setRadarTurnRate(1000); // > MAX_RADAR_TURN_RATE
        bot.setTargetSpeed(123); // > MAX_SPEED

        // Trigger sending of intent on next go after settings
        goAsync(bot);
        awaitBotIntent();

        // Assert
        BotIntent intent = server.getBotIntent();
        assertThat(intent).isNotNull();
        assertThat(intent.getTurnRate()).isEqualTo((double) MAX_TURN_RATE);
        assertThat(intent.getGunTurnRate()).isEqualTo(-(double) MAX_GUN_TURN_RATE);
        assertThat(intent.getRadarTurnRate()).isEqualTo((double) MAX_RADAR_TURN_RATE);
        assertThat(intent.getTargetSpeed()).isEqualTo((double) MAX_SPEED);
    }

    @Test
    @Tag("CMD")
    @Tag("TR-API-CMD-001")
    void givenNaNValues_whenSettingMovementCommands_thenThrowIllegalArgumentException() {
        var bot = startAndAwaitGameStarted();

        assertThrows(IllegalArgumentException.class, () -> bot.setTurnRate(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> bot.setGunTurnRate(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> bot.setRadarTurnRate(Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> bot.setTargetSpeed(Double.NaN));
    }
}
