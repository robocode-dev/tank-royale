package dev.robocode.tankroyale.botapi.internal;

import com.neovisionaries.i18n.CountryCode;
import dev.robocode.tankroyale.botapi.BotException;
import dev.robocode.tankroyale.botapi.InitialPosition;
import dev.robocode.tankroyale.botapi.test_utils.EnvironmentVariablesBuilder;
import dev.robocode.tankroyale.botapi.test_utils.EnvironmentVariablesConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(SystemStubsExtension.class)
public class EnvVarsTest {

    @Test
    void getBotInfo_shouldWorkWhenAllEnvVarsAreSetCorrectly() throws Exception {
        EnvironmentVariablesBuilder.createAll().execute(()-> {
            var info = EnvVars.getBotInfo();
            assertThat(info.getName()).isEqualTo("MyBot");
            assertThat(info.getVersion()).isEqualTo("1.0");
            assertThat(info.getAuthors()).containsAll(List.of("Author1", "Author2"));
            assertThat(info.getGameTypes()).containsAll(List.of("classic", "melee", "1v1"));
            assertThat(info.getDescription()).isEqualTo("Short description");
            assertThat(info.getHomepage()).isEqualTo("https://somewhere.net/MyBot");
            assertThat(info.getCountryCodes()).containsAll(List.of("UK", "US"));
            assertThat(info.getPlatform()).isEqualTo("JVM");
            assertThat(info.getProgrammingLang()).isEqualTo("Java 11");
            assertThat(info.getInitialPosition()).isEqualTo(InitialPosition.fromString("50,50,90"));
        });
    }

    @Test
    void getBotInfo_shouldFailWhenBotNameIsNull() throws Exception {
        var envVars = EnvironmentVariablesBuilder.createAll();
        envVars.set(EnvironmentVariablesConstants.BOT_NAME, null);
        envVars.execute(() -> {
            assertThrows(BotException.class, EnvVars::getBotInfo);
        });
    }

    @Test
    void getBotInfo_shouldFailWhenBotNameIsBlank() throws Exception {
        var envVars = EnvironmentVariablesBuilder.createAll();
        envVars.set(EnvironmentVariablesConstants.BOT_NAME, "  \t");
        envVars.execute(() -> {
            assertThrows(BotException.class, EnvVars::getBotInfo);
        });
    }

    @Test
    void getBotInfo_shouldFailWhenBotVersionIsNull() throws Exception {
        var envVars = EnvironmentVariablesBuilder.createAll();
        envVars.set(EnvironmentVariablesConstants.BOT_VERSION, null);
        envVars.execute(() -> {
            assertThrows(BotException.class, EnvVars::getBotInfo);
        });
    }

    @Test
    void getBotInfo_shouldFailWhenBotVersionIsBlank() throws Exception {
        var envVars = EnvironmentVariablesBuilder.createAll();
        envVars.set(EnvironmentVariablesConstants.BOT_VERSION, "  \t");
        envVars.execute(() -> {
            assertThrows(BotException.class, EnvVars::getBotInfo);
        });
    }

    @Test
    void getBotInfo_shouldFailWhenBotAuthorsIsNull() throws Exception {
        var envVars = EnvironmentVariablesBuilder.createAll();
        envVars.set(EnvironmentVariablesConstants.BOT_AUTHORS, null);
        envVars.execute(() -> {
            assertThrows(BotException.class, EnvVars::getBotInfo);
        });
    }

    @Test
    void getBotInfo_shouldFailWhenBotAuthorsIsBlank() throws Exception {
        var envVars = EnvironmentVariablesBuilder.createAll();
        envVars.set(EnvironmentVariablesConstants.BOT_AUTHORS, "  \t");
        envVars.execute(() -> {
            assertThrows(BotException.class, EnvVars::getBotInfo);
        });
    }

    @Test
    void getBotInfo_shouldFailWhenBotGameTypesIsNull() throws Exception {
        var envVars = EnvironmentVariablesBuilder.createAll();
        envVars.set(EnvironmentVariablesConstants.BOT_GAME_TYPES, null);
        envVars.execute(() -> {
            assertThrows(BotException.class, EnvVars::getBotInfo);
        });
    }

    @Test
    void getBotInfo_shouldFailWhenBotGameTypesIsBlank() throws Exception {
        var envVars = EnvironmentVariablesBuilder.createAll();
        envVars.set(EnvironmentVariablesConstants.BOT_GAME_TYPES, "  \t");
        envVars.execute(() -> {
            assertThrows(BotException.class, EnvVars::getBotInfo);
        });
    }

    @Test
    void getBotInfo_shouldReturnLocalCountryCodeIfWhenBotCountryCodesIsInvalid() throws Exception {
        var envVars = EnvironmentVariablesBuilder.createAll();
        envVars.set(EnvironmentVariablesConstants.BOT_COUNTRY_CODES, "XYZ tew");
        envVars.execute(() -> {
            String localCountryCode = CountryCode.getByLocale(Locale.getDefault()).getAlpha2();
            assertThat(EnvVars.getBotInfo().getCountryCodes()).containsAll(List.of(localCountryCode));
        });
    }

    @ParameterizedTest
    @MethodSource("initialPositionProvider")
    void getBotInfo_initialPosition_shouldWorkWithNormalInput(String input, Double x, Double y, Double angle) throws Exception {
        var envVars = EnvironmentVariablesBuilder.createAll();
        envVars.set(EnvironmentVariablesConstants.BOT_INITIAL_POS, input);
        envVars.execute(() -> {
            var pos = EnvVars.getBotInfo().getInitialPosition();
            assertThat(pos.getX()).isEqualTo(x);
            assertThat(pos.getY()).isEqualTo(y);
            assertThat(pos.getAngle()).isEqualTo(angle);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  \t"})
    void getBotInfo_initialPosition_shouldWorkWithBlankInput(String input) throws Exception {
        var envVars = EnvironmentVariablesBuilder.createAll();
        envVars.set(EnvironmentVariablesConstants.BOT_INITIAL_POS, input);
        envVars.execute(() -> {
            var pos = EnvVars.getBotInfo().getInitialPosition();
            assertThat(pos).isNull();
        });
    }

    static Stream<Arguments> initialPositionProvider() {
        return Stream.of(
                arguments("  50 ", 50.0, null, null),
                arguments("  50, ", 50.0, null, null),
                arguments("  50 70.0", 50.0, 70.0, null),
                arguments("  50.0, 70", 50.0, 70.0, null),
                arguments("  50, 70.0 ,", 50.0, 70.0, null),
                arguments("  50.0, 70, 100", 50.0, 70.0, 100.0),
                arguments("  50, 70.0 100", 50.0, 70.0, 100.0)
        );
    }
}