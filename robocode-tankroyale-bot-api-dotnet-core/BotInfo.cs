using System.Linq;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Threading;

namespace Robocode.TankRoyale
{
  /// <summary>
  /// Required information about the bot.
  /// </summary>
  public class BotInfo
  {
    string name;
    string version;
    string author;
    ICollection<string> gameTypes;

    /// <summary>
    /// Name, e.g. "MyBot" (required field).
    /// </summary>
    string Name
    {
      get { return name; }
      set { if (value == null) { throw new NullReferenceException("Name cannot be null"); } }
    }

    /// <summary>
    /// Version, e.g. "1.0" (required field).
    /// </summary>
    string Version
    {
      get { return version; }
      set { if (value == null) { throw new NullReferenceException("Version cannot be null"); } }
    }

    /// <summary>
    /// Author, e.g. "John Doe (johndoe@somewhere.io)" (required field).
    /// </summary>
    string Author
    {
      get { return author; }
      set { if (value == null) { throw new NullReferenceException("Author cannot be null"); } }
    }

    /// <summary>
    /// Short description of the bot, preferable a one-liner.
    /// </summary>
    string Description { get; set; }

    /// <summary>
    /// Country code defined by ISO 3166-1 alpha-2: https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2.
    /// If no country code is provided, the locale of the system is being used instead.
    /// </summary>
    string CountryCode { get; set; }

    /// <summary>
    /// Game types accepted by the bot, e.g. "melee", "1v1". The game types defines which game types
    /// the bot is able to participate in. See <see cref="GameType"/> for using predefined game type.
    /// </summary>
    ICollection<string> GameTypes
    {
      get { return gameTypes; }
      set { if (value == null) { throw new NullReferenceException("GameTypes cannot be null"); } }
    }

    /// <summary>
    /// Programming language used for developing the bot, e.g. "Java" or "C#".
    /// </summary>
    string ProgrammingLang { get; set; }

    /// <summary>
    /// Counstructor.
    /// </summary>
    public BotInfo(
      string name,
      string version,
      string author,
      string description,
      string countryCode,
      ICollection<string> gameTypes,
      string programmingLang)
    {
      if (string.IsNullOrWhiteSpace(name))
      {
        throw new ArgumentException("Name cannot be null, empty or blank");
      }
      if (string.IsNullOrWhiteSpace(version))
      {
        throw new ArgumentException("Version cannot be null, empty or blank");
      }
      if (string.IsNullOrWhiteSpace(author))
      {
        throw new ArgumentException("Author cannot be null, empty or blank");
      }
      if (gameTypes == null)
      {
        throw new ArgumentException("GameTypes cannot be null, empty or blank");
      }

      RegionInfo regionInfo = null;
      if (!string.IsNullOrWhiteSpace(countryCode))
      {
        // Get country code from input parameter
        try
        {
          regionInfo = new RegionInfo(countryCode.Trim());
        }
        catch (ArgumentException)
        {
          throw new ArgumentException($"CountryCode is not valid: '{countryCode}'");
        }
      }
      else
      {
        // Get local country code
        CultureInfo cultureInfo = Thread.CurrentThread.CurrentCulture;
        regionInfo = new RegionInfo(cultureInfo.Name);
      }

      var trimmedGameTypes = gameTypes.Where(s => !string.IsNullOrWhiteSpace(s)).Distinct().ToHashSet();
      if (trimmedGameTypes.Count == 0)
      {
        throw new ArgumentException("GameTypes does not contain any game types");
      }

      this.name = name;
      this.version = version;
      this.author = author;
      this.Description = description;
      this.CountryCode = regionInfo.TwoLetterISORegionName;
      this.gameTypes = trimmedGameTypes;
      this.ProgrammingLang = programmingLang;
    }
  }
}