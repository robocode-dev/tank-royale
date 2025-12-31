using System.Threading.Tasks;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;

namespace Robocode.TankRoyale.BotApi.Tests;

[TestFixture]
[Description("TR-API-CMD-003 Radar/Scan commands")]
public class CommandsRadarTest : AbstractBotTest
{
    [Test]
    [Category("CMD")]
    [Property("ID", "TR-API-CMD-003")]
    public void Test_Rescan_Intent()
    {
        // Arrange
        var bot = Start();
        AwaitBotHandshake();
        AwaitGameStarted(bot);

        // Act
        bot.SetRescan();
        GoAsync(bot);
        AwaitBotIntent();

        // Assert
        var intent = Server.BotIntent;
        Assert.That(intent, Is.Not.Null);
        Assert.That(intent.Rescan, Is.True);
    }

    [Test]
    [Category("CMD")]
    [Property("ID", "TR-API-CMD-003")]
    public void Test_Blocking_Rescan()
    {
        // Arrange
        var bot = new RescanTestBot();
        StartAsync(bot);
        AwaitBotHandshake();
        AwaitGameStarted(bot);

        // Act (rescan is blocking and calls Go() internally)
        var rescanTask = Task.Run(() =>
        {
            try
            {
                bot.Rescan();
            }
            catch (System.Exception ex)
            {
                System.Console.Error.WriteLine(ex);
            }
        });

        // Small delay to ensure rescan task starts
        Thread.Sleep(100);

        AwaitBotIntent();

        // Assert
        var intent = Server.BotIntent;
        Assert.That(intent, Is.Not.Null);
        Assert.That(intent.Rescan, Is.True);

        // Cleanup
        rescanTask.Wait(1000);
    }

    [Test]
    [Category("CMD")]
    [Property("ID", "TR-API-CMD-003")]
    public void Test_Adjust_Radar_Body()
    {
        // Arrange
        var bot = Start();
        AwaitBotHandshake();
        AwaitGameStarted(bot);

        // Act
        bot.AdjustRadarForBodyTurn = true;
        GoAsync(bot);
        AwaitBotIntent();

        // Assert
        var intent = Server.BotIntent;
        Assert.That(intent, Is.Not.Null);
        Assert.That(intent.AdjustRadarForBodyTurn, Is.True);

        // Reset for next step
        Server.ResetBotIntentEvent();

        // Act
        bot.AdjustRadarForBodyTurn = false;
        GoAsync(bot);
        AwaitBotIntent();

        // Assert
        intent = Server.BotIntent;
        Assert.That(intent.AdjustRadarForBodyTurn, Is.False);
    }

    [Test]
    [Category("CMD")]
    [Property("ID", "TR-API-CMD-003")]
    public void Test_Adjust_Radar_Gun()
    {
        // Arrange
        var bot = Start();
        AwaitBotHandshake();
        AwaitGameStarted(bot);

        // Act
        bot.AdjustRadarForGunTurn = true;
        GoAsync(bot);
        AwaitBotIntent();

        // Assert
        var intent = Server.BotIntent;
        Assert.That(intent, Is.Not.Null);
        Assert.That(intent.AdjustRadarForGunTurn, Is.True);
        Assert.That(intent.FireAssist, Is.False);

        // Reset for next step
        Server.ResetBotIntentEvent();

        // Act
        bot.AdjustRadarForGunTurn = false;
        GoAsync(bot);
        AwaitBotIntent();

        // Assert
        intent = Server.BotIntent;
        Assert.That(intent.AdjustRadarForGunTurn, Is.False);
        Assert.That(intent.FireAssist, Is.True);
    }

    private class RescanTestBot : Bot
    {
        public RescanTestBot() : base(BotInfo, MockedServer.ServerUrl)
        {
        }

        public override void Run()
        {
            // Do nothing, we will call Rescan from outside
        }
    }
}

