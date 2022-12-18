package dev.robocode.tankroyale.botapi.internal;

import com.neovisionaries.i18n.CountryCode;
import dev.robocode.tankroyale.botapi.BotException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static test_utils.EnvironmentVariables.*;
import static org.assertj.core.api.Assertions.assertThat;

// Set all environment variables to a default value
@SetEnvironmentVariable(key = "BOT_NAME", value = "dummy")
@SetEnvironmentVariable(key = "BOT_VERSION", value = "dummy")
@SetEnvironmentVariable(key = "BOT_AUTHORS", value = "dummy")
@SetEnvironmentVariable(key = "BOT_GAME_TYPES", value = "dummy")
@SetEnvironmentVariable(key = "BOT_DESCRIPTION", value = "dummy")
@SetEnvironmentVariable(key = "BOT_HOMEPAGE", value = "dummy")
@SetEnvironmentVariable(key = "BOT_COUNTRY_CODES", value = "dummy")
@SetEnvironmentVariable(key = "BOT_PLATFORM", value = "dummy")
@SetEnvironmentVariable(key = "BOT_PROG_LANG", value = "dummy")
@SetEnvironmentVariable(key = "BOT_INITIAL_POS", value = "dummy")
@DisplayName("Tests for the EnvVars class")
class EnvVarsTest {

    final static String MISSING_ENV_VAR_TEXT = "Missing environment variable: ";

    @Nested
    @DisplayName("Test for getBotInfo()")
    class GetBotInfo {

        @Nested
        @DisplayName("Tests for getName()")
        class GetName {

            @Test
            @SetEnvironmentVariable(key = BOT_NAME, value = "MyBot")
            void givenValidEnvVar_whenCallingGetName_thenReturnedNameIsTheSame() {
                assertThat(EnvVars.getBotInfo().getName()).isEqualTo("MyBot");
            }

            @Test
            @ClearEnvironmentVariable(key = BOT_NAME)
            void givenMissingEnvVar_whenCallingGetName_thenThrowBotExceptionWithMissingEnvInfo() {
                // when
                var thrown = catchThrowable(EnvVars::getBotInfo);
                // then
                assertThat(thrown)
                        .isInstanceOf(BotException.class)
                        .hasMessageContaining(MISSING_ENV_VAR_TEXT + BOT_NAME);
            }

            @Test
            @SetEnvironmentVariable(key = BOT_NAME, value = "  \t")
            void givenBlankEnvVar_whenCallingGetName_thenThrowBotExceptionWithMissingEnvInfo() {
                // when
                var thrown = catchThrowable(EnvVars::getBotInfo);
                // then
                assertThat(thrown)
                        .isInstanceOf(BotException.class)
                        .hasMessageContaining(MISSING_ENV_VAR_TEXT + BOT_NAME);
            }
        }

        @Nested
        @DisplayName("Tests for getVersion()")
        class GetVersion {

            @Test
            @SetEnvironmentVariable(key = BOT_VERSION, value = "1.0")
            void givenValidEnvVar_whenCallingGetVersion_thenReturnedVersionIsTheSame() {
                assertThat(EnvVars.getBotInfo().getVersion()).isEqualTo("1.0");
            }

            @Test
            @ClearEnvironmentVariable(key = BOT_VERSION)
            void givenMissingEnvVar_whenCallingGetVersion_thenThrowBotExceptionWithMissingEnvInfo() {
                // when
                var thrown = catchThrowable(EnvVars::getBotInfo);
                // then
                assertThat(thrown)
                        .isInstanceOf(BotException.class)
                        .hasMessageContaining(MISSING_ENV_VAR_TEXT + BOT_VERSION);
            }

            @Test
            @SetEnvironmentVariable(key = BOT_VERSION, value = "  \t")
            void givenBlankEnvVar_whenCallingGetVersion_thenThrowBotExceptionWithMissingEnvInfo() {
                // when
                var thrown = catchThrowable(EnvVars::getBotInfo);
                // then
                assertThat(thrown)
                        .isInstanceOf(BotException.class)
                        .hasMessageContaining(MISSING_ENV_VAR_TEXT + BOT_VERSION);
            }
        }

        @Nested
        @DisplayName("Tests for getAuthors()")
        class GetAuthors {

            @Test
            @SetEnvironmentVariable(key = BOT_AUTHORS, value = "Author 1, Author 2")
            void givenValidEnvVar_whenCallingGetAuthors_thenReturnedListContainsSameAuthors() {
                assertThat(EnvVars.getBotInfo().getAuthors()).containsAll(List.of("Author 1", "Author 2"));
            }

            @Test
            @ClearEnvironmentVariable(key = BOT_AUTHORS)
            void givenMissingEnvVar_whenCallingGetAuthors_thenThrowBotExceptionWithMissingEnvInfo() {
                // when
                var thrown = catchThrowable(EnvVars::getBotInfo);
                // then
                assertThat(thrown)
                        .isInstanceOf(BotException.class)
                        .hasMessageContaining(MISSING_ENV_VAR_TEXT + BOT_AUTHORS);
            }

            @Test
            @SetEnvironmentVariable(key = BOT_AUTHORS, value = "  \t")
            void givenBlankEnvVar_whenCallingGetAuthors_thenThrowBotExceptionWithMissingEnvInfo() {
                // when
                var thrown = catchThrowable(EnvVars::getBotInfo);
                // then
                assertThat(thrown)
                        .isInstanceOf(BotException.class)
                        .hasMessageContaining(MISSING_ENV_VAR_TEXT + BOT_AUTHORS);
            }
        }

        @Nested
        @DisplayName("Tests for getGameTypes()")
        class GetGameTypes {

            @Test
            @SetEnvironmentVariable(key = BOT_GAME_TYPES, value = "classic, 1v1, melee")
            void givenValidEnvVar_whenCallingGetGameTypes_thenReturnedListContainsSameGameTypes() {
                assertThat(EnvVars.getBotInfo().getGameTypes()).containsAll(List.of("classic", "1v1", "melee"));
            }

            @Nested
            @DisplayName("Tests for getDescription()")
            class GetDescription {

                @Test
                @SetEnvironmentVariable(key = BOT_DESCRIPTION, value = "description")
                void givenValidEnvVar_whenCallingGetDescription_thenReturnedDescriptionIsTheSame() {
                    assertThat(EnvVars.getBotInfo().getDescription()).isEqualTo("description");
                }

                @Test
                @ClearEnvironmentVariable(key = BOT_DESCRIPTION)
                void givenMissingEnvVar_whenCallingGetDescription_thenReturnNull() {
                    assertThat(EnvVars.getBotInfo().getDescription()).isNull();
                }

                @Test
                @SetEnvironmentVariable(key = BOT_DESCRIPTION, value = "  \t")
                void givenBlankEnvVar_whenCallingGetDescription_thenReturnNull() {
                    assertThat(EnvVars.getBotInfo().getDescription()).isNull();
                }
            }

            @Nested
            @DisplayName("Tests for getHomepage()")
            class GetHomepage {

                @Test
                @SetEnvironmentVariable(key = BOT_HOMEPAGE, value = "https://robocode.dev/")
                void givenValidEnvVar_whenCallingGetHomepage_thenReturnedHomepageIsTheSame() {
                    assertThat(EnvVars.getBotInfo().getHomepage()).isEqualTo("https://robocode.dev/");
                }

                @Test
                @ClearEnvironmentVariable(key = BOT_HOMEPAGE)
                void givenMissingEnvVar_whenCallingGetHomepage_thenReturnNull() {
                    assertThat(EnvVars.getBotInfo().getHomepage()).isNull();
                }

                @Test
                @SetEnvironmentVariable(key = BOT_HOMEPAGE, value = "  \t")
                void givenBlankEnvVar_whenCallingGetHomepage_thenReturnNull() {
                    assertThat(EnvVars.getBotInfo().getHomepage()).isNull();
                }
            }

            @Nested
            @DisplayName("Tests for getCountryCodes()")
            class GetCountryCodes {

                @Test
                @SetEnvironmentVariable(key = BOT_COUNTRY_CODES, value = "dk, us")
                void givenValidEnvVar_whenCallingGetCountryCodes_thenReturnedListContainsSameCountryCodesInUpperCase() {
                    assertThat(EnvVars.getBotInfo().getCountryCodes()).containsAll(List.of("DK", "US"));
                }

                @Test
                @SetEnvironmentVariable(key = BOT_COUNTRY_CODES, value = "xyz")
                void givenInvalidEnvVar_whenCallingGetCountryCodes_thenReturnDefaultLocaleCountryCode() {
                    String localCountryCode = CountryCode.getByLocale(Locale.getDefault()).getAlpha2();
                    assertThat(EnvVars.getBotInfo().getCountryCodes()).containsAll(List.of(localCountryCode));
                }

                @Test
                @ClearEnvironmentVariable(key = BOT_COUNTRY_CODES)
                void givenMissingEnvVar_whenCallingGetCountryCodes_thenReturnDefaultLocaleCountryCode() {
                    String localCountryCode = CountryCode.getByLocale(Locale.getDefault()).getAlpha2();
                    assertThat(EnvVars.getBotInfo().getCountryCodes()).containsAll(List.of(localCountryCode));
                }

                @Test
                @SetEnvironmentVariable(key = BOT_COUNTRY_CODES, value = "  \t")
                void givenBlankEnvVar_whenCallingGetCountryCodes_thenReturnDefaultLocaleCountryCode() {
                    String localCountryCode = CountryCode.getByLocale(Locale.getDefault()).getAlpha2();
                    assertThat(EnvVars.getBotInfo().getCountryCodes()).containsAll(List.of(localCountryCode));
                }
            }

            @Nested
            @DisplayName("Tests for getPlatform()")
            class GetPlatform {

                @Test
                @SetEnvironmentVariable(key = BOT_PLATFORM, value = "JVM")
                void givenValidEnvVar_whenCallingGetPlatform_thenReturnedPlatformIsTheSame() {
                    assertThat(EnvVars.getBotInfo().getPlatform()).isEqualTo("JVM");
                }

                @Test
                @ClearEnvironmentVariable(key = BOT_PLATFORM)
                void givenMissingEnvVar_whenCallingGetPlatform_thenReturnedPlatformMustContainJava() {
                    assertThat(EnvVars.getBotInfo().getPlatform()).contains("Java");
                }

                @Test
                @SetEnvironmentVariable(key = BOT_PLATFORM, value = "  \t")
                void givenBlankEnvVar_whenCallingGetPlatform_thenReturnedPlatformMustContainJava() {
                    assertThat(EnvVars.getBotInfo().getPlatform()).contains("Java");
                }
            }

            @Nested
            @DisplayName("Tests for getProgrammingLang()")
            class GetProgrammingLang {

                @Test
                @SetEnvironmentVariable(key = BOT_PROG_LANG, value = "Java 19")
                void givenValidEnvVar_whenCallingGetProgrammingLang_thenReturnedProgrammingLangIsTheSame() {
                    assertThat(EnvVars.getBotInfo().getProgrammingLang()).isEqualTo("Java 19");
                }

                @Test
                @ClearEnvironmentVariable(key = BOT_PROG_LANG)
                void givenMissingEnvVar_whenCallingGetProgrammingLang_thenReturnNull() {
                    assertThat(EnvVars.getBotInfo().getProgrammingLang()).isNull();
                }

                @Test
                @SetEnvironmentVariable(key = BOT_PROG_LANG, value = "  \t")
                void givenBlankEnvVar_whenCallingGetProgrammingLang_thenReturnNull() {
                    assertThat(EnvVars.getBotInfo().getProgrammingLang()).isNull();
                }
            }

            @Nested
            @DisplayName("Tests for getInitialPosition()")
            class GetInitialPosition {

                @Test
                @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "50, 100, 45")
                void givenValidPositionEnvVar_whenCallingGetInitialPosition_thenReturnedCoordinatesAreTheSame() {
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isEqualTo(100);
                    assertThat(pos.getAngle()).isEqualTo(45);
                }

                @Test
                @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  50 ")
                void givenValidXCoordinateOnly_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXCoordinate() {
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isNull();
                    assertThat(pos.getAngle()).isNull();
                }

                @Test
                @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  50, ")
                void givenValidXCoordinateWithCommaAfter_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXCoordinate() {
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isNull();
                    assertThat(pos.getAngle()).isNull();
                }

                @Test
                @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  50 70.0")
                void givenValidXAndYCoordinateOnlyWithNoCommaInBetween_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXAndYCoordinate() {
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isEqualTo(70);
                    assertThat(pos.getAngle()).isNull();
                }

                @Test
                @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  50.0, 70")
                void givenValidXAndYCoordinateOnlyWithACommaInBetween_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXAndYCoordinate() {
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isEqualTo(70);
                    assertThat(pos.getAngle()).isNull();
                }

                @Test
                @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  50, 70.0 ,")
                void givenValidXAndYCoordinateOnlyWithACommaInBetweenAndAfter_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXAndYCoordinate() {
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isEqualTo(70);
                    assertThat(pos.getAngle()).isNull();
                }

                @Test
                @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  50.0, 70, 100")
                void givenValidCoordinatesWithCommaSeparators_whenCallingGetInitialPosition_thenReturnedCoordinatesAreTheSame() {
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isEqualTo(70);
                    assertThat(pos.getAngle()).isEqualTo(100);
                }

                @Test
                @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  50, 70.0 100")
                void givenValidCoordinatesWithMixedSpaceAndCommaSeparator_whenCallingGetInitialPosition_thenReturnedCoordinatesAreTheSame() {
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isEqualTo(70);
                    assertThat(pos.getAngle()).isEqualTo(100);
                }

                @Test
                @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "")
                void givenEmptyPosition_whenCallingGetInitialPosition_thenReturnNull() {
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos).isNull();
                }

                @Test
                @SetEnvironmentVariable(key = BOT_INITIAL_POS, value = "  \t")
                void givenBlankPosition_whenCallingGetInitialPosition_thenReturnNull() {
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos).isNull();
                }
            }
        }
    }
}
