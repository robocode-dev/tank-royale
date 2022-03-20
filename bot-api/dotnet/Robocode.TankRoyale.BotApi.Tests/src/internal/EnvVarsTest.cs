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
        }
    }
}