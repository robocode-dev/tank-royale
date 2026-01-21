

package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("TR-API-CMD-002 Fire command")
@Tag("CMD")
class CommandsFireTest extends AbstractBotTest {

    @Test
    @DisplayName("TR-API-CMD-002 Fire power bounds")
    void test_TR_API_CMD_002_fire_power_bounds() {
        var bot = startAndAwaitGameStarted();
        goAsync(bot); // Make bot active so it responds to ticks

        // Ensure gun heat is 0 so we can fire
        assertThat(server.setBotStateAndAwaitTick(null, 0.0, null, null, null, null)).isTrue();

        // 1. Firepower below 0.1 -> clamped to 0.1
        System.out.println("TEST: Setting fire(0.0)");
        server.resetBotIntentLatch();
        bot.setFire(0.0);
        goAsync(bot);
        awaitBotIntent();
        var intent1 = server.getBotIntent();

        System.out.println("TEST: Intent1: " + intent1);
        assertThat(intent1.getFirepower()).isEqualTo(0.1);

        server.resetBotIntentLatch();

        // 2. Firepower above 3.0 -> clamped to 3.0
        System.out.println("TEST: Setting fire(5.0)");
        bot.setFire(5.0);
        goAsync(bot);
        awaitBotIntent();
        var intent2 = server.getBotIntent();

        System.out.println("TEST: Intent2: " + intent2);
        assertThat(intent2.getFirepower()).isEqualTo(3.0);
    }

    @Test
    @DisplayName("TR-API-CMD-002 Fire cooldown prevents firing")
    void test_TR_API_CMD_002_fire_cooldown() {
        var bot = startAndAwaitGameStarted();

        // Set gun heat > 0
        assertThat(server.setBotStateAndAwaitTick(null, 1.0, null, null, null, null)).isTrue();

        server.resetBotIntentLatch();

        // setFire() should return false when gunHeat > 0
        bot.setFire(1.0);
        goAsync(bot);
        awaitBotIntent();
        var intent = server.getBotIntent();

        assertThat(intent.getFirepower()).isNull();
    }

    @Test
    @DisplayName("TR-API-CMD-002 Fire energy limit prevents firing")
    void test_TR_API_CMD_002_fire_energy_limit() {
        var bot = startAndAwaitGameStarted();

        // Set energy < firepower
        assertThat(server.setBotStateAndAwaitTick(0.5, 0.0, null, null, null, null)).isTrue();

        server.resetBotIntentLatch();

        // setFire(1.0) should return false when energy is 0.5
        bot.setFire(1.0);
        goAsync(bot);
        awaitBotIntent();
        var intent = server.getBotIntent();

        assertThat(intent.getFirepower()).isNull();
    }

    @Test
    @DisplayName("TR-API-CMD-002 Fire with NaN throws exception")
    @Tag("TR-API-CMD-002")
    void test_TR_API_CMD_002_fire_nan_throws() {
        var bot = startAndAwaitGameStarted();

        assertThrows(IllegalArgumentException.class, () -> bot.setFire(Double.NaN));
    }
}
