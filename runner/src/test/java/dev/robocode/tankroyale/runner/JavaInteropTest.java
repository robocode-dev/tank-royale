package dev.robocode.tankroyale.runner;

import dev.robocode.tankroyale.common.rules.GameType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifies that the Battle Runner public API is usable from pure Java 11+ code.
 * <p>
 * These tests exercise compile-time accessibility and runtime behavior of factory methods,
 * builders, and data classes — ensuring {@code @JvmStatic}, {@code @JvmOverloads}, and
 * {@code Consumer<Builder>} overloads work correctly from Java.
 */
class JavaInteropTest {

    @TempDir
    Path tempDir;

    // -------------------------------------------------------------------------------------
    // BattleRunner — static factory + Consumer<Builder> overload
    // -------------------------------------------------------------------------------------

    @Test
    void createWithDefaults() {
        var runner = BattleRunner.create();
        assertThat(runner.getConfig().getServerMode()).isInstanceOf(BattleRunner.ServerMode.Embedded.class);
        assertThat(runner.getConfig().getIntentDiagnosticsEnabled()).isFalse();
        assertThat(runner.getConfig().getRecordingPath()).isNull();
    }

    @Test
    void createWithConsumerBuilder() {
        var runner = BattleRunner.create(b -> b.externalServer("ws://localhost:7654"));

        assertThat(runner.getConfig().getServerMode()).isInstanceOf(BattleRunner.ServerMode.External.class);
        var external = (BattleRunner.ServerMode.External) runner.getConfig().getServerMode();
        assertThat(external.getUrl()).isEqualTo("ws://localhost:7654");
    }

    @Test
    void createWithEmbeddedServerNoArgOverload() {
        var runner = BattleRunner.create(b -> b.embeddedServer());
        var embedded = (BattleRunner.ServerMode.Embedded) runner.getConfig().getServerMode();
        assertThat(embedded.getPort()).isEqualTo(0);
    }

    @Test
    void createWithEmbeddedServerExplicitPort() {
        var runner = BattleRunner.create(b -> b.embeddedServer(9999));
        var embedded = (BattleRunner.ServerMode.Embedded) runner.getConfig().getServerMode();
        assertThat(embedded.getPort()).isEqualTo(9999);
    }

    @Test
    void createWithIntentDiagnosticsAndRecording() {
        Path recordingPath = tempDir.resolve("recording.battle.gz");
        var runner = BattleRunner.create(b -> b
                .enableIntentDiagnostics()
                .enableRecording(recordingPath));

        assertThat(runner.getConfig().getIntentDiagnosticsEnabled()).isTrue();
        assertThat(runner.getConfig().getRecordingPath()).isEqualTo(recordingPath);
    }

    // -------------------------------------------------------------------------------------
    // BattleSetup — static factories + Consumer<Builder> overloads
    // -------------------------------------------------------------------------------------

    @Test
    void classicPresetDefaults() {
        var setup = BattleSetup.classic();
        assertThat(setup.getGameType()).isEqualTo(GameType.CLASSIC);
        assertThat(setup.getArenaWidth()).isEqualTo(800);
        assertThat(setup.getArenaHeight()).isEqualTo(600);
        assertThat(setup.getNumberOfRounds()).isEqualTo(10);
    }

    @Test
    void classicPresetWithConsumerOverrides() {
        var setup = BattleSetup.classic(s -> {
            s.setNumberOfRounds(25);
            s.setTurnTimeoutMicros(50_000);
        });
        assertThat(setup.getGameType()).isEqualTo(GameType.CLASSIC);
        assertThat(setup.getNumberOfRounds()).isEqualTo(25);
        assertThat(setup.getTurnTimeoutMicros()).isEqualTo(50_000);
        // non-overridden fields retain preset defaults
        assertThat(setup.getArenaWidth()).isEqualTo(800);
    }

    @Test
    void meleePresetDefaults() {
        var setup = BattleSetup.melee();
        assertThat(setup.getGameType()).isEqualTo(GameType.MELEE);
        assertThat(setup.getArenaWidth()).isEqualTo(1000);
        assertThat(setup.getArenaHeight()).isEqualTo(1000);
        assertThat(setup.getMinNumberOfParticipants()).isEqualTo(10);
    }

    @Test
    void meleePresetWithConsumerOverrides() {
        var setup = BattleSetup.melee(s -> {
            s.setNumberOfRounds(3);
            s.setMaxInactivityTurns(200);
        });
        assertThat(setup.getGameType()).isEqualTo(GameType.MELEE);
        assertThat(setup.getNumberOfRounds()).isEqualTo(3);
        assertThat(setup.getMaxInactivityTurns()).isEqualTo(200);
        // non-overridden fields retain preset defaults
        assertThat(setup.getArenaWidth()).isEqualTo(1000);
    }

    @Test
    void oneVsOnePresetDefaults() {
        var setup = BattleSetup.oneVsOne();
        assertThat(setup.getGameType()).isEqualTo(GameType.ONE_VS_ONE);
        assertThat(setup.getMaxNumberOfParticipants()).isEqualTo(2);
    }

    @Test
    void oneVsOnePresetWithConsumerOverrides() {
        var setup = BattleSetup.oneVsOne(s -> {
            s.setNumberOfRounds(50);
            s.setReadyTimeoutMicros(2_000_000);
        });
        assertThat(setup.getGameType()).isEqualTo(GameType.ONE_VS_ONE);
        assertThat(setup.getNumberOfRounds()).isEqualTo(50);
        assertThat(setup.getReadyTimeoutMicros()).isEqualTo(2_000_000);
        assertThat(setup.getMaxNumberOfParticipants()).isEqualTo(2);
    }

    @Test
    void customPresetDefaults() {
        var setup = BattleSetup.custom();
        assertThat(setup.getGameType()).isEqualTo(GameType.CUSTOM);
        assertThat(setup.getArenaWidth()).isEqualTo(800);
        assertThat(setup.getMaxInactivityTurns()).isEqualTo(450);
        assertThat(setup.getReadyTimeoutMicros()).isEqualTo(1_000_000);
    }

    @Test
    void customPresetWithConsumerOverrides() {
        var setup = BattleSetup.custom(s -> {
            s.setArenaWidth(1200);
            s.setArenaHeight(900);
            s.setNumberOfRounds(50);
            s.setGunCoolingRate(0.2);
            s.setMinNumberOfParticipants(4);
            s.setMaxNumberOfParticipants(8);
            s.setMaxInactivityTurns(300);
            s.setReadyTimeoutMicros(500_000);
        });
        assertThat(setup.getGameType()).isEqualTo(GameType.CUSTOM);
        assertThat(setup.getArenaWidth()).isEqualTo(1200);
        assertThat(setup.getArenaHeight()).isEqualTo(900);
        assertThat(setup.getNumberOfRounds()).isEqualTo(50);
        assertThat(setup.getGunCoolingRate()).isEqualTo(0.2);
        assertThat(setup.getMinNumberOfParticipants()).isEqualTo(4);
        assertThat(setup.getMaxNumberOfParticipants()).isEqualTo(8);
        assertThat(setup.getMaxInactivityTurns()).isEqualTo(300);
        assertThat(setup.getReadyTimeoutMicros()).isEqualTo(500_000);
    }

    @Test
    void builderGameTypeIsReadOnly() {
        var setup = BattleSetup.classic(s -> {
            assertThat(s.getGameType()).isEqualTo(GameType.CLASSIC);
        });
        assertThat(setup.getGameType()).isEqualTo(GameType.CLASSIC);
    }

    // -------------------------------------------------------------------------------------
    // BotEntry — static factories
    // -------------------------------------------------------------------------------------

    @Test
    void botEntryFromPath() {
        var entry = BotEntry.of(tempDir);
        assertThat(entry.getPath()).isEqualTo(tempDir);
    }

    @Test
    void botEntryFromString() {
        var entry = BotEntry.of(tempDir.toString());
        assertThat(entry.getPath()).isEqualTo(tempDir);
    }

    @Test
    void botEntryRejectsNonDirectory() {
        assertThatThrownBy(() -> BotEntry.of(tempDir.resolve("nonexistent")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bot path must be a directory");
    }

    // -------------------------------------------------------------------------------------
    // BattleResults / BotResult — data class accessors
    // -------------------------------------------------------------------------------------

    @Test
    void battleResultsAccessors() {
        var botResult = new BotResult(
                1, "TestBot", "1.0", false, 1, 100,
                50, 10, 20, 5, 10, 5, 3, 2, 1);

        var results = new BattleResults(10, List.of(botResult));

        assertThat(results.getNumberOfRounds()).isEqualTo(10);
        assertThat(results.getResults()).hasSize(1);

        var r = results.getResults().get(0);
        assertThat(r.getId()).isEqualTo(1);
        assertThat(r.getName()).isEqualTo("TestBot");
        assertThat(r.getVersion()).isEqualTo("1.0");
        assertThat(r.isTeam()).isFalse();
        assertThat(r.getRank()).isEqualTo(1);
        assertThat(r.getTotalScore()).isEqualTo(100);
        assertThat(r.getSurvival()).isEqualTo(50);
        assertThat(r.getLastSurvivorBonus()).isEqualTo(10);
        assertThat(r.getBulletDamage()).isEqualTo(20);
        assertThat(r.getBulletKillBonus()).isEqualTo(5);
        assertThat(r.getRamDamage()).isEqualTo(10);
        assertThat(r.getRamKillBonus()).isEqualTo(5);
        assertThat(r.getFirstPlaces()).isEqualTo(3);
        assertThat(r.getSecondPlaces()).isEqualTo(2);
        assertThat(r.getThirdPlaces()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------------------
    // BattleException
    // -------------------------------------------------------------------------------------

    @Test
    void battleExceptionIsRuntimeException() {
        var ex = new BattleException("test error", null);
        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("test error");
    }

    @Test
    void battleExceptionWithCause() {
        var cause = new RuntimeException("root cause");
        var ex = new BattleException("wrapper", cause);
        assertThat(ex.getMessage()).isEqualTo("wrapper");
        assertThat(ex.getCause()).isSameAs(cause);
    }
}
