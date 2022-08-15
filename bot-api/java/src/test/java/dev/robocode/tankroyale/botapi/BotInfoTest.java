package dev.robocode.tankroyale.botapi;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.robocode.tankroyale.botapi.BotInfo.*;
import static dev.robocode.tankroyale.botapi.util.CountryCodeUtil.getLocalCountryCode;
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

    // -- Constructor: All parameters --

    @Test
    void constructor_OK_AllFieldSetToValidCommonValues() {
        var botInfo = prefilledBuilder().build();

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

    // -- Constructor: Name --

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void constructor_Throws_NameIsNullEmptyOrBlank(String name) {
        var builder = prefilledBuilder().setName(name);
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("name cannot be null, empty or blank");
    }

    @Test
    void constructor_OK_NameIsMaxLength() {
        var name = stringOfLength(MAX_NAME_LENGTH);
        var botInfo = prefilledBuilder().setName(name).build();
        assertThat(botInfo.getName()).isEqualTo(name);
    }

    @Test
    void constructor_Throws_NameLengthIsTooLong() {
        var builder = prefilledBuilder().setName(stringOfLength(MAX_NAME_LENGTH + 1));
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("name length exceeds");
    }

    // -- Constructor: Version --

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void constructor_Throws_VersionIsNullEmptyOrBlank(String version) {
        var builder = prefilledBuilder().setVersion(version);
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("version cannot be null, empty or blank");
    }

    @Test
    void constructor_OK_VersionIsMaxLength() {
        var version = stringOfLength(MAX_VERSION_LENGTH);
        var botInfo = prefilledBuilder().setVersion(version).build();
        assertThat(botInfo.getVersion()).isEqualTo(version);
    }

    @Test
    void constructor_Throws_VersionLengthIsTooLong() {
        var builder = prefilledBuilder().setVersion(stringOfLength(MAX_VERSION_LENGTH + 1));
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("version length exceeds");
    }

    // -- Constructor: Authors --

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("listOfEmptyOrBlanks")
    void constructor_Throws_AuthorsAreInvalid(List<String> authors) {
        var builder = prefilledBuilder().setAuthors(authors);
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("authors cannot be null or empty or contain blanks");
    }

    @Test
    void constructor_OK_AuthorIsMaxLength() {
        var author = stringOfLength(MAX_AUTHOR_LENGTH);
        var botInfo = prefilledBuilder().setAuthors(List.of(author)).build();
        assertThat(botInfo.getAuthors().get(0)).isEqualTo(author);
    }

    @Test
    void constructor_Throws_AuthorLengthIsTooLong() {
        var builder = prefilledBuilder().addAuthor(stringOfLength(MAX_AUTHOR_LENGTH + 1));
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("author length exceeds");
    }

    @Test
    void constructor_OK_MaxNumberOfAuthors() {
        var builder = prefilledBuilder().setAuthors(null);
        for (int i = 0; i < MAX_NUMBER_OF_AUTHORS; i++) {
            builder.addAuthor(AUTHORS.get(0));
        }
        assertThat(builder.build().getAuthors().size()).isEqualTo(MAX_NUMBER_OF_AUTHORS);
    }

    @Test
    void constructor_Throw_MoreThanMaxNumberOfAuthor() {
        var builder = prefilledBuilder().setAuthors(null);
        for (int i = 0; i < MAX_NUMBER_OF_AUTHORS + 1; i++) {
            builder.addAuthor(AUTHORS.get(0));
        }
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("number of authors exceeds the maximum");
    }

    // -- Constructor: Description --

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void constructor_DescriptionIsNull_DescriptionIsNullOrEmptyOrBlank(String description) {
        var botInfo = prefilledBuilder().setDescription(description).build();
        assertThat(botInfo.getDescription()).isNull();
    }

    @Test
    void constructor_OK_DescriptionIsMaxLength() {
        var desc = stringOfLength(MAX_DESCRIPTION_LENGTH);
        var botInfo = prefilledBuilder().setDescription(desc).build();
        assertThat(botInfo.getDescription()).isEqualTo(desc);
    }

    @Test
    void constructor_Throws_DescriptionLengthIsTooLong() {
        var builder = prefilledBuilder().setDescription(stringOfLength(MAX_DESCRIPTION_LENGTH + 1));
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("description length exceeds");
    }

    // -- Constructor: Homepage --

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void constructor_HomePageIsNull_HomepageIsNullOrEmptyOrBlank(String homepage) {
        var botInfo = prefilledBuilder().setHomepage(homepage).build();
        assertThat(botInfo.getHomepage()).isNull();
    }

    @Test
    void constructor_OK_HomepageIsMaxLength() {
        var homepage = stringOfLength(MAX_HOMEPAGE_LENGTH);
        var botInfo = prefilledBuilder().setHomepage(homepage).build();
        assertThat(botInfo.getHomepage()).isEqualTo(homepage);
    }

    @Test
    void constructor_Throws_HomepageLengthIsTooLong() {
        var builder = prefilledBuilder().setHomepage(stringOfLength(MAX_HOMEPAGE_LENGTH + 1));
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("homepage length exceeds");
    }

    // -- Constructor: Country codes --

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("listOfEmptyOrBlanks")
    void constructor_DefaultCountryCode_CountryCodesContainsEmptyOrBlanks(List<String> countryCodes) {
        var botInfo = prefilledBuilder().setCountryCodes(countryCodes).build();
        assertThat(botInfo.getCountryCodes()).isEqualTo(List.of(getLocalCountryCode()));
    }

    @Test
    void constructor_OK_CountryCodeIsValid() {
        var botInfo = prefilledBuilder().setCountryCodes(List.of("dk")).build();
        assertThat(botInfo.getCountryCodes().get(0)).isEqualToIgnoringCase("dk");
    }

    @ParameterizedTest
    @ValueSource(strings = {"d", "dnk", "xx"})
    void constructor_DefaultCountryCode_CountryCodeIsInvalid(String countryCode) {
        var botInfo = prefilledBuilder().setCountryCodes(null).addCountryCode(countryCode).build();
        assertThat(botInfo.getCountryCodes().get(0)).isEqualTo(getLocalCountryCode());
    }

    @Test
    void constructor_OK_MaxNumberOfCountryCodes() {
        var builder = prefilledBuilder().setCountryCodes(null);
        for (int i = 0; i < MAX_NUMBER_OF_COUNTRY_CODES; i++) {
            builder.addCountryCode(COUNTRY_CODES.get(0));
        }
        assertThat(builder.build().getCountryCodes().size()).isEqualTo(MAX_NUMBER_OF_COUNTRY_CODES);
    }

    @Test
    void constructor_Throw_MoreThanMaxNumberOfCountryCodes() {
        var builder = prefilledBuilder().setCountryCodes(null);
        for (int i = 0; i < MAX_NUMBER_OF_COUNTRY_CODES + 1; i++) {
            builder.addCountryCode(COUNTRY_CODES.get(0));
        }
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("number of country codes exceeds the maximum");
    }

    // -- Constructor: Game types --

    @ParameterizedTest
    @NullAndEmptySource
    @MethodSource("listOfEmptyOrBlanks")
    void constructor_GameTypesIsEmpty_GameTypesContainsEmptyOrBlanks(List<String> gameTypes) {
        var botInfo = prefilledBuilder().setGameTypes(gameTypes).build();
        assertThat(botInfo.getGameTypes()).isEmpty();
    }

    @Test
    void constructor_OK_GameTypeIsMaxLength() {
        var gameType = stringOfLength(MAX_GAME_TYPE_LENGTH);
        var botInfo = prefilledBuilder().setGameTypes(List.of(gameType)).build();
        var optGameType = botInfo.getGameTypes().stream().findFirst();
        assertThat(optGameType).isPresent();
        assertThat(optGameType.get()).isEqualTo(gameType);
    }

    @Test
    void constructor_Throws_GameTypeLengthIsTooLong() {
        var builder = prefilledBuilder().addGameType(stringOfLength(MAX_GAME_TYPE_LENGTH + 1));
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("game type length exceeds");
    }

    @Test
    void constructor_OK_MaxNumberOfGameTypes() {
        var builder = prefilledBuilder().setGameTypes(null);
        for (int i = 0; i < MAX_NUMBER_OF_GAME_TYPES; i++) {
            builder.addGameType(GAME_TYPES.get(0) + i);
        }
        assertThat(builder.build().getGameTypes().size()).isEqualTo(MAX_NUMBER_OF_GAME_TYPES);
    }

    @Test
    void constructor_Throw_MoreThanMaxNumberOfGameTypes() {
        var builder = prefilledBuilder().setGameTypes(null);
        for (int i = 0; i < MAX_NUMBER_OF_GAME_TYPES + 1; i++) {
            builder.addGameType(GAME_TYPES.get(0) + i);
        }
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("number of game types exceeds the maximum");
    }

    // -- Constructor: Platform --

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void constructor_DefaultPlatform_PlatformIsNullOrEmptyOrBlank(String platform) {
        var builder = prefilledBuilder().setPlatform(platform);
        var botInfo = builder.build();
        assertThat(botInfo.getPlatform()).isEqualTo("Java Runtime Environment (JRE) " + System.getProperty("java.version"));
    }

    @Test
    void constructor_OK_PlatformIsMaxLength() {
        var platform = stringOfLength(MAX_PLATFORM_LENGTH);
        var botInfo = prefilledBuilder().setPlatform(platform).build();
        assertThat(botInfo.getPlatform()).isEqualTo(platform);
    }

    @Test
    void constructor_Throws_PlatformLengthIsTooLong() {
        var builder = prefilledBuilder().setPlatform(stringOfLength(MAX_PLATFORM_LENGTH + 1));
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("platform length exceeds");
    }

    // -- Constructor: Programming language --

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void constructor_ProgrammingLangIsNull_ProgrammingLangIsNullOrEmptyOrBlank(String programmingLang) {
        var builder = prefilledBuilder().setProgrammingLang(programmingLang);
        var botInfo = builder.build();
        assertThat(botInfo.getProgrammingLang()).isNull();
    }

    @Test
    void constructor_OK_ProgrammingLangIsMaxLength() {
        var programmingLang = stringOfLength(MAX_PROGRAMMING_LANG_LENGTH);
        var botInfo = prefilledBuilder().setProgrammingLang(programmingLang).build();
        assertThat(botInfo.getProgrammingLang()).isEqualTo(programmingLang);
    }

    @Test
    void constructor_Throws_ProgrammingLangIsTooLong() {
        var builder = prefilledBuilder().setProgrammingLang(stringOfLength(MAX_PROGRAMMING_LANG_LENGTH + 1));
        var exception = assertThrows(IllegalArgumentException.class, builder::build);
        assertThat(exception.getMessage()).containsIgnoringCase("programmingLang length exceeds");
    }

    // -- Constructor: Initial position --

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t ", "\n"})
    void constructor_InitialPositionIsNull_InitialPositionIsNullOrEmptyOrBlank(String initialPosition) {
        var builder = prefilledBuilder().setInitialPosition(InitialPosition.fromString(initialPosition));
        var botInfo = builder.build();
        assertThat(botInfo.getInitialPosition()).isNull();
    }

    // -- fromResourceFile(filename) --

    @Test
    void fromResourceFile_OK_UsingFilenameOfValidResourceFile() {
        var botInfo = BotInfo.fromResourceFile("/TestBot.json");
        assertThat(botInfo.getName()).isEqualTo("TestBot");
        assertThat(botInfo.getVersion()).isEqualTo("1.0");
    }

    @Test
    void fromResourceFile_Throws_UsingNonExistingFilename() {
        final String filename = "non-existing-filename";
        var exception = assertThrows(BotException.class, () -> BotInfo.fromResourceFile(filename));
        assertThat(exception.getMessage().toLowerCase()).isEqualTo("could not read the resource file: " + filename);
    }

    // -- fromFile(filename) --

    @Test
    void fromFile_OK_UsingFilenameOfValidResourceFile() {
        var file = Objects.requireNonNull(BotInfo.class.getResource("/TestBot.json")).getFile();
        var botInfo = BotInfo.fromFile(file);
        assertThat(botInfo.getName()).isEqualTo("TestBot");
        assertThat(botInfo.getVersion()).isEqualTo("1.0");
    }

    @Test
    void fromFile_Throws_UsingNonExistingFilename() {
        final String filename = "non-existing-filename";
        var exception = assertThrows(BotException.class, () -> BotInfo.fromFile(filename));
        assertThat(exception.getMessage().toLowerCase()).isEqualTo("could not read the file: " + filename);
    }

    // -- fromInputStream(filename) --

    @Test
    void fromInputStream_OK_UsingFilenameOfValidResourceFile() {
        var inputStream = BotInfo.class.getResourceAsStream("/TestBot.json");
        var botInfo = BotInfo.fromInputStream(inputStream);
        assertThat(botInfo.getName()).isEqualTo("TestBot");
        assertThat(botInfo.getVersion()).isEqualTo("1.0");
    }

    @Test
    void fromInputStream_Throws_ClosingValidInputStream() throws IOException {
        var inputStream = BotInfo.class.getResourceAsStream("/TestBot.json");
        assert inputStream != null;
        inputStream.close();
        assertThrows(Exception.class, () -> BotInfo.fromInputStream(inputStream));
    }

    // -- Helper methods --

    private static Stream<List<String>> listOfEmptyOrBlanks() {
        return Stream.of(
                List.of(),
                List.of(""),
                List.of("\t"),
                List.of(" \n"),
                List.of(" ", "")
        );
    }

    private static BotInfo.IBuilder prefilledBuilder() {
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
}
