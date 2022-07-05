using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using Microsoft.Extensions.Configuration;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;
using Robocode.TankRoyale.BotApi.Util;

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

    [Test]
    public void Constructor_WhenGivenValidArguments_ThenBotInfoIsCreated()
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

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("  ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void Constructor_WhenNameIsNullEmptyOrBlank_ThenThrowArgumentException(string name)
    {
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            Name = name
        };
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower(), Is.EqualTo("name cannot be null, empty or blank"));
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("  ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void Constructor_WhenVersionIsNullEmptyOrBlank_ThenThrowArgumentException(string version)
    {
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            Version = version
        };
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower(), Is.EqualTo("version cannot be null, empty or blank"));
    }

    [Test]
    [TestCaseSource(nameof(InvalidListOfStrings))]
    public void Constructor_WhenAuthorsAreInvalid_ThenThrowArgumentException(List<string> authors)
    {
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            Authors = authors
        };
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower(), Is.EqualTo("authors cannot be null or empty or contain blanks"));
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("  ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void Constructor_WhenDescriptionIsNullEmptyOrBlank_ThenDescriptionMustBeNull(string description)
    {
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            Description = description
        };
        var botInfo = builder.Build();
        Assert.That(botInfo.Description, Is.Null);
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("  ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void Constructor_WhenHomepageIsNullEmptyOrBlank_ThenHomepageMustBeNull(string homepage)
    {
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            Homepage = homepage
        };
        var botInfo = builder.Build();
        Assert.That(botInfo.Homepage, Is.Null);
    }

    [Test]
    [TestCaseSource(nameof(InvalidListOfStrings))]
    public void Constructor_WhenCountryCodesAreInvalid_ThenLocalCountryCodeIsUsed(List<string> countryCodes)
    {
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            CountryCodes = countryCodes
        };
        var botInfo = builder.Build();
        Assert.That(botInfo.CountryCodes, Is.EqualTo(new List<string> { CountryCode.GetLocalCountryCode() }));
    }

    [Test]
    [TestCaseSource(nameof(InvalidListOfStrings))]
    public void Constructor_WhenGameTypesAreInvalid_ThenThrowArgumentException(List<string> gameTypes)
    {
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            GameTypes = gameTypes
        };
        var exception = Assert.Throws<ArgumentException>(() => builder.Build());
        Assert.That(exception?.Message.ToLower(), Is.EqualTo("game types cannot be null or empty or contain blanks"));
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("  ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void Constructor_WhenPlatformIsNullEmptyOrBlank_ThenUseCurrentRuntimePlatform(string homepage)
    {
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            Homepage = homepage
        };
        var botInfo = builder.Build();
        Assert.That(botInfo.Homepage, Is.Null);
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("  ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void Constructor_WhenProgrammingLangIsInvalid_ThenProgrammingLangMustBeNull(string programmingLang)
    {
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            ProgrammingLang = programmingLang
        };
        var botInfo = builder.Build();
        Assert.That(botInfo.ProgrammingLang, Is.Null);
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase("  ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void Constructor_WhenInitialPositionIsInvalid_ThenInitialPositionMustBeNull(string initialPosition)
    {
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            InitialPosition = InitialPosition.FromString(initialPosition)
        };
        var botInfo = builder.Build();
        Assert.That(botInfo.InitialPosition, Is.Null);
    }

    [Test]
    public void FromFile_WhenUsingValidFilePath_ThenBotInfoIsCreated()
    {
        var filePath = Path.Combine(TestContext.CurrentContext.WorkDirectory, "../../../resources/TestBot.json");
        var botInfo = BotInfo.FromFile(filePath);
        Assert.That(botInfo.Name, Is.EqualTo("TestBot"));
        Assert.That(botInfo.Version, Is.EqualTo("1.0"));
    }

    [Test]
    public void FromFile_WhenUsingValidFilePathAndBasePath_ThenBotInfoIsCreated()
    {
        var basePath = Path.Combine(TestContext.CurrentContext.WorkDirectory, "../../../resources");
        var botInfo = BotInfo.FromFile("TestBot.json", basePath);
        Assert.That(botInfo.Name, Is.EqualTo("TestBot"));
        Assert.That(botInfo.Version, Is.EqualTo("1.0"));
    }

    [Test]
    public void FromConfiguration_WhenUsingValidBasePathAndJsonFile_ThenBotInfoIsCreated()
    {
        var configBuilder = new ConfigurationBuilder()
            .SetBasePath(Path.Combine(TestContext.CurrentContext.WorkDirectory, "../../../resources"))
            .AddJsonFile("TestBot.json");

        var botInfo = BotInfo.FromConfiguration(configBuilder.Build());
        Assert.That(botInfo.Name, Is.EqualTo("TestBot"));
        Assert.That(botInfo.Version, Is.EqualTo("1.0"));
    }

    [Test]
    public void FromConfiguration_WhenUsingInvalidConfiguration_ThenThrowException()
    {
        Assert.That(() => BotInfo.FromConfiguration(new ConfigurationBuilder().Build()), Throws.Exception);
    }

    [Test]
    public void FromFile_WhenUsingNonExistingFile_ThenThrowFileNotFoundException()
    {
        const string filename = "non-existing-filename";
        Assert.Throws<FileNotFoundException>(() => BotInfo.FromFile(filename));
    }

    private static readonly object[] InvalidListOfStrings =
    {
        new object[] { new List<string>() },
        new object[] { new List<string> { "" } },
        new object[] { new List<string> { "\t" } },
        new object[] { new List<string> { " \n" } },
        new object[] { new List<string> { " ", "" } }
    };

    private static BotInfo CreateBotInfo()
    {
        return new BotInfo(Name, Version, Authors, Description, Homepage, CountryCodes, GameTypes, Platform,
            ProgrammingLang, InitialPosition);
    }
}