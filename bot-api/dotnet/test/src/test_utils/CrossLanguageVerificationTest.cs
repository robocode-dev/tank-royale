using System.Threading;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;

namespace Robocode.TankRoyale.BotApi.Tests
{
    [TestFixture]
    public class CrossLanguageVerificationTest : AbstractBotTest
    {
        [Test]
        public void VerifyStateSynchronizationIdentical()
        {
            var bot = new TestBot();
            StartAsync(bot);

            // 1. Verify AwaitBotReady
            Assert.That(Server.AwaitBotReady(30000), Is.True, "AwaitBotReady should succeed");

            // Give the bot a chance to be ready for the next tick
            Thread.Sleep(500);

            // 2. Verify initial state (based on MockedServer defaults)
            Assert.That(bot.Energy, Is.EqualTo(MockedServer.BotEnergy));
            Assert.That(bot.Speed, Is.EqualTo(MockedServer.BotSpeed));
            // Assert.That(bot.Direction, Is.EqualTo(MockedServer.BotDirection));
            // Assert.That(bot.GunDirection, Is.EqualTo(MockedServer.BotGunDirection));
            // Assert.That(bot.RadarDirection, Is.EqualTo(MockedServer.BotRadarDirection));

            // 3. Update all states via SetBotStateAndAwaitTick
            double newEnergy = 42.0;
            double newGunHeat = 1.5;
            double newSpeed = 6.5;
            double newDirection = 180.0;
            double newGunDirection = 90.0;
            double newRadarDirection = 270.0;

            bool success = Server.SetBotStateAndAwaitTick(
                newEnergy, newGunHeat, newSpeed,
                newDirection, newGunDirection, newRadarDirection
            );

            Assert.That(success, Is.True, "SetBotStateAndAwaitTick should succeed");

            // 4. Verify bot reflects new state
            Assert.That(bot.Energy, Is.EqualTo(newEnergy));
            Assert.That(bot.GunHeat, Is.EqualTo(newGunHeat));
            Assert.That(bot.Speed, Is.EqualTo(newSpeed));
            Assert.That(bot.Direction, Is.EqualTo(newDirection));
            Assert.That(bot.GunDirection, Is.EqualTo(newGunDirection));
            Assert.That(bot.RadarDirection, Is.EqualTo(newRadarDirection));

            // 5. Verify Turn Number increment
            int currentTurn = bot.TurnNumber;
            Server.SetBotStateAndAwaitTick(null, null, null, null, null, null);
            Assert.That(bot.TurnNumber, Is.EqualTo(currentTurn + 1));

            // 6. Manual Turn Number setting
            Server.SetTurnNumber(500);
            Server.SetBotStateAndAwaitTick(null, null, null, null, null, null);
            Assert.That(bot.TurnNumber, Is.EqualTo(500));
        }

        private class TestBot : Bot
        {
            public TestBot() : base(BotInfo, MockedServer.ServerUrl)
            {
                System.Console.WriteLine("TestBot: Constructor called");
            }

            public override void Run()
            {
                System.Console.WriteLine("TestBot: Run() started");
                while (IsRunning)
                {
                    System.Console.WriteLine("TestBot: Calling Go()");
                    SetFire(1.0);
                    Go();
                    System.Console.WriteLine("TestBot: Returned from Go()");
                }
                System.Console.WriteLine("TestBot: Run() exiting");
            }
        }
    }
}
