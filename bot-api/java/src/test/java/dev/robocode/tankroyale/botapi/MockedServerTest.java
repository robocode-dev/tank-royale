package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockedServerTest extends AbstractBotTest {

    @Test
    void testAwaitBotReady() {
        var bot = start();
        assertThat(server.awaitBotReady(Integer.valueOf(1000))).isTrue();
    }

    @Test
    void testSetBotStateAndAwaitTick() {
        var bot = startAndAwaitGameStarted();

        double newEnergy = 42.5;
        double newGunHeat = 0.33;

        boolean ok = server.setBotStateAndAwaitTick(Double.valueOf(newEnergy), Double.valueOf(newGunHeat), null, null, null, null);
        assertThat(ok).isTrue();

        // wait until bot reflects the updated state
        boolean reflected = awaitCondition(() -> Math.abs(bot.getEnergy() - newEnergy) < 1e-6, 1000);
        assertThat(reflected).isTrue();

        boolean reflectedGunHeat = awaitCondition(() -> Math.abs(bot.getGunHeat() - newGunHeat) < 1e-6, 1000);
        assertThat(reflectedGunHeat).isTrue();
    }

    // Note: ExecuteCommandAndGetIntent tests are implemented in Python only.
    // Java has different timing semantics that make intent capture after property
    // setting more complex. The core state sync tests above verify cross-language
    // parity. See CROSS-LANGUAGE-VERIFICATION.md for details.
}
