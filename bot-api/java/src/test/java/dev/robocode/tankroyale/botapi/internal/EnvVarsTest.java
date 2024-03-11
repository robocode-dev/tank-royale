package dev.robocode.tankroyale.botapi.internal;

import dev.robocode.tankroyale.botapi.BotException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.List;

import static dev.robocode.tankroyale.botapi.util.CountryCodeUtil.getLocalCountryCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowable;
import static test_utils.EnvironmentVariables.*;

@ExtendWith(SystemStubsExtension.class)
class EnvVarsTest {

    final static String MISSING_ENV_VAR_TEXT = "Missing environment variable: ";

    static EnvironmentVariables createEnvVars() {
        return new EnvironmentVariables(
                BOT_NAME, "dummy",
                BOT_VERSION, "dummy",
                BOT_AUTHORS, "dummy"
        );
    }

    @Nested
    class GetBotInfo {

        @SystemStub
        final EnvironmentVariables envVars = createEnvVars();

        @Nested
        class GetName {

            @Test
            void givenValidEnvVar_whenCallingGetName_thenReturnedNameIsTheSame() {
                envVars.set(BOT_NAME, "MyBot");
                assertThat(EnvVars.getBotInfo().getName()).isEqualTo("MyBot");
            }

            @Test
            void givenMissingEnvVar_whenCallingGetName_thenThrowBotExceptionWithMissingEnvInfo() {
                envVars.set(BOT_NAME, null);
                // when
                var thrown = catchThrowable(EnvVars::getBotInfo);
                // then
                assertThat(thrown)
                        .isInstanceOf(BotException.class)
                        .hasMessageContaining(MISSING_ENV_VAR_TEXT + BOT_NAME);
            }

            @Test
            void givenBlankEnvVar_whenCallingGetName_thenThrowBotExceptionWithMissingEnvInfo() {
                envVars.set(BOT_NAME, "  \t");
                // when
                var thrown = catchThrowable(EnvVars::getBotInfo);
                // then
                assertThat(thrown)
                        .isInstanceOf(BotException.class)
                        .hasMessageContaining(MISSING_ENV_VAR_TEXT + BOT_NAME);
            }
        }

        @Nested
        class GetVersion {

            @SystemStub
            final EnvironmentVariables envVars = createEnvVars();

            @Test
            void givenValidEnvVar_whenCallingGetVersion_thenReturnedVersionIsTheSame() {
                envVars.set(BOT_VERSION, "1.0");
                assertThat(EnvVars.getBotInfo().getVersion()).isEqualTo("1.0");
            }

            @Test
            void givenMissingEnvVar_whenCallingGetVersion_thenThrowBotExceptionWithMissingEnvInfo() {
                envVars.set(BOT_VERSION, null);
                // when
                var thrown = catchThrowable(EnvVars::getBotInfo);
                // then
                assertThat(thrown)
                        .isInstanceOf(BotException.class)
                        .hasMessageContaining(MISSING_ENV_VAR_TEXT + BOT_VERSION);
            }

            @Test
            void givenBlankEnvVar_whenCallingGetVersion_thenThrowBotExceptionWithMissingEnvInfo() {
                envVars.set(BOT_VERSION, "  \t");
                // when
                var thrown = catchThrowable(EnvVars::getBotInfo);
                // then
                assertThat(thrown)
                        .isInstanceOf(BotException.class)
                        .hasMessageContaining(MISSING_ENV_VAR_TEXT + BOT_VERSION);
            }
        }

        @Nested
        class GetAuthors {

            @SystemStub
            final EnvironmentVariables envVars = createEnvVars();

            @Test
            void givenValidEnvVar_whenCallingGetAuthors_thenReturnedListContainsSameAuthors() {
                envVars.set(BOT_AUTHORS, "Author 1, Author 2");
                assertThat(EnvVars.getBotInfo().getAuthors()).containsAll(List.of("Author 1", "Author 2"));
            }

            @Test
            void givenMissingEnvVar_whenCallingGetAuthors_thenThrowBotExceptionWithMissingEnvInfo() {
                envVars.set(BOT_AUTHORS, null);
                // when
                var thrown = catchThrowable(EnvVars::getBotInfo);
                // then
                assertThat(thrown)
                        .isInstanceOf(BotException.class)
                        .hasMessageContaining(MISSING_ENV_VAR_TEXT + BOT_AUTHORS);
            }

            @Test
            void givenBlankEnvVar_whenCallingGetAuthors_thenThrowBotExceptionWithMissingEnvInfo() {
                envVars.set(BOT_AUTHORS, "  \t");
                // when
                var thrown = catchThrowable(EnvVars::getBotInfo);
                // then
                assertThat(thrown)
                        .isInstanceOf(BotException.class)
                        .hasMessageContaining(MISSING_ENV_VAR_TEXT + BOT_AUTHORS);
            }
        }

        @Nested
        class GetGameTypes {

            @SystemStub
            final EnvironmentVariables envVars = createEnvVars();

            @Test
            void givenValidEnvVar_whenCallingGetGameTypes_thenReturnedListContainsSameGameTypes() {
                envVars.set(BOT_GAME_TYPES, "classic, 1v1, melee");
                assertThat(EnvVars.getBotInfo().getGameTypes()).containsAll(List.of("classic", "1v1", "melee"));
            }

            @Nested
            class GetDescription {

                @Test
                void givenValidEnvVar_whenCallingGetDescription_thenReturnedDescriptionIsTheSame() {
                    envVars.set(BOT_DESCRIPTION, "description");
                    assertThat(EnvVars.getBotInfo().getDescription()).isEqualTo("description");
                }

                @Test
                void givenMissingEnvVar_whenCallingGetDescription_thenReturnNull() {
                    envVars.set(BOT_DESCRIPTION, null);
                    assertThat(EnvVars.getBotInfo().getDescription()).isNull();
                }

                @Test
                void givenBlankEnvVar_whenCallingGetDescription_thenReturnNull() {
                    envVars.set(BOT_DESCRIPTION, "  \t");
                    assertThat(EnvVars.getBotInfo().getDescription()).isNull();
                }
            }

            @Nested
            class GetHomepage {

                @SystemStub
                final EnvironmentVariables envVars = createEnvVars();

                @Test
                void givenValidEnvVar_whenCallingGetHomepage_thenReturnedHomepageIsTheSame() {
                    envVars.set(BOT_HOMEPAGE, "https://robocode.dev/");
                    assertThat(EnvVars.getBotInfo().getHomepage()).isEqualTo("https://robocode.dev/");
                }

                @Test
                void givenMissingEnvVar_whenCallingGetHomepage_thenReturnNull() {
                    envVars.set(BOT_HOMEPAGE, null);
                    assertThat(EnvVars.getBotInfo().getHomepage()).isNull();
                }

                @Test
                void givenBlankEnvVar_whenCallingGetHomepage_thenReturnNull() {
                    envVars.set(BOT_HOMEPAGE, "  \t");
                    assertThat(EnvVars.getBotInfo().getHomepage()).isNull();
                }
            }

            @Nested
            class GetCountryCodes {

                @SystemStub
                final EnvironmentVariables envVars = createEnvVars();

                @Test
                void givenValidEnvVar_whenCallingGetCountryCodes_thenReturnedListContainsSameCountryCodesInUpperCase() {
                    envVars.set(BOT_COUNTRY_CODES, "dk, us");
                    assertThat(EnvVars.getBotInfo().getCountryCodes()).containsAll(List.of("DK", "US"));
                }

                @Test
                void givenInvalidEnvVar_whenCallingGetCountryCodes_thenReturnDefaultLocaleCountryCode() {
                    envVars.set(BOT_COUNTRY_CODES, "xyz");
                    assertThat(EnvVars.getBotInfo().getCountryCodes()).containsAll(getLocalCountryCodeAsList());
                }

                @Test
                void givenMissingEnvVar_whenCallingGetCountryCodes_thenReturnDefaultLocaleCountryCode() {
                    envVars.set(BOT_COUNTRY_CODES, null);
                    assertThat(EnvVars.getBotInfo().getCountryCodes()).containsAll(getLocalCountryCodeAsList());
                }

                @Test
                void givenBlankEnvVar_whenCallingGetCountryCodes_thenReturnDefaultLocaleCountryCode() {
                    envVars.set(BOT_COUNTRY_CODES, "  \t");
                    assertThat(EnvVars.getBotInfo().getCountryCodes()).containsAll(getLocalCountryCodeAsList());
                }
            }

            @Nested
            class GetPlatform {

                @SystemStub
                final EnvironmentVariables envVars = createEnvVars();

                @Test
                void givenValidEnvVar_whenCallingGetPlatform_thenReturnedPlatformIsTheSame() {
                    envVars.set(BOT_PLATFORM, "JVM");
                    assertThat(EnvVars.getBotInfo().getPlatform()).isEqualTo("JVM");
                }

                @Test
                void givenMissingEnvVar_whenCallingGetPlatform_thenReturnedPlatformMustContainJava() {
                    envVars.set(BOT_PLATFORM, null);
                    assertThat(EnvVars.getBotInfo().getPlatform()).contains("Java");
                }

                @Test
                void givenBlankEnvVar_whenCallingGetPlatform_thenReturnedPlatformMustContainJava() {
                    envVars.set(BOT_PLATFORM, "  \t");
                    assertThat(EnvVars.getBotInfo().getPlatform()).contains("Java");
                }
            }

            @Nested
            class GetProgrammingLang {

                @SystemStub
                final EnvironmentVariables envVars = createEnvVars();

                @Test
                void givenValidEnvVar_whenCallingGetProgrammingLang_thenReturnedProgrammingLangIsTheSame() {
                    envVars.set(BOT_PROG_LANG, "Java 19");
                    assertThat(EnvVars.getBotInfo().getProgrammingLang()).isEqualTo("Java 19");
                }

                @Test
                void givenMissingEnvVar_whenCallingGetProgrammingLang_thenReturnNull() {
                    envVars.set(BOT_PROG_LANG, null);
                    assertThat(EnvVars.getBotInfo().getProgrammingLang()).isNull();
                }

                @Test
                void givenBlankEnvVar_whenCallingGetProgrammingLang_thenReturnNull() {
                    envVars.set(BOT_PROG_LANG, "  \t");
                    assertThat(EnvVars.getBotInfo().getProgrammingLang()).isNull();
                }
            }

            @Nested
            class GetInitialPosition {

                @SystemStub
                final EnvironmentVariables envVars = createEnvVars();

                @Test
                void givenValidPositionEnvVar_whenCallingGetInitialPosition_thenReturnedCoordinatesAreTheSame() {
                    envVars.set(BOT_INITIAL_POS, "50, 100, 45");
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isEqualTo(100);
                    assertThat(pos.getDirection()).isEqualTo(45);
                }

                @Test
                void givenValidXCoordinateOnly_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXCoordinate() {
                    envVars.set(BOT_INITIAL_POS, "  50 ");
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isNull();
                    assertThat(pos.getDirection()).isNull();
                }

                @Test
                void givenValidXCoordinateWithCommaAfter_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXCoordinate() {
                    envVars.set(BOT_INITIAL_POS, "  50, ");
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isNull();
                    assertThat(pos.getDirection()).isNull();
                }

                @Test
                void givenValidXAndYCoordinateOnlyWithNoCommaInBetween_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXAndYCoordinate() {
                    envVars.set(BOT_INITIAL_POS, "  50 70.0");
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isEqualTo(70);
                    assertThat(pos.getDirection()).isNull();
                }

                @Test
                void givenValidXAndYCoordinateOnlyWithACommaInBetween_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXAndYCoordinate() {
                    envVars.set(BOT_INITIAL_POS, "  50.0, 70");
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isEqualTo(70);
                    assertThat(pos.getDirection()).isNull();
                }

                @Test
                void givenValidXAndYCoordinateOnlyWithACommaInBetweenAndAfter_whenCallingGetInitialPosition_thenReturnedCoordinatesContainsOnlySameXAndYCoordinate() {
                    envVars.set(BOT_INITIAL_POS, "  50, 70.0 ,");
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isEqualTo(70);
                    assertThat(pos.getDirection()).isNull();
                }

                @Test
                void givenValidCoordinatesWithCommaSeparators_whenCallingGetInitialPosition_thenReturnedCoordinatesAreTheSame() {
                    envVars.set(BOT_INITIAL_POS, "  50.0, 70, 100");
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isEqualTo(70);
                    assertThat(pos.getDirection()).isEqualTo(100);
                }

                @Test
                void givenValidCoordinatesWithMixedSpaceAndCommaSeparator_whenCallingGetInitialPosition_thenReturnedCoordinatesAreTheSame() {
                    envVars.set(BOT_INITIAL_POS, "  50, 70.0 100");
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos.getX()).isEqualTo(50);
                    assertThat(pos.getY()).isEqualTo(70);
                    assertThat(pos.getDirection()).isEqualTo(100);
                }

                @Test
                void givenEmptyPosition_whenCallingGetInitialPosition_thenReturnNull() {
                    envVars.set(BOT_INITIAL_POS, "");
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos).isNull();
                }

                @Test
                void givenBlankPosition_whenCallingGetInitialPosition_thenReturnNull() {
                    envVars.set(BOT_INITIAL_POS, "  \t");
                    var pos = EnvVars.getBotInfo().getInitialPosition();
                    assertThat(pos).isNull();
                }
            }
        }
    }

    private static List<String> getLocalCountryCodeAsList() {
        var localCountryCode = getLocalCountryCode();
        return (localCountryCode != null) ? List.of(localCountryCode) : List.of();
    }
}
