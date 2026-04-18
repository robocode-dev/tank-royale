package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("BOT")
class BaseBotStateTest {

    static class TestBot extends BaseBot {
        TestBot() {
            super(BotInfo.builder()
                .setName("TestBot")
                .setVersion("1.0")
                .addAuthor("Author")
                .addGameType("classic")
                .build());
        }
    }

    @Test
    @Tag("TR-API-BOT-007")
    @Tag("LEGACY")
    void test_TR_API_BOT_007_base_bot_accessor_defaults() {
        var bot = new TestBot();
        
        // Metadata accessors should throw BotException when not connected
        assertThrows(BotException.class, bot::getMyId);
        assertThrows(BotException.class, bot::getVariant);
        assertThrows(BotException.class, bot::getVersion);
        
        // State-dependent accessors should throw BotException when no state is available
        assertThrows(BotException.class, bot::getEnergy);
        assertThrows(BotException.class, bot::getX);
        assertThrows(BotException.class, bot::getY);
        assertThrows(BotException.class, bot::getDirection);
        assertThrows(BotException.class, bot::getGunDirection);
        assertThrows(BotException.class, bot::getRadarDirection);
        assertThat(bot.getSpeed()).isEqualTo(0);
        assertThat(bot.getGunHeat()).isEqualTo(0);
        assertThat(bot.getBulletStates()).isEmpty();
        assertThrows(BotException.class, bot::getEvents);
        
        // Game setup accessors should throw BotException when no game setup is available
        assertThrows(BotException.class, bot::getArenaWidth);
        assertThrows(BotException.class, bot::getArenaHeight);
        assertThrows(BotException.class, bot::getGameType);
    }

    @Test
    @Tag("TR-API-BOT-008")
    @Tag("LEGACY")
    void test_TR_API_BOT_008_adjustment_flags_default_false() {
        var bot = new TestBot();
        
        assertThat(bot.isAdjustGunForBodyTurn()).isFalse();
        assertThat(bot.isAdjustRadarForBodyTurn()).isFalse();
        assertThat(bot.isAdjustRadarForGunTurn()).isFalse();
    }
}
