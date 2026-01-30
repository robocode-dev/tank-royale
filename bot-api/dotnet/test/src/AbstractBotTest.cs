using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi.Tests;

/// <summary>
/// Abstract base class for bot API tests.
/// Provides common test infrastructure including MockedServer lifecycle management,
/// bot task tracking, and command execution utilities.
///
/// All tests inheriting from this class have a global timeout of 10 seconds
/// to prevent hangs.
/// </summary>
[Timeout(10000)] // 10 seconds timeout for all tests
public class AbstractBotTest
{
    protected MockedServer Server;
    private readonly List<Task> _trackedTasks = new();

    protected static readonly BotInfo BotInfo = BotInfo.Builder()
        .SetName("TestBot")
        .SetVersion("1.0")
        .AddAuthor("Author 1")
        .AddAuthor("Author 2")
        .SetDescription("Short description")
        .SetHomepage("https://testbot.robocode.dev")
        .AddCountryCode("gb")
        .AddCountryCode("us")
        .AddGameType("classic")
        .AddGameType("1v1")
        .AddGameType("melee")
        .SetPlatform(".NET 6")
        .SetProgrammingLang("C# 10")
        .SetInitialPosition(InitialPosition.FromString("10, 20, 30"))
        .Build();

    private class TestBot : BaseBot {
        public TestBot(Uri serverUrl) : base(BotInfo, serverUrl)
        {
        }
    }

    [SetUp]
    public void SetUp()
    {
        Server = new MockedServer();
        Server.Start();
    }

    [TearDown]
    public void Teardown()
    {
        // Stop server first to trigger bot task shutdown
        Server.Stop();

        // Wait for tracked tasks to complete with timeout
        if (_trackedTasks.Count > 0)
        {
            try
            {
                Task.WaitAll(_trackedTasks.ToArray(), TimeSpan.FromSeconds(2));
            }
            catch (AggregateException)
            {
                // Tasks didn't complete cleanly - acceptable in test teardown
            }
            _trackedTasks.Clear();
        }
    }

    /// <summary>
    /// Create and start a test bot asynchronously.
    /// The bot task is automatically tracked for clean shutdown.
    /// </summary>
    /// <returns>The started bot instance</returns>
    protected BaseBot Start()
    {
        var bot = new TestBot(Server.ServerUrl);
        StartAsync(bot);
        return bot;
    }

    /// <summary>
    /// Start a bot asynchronously in a tracked task.
    /// The task is registered for cleanup during teardown.
    /// </summary>
    /// <param name="bot">The bot to start</param>
    /// <returns>The task running the bot</returns>
    protected Task StartAsync(BaseBot bot)
    {
        var task = Task.Run(bot.Start);
        _trackedTasks.Add(task);
        return task;
    }

    /// <summary>
    /// Execute bot.Go() asynchronously in a tracked task.
    /// The task is registered for cleanup during teardown.
    /// </summary>
    /// <param name="bot">The bot to run</param>
    protected void GoAsync(BaseBot bot)
    {
        var task = Task.Run(bot.Go);
        _trackedTasks.Add(task);
    }

    protected BaseBot StartAndAwaitHandshake()
    {
        var bot = Start();
        AwaitBotHandshake();
        return bot;
    }

    protected BaseBot StartAndAwaitTick()
    {
        var bot = Start();
        AwaitTick(bot);
        return bot;
    }

    protected BaseBot StartAndAwaitGameStarted()
    {
        var bot = Start();
        AwaitGameStarted(bot);
        return bot;
    }

    protected void AwaitBotHandshake()
    {
        Assert.That(Server.AwaitBotHandshake(1000), Is.True);
    }

    protected void AwaitGameStarted(BaseBot bot)
    {
        Assert.That(Server.AwaitGameStarted(1000), Is.True);

        var startMillis = DateTimeOffset.Now.ToUnixTimeMilliseconds();
        var noException = false;
        do {
            try
            {
                var gameType = bot.GameType;
                noException = true;
            } catch (BotException) {
                Thread.Yield();
            }
        } while (!noException && DateTimeOffset.Now.ToUnixTimeMilliseconds() - startMillis < 1000);
    }

    protected void AwaitTick(BaseBot bot)
    {
        Assert.That(Server.AwaitTick(1000), Is.True);

        var startMillis = DateTimeOffset.Now.ToUnixTimeMilliseconds();
        var noException = false;
        do {
            try
            {
                var energy = bot.Energy;
                noException = true;
            } catch (BotException) {
                Thread.Yield();
            }
        } while (!noException && DateTimeOffset.Now.ToUnixTimeMilliseconds() - startMillis < 1000);
    }

    protected void AwaitBotIntent()
    {
        Assert.That(Server.AwaitBotIntent(1000), Is.True);
    }

    /// <summary>
    /// Execute a command and wait for the bot to send its intent to the server.
    /// This is useful for testing non-blocking commands that immediately return.
    /// </summary>
    /// <typeparam name="T">The return type of the command</typeparam>
    /// <param name="command">The command to execute</param>
    /// <returns>The result of the command</returns>
    protected T ExecuteCommand<T>(Func<T> command)
    {
        Server.ResetBotIntentEvent();
        var result = command();
        AwaitBotIntent();
        return result;
    }

    /// <summary>
    /// Execute a blocking action and wait for the bot to send its intent to the server.
    /// This is useful for testing blocking commands like Go().
    /// </summary>
    /// <param name="action">The action to execute</param>
    protected void ExecuteBlocking(Action action)
    {
        Server.ResetBotIntentEvent();
        action();
        AwaitBotIntent();
    }

    /// <summary>
    /// Execute a command and capture both the result and the bot intent sent to the server.
    /// This is useful for verifying that commands produce the expected intent values.
    /// </summary>
    /// <typeparam name="T">The return type of the command</typeparam>
    /// <param name="command">The command to execute</param>
    /// <returns>A CommandResult containing both the command result and captured intent</returns>
    protected CommandResult<T> ExecuteCommandAndGetIntent<T>(Func<T> command)
    {
        Server.ResetBotIntentEvent();
        var result = command();
        AwaitBotIntent();
        return new CommandResult<T>(result, Server.BotIntent);
    }

    /// <summary>
    /// Wrapper class that holds both a command's return value and the captured bot intent.
    /// </summary>
    protected class CommandResult<T>
    {
        public T Result { get; }
        public BotIntent Intent { get; }

        public CommandResult(T result, BotIntent intent)
        {
            Result = result;
            Intent = intent;
        }
    }

    protected static bool ExceptionContainsEnvVarName(BotException botException, string envVarName) =>
        botException != null && botException.Message.ToUpper().Contains(envVarName);

    /// <summary>
    /// Wait for a condition to become true, polling with a timeout.
    /// </summary>
    /// <param name="condition">The condition to check</param>
    /// <param name="milliSeconds">Timeout in milliseconds</param>
    /// <returns>True if condition became true, false if timeout</returns>
    protected bool AwaitCondition(Func<bool> condition, int milliSeconds)
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
                // Ignore exceptions during polling
            }
            Thread.Yield();
        } while (DateTimeOffset.Now.ToUnixTimeMilliseconds() - startTime < milliSeconds);
        return false;
    }

    /// <summary>
    /// Start bot, wait for game started, and set gun heat to 0 for fire tests.
    /// This convenience method prepares the bot to be able to fire immediately.
    /// </summary>
    /// <returns>The started bot with gun heat at 0</returns>
    protected BaseBot StartAndPrepareForFire()
    {
        var bot = Start();
        AwaitGameStarted(bot);
        // Set gun heat to 0 and energy to 100 so bot can fire immediately
        // Use SetBotStateAndAwaitTick to actually send the state to the bot
        bool tickSent = Server.SetBotStateAndAwaitTick(100.0, 0.0, null, null, null, null);
        Assert.That(tickSent, Is.True, "SetBotStateAndAwaitTick should send tick");
        // Wait for bot to update its internal state by polling until energy matches
        bool stateUpdated = AwaitCondition(() => bot.Energy == 100.0 && bot.GunHeat == 0.0, 2000);
        Assert.That(stateUpdated, Is.True, $"Bot state should update to energy=100, gunHeat=0 (actual: energy={bot.Energy}, gunHeat={bot.GunHeat})");
        return bot;
    }
}
