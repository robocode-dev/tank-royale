package dev.robocode.tankroyale.botapi.internal;

import com.neovisionaries.i18n.CountryCode;
import dev.robocode.tankroyale.botapi.BotException;
import dev.robocode.tankroyale.botapi.InitialPosition;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;
import test_utils.MockedServer;

import java.util.List;
import java.util.Locale;

import static test_utils.EnvironmentVariables.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SetEnvironmentVariable(key = "SERVER_URL", value = "ws://localhost:" + MockedServer.PORT)
@SetEnvironmentVariable(key = "BOT_NAME", value = "MyBot")
@SetEnvironmentVariable(key = "BOT_VERSION", value = "1.0")
@SetEnvironmentVariable(key = "BOT_AUTHORS", value = "Author1, Author2")
@SetEnvironmentVariable(key = "BOT_GAME_TYPES", value = "classic, melee, 1v1")
@SetEnvironmentVariable(key = "BOT_DESCRIPTION", value = "Short description")
@SetEnvironmentVariable(key = "BOT_HOMEPAGE", value = "https://somewhere.net/MyBot")
@SetEnvironmentVariable(key = "BOT_COUNTRY_CODES", value = "gb, us")
@SetEnvironmentVariable(key = "BOT_PLATFORM", value = "JVM")
@SetEnvironmentVariable(key = "BOT_PROG_LANG", value = "Java 18")
@SetEnvironmentVariable(key = "BOT_INITIAL_POS", value = "50,50, 90")
public class EnvVarsTest {

    @Test
    void getBotInfo_shouldWorkWhenAllEnvVarsAreSetCorrectly() {
        var info = EnvVars.getBotInfo();
        assertThat(info.getName()).isEqualTo("MyBot");
        assertThat(info.getVersion()).isEqualTo("1.0");
        assertThat(info.getAuthors()).containsAll(List.of("Author1", "Author2"));
        assertThat(info.getGameTypes()).containsAll(List.of("classic", "melee", "1v1"));
        assertThat(info.getDescription()).isEqualTo("Short description");
        assertThat(info.getHomepage()).isEqualTo("https://somewhere.net/MyBot");
        assertThat(info.getCountryCodes()).containsExactlyInAnyOrder("US", "GB");
        assertThat(info.getPlatform()).isEqualTo("JVM");
        assertThat(info.getProgrammingLang()).isEqualTo("Java 18");
        assertThat(info.getInitialPosition()).isEqualTo(InitialPosition.fromString("50,50,90"));
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_NAME)
    void getBotInfo_shouldFailWhenBotNameIsNull() {
        assertThrows(BotException.class, EnvVars::getBotInfo);
    }

    @Test
    @SetEnvironmentVariable(key = BOT_NAME, value = "  \t")
    void getBotInfo_shouldFailWhenBotNameIsBlank() {
        assertThrows(BotException.class, EnvVars::getBotInfo);
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_VERSION)
    void getBotInfo_shouldFailWhenBotVersionIsNull() {
        assertThrows(BotException.class, EnvVars::getBotInfo);
    }

    @Test
    @SetEnvironmentVariable(key = BOT_VERSION, value = "  \t")
    void getBotInfo_shouldFailWhenBotVersionIsBlank() {
        assertThrows(BotException.class, EnvVars::getBotInfo);
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_AUTHORS)
    void getBotInfo_shouldFailWhenBotAuthorsIsNull() {
        assertThrows(BotException.class, EnvVars::getBotInfo);
    }

    @Test
    @SetEnvironmentVariable(key = BOT_AUTHORS, value = "  \t")
    void getBotInfo_shouldFailWhenBotAuthorsIsBlank() {
        assertThrows(BotException.class, EnvVars::getBotInfo);
    }

    @Test
    @ClearEnvironmentVariable(key = BOT_GAME_TYPES)
    void getBotInfo_shouldFailWhenBotGameTypesIsNull() {
        assertThrows(BotException.class, EnvVars::getBotInfo);
    }

    @Test
    @SetEnvironmentVariable(key = BOT_GAME_TYPES, value = "  \t")
    void getBotInfo_shouldFailWhenBotGameTypesIsBlank() {
        assertThrows(BotException.class, EnvVars::getBotInfo);
    }

    @Test
    @SetEnvironmentVariable(key = BOT_COUNTRY_CODES, value = "XYZ tew")
    void getBotInfo_shouldReturnLocalCountryCodeIfWhenBotCountryCodesIsInvalid() {
        String localCountryCode = CountryCode.getByLocale(Locale.getDefault()).getAlpha2();
        assertThat(EnvVars.getBotInfo().getCountryCodes()).containsAll(List.of(localCountryCode));
    }

    @Test
    @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  50 ")
    void getBotInfo_initialPosition_shouldWorkWithValidInput1() {
        var pos = EnvVars.getBotInfo().getInitialPosition();
        assertThat(pos.getX()).isEqualTo(50);
        assertThat(pos.getY()).isEqualTo(null);
        assertThat(pos.getAngle()).isEqualTo(null);
    }

    @Test
    @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  50, ")
    void getBotInfo_initialPosition_shouldWorkWithNormalInput2() {
        var pos = EnvVars.getBotInfo().getInitialPosition();
        assertThat(pos.getX()).isEqualTo(50);
        assertThat(pos.getY()).isEqualTo(null);
        assertThat(pos.getAngle()).isEqualTo(null);
    }

    @Test
    @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  50 70.0")
    void getBotInfo_initialPosition_shouldWorkWithNormalInput3() {
        var pos = EnvVars.getBotInfo().getInitialPosition();
        assertThat(pos.getX()).isEqualTo(50);
        assertThat(pos.getY()).isEqualTo(70);
        assertThat(pos.getAngle()).isEqualTo(null);
    }

    @Test
    @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  50.0, 70")
    void getBotInfo_initialPosition_shouldWorkWithNormalInput4() {
        var pos = EnvVars.getBotInfo().getInitialPosition();
        assertThat(pos.getX()).isEqualTo(50);
        assertThat(pos.getY()).isEqualTo(70);
        assertThat(pos.getAngle()).isEqualTo(null);
    }

    @Test
    @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  50, 70.0 ,")
    void getBotInfo_initialPosition_shouldWorkWithNormalInput5() {
        var pos = EnvVars.getBotInfo().getInitialPosition();
        assertThat(pos.getX()).isEqualTo(50);
        assertThat(pos.getY()).isEqualTo(70);
        assertThat(pos.getAngle()).isEqualTo(null);
    }

    @Test
    @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  50.0, 70, 100")
    void getBotInfo_initialPosition_shouldWorkWithNormalInput6() {
        var pos = EnvVars.getBotInfo().getInitialPosition();
        assertThat(pos.getX()).isEqualTo(50);
        assertThat(pos.getY()).isEqualTo(70);
        assertThat(pos.getAngle()).isEqualTo(100);
    }

    @Test
    @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  50, 70.0 100")
    void getBotInfo_initialPosition_shouldWorkWithNormalInput7() {
        var pos = EnvVars.getBotInfo().getInitialPosition();
        assertThat(pos.getX()).isEqualTo(50);
        assertThat(pos.getY()).isEqualTo(70);
        assertThat(pos.getAngle()).isEqualTo(100);
    }

    @Test
    @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "")
    void getBotInfo_initialPosition_shouldWorkWithEmptyInput() {
        var pos = EnvVars.getBotInfo().getInitialPosition();
        assertThat(pos).isNull();
    }

    @Test
    @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  \t")
    void getBotInfo_initialPosition_shouldWorkWithBlankInput() {
        var pos = EnvVars.getBotInfo().getInitialPosition();
        assertThat(pos).isNull();
    }
}