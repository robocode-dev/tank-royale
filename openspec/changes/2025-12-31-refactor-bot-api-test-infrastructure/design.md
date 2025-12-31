# Design: Bot API Test Infrastructure Refactoring

## Overview

This design addresses the root cause of test complexity: the current MockedServer is a **message-capture framework**
optimized for protocol validation, not a **state-driven testing framework** optimized for behavior testing.

## Problem Analysis

### Current Architecture Issues

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Test      │ manual  │  MockedServer│ async   │    Bot      │
│   Code      │───────→ │  (WebSocket) │←──────→ │  Instance   │
└─────────────┘  steps  └──────────────┘  msgs   └─────────────┘
      │                         │                        │
      │ 1. server.setEnergy(5)  │                        │
      │ 2. server.sendTick()    │                        │
      │ 3. sleep(100)           │                        │
      │ 4. bot.setFire(1.0)     │                        │
      │ 5. bot.go()             │───────intent──────────→│
      │ 6. awaitIntent()        │                        │
      │ 7. assert intent        │                        │
      └─────────────────────────┴────────────────────────┘
```

**Problems**:

1. **7-step process** for a simple assertion
2. **Race conditions** between steps 2-4 (did bot process the tick?)
3. **No guarantee** that `bot.getEnergy()` returns 5 when `setFire()` is called
4. **Timeout ambiguity**: Is the bot stuck, or just slow?

### Desired Architecture

```
┌─────────────┐         ┌──────────────┐         ┌─────────────┐
│   Test      │  sync   │  Enhanced    │  sync   │    Bot      │
│   Code      │───────→ │  MockedServer│←──────→ │  Instance   │
└─────────────┘  API    └──────────────┘  coord  └─────────────┘
      │                         │                        │
      │ execute(() => {         │                        │
      │   return bot.setFire(1.0);                      │
      │ })                      │                        │
      │  ↓ (internally handles all coordination)        │
      │ returns (false, intent) │                        │
      └─────────────────────────┴────────────────────────┘
```

**Benefits**:

1. **1-step process** from test perspective
2. **Guaranteed state** before command execution
3. **Clear semantics**: command result + captured intent in one call
4. **Deterministic**: No sleeps, no race conditions

## Core Design Patterns

### Pattern 1: State Synchronization

**Problem**: Test sets `server.setEnergy(5.0)`, but bot might not see it yet.

**Solution**: Atomic "set-and-sync" operations.

```java
// Instead of:
server.setEnergy(5.0);
server.

sendTick();
Thread.

sleep(100); // hope the bot processed it
bot.

setFire(1.0);

// Use:
server.

setBotStateAndAwaitTick(
    energy:5.0,
    gunHeat:0.0,
    /* other fields use defaults */
);

boolean fired = bot.setFire(1.0); // Guaranteed to see energy=5.0
```

**Implementation**:

1. Update MockedServer internal state
2. Send TickEventForBot with new state
3. Block until bot has processed the tick (captured via BotReady or next BotIntent)
4. Return control to test

### Pattern 2: Command Execution with Intent Capture

**Problem**: Blocking methods like `fire()` and `rescan()` call `bot.go()` internally, creating async complexity.

**Solution**: Wrapper that handles async coordination transparently.

```java
// Instead of:
final boolean[] result = new boolean[1];
Thread thread = new Thread(() -> {
    result[0] = bot.setFire(1.0);
    bot.go();
});
thread.start();
Thread.sleep(100);
awaitBotIntent();
assertTrue(result[0]);

// Use:
var (fired, intent) = executeCommand(() -> bot.setFire(1.0));
assertTrue(fired);
assertEquals(1.0, intent.getFirepower());
```

**Implementation**:

```java
protected <T> CommandResult<T> executeCommand(Supplier<T> command) {
    server.resetBotIntentEvent();
    T result = command.get();
    bot.go(); // Trigger intent submission
    awaitBotIntent(); // Wait for intent capture
    return new CommandResult<>(result, server.getBotIntent());
}
```

### Pattern 3: Blocking Method Execution

**Problem**: Methods like `fire()` and `rescan()` are blocking and call `go()` internally.

**Solution**: Dedicated helper that spawns command in background.

```java
// Instead of:
Thread thread = new Thread(() -> {
    bot.fire(1.0);
});
thread.start();
Thread.sleep(100);
awaitBotIntent();

// Use:
BotIntent intent = executeBlocking(() -> bot.fire(1.0));
assertNotNull(intent.getFirepower());
```

**Implementation**:

```java
protected BotIntent executeBlocking(Runnable blockingCommand) {
    server.resetBotIntentEvent();
    CompletableFuture.runAsync(blockingCommand);
    Thread.sleep(50); // Small delay to ensure command starts
    awaitBotIntent();
    return server.getBotIntent();
}
```

## API Specifications

### MockedServer Enhancements

#### Java (`MockedServer.java`)

```java
public class MockedServer {
    // Existing methods...
    
    /**
     * Block until the bot has processed the handshake and is ready to accept commands.
     * Ensures that bot.getGameType(), bot.getEnergy(), etc. will not throw BotException.
     */
    public boolean awaitBotReady(int milliSeconds) {
        if (!awaitBotHandshake(milliSeconds)) return false;
        if (!awaitGameStarted(milliSeconds)) return false;
        if (!awaitTick(milliSeconds)) return false;
        return true;
    }
    
    /**
     * Atomically set bot state fields and send a tick, blocking until the bot has processed it.
     * 
     * @param energy Bot energy (null to keep current)
     * @param gunHeat Gun heat (null to keep current)
     * @param speed Bot speed (null to keep current)
     * @param direction Bot direction (null to keep current)
     * @param gunDirection Gun direction (null to keep current)
     * @param radarDirection Radar direction (null to keep current)
     * @return true if tick was processed within timeout
     */
    public boolean setBotStateAndAwaitTick(
            Double energy,
            Double gunHeat,
            Double speed,
            Double direction,
            Double gunDirection,
            Double radarDirection) {
        // Update internal state
        if (energy != null) setEnergy(energy);
        if (gunHeat != null) setGunHeat(gunHeat);
        if (speed != null) setSpeed(speed);
        if (direction != null) this.direction = direction;
        if (gunDirection != null) this.gunDirection = gunDirection;
        if (radarDirection != null) this.radarDirection = radarDirection;
        
        // Reset tick latch before sending
        tickEventLatch = new CountDownLatch(1);
        
        // Send tick (done in response to BotReady or BotIntent in actual impl)
        // For this method, we need to trigger a tick send
        // Implementation depends on current message handling flow
        
        // Wait for bot to process
        return awaitTick(1000);
    }
}
```

#### .NET (`MockedServer.cs`)

```csharp
public class MockedServer {
    // Existing methods...
    
    /// <summary>
    /// Block until the bot has processed the handshake and is ready to accept commands.
    /// </summary>
    public bool AwaitBotReady(int timeoutMs = 1000) {
        if (!AwaitBotHandshake(timeoutMs)) return false;
        if (!AwaitGameStarted(timeoutMs)) return false;
        if (!AwaitTick(timeoutMs)) return false;
        return true;
    }
    
    /// <summary>
    /// Atomically set bot state fields and send a tick, blocking until processed.
    /// </summary>
    public bool SetBotStateAndAwaitTick(
        double? energy = null,
        double? gunHeat = null,
        double? speed = null,
        double? direction = null,
        double? gunDirection = null,
        double? radarDirection = null) {
        
        if (energy.HasValue) SetEnergy(energy.Value);
        if (gunHeat.HasValue) SetGunHeat(gunHeat.Value);
        if (speed.HasValue) SetSpeed(speed.Value);
        if (direction.HasValue) _direction = direction.Value;
        if (gunDirection.HasValue) _gunDirection = gunDirection.Value;
        if (radarDirection.HasValue) _radarDirection = radarDirection.Value;
        
        _tickEventLatch = new CountDownLatch(1);
        
        // Trigger tick send (implementation specific)
        
        return AwaitTick(1000);
    }
}
```

#### Python (`mocked_server.py`)

```python
class MockedServer:
    # Existing methods...
    
    def await_bot_ready(self, timeout_ms: int = 1000) -> bool:
        """Block until bot has processed handshake and is ready for commands."""
        if not self.await_bot_handshake(timeout_ms):
            return False
        if not self.await_game_started(timeout_ms):
            return False
        if not self.await_tick(timeout_ms):
            return False
        return True
    
    def set_bot_state_and_await_tick(
        self,
        energy: Optional[float] = None,
        gun_heat: Optional[float] = None,
        speed: Optional[float] = None,
        direction: Optional[float] = None,
        gun_direction: Optional[float] = None,
        radar_direction: Optional[float] = None
    ) -> bool:
        """Atomically set bot state fields and send a tick, blocking until processed."""
        if energy is not None:
            self._energy = energy
        if gun_heat is not None:
            self._gun_heat = gun_heat
        if speed is not None:
            self._speed = speed
        if direction is not None:
            self._direction = direction
        if gun_direction is not None:
            self._gun_direction = gun_direction
        if radar_direction is not None:
            self._radar_direction = radar_direction
        
        self._tick_event.clear()
        
        # Trigger tick send (implementation specific)
        
        return self.await_tick(1000)
```

### AbstractBotTest Utilities

#### Java (`AbstractBotTest.java` - new file)

```java
package test_utils;

import dev.robocode.tankroyale.botapi.BaseBot;
import dev.robocode.tankroyale.schema.BotIntent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertTrue;

public abstract class AbstractBotTest {
    protected MockedServer server;
    protected BaseBot bot;
    
    @BeforeEach
    public void setUp() {
        server = new MockedServer();
        server.start();
    }
    
    @AfterEach
    public void tearDown() {
        if (bot != null) {
            server.closeConnections();
        }
        server.stop();
    }
    
    protected BaseBot startBot() {
        bot = createTestBot();
        CompletableFuture.runAsync(bot::start);
        assertTrue(server.awaitBotReady(2000), "Bot failed to become ready");
        return bot;
    }
    
    protected abstract BaseBot createTestBot();
    
    protected void awaitBotIntent() {
        assertTrue(server.awaitBotIntent(1000), "Bot intent not received");
    }
    
    /**
     * Execute a command that returns a value and capture the resulting intent.
     * Automatically calls go() and waits for intent.
     */
    protected <T> CommandResult<T> executeCommand(Supplier<T> command) {
        server.resetBotIntentLatch();
        T result = command.get();
        CompletableFuture.runAsync(bot::go);
        awaitBotIntent();
        return new CommandResult<>(result, server.getBotIntent());
    }
    
    /**
     * Execute a blocking command (like fire() or rescan()) and capture intent.
     * The command runs in a background thread.
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
     */
    public static class CommandResult<T> {
        public final T result;
        public final BotIntent intent;
        
        public CommandResult(T result, BotIntent intent) {
            this.result = result;
            this.intent = intent;
        }
    }
}
```

#### .NET (`AbstractBotTest.cs` - enhancement)

```csharp
public abstract class AbstractBotTest {
    // Existing code...
    
    /// <summary>
    /// Execute a command that returns a value and capture the resulting intent.
    /// </summary>
    protected (T result, BotIntent intent) ExecuteCommand<T>(Func<T> command) {
        Server.ResetBotIntentEvent();
        var result = command();
        GoAsync(bot);
        AwaitBotIntent();
        return (result, Server.BotIntent);
    }
    
    /// <summary>
    /// Execute a void command and capture the resulting intent.
    /// </summary>
    protected BotIntent ExecuteAndCaptureIntent(Action command) {
        Server.ResetBotIntentEvent();
        command();
        GoAsync(bot);
        AwaitBotIntent();
        return Server.BotIntent;
    }
    
    /// <summary>
    /// Execute a blocking command (like Fire() or Rescan()) and capture intent.
    /// </summary>
    protected BotIntent ExecuteBlocking(Action blockingCommand) {
        Server.ResetBotIntentEvent();
        var task = Task.Run(blockingCommand);
        Thread.Sleep(50); // Small delay for command to start
        AwaitBotIntent();
        return Server.BotIntent;
    }
}
```

#### Python (`abstract_bot_test.py` - new file)

```python
import threading
import time
from typing import Any, Callable, Optional, Tuple
from robocode_tank_royale.bot_api import BaseBot, BotInfo
from test_utils.mocked_server import MockedServer


class AbstractBotTest:
    """Base class for Bot API tests with MockedServer."""
    
    def setup_method(self):
        """Setup before each test method."""
        self.server = MockedServer()
        self.server.start()
        self.bot: Optional[BaseBot] = None
    
    def teardown_method(self):
        """Teardown after each test method."""
        if self.bot:
            self.server.close_connections()
        self.server.stop()
    
    def start_bot(self) -> BaseBot:
        """Start a test bot and wait for it to be ready."""
        self.bot = self.create_test_bot()
        self._start_async(self.bot)
        assert self.server.await_bot_ready(2000), "Bot failed to become ready"
        return self.bot
    
    def create_test_bot(self) -> BaseBot:
        """Override to provide test bot instance."""
        raise NotImplementedError("Subclass must implement create_test_bot()")
    
    def _start_async(self, bot: BaseBot):
        """Start bot in background thread."""
        thread = threading.Thread(target=bot.start, daemon=True)
        thread.start()
    
    def _go_async(self, bot: BaseBot):
        """Call bot.go() in background thread."""
        thread = threading.Thread(target=bot.go, daemon=True)
        thread.start()
    
    def await_bot_intent(self):
        """Wait for bot intent to be captured."""
        assert self.server.await_bot_intent(1000), "Bot intent not received"
    
    def execute_command(self, command_fn: Callable[[], Any]) -> Tuple[Any, Any]:
        """
        Execute a command that returns a value and capture the resulting intent.
        
        Returns:
            Tuple of (command_result, bot_intent)
        """
        self.server.reset_bot_intent_event()
        result = command_fn()
        self._go_async(self.bot)
        self.await_bot_intent()
        return result, self.server._bot_intent
    
    def execute_and_capture_intent(self, command_fn: Callable[[], None]) -> Any:
        """
        Execute a void command and capture the resulting intent.
        
        Returns:
            The captured bot_intent
        """
        self.server.reset_bot_intent_event()
        command_fn()
        self._go_async(self.bot)
        self.await_bot_intent()
        return self.server._bot_intent
    
    def execute_blocking(self, blocking_command: Callable[[], None]) -> Any:
        """
        Execute a blocking command (like fire() or rescan()) and capture intent.
        
        Returns:
            The captured bot_intent
        """
        self.server.reset_bot_intent_event()
        thread = threading.Thread(target=blocking_command, daemon=True)
        thread.start()
        time.sleep(0.05)  # Small delay for command to start
        self.await_bot_intent()
        return self.server._bot_intent
```

## Test Examples

### Fire Command Test (Java)

```java
@Test
@DisplayName("TR-API-CMD-002 Fire cooldown prevents firing")
void test_TR_API_CMD_002_fire_cooldown() {
    // Arrange
    BaseBot bot = startBot();
    server.setBotStateAndAwaitTick(
        /* energy */ 100.0,
        /* gunHeat */ 5.0,  // Gun is hot!
        /* speed */ null,
        /* direction */ null,
        /* gunDirection */ null,
        /* radarDirection */ null
    );
    
    // Act
    var result = executeCommand(() -> bot.setFire(1.0));
    
    // Assert
    assertFalse(result.result, "setFire should return false when gun is cooling");
    assertNull(result.intent.getFirepower(), "Firepower should not be set in intent");
}
```

### Radar Test (C#)

```csharp
[Test]
[Description("TR-API-CMD-003 Blocking rescan")]
public void Test_TR_API_CMD_003_Blocking_Rescan() {
    // Arrange
    var bot = Start();
    AwaitBotHandshake();
    AwaitGameStarted(bot);
    
    // Act
    var intent = ExecuteBlocking(() => bot.Rescan());
    
    // Assert
    Assert.That(intent, Is.Not.Null);
    Assert.That(intent.Rescan, Is.True);
}
```

### Fire Test (Python)

```python
def test_TR_API_CMD_002_fire_energy_limit():
    """TR-API-CMD-002 Fire energy limit prevents firing"""
    # Arrange
    test = AbstractBotTest()
    test.setup_method()
    bot = test.start_bot()
    test.server.set_bot_state_and_await_tick(
        energy=1.0,  # Low energy!
        gun_heat=0.0
    )
    
    # Act
    fired, intent = test.execute_command(lambda: bot.set_fire(3.0))
    
    # Assert
    assert not fired, "setFire should return False when energy < firepower"
    assert intent.firepower is None, "Firepower should not be set in intent"
```

## Migration Strategy

### Phase 1: Add New APIs (Non-Breaking)

1. Add `awaitBotReady()` / `AwaitBotReady()` / `await_bot_ready()` to MockedServer
2. Add `setBotStateAndAwaitTick()` / etc. to MockedServer
3. Create AbstractBotTest base classes with helper methods
4. Verify new APIs work with simple smoke tests

### Phase 2: Implement New Tests

1. Create CommandsFireTest in all three languages
2. Implement all TR-API-CMD-002 sub-tests using new patterns
3. Verify tests pass and are stable

### Phase 3: Refactor Existing Tests

1. Start with CommandsRadarTest (already partially done in C#)
2. Apply new patterns one test at a time
3. Keep old code commented out initially
4. Remove old code once new version is verified

### Phase 4: Documentation

1. Create TESTING-GUIDE.md with patterns and examples
2. Update TEST-MATRIX.md
3. Add inline comments to key test methods

## Open Questions

1. **State injection timing**: Should `setBotStateAndAwaitTick()` send a tick immediately, or only on next bot action?
    - **Decision**: Send immediately for predictability

2. **Python threading model**: Python's GIL may affect threading patterns.
    - **Decision**: Use threading.Thread for simplicity; monitor for issues

3. **Test isolation**: Should each test get a fresh MockedServer instance?
    - **Decision**: Yes, via setUp/tearDown in base class

4. **Timeout values**: Should we use configurable timeouts?
    - **Decision**: Start with hardcoded reasonable values (1000ms); make configurable if needed

## Alternatives Considered

### Alternative 1: Full Mock Framework

Replace MockedServer with a full mocking framework (Mockito/Moq/unittest.mock).

**Pros**: Mature tooling, familiar patterns
**Cons**:

- Doesn't address WebSocket async nature
- Still requires significant test code
- Less control over message timing

**Decision**: Not chosen - MockedServer provides better control for our specific needs

### Alternative 2: Recording/Replay Pattern

Record a sequence of messages and replay them.

**Pros**: Deterministic, easy to version control
**Cons**:

- Harder to test conditional logic
- Brittle to small protocol changes
- Doesn't test bot's actual decision-making

**Decision**: Not chosen - too rigid for behavior testing

### Alternative 3: Integration Tests Only

Skip unit tests, only test against real server.

**Pros**: Tests real behavior
**Cons**:

- Slow
- Hard to test edge cases
- Requires server setup
- Flaky

**Decision**: Not chosen - need fast, reliable unit tests

