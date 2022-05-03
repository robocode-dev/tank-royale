using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Runtime.Versioning;
using System.Text.RegularExpressions;
using System.Threading;
using Microsoft.Extensions.Configuration;
using Robocode.TankRoyale.BotApi.Util;

namespace Robocode.TankRoyale.BotApi;

/// <summary>
/// Bot info contains the properties of a bot.
/// </summary>
public sealed class BotInfo
{
    private string _name;
    private string _version;
    private IEnumerable<string> _authors;
    private IEnumerable<string> _countryCodes;
    private IEnumerable<string> _gameTypes;
    private string _platform;

    /// <summary>
    /// Initializes a new instance of the BotInfo class.
    /// </summary>
    /// <param name="name">The name of the bot (required).</param>
    /// <param name="version">The version of the bot (required).</param>
    /// <param name="authors">The author(s) of the bot (required).</param>
    /// <param name="description">A short description of the bot (optional).</param>
    /// <param name="homepage">The URL to a web page for the bot (optional).</param>
    /// <param name="countryCodes">The country code(s) for the bot (optional).</param>
    /// <param name="gameTypes">The game type(s) that this bot can handle (required).</param>
    /// <param name="platform">The platform used for running the bot (optional).</param>
    /// <param name="programmingLang">The programming language used for developing the bot (optional).</param>
    public BotInfo(
        string name,
        string version,
        IEnumerable<string> authors,
        string description,
        string homepage,
        IEnumerable<string> countryCodes,
        IEnumerable<string> gameTypes,
        string platform,
        string programmingLang,
        InitialPosition initialPosition)
    {
        Name = name;
        Version = version;
        Authors = authors;
        Description = description;
        Homepage = homepage;
        CountryCodes = countryCodes;
        GameTypes = gameTypes;
        Platform = platform;
        ProgrammingLang = programmingLang;
        InitialPosition = initialPosition;
    }

    /// <summary>
    /// The name, e.g., "MyBot". This field must always be provided with the bot info.
    /// </summary>
    /// <value>The name of the bot.</value>
    public string Name
    {
        get => _name;
        private set
        {
            if (string.IsNullOrWhiteSpace(value))
                throw new ArgumentException("Name cannot be null, empty or blank");
            _name = value;
        }
    }

    /// <summary>
    /// The version, e.g., "1.0". This field must always be provided with the bot info.
    /// </summary>
    /// <value>The version of the bot.</value>
    public string Version
    {
        get => _version;
        private set
        {
            if (string.IsNullOrWhiteSpace(value))
                throw new NullReferenceException("Version cannot be null, empty or blank");
            _version = value;
        }
    }

    /// <summary>
    /// List of author(s) of the bot, e.g., "John Doe (johndoe@somewhere.io)".
    /// At least one author must be provided.
    /// </summary>
    /// <value>The author(s) of the bot.</value>
    public IEnumerable<string> Authors
    {
        get => _authors;
        private set
        {
            if (value.IsNullOrEmptyOrContainsBlanks())
                throw new ArgumentException("Authors cannot be null or empty or contain blanks");
            _authors = value.ToListWithNoBlanks();
        }
    }

    /// <summary>
    /// Short description of the bot, preferably a one-liner.
    /// This field is optional.
    /// </summary>
    /// <value>A short description of the bot.</value>
    public string Description { get; }

    /// <summary>
    /// The URL of a web page for the bot.
    /// This field is optional.
    /// </summary>
    /// <value>The URL of a web page for the bot.</value>
    public string Homepage { get; }

    /// <summary>
    /// The country code(s) defined by ISO 3166-1 alpha-2, e.g. "us":
    /// https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2.
    /// If no country code is provided, the locale of the system is being used instead.
    /// </summary>
    /// <value>The country code(s) for the bot.</value>
    public IEnumerable<string> CountryCodes
    {
        get => _countryCodes;
        private set
        {
            _countryCodes = value.ToListWithNoBlanks().ConvertAll(cc => cc.ToUpper());

            foreach (var countryCode in _countryCodes)
                if (!CountryCode.IsCountryCodeValid(countryCode))
                    throw new ArgumentException($"Country Code is not valid: '{countryCode}'");

            if (CountryCodes.Any()) return;
            var list = new List<string>
            {
                // Get local country code
                Thread.CurrentThread.CurrentCulture.Name
            };
            _countryCodes = list;
        }
    }

    /// <summary>
    /// The game type(s) accepted by the bot, e.g., "classic", "melee", "1v1". This field must always be
    /// provided with the bot info. The game types define which game types the bot can participate
    /// in. See <see cref="GameType"/> for using predefined game type.
    /// </summary>
    /// <value>The game type(s) that this bot can handle.</value>
    public IEnumerable<string> GameTypes
    {
        get => _gameTypes;
        private set
        {
            if (value.IsNullOrEmptyOrContainsBlanks())
                throw new ArgumentException("Game types cannot be null or empty or contain blanks");
            _gameTypes = value.ToListWithNoBlanks().Distinct().ToHashSet();
        }
    }

    /// <summary>
    /// The platform used for running the bot, e.g., ".Net 5.0".
    /// This field is optional.
    /// </summary>
    /// <value>The platform used for running the bot.</value>
    public string Platform
    {
        get => _platform;
        private set
        {
            if (string.IsNullOrWhiteSpace(value))
                value = Assembly.GetEntryAssembly()?.GetCustomAttribute<TargetFrameworkAttribute>()?.FrameworkName;
            _platform = value;
        }
    }

    /// <summary>
    /// The programming language used for developing the bot, e.g., "C# 8.0" or "F#".
    /// This field is optional.
    /// </summary>
    /// <value>The programming language used for developing the bot.</value>
    public string ProgrammingLang { get; }

    /// <summary>
    /// The initial starting position used for debugging only, which must be enabled at the server.
    /// </summary>
    /// <value>The initial starting position used for debugging only.</value>
    public InitialPosition InitialPosition { get; }

    /// <summary>
    /// Reads the bot info from a file at the specified base path.
    /// The file is assumed to be in JSON format.
    /// </summary>
    /// <example>
    /// Example file in JSON format:
    /// <code>
    /// {
    ///   "name": "MyBot",
    ///   "version": "1.0",
    ///   "authors": "John Doe",
    ///   "description": "Short description",
    ///   "url": "https://somewhere.net/MyBot",
    ///   "countryCodes": "us",
    ///   "gameTypes": "classic, melee, 1v1",
    ///   "platform": ".Net 5.0",
    ///   "programmingLang": "C# 8.0",
    ///   "initialPosition": "50,50, 90"
    /// }
    /// </code>
    /// Note that these fields are required:
    /// <ul>
    ///   <li>name</li>
    ///   <li>version</li>
    ///   <li>authors</li>
    ///   <li>gameTypes</li>
    /// </ul>
    /// These value can take multiple values separated by a comma:
    /// <ul>
    ///   <li>authors, e.g. "John Doe, Jane Doe"</li>
    ///   <li>countryCodes, e.g. "se, no, dk"</li>
    ///   <li>gameTypes, e.g. "classic, melee, 1v1"</li>
    /// </ul>
    /// The <c>initialPosition</c> variable is optional and should <em>only</em> be used for debugging.
    /// </example>
    /// <param name="filePath">Is the file path, e.g. "bot-settings.json</param>
    /// <param name="basePath">Is the base path, e.g. <c>Directory.GetCurrentDirectory()</c>.
    /// If null, the current directory will automatically be used as base path</param>
    /// <returns> A BotInfo instance containing the bot properties read from the configuration.</returns>
    public static BotInfo FromFile(string filePath, string basePath)
    {
        basePath ??= Directory.GetCurrentDirectory();
        var configBuilder = new ConfigurationBuilder().SetBasePath(basePath).AddJsonFile(filePath);
        var config = configBuilder.Build();

        return FromConfiguration(config);
    }

    /// <summary>
    /// Reads the bot info from a file at the current working dir
    /// The file is assumed to be in JSON format.
    ///
    /// See <see cref="FromFile(String, String)"/> for an example file.
    /// </summary>
    /// <param name="filePath">Is the file path, e.g. "bot-settings.json</param>
    public static BotInfo FromFile(string filePath)
    {
        return FromFile(filePath, null);
    }

    /// <summary>
    /// Reads the bot info from a configuration.
    ///
    /// See <see cref="FromFile(String, String)"/> for an example file.
    /// </summary>
    /// <param name="configuration">Is the configuration</param>
    /// <returns> A BotInfo instance containing the bot properties read from the configuration.</returns>
    public static BotInfo FromConfiguration(IConfiguration configuration)
    {
        var name = configuration["name"];
        ThrowExceptionIfFieldIsBlank(name);

        var version = configuration["version"];
        ThrowExceptionIfFieldIsBlank(version);

        var authors = configuration["authors"];
        ThrowExceptionIfFieldIsBlank(authors);

        var gameTypes = configuration["gameTypes"];
        ThrowExceptionIfFieldIsBlank(gameTypes);

        var countryCodes = configuration["countryCodes"] ?? "";
        return new BotInfo(
            name,
            version,
            Regex.Split(authors, @"\s*,\s*"),
            configuration["description"],
            configuration["url"],
            Regex.Split(countryCodes, @"\s*,\s*"),
            Regex.Split(gameTypes, @"\s*,\s*"),
            configuration["platform"],
            configuration["programmingLang"],
            InitialPosition.FromString(configuration["initialPosition"])
        );
    }

    private static void ThrowExceptionIfFieldIsBlank(string fieldName)
    {
        if (string.IsNullOrWhiteSpace(fieldName))
        {
            throw new ArgumentException($"The required JSON field '{fieldName}' is missing or blank");
        }
    }
}