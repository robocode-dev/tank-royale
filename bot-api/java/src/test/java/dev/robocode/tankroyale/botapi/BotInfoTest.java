package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Nested;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.robocode.tankroyale.botapi.BotInfo.*;
import static dev.robocode.tankroyale.botapi.util.CountryCodeUtil.getLocalCountryCode;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BotInfoTest {

    static final String NAME = "  TestBot  ";
    static final String VERSION = "  1.0  ";
    static final List<String> AUTHORS = List.of(" Author 1  ", " Author 2 ");
    static final String DESCRIPTION = "  short description ";
    static final String HOME_PAGE = " https://testbot.robocode.dev ";
    static final List<String> COUNTRY_CODES = List.of(" gb ", "  US ");
    static final List<String> GAME_TYPES = List.of(" classic ", " melee ", " 1v1 ");
    static final String PLATFORM = " JVM 19 ";
    static final String PROGRAMMING_LANGUAGE = " Java 19 ";
    static final InitialPosition INITIAL_POSITION = InitialPosition.fromString("  10, 20, 30  ");

    @Nested
    class NameTest {
        @Test
        void givenPrefilledBotInfoWithNameSet_whenGettingNameFromBotInfo_thenTrimmedNameIsReturned() {
            var botInfo = prefilledBuilder().build();
            assertThat(botInfo.getName()).isEqualTo(NAME.trim());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t ", "\n"})
        void givenNameIsNullOrEmptyOrBlank_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo(String name) {
            var builder = prefilledBuilder().setName(name);
            var exception = assertThrows(IllegalArgumentException.class, builder::build);
            assertThat(exception.getMessage()).containsIgnoringCase("'name' cannot be null, empty or blank");
        }

        @Test
        void givenNameOfMaxLength_whenConstructingBotInfo_thenReturnTheSameName() {
            var name = stringOfLength(MAX_NAME_LENGTH);
            var botInfo = prefilledBuilder().setName(name).build();
            assertThat(botInfo.getName()).isEqualTo(name);
        }

        @Test
        void givenNameOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = prefilledBuilder().setName(stringOfLength(MAX_NAME_LENGTH + 1));
            var exception = assertThrows(IllegalArgumentException.class, builder::build);
            assertThat(exception.getMessage()).containsIgnoringCase("'name' length exceeds");
        }
    }

    @Nested
    class VersionTest {
        @Test
        void givenPrefilledBotInfoWithVersionSet_whenGettingVersionFromBotInfo_thenTrimmedVersionIsReturned() {
            var botInfo = prefilledBuilder().build();
            assertThat(botInfo.getVersion()).isEqualTo(VERSION.trim());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t ", "\n"})
        void givenVersionIsNullOrEmptyOrBlank_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo(String version) {
            var builder = prefilledBuilder().setVersion(version);
            var exception = assertThrows(IllegalArgumentException.class, builder::build);
            assertThat(exception.getMessage()).containsIgnoringCase("'version' cannot be null, empty or blank");
        }

        @Test
        void givenVersionOfMaxLength_whenConstructingBotInfo_thenReturnTheSameVersion() {
            var version = stringOfLength(MAX_VERSION_LENGTH);
            var botInfo = prefilledBuilder().setVersion(version).build();
            assertThat(botInfo.getVersion()).isEqualTo(version);
        }

        @Test
        void givenVersionOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = prefilledBuilder().setVersion(stringOfLength(MAX_VERSION_LENGTH + 1));
            var exception = assertThrows(IllegalArgumentException.class, builder::build);
            assertThat(exception.getMessage()).containsIgnoringCase("'version' length exceeds");
        }
    }

    @Nested
    class AuthorsTest {
        @Test
        void givenPrefilledBotInfoWithAuthorsSet_whenGettingAuthorsFromBotInfo_thenTrimmedAuthorsCollectionIsReturned() {
            var botInfo = prefilledBuilder().build();
            assertThat(botInfo.getAuthors()).isEqualTo(AUTHORS.stream().map(String::trim).collect(Collectors.toList()));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @MethodSource("dev.robocode.tankroyale.botapi.BotInfoTest#listOfEmptyOrBlanks")
        void givenEmptyOrBlankAuthors_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo(List<String> authors) {
            var builder = prefilledBuilder().setAuthors(authors);
            var exception = assertThrows(IllegalArgumentException.class, builder::build);
            assertThat(exception.getMessage()).containsIgnoringCase("'authors' cannot be null or empty or contain blanks");
        }

        @Test
        void givenSingleAuthorOfMaxLength_whenConstructingBotInfo_thenReturnTheSameAuthor() {
            var author = stringOfLength(MAX_AUTHOR_LENGTH);
            var botInfo = prefilledBuilder().setAuthors(List.of(author)).build();
            assertThat(botInfo.getAuthors().get(0)).isEqualTo(author);
        }

        @Test
        void givenSingleAuthorOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = prefilledBuilder().addAuthor(stringOfLength(MAX_AUTHOR_LENGTH + 1));
            var exception = assertThrows(IllegalArgumentException.class, builder::build);
            assertThat(exception.getMessage()).containsIgnoringCase("'author' length exceeds");
        }

        @Test
        void givenMaxNumberOfAuthors_whenConstructingBotInfo_thenReturnAuthorsWithTheSameMaxCount() {
            var builder = prefilledBuilder().setAuthors(null);
            for (int i = 0; i < MAX_NUMBER_OF_AUTHORS; i++) {
                builder.addAuthor(AUTHORS.get(0));
            }
            assertThat(builder.build().getAuthors()).hasSize(MAX_NUMBER_OF_AUTHORS);
        }

        @Test
        void givenOneMoreThanMaxNumberOfAuthors_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = prefilledBuilder().setAuthors(null);
            for (int i = 0; i < MAX_NUMBER_OF_AUTHORS + 1; i++) {
                builder.addAuthor(AUTHORS.get(0));
            }
            var exception = assertThrows(IllegalArgumentException.class, builder::build);
            assertThat(exception.getMessage()).containsIgnoringCase("Size of 'authors' exceeds");
        }
    }

    @Nested
    class DescriptionTest {
        @Test
        void givenPrefilledBotInfoWithDescriptionSet_whenGettingDescriptionFromBotInfo_thenTrimmedDescriptionIsReturned() {
            var botInfo = prefilledBuilder().build();
            assertThat(botInfo.getDescription()).isEqualTo(DESCRIPTION.trim());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t ", "\n"})
        void givenDescriptionIsNullOrEmptyOrBlank_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo(String description) {
            var botInfo = prefilledBuilder().setDescription(description).build();
            assertThat(botInfo.getDescription()).isNull();
        }

        @Test
        void givenDescriptionOfMaxLength_whenConstructingBotInfo_thenReturnTheSameVersion() {
            var desc = stringOfLength(MAX_DESCRIPTION_LENGTH);
            var botInfo = prefilledBuilder().setDescription(desc).build();
            assertThat(botInfo.getDescription()).isEqualTo(desc);
        }

        @Test
        void givenDescriptionOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = prefilledBuilder().setDescription(stringOfLength(MAX_DESCRIPTION_LENGTH + 1));
            var exception = assertThrows(IllegalArgumentException.class, builder::build);
            assertThat(exception.getMessage()).containsIgnoringCase("'description' length exceeds");
        }
    }

    @Nested
    class HomepageTest {
        @Test
        void givenPrefilledBotInfoWithHomepageSet_whenGettingHomepageFromBotInfo_thenTrimmedHomepageIsReturned() {
            var botInfo = prefilledBuilder().build();
            assertThat(botInfo.getHomepage()).isEqualTo(HOME_PAGE.trim());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t ", "\n"})
        void givenHomepageIsNullOrEmptyOrBlank_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo(String homepage) {
            var botInfo = prefilledBuilder().setHomepage(homepage).build();
            assertThat(botInfo.getHomepage()).isNull();
        }

        @Test
        void givenHomepageOfMaxLength_whenConstructingBotInfo_thenReturnTheSameHomepage() {
            var homepage = stringOfLength(MAX_HOMEPAGE_LENGTH);
            var botInfo = prefilledBuilder().setHomepage(homepage).build();
            assertThat(botInfo.getHomepage()).isEqualTo(homepage);
        }

        @Test
        void givenHomepageOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = prefilledBuilder().setHomepage(stringOfLength(MAX_HOMEPAGE_LENGTH + 1));
            var exception = assertThrows(IllegalArgumentException.class, builder::build);
            assertThat(exception.getMessage()).containsIgnoringCase("'homepage' length exceeds");
        }
    }

    @Nested
    class CountryCodesTest {
        @Test
        void givenPrefilledBotInfoWithCountryCodesSet_whenGettingCountryCodesFromBotInfo_thenTrimmedContryCodesCollectionIsReturned() {
            var botInfo = prefilledBuilder().build();
            assertThat(botInfo.getCountryCodes()).isEqualTo(COUNTRY_CODES.stream().map(String::trim).map(String::toUpperCase).collect(Collectors.toList()));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @MethodSource("dev.robocode.tankroyale.botapi.BotInfoTest#listOfEmptyOrBlanks")
        void givenEmptyOrBlankCountryCodes_whenConstructingBotInfo_thenListOfDefaultLocalCountryCodeReturned(List<String> countryCodes) {
            var botInfo = prefilledBuilder().setCountryCodes(countryCodes).build();
            assertThat(botInfo.getCountryCodes()).containsAll(getLocalCountryCodeAsList());
        }

        @Test
        void givenListOfValidCountryCodes_whenCallingSetCountryCodes_thenReturnListOfSameCountryCodes() {
            var botInfo = prefilledBuilder().setCountryCodes(List.of("dk")).build();
            assertThat(botInfo.getCountryCodes()).isEqualTo(List.of("DK"));
        }

        @ParameterizedTest
        @ValueSource(strings = {"d", "dnk", "xx"})
        void givenListOfInvalidCountryCodes_whenCallingSetCountryCodes_thenListOfDefaultLocalCountryCodeReturned(String countryCode) {
            var botInfo = prefilledBuilder().setCountryCodes(List.of(countryCode)).build();
            assertThat(botInfo.getCountryCodes()).containsAll(getLocalCountryCodeAsList());
        }

        @Test
        void givenMaxNumberOfCountryCodes_whenConstructingBotInfo_thenReturnCountryCodesWithTheSameMaxCount() {
            var builder = prefilledBuilder().setCountryCodes(null);
            for (int i = 0; i < MAX_NUMBER_OF_COUNTRY_CODES; i++) {
                builder.addCountryCode(COUNTRY_CODES.get(0));
            }
            assertThat(builder.build().getCountryCodes()).hasSize(MAX_NUMBER_OF_COUNTRY_CODES);
        }

        @Test
        void givenOneMoreThanMaxNumberOfCountryCodes_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = prefilledBuilder().setCountryCodes(null);
            for (int i = 0; i < MAX_NUMBER_OF_COUNTRY_CODES + 1; i++) {
                builder.addCountryCode(COUNTRY_CODES.get(0));
            }
            var exception = assertThrows(IllegalArgumentException.class, builder::build);
            assertThat(exception.getMessage()).containsIgnoringCase("Size of 'countryCodes' exceeds the maximum");
        }
    }

    @Nested
    class GameTypesTest {
        @Test
        void givenPrefilledBotInfoWithGameTypesSet_whenGettingGameTypesFromBotInfo_thenTrimmedGameTypesCollectionIsReturned() {
            var botInfo = prefilledBuilder().build();
            assertThat(botInfo.getGameTypes()).isEqualTo(GAME_TYPES.stream().map(String::trim).collect(Collectors.toSet()));
        }

        @ParameterizedTest
        @NullAndEmptySource
        @MethodSource("dev.robocode.tankroyale.botapi.BotInfoTest#setOfEmptyOrBlanks")
        void givenEmptyOrBlankGameTypes_whenConstructingBotInfo_thenEmptyListIsReturned(Set<String> gameTypes) {
            var botInfo = prefilledBuilder().setGameTypes(gameTypes).build();
            assertThat(botInfo.getGameTypes()).isEmpty();
        }

        @Test
        void givenGameTypeOfMaxLength_whenConstructingBotInfo_thenReturnTheSameGameType() {
            var gameType = stringOfLength(MAX_GAME_TYPE_LENGTH);
            var botInfo = prefilledBuilder().setGameTypes(Set.of(gameType)).build();
            var optGameType = botInfo.getGameTypes().stream().findFirst();
            assertThat(optGameType).contains(gameType);
        }

        @Test
        void givenGameTypeOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = prefilledBuilder().addGameType(stringOfLength(MAX_GAME_TYPE_LENGTH + 1));
            var exception = assertThrows(IllegalArgumentException.class, builder::build);
            assertThat(exception.getMessage()).containsIgnoringCase("'gameTypes' length exceeds");
        }

        @Test
        void givenMaxNumberOfGameTypes_whenConstructingBotInfo_thenReturnCountryCodesWithTheSameMaxCount() {
            var builder = prefilledBuilder().setGameTypes(null);
            for (int i = 0; i < MAX_NUMBER_OF_GAME_TYPES; i++) {
                builder.addGameType(GAME_TYPES.get(0) + i);
            }
            assertThat(builder.build().getGameTypes()).hasSize(MAX_NUMBER_OF_GAME_TYPES);
        }

        @Test
        void givenOneMoreThanMaxNumberOfGameTypes_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = prefilledBuilder().setGameTypes(null);
            for (int i = 0; i < MAX_NUMBER_OF_GAME_TYPES + 1; i++) {
                builder.addGameType(GAME_TYPES.get(0) + i);
            }
            var exception = assertThrows(IllegalArgumentException.class, builder::build);
            assertThat(exception.getMessage()).containsIgnoringCase("Size of 'gameTypes' exceeds the maximum");
        }
    }

    @Nested
    class PlatformTest {
        @Test
        void givenPrefilledBotInfoWithPlatformSet_whenGettingPlatformFromBotInfo_thenTrimmedPlatformIsReturned() {
            var botInfo = prefilledBuilder().build();
            assertThat(botInfo.getPlatform()).isEqualTo(PLATFORM.trim());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t ", "\n"})
        void givenPlatformIsNullOrEmptyOrBlank_whenConstructingBotInfo_thenReturnStringWithJreAndVersion(String platform) {
            var builder = prefilledBuilder().setPlatform(platform);
            var botInfo = builder.build();
            assertThat(botInfo.getPlatform()).isEqualTo("Java Runtime Environment (JRE) " + System.getProperty("java.version"));
        }

        @Test
        void givenPlatformOfMaxLength_whenConstructingBotInfo_thenReturnTheSamePlatform() {
            var platform = stringOfLength(MAX_PLATFORM_LENGTH);
            var botInfo = prefilledBuilder().setPlatform(platform).build();
            assertThat(botInfo.getPlatform()).isEqualTo(platform);
        }

        @Test
        void givenPlatformOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = prefilledBuilder().setPlatform(stringOfLength(MAX_PLATFORM_LENGTH + 1));
            var exception = assertThrows(IllegalArgumentException.class, builder::build);
            assertThat(exception.getMessage()).containsIgnoringCase("'platform' length exceeds");
        }
    }

    @Nested
    class ProgrammingLangTest {
        @Test
        void givenPrefilledBotInfoWithProgrammingLangSet_whenGettingProgrammingLangFromBotInfo_thenTrimmedProgrammingLangIsReturned() {
            var botInfo = prefilledBuilder().build();
            assertThat(botInfo.getProgrammingLang()).isEqualTo(PROGRAMMING_LANGUAGE.trim());
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t ", "\n"})
        void givenProgrammingLangIsNullOrEmptyOrBlank_whenConstructingBotInfo_thenReturnNull(String programmingLang) {
            var builder = prefilledBuilder().setProgrammingLang(programmingLang);
            var botInfo = builder.build();
            assertThat(botInfo.getProgrammingLang()).isNull();
        }

        @Test
        void givenProgrammingLangOfMaxLength_whenConstructingBotInfo_thenReturnTheSameProgrammingLang() {
            var programmingLang = stringOfLength(MAX_PROGRAMMING_LANG_LENGTH);
            var botInfo = prefilledBuilder().setProgrammingLang(programmingLang).build();
            assertThat(botInfo.getProgrammingLang()).isEqualTo(programmingLang);
        }

        @Test
        void givenProgrammingLangOneCharMoreThanMaxLength_whenConstructingBotInfo_thenThrowIllegalArgumentExceptionWithErrorInfo() {
            var builder = prefilledBuilder().setProgrammingLang(stringOfLength(MAX_PROGRAMMING_LANG_LENGTH + 1));
            var exception = assertThrows(IllegalArgumentException.class, builder::build);
            assertThat(exception.getMessage()).containsIgnoringCase("'programmingLang' length exceeds");
        }
    }

    @Nested
    class InitialPositionTest {
        @Test
        void givenPrefilledBotInfoWithInitialPositionSet_whenGettingInitialPositionFromBotInfo_thenInitialPositionObjectIsReturned() {
            var botInfo = prefilledBuilder().build();
            assertThat(botInfo.getInitialPosition()).isEqualTo(INITIAL_POSITION);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t ", "\n"})
        void givenInitialPositionIsNullOrEmptyOrBlank_whenConstructingBotInfo_thenReturnNull(String initialPosition) {
            var builder = prefilledBuilder().setInitialPosition(InitialPosition.fromString(initialPosition));
            var botInfo = builder.build();
            assertThat(botInfo.getInitialPosition()).isNull();
        }
    }

    @SuppressWarnings("java:S1144") // used from @MethodSource
    private static Stream<List<String>> listOfEmptyOrBlanks() {
        return Stream.of(
                List.of(),
                List.of(""),
                List.of("\t"),
                List.of(" \n"),
                List.of(" ", "")
        );
    }

    @SuppressWarnings("java:S1144") // used from @MethodSource
    private static Stream<Set<String>> setOfEmptyOrBlanks() {
        return Stream.of(
                Set.of(),
                Set.of(""),
                Set.of("\t"),
                Set.of(" \n"),
                Set.of(" ", "")
        );
    }

    private static IBuilder prefilledBuilder() {
        return BotInfo.builder().copy(new BotInfo(NAME, VERSION, AUTHORS, DESCRIPTION, HOME_PAGE, COUNTRY_CODES,
                GAME_TYPES, PLATFORM, PROGRAMMING_LANGUAGE, INITIAL_POSITION));
    }

    private static String stringOfLength(int length) {
        if (length > 0) {
            char[] array = new char[length];
            Arrays.fill(array, 'x');
            return new String(array);
        }
        return "";
    }

    private static List<String> getLocalCountryCodeAsList() {
        var localCountryCode = getLocalCountryCode();
        return (localCountryCode != null) ? List.of(localCountryCode) : List.of();
    }
}
