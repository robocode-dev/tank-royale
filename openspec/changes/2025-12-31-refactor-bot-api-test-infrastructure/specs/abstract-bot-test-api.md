# AbstractBotTest API Specification

## Overview

AbstractBotTest is a base class that provides common utilities for testing Bot API behavior with MockedServer. It
handles lifecycle management, bot instantiation, and provides helper methods for executing commands and capturing
intents.

## Language Implementations

- Java: `bot-api/java/src/test/java/test_utils/AbstractBotTest.java` (new)
- .NET: `bot-api/dotnet/test/src/AbstractBotTest.cs` (enhanced)
- Python: `bot-api/python/tests/test_utils/abstract_bot_test.py` (new)

## Design Principles

1. **Hide complexity**: Tests should not deal with threads, latches, or timing
2. **Guarantee state**: When a command executes, bot state is guaranteed to be as configured
3. **Clear semantics**: One method does one thing; no surprises
4. **Language idioms**: Follow each language's conventions while maintaining semantic equivalence

## API Overview

### Lifecycle Management

```
setUp() / setup_method()
  - Create and start MockedServer
  - Initialize bot to null
  - Called before each test
  
tearDown() / teardown_method()
  - Close connections
  - Stop MockedServer
  - Called after each test
```

### Bot Creation

```
startBot() : BaseBot
  - Create test bot (via createTestBot())
  - Start bot asynchronously
  - Wait for bot to be ready
  - Return bot instance
  
createTestBot() : BaseBot
  - Abstract method
  - Subclass must implement
  - Returns a bot instance configured to connect to MockedServer
```

### Command Execution

```
executeCommand<T>(command: () -> T) : (T, BotIntent)
  - Execute a command that returns a value
  - Automatically call bot.go()
  - Wait for and capture intent
  - Return (command result, captured intent)
  
executeAndCaptureIntent(command: () -> void) : BotIntent
  - Execute a void command
  - Automatically call bot.go()
  - Wait for and capture intent
  - Return captured intent
  
executeBlocking(command: () -> void) : BotIntent
  - Execute a blocking command (fire, rescan, etc.)
  - Run command in background thread
  - Wait for and capture intent
  - Return captured intent
```

### Utilities

```
awaitBotIntent() : void
  - Wait for bot intent with timeout
  - Assert if timeout occurs
```

## Detailed API Specifications

### Java Implementation

```java
package test_utils;

import dev.robocode.tankroyale.botapi.BaseBot;
import dev.robocode.tankroyale.botapi.BotInfo;
import dev.robocode.tankroyale.schema.BotIntent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract base class for Bot API tests using MockedServer.
 * 
 * <p>Provides:
 * <ul>
 * <li>Automatic MockedServer lifecycle management</li>
 * <li>Bot creation and startup utilities</li>
 * <li>Command execution helpers that handle async coordination</li>
 * <li>Intent capture utilities</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>
 * class MyBotTest extends AbstractBotTest {
 *     {@literal @}Override
 *     protected BaseBot createTestBot() {
 *         return new MyBot(BotInfo.builder()...build(), server.getServerUrl());
 *     }
 *     
 *     {@literal @}Test
 *     void testSomething() {
 *         BaseBot bot = startBot();
 *         var result = executeCommand(() -> bot.setFire(1.0));
 *         assertTrue(result.result);
 *     }
 * }
 * </pre>
 */
public abstract class AbstractBotTest {
    protected MockedServer server;
    protected BaseBot bot;
    
    /**
     * Standard JUnit 5 setup. Creates and starts MockedServer.
     */
    @BeforeEach
    public void setUp() {
        server = new MockedServer();
        server.start();
    }
    
    /**
     * Standard JUnit 5 teardown. Stops MockedServer and closes connections.
     */
    @AfterEach
    public void tearDown() {
        if (bot != null) {
            server.closeConnections();
        }
        server.stop();
    }
    
    /**
     * Start a test bot and wait for it to be ready.
     * 
     * <p>This method:
     * <ol>
     * <li>Calls createTestBot() to get a bot instance</li>
     * <li>Starts the bot asynchronously</li>
     * <li>Waits for the bot to complete handshake and receive first tick</li>
     * <li>Returns the bot, ready for commands</li>
     * </ol>
     * 
     * @return The started bot
     * @throws AssertionError if bot fails to start within timeout
     */
    protected BaseBot startBot() {
        bot = createTestBot();
        CompletableFuture.runAsync(bot::start);
        assertTrue(server.awaitBotReady(2000), "Bot failed to become ready");
        return bot;
    }
    
    /**
     * Create a test bot instance.
     * 
     * <p>Subclasses must implement this to provide a bot configured to
     * connect to the MockedServer.
     * 
     * <p>Example:
     * <pre>
     * {@literal @}Override
     * protected BaseBot createTestBot() {
     *     return new SimpleBot(
     *         BotInfo.builder()
     *             .setName("TestBot")
     *             .setVersion("1.0")
     *             .addAuthor("Test")
     *             .build(),
     *         server.getServerUrl()
     *     );
     * }
     * </pre>
     * 
     * @return A bot instance
     */
    protected abstract BaseBot createTestBot();
    
    /**
     * Wait for bot intent to be captured by the server.
     * 
     * @throws AssertionError if intent not received within timeout
     */
    protected void awaitBotIntent() {
        assertTrue(server.awaitBotIntent(1000), "Bot intent not received");
    }
    
    /**
     * Execute a command that returns a value and capture the resulting intent.
     * 
     * <p>This method handles all the async coordination:
     * <ol>
     * <li>Resets the intent latch</li>
     * <li>Executes the command</li>
     * <li>Calls bot.go() to submit the intent</li>
     * <li>Waits for the intent to be captured</li>
     * <li>Returns both the command result and the captured intent</li>
     * </ol>
     * 
     * <p>Example:
     * <pre>
     * var result = executeCommand(() -> bot.setFire(1.0));
     * assertTrue(result.result, "setFire should return true");
     * assertEquals(1.0, result.intent.getFirepower(), "Firepower should be set");
     * </pre>
     * 
     * @param command A supplier that executes the command and returns a result
     * @param <T> The return type of the command
     * @return A CommandResult containing both the command result and captured intent
     */
    protected <T> CommandResult<T> executeCommand(Supplier<T> command) {
        server.resetBotIntentLatch();
        T result = command.get();
        CompletableFuture.runAsync(bot::go);
        awaitBotIntent();
        return new CommandResult<>(result, server.getBotIntent());
    }
    
    /**
     * Execute a void command and capture the resulting intent.
     * 
     * <p>Similar to executeCommand but for commands that don't return a value.
     * 
     * <p>Example:
     * <pre>
     * BotIntent intent = executeAndCaptureIntent(() -> bot.setRescan());
     * assertTrue(intent.getRescan(), "Rescan should be set");
     * </pre>
     * 
     * @param command A runnable that executes the command
     * @return The captured intent
     */
    protected BotIntent executeAndCaptureIntent(Runnable command) {
        server.resetBotIntentLatch();
        command.run();
        CompletableFuture.runAsync(bot::go);
        awaitBotIntent();
        return server.getBotIntent();
    }
    
    /**
     * Execute a blocking command and capture the resulting intent.
     * 
     * <p>Use this for commands like fire() and rescan() that block and call
     * bot.go() internally.
     * 
     * <p>This method:
     * <ol>
     * <li>Resets the intent latch</li>
     * <li>Starts the command in a background thread</li>
     * <li>Waits briefly for the command to start</li>
     * <li>Waits for the intent to be captured</li>
     * <li>Returns the captured intent</li>
     * </ol>
     * 
     * <p>Example:
     * <pre>
     * BotIntent intent = executeBlocking(() -> bot.fire(1.0));
     * assertNotNull(intent.getFirepower(), "Fire should set firepower");
     * </pre>
     * 
     * @param blockingCommand A runnable that executes the blocking command
     * @return The captured intent
     */
    protected BotIntent executeBlocking(Runnable blockingCommand) {
        server.resetBotIntentLatch();
        CompletableFuture.runAsync(blockingCommand);
        try {
            Thread.sleep(50); // Small delay for command to start
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        awaitBotIntent();
        return server.getBotIntent();
    }
    
    /**
     * Simple wrapper for command result + captured intent.
     * 
     * @param <T> The type of the command result
     */
    public static class CommandResult<T> {
        /** The result returned by the command */
        public final T result;
        
        /** The intent captured after the command executed */
        public final BotIntent intent;
        
        /**
         * Create a command result.
         * 
         * @param result The command result
         * @param intent The captured intent
         */
        public CommandResult(T result, BotIntent intent) {
            this.result = result;
            this.intent = intent;
        }
    }
}
```

### .NET Implementation

```csharp
using System;
using System.Threading;
using System.Threading.Tasks;
using NUnit.Framework;
using Robocode.TankRoyale.BotApi;
using Robocode.TankRoyale.BotApi.Tests.Test_utils;

namespace Robocode.TankRoyale.BotApi.Tests;

/// <summary>
/// Abstract base class for Bot API tests using MockedServer.
/// </summary>
/// <remarks>
/// Provides:
/// <list type="bullet">
/// <item>Automatic MockedServer lifecycle management</item>
/// <item>Bot creation and startup utilities</item>
/// <item>Command execution helpers that handle async coordination</item>
/// <item>Intent capture utilities</item>
/// </list>
/// </remarks>
public abstract class AbstractBotTest
{
    protected MockedServer Server;
    protected BaseBot Bot;
    
    protected static readonly BotInfo TestBotInfo = BotInfo.Builder()
        .SetName("TestBot")
        .SetVersion("1.0")
        .AddAuthor("Test")
        .Build();
    
    /// <summary>
    /// Setup before each test. Creates and starts MockedServer.
    /// </summary>
    [SetUp]
    public void SetUp()
    {
        Server = new MockedServer();
        Server.Start();
    }
    
    /// <summary>
    /// Teardown after each test. Stops MockedServer.
    /// </summary>
    [TearDown]
    public void TearDown()
    {
        if (Bot != null)
        {
            Server.CloseConnections();
        }
        Server.Stop();
    }
    
    /// <summary>
    /// Start a test bot and wait for it to be ready.
    /// </summary>
    protected BaseBot StartBot()
    {
        Bot = CreateTestBot();
        StartAsync(Bot);
        Assert.That(Server.AwaitBotReady(2000), Is.True, "Bot failed to become ready");
        return Bot;
    }
    
    /// <summary>
    /// Create a test bot instance. Subclasses must override.
    /// </summary>
    protected abstract BaseBot CreateTestBot();
    
    /// <summary>
    /// Start bot asynchronously.
    /// </summary>
    protected static void StartAsync(BaseBot bot) => Task.Run(bot.Start);
    
    /// <summary>
    /// Call bot.Go() asynchronously.
    /// </summary>
    protected static void GoAsync(BaseBot bot) => Task.Run(bot.Go);
    
    /// <summary>
    /// Wait for bot intent to be captured.
    /// </summary>
    protected void AwaitBotIntent()
    {
        Assert.That(Server.AwaitBotIntent(1000), Is.True, "Bot intent not received");
    }
    
    /// <summary>
    /// Execute a command that returns a value and capture the resulting intent.
    /// </summary>
    /// <typeparam name="T">The return type of the command</typeparam>
    /// <param name="command">The command to execute</param>
    /// <returns>Tuple of (command result, captured intent)</returns>
    /// <example>
    /// <code>
    /// var (fired, intent) = ExecuteCommand(() => bot.SetFire(1.0));
    /// Assert.That(fired, Is.True);
    /// Assert.That(intent.Firepower, Is.EqualTo(1.0));
    /// </code>
    /// </example>
    protected (T result, BotIntent intent) ExecuteCommand<T>(Func<T> command)
    {
        Server.ResetBotIntentEvent();
        var result = command();
        GoAsync(Bot);
        AwaitBotIntent();
        return (result, Server.BotIntent);
    }
    
    /// <summary>
    /// Execute a void command and capture the resulting intent.
    /// </summary>
    /// <param name="command">The command to execute</param>
    /// <returns>The captured intent</returns>
    protected BotIntent ExecuteAndCaptureIntent(Action command)
    {
        Server.ResetBotIntentEvent();
        command();
        GoAsync(Bot);
        AwaitBotIntent();
        return Server.BotIntent;
    }
    
    /// <summary>
    /// Execute a blocking command (like Fire or Rescan) and capture intent.
    /// </summary>
    /// <param name="blockingCommand">The blocking command to execute</param>
    /// <returns>The captured intent</returns>
    /// <example>
    /// <code>
    /// var intent = ExecuteBlocking(() => bot.Fire(1.0));
    /// Assert.That(intent.Firepower, Is.Not.Null);
    /// </code>
    /// </example>
    protected BotIntent ExecuteBlocking(Action blockingCommand)
    {
        Server.ResetBotIntentEvent();
        var task = Task.Run(blockingCommand);
        Thread.Sleep(50); // Small delay for command to start
        AwaitBotIntent();
        return Server.BotIntent;
    }
}
```

### Python Implementation

```python
"""Base class for Bot API tests using MockedServer."""

import threading
import time
from typing import Any, Callable, Optional, Tuple

from robocode_tank_royale.bot_api import BaseBot, BotInfo
from test_utils.mocked_server import MockedServer


class AbstractBotTest:
    """
    Abstract base class for Bot API tests using MockedServer.
    
    Provides:
    - Automatic MockedServer lifecycle management
    - Bot creation and startup utilities
    - Command execution helpers that handle async coordination
    - Intent capture utilities
    
    Usage:
        class MyBotTest(AbstractBotTest):
            def create_test_bot(self) -> BaseBot:
                return MyBot(self.create_bot_info(), self.server.server_url)
            
            def test_something(self):
                bot = self.start_bot()
                fired, intent = self.execute_command(lambda: bot.set_fire(1.0))
                assert fired
                assert intent.firepower == 1.0
    """
    
    def setup_method(self):
        """Setup before each test method. Creates and starts MockedServer."""
        self.server = MockedServer()
        self.server.start()
        self.bot: Optional[BaseBot] = None
    
    def teardown_method(self):
        """Teardown after each test method. Stops MockedServer."""
        if self.bot:
            self.server.close_connections()
        self.server.stop()
    
    def start_bot(self) -> BaseBot:
        """
        Start a test bot and wait for it to be ready.
        
        Returns:
            The started bot, ready for commands
            
        Raises:
            AssertionError: If bot fails to start within timeout
        """
        self.bot = self.create_test_bot()
        self._start_async(self.bot)
        assert self.server.await_bot_ready(2000), "Bot failed to become ready"
        return self.bot
    
    def create_test_bot(self) -> BaseBot:
        """
        Create a test bot instance. Subclasses must override.
        
        Example:
            def create_test_bot(self) -> BaseBot:
                return SimpleBot(self.create_bot_info(), self.server.server_url)
        
        Returns:
            A bot instance configured to connect to MockedServer
        """
        raise NotImplementedError("Subclass must implement create_test_bot()")
    
    def create_bot_info(self) -> BotInfo:
        """
        Create a default BotInfo for testing.
        
        Override to customize bot info.
        """
        return BotInfo(
            name="TestBot",
            version="1.0",
            authors=["Test"],
            description="Test bot",
            homepage="",
            country_codes=[],
            game_types=["classic"],
            platform="Python",
            programming_lang="Python 3.x",
            initial_position=None
        )
    
    def _start_async(self, bot: BaseBot):
        """Start bot in background thread."""
        thread = threading.Thread(target=bot.start, daemon=True)
        thread.start()
    
    def _go_async(self, bot: BaseBot):
        """Call bot.go() in background thread."""
        thread = threading.Thread(target=bot.go, daemon=True)
        thread.start()
    
    def await_bot_intent(self):
        """
        Wait for bot intent to be captured by the server.
        
        Raises:
            AssertionError: If intent not received within timeout
        """
        assert self.server.await_bot_intent(1000), "Bot intent not received"
    
    def execute_command(self, command_fn: Callable[[], Any]) -> Tuple[Any, Any]:
        """
        Execute a command that returns a value and capture the resulting intent.
        
        This method handles all the async coordination:
        1. Resets the intent event
        2. Executes the command
        3. Calls bot.go() to submit the intent
        4. Waits for the intent to be captured
        5. Returns both the command result and the captured intent
        
        Args:
            command_fn: A callable that executes the command and returns a result
            
        Returns:
            Tuple of (command_result, captured_intent)
            
        Example:
            fired, intent = self.execute_command(lambda: bot.set_fire(1.0))
            assert fired
            assert intent.firepower == 1.0
        """
        self.server.reset_bot_intent_event()
        result = command_fn()
        self._go_async(self.bot)
        self.await_bot_intent()
        return result, self.server._bot_intent
    
    def execute_and_capture_intent(self, command_fn: Callable[[], None]) -> Any:
        """
        Execute a void command and capture the resulting intent.
        
        Args:
            command_fn: A callable that executes the command
            
        Returns:
            The captured bot_intent
            
        Example:
            intent = self.execute_and_capture_intent(lambda: bot.set_rescan())
            assert intent.rescan is True
        """
        self.server.reset_bot_intent_event()
        command_fn()
        self._go_async(self.bot)
        self.await_bot_intent()
        return self.server._bot_intent
    
    def execute_blocking(self, blocking_command: Callable[[], None]) -> Any:
        """
        Execute a blocking command (like fire() or rescan()) and capture intent.
        
        Use this for commands that block and call bot.go() internally.
        
        Args:
            blocking_command: A callable that executes the blocking command
            
        Returns:
            The captured bot_intent
            
        Example:
            intent = self.execute_blocking(lambda: bot.fire(1.0))
            assert intent.firepower is not None
        """
        self.server.reset_bot_intent_event()
        thread = threading.Thread(target=blocking_command, daemon=True)
        thread.start()
        time.sleep(0.05)  # Small delay for command to start
        self.await_bot_intent()
        return self.server._bot_intent
```

## Usage Patterns

### Pattern 1: Simple Command Test

```java
@Test
void testSimpleCommand() {
    BaseBot bot = startBot();
    
    var result = executeCommand(() -> bot.setTurnRate(10.0));
    
    assertEquals(10.0, result.intent.getTurnRate());
}
```

### Pattern 2: Command with State Setup

```java
@Test
void testCommandWithState() {
    BaseBot bot = startBot();
    
    // Set specific state
    server.setBotStateAndAwaitTick(5.0, 10.0, null, null, null, null);
    
    // Execute command
    var result = executeCommand(() -> bot.setFire(1.0));
    
    // Assert on result
    assertFalse(result.result); // Can't fire with hot gun
}
```

### Pattern 3: Blocking Command Test

```java
@Test
void testBlockingCommand() {
    BaseBot bot = startBot();
    
    BotIntent intent = executeBlocking(() -> bot.rescan());
    
    assertTrue(intent.getRescan());
}
```

### Pattern 4: Multiple Commands in Sequence

```java
@Test
void testCommandSequence() {
    BaseBot bot = startBot();
    
    // First command
    var result1 = executeCommand(() -> bot.setTurnRate(10.0));
    assertEquals(10.0, result1.intent.getTurnRate());
    
    // Update state
    server.setBotStateAndAwaitTick(100.0, 0.0, null, null, null, null);
    
    // Second command
    var result2 = executeCommand(() -> bot.setFire(2.0));
    assertTrue(result2.result);
    assertEquals(2.0, result2.intent.getFirepower());
}
```

## Best Practices

1. **Always use startBot()**: Don't create bots manually
2. **Always use execute methods**: Don't call bot.go() directly in tests
3. **Set state before commands**: Use `setBotStateAndAwaitTick()` to guarantee state
4. **Check both result and intent**: Verify command returned expected value AND intent is correct
5. **Use meaningful assertions**: Don't just check not-null; verify exact values
6. **One concept per test**: Test one thing at a time
7. **Clean test names**: Use descriptive names that explain what's being tested

## Common Pitfalls

### ❌ Don't do this:

```java
// BAD: Manual thread coordination
BaseBot bot = createTestBot();
Thread t = new Thread(bot::start);
t.

start();
Thread.

sleep(1000); // Hope it's ready
bot.

setFire(1.0);
bot.

go();
Thread.

sleep(500); // Hope intent arrives

BotIntent intent = server.getBotIntent();
```

### ✅ Do this instead:

```java
// GOOD: Use helper methods
BaseBot bot = startBot();
var result = executeCommand(() -> bot.setFire(1.0));

assertTrue(result.result);

assertEquals(1.0,result.intent.getFirepower());
```

### ❌ Don't do this:

```java
// BAD: Assume state without setting it
BaseBot bot = startBot();
boolean fired = bot.setFire(3.0);
// What is the bot's energy? Who knows!
```

### ✅ Do this instead:

```java
// GOOD: Explicitly set and verify state
BaseBot bot = startBot();
server.

setBotStateAndAwaitTick(100.0,0.0,null,null,null,null);

var result = executeCommand(() -> bot.setFire(3.0));

assertTrue(result.result); // We know energy=100, so this should succeed
```

## Thread Safety Notes

- **Java**: Uses CompletableFuture for async operations; thread-safe
- **.NET**: Uses Task for async operations; thread-safe
- **Python**: Uses threading.Thread; thread-safe with proper locking in MockedServer

Tests should not need to worry about thread safety when using the provided helper methods.

## Test Bot Builder/Factory

### Overview

The TestBotBuilder provides a fluent API for creating configurable test bots without boilerplate. This eliminates the
need to create inner classes for each test scenario.

### Java Implementation

```java
package test_utils;

import dev.robocode.tankroyale.botapi.*;
import dev.robocode.tankroyale.botapi.events.*;

import java.net.URI;
import java.util.function.Consumer;

/**
 * Builder for creating configurable test bots.
 *
 * <p>Usage:
 * <pre>
 * BaseBot bot = TestBotBuilder.create(server)
 *     .withName("TestBot")
 *     .onRun(() -> { /* custom run logic */ })
    *.onScannedBot(e ->{ /* handle scan */ })
    *.

build();
 * </pre>
    */

public class TestBotBuilder {
    private final MockedServer server;
    private String name = "TestBot";
    private String version = "1.0";
    private Runnable onRun = () -> {
    };
    private Consumer<ScannedBotEvent> onScannedBot = e -> {
    };
    private Consumer<HitByBulletEvent> onHitByBullet = e -> {
    };
    private Consumer<BulletHitBotEvent> onBulletHit = e -> {
    };
    private Consumer<DeathEvent> onDeath = e -> {
    };

    private TestBotBuilder(MockedServer server) {
        this.server = server;
    }

    public static TestBotBuilder create(MockedServer server) {
        return new TestBotBuilder(server);
    }

    public TestBotBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public TestBotBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public TestBotBuilder onRun(Runnable handler) {
        this.onRun = handler;
        return this;
    }

    public TestBotBuilder onScannedBot(Consumer<ScannedBotEvent> handler) {
        this.onScannedBot = handler;
        return this;
    }

    public TestBotBuilder onHitByBullet(Consumer<HitByBulletEvent> handler) {
        this.onHitByBullet = handler;
        return this;
    }

    public TestBotBuilder onBulletHit(Consumer<BulletHitBotEvent> handler) {
        this.onBulletHit = handler;
        return this;
    }

    public TestBotBuilder onDeath(Consumer<DeathEvent> handler) {
        this.onDeath = handler;
        return this;
    }

    public BaseBot build() {
        var botInfo = BotInfo.builder()
            .setName(name)
            .setVersion(version)
            .addAuthor("Test")
            .build();
        return new ConfigurableTestBot(botInfo, server.getServerUrl(),
            onRun, onScannedBot, onHitByBullet, onBulletHit, onDeath);
    }

    private static class ConfigurableTestBot extends BaseBot {
        private final Runnable onRunHandler;
        private final Consumer<ScannedBotEvent> onScannedBotHandler;
        private final Consumer<HitByBulletEvent> onHitByBulletHandler;
        private final Consumer<BulletHitBotEvent> onBulletHitHandler;
        private final Consumer<DeathEvent> onDeathHandler;

        ConfigurableTestBot(BotInfo botInfo, URI serverUrl,
                            Runnable onRun,
                            Consumer<ScannedBotEvent> onScannedBot,
                            Consumer<HitByBulletEvent> onHitByBullet,
                            Consumer<BulletHitBotEvent> onBulletHit,
                            Consumer<DeathEvent> onDeath) {
            super(botInfo, serverUrl);
            this.onRunHandler = onRun;
            this.onScannedBotHandler = onScannedBot;
            this.onHitByBulletHandler = onHitByBullet;
            this.onBulletHitHandler = onBulletHit;
            this.onDeathHandler = onDeath;
        }

        @Override
        public void run() {
            onRunHandler.run();
        }

        @Override
        public void onScannedBot(ScannedBotEvent e) {
            onScannedBotHandler.accept(e);
        }

        @Override
        public void onHitByBullet(HitByBulletEvent e) {
            onHitByBulletHandler.accept(e);
        }

        @Override
        public void onBulletHitBot(BulletHitBotEvent e) {
            onBulletHitHandler.accept(e);
        }

        @Override
        public void onDeath(DeathEvent e) {
            onDeathHandler.accept(e);
        }
    }
}
```

### .NET Implementation

```csharp
public class TestBotBuilder
{
    private readonly MockedServer _server;
    private string _name = "TestBot";
    private string _version = "1.0";
    private Action _onRun = () => {};
    private Action<ScannedBotEvent> _onScannedBot = _ => {};
    private Action<HitByBulletEvent> _onHitByBullet = _ => {};
    private Action<BulletHitBotEvent> _onBulletHit = _ => {};
    private Action<DeathEvent> _onDeath = _ => {};
    
    private TestBotBuilder(MockedServer server) { _server = server; }
    
    public static TestBotBuilder Create(MockedServer server) => new(server);
    
    public TestBotBuilder WithName(string name) { _name = name; return this; }
    public TestBotBuilder WithVersion(string version) { _version = version; return this; }
    public TestBotBuilder OnRun(Action handler) { _onRun = handler; return this; }
    public TestBotBuilder OnScannedBot(Action<ScannedBotEvent> handler) { _onScannedBot = handler; return this; }
    public TestBotBuilder OnHitByBullet(Action<HitByBulletEvent> handler) { _onHitByBullet = handler; return this; }
    public TestBotBuilder OnBulletHit(Action<BulletHitBotEvent> handler) { _onBulletHit = handler; return this; }
    public TestBotBuilder OnDeath(Action<DeathEvent> handler) { _onDeath = handler; return this; }
    
    public BaseBot Build()
    {
        var botInfo = BotInfo.Builder()
            .SetName(_name)
            .SetVersion(_version)
            .AddAuthor("Test")
            .Build();
        return new ConfigurableTestBot(botInfo, MockedServer.ServerUrl,
            _onRun, _onScannedBot, _onHitByBullet, _onBulletHit, _onDeath);
    }
    
    private class ConfigurableTestBot : BaseBot
    {
        private readonly Action _onRunHandler;
        private readonly Action<ScannedBotEvent> _onScannedBotHandler;
        private readonly Action<HitByBulletEvent> _onHitByBulletHandler;
        private readonly Action<BulletHitBotEvent> _onBulletHitHandler;
        private readonly Action<DeathEvent> _onDeathHandler;
        
        public ConfigurableTestBot(BotInfo botInfo, Uri serverUrl,
            Action onRun,
            Action<ScannedBotEvent> onScannedBot,
            Action<HitByBulletEvent> onHitByBullet,
            Action<BulletHitBotEvent> onBulletHit,
            Action<DeathEvent> onDeath) : base(botInfo, serverUrl)
        {
            _onRunHandler = onRun;
            _onScannedBotHandler = onScannedBot;
            _onHitByBulletHandler = onHitByBullet;
            _onBulletHitHandler = onBulletHit;
            _onDeathHandler = onDeath;
        }
        
        public override void Run() => _onRunHandler();
        public override void OnScannedBot(ScannedBotEvent e) => _onScannedBotHandler(e);
        public override void OnHitByBullet(HitByBulletEvent e) => _onHitByBulletHandler(e);
        public override void OnBulletHitBot(BulletHitBotEvent e) => _onBulletHitHandler(e);
        public override void OnDeath(DeathEvent e) => _onDeathHandler(e);
    }
}
```

### Python Implementation

```python
class TestBotFactory:
    """Factory for creating configurable test bots."""
    
    def __init__(self, server):
        self.server = server
        self._name = "TestBot"
        self._version = "1.0"
        self._on_run = lambda: None
        self._on_scanned_bot = lambda e: None
        self._on_hit_by_bullet = lambda e: None
        self._on_bullet_hit = lambda e: None
        self._on_death = lambda e: None
    
    @classmethod
    def create(cls, server):
        return cls(server)
    
    def with_name(self, name):
        self._name = name
        return self
    
    def with_version(self, version):
        self._version = version
        return self
    
    def on_run(self, handler):
        self._on_run = handler
        return self
    
    def on_scanned_bot(self, handler):
        self._on_scanned_bot = handler
        return self
    
    def on_hit_by_bullet(self, handler):
        self._on_hit_by_bullet = handler
        return self
    
    def on_bullet_hit(self, handler):
        self._on_bullet_hit = handler
        return self
    
    def on_death(self, handler):
        self._on_death = handler
        return self
    
    def build(self):
        bot_info = BotInfo(
            name=self._name,
            version=self._version,
            authors=["Test"],
            description="Test bot",
            homepage="",
            country_codes=[],
            game_types=["classic"],
            platform="Python",
            programming_lang="Python 3.x",
            initial_position=None
        )
        return ConfigurableTestBot(
            bot_info, self.server.server_url,
            self._on_run, self._on_scanned_bot, self._on_hit_by_bullet,
            self._on_bullet_hit, self._on_death
        )


class ConfigurableTestBot(BaseBot):
    """Test bot with configurable event handlers."""
    
    def __init__(self, bot_info, server_url, on_run, on_scanned_bot,
                 on_hit_by_bullet, on_bullet_hit, on_death):
        super().__init__(bot_info, server_url)
        self._on_run_handler = on_run
        self._on_scanned_bot_handler = on_scanned_bot
        self._on_hit_by_bullet_handler = on_hit_by_bullet
        self._on_bullet_hit_handler = on_bullet_hit
        self._on_death_handler = on_death
    
    def run(self):
        self._on_run_handler()
    
    def on_scanned_bot(self, event):
        self._on_scanned_bot_handler(event)
    
    def on_hit_by_bullet(self, event):
        self._on_hit_by_bullet_handler(event)
    
    def on_bullet_hit_bot(self, event):
        self._on_bullet_hit_handler(event)
    
    def on_death(self, event):
        self._on_death_handler(event)
```

### Usage Examples

#### Simple Passive Bot

```java
BaseBot bot = TestBotBuilder.create(server)
    .withName("PassiveBot")
    .build();
```

#### Bot That Fires on Scan

```java
BaseBot bot = TestBotBuilder.create(server)
    .withName("AggressiveBot")
    .onScannedBot(e -> {
        fire(1.0);
    })
    .build();
```

#### Bot for Testing Specific Behavior

```java
AtomicBoolean scanned = new AtomicBoolean(false);
BaseBot bot = TestBotBuilder.create(server)
    .onScannedBot(e -> scanned.set(true))
    .build();

// ... run test ...

assertTrue(scanned.get());
```
