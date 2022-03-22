using System.Collections.Generic;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Internal;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;

namespace Robocode.TankRoyale.BotApi.Tests.Internal
{
    public class EnvVarsTest
    {
        [Test]
        public void GetBotInfo_ShouldWorkWhenAllEnvVarsAreSetCorrectly()
        {
            EnvironmentVariablesUtil.SetAll();
            var info = EnvVars.GetBotInfo();
            Assert.That(info.Name, Is.EqualTo("MyBot"));
            Assert.That(info.Version, Is.EqualTo("1.0"));
            Assert.That(info.Authors, Is.SupersetOf(new [] {"Author1", "Author2"}));
            Assert.That(info.GameTypes, Is.SupersetOf(new [] {"1v1", "classic", "melee"}));
            Assert.That(info.Description, Is.EqualTo("Short description"));
            Assert.That(info.Homepage, Is.EqualTo("https://somewhere.net/MyBot"));
            Assert.That(info.CountryCodes, Is.SupersetOf(new[] {"US", "UK"}));
            Assert.That(info.Platform, Is.EqualTo(".Net 5"));
            Assert.That(info.ProgrammingLang, Is.EqualTo("C# 8"));
            Assert.That(info.InitialPosition, Is.EqualTo(InitialPosition.FromString("50, 70, 270")));
        }
    }
}