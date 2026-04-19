package dev.robocode.tankroyale.botapi;

import dev.robocode.tankroyale.botapi.graphics.Color;
import dev.robocode.tankroyale.botapi.util.ColorUtil;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("MDL")
class DataModelTest {

    @Test
    @Tag("TR-API-MDL-002")
    void test_TR_API_MDL_002_bot_state_constructor() {
        Color bodyColor = ColorUtil.fromHexColor("#111111");
        Color turretColor = ColorUtil.fromHexColor("#222222");
        Color radarColor = ColorUtil.fromHexColor("#333333");
        Color bulletColor = ColorUtil.fromHexColor("#444444");
        Color scanColor = ColorUtil.fromHexColor("#555555");
        Color tracksColor = ColorUtil.fromHexColor("#666666");
        Color gunColor = ColorUtil.fromHexColor("#777777");

        var state = new BotState(
            true, 100.0, 50.0, 60.0, 45.0, 90.0, 135.0, 5.0, 1.0, 2.0, 3.0, 4.0, 0.5, 3, 
            bodyColor, turretColor, radarColor, bulletColor, scanColor, tracksColor, gunColor, true
        );
        
        assertThat(state.isDroid()).isTrue();
        assertThat(state.getEnergy()).isEqualTo(100.0);
        assertThat(state.getX()).isEqualTo(50.0);
        assertThat(state.getY()).isEqualTo(60.0);
        assertThat(state.getDirection()).isEqualTo(45.0);
        assertThat(state.getGunDirection()).isEqualTo(90.0);
        assertThat(state.getRadarDirection()).isEqualTo(135.0);
        assertThat(state.getRadarSweep()).isEqualTo(5.0);
        assertThat(state.getSpeed()).isEqualTo(1.0);
        assertThat(state.getTurnRate()).isEqualTo(2.0);
        assertThat(state.getGunTurnRate()).isEqualTo(3.0);
        assertThat(state.getRadarTurnRate()).isEqualTo(4.0);
        assertThat(state.getGunHeat()).isEqualTo(0.5);
        assertThat(state.getEnemyCount()).isEqualTo(3);
        assertThat(state.getBodyColor()).isEqualTo(bodyColor);
        assertThat(state.getTurretColor()).isEqualTo(turretColor);
        assertThat(state.getRadarColor()).isEqualTo(radarColor);
        assertThat(state.getBulletColor()).isEqualTo(bulletColor);
        assertThat(state.getScanColor()).isEqualTo(scanColor);
        assertThat(state.getTracksColor()).isEqualTo(tracksColor);
        assertThat(state.getGunColor()).isEqualTo(gunColor);
        assertThat(state.isDebuggingEnabled()).isTrue();
    }

    @Test
    @Tag("TR-API-MDL-003")
    void test_TR_API_MDL_003_bot_results_constructor() {
        var results = new BotResults(1, 100.0, 50.0, 30.0, 20.0, 10.0, 5.0, 215.0, 3, 2, 4);
        
        assertThat(results.getRank()).isEqualTo(1);
        assertThat(results.getSurvival()).isEqualTo(100.0);
        assertThat(results.getLastSurvivorBonus()).isEqualTo(50.0);
        assertThat(results.getBulletDamage()).isEqualTo(30.0);
        assertThat(results.getBulletKillBonus()).isEqualTo(20.0);
        assertThat(results.getRamDamage()).isEqualTo(10.0);
        assertThat(results.getRamKillBonus()).isEqualTo(5.0);
        assertThat(results.getTotalScore()).isEqualTo(215.0);
        assertThat(results.getFirstPlaces()).isEqualTo(3);
        assertThat(results.getSecondPlaces()).isEqualTo(2);
        assertThat(results.getThirdPlaces()).isEqualTo(4);
    }

    @Test
    @Tag("TR-API-MDL-004")
    void test_TR_API_MDL_004_game_setup_constructor() {
        var setup = new GameSetup("classic", 800, 600, 10, 0.1, 450, 30000, 1000);
        
        assertThat(setup.getGameType()).isEqualTo("classic");
        assertThat(setup.getArenaWidth()).isEqualTo(800);
        assertThat(setup.getArenaHeight()).isEqualTo(600);
        assertThat(setup.getNumberOfRounds()).isEqualTo(10);
        assertThat(setup.getGunCoolingRate()).isEqualTo(0.1);
        assertThat(setup.getMaxInactivityTurns()).isEqualTo(450);
        assertThat(setup.getTurnTimeout()).isEqualTo(30000);
        assertThat(setup.getReadyTimeout()).isEqualTo(1000);
    }
}
