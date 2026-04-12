using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;

namespace Robocode.TankRoyale.BotApi.Tests;

[TestFixture]
public class MockedServerEnhancementTest : AbstractBotTest
{
    private class BotWithLoop : Bot
    {
        public BotWithLoop() : base(BotInfo, MockedServer.ServerUrl)
        {
        }

        public override void Run()
        {
            while (IsRunning)
            {
                Go();
            }
        }
    }

    [Test]
    public void AwaitBotReady_ShouldSucceed()
    {
        var bot = Start();
        bool ready = Server.AwaitBotReady(2000);
        Assert.That(ready, Is.True);
    }

    [Test]
    public void SetBotStateAndAwaitTick_ShouldUpdateState()
    {
        var bot = new BotWithLoop();
        StartAsync(bot);

        Assert.That(Server.AwaitBotReady(2000), Is.True);

        AwaitTick(bot);

        // Initial state check
        Assert.That(bot.Energy, Is.EqualTo(MockedServer.BotEnergy));

        // Update state
        double newEnergy = 50.0;
        double newGunHeat = 1.5;
        bool success = Server.SetBotStateAndAwaitTick(energy: newEnergy, gunHeat: newGunHeat);

        Assert.That(success, Is.True);

        // In .NET, AwaitTick(bot) might return the same tick if called too fast because of how ManualResetEvent works.
        // But here we reset it in SetBotStateAndAwaitTick, so it SHOULD be the new tick.
        AwaitTick(bot);

        Assert.That(bot.Energy, Is.EqualTo(newEnergy).Within(0.01));
        Assert.That(bot.GunHeat, Is.EqualTo(newGunHeat).Within(0.01));
    }
}
