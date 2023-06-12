using System;
using System.Linq;
using System.Text.RegularExpressions;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;
using static Robocode.TankRoyale.BotApi.Tests.Test_utils.EnvironmentVariables;

namespace Robocode.TankRoyale.BotApi.Tests;

public class BaseBotConstructorTest : AbstractBotTest
{
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

    [SetUp]
    public new void SetUp()
    {
        SetAllEnvVarsToDefaultValues();
    }

    [Test]
    public void GivenAllRequiredEnvVarsSet_whenCallingDefaultConstructor_thenBotIsCreated()
    {
        new TestBot();
        Assert.Pass();
    }

    [Test]
    public void GivenMissingServerUrlEnvVar_whenCallingDefaultConstructor_thenBotIsCreated()
    {
        ClearEnvVar(ServerUrl);
        new TestBot();
        Assert.Pass();
    }

    [Test]
    public void GivenMissingBotNameEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo()
    {
        ClearEnvVar(BotName);
        var botException = Assert.Throws<BotException>(() => new TestBot());
        Assert.That(ExceptionContainsEnvVarName(botException, BotName));
    }

    [Test]
    public void
        GivenMissingBotVersionEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo()
    {
        ClearEnvVar(BotVersion);
        var botException = Assert.Throws<BotException>(() => new TestBot());
        Assert.That(ExceptionContainsEnvVarName(botException, BotVersion));
    }

    [Test]
    public void
        GivenMissingBotAuthorsEnvVar_whenCallingDefaultConstructor_thenBotExceptionIsThrownWithMissingEnvVarInfo()
    {
        ClearEnvVar(BotAuthors);
        var botException = Assert.Throws<BotException>(() => new TestBot());
        Assert.That(ExceptionContainsEnvVarName(botException, BotAuthors));
    }

    [Test]
    public void GivenAllRequiredEnvVarsSet_callingDefaultConstructorFromThread_thenBotIsCreatedAndConnectingToServer()
    {
        StartAndAwaitHandshake();
    }

    [Test]
    public void
        GivenMissingServerUrlEnvVar_callingDefaultConstructorFromThread_thenBotIsCreatedButNotConnectingToServer()
    {
        ClearEnvVar(ServerUrl);

        Assert.Throws<BotException>(() => new TestBot().Start());
    }

    [Test]
    public void GivenAllRequiredEnvVarsSet_callingDefaultConstructorFromThread_thenBotHandshakeMustBeCorrect()
    {
        StartAndAwaitHandshake();
        var botHandshake = Server.Handshake;

        var regexCsv = new Regex("\\s*,\\s*");

        Assert.That(botHandshake, Is.Not.Null);
        Assert.That(botHandshake.Name, Is.EqualTo(GetEnvVar(BotName)));
        Assert.That(botHandshake.Version, Is.EqualTo(GetEnvVar(BotVersion)));
        Assert.That(botHandshake.Authors, Is.EqualTo(regexCsv.Split((string)GetEnvVar(BotAuthors))));
        Assert.That(botHandshake.GameTypes, Is.EqualTo(regexCsv.Split((string)GetEnvVar(BotGameTypes))));
        Assert.That(botHandshake.CountryCodes.ToList().ConvertAll(cc => cc.ToLower()),
            Is.EqualTo(regexCsv.Split((string)GetEnvVar(BotCountryCodes))));
        Assert.That(botHandshake.Description, Is.EqualTo(GetEnvVar(BotDescription)));
        Assert.That(botHandshake.Homepage, Is.EqualTo(GetEnvVar(BotHomepage)));
        Assert.That(botHandshake.Platform, Is.EqualTo(GetEnvVar(BotPlatform)));
        Assert.That(botHandshake.ProgrammingLang, Is.EqualTo(GetEnvVar(BotProgrammingLang)));
    }

    [Test]
    public void GivenNoEnvVarsSet_callingDefaultConstructorWithBotInfoFromThread_thenBotHandshakeMustBeCorrect()
    {
        ClearAllEnvVars();
        new TestBot(BotInfo);
        Assert.Pass();
    }

    [Test]
    public void GivenServerUrlWithValidPortAsParameter_whenCallingConstructor_thenBotIsConnectingToServer()
    {
        var bot = new TestBot(null, new Uri("ws://localhost:" + MockedServer.Port));
        StartAsync(bot);
        Assert.That(Server.AwaitConnection(10_000), Is.True);
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
        var bot = new TestBot(null, new Uri("ws://127.0.0.1:" + MockedServer.Port), secret);
        StartAsync(bot);
        AwaitBotHandshake();
        var handshake = Server.Handshake;

        Assert.That(handshake, Is.Not.Null);
        Assert.That(handshake.Secret, Is.EqualTo(secret));
    }

    [Test]
    [TestCase("file:///")]
    [TestCase("dict://")]
    [TestCase("ftp://")]
    [TestCase("gopher://")]
    public void GivenUnknownScheme_whenCallingConstructor_thenThrowException(string scheme)
    {
        var bot = new TestBot(null, new Uri(scheme + "localhost:" + MockedServer.Port));
        try
        {
            StartAsync(bot).Wait();
        }
        catch (AggregateException e)
        {
            var ex = e.InnerException;
            Assert.That(ex, Is.InstanceOf<BotException>());
            Assert.That(ex?.Message, Does.StartWith("Wrong scheme used with server URL"));
        }
    }
}