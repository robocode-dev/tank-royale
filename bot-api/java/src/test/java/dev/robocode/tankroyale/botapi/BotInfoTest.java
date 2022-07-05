package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import test_utils.BotInfoBuilder;
import test_utils.CountryCodeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BotInfoTest {

    static final String NAME = "  TestBot  ";
    static final String VERSION = "  1.0  ";
    static final List<String> AUTHORS = List.of(" Author1  ", " Author2 ");
    static final String DESCRIPTION = "  description ";
    static final String HOME_PAGE = " https://testbot.robocode.dev ";
    static final List<String> COUNTRY_CODES = List.of(" gb ", "  US ");
    static final List<String> GAME_TYPES = List.of(" classic ", " melee ", " 1v1 ");
    static final String PLATFORM = " JVM ";
    static final String PROGRAMMING_LANGUAGE = " Java 18 ";
    static final InitialPosition INITIAL_POSITION = InitialPosition.fromString("  10, 20, 30  ");

    @Test
    void constructor_whenGivenValidArguments_thenBotInfoIsCreated() {
        var botInfo = createBotInfo();

        assertThat(botInfo.getName()).isEqualTo(NAME.trim());
        assertThat(botInfo.getVersion()).isEqualTo(VERSION.trim());
        assertThat(botInfo.getAuthors()).isEqualTo(AUTHORS.stream().map(String::trim).collect(Collectors.toList()));
        assertThat(botInfo.getDescription()).isEqualTo(DESCRIPTION.trim());
        assertThat(botInfo.getHomepage()).isEqualTo(HOME_PAGE.trim());
        assertThat(botInfo.getCountryCodes().stream().map(String::toUpperCase).map(String::trim).collect(Collectors.toList()))
            .containsAll(COUNTRY_CODES.stream().map(String::toUpperCase).map(String::trim).collect(Collectors.toList()));
        assertThat(new ArrayList<>(botInfo.getGameTypes())).containsAll(GAME_TYPES.stream().map(String::trim).collect(Collectors.toList()));
        assertThat(botInfo.getPlatform()).isEqualTo(PLATFORM.trim());
        assertThat(botInfo.getProgrammingLang()).isEqualTo(PROGRAMMING_LANGUAGE.trim());
        assertThat(botInfo.getInitialPosition()).isEqualTo(INITIAL_POSITION);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void constructor_whenNameIsNullEmptyOrBlank_thenThrowIllegalArgumentException(String name) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setName(name);
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("name cannot be null, empty or blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void constructor_whenVersionIsNullEmptyOrBlank_thenThrowIllegalArgumentException(String version) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setVersion(version);
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("version cannot be null, empty or blank");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("invalidListOfStrings")
    void constructor_whenAuthorsAreInvalid_thenThrowIllegalArgumentException(List<String> authors) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setAuthors(authors);
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("authors cannot be null or empty or contain blanks");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void constructor_whenDescriptionIsNullEmptyOrBlank_thenDescriptionMustBeNull(String description) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setDescription(description);
        builder.build();
        var botInfo = builder.build();
        assertThat(botInfo.getDescription()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void constructor_whenHomepageIsInvalid_thenHomepageMustBeNull(String homepage) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setHomepage(homepage);
        var botInfo = builder.build();
        assertThat(botInfo.getHomepage()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("invalidListOfStrings")
    void constructor_whenCountryCodesAreInvalid_thenLocalCountryCodeIsUsed(List<String> countryCodes) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setCountryCodes(countryCodes);
        var botInfo = builder.build();
        assertThat(botInfo.getCountryCodes()).isEqualTo(List.of(CountryCodeUtil.getLocalCountryCode()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("invalidListOfStrings")
    void constructor_whenGameTypesAreInvalid_thenThrowIllegalArgumentException(List<String> gameTypes) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setGameTypes(gameTypes);
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("game types cannot be null or empty or contain blanks");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void constructor_whenPlatformIsInvalid_thenUseCurrentRuntimePlatform(String platform) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setPlatform(platform);
        var botInfo = builder.build();
        assertThat(botInfo.getPlatform()).isEqualTo("Java Runtime Environment (JRE) " + System.getProperty("java.version"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void constructor_whenProgrammingLangIsInvalid_thenProgrammingLangMustBeNull(String programmingLang) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setProgrammingLang(programmingLang);
        var botInfo = builder.build();
        assertThat(botInfo.getProgrammingLang()).isNull();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void constructor_whenInitialPositionIsInvalid_thenInitialPositionMustBeNull(String initialPosition) {
        var builder = new BotInfoBuilder(createBotInfo());
        builder.setInitialPosition(InitialPosition.fromString(initialPosition));
        var botInfo = builder.build();
        assertThat(botInfo.getInitialPosition()).isNull();
    }

    private static Stream<List<String>> invalidListOfStrings() {
        return Stream.of(
                List.of(),
                List.of(""),
                List.of("\t"),
                List.of(" \n"),
                List.of(" ", "")
        );
    }

    private static BotInfo createBotInfo() {
        return new BotInfo(NAME, VERSION, AUTHORS, DESCRIPTION, HOME_PAGE, COUNTRY_CODES, GAME_TYPES, PLATFORM,
                PROGRAMMING_LANGUAGE, INITIAL_POSITION);
    }
}
