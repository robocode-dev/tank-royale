using System.Threading.Tasks;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;

namespace Robocode.TankRoyale.BotApi.Tests
{
    public class MockedServerTest
    {
        private MockedServer _server;

        [SetUp]
        public void SetUp()
        {
            _server = new MockedServer();
            _server.Start();
        }

        [TearDown]
        public void TearDown()
        {
            _server.Stop();
        }

        [Test]
        public void AwaitBotReady_ShouldSucceed()
        {
            var bot = new TestBot();
            Task.Run(bot.Start);

            bool ready = _server.AwaitBotReady(30000);
            Assert.That(ready, Is.True, "Bot should be ready");
        }

        [Test]
        public void SetBotStateAndAwaitTick_ShouldUpdateStateAndReturnTrue()
        {
            var bot = new TestBot();
            Task.Run(bot.Start);

            Assert.That(_server.AwaitBotReady(30000), Is.True, "Bot should be ready");

            // Start the bot's game loop
            Task.Run(bot.Go);

            double newEnergy = 50.0;
            double newSpeed = 4.0;

            bool success = _server.SetBotStateAndAwaitTick(newEnergy, null, newSpeed, null, null, null);

            Assert.That(success, Is.True, "SetBotStateAndAwaitTick should succeed");

            // Wait a bit for the bot to process the tick
            System.Threading.Thread.Sleep(200);

            Assert.That(bot.Energy, Is.EqualTo(newEnergy));
            Assert.That(bot.Speed, Is.EqualTo(newSpeed));
        }

        private class TestBot : BaseBot
        {
            public TestBot() : base(BotInfo.Builder()
                .SetName("TestBot")
                .SetVersion("1.0")
                .AddAuthor("Author")
                .SetDescription("Description")
                .SetHomepage("https://test.com")
                .AddCountryCode("us")
                .AddGameType("classic")
                .SetPlatform(".NET")
                .SetProgrammingLang("C#")
                .Build(), MockedServer.ServerUrl)
            {
            }
        }
    }
}
