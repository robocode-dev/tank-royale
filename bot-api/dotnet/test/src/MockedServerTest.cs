using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests;
using System;

namespace Robocode.TankRoyale.BotApi.Tests
{
    [TestFixture]
    public class MockedServerTest : AbstractBotTest
    {
        [Test]
        public void TestAwaitBotReady()
        {
            var bot = Start();
            Assert.That(Server.AwaitBotReady(1000), Is.True);
        }

        [Test]
        public void TestSetBotStateAndAwaitTick()
        {
            var bot = StartAndAwaitGameStarted();

            double newEnergy = 42.5;
            double newGunHeat = 0.33;

            bool ok = Server.SetBotStateAndAwaitTick(energy: newEnergy, gunHeat: newGunHeat);
            Assert.That(ok, Is.True);

            // wait until bot reflects the updated state
            bool reflected = AwaitCondition(() => Math.Abs(bot.Energy - newEnergy) < 1e-6, 1000);
            Assert.That(reflected, Is.True);

            bool reflectedGunHeat = AwaitCondition(() => Math.Abs(bot.GunHeat - newGunHeat) < 1e-6, 1000);
            Assert.That(reflectedGunHeat, Is.True);
        }

        private bool AwaitCondition(Func<bool> condition, int milliSeconds)
        {
            var startTime = DateTimeOffset.Now.ToUnixTimeMilliseconds();
            do
            {
                try
                {
                    if (condition())
                    {
                        return true;
                    }
                }
                catch (BotException)
                {
                }
                System.Threading.Thread.Yield();
            } while (DateTimeOffset.Now.ToUnixTimeMilliseconds() - startTime < milliSeconds);
            return false;
        }
    }
}
