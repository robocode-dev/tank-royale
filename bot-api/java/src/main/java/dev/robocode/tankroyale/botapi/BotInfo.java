package dev.robocode.tankroyale.botapi;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.neovisionaries.i18n.CountryCode;

import java.io.*;
import java.util.*;

import static dev.robocode.tankroyale.botapi.util.CollectionUtil.toMutableList;
import static dev.robocode.tankroyale.botapi.util.CollectionUtil.toMutableSet;
import static dev.robocode.tankroyale.botapi.util.CountryCodeUtil.getLocalCountryCode;
import static dev.robocode.tankroyale.botapi.util.CountryCodeUtil.toCountryCode;

/**
 * Bot info contains the properties of a bot.
 * <br>
 * <script src="../../../../../prism.js"></script>
 */
@SuppressWarnings("unused")
public final class BotInfo {

    /**
     * Maximum number of characters accepted for the name.
     */
    public static final int MAX_NAME_LENGTH = 30;

    /**
     * Maximum number of characters accepted for the version.
     */
    public static final int MAX_VERSION_LENGTH = 20;

    /**
     * Maximum number of characters accepted for an author name.
     */
    public static final int MAX_AUTHOR_LENGTH = 50;

    /**
     * Maximum number of characters accepted for the description.
     */
    public static final int MAX_DESCRIPTION_LENGTH = 250;

    /**
     * Maximum number of characters accepted for the link to the homepage.
     */
    public static final int MAX_HOMEPAGE_LENGTH = 150;

    /**
     * Maximum number of characters accepted for a game type.
     */
    public static final int MAX_GAME_TYPE_LENGTH = 20;

    /**
     * Maximum number of characters accepted for the platform name.
     */
    public static final int MAX_PLATFORM_LENGTH = 30;

    /**
     * Maximum number of characters accepted for the programming language name.
     */
    public static final int MAX_PROGRAMMING_LANG_LENGTH = 30;

    /**
     * Maximum number of authors accepted.
     */
    public static final int MAX_NUMBER_OF_AUTHORS = 5;

    /**
     * Maximum number of country codes accepted.
     */
    public static final int MAX_NUMBER_OF_COUNTRY_CODES = 5;

    /**
     * Maximum number of game types accepted.
     */
    public static final int MAX_NUMBER_OF_GAME_TYPES = 10;

    // required fields:
    private final String name;
    private final String version;
    private final List<String> authors;
    // optional fields:
    private final String description;
    private final String homepage;
    private final List<String> countryCodes;
    private final Set<String> gameTypes;
    private final String platform;
    private final String programmingLang;
    // optional special field:
    private final InitialPosition initialPosition;

    /**
     * Initializes a new instance of the BotInfo class.<br>
     * </br>
     * Note that the recommended method for creating a BotInfo class is to use the {@link IBuilder} interface provided
     * with the static {@link BotInfo#builder} method.
     *
     * @param name            is the name of the bot (required).
     * @param version         is the version of the bot (required).
     * @param authors         is the author(s) of the bot (required).
     * @param description     is a short description of the bot (optional).
     * @param homepage        is the link to a homepage for the bot (optional).
     * @param countryCodes    is the country code(s) for the bot (optional).
     * @param gameTypes       is the game types that this bot can handle (optional).
     * @param platform        is the platform used for running the bot (optional).
     * @param programmingLang is the programming language used for developing the bot (optional).
     * @param initialPosition is the initial position with starting coordinate and angle (optional).
     */
    public BotInfo(
            final String name,
            final String version,
            final List<String> authors,
            final String description,
            final String homepage,
            final List<String> countryCodes,
            final Collection<String> gameTypes,
            final String platform,
            final String programmingLang,
            final InitialPosition initialPosition) {

        this.name = processName(name);
        this.version = processVersion(version);
        this.authors = processAuthors(authors);
        this.description = processDescription(description);
        this.homepage = processHomepage(homepage);
        this.countryCodes = processCountryCodes(countryCodes);
        this.gameTypes = processGameTypes(gameTypes);
        this.platform = processPlatform(platform);
        this.programmingLang = processProgrammingLang(programmingLang);
        this.initialPosition = initialPosition;
    }

    /**
     * Returns a builder for a convenient way of building a {@link BotInfo} object using the
     * <a href="https://en.wikipedia.org/wiki/Builder_pattern">builder pattern</a>.<br>
     * <br>
     * Example of use:
     * <pre><code class="language-java">
     * BotInfo botInfo = BotInfo.builder()
     *     .setName("Rampage")
     *     .setVersion("1.0")
     *     .addAuthor("John Doh")
     *     .setGameTypes(List.of(GameType.CLASSIC, GameType.MELEE))
     *     .build();
     * </code></pre>
     *
     * @return a builder for building a {@link BotInfo} object.
     */
    public static IBuilder builder() {
        return new Builder();
    }

    /**
     * Returns the name, e.g., "MyBot". This field must always be provided with the bot info.
     *
     * @return The name of the bot.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the version, e.g., "1.0". This field must always be provided with the bot info.
     *
     * @return The version of the bot.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the list of authors of the bot, e.g., "John Doe (johndoe@somewhere.io)". At least one
     * author must be provided.
     *
     * @return The name(s) of the author(s) of the bot.
     */
    public List<String> getAuthors() {
        return authors;
    }

    /**
     * Returns a short description of the bot, preferably a one-liner.<br>
     * This field is optional.
     *
     * @return a short description of the bot.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the URL of a web page for the bot.<br>
     * This field is optional.
     *
     * @return The URL of a web page for the bot.
     */
    public String getHomepage() {
        return homepage;
    }

    /**
     * Returns a list of country code(s) defined by
     * <a href="https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2">ISO 3166-1 alpha-2</a>, e.g. { "us" }
     * This field is optional. If no country codes are provided, the locale of the system is being
     * used instead.
     *
     * @return The country code(s) for the bot.
     */
    public List<String> getCountryCodes() {
        return countryCodes;
    }

    /**
     * Returns the game type(s) accepted by the bot, e.g., "classic", "melee", "1v1". At least one
     * game type must be provided to indicate the type(s) of games that this bot can participate in.
     * The game types define which game types the bot can participate in. See {@link GameType} for
     * using predefined game type.
     *
     * @return The game type(s) that this bot can handle.
     */
    public Set<String> getGameTypes() {
        return gameTypes;
    }

    /**
     * Returns the platform used for running the bot, e.g., "Java Runtime Environment (JRE) 11".<br>
     * This field is optional.
     *
     * @return The platform used for running the bot.
     */
    public String getPlatform() {
        return platform;
    }

    /**
     * Returns the programming language used for developing the bot, e.g., "Java 11" or "Kotlin 1.7.20".<br>
     * This field is optional.
     *
     * @return The programming language used for developing the bot.
     */
    public String getProgrammingLang() {
        return programmingLang;
    }

    /**
     * Returns the initial starting position used for debugging only, which must be enabled at the server.<br>
     * This field is optional.
     *
     * @return The initial starting position used for debugging only.
     */
    public InitialPosition getInitialPosition() {
        return initialPosition;
    }

    /**
     * Reads the bot info from a resource file, e.g. when the file is located in a jar file or resource path in IDE.<br>
     * The file is assumed to be in JSON format.<br>
     * <br>
     * See the {@link #fromInputStream} to see the required JSON format for the file.
     *
     * @param filename is the filename of the file containing bot properties.
     * @return A BotInfo instance containing the bot properties read from the file.
     * @throws BotException if the resource file could not be read, or if some field read from the file is invalid.
     * @see #fromFile
     * @see #fromInputStream
     */
    public static BotInfo fromResourceFile(String filename) {
        try (InputStream is = BotInfo.class.getResourceAsStream(filename)) {
            if (is == null) {
                throw new FileNotFoundException("File not found: " + filename);
            }
            return fromInputStream(is);
        } catch (IOException ioe) {
            throw new BotException("Could not read the resource file: " + filename, ioe);
        }
    }

    /**
     * Reads the bot info from a local file on a file system.<br>
     * The file is assumed to be in JSON format.<br>
     * <br>
     * See the {@link #fromInputStream} to see the required JSON format for the file.
     *
     * @param filename is the filename of the file containing bot properties.
     * @return A BotInfo instance containing the bot properties read from the file.
     * @throws BotException if the file could not be read, or if some field read from the file is invalid.
     * @see #fromResourceFile
     * @see #fromInputStream
     */
    public static BotInfo fromFile(String filename) {
        try (InputStream is = new FileInputStream(filename)) {
            return fromInputStream(is);
        } catch (IOException ioe) {
            throw new BotException("Could not read the file: " + filename, ioe);
        }
    }

    /**
     * Reads the bot info from an input stream.<br>
     * The file is assumed to be in JSON format.<br>
     * <br>
     * Example file in JSON format:<br>
     *
     * <pre><code class="language-java">
     * {
     *   "name": "MyBot",
     *   "version": "1.0",
     *   "authors": "John Doe",
     *   "description": "Short description",
     *   "homepage": "https://somewhere.net/MyBot",
     *   "countryCodes": "us",
     *   "gameTypes": "classic, melee, 1v1",
     *   "platform": "JVM",
     *   "programmingLang": "Java 11",
     *   "initialPosition": "50,50, 90"
     * }
     * </code></pre>
     * Note that these fields are required as these are used to identify the bot:
     * <ul>
     *     <li>name</li>
     *     <li>version</li>
     *     <li>authors</li>
     * </ul>
     * These value can take multiple values separated by a comma:
     * <ul>
     *     <li>authors, e.g. "John Doe, Jane Doe"</li>
     *     <li>countryCodes, e.g. "se, no, dk"</li>
     *     <li>gameTypes, e.g. "classic, melee, 1v1"</li>
     * </ul>
     * The {@code initialPosition} variable is optional and should <em>only</em> be used for debugging.<br>
     * <br>
     * The {@code gameTypes} is optional, but can be used to limit which game types the bot is capable of
     * participating in.
     *
     * @param inputStream is the input stream providing the bot properties.
     * @return A BotInfo instance containing the bot properties read from the stream.
     * @throws BotException if some fields read from the stream is invalid.
     * @see #fromFile
     * @see #fromResourceFile
     */
    public static BotInfo fromInputStream(InputStream inputStream) {
        var gson = new Gson();
        var reader = new JsonReader(new InputStreamReader(inputStream));
        JsonProperties data = gson.fromJson(reader, JsonProperties.class);

        throwExceptionIfJsonFieldIsBlank("name", data.name);
        throwExceptionIfJsonFieldIsBlank("version", data.version);
        throwExceptionIfJsonFieldIsNullOrEmpty("authors", data.authors);

        List<String> countryCodes = data.countryCodes;
        if (countryCodes == null) {
            countryCodes = Collections.emptyList();
        }
        return new BotInfo(
                data.name,
                data.version,
                data.authors,
                data.description,
                data.homepage,
                countryCodes,
                data.gameTypes == null ? null : new HashSet<>(data.gameTypes),
                data.platform,
                data.programmingLang,
                InitialPosition.fromString(data.initialPosition));
    }

    private static String processName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("'name' cannot be null, empty or blank");
        }
        name = name.trim();
        if (name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException("'name' length exceeds the maximum of " + MAX_NAME_LENGTH + " characters");
        }
        return name;
    }

    private static String processVersion(String version) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("'version' cannot be null, empty or blank");
        }
        version = version.trim();
        if (version.length() > MAX_VERSION_LENGTH) {
            throw new IllegalArgumentException("'version' length exceeds the maximum of " + MAX_VERSION_LENGTH + " characters");
        }
        return version;
    }

    private static List<String> processAuthors(List<String> authors) {
        if (isNullOrEmptyOrContainsOnlyBlanks(authors)) {
            throw new IllegalArgumentException("'authors' cannot be null or empty or contain blanks");
        }
        if (authors.size() > MAX_NUMBER_OF_AUTHORS) {
            throw new IllegalArgumentException("Size of 'authors' exceeds the maximum of " + MAX_NUMBER_OF_AUTHORS);
        }
        List<String> authorsCopy = new ArrayList<>();
        authors.stream().filter(Objects::nonNull).forEach(author -> {
            author = author.trim();
            if (author.length() > MAX_AUTHOR_LENGTH) {
                throw new IllegalArgumentException("'author' length exceeds the maximum of " + MAX_AUTHOR_LENGTH + " characters");
            }
            authorsCopy.add(author);
        });
        authorsCopy.removeIf(String::isBlank);

        return authorsCopy;
    }

    private static String processDescription(String description) {
        if (description != null && description.trim().length() > MAX_DESCRIPTION_LENGTH) {
            throw new IllegalArgumentException("'description' length exceeds the maximum of " + MAX_DESCRIPTION_LENGTH + " characters");
        }
        return toNullIfBlankElseTrim(description);
    }

    private static String processHomepage(String homepage) {
        if (homepage != null && homepage.trim().length() > MAX_HOMEPAGE_LENGTH) {
            throw new IllegalArgumentException("'homepage' length exceeds the maximum of " + MAX_HOMEPAGE_LENGTH + " characters");
        }
        return toNullIfBlankElseTrim(homepage);
    }

    private static List<String> processCountryCodes(List<String> countryCodeStrings) {
        List<CountryCode> countryCodes = new ArrayList<>();
        if (countryCodeStrings != null) {
            countryCodeStrings.stream().filter(Objects::nonNull).forEach(string -> {
                var countryCode = toCountryCode(string);
                if (countryCode != null) {
                    countryCodes.add(countryCode);
                }
            });
        }
        if (countryCodes.isEmpty()) {
            var countryCode = toCountryCode(getLocalCountryCode());
            if (countryCode != null) {
                countryCodes.add(countryCode);
            }
        }
        if (countryCodes.size() > MAX_NUMBER_OF_COUNTRY_CODES) {
            throw new IllegalArgumentException("Size of 'countryCodes' exceeds the maximum of " + MAX_NUMBER_OF_COUNTRY_CODES);
        }

        List<String> countryCodesAlpha2 = new ArrayList<>();
        countryCodes.forEach(countryCode -> countryCodesAlpha2.add(countryCode.getAlpha2()));

        return countryCodesAlpha2;
    }

    private static Set<String> processGameTypes(Collection<String> gameTypes) {
        if (isNullOrEmptyOrContainsOnlyBlanks(gameTypes)) {
            return Collections.emptySet();
        }
        if (gameTypes.size() > MAX_NUMBER_OF_GAME_TYPES) {
            throw new IllegalArgumentException("Size of 'gameTypes' exceeds the maximum of " + MAX_NUMBER_OF_GAME_TYPES);
        }
        Set<String> gameTypesCopy = new HashSet<>();
        gameTypes.stream().filter(Objects::nonNull).forEach(gameType -> {
            gameType = gameType.trim();
            if (gameType.length() > MAX_GAME_TYPE_LENGTH) {
                throw new IllegalArgumentException("'gameTypes' length exceeds the maximum of " + MAX_GAME_TYPE_LENGTH + " characters");
            }
            gameTypesCopy.add(gameType);
        });
        gameTypesCopy.removeIf(String::isBlank);

        return gameTypesCopy;
    }

    private static String processPlatform(String platform) {
        if (platform == null || platform.trim().isEmpty()) {
            return "Java Runtime Environment (JRE) " + System.getProperty("java.version");
        }
        if (platform.trim().length() > MAX_PLATFORM_LENGTH) {
            throw new IllegalArgumentException("'platform' length exceeds the maximum of " + MAX_PLATFORM_LENGTH + " characters");
        }
        return toNullIfBlankElseTrim(platform);
    }

    private static String processProgrammingLang(String programmingLang) {
        if (programmingLang != null && programmingLang.trim().length() > MAX_PROGRAMMING_LANG_LENGTH) {
            throw new IllegalArgumentException("'programmingLang' length exceeds the maximum of " + MAX_PROGRAMMING_LANG_LENGTH + " characters");
        }
        return toNullIfBlankElseTrim(programmingLang);
    }

    private static void throwExceptionIfJsonFieldIsBlank(String fieldName, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("The required field '" + fieldName + "' is missing or blank");
        }
    }

    private static void throwExceptionIfJsonFieldIsNullOrEmpty(String fieldName, List<String> value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("The required field '" + fieldName + "' is missing or empty");
        }
    }

    private static boolean isNullOrEmptyOrContainsOnlyBlanks(Collection<String> collection) {
        return (collection == null || collection.isEmpty() || collection.stream().allMatch(String::isBlank));
    }

    private static String toNullIfBlankElseTrim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static class JsonProperties {
        String name;
        String version;
        List<String> authors;
        String description;
        String homepage;
        List<String> countryCodes;
        Set<String> gameTypes;
        String platform;
        String programmingLang;
        String initialPosition;
    }

    /**
     * Builder interface for providing a builder for building {@link BotInfo} objects, and which supports method
     * chaining.
     */
    public interface IBuilder {

        /**
         * Builds and returns the {@link BotInfo} instance based on the data set and added to this builder so far.
         * This method is typically the last method to call on the builder in order to extract the result of building.
         *
         * @return a {@link BotInfo} instance.
         */
        BotInfo build();

        /**
         * Copies all fields from a {@link BotInfo} instance into this builder.
         *
         * @param botInfo is the {@link BotInfo} instance to copy.
         * @return this {@link IBuilder} instance provided for method chaining.
         */
        IBuilder copy(BotInfo botInfo);

        /**
         * Sets the bot name. (required)<br>
         * <br>
         * Note that the maximum length of the name is {@value MAX_NAME_LENGTH} characters.<br>
         * <br>
         * Example of a name: "Rampage"
         *
         * @param name is the name of the bot.
         * @return this {@link IBuilder} instance provided for method chaining.
         */
        IBuilder setName(String name);

        /**
         * Sets the bot version. (required)<br>
         * <br>
         * Note that the maximum length of the version is {@value MAX_VERSION_LENGTH} characters.<br>
         * <br>
         * Example of a version: "1.0"
         *
         * @param version is the version of the bot.
         * @return this {@link IBuilder} instance provided for method chaining.
         */
        IBuilder setVersion(String version);

        /**
         * Sets the names(s) of the author(s) of the bot. (required)<br>
         * <br>
         * Note that the maximum length of an author name is {@value MAX_AUTHOR_LENGTH} characters, and the maximum
         * number of names is {@value MAX_NUMBER_OF_AUTHORS}.<br>
         * <br>
         * Example of the name of an author: "John Doe"
         *
         * @param authors is a list containing the names(s) of the author(s).
         *                A {@code null} removes all authors.
         * @return this {@link IBuilder} instance provided for method chaining.
         * @see #addAuthor
         */
        IBuilder setAuthors(List<String> authors);

        /**
         * Adds an author of the bot. (required)<br>
         * <br>
         * See {@link #setAuthors} for more details.<br>
         *
         * @param author is the name of an author to add.
         * @return this {@link IBuilder} instance provided for method chaining.
         * @see #setAuthors
         */
        IBuilder addAuthor(String author);

        /**
         * Sets a short description of the bot. (optional)<br>
         * <br>
         * Note that the maximum length of the description is {@value MAX_DESCRIPTION_LENGTH} characters. Line-breaks
         * (line-feed / new-line character) are supported, but only expect up to 3 lines to be displayed on a UI.<br>
         * <br>
         * Example of a description:
         * <pre>
         * "The rampage bot will try to ram bots that are very close.\n
         * Sneaks around the corners and shoot at the bots that come too near."
         * </pre>
         *
         * @param description is a short description of the bot.
         * @return this {@link IBuilder} instance provided for method chaining.
         */
        IBuilder setDescription(String description);

        /**
         * Sets a link to the homepage for the bot. (optional)<br>
         * <br>
         * Note that the maximum length of a link is {@value MAX_HOMEPAGE_LENGTH} characters.<br>
         * <br>
         * Example of a link: "https://fictive-homepage.net/Rampage"
         *
         * @param homepage is a link to a homepage for the bot.
         * @return this {@link IBuilder} instance provided for method chaining.
         */
        IBuilder setHomepage(String homepage);

        /**
         * Sets the country codes for the bot. (optional)<br>
         * <br>
         * Note that the maximum length of each country code is 2 (alpha-2) from the ISO 3166 international standard,
         * and the maximum number of country codes is {@value MAX_NUMBER_OF_COUNTRY_CODES}.<br>
         * <br>
         * Example of a country code: "dk"<br>
         * <br>
         * Note that if no country code is specified, or the none of the country codes provided is valid, then the
         * default a list containing a single country code will automatically be used containing the current locale
         * country code. The current local country code will be extracted using {@link Locale#getDefault()}.
         *
         * @param countryCodes is a list containing the country codes.
         *                     A {@code null} removes all country codes.
         * @return this {@link IBuilder} instance provided for method chaining.
         * @see #addCountryCode
         */
        IBuilder setCountryCodes(List<String> countryCodes);

        /**
         * Adds a country code for the bot. (optional)<br>
         * <br>
         * See {@link #setCountryCodes} for more details.
         *
         * @param countryCode is the country code to add.
         * @return this {@link IBuilder} instance provided for method chaining.
         * @see #setCountryCodes
         */
        IBuilder addCountryCode(String countryCode);

        /**
         * Sets the game types that this bot is capable of participating in. (required)<br>
         * <br>
         * The standard game types <a href="https://robocode-dev.github.io/tank-royale/articles/game_types.html">
         * are listed here</a>.<br>
         * <br>
         * Note that more game types might be added in the future.<br>
         * <br>
         * The {@link GameType} class contains the string for the current predefined game types, which can be used
         * when setting the game types of this method.<br>
         * <br>
         * Note that the maximum length of a game type is {@value MAX_GAME_TYPE_LENGTH}, and the maximum number of
         * game types is {@value MAX_NUMBER_OF_GAME_TYPES}.<br>
         * <br>
         * Example of a game type: "classic"<br>
         * <br>
         * Example of usage:
         * <pre><code class="language-java">
         * BotInfo.builder()
         *     .setGameTypes(Set.of(GameType.CLASSIC, GameType.MELEE, "future-type"))
         *     ...
         * </code></pre>
         *
         * @param gameTypes is a set of game types that the bot is capable of participating in.
         *                  A {@code null} removes all game types.
         * @return this {@link IBuilder} instance provided for method chaining.
         * @see #addGameType 
         */
        IBuilder setGameTypes(Set<String> gameTypes);

        /**
         * Adds a game type that this bot is capable of participating in. (required)<br>
         * <br>
         * See {@link #setGameTypes} for more details.<br>
         * <br>
         * Example of usage:
         * <pre><code class="language-java">
         * BotInfo.builder()
         *     .addGameType(GameType.CLASSIC)
         *     .addGameType(GameType.MELEE)
         *     ...
         * </code></pre>
         *
         * @param gameType is a game type that the bot is capable of participating in.
         * @return this {@link IBuilder} instance provided for method chaining.
         * @see #setGameTypes
         */
        IBuilder addGameType(String gameType);

        /**
         * Sets the name of the platform that this bot is build for. (optional)<br>
         * <br>
         * Note that the maximum length of the name of the platform is {@value MAX_PLATFORM_LENGTH}.<br>
         * <br>
         * If the platform is set to {@code null} or a blank string, then this default string will be used for this API:
         * <pre>
         * Java Runtime Environment (JRE) [version]
         * </pre>
         *
         * @param platform is the name of the platform that this bot is build for.
         * @return this {@link IBuilder} instance provided for method chaining.
         */
        IBuilder setPlatform(String platform);

        /**
         * Sets the name of the programming language used for developing this bot. (optional)<br>
         * <br>
         * Note that the maximum length of the name of the programming language is {@value MAX_PROGRAMMING_LANG_LENGTH}.
         *
         * @param programmingLang is the name of the programming language used for developing this bot.
         * @return this {@link IBuilder} instance provided for method chaining.
         */
        IBuilder setProgrammingLang(String programmingLang);

        /**
         * Sets the initial position of this bot. (optional)<br>
         * <br>
         * Note that initial positions must be enabled/allowed with the game (server) in order to take effect.
         *
         * @param initialPosition is the initial position of this bot.
         * @return this {@link IBuilder} instance provided for method chaining.
         */
        IBuilder setInitialPosition(InitialPosition initialPosition);
    }

    private static final class Builder implements IBuilder {

        private String name;
        private String version;
        private List<String> authors = new ArrayList<>();
        private String description;
        private String homepage;
        private List<String> countryCodes = new ArrayList<>();
        private Set<String> gameTypes = new HashSet<>();
        private String platform;
        private String programmingLang;
        private InitialPosition initialPosition;

        @Override
        public BotInfo build() {
            return new BotInfo(name, version, authors, description, homepage, countryCodes, gameTypes, platform,
                    programmingLang, initialPosition);
        }

        @Override
        public IBuilder copy(BotInfo botInfo) {
            name = botInfo.getName();
            version = botInfo.getVersion();
            authors = botInfo.getAuthors();
            description = botInfo.getDescription();
            homepage = botInfo.getHomepage();
            countryCodes = botInfo.getCountryCodes();
            gameTypes = botInfo.getGameTypes();
            platform = botInfo.getPlatform();
            programmingLang = botInfo.getProgrammingLang();
            initialPosition = botInfo.getInitialPosition();
            return this;
        }

        @Override
        public IBuilder setName(String name) {
            this.name = name;
            return this;
        }

        @Override
        public IBuilder setVersion(String version) {
            this.version = version;
            return this;
        }

        @Override
        public IBuilder setAuthors(List<String> authors) {
            this.authors = toMutableList(authors);
            return this;
        }

        @Override
        public IBuilder addAuthor(String author) {
            authors.add(author);
            return this;
        }

        @Override
        public IBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        @Override
        public IBuilder setHomepage(String homepage) {
            this.homepage = homepage;
            return this;
        }

        @Override
        public IBuilder setCountryCodes(List<String> countryCodes) {
            this.countryCodes = toMutableList(countryCodes);
            return this;
        }

        @Override
        public IBuilder addCountryCode(String countryCode) {
            countryCodes.add(countryCode);
            return this;
        }

        @Override
        public IBuilder setGameTypes(Set<String> gameTypes) {
            this.gameTypes = toMutableSet(gameTypes);
            return this;
        }

        @Override
        public IBuilder addGameType(String gameType) {
            gameTypes.add(gameType);
            return this;
        }

        @Override
        public IBuilder setPlatform(String platform) {
            this.platform = platform;
            return this;
        }

        @Override
        public IBuilder setProgrammingLang(String programmingLang) {
            this.programmingLang = programmingLang;
            return this;
        }

        @Override
        public IBuilder setInitialPosition(InitialPosition initialPosition) {
            this.initialPosition = initialPosition;
            return this;
        }
    }
}
