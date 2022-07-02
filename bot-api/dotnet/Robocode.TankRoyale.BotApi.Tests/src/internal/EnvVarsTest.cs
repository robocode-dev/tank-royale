using System.Linq;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Internal;
using Robocode.TankRoyale.BotApi.Util;
using static Robocode.TankRoyale.BotApi.Tests.Test_utils.EnvironmentVariables;

namespace Robocode.TankRoyale.BotApi.Tests.Internal;

public class EnvVarsTest
{
    [SetUp]
    public void SetUp()
    {
        SetAllEnvVars();
    }
    
    [Test]
    public void GetBotInfo_ShouldWorkWhenAllEnvVarsAreSetCorrectly()
    {
        var info = EnvVars.GetBotInfo();
        Assert.That(info.Name, Is.EqualTo("MyBot"));
        Assert.That(info.Version, Is.EqualTo("1.0"));
        Assert.That(info.Authors, Is.SupersetOf(new[] { "Author1", "Author2" }));
        Assert.That(info.GameTypes, Is.SupersetOf(new[] { "1v1", "classic", "melee" }));
        Assert.That(info.Description, Is.EqualTo("Short description"));
        Assert.That(info.Homepage, Is.EqualTo("https://somewhere.net/MyBot"));
        Assert.That(info.CountryCodes, Is.SupersetOf(new[] { "US", "GB" }));
        Assert.That(info.Platform, Is.EqualTo(".Net 5"));
        Assert.That(info.ProgrammingLang, Is.EqualTo("C# 8"));
        Assert.That(info.InitialPosition, Is.EqualTo(InitialPosition.FromString("50,50, 90")));
    }

    [Test]
    public void GetBotInfo_ShouldFailWhenBotNameIsNull()
    {
        ClearEnvVar(BotName);

        Assert.Throws<BotException>(() => EnvVars.GetBotInfo());
    }

    [Test]
    public void GetBotInfo_ShouldFailWhenBotNameIsBlank()
    {
        SetEnvVar(BotName, "  \t");

        Assert.Throws<BotException>(() => EnvVars.GetBotInfo());
    }

    [Test]
    public void GetBotInfo_ShouldFailWhenBotVersionIsNull()
    {
        ClearEnvVar(BotVersion);

        Assert.Throws<BotException>(() => EnvVars.GetBotInfo());
    }

    [Test]
    public void GetBotInfo_ShouldFailWhenBotVersionIsBlank()
    {
        SetEnvVar(BotVersion, "  \t");

        Assert.Throws<BotException>(() => EnvVars.GetBotInfo());
    }

    [Test]
    public void GetBotInfo_ShouldFailWhenBotAuthorsIsNull()
    {
        ClearEnvVar(BotAuthors);

        Assert.Throws<BotException>(() => EnvVars.GetBotInfo());
    }

    [Test]
    public void GetBotInfo_ShouldFailWhenBotAuthorsIsBlank()
    {
        SetEnvVar(BotAuthors, "  \t");

        Assert.Throws<BotException>(() => EnvVars.GetBotInfo());
    }

    [Test]
    public void GetBotInfo_ShouldFailWhenBotGameTypesIsNull()
    {
        ClearEnvVar(BotGameTypes);

        Assert.Throws<BotException>(() => EnvVars.GetBotInfo());
    }

    [Test]
    public void GetBotInfo_ShouldFailWhenBotGameTypesIsBlank()
    {
        SetEnvVar(BotGameTypes, "  \t");

        Assert.Throws<BotException>(() => EnvVars.GetBotInfo());
    }

    [Test]
    public void GetBotInfo_ShouldReturnLocalCountryCodeIfWhenBotCountryCodesIsInvalid()
    {
        SetEnvVar(BotCountryCodes, "XYZ tew");

        var countryCode = EnvVars.GetBotInfo().CountryCodes.First();
        Assert.That(countryCode, Is.EqualTo(CountryCode.GetLocalCountryCode()));
    }
    
    [Test]
    public void GetBotInfo_InitialPosition_ShouldWorkWithValidInput1() {
        SetEnvVar(BotInitialPosition, "  50 ");
        
        var pos = EnvVars.GetBotInfo().InitialPosition;
        Assert.That(pos.X, Is.EqualTo(50));
        Assert.That(pos.Y, Is.EqualTo(null));
        Assert.That(pos.Angle, Is.EqualTo(null));
    }
    
    [Test]
    public void GetBotInfo_InitialPosition_ShouldWorkWithValidInput2() {
        SetEnvVar(BotInitialPosition, "  50, ");
        
        var pos = EnvVars.GetBotInfo().InitialPosition;
        Assert.That(pos.X, Is.EqualTo(50));
        Assert.That(pos.Y, Is.EqualTo(null));
        Assert.That(pos.Angle, Is.EqualTo(null));
    }

    [Test]
    public void GetBotInfo_InitialPosition_ShouldWorkWithValidInput3() {
        SetEnvVar(BotInitialPosition, "  50 70.0");
        
        var pos = EnvVars.GetBotInfo().InitialPosition;
        Assert.That(pos.X, Is.EqualTo(50));
        Assert.That(pos.Y, Is.EqualTo(70));
        Assert.That(pos.Angle, Is.EqualTo(null));
    }

    [Test]
    public void GetBotInfo_InitialPosition_ShouldWorkWithValidInput4() {
        SetEnvVar(BotInitialPosition, "  50.0, 70");
        
        var pos = EnvVars.GetBotInfo().InitialPosition;
        Assert.That(pos.X, Is.EqualTo(50));
        Assert.That(pos.Y, Is.EqualTo(70));
        Assert.That(pos.Angle, Is.EqualTo(null));
    }

    [Test]
    public void GetBotInfo_InitialPosition_ShouldWorkWithValidInput5() {
        SetEnvVar(BotInitialPosition, "  50, 70.0 ,");
        
        var pos = EnvVars.GetBotInfo().InitialPosition;
        Assert.That(pos.X, Is.EqualTo(50));
        Assert.That(pos.Y, Is.EqualTo(70));
        Assert.That(pos.Angle, Is.EqualTo(null));
    }
    
    [Test]
    public void GetBotInfo_InitialPosition_ShouldWorkWithValidInput6() {
        SetEnvVar(BotInitialPosition, "  50.0, 70, 100");
        
        var pos = EnvVars.GetBotInfo().InitialPosition;
        Assert.That(pos.X, Is.EqualTo(50));
        Assert.That(pos.Y, Is.EqualTo(70));
        Assert.That(pos.Angle, Is.EqualTo(100));
    }
    
    [Test]
    public void GetBotInfo_InitialPosition_ShouldWorkWithValidInput7() {
        SetEnvVar(BotInitialPosition, "  50, 70.0 100");
        
        var pos = EnvVars.GetBotInfo().InitialPosition;
        Assert.That(pos.X, Is.EqualTo(50));
        Assert.That(pos.Y, Is.EqualTo(70));
        Assert.That(pos.Angle, Is.EqualTo(100));
    }

    [Test]
    public void GetBotInfo_InitialPosition_ShouldWorkWithEmptyInput()
    {
        SetEnvVar(BotInitialPosition, "");

        var pos = EnvVars.GetBotInfo().InitialPosition;
        Assert.That(pos, Is.Null);
    }
    
    [Test]
    public void GetBotInfo_InitialPosition_ShouldWorkWithBlankInput()
    {
        SetEnvVar(BotInitialPosition, "  \t");

        var pos = EnvVars.GetBotInfo().InitialPosition;
        Assert.That(pos, Is.Null);
    }
}