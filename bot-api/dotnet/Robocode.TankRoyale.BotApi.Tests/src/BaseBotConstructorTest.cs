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
        SetAllEnvVarsToDefaultValues();
        _server = new MockedServer();
        _server.Start();
    }

    [TearDown]
    public void Teardown()
    {
        _server.Stop();
    }

    [Test]
    public void GivenAllRequiredEnvVarsSet_whenCallingDefaultConstructor_thenBotIsCreated()
    {
        new TestBot();
        // passed when this point is reached
    }

    [Test]
    public void GivenMissingServerUrlEnvVar_whenCallingDefaultConstructor_thenBotIsCreated()
    {
        ClearEnvVar(ServerUrl);
        new TestBot();
        // passed when this point is reached
    }

    [Test]
    public void GivenMissingBotNameEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo()
    {
        ClearEnvVar(BotName);
        var botException = Assert.Throws<BotException>(() => new TestBot());
        Assert.That(ExceptionContainsEnvVarName(botException, BotName));
    }

    [Test]
    public void GivenMissingBotVersionEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo()
    {
        ClearEnvVar(BotVersion);
        var botException = Assert.Throws<BotException>(() => new TestBot());
        Assert.That(ExceptionContainsEnvVarName(botException, BotVersion));
    }

    [Test]
    public void GivenMissingBotAuthorsEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo()
    {
        ClearEnvVar(BotAuthors);
        var botException = Assert.Throws<BotException>(() => new TestBot());
        Assert.That(ExceptionContainsEnvVarName(botException, BotAuthors));
    }

    [Test]
    public void GivenMissingBotGameTypesEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo()
    {
        ClearEnvVar(BotGameTypes);
        var botException = Assert.Throws<BotException>(() => new TestBot());
        Assert.That(ExceptionContainsEnvVarName(botException, BotGameTypes));
    }

    [Test]
    public void GivenAllRequiredEnvVarsSet_callingDefaultConstructorFromThread_thenBotIsCreatedAndConnectingToServer()
    {
        StartBotFromThread();
        Assert.That(_server.AwaitConnection(5_000), Is.True);
    }

    [Test]
    public void GivenMissingServerUrlEnvVar_callingDefaultConstructorFromThread_thenBotIsCreatedButNotConnectingToServer()
    {
        ClearEnvVar(ServerUrl);

        Assert.Throws<BotException>(() => new TestBot().Start());
    }

    [Test]
    public void GivenAllRequiredEnvVarsSet_callingDefaultConstructorFromThread_thenBotHandshakeMustBeCorrect()
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
        Assert.That(botHandshake.CountryCodes.ToList().ConvertAll(cc => cc.ToLower()),
            Is.EqualTo(new List<string> { CountryCode.GetLocalCountryCode().ToLower() }));
        Assert.That(botHandshake.Description, Is.EqualTo(GetEnvVar(BotDescription)));
        Assert.That(botHandshake.Homepage, Is.EqualTo(GetEnvVar(BotHomepage)));
        Assert.That(botHandshake.Platform, Is.EqualTo(GetEnvVar(BotPlatform)));
        Assert.That(botHandshake.ProgrammingLang, Is.EqualTo(GetEnvVar(BotProgrammingLang)));
    }

    [Test]
    public void GivenNoEnvVarsSet_callingDefaultConstructorWithBotInfoFromThread_thenBotHandshakeMustBeCorrect()
    {
        ClearAllEnvVars();
        new TestBot(CreateBotInfo());
        // passed when this point is reached
    }

    [Test]
    public void GivenServerUrlWithValidPortAsParameter_whenCallingConstructor_thenBotIsConnectingToServer()
    {
        var bot = new TestBot(null, new Uri("ws://localhost:" + MockedServer.Port));
        StartBotFromThread(bot);
        Assert.That(_server.AwaitBotHandshake(5000), Is.True);
    }

    [Test]
    public void GivenServerUrlWithInvalidPortAsParameter_whenCallingConstructor_thenBotIsNotConnectingToServer()
    {
        var bot = new TestBot(null, new Uri("ws://localhost:" + (MockedServer.Port + 1)));
        Assert.Throws<BotException>(() => bot.Start());
    }

    [Test]
    public void GivenServerSecretConstructor_whenCallingConstructor_thenReturnedBotHandshakeContainsSecret()
    {
        var secret = Guid.NewGuid().ToString();
        var bot = new TestBot(null, new Uri("ws://localhost:" + MockedServer.Port), secret);
        StartBotFromThread(bot);
        Assert.That(_server.AwaitBotHandshake(5_000), Is.True);
        var botHandshake = _server.GetBotHandshake();
        Assert.That(botHandshake.Secret, Is.EqualTo(secret));
    }

    private static void StartBotFromThread()
    {
        StartBotFromThread(new TestBot());
    }

    private static void StartBotFromThread(IBaseBot bot)
    {
        new Thread(bot.Start).Start();
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

    private class TestBot : BaseBot
    {
        public TestBot()
        {
        }

        public TestBot(BotInfo botInfo) : base(botInfo)
        {
        }

        public TestBot(BotInfo botInfo, Uri serverUrl) : base(botInfo, serverUrl)
        {
        }
        
        public TestBot(BotInfo botInfo, Uri serverUrl, string serverSecret) : base(botInfo, serverUrl, serverSecret)
        {
        }
    }
}