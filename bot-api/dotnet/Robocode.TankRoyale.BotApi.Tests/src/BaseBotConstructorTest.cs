using System;
using System.Collections.Generic;
using System.Linq;
using System.Text.RegularExpressions;
using System.Threading;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;
using Robocode.TankRoyale.BotApi.Util;
using static Robocode.TankRoyale.BotApi.Tests.Test_utils.EnvironmentVariables;

namespace Robocode.TankRoyale.BotApi.Tests;

public class BaseBotConstructorTest
{
    private MockedServer _server;

    [SetUp]
    public void SetUp()
    {
        SetAllEnvVars();
        _server = new MockedServer();
        _server.Start();
    }

    [TearDown]
    public void Teardown()
    {
        _server.Stop();
    }

    [Test]
    public void GivenEmptyConstructor_WhenAllRequiredBotEnvVarsAreSet_TthenBotIsCreatedSuccessfully()
    {
        new TestBot();
        // passed when this point is reached
    }

    [Test]
    public void GivenEmptyConstructor_WhenServerUrlEnvVarIsMissing_ThenBotIsCreatedSuccessfully()
    {
        ClearEnvVar(ServerUrl);
        new TestBot();
        // passed when this point is reached
    }

    [Test]
    public void GivenEmptyConstructor_WhenBotNameEnvVarIsMissing_ThenBotExceptionIsThrown()
    {
        ClearEnvVar(BotName);
        var botException = Assert.Throws<BotException>(() => new TestBot());
        Assert.That(ExceptionContainsEnvVarName(botException, BotName));
    }

    [Test]
    public void GivenEmptyConstructor_WhenBotVersionEnvVarIsMissing_ThenBotExceptionIsThrown()
    {
        ClearEnvVar(BotVersion);
        var botException = Assert.Throws<BotException>(() => new TestBot());
        Assert.That(ExceptionContainsEnvVarName(botException, BotVersion));
    }

    [Test]
    public void GivenEmptyConstructor_WhenBotAuthorEnvVarIsMissing_ThenBotExceptionIsThrown()
    {
        ClearEnvVar(BotAuthors);
        var botException = Assert.Throws<BotException>(() => new TestBot());
        Assert.That(ExceptionContainsEnvVarName(botException, BotAuthors));
    }

    [Test]
    public void GivenEmptyConstructor_WhenBotGameTypesEnvVarIsMissing_ThenBotExceptionIsThrown()
    {
        ClearEnvVar(BotGameTypes);
        var botException = Assert.Throws<BotException>(() => new TestBot());
        Assert.That(ExceptionContainsEnvVarName(botException, BotGameTypes));
    }

    [Test]
    public void GivenEmptyConstructor_WhenAllRequiredBotEnvVarsAreSetAndStartingBot_ThenBotMustConnectToServer()
    {
        StartBotFromThread();
        Assert.That(_server.AwaitConnection(5_000), Is.True);
    }

    [Test]
    public void GivenEmptyConstructor_WhenServerUrlEnvVarIsMissingAndStartingBot_ThenBotCannotConnect()
    {
        ClearEnvVar(ServerUrl);

        StartBotFromThread();
        Assert.That(_server.AwaitConnection(5_000), Is.False);
    }

    [Test]
    public void GivenEmptyConstructor_WhenAllRequiredBotEnvVarsAreSetAndStartingBot_ThenBotHandshakeMustBeCorrect()
    {
        ClearEnvVar(BotCountryCodes);

        StartBotFromThread();
        Assert.That(_server.AwaitBotHandshake(5_000), Is.True);

        var botHandshake = _server.GetBotHandshake();

        var regexCsv = new Regex("\\s*,\\s*");

        Assert.That(botHandshake, Is.Not.Null);
        Assert.That(botHandshake.Name, Is.EqualTo(GetEnvVar(BotName)));
        Assert.That(botHandshake.Version, Is.EqualTo(GetEnvVar(BotVersion)));
        Assert.That(botHandshake.Authors, Is.EqualTo(regexCsv.Split((string)GetEnvVar(BotAuthors))));
        Assert.That(botHandshake.GameTypes, Is.EqualTo(regexCsv.Split((string)GetEnvVar(BotGameTypes))));
        Assert.That(botHandshake.Description, Is.EqualTo(GetEnvVar(BotDescription)));
        Assert.That(botHandshake.Homepage, Is.EqualTo(GetEnvVar(BotHomepage)));
        Assert.That(botHandshake.Platform, Is.EqualTo(GetEnvVar(BotPlatform)));
        Assert.That(botHandshake.ProgrammingLang, Is.EqualTo(GetEnvVar(BotProgrammingLang)));
        
        Assert.That(botHandshake.CountryCodes.ToList().ConvertAll(cc => cc.ToLower()),
            Is.EqualTo(new List<string> { CountryCode.GetLocalCountryCode().ToLower() }));
    }

    [Test]
    public void GivenBotInfoConstructor_WhenBotInfoAndServerUrlAndServerSecretAreValid_ThenBotIsCreated()
    {
        ClearAllEnvVars();
        new TestBot(CreateBotInfo());
        // passed when this point is reached
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase(" ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void GivenBotInfoConstructor_WhenNameIsNullEmptyOrBlank_ThenThrowIllegalArgumentException(string name)
    {
        ClearAllEnvVars();
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            Name = name
        };
        var exception = Assert.Throws<ArgumentException>(() => new TestBot(builder.Build()));
        Assert.That(exception?.Message.ToLower(), Is.EqualTo("name cannot be null, empty or blank"));
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase(" ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void GivenBotInfoConstructor_WhenVersionIsNullEmptyOrBlank_ThenThrowIllegalArgumentException(string version)
    {
        ClearAllEnvVars();
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            Version = version
        };
        var exception = Assert.Throws<ArgumentException>(() => new TestBot(builder.Build()));
        Assert.That(exception?.Message.ToLower(), Is.EqualTo("version cannot be null, empty or blank"));
    }

    [Test]
    [TestCaseSource(nameof(InvalidListOfStrings))]
    public void GivenBotInfoConstructor_WhenAuthorsIsInvalid_ThenThrowIllegalArgumentException(List<string> authors)
    {
        ClearAllEnvVars();
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            Authors = authors
        };
        var exception = Assert.Throws<ArgumentException>(() => new TestBot(builder.Build()));
        Assert.That(exception?.Message.ToLower(), Is.EqualTo("authors cannot be null or empty or contain blanks"));
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase(" ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void GivenBotInfoConstructor_WhenDescriptionIsNullEmptyOrBlank_ThenBotIsCreated(string description)
    {
        ClearAllEnvVars();
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            Description = description
        };
        new TestBot(builder.Build());
        // passed when this point is reached
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase(" ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void GivenBotInfoConstructor_WhenHomepageIsNullEmptyOrBlank_ThenBotIsCreated(string homepage)
    {
        ClearAllEnvVars();
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            Homepage = homepage
        };
        new TestBot(builder.Build());
        // passed when this point is reached
    }

    [Test]
    [TestCaseSource(nameof(InvalidListOfStrings))]
    public void GivenBotInfoConstructor_WhenGameTypesIsInvalid_ThenBotIsCreated(List<string> gameTypes)
    {
        ClearAllEnvVars();
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            GameTypes = gameTypes
        };
        var exception = Assert.Throws<ArgumentException>(() => new TestBot(builder.Build()));
        Assert.That(exception?.Message.ToLower(), Is.EqualTo("game types cannot be null or empty or contain blanks"));
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase(" ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void GivenBotInfoConstructor_WhenPlatformIsInvalid_ThenBotIsCreated(string platform)
    {
        ClearAllEnvVars();
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            Platform = platform
        };
        new TestBot(builder.Build());
        // passed when this point is reached
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase(" ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void GivenBotInfoConstructor_WhenProgrammingLangIsInvalid_ThenBotIsCreated(string programmingLang)
    {
        ClearAllEnvVars();
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            ProgrammingLang = programmingLang
        };
        new TestBot(builder.Build());
        // passed when this point is reached
    }

    [Test]
    [TestCase(null)]
    [TestCase("")]
    [TestCase(" ")]
    [TestCase("\t ")]
    [TestCase("\n")]
    public void GivenBotInfoConstructor_WhenInitialPositionIsInvalid_ThenBotIsCreated(string initialPosition)
    {
        ClearAllEnvVars();
        var builder = new BotInfoBuilder(CreateBotInfo())
        {
            InitialPosition = InitialPosition.FromString(initialPosition)
        };
        new TestBot(builder.Build());
        // passed when this point is reached
    }

    private static void StartBotFromThread()
    {
        new Thread(() => new TestBot().Start()).Start();
    }

    private bool ExceptionContainsEnvVarName(BotException botException, string envVarName) =>
        botException != null && botException.Message.ToUpper().Contains(envVarName);

    private static BotInfo CreateBotInfo()
    {
        return new BotInfo(
            "TestBot",
            "1.0",
            new List<string> { "Author1", "Author2" },
            "description",
            "https://testbot.robocode.dev",
            new List<string> { "gb", "US" },
            new List<string> { "classic", "melee", "1v1" },
            ".Net 6",
            "C# 10",
            InitialPosition.FromString("10, 20, 30")
        );
    }

    private static readonly object[] InvalidListOfStrings =
    {
        new object[] { new List<string> { } },
        new object[] { new List<string> { "" } },
        new object[] { new List<string> { "\t" } },
        new object[] { new List<string> { " \n" } },
        new object[] { new List<string> { " ", "" } }
    };

    private class TestBot : BaseBot
    {
        public TestBot()
        {
        }

        public TestBot(BotInfo botInfo) : base(botInfo)
        {
        }
    }
}