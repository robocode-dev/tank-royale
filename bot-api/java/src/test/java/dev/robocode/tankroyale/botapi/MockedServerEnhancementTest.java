package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MockedServer Enhancement Tests")
class MockedServerEnhancementTest extends AbstractBotTest {

    @Test
    @DisplayName("awaitBotReady() should succeed when bot is ready")
    void awaitBotReady_ShouldSucceed() {
        var bot = new TestBot();
        startAsync(bot);

        boolean ready = server.awaitBotReady(2000);
        assertThat(ready).isTrue();
    }

    @Test
    @DisplayName("setBotStateAndAwaitTick() should update state and await next tick")
    void setBotStateAndAwaitTick_ShouldUpdateState() {
        // Let's use a bot with a loop
        var botWithLoop = new Bot(botInfo, test_utils.MockedServer.getServerUrl()) {
            @Override
            public void run() {
                while (isRunning()) {
                    go();
                }
            }
        };
        startAsync(botWithLoop);
        assertThat(server.awaitBotReady(2000)).isTrue();

        // Ensure we are in a clean state after awaitBotReady
        awaitTick(botWithLoop);

        // Update state
        double newEnergy = 50.0;
        double newGunHeat = 1.5;
        boolean success = server.setBotStateAndAwaitTick(newEnergy, newGunHeat, null, null, null, null);

        assertThat(success).isTrue();

        // Poll until the bot has processed the tick carrying the updated state.
        // awaitTick() only confirms the server sent the tick, not that the bot received it.
        assertThat(awaitCondition(() -> botWithLoop.getEnergy() == newEnergy, 2000))
                .as("bot.getEnergy() should equal " + newEnergy + " after tick with updated state")
                .isTrue();
        assertThat(awaitCondition(() -> botWithLoop.getGunHeat() == newGunHeat, 2000))
                .as("bot.getGunHeat() should equal " + newGunHeat + " after tick with updated state")
                .isTrue();
    }
}
