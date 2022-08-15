using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Microsoft.Extensions.Configuration;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Util;

using static Robocode.TankRoyale.BotApi.BotInfo;
using static Robocode.TankRoyale.BotApi.Util.CountryCode;

namespace Robocode.TankRoyale.BotApi.Tests;

public class BotInfoTest
{
    static readonly string Name = "  TestBot  ";
    static readonly string Version = "  1.0  ";
    static readonly List<string> Authors = new List<string> { " Author1  ", " Author2 " };
    static readonly string Description = "  description ";
    static readonly string Homepage = " https://testbot.robocode.dev ";
    static readonly List<string> CountryCodes = new List<string> { " gb ", "  US " };
    static readonly List<string> GameTypes = new List<string> { " classic ", " melee ", " 1v1 " };
    static readonly string Platform = " .Net 6 ";
    static readonly string ProgrammingLang = " C# 10 ";
    static readonly InitialPosition InitialPosition = InitialPosition.FromString("  10, 20, 30  ");

    // -- Constructor: All parameters --

    [Test]
    public void Constructor_OK_AllFieldSetToValidCommonValues()
    {
        var botInfo = CreateBotInfo();

        Assert.That(botInfo.Name, Is.EqualTo(Name.Trim()));
        Assert.That(botInfo.Version, Is.EqualTo(Version.Trim()));
        Assert.That(botInfo.Authors, Is.EqualTo(Authors.ConvertAll(str => str.Trim())));
        Assert.That(botInfo.Description, Is.EqualTo(Description.Trim()));
        Assert.That(botInfo.Homepage, Is.EqualTo(Homepage.Trim()));
        Assert.That(botInfo.CountryCodes.ToList().ConvertAll(str => str.ToUpper()),
            Is.EqualTo(CountryCodes.ConvertAll(str => str.ToUpper()).ConvertAll(str => str.Trim())));
        Assert.That(botInfo.GameTypes, Is.EqualTo(GameTypes.ConvertAll(str => str.Trim())));
        Assert.That(botInfo.Platform, Is.EqualTo(Platform.Trim()));
        Assert.That(botInfo.ProgrammingLang, Is.EqualTo(ProgrammingLang.Trim()));
        Assert.That(botInfo.InitialPosition, Is.EqualTo(InitialPosition));
    }

    // -- Constructor: Name --

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("  ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void Constructor_Throws_NameIsNullEmptyOrBlank(string name)
    {
        var builder = PrefilledBuilder().SetName(name);
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower(), Is.EqualTo("name cannot be null, empty or blank"));
    }

    [Test]
    public void Constructor_OK_NameIsMaxLength()
    {
        var name = StringOfLength(MaxNameLength);
        var botInfo = PrefilledBuilder().SetName(name).Build();
        Assert.That(botInfo.Name, Is.EqualTo(name));
    }

    [Test]
    public void Constructor_Throws_NameLengthIsTooLong() {
        var builder = PrefilledBuilder().SetName(StringOfLength(MaxNameLength + 1));
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower().Contains("name length exceeds"), Is.True);
    }
    
    // -- Constructor: Version --

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("  ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void Constructor_Throws_VersionIsNullEmptyOrBlank(string version)
    {
        var builder = PrefilledBuilder().SetVersion(version);
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower(), Is.EqualTo("version cannot be null, empty or blank"));
    }
    
    [Test]
    public void Constructor_OK_VersionIsMaxLength()
    {
        var version = StringOfLength(MaxVersionLength);
        var botInfo = PrefilledBuilder().SetVersion(version).Build();
        Assert.That(botInfo.Version, Is.EqualTo(version));
    }
    
    [Test]
    public void Constructor_Throws_VersionLengthIsTooLong() {
        var builder = PrefilledBuilder().SetVersion(StringOfLength(MaxVersionLength + 1));
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower().Contains("version length exceeds"), Is.True);
    }

    // -- Constructor: Authors --

    [Test]
    [TestCaseSource(nameof(ListOfEmptyOrBlanks))]
    public void Constructor_Throws_AuthorsAreInvalid(List<string> authors)
    {
        var builder = PrefilledBuilder().SetAuthors(authors);
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower(), Is.EqualTo("authors cannot be null or empty or contain blanks"));
    }

    [Test]
    public void Constructor_OK_AuthorIsMaxLength()
    {
        var author = StringOfLength(MaxAuthorLength);
        var botInfo = PrefilledBuilder().SetAuthors(new List<string> { author }).Build();
        Assert.That(botInfo.Authors[0], Is.EqualTo(author));
    }
    
    [Test]
    public void Constructor_Throws_AuthorLengthIsTooLong() {
        var builder = PrefilledBuilder().AddAuthor(StringOfLength(MaxAuthorLength + 1));
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower().Contains("author length exceeds"), Is.True);
    }
    
    [Test]
    public void Constructor_OK_MaxNumberOfAuthors() {
        var builder = PrefilledBuilder().SetAuthors(null);
        for (var i = 0; i < MaxNumberOfAuthors; i++) {
            builder.AddAuthor(Authors[0]);
        }
        Assert.That(builder.Build().Authors.Count, Is.EqualTo(MaxNumberOfAuthors));
    }

    [Test]
    public void Constructor_Throw_MoreThanMaxNumberOfAuthor()
    {
        var builder = PrefilledBuilder().SetAuthors(null);
        for (var i = 0; i < MaxNumberOfAuthors + 1; i++) {
            builder.AddAuthor(Authors[0]);
        }
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower().Contains("number of authors exceeds the maximum"), Is.True);
    }

    // -- Constructor: Description --

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("  ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void Constructor_DescriptionIsNull_DescriptionIsNullOrEmptyOrBlank(string description)
    {
        var builder = PrefilledBuilder().SetDescription(description);
        var botInfo = builder.Build();
        Assert.That(botInfo.Description, Is.Null);
    }

    [Test]
    public void Constructor_OK_DescriptionIsMaxLength()
    {
        var description = StringOfLength(MaxDescriptionLength);
        var botInfo = PrefilledBuilder().SetDescription(description).Build();
        Assert.That(botInfo.Description, Is.EqualTo(description));
    }
    
    [Test]
    public void Constructor_Throws_DescriptionLengthIsTooLong() {
        var builder = PrefilledBuilder().SetDescription(StringOfLength(MaxDescriptionLength + 1));
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower().Contains("description length exceeds"), Is.True);
    }
    
    // -- Constructor: Homepage --

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("  ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void Constructor_HomePageIsNull_HomepageIsNullOrEmptyOrBlank(string homepage)
    {
        var builder = PrefilledBuilder().SetHomepage(homepage);
        var botInfo = builder.Build();
        Assert.That(botInfo.Homepage, Is.Null);
    }

    [Test]
    public void Constructor_OK_HomepageIsMaxLength()
    {
        var homepage = StringOfLength(MaxHomepageLength);
        var botInfo = PrefilledBuilder().SetHomepage(homepage).Build();
        Assert.That(botInfo.Homepage, Is.EqualTo(homepage));
    }
    
    [Test]
    public void Constructor_Throws_HomepageLengthIsTooLong() {
        var builder = PrefilledBuilder().SetHomepage(StringOfLength(MaxHomepageLength + 1));
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower().Contains("homepage length exceeds"), Is.True);
    }
    
    // -- Constructor: Country codes --
    
    [Test]
    [TestCaseSource(nameof(ListOfEmptyOrBlanks))]
    public void Constructor_WhenCountryCodesAreInvalid_ThenLocalCountryCodeIsUsed(List<string> countryCodes)
    {
        var builder = PrefilledBuilder().SetCountryCodes(countryCodes);
        var botInfo = builder.Build();
        Assert.That(botInfo.CountryCodes, Is.EqualTo(new List<string> { GetLocalCountryCode() }));
    }

    [Test]
    public void Constructor_OK_CountryCodeIsValid()
    {
        var botInfo = PrefilledBuilder().SetCountryCodes(new List<string> { "dk" }).Build();
        Assert.That(botInfo.CountryCodes[0].ToLower(), Is.EqualTo("dk"));
    }

    [Test]
    [TestCase("d")]
    [TestCase("dnk")]
    [TestCase("xx")]
    public void Constructor_DefaultCountryCode_CountryCodeIsInvalid(string countryCode)
    {
        var botInfo = PrefilledBuilder().SetCountryCodes(null).AddCountryCode(countryCode).Build();
        Assert.That(botInfo.CountryCodes[0].ToLower(), Is.EqualTo(GetLocalCountryCode().ToLower()));
    }
    
    [Test]
    public void constructor_OK_MaxNumberOfCountryCodes() {
        var builder = PrefilledBuilder().SetCountryCodes(null);
        for (var i = 0; i < MaxNumberOfCountryCodes; i++) {
            builder.AddCountryCode(CountryCodes[0]);
        }
        Assert.That(builder.Build().CountryCodes.Count, Is.EqualTo(MaxNumberOfCountryCodes));
    }

    [Test]
    public void constructor_Throw_MoreThanMaxNumberOfCountryCodes()
    {
        var builder = PrefilledBuilder().SetCountryCodes(null);
        for (var i = 0; i < MaxNumberOfCountryCodes + 1; i++) {
            builder.AddCountryCode(CountryCodes[0]);
        }
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower().Contains("number of country codes exceeds the maximum"), Is.True);
    }
    
    // -- Constructor: Game types --
    
    [Test]
    [TestCaseSource(nameof(ListOfEmptyOrBlanks))]
    public void Constructor_Throws_GameTypesContainsEmptyOrBlanks(List<string> gameTypes)
    {
        var botInfo = PrefilledBuilder().SetGameTypes(gameTypes).Build();
        Assert.That(botInfo.GameTypes.Count, Is.Zero);
    }

    [Test]
    public void Constructor_OK_GameTypeIsMaxLength()
    {
        var gameType = StringOfLength(MaxGameTypeLength);
        var botInfo = PrefilledBuilder().SetGameTypes(new List<string> { gameType }).Build();
        Assert.That(botInfo.GameTypes.Contains(gameType), Is.True);
    }
    
    [Test]
    public void Constructor_Throws_GameTypeLengthIsTooLong() {
        var builder = PrefilledBuilder().AddGameType(StringOfLength(MaxGameTypeLength + 1));
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower().Contains("game type length exceeds"), Is.True);
    }
    
    [Test]
    public void Constructor_OK_MaxNumberOfGameTypes() {
        var builder = PrefilledBuilder().SetCountryCodes(null);
        for (var i = 0; i < MaxNumberOfCountryCodes; i++) {
            builder.AddCountryCode(CountryCodes[0]);
        }
        Assert.That(builder.Build().CountryCodes.Count, Is.EqualTo(MaxNumberOfCountryCodes));
    }

    [Test]
    public void Constructor_Throw_MoreThanMaxNumberOfGameTypes()
    {
        var builder = PrefilledBuilder().SetCountryCodes(null);
        for (var i = 0; i < MaxNumberOfCountryCodes + 1; i++) {
            builder.AddCountryCode(CountryCodes[0]);
        }
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower().Contains("number of country codes exceeds the maximum"), Is.True);
    }

    // -- Constructor: Platform --
    
    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("  ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void Constructor_DefaultPlatform_PlatformIsNullOrEmptyOrBlank(string platform)
    {
        var botInfo = PrefilledBuilder().SetPlatform(platform).Build();
        Assert.That(botInfo.Platform, Is.EqualTo(PlatformUtil.GetPlatformName()));
    }

    [Test]
    public void Constructor_OK_PlatformIsMaxLength() {
        var platform = StringOfLength(MaxPlatformLength);
        var botInfo = PrefilledBuilder().SetPlatform(platform).Build();
        Assert.That(botInfo.Platform, Is.EqualTo(platform));
    }

    [Test]
    public void Constructor_Throws_PlatformLengthIsTooLong() {
        var builder = PrefilledBuilder().SetPlatform(StringOfLength(MaxPlatformLength + 1));
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower().Contains("platform length exceeds"), Is.True);
    }
    
    // -- Constructor: Programming language --
    
    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("  ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void Constructor_ProgrammingLangIsNull_ProgrammingLangIsNullOrEmptyOrBlank(string platform)
    {
        var builder = PrefilledBuilder().SetProgrammingLang(platform);
        var botInfo = builder.Build();
        Assert.That(botInfo.ProgrammingLang, Is.Null);
    }

    [Test]
    public void Constructor_OK_ProgrammingLangIsMaxLength() {
        var programmingLang = StringOfLength(MaxProgrammingLangLength);
        var botInfo = PrefilledBuilder().SetProgrammingLang(programmingLang).Build();
        Assert.That(botInfo.ProgrammingLang, Is.EqualTo(programmingLang));
    }

    [Test]
    public void Constructor_Throws_ProgrammingLangIsTooLong() {
        var builder = PrefilledBuilder().SetProgrammingLang(StringOfLength(MaxProgrammingLangLength + 1));
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower().Contains("programminglang length exceeds"), Is.True);
    }
    
    // -- Constructor: Initial position --
    
    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("  ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void constructor_InitialPositionIsNull_InitialPositionIsNullOrEmptyOrBlank(string initialPosition)
    {
        var builder = PrefilledBuilder().SetInitialPosition(InitialPosition.FromString(initialPosition));
        var botInfo = builder.Build();
        Assert.That(botInfo.InitialPosition, Is.Null);
    }

    // -- FromFile(filename) --

    [Test]
    public void FromFile_OK_ValidFilePath()
    {
        var filePath = Path.Combine(TestContext.CurrentContext.WorkDirectory, "../../../resources/TestBot.json");
        var botInfo = FromFile(filePath);
        Assert.That(botInfo.Name, Is.EqualTo("TestBot"));
        Assert.That(botInfo.Version, Is.EqualTo("1.0"));
    }

    [Test]
    public void FromFile_OK_ValidFileAndBasePath()
    {
        var basePath = Path.Combine(TestContext.CurrentContext.WorkDirectory, "../../../resources");
        var botInfo = FromFile("TestBot.json", basePath);
        Assert.That(botInfo.Name, Is.EqualTo("TestBot"));
        Assert.That(botInfo.Version, Is.EqualTo("1.0"));
    }

    [Test]
    public void FromFile_Throw_NonExistingFile()
    {
        const string filename = "non-existing-filename";
        Assert.Throws<FileNotFoundException>(() => FromFile(filename));
    }
    
    // -- FromConfiguration(filename) --

    [Test]
    public void FromConfiguration_OK_ValidFilePath()
    {
        var configBuilder = new ConfigurationBuilder()
            .SetBasePath(Path.Combine(TestContext.CurrentContext.WorkDirectory, "../../../resources"))
            .AddJsonFile("TestBot.json");

        var botInfo = FromConfiguration(configBuilder.Build());
        Assert.That(botInfo.Name, Is.EqualTo("TestBot"));
        Assert.That(botInfo.Version, Is.EqualTo("1.0"));
    }

    [Test]
    public void FromConfiguration_Throw_InvalidConfiguration()
    {
        Assert.That(() => FromConfiguration(new ConfigurationBuilder().Build()), Throws.Exception);
    }


    private static readonly object[] ListOfEmptyOrBlanks =
    {
        new object[] { new List<string>() },
        new object[] { new List<string> { "" } },
        new object[] { new List<string> { "\t" } },
        new object[] { new List<string> { " \n" } },
        new object[] { new List<string> { " ", "" } }
    };

    private static IBuilder PrefilledBuilder()
    {
        return Builder().Copy(new BotInfo(Name, Version, Authors, Description, Homepage, CountryCodes,
            GameTypes, Platform, ProgrammingLang, InitialPosition));
    }

    private static string StringOfLength(int length) => new string('x', length);
    
    private static BotInfo CreateBotInfo()
    {
        return new BotInfo(Name, Version, Authors, Description, Homepage, CountryCodes, GameTypes, Platform,
            ProgrammingLang, InitialPosition);
    }
}