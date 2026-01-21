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
        // Ensure all movement limits are unset so intent is always accepted
        server.setSpeedMinLimit(Double.NEGATIVE_INFINITY);
        server.setSpeedMaxLimit(Double.POSITIVE_INFINITY);
        server.setDirectionMinLimit(Double.NEGATIVE_INFINITY);
        server.setDirectionMaxLimit(Double.POSITIVE_INFINITY);
        server.setGunDirectionMinLimit(Double.NEGATIVE_INFINITY);
        server.setGunDirectionMaxLimit(Double.POSITIVE_INFINITY);
        server.setRadarDirectionMinLimit(Double.NEGATIVE_INFINITY);
        server.setRadarDirectionMaxLimit(Double.POSITIVE_INFINITY);
        awaitBotHandshake();
        awaitGameStarted(bot);
        // Act: set values beyond limits to verify clamping
        System.out.println("Setting movement commands...");
        bot.setTurnRate(999); // > MAX_TURN_RATE
        bot.setGunTurnRate(-999); // < -MAX_GUN_TURN_RATE
        bot.setRadarTurnRate(1000); // > MAX_RADAR_TURN_RATE
        bot.setTargetSpeed(123); // > MAX_SPEED

        // Trigger sending of intent on next go after settings
        System.out.println("Calling goAsync(bot)...");
        goAsync(bot);
        System.out.println("Waiting for bot intent...");
        awaitBotIntent();

        // Assert
        BotIntent intent = server.getBotIntent();
        System.out.println("Intent received: " + intent);
        assertThat(intent).isNotNull();
        if (intent != null) {
            System.out.printf("TurnRate: %s, GunTurnRate: %s, RadarTurnRate: %s, TargetSpeed: %s\n",
                    intent.getTurnRate(), intent.getGunTurnRate(), intent.getRadarTurnRate(), intent.getTargetSpeed());
        }
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
