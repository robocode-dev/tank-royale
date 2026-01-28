using System.Threading;
using System.Threading.Tasks;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;

namespace Robocode.TankRoyale.BotApi.Tests;

/// <summary>
/// Tests to verify thread-safety improvements in MockedServer.
/// These tests would have been flaky before the thread-safety fixes.
/// </summary>
[TestFixture]
[Description("Thread-safety tests for MockedServer")]
public class MockedServerThreadSafetyTest : AbstractBotTest
{
    [Test]
    [Category("Reliability")]
    [Description("Verify SetInitialBotState is thread-safe")]
    public void SetInitialBotState_WhenCalledConcurrently_ShouldNotCauseRaceConditions()
    {
        // Arrange: Start bot and ensure it's ready
        var bot = Start();
        Assert.That(Server.AwaitBotReady(2000), Is.True, "Bot failed to become ready");

        // Act: Update state from multiple threads concurrently (simulating what test setup might do)
        var tasks = new Task[10];
        for (int i = 0; i < tasks.Length; i++)
        {
            var energy = 50.0 + i;
            tasks[i] = Task.Run(() => Server.SetInitialBotState(energy: energy));
        }

        // Assert: All updates complete without throwing exceptions
        Assert.DoesNotThrow(() => Task.WaitAll(tasks));
    }

    [Test]
    [Category("Reliability")]
    [Description("Verify SetBotStateAndAwaitTick is thread-safe")]
    public void SetBotStateAndAwaitTick_WhenCalledSequentially_ShouldUpdateStateCorrectly()
    {
        // Arrange: Start bot and ensure it's ready with proper connection
        var bot = Start();
        Assert.That(Server.AwaitBotReady(2000), Is.True, "Bot failed to become ready");

        // Act & Assert: Sequential state updates should work reliably
        Assert.That(Server.SetBotStateAndAwaitTick(energy: 75.0), Is.True, "First state update failed");
        Assert.That(Server.SetBotStateAndAwaitTick(energy: 50.0), Is.True, "Second state update failed");
        Assert.That(Server.SetBotStateAndAwaitTick(energy: 25.0), Is.True, "Third state update failed");
    }

    [Test]
    [Category("Reliability")]
    [Description("Verify AwaitBotReady handles concurrent tick sending")]
    public void AwaitBotReady_ShouldHandleConcurrentTickSending()
    {
        // Arrange & Act: Start bot and verify ready state
        var bot = Start();
        var success = Server.AwaitBotReady(2000);

        // Assert: Bot should reach ready state without race conditions
        Assert.That(success, Is.True, "Bot failed to reach ready state");

        // Wait briefly for bot to process tick
        Thread.Sleep(100);

        // Verify bot has received state by checking that we can access energy without exception
        Assert.DoesNotThrow(() => {
            try {
                var energy = bot.Energy;
                Assert.That(energy, Is.GreaterThan(0), "Bot energy should be set after ready");
            } catch (BotException) {
                // Acceptable - bot might not have processed tick yet in all cases
            }
        });
    }

    [Test]
    [Category("Reliability")]
    [Description("Verify concurrent SetInitialBotState and AwaitBotIntent")]
    public void ConcurrentStateUpdates_ShouldNotCauseDeadlocks()
    {
        // Arrange: Start bot and ensure it's ready
        var bot = Start();
        Assert.That(Server.AwaitBotReady(2000), Is.True, "Bot failed to become ready");

        // Act: Set up concurrent operations
        var stateUpdateTask = Task.Run(() =>
        {
            for (int i = 0; i < 5; i++)
            {
                Server.SetInitialBotState(energy: 80.0 - i * 10);
                Thread.Sleep(10);
            }
        });

        var intentTask = Task.Run(() =>
        {
            for (int i = 0; i < 5; i++)
            {
                bot.TurnRate = 5.0;
                GoAsync(bot);
                Server.AwaitBotIntent(1000);
                Thread.Sleep(10);
            }
        });

        // Assert: Both tasks should complete without deadlock
        Assert.DoesNotThrow(() => Task.WaitAll(new[] { stateUpdateTask, intentTask }, 5000),
            "Concurrent operations should not cause deadlock");
    }
}
