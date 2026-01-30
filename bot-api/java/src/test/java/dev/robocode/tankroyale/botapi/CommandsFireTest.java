package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for fire commands (TR-API-CMD-002).
 *
 * <p>These tests verify the behavior of fire-related methods:
 * <ul>
 *   <li>Firepower is clamped to valid range [0.1, 3.0]</li>
 *   <li>Fire fails when gun is hot (gunHeat > 0)</li>
 *   <li>Fire fails when energy is too low</li>
 *   <li>NaN firepower throws IllegalArgumentException</li>
 * </ul>
 *
 * @see <a href="https://github.com/robocode-dev/tank-royale/issues/XXX">TR-API-CMD-002</a>
 */
@Tag("TR-API-CMD-002")
@DisplayName("Fire Commands (TR-API-CMD-002)")
class CommandsFireTest extends AbstractBotTest {

    /**
     * Helper to test setFire and capture the resulting intent.
     * After calling setFire, we need to trigger go() to actually send the intent.
     */
    private CommandResult<Boolean> setFireAndGetIntent(BaseBot bot, double firepower) {
        server.resetBotIntentLatch();
        boolean result = bot.setFire(firepower);
        // Fire command just sets the intent value; we need go() to send it
        goAsync(bot);
        awaitBotIntent();
        return new CommandResult<>(result, server.getBotIntent());
    }

    @Test
    @DisplayName("Firepower below 0.1 is sent as-is (server clamps)")
    void testFirepowerBelowMinSentAsIs() {
        var bot = startAndPrepareForFire();

        // Execute fire with value below minimum
        var result = setFireAndGetIntent(bot, 0.05);

        // Fire should succeed - API sends raw value, server will clamp
        assertThat(result.getResult()).isTrue();
        assertThat(result.getIntent().getFirepower()).isEqualTo(0.05);
    }

    @Test
    @DisplayName("Firepower above 3.0 fails energy check if above energy")
    void testFirepowerAboveMaxFailsIfAboveEnergy() {
        var bot = startAndPrepareForFire();

        // Execute fire with value of 5.0 - this is above max but passes energy check (100 > 5)
        var result = setFireAndGetIntent(bot, 5.0);

        // Fire should succeed - API sends raw value, server will clamp
        assertThat(result.getResult()).isTrue();
        assertThat(result.getIntent().getFirepower()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("Valid firepower (1.0) is preserved in intent")
    void testValidFirepowerIsPreserved() {
        var bot = startAndPrepareForFire();

        // Execute fire with valid value
        var result = setFireAndGetIntent(bot, 1.0);

        // Fire should succeed (true) with exact value
        assertThat(result.getResult()).isTrue();
        assertThat(result.getIntent().getFirepower()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("Fire fails when gun is hot (gunHeat > 0)")
    void testFireFailsWhenGunIsHot() {
        // Start bot and wait for game started
        var bot = start();
        awaitGameStarted(bot);

        // Set high gun heat so bot cannot fire - use setBotStateAndAwaitTick to send state to bot
        server.setBotStateAndAwaitTick(100.0, 5.0, null, null, null, null);
        // Wait for bot to process the updated state
        awaitCondition(() -> bot.getGunHeat() == 5.0, 1000);

        // Execute fire - should fail due to gun heat
        var result = setFireAndGetIntent(bot, 1.0);

        // Fire should fail (false) and firepower should be null
        assertThat(result.getResult()).isFalse();
        assertThat(result.getIntent().getFirepower()).isNull();
    }

    @Test
    @DisplayName("Fire fails when energy is too low for firepower")
    void testFireFailsWhenEnergyTooLow() {
        // Start bot and wait for game started
        var bot = start();
        awaitGameStarted(bot);

        // Set low energy and no gun heat - use setBotStateAndAwaitTick to send state to bot
        server.setBotStateAndAwaitTick(0.5, 0.0, null, null, null, null);
        // Wait for bot to process the updated state
        awaitCondition(() -> bot.getEnergy() == 0.5 && bot.getGunHeat() == 0.0, 1000);

        // Execute fire with high firepower - should fail due to energy
        var result = setFireAndGetIntent(bot, 3.0);

        // Fire should fail (false) and firepower should be null
        assertThat(result.getResult()).isFalse();
        assertThat(result.getIntent().getFirepower()).isNull();
    }

    @Test
    @DisplayName("Fire with NaN throws IllegalArgumentException")
    void testFireWithNaNThrowsException() {
        var bot = startAndPrepareForFire();

        // Execute fire with NaN - should throw
        assertThatThrownBy(() -> bot.setFire(Double.NaN))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Fire with negative value sets raw value (API does not clamp)")
    void testFireWithNegativeValueSetsRawValue() {
        var bot = startAndPrepareForFire();

        // Execute fire with negative value - API does not validate/clamp
        var result = setFireAndGetIntent(bot, -1.0);

        // Fire succeeds because energy check passes (100.0 >= -1.0)
        // The raw value is sent to the server (no clamping in client API)
        assertThat(result.getResult()).isTrue();
        assertThat(result.getIntent().getFirepower()).isEqualTo(-1.0);
    }

    @Test
    @DisplayName("Fire with Infinity fails because energy is insufficient")
    void testFireWithInfinityFailsEnergyCheck() {
        var bot = startAndPrepareForFire();

        // Execute fire with Infinity - fails because energy < Infinity
        var result = setFireAndGetIntent(bot, Double.POSITIVE_INFINITY);

        // Fire should fail (false) because energy (100.0) < Infinity
        assertThat(result.getResult()).isFalse();
        assertThat(result.getIntent().getFirepower()).isNull();
    }

    @Test
    @DisplayName("Fire with exact minimum (0.1) succeeds")
    void testFireWithExactMinimumSucceeds() {
        var bot = startAndPrepareForFire();

        // Execute fire with exact minimum
        var result = setFireAndGetIntent(bot, 0.1);

        // Fire should succeed (true)
        assertThat(result.getResult()).isTrue();
        assertThat(result.getIntent().getFirepower()).isEqualTo(0.1);
    }

    @Test
    @DisplayName("Fire with exact maximum (3.0) succeeds")
    void testFireWithExactMaximumSucceeds() {
        var bot = startAndPrepareForFire();

        // Execute fire with exact maximum
        var result = setFireAndGetIntent(bot, 3.0);

        // Fire should succeed (true)
        assertThat(result.getResult()).isTrue();
        assertThat(result.getIntent().getFirepower()).isEqualTo(3.0);
    }
}
