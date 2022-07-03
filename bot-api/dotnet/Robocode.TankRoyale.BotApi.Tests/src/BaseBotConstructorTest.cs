using System.Linq;
using System.Text.RegularExpressions;
using System.Threading;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;
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
        Assert.That(botHandshake.CountryCodes.ToList().ConvertAll(cc => cc.ToLower()),
            Is.EqualTo(regexCsv.Split((string)GetEnvVar(BotCountryCodes)).ToList().ConvertAll(cc => cc.ToLower())));
        Assert.That(botHandshake.Platform, Is.EqualTo(GetEnvVar(BotPlatform)));
        Assert.That(botHandshake.ProgrammingLang, Is.EqualTo(GetEnvVar(BotProgrammingLang)));
    }

    private bool ExceptionContainsEnvVarName(BotException botException, string envVarName) =>
        botException != null && botException.Message.ToUpper().Contains(envVarName);


    private static void StartBotFromThread()
    {
        new Thread(() => new TestBot().Start()).Start();
    }

    private class TestBot : BaseBot
    {
    }
}