﻿using System;
using System.Collections.Generic;
using System.Threading;
using System.Threading.Tasks;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;
using Robocode.TankRoyale.Schema;

namespace Robocode.TankRoyale.BotApi.Tests;

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
        public TestBot() : base(BotInfo, MockedServer.ServerUrl)
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

    protected BaseBot Start()
    {
        var bot = new TestBot();
        StartAsync(bot);
        return bot;
    }

    protected Task StartAsync(BaseBot bot)
    {
        var task = Task.Run(bot.Start);
        _trackedTasks.Add(task);
        return task;
    }

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

    private void AwaitGameStarted(BaseBot bot)
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

    protected T ExecuteCommand<T>(Func<T> command)
    {
        Server.ResetBotIntentEvent();
        var result = command();
        AwaitBotIntent();
        return result;
    }

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
}
